
import com.typesafe.config.Config
import akka.actor.{Props, ActorSystem}
import seshat.config.plugins.Plugins


/** Seshat is a tool and library to handle streams of logs.
  *
  * {{{
  *
  * TODO parse configuration (just extract input, filter, output)
  * TODO resolve plugins and instantiate as appropriate.
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

  def spawnCoordinator(system: ActorSystem, config: SeshatConfig, plugins: Plugins) =
    system.actorOf(
      Props(new Processor(config,plugins)),
      s"${config.name.replace(" ","_").toUpperCase}-COORDINATOR"
    )

  case class SeshatConfig (
    name:      String,
    inputs:    Seq[Config],
    filters:   Seq[Config],
    outputs:   Seq[Config]
  )

  object RTX {
    def apply(msg: String) = new RuntimeException(msg)
  }

}



