
import com.typesafe.config.Config
import scala.concurrent.duration.FiniteDuration
import akka.actor.{Props, ActorSystem}


/** Cassandra Benchmarks */
package object seshat {

  def spawnCoordinator(system: ActorSystem, config: SeshatConfig) = system.actorOf(
    Props(new Coordinator(config)),
    s"${config.name.toUpperCase}-COORDINATOR"
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



