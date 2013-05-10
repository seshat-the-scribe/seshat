
import com.typesafe.config.Config
import akka.actor.{Props, ActorSystem}


/** Seshat is a library and program to handle streams of logs.  */
package object seshat {

  def spawnCoordinator(system: ActorSystem, config: SeshatConfig) = system.actorOf(
    Props(new Processor(config)),
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



