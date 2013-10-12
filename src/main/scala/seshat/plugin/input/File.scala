package seshat.plugin.input

import seshat.plugin.{PluginConfig, InputPlugin}
import java.util.Date
import seshat.processor.Processor.Internal.Batch
import seshat.Event
import scala.concurrent.blocking
import akka.actor.ActorRef
import java.io.RandomAccessFile
import seshat.processor.AskAgainProtocol


class File(config:PluginConfig)
  extends InputPlugin(config) with AskAgainProtocol {

  private val scheduler = context.system.scheduler

  private case object Moar

  private val batchSize = 1000

  private val file  = new RandomAccessFile(config.config("fileName"),"r")

  private val fieldsToAdd = Map(
    "@input.file.fileName" -> config.config("fileName")
  )

  override def receive: Receive = defaultHandler orElse fileHandler

  // Should provide another dispatcher for the blocking futures.
  implicit val exCtx = context.dispatcher

  def fileHandler: Receive = {

    case Moar if started  =>
      log.debug("Got Moar Msg")
      val parent = context.parent // fix reference
      blocking {
        var i = 0
        var lines = List[String]()
        while(i<batchSize){
          i = i + 1
          val line = file.readLine()
          if( line != null ) {
            resetRetries()
            lines = line +: lines
          } else {
            i = batchSize // Breaks
          }
        }
        if(lines.size > 0) sendLines(lines, parent)
      }
  }

  private def sendLines(lines: List[String], parent: ActorRef) {

    log.debug(s"About to send ${lines.size} lines")

    val events = lines map { line =>
      Event( line, "file", (new Date()).getTime, fieldsToAdd  )
    }

    parent ! Batch( events.reverse )

    log.debug("Rescheduling Moar")
    scheduleAsk(self,Moar)

  }


  override def start() {
    log.info("Started")
    self ! Moar
    started = true
  }

  override def postStop() {
    file.close()
    log.debug("Closed")
  }
}


