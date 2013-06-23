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
      descr => spawnOutputActor(descr, cfg)
    )
  }

  def receive: Receive = defaultHandler orElse askAgainHandler

  def defaultHandler: Receive = {

    case Processor.Common.Events(events) =>
      if (events.size > 0) stashedEvents enqueue (events: _*)
      else scheduleAsk(filter, Processor.Common.GetEvents)

    case Processor.Common.GetEvents =>
      if( stashedEvents(sender).size > 0 ) sendEvents(sender)
      else sender ! Processor.Common.Events(Seq.empty)

  }

  private def sendEvents(sndr: ActorRef) {

    val size =
      if (stashedEvents.size >= config.queueSize)
        config.queueSize
      else
        stashedEvents.size

    val events =
      (1 to size).map( _ => stashedEvents.dequeue() )

    sndr ! Processor.Common.Events(events)

  }

  def spawnOutputActor(descriptor: PluginDescriptor, config: PluginConfig): ActorRef =
    watch(
      actorOf( Props(descriptor.clazz, config ), descriptor.name )
    )




}
