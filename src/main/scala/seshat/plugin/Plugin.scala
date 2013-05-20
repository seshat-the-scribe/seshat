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

/**  An input plugin is an actor that is built with  a config and an
 *  ActorRef to the filter actor.
 *
 *  It must support the messages [[seshat.plugin.InputPlugin.Msg.Start]],
 *  [[seshat.plugin.InputPlugin.Msg.Stop]] and [[seshat.plugin.InputPlugin.Msg.Throttle]].
 *
 *  An input plugins must start consuming input ONLY when the `Start` message is received and
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
abstract class InputPlugin(val config:PluginConfig, val filterPipeline: ActorRef)
  extends Plugin with Actor with ActorLogging {

  import InputPlugin.Msg

  def receive = {
    case Msg.Start    => start()
    case Msg.Stop     => stop()
    case Msg.Throttle => throttle()
  }

  def start() {}
  def stop() {}
  def throttle() {}

}

/**  A Filter is an  `Option[Event] => Option[Event]` function (or  `Function1[Option[Event],Option[Event]]`).
  *
  * It is created from a config and a set ActorRefs which point to the output plugins.
  * Filter plugins are composed together and attached to a host actor by the Processor.
  * They are invoked when the host actor is handled a message.
  *
  * @param config configuration of the plugin
  *
  */
abstract class FilterPlugin(val config: PluginConfig, val outputs: Set[ActorRef])
  extends Plugin with ( Option[Event] => Option[Event] ) {

}


/** An output plugin is an actor which accepts Start, Stop and Event messages.
  *
  * They are created and handled by a Outputs actor which acts as a broadcaster and as a buffer.
  *
  * @param config
  */
abstract class OutputPlugin(val config: PluginConfig)  extends Plugin

