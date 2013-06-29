package seshat.processor

import seshat.Kaboom
import seshat.plugin.InputPlugin
import akka.actor._
import seshat.plugin.PluginConfig
import seshat.Event
import seshat.plugin.PluginDescriptor
import akka.actor.OneForOneStrategy
import seshat.SeshatConfig

/** Supervises the input plugins and maintains a queue of events.
  *
  * Filter plugins are supposed to ask for data when they are ready.
  *
  */
class InputHandler( val config: SeshatConfig, val descriptors: Seq[PluginDescriptor] )
  extends Actor with ActorLogging {

  import context.{actorOf, watch}

  // FIXME change to var x:List[Event]
  private val receivedEvents = collection.mutable.Queue[Event]()

  private val inputs: Set[ActorRef] = config.inputs.map { cfg =>
    descriptors.find(_.name == cfg.name).fold ( throw Kaboom(s"No plugins defined for config $cfg") ) (
      descr => spawnInputActor(descr, cfg)
    )
  }

  def receive: Receive = {

    case Processor.Msg.Start =>
      log.debug("Starting inputs")
      inputs foreach ( _ ! Processor.Msg.Start )

    case Processor.Msg.Stop =>
      log.debug("Stopping inputs")
      inputs foreach ( _ ! Processor.Msg.Stop  )

    case Processor.Common.Events(es) =>
      // FIXME Check the soft limit.
      log.debug(s"Got Processor.Common.Events(${es.size})")
      receivedEvents.enqueue(es : _*)
      if( receivedEvents.size > config.queueSize ) {
        inputs foreach { _ ! Processor.Msg.Stop }
      if( receivedEvents.size > config.queueSize*1.5 ) {
        //FIXME use statistics to determine throttle time
        inputs foreach { _ ! InputPlugin.Msg.Throttle(Some(100)) }
      }
      log.debug(s"Received events queue size ${receivedEvents.size}")

    case Processor.Common.GetEvents =>
      log.debug(s"Got Processor.Common.GetEvents from $sender")
      if( receivedEvents.size > 0 ) sendEvents(sender)
      else sender ! Processor.Common.Events(Seq.empty)

  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e:Exception =>
      log.error(e, "A child Failed")
      SupervisorStrategy.Escalate
  }


  /** Sends `config.queueSize` events to the passed actorRef */
  private def sendEvents(who: ActorRef) {

    log.debug("sendEvents")
    // Up to queueSize because that is what the next guy is expecting.
    val size =
      if (receivedEvents.size >= config.queueSize)
        config.queueSize
      else
        receivedEvents.size

    val events =
      (1 to size).map( _ => receivedEvents.dequeue() )

    log.debug(s"Will send ${events.size} events")
    who ! Processor.Common.Events(events)

    if( receivedEvents.size < config.queueSize ) {
      inputs foreach { _ ! Processor.Msg.Start }
    }

  }

  private def spawnInputActor(descriptor: PluginDescriptor, config: PluginConfig ): ActorRef =
    watch(
      actorOf( Props(descriptor.clazz, config ), descriptor.name )
    )

}
