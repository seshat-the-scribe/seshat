package seshat

import seshat.plugin._

import akka.actor._
import seshat.plugin.PluginConfig
import seshat.plugin.Plugins
import seshat.plugin.PluginDescriptor


/**
 * Companion Module of the [[seshat.Processor]] class
 *
 * The [[seshat.Processor.Msg]] object defines the msg protocol.
 *
 */
object Processor {

  object Msg {
    case object   Start // Tell inputs to start.
    case object   Stop  // Tell every one to stop
    case object   GetEvents
    case class    Events(events: Seq[Event])
  }

}

/**
 * Controls the creation of inputs, filters and outputs and acts as controller for the
 * whole thing.
 */
class Processor( val config: SeshatConfig, val plugins: Plugins )
  extends Actor
     with ActorLogging {

  import Processor.Msg
  import context.{actorOf,watch}

  def receive: Receive = {
    case Msg.Start  => start()
    case Msg.Stop   => stop()
  }

  def start() {
    inputHandler ! Processor.Msg.Start
  }
  def stop()  {}

  val inputHandler = watch(actorOf(
    Props(classOf[InputHandler], config, plugins.inputs), "INPUT_HANDLER")
  )

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: Exception =>
      log.error(e, "A Child Failed")
      context.stop(self)
      context.system.shutdown()
      SupervisorStrategy.Escalate
  }

}



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
    descriptors.find(_.name == cfg.name).fold (
      throw Kaboom(s"No plugins defined for config $cfg")
    ) (
      descr => spawn(descr,cfg)
    )
  }

  def receive: Actor.Receive = {
    case Processor.Msg.Start      => inputs foreach ( _ ! InputPlugin.Msg.Start )
    case Processor.Msg.Stop       => inputs foreach ( _ ! InputPlugin.Msg.Stop  )
    case Processor.Msg.Events(es) => receivedEvents.enqueue(es : _*)
    case Processor.Msg.GetEvents  => if( receivedEvents.size > 0 ) sendEvents(sender)
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e:Exception =>
      log.error(e, "A child Failed")
      SupervisorStrategy.Escalate
  }


  /** Sends half our queue to the passed actorRef */
  private def sendEvents(requestor: ActorRef) {

    val size = if (receivedEvents.size >= config.queueSize)
      config.queueSize / 2
    else
      receivedEvents.size

    val events =
      (1 to size).map( _ => receivedEvents.dequeue() )

    requestor ! Processor.Msg.Events(events)

  }

  private def spawn(descriptor: PluginDescriptor, config: PluginConfig ): ActorRef =
    watch(
      actorOf( Props(descriptor.clazz, config ), descriptor.name )
    )

}

/** Composes together a set of filter functions and asks for events from the input.
  * It maintains a queue of already filtered events and responds to GetEvents by sending them
  * to the `sender`.
  */
class FilterSupervisor(val input: ActorRef,  val plugins: Seq[Plugin]  )
    extends Actor with ActorLogging {

  private val filteredEvents = collection.mutable.Queue[Event]()

  def receive: Actor.Receive = ???

}

class OutputSupervisor extends Actor{
  def receive: Actor.Receive = ???
}


