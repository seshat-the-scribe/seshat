package seshat.processor

import seshat.{Kaboom, Event, SeshatConfig}
import seshat.plugin.{FilterPlugin, PluginConfig, PluginDescriptor}

import akka.actor.{Terminated, ActorLogging, Actor, ActorRef}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/** Composes together a set of filter functions and asks for events from the input.
  * It maintains a queue of already filtered events and responds to NextBatch by sending
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

  def receive: Actor.Receive =  defaultHandler

  def defaultHandler: Receive = {

    case Processor.Msg.Start =>
      log.debug("Starting")
      input ! Processor.Internal.NextBatch

    case Processor.Internal.Batch(events)  =>
      log.debug(s"Received ${events.size} events")
      if (events.size > 0) runFilters(events)
      else scheduleAsk(input, Processor.Internal.NextBatch)

    case Processor.Internal.NextBatch =>
      log.debug(s"Got NextBatch from ${sender}")
      log.debug(s"Available events ${filteredEvents.size}")
      if( ! filteredEvents.isEmpty) {
        sendEvents(sender)
      }
      else {
        sender  ! Processor.Internal.Batch(Seq.empty)
        scheduleAsk(input, Processor.Internal.NextBatch)
      }


    case Msg.Failed(event, ex) =>
      log.warning(s"Got Msg.Failed for event $event with error $ex ")
      //runFilters(Seq(event))
      ex.printStackTrace()
      throw(ex)


    case Msg.Filtered(e) =>
      filteredEvents enqueue e
      if (filteredEvents.isEmpty) {
        input ! Processor.Internal.NextBatch
      }

  }

  private def runFilters(events: Seq[Event]) {
    log.debug("runFilters")
    // and run
    // FIXME rate limit
    resetRetries()
    val s = self
    events foreach ( event =>
     //try {
       //self ! Msg.Filtered(filterPipeline(event))
     //} catch {
       //case e: Exception => self ! Msg.Failed(event,e)
     //}

      Future{ filterPipeline(event) }
        .onComplete {
          case Success(e:Event) => s ! Msg.Filtered(e)
          case Failure(ex)      => s ! Msg.Failed(event,ex)
        }
    )
  }

  private def sendEvents(who: ActorRef) {
    log.debug("sendEvents")

    val size =
      if (filteredEvents.size >= config.queueSize)
        config.queueSize
      else
        filteredEvents.size

    val events =
      (1 to size).map( _ => filteredEvents.dequeue() )

    log.debug(s"Sending ${events.size} events to $who")
    who ! Processor.Internal.Batch(events)

  }

  private def instance(descriptor: PluginDescriptor, config: PluginConfig): FilterPlugin =
    descriptor.clazz.getConstructor(classOf[PluginConfig])
      .newInstance(config)
      .asInstanceOf[FilterPlugin]


  private def buildPipeline: (Event => Event) = {
    if( config.filters.size > 0  ){
      type FP = ( Event => Event )
      val filters = config.filters.map{ cfg =>
        descriptors.find(_.name == cfg.name).fold ( throw Kaboom(s"No plugins defined for config $cfg")) (
          descr => instance(descr, cfg) : FP // type ascription to help the inferencer.
        )
      }
      filters.tail.foldLeft(filters.head){ (a,e) => a andThen e }
    }
    else markEvent
  }

  private def markEvent(ev: Event): Event = {
    ev.copy(tags = ev.tags+"SESHAT_SEEN")
  }



}
