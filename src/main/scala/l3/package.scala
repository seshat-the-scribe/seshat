
import scala.concurrent.duration.FiniteDuration
import akka.actor.{Props, ActorSystem}


/** Cassandra Benchmarks */
package object l3 {

  def spawnCoordinator(system: ActorSystem, config: L3Config) = system.actorOf(
    Props(new Coordinator(config)),
    s"${config.name.toUpperCase}-COORDINATOR"
  )

  case class L3Config (
    name:             String,
    keySpace:         String,
    duration:         FiniteDuration,
    cassandraConfig:  CassandraConfig,
    workerConfigs:    Set[WorkerConfig]
  )

  case class CassandraConfig ( ip: String, port: Int )

  // FIXME reducir a name, clazz y que lo demás se pase via config
  case class WorkerConfig (
    name:         String,
    clazz:        String,
    readRatio:    OpRate,
    writeRatio:   OpRate,
    writeSize:    Int,
    other:        Map[String,String] = Map()
  )

  case class OpRate( ops: Int, every: FiniteDuration  )

  object RTX {
    def apply(msg:String) = new RuntimeException(msg)
  }

}



