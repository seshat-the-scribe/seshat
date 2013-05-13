
import akka.actor.{ActorRef, Props, ActorSystem}
import seshat.plugin._
import seshat.config._


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
    name:    String,
    inputs:  Set[PluginConfig],
    filters: Set[PluginConfig],
    outputs: Set[PluginConfig]
  )

  def start( name: String, system: ActorSystem ) {
    val config  = buildConfig( name )
    spawnProcessor(system, config) ! Processor.Msg.Start
  }

  def spawnProcessor(system: ActorSystem, config: SeshatConfig): ActorRef = {
    val plugins = resolvePlugins
    system.actorOf(
      Props(new Processor(config, plugins)),
      s"${config.name.replace(" ", "_").toUpperCase}-PROCESSOR"
    )
  }

  object RTX {
    def apply(msg: String) = new RuntimeException(msg)
  }

}

