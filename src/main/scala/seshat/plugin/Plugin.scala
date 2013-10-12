package seshat.plugin

import seshat._

import akka.actor.{Actor, ActorLogging, ActorRef}

/**
 *
 */
object Plugin {
  object Msg {
    case object Start
    case object Stop
  }
}

/**
 *
 */
trait Plugin {
 val config: PluginConfig
}

/**  An input plugin is an actor that is built with a config 
 *
 *  It must support the messages [[seshat.plugin.Plugin.Msg.Start]]
 *  and [[seshat.plugin.Plugin.Msg.Stop]].
 *
 *  An input plugins must start consuming input ONLY when the `Start` message is received and
 *  must stop when `Stop` is received.
 *
 *  Start means reading the input and `Events` to the filter pipeline.
 *  Stops means stop reading and send the remaining events to the pipeline; no Events should be dropped.
 *
 *  Input plugins are free to use postStop and postStart lifecycle events.
 *
 *  @param config configuration of the plugin
 *
 */
abstract class InputPlugin(val config:PluginConfig)
  extends Plugin with Actor with ActorLogging {

}

/**  A Filter is an  `Option[Event] => Option[Event]` function (or  `Function1[Option[Event],Option[Event]]`).
  *
  * Filter plugins are composed together and attached to a host actor by the Processor.
  * They are invoked when the host actor is handled a message.
  *
  * @param config configuration of the plugin
  *
  */
abstract class FilterPlugin(val config:PluginConfig)  
  extends Plugin with ( Option[Event] => Option[Event] ) {

}


/** An output plugin is an actor which accepts Start, Stop and Event messages.
  *
  * They are created and handled by a Outputs actor which acts as a broadcaster and as a buffer.
  *
  * @param config
  */
abstract class OutputPlugin(val config:PluginConfig)  extends Plugin

