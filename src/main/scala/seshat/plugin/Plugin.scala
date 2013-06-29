package seshat.plugin

import seshat._

import akka.actor.{Actor, ActorLogging, ActorRef}
import seshat.processor.{AskAgainProtocol, Processor}

/**
 *
 */
trait Plugin {
 val config: PluginConfig
}

object InputPlugin {
  object Msg {
    case class Throttle(millis:Option[Int])
  }
}

/**  An input plugin is an actor that reads events from a source and sends them to its parent.
  *
  *  It must support the messages [[seshat.processor.Processor.Msg.Start]],
  *  [[seshat.processor.Processor.Msg.Stop]] and [[seshat.plugin.InputPlugin.Msg.Throttle]].
  *
  *  An input plugin must start consuming input ONLY when the `Start` message is received and
  *  must stop when `Stop` is received.
  *
  *  An input plugin must send the `Event`s via [[seshat.processor.Processor.Internal.Batch]] messages with no more events than what
  *  [[seshat.SeshatConfig.queueSize]] indicates.
  *
  *  Start means reading the input or accepting connections and send `Batch` to the parent.
  *  Stops means stop reading but keep sending the remaining events when asked; no Batch should be dropped.
  *
  *  Input plugins should never `context.stop` themselves.
  *
  *  Input plugins are free to use postStop and postStart lifecycle events.
  *
  *  @param config configuration of the plugin
  *
*/
abstract class InputPlugin(val config:PluginConfig)
  extends Plugin with Actor with ActorLogging {

  protected var started = false

  final protected val defaultHandler: Receive  = {
    case Processor.Msg.Start    => start()
    case Processor.Msg.Stop     => stop()
    case InputPlugin.Msg.Throttle => throttle()
  }

  def receive: Receive = defaultHandler

  def start() {
    started = true
    log.debug("Started")
  }
  def stop()  {
    started = false
    log.debug("Stopped")
  }
  def throttle() {}


}

/**  A Filter is an  `Option[Event] => Option[Event]` function (or  `Function1[Option[Event],Option[Event]]`).
  *
  * It is created from a config and composed with other filter functions by a host actor.
  *
  *
  * @param config configuration of the plugin
  *
  */
abstract class FilterPlugin(val config: PluginConfig)
  extends Plugin with ( Event => Event )



/** An output plugin is an actor which accepts Start, Stop.
  *
  * They are created and handled by a Outputs actor which acts as a broadcaster and as a buffer.
  *
  * @param config
  */
abstract class OutputPlugin(val config: PluginConfig)
  extends Plugin
  with Actor
  with AskAgainProtocol
  with ActorLogging {



  final protected val defaultHandler: Receive  = {

    case Processor.Msg.Start => start()
    case Processor.Msg.Stop  => stop()

    case Processor.Internal.Batch(events) =>
      log.debug(s"Received ${events.size} events")
      if ( events.size > 0  ) {
        resetRetries()
        performOutput(events)
        scheduleAsk(context.parent, Processor.Internal.NextBatch)
      } else {
        scheduleAsk(context.parent, Processor.Internal.NextBatch)
      }

  }

  def receive: Receive = defaultHandler

  protected def start() {
    scheduleAsk(context.parent, Processor.Internal.NextBatch)
  }

  protected def stop() {}

  protected def performOutput(events: Seq[Event])



}

