package icc.gym


import scala.util.control.Exception._
import scala.collection.JavaConverters._
import scala.concurrent.duration._

import org.slf4j.{MDC, LoggerFactory}

import icc.util.arguments.{ShortDefinitions, ArgsParser}
import icc.util.AnsiColors._

import com.typesafe.config.ConfigFactory
import java.io.File

import akka.util.Timeout
import akka.pattern._
import scala.util.{Failure, Success}
import akka.actor.ActorSystem

/**
 *
 * User: f
 * Date: 11/04/13
 * Time: 17:21
 */
object L3Main {

  implicit val timeout = Timeout(2 seconds)

  val system = ActorSystem("L3Agent")

  sys addShutdownHook {
    system.shutdown()
  }


  def main(args: Array[String]) {

    try {

      val as = Arguments.parse(args)

      MDC.put("symName", as.valuesMap("name"))

      val config = buildConfig( as )

      log.info( s"Launching coordinator with config ${config}" )
      val coord = spawnCoordinator(system, config)
      coord ! Coordinator.Msg.Start

    } catch {

      case e: Exception =>
        system.shutdown()
        println(Red(e.getMessage))
        log.error( Red(e.getMessage) )
        throw e

    }


  }


  def buildConfig(as: ArgsParser) = {



    val symName = failNoSuchElementWith("Falta parametro `name`") {
      as.valuesMap("name")
    }

    val cassandraIp = failNoSuchElementWith("Falta parametro `cassandraIp`") {
      as.valuesMap("cassandraIp")
    }

    val cassandraPort = failBadNumberFormatWith("Falta parametro `cassandraPort`") {
      as.valuesMap.getOrElse( "cassandraPort", "9160" ).toInt
    }

    val cassandraConfig = CassandraConfig( cassandraIp, cassandraPort )

    val rt = failNoSuchElementWith( "Falta parámetro `routine`"  ) {
      as.valuesMap("routine")
    }

    val config = ConfigFactory.parseFile(new File(rt))

    val keySpace =  failExceptionWith("keySpace not present"){
      config.getString("gym.routine.keySpace")
    }

    val duration = failExceptionWith("Duration not present") {
      config.getInt("gym.routine.duration").seconds
    }

    log.debug( "building configses"  )
    val configses = {

      val cfs = config.getConfigList("gym.routine.columnFamilies").asScala

      for {
        cfg       <- cfs
        name      <- Option(cfg.getString("name"))
        clazz     <- Option(cfg.getString("class"))
        readRate  <- Option(cfg.getString("readRate"))
        writeRate <- Option(cfg.getString("writeRate"))
        writeSize <- Option(cfg.getInt("writeSize"))
      } yield (
        WorkerConfig(
          name,
          clazz,
          parseRate(readRate, "ReadRate"),
          parseRate(writeRate, "WriteRate"),
          writeSize
        )
      )

    }

    val gymConfig = L3Config( symName, keySpace, duration, cassandraConfig, configses.toSet )

    gymConfig



  }

  def parseRate( rate: String, failWith: String ) = {
    val (ops,int) = rate.split(":") match {
      case Array(o,i) if o.size > 0 && i.size > 0  => (o,i)
      case _ => throw RTX(failWith)
    }
    val operations = failBadNumberFormatWith(failWith+" Bad operation parameter: ") {
      ops.toInt
    }
    val interval = failBadNumberFormatWith(failWith+" Bad interval parameter: ") {
      int.toInt.millis
    }
    OpRate(operations, interval)
  }


  def failBadNumberFormatWith(msg: String) =
    catching(classOf[NumberFormatException]).withApply( x => throw RTX(msg + " => " + x.getMessage) )

  def failExceptionWith(msg: String) =
    catching(classOf[Exception]).withApply( x => throw RTX(msg + " => " + x.getMessage) )

  def failNoSuchElementWith(msg: String) =
    catching(classOf[NoSuchElementException]).withApply( x => throw RTX(msg + " => " + x.getMessage) )


  private lazy val log = LoggerFactory.getLogger(getClass)

}

object Arguments {

  import ShortDefinitions._

  val defs = List (
    VD("cassandraIp"),
    VD("cassandraPort"),
    VD("routine")
  )

  def parse(args: Arguments) = ArgsParser( args, defs )


}
