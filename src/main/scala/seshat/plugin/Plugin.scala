package seshat.plugin

import seshat._

import akka.actor.{Actor, ActorLogging, ActorRef}

/**
 *
 */
trait Plugin {
 val config: PluginConfig
}


object InputPlugin {
  object Msg {
    case object Start
    case object Stop
    case object Throttle
  }
}

/**  An input plugin is an actor that reads events from a source and sends them to its parent.
 *
 *  It must support the messages [[seshat.plugin.InputPlugin.Msg.Start]],
 *  [[seshat.plugin.InputPlugin.Msg.Stop]] and [[seshat.plugin.InputPlugin.Msg.Throttle]].
 *
 *  An input plugin must start consuming input ONLY when the `Start` message is received and
 *  must stop when `Stop` is received.
 *
 *  Start means reading the input and send `Events` to the filter pipeline.
 *  Stops means stop reading and send the remaining events to the pipeline; no Events should be dropped.
 *
 *  Input plugins are free to use postStop and postStart lifecycle events.
 *
 *  @param config configuration of the plugin
 *  @param filterPipeline the configured filter pipeline.
 *
 */
abstract class InputPlugin(val config:PluginConfig)
  extends Plugin with Actor with ActorLogging {

  import InputPlugin.Msg

  def receive = {
    case Msg.Start    => start()
    case Msg.Stop     => stop()
    case Msg.Throttle => throttle()
  }

  def start(): Unit
  def stop(): Unit
  def throttle(): Unit

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
  extends Plugin with ( Option[Event] => Option[Event] )



/** An output plugin is an actor which accepts Start, Stop and Event messages.
  *
  * They are created and handled by a Outputs actor which acts as a broadcaster and as a buffer.
  *
  * @param config
  */
abstract class OutputPlugin(val config: PluginConfig)  extends Plugin

