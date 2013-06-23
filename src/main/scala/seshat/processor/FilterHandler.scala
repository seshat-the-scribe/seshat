package seshat.processor

import seshat.{Kaboom, Event, SeshatConfig}
import seshat.plugin.{FilterPlugin, PluginConfig, PluginDescriptor}

import akka.actor.{ActorLogging, Actor, ActorRef}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/** Composes together a set of filter functions and asks for events from the input.
  * It maintains a queue of already filtered events and responds to GetEvents by sending
  * those events back.
  */
class FilterHandler( val input: ActorRef, val config: SeshatConfig, val descriptors: Seq[PluginDescriptor] )
    extends Actor
    with AskAgainProtocol
    with ActorLogging {

  // FIXME configure a dispatcher
  implicit val exCtx = context.dispatcher

  /** Private message protocol */
  private object Msg {
    case class Filtered(event: Event)
    case class Failed(event: Event, cause: Throwable)
  }

  private val filteredEvents = collection.mutable.Queue[Event]()
  private val filterPipeline = buildPipeline

  def receive: Actor.Receive =  defaultHandler orElse askAgainHandler

  def defaultHandler: Receive = {

    case Processor.Common.Events(events)  =>
      if (events.size > 0) runFilters(events)
      else scheduleAsk(input, Processor.Common.GetEvents)

    case Processor.Common.GetEvents =>
      if( filteredEvents.size > 0 ) sendEvents(sender)
      else sender ! Processor.Common.Events(Seq.empty)

    case Msg.Filtered(e)  =>
      filteredEvents enqueue e
      if (filteredEvents.size <= config.queueSize )
        input ! Processor.Common.GetEvents

  }

  private def runFilters(events: Seq[Event]) {
    resetRetries()
    // and run
    // FIXME rate limit
    events foreach ( event =>
      Future{ filterPipeline(event) }
        .onComplete {
          case Success(e:Event) => self ! Msg.Filtered(e)
          case Failure(ex)      => self ! Msg.Failed(event,ex)
        }
    )
  }

  // FIXME move to trait, to be shared by all handlers.
  private def sendEvents(sndr: ActorRef) {

    val size =
      if (filteredEvents.size >= config.queueSize)
        config.queueSize
      else
        filteredEvents.size

    val events =
      (1 to size).map( _ => filteredEvents.dequeue() )

    sndr ! Processor.Common.Events(events)

  }

  private def instance(descriptor: PluginDescriptor, config: PluginConfig): FilterPlugin =
    descriptor.clazz.getConstructor(classOf[PluginConfig])
      .newInstance(config)
      .asInstanceOf[FilterPlugin]


  private def buildPipeline: (Event => Event) = {
    type FP = ( Event => Event )
    val filters = config.filters.map{ cfg =>
      descriptors.find(_.name == cfg.name).fold ( throw Kaboom(s"No plugins defined for config $cfg")) (
        descr => instance(descr, cfg) : FP // type ascription to help the inferencer.
      )
    }
    filters.tail.foldLeft(filters.head){ (a,e) => a andThen e }
  }

}
