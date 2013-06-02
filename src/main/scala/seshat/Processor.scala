package seshat

import seshat.plugin._

import akka.actor._
import seshat.plugin.PluginConfig
import seshat.plugin.Plugins
import seshat.plugin.PluginDescriptor
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}


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
    // send to the rest of the handlers
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
    descriptors.find(_.name == cfg.name).fold ( throw Kaboom(s"No plugins defined for config $cfg")) (
      descr => spawnInputActor(descr,cfg)
    )
  }

  def receive: Actor.Receive = {
    case Processor.Msg.Start      => inputs foreach ( _ ! InputPlugin.Msg.Start )
    case Processor.Msg.Stop       => inputs foreach ( _ ! InputPlugin.Msg.Stop  )
    case Processor.Msg.Events(es) => receivedEvents.enqueue(es : _*)
    case Processor.Msg.GetEvents  => if( receivedEvents.size > 0 ) sendEvents(sender)
                                      else sender ! Processor.Msg.Events(Seq.empty)
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e:Exception =>
      log.error(e, "A child Failed")
      SupervisorStrategy.Escalate
  }


  /** Sends our queue to the passed actorRef */
  private def sendEvents(sndr: ActorRef) {

    val size = if (receivedEvents.size >= config.queueSize)
      config.queueSize
    else
      receivedEvents.size

    val events =
      (1 to size).map( _ => receivedEvents.dequeue() )

    sndr ! Processor.Msg.Events(events)

  }

  private def spawnInputActor(descriptor: PluginDescriptor, config: PluginConfig ): ActorRef =
    watch(
      actorOf( Props(descriptor.clazz, config ), descriptor.name )
    )

}

/** Composes together a set of filter functions and asks for events from the input.
  * It maintains a queue of already filtered events and responds to GetEvents by sending them
  * to the `sender`.
  */
class FilterSupervisor(val input: ActorRef, val config: SeshatConfig,  val descriptors: Seq[PluginDescriptor]  )
    extends Actor with ActorLogging {

  // FIXME configure a dispatcher
  implicit val exCtx = context.dispatcher


  /** Private message protocol */
  private object Msg {
    /** This message forces the sending of a GetEvents message to the input */
    case object AskAgain
    case class Filtered(event: Event)
    case class Failed(event: Event, cause: Throwable)
  }

  private val filteredEvents = collection.mutable.Queue[Event]()
  private val filterPipeline = buildPipeline


  def receive: Actor.Receive = {
    case Processor.Msg.Events(es) => if(es.size > 0 ) runFilters(es)
                                      else scheduleAsk
    case Processor.Msg.GetEvents  => if( filteredEvents.size > 0 ) sendEvents(sender)
    case Msg.Filtered(e)          => filteredEvents enqueue e
    case Msg.AskAgain             => input ! Processor.Msg.GetEvents
  }

  private def runFilters(es:Seq[Event]) {
    es foreach ( event =>
      Future{ filterPipeline(event) }
        .onComplete {
          case Success(e:Event) => self ! Msg.Filtered(e)
          case Failure(ex)      => self ! Msg.Failed(event,ex)
        }
    )
    input ! Processor.Msg.GetEvents
  }

  private def sendEvents(sndr:ActorRef) = ???

  private def scheduleAsk {
    // //FIXME Exponential backoff? ask once? an then?
    context.system.scheduler.scheduleOnce(100 millis, self, Msg.AskAgain )
  }

  private def instance(descriptor: PluginDescriptor, config: PluginConfig): FilterPlugin = {
    descriptor.clazz.getConstructor(classOf[PluginConfig])
      .newInstance(config)
      .asInstanceOf[FilterPlugin]
  }

  private def buildPipeline: (Event => Event) = {
    type FP = ( Event => Event )
    val filters = config.filters.map{ cfg =>
      descriptors.find(_.name == cfg.name).fold ( throw Kaboom(s"No plugins defined for config $cfg")) (
        descr => instance(descr, cfg) : FP
      )
    }
    filters.tail.foldLeft(filters.head){ (a,e) => a andThen e }
  }

}

class OutputSupervisor extends Actor{
  def receive: Actor.Receive = ???
}
