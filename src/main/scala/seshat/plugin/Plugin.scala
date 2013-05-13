package seshat.plugin

import akka.actor.{ActorRef, ActorLogging, Actor}

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

/**  An input plugin is an actor that is built from with a config and an
 *  ActorRef to the filter pipeline.
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
 *  @param filterPipeline the configured filter pipeline.
 *
 */
abstract class InputPlugin(val config:PluginConfig, filterPipeline: ActorRef)
  extends Plugin {

}

/**  A Filter is a `Function1[Option[Event],Option[Event]]`.
  *
  * It is created from a config and a set ActorRefs which point to the output plugins.
  *
  * @param config configuration of the plugin
  *
  */
abstract class FilterPlugin(val config:PluginConfig)  extends Plugin with ((Option[Event]) => Option[Event])


abstract class OutputPlugin(val config:PluginConfig)  extends Plugin

