
import akka.actor.{ActorRef, Props, ActorSystem}
import seshat.plugin._
import seshat.config._
import seshat.processor.Processor


/** Seshat is a tool and library to handle streams of logs.
  *
  * {{{
  *
  * TODO OK parse configuration (just extract input, filter, output)
  * TODO ~ resolve plugins and instantiate as appropriate.
  * TODO create output children
  * TODO create filter pipeline passing outputs
  * TODO create children inputs passing pipeline
  * TODO start inputs
  *
  * TODO measure all the things!
  *
  * }}}
  *
  */
package object seshat {

  case class SeshatConfig (
    name:               String,
    inputs:             Set[PluginConfig],
    filters:            Set[PluginConfig],
    outputs:            Set[PluginConfig],
    queueSize:          Int = 100,
    filterParallelism:  Int = 2
  )

  /** Represents an event going through the pipeline.
    *
    *  This class should be instantiated by an InputPlugin with the raw data,
    *  a timestamp and an event kind.
    *
    */
  case class Event(
    raw:        Any,
    kind:       String,
    timestamp:  Long,
    fields:     Map[String, String] = Map(),
    tags:       Set[String]         = Set()
  )


  // FIXME move to CLI object
  def start( name: String, system: ActorSystem ) {
    val config = buildConfigFromFile( name )
    spawnProcessor(system, config) ! Processor.Msg.Start
  }

  // FIXME extract resolvePlugins and pass plugins via parameter
  def spawnProcessor(system: ActorSystem, config: SeshatConfig): ActorRef = {
    val plugins: Plugins = resolvePlugins
    system.actorOf(
      Props( classOf[Processor], config, plugins ),
      s"${config.name.replace(" ", "_").toUpperCase}-PROCESSOR"
    )
  }

  object Kaboom {
    def apply(msg: String) = new RuntimeException(msg)
  }

}

