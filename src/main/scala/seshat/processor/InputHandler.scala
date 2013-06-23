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
class InputHandler( val config: SeshatConfig, val descriptors: Set[PluginDescriptor] )
  extends Actor with ActorLogging {

  import context.{actorOf,watch}

  private val receivedEvents = collection.mutable.Queue[Event]()

  private val inputs: Set[ActorRef] = config.inputs.map { cfg =>
    descriptors.find(_.name == cfg.name).fold ( throw Kaboom(s"No plugins defined for config $cfg") ) (
      descr => spawnInputActor(descr, cfg)
    )
  }

  def receive: Actor.Receive = {

    case Processor.Msg.Start =>
      inputs foreach ( _ ! InputPlugin.Msg.Start )

    case Processor.Msg.Stop =>
      inputs foreach ( _ ! InputPlugin.Msg.Stop  )

    case Processor.Common.Events(es) =>
      receivedEvents.enqueue(es : _*)

    case Processor.Common.GetEvents =>
      if( receivedEvents.size > 0 ) sendEvents(sender)
      else sender ! Processor.Common.Events(Seq.empty)

  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e:Exception =>
      log.error(e, "A child Failed")
      SupervisorStrategy.Escalate
  }


  /** Sends `config.queueSize` events to the passed actorRef */
  private def sendEvents(sndr: ActorRef) {

    // Up to queueSize because that is what the next guy is expecting.
    val size =
      if (receivedEvents.size >= config.queueSize)
        config.queueSize
      else
        receivedEvents.size

    val events =
      (1 to size).map( _ => receivedEvents.dequeue() )

    sndr ! Processor.Common.Events(events)

  }

  private def spawnInputActor(descriptor: PluginDescriptor, config: PluginConfig ): ActorRef =
    watch(
      actorOf( Props(descriptor.clazz, config ), descriptor.name )
    )

}
