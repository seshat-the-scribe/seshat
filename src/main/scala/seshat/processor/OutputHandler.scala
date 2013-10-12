package seshat.processor

import akka.actor.{Props, ActorLogging, ActorRef, Actor}
import seshat.{Kaboom, Event, SeshatConfig}
import seshat.plugin.{PluginConfig, PluginDescriptor}
import scala.concurrent.ExecutionContext

/** Coordinates output plugins, gets filtered events and sends them to the outputs.
  *
  * Created by f on 6/22/13.
  *
  */
class OutputHandler( val filter: ActorRef, val config: SeshatConfig, val descriptors: Seq[PluginDescriptor] )
  extends Actor
  with ActorLogging
  with AskAgainProtocol {

  import context.{watch,actorOf}

  // FIXME add a config with own dispatcher
  implicit val exCtx: ExecutionContext = context.dispatcher

  private val stashedEvents = collection.mutable.Map[ActorRef, Seq[Event]]()

  private val outputs: Set[ActorRef] = config.outputs.map { cfg =>
    descriptors.find(_.name == cfg.name).fold ( throw Kaboom(s"No plugins defined for config $cfg") ) (
      descr => {
        val o = spawnOutputActor(descr, cfg)
        stashedEvents.put(o, Seq()) // look ma! ugly side effect.
        o
      }
    )
  }

  def receive: Receive = defaultHandler

  private def defaultHandler: Receive = {

    case Processor.Msg.Start =>
      log.debug("Starting")
      outputs foreach ( _ ! Processor.Msg.Start )
      scheduleAsk(filter, Processor.Internal.NextBatch)

    case Processor.Msg.Stop =>
      log.debug("Stopping")
      outputs foreach ( _ ! Processor.Msg.Stop  )

    case Processor.Internal.Batch(events) =>
      log.debug(s"Got Batch with ${events.size} events")
      if (events.size > 0) {
        resetRetries()
        storeEvents(events)
        if ( storeAvailable  ) {
          filter ! Processor.Internal.NextBatch
        }
      }
      else scheduleAsk(filter, Processor.Internal.NextBatch)

    case Processor.Internal.NextBatch =>
      log.debug(s"Got NextBatch from $sender" )
      if( stashedEvents(sender).size > 0 ) {
        sendEvents(sender)
        if ( storeAvailable ) {
          filter ! Processor.Internal.NextBatch
        }
      }
      else {
        sender ! Processor.Internal.Batch(Seq.empty)
        scheduleAsk(filter, Processor.Internal.NextBatch)
      }

  }

  private def storeAvailable = {
    log.debug(s"Checking storage for all output")
    val available = stashedEvents forall ({case (k,es) => es.size < config.queueSize })
    log.debug(s"Is there available storage? $available")
    available
  }

  /** Copy events to each output's storage */
  private def storeEvents(events: Seq[Event]) {
    log.debug(s"Storing events in output specific storage")
    outputs foreach { output =>
      val previous = stashedEvents.getOrElseUpdate(output, Seq())
      stashedEvents.put(output,events++previous)
    }
    logStorageSizes()
  }

  /**  Takes `config.queueSize` messages from the `who`'s storage
    *  and sends it.
    */
  private def sendEvents(who: ActorRef) {

    // if for some reason there is no
    //  storage for this actorRef then blow up
    val allEvents = stashedEvents(who)

    val size =
      if (allEvents.size >= config.queueSize)
        config.queueSize
      else
        allEvents.size

    val (nextBatch, remaining) = allEvents splitAt size

    stashedEvents.put(who,remaining)

    who ! Processor.Internal.Batch(nextBatch)

  }

  private def logStorageSizes() {
    log.debug("Stashed events sizes")
    stashedEvents foreach { case (k,v) =>
      log.debug(s"Output $k -> ${v.size}")
    }
  }

  private def spawnOutputActor(descriptor: PluginDescriptor, config: PluginConfig): ActorRef =
    watch(
      actorOf( Props(descriptor.clazz, config ), descriptor.name )
    )

}
