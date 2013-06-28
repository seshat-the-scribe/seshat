package seshat.plugin.input

import seshat.plugin.{PluginConfig, InputPlugin}
import java.util.{Date, Scanner}
import concurrent.duration._
import seshat.processor.Processor.Common.Events
import seshat.Event
import concurrent.blocking
import akka.actor.ActorRef


class Stdin(config:PluginConfig) extends InputPlugin(config) {

  private val scheduler = context.system.scheduler

  private val sc = new Scanner(System.in)

  private case object Moar

  private val sleepPeriod = 5.millis

  override def receive: Receive = defaultHandler orElse stdinHandler

  // Should provide another dispatcher for the blocking futures.
  implicit val exCtx = context.dispatcher

  def stdinHandler: Receive = {

    case Moar if started  =>
      log.debug("Got Moar Msg")
      val parent = context.parent // fix reference
      blocking {
        if( sc.hasNextLine ) {
          log.debug("Has Next Line")
          sendLines(parent)
        } else {
          log.debug(s"Scheduling Moar $sleepPeriod ahead")
          scheduler.scheduleOnce(sleepPeriod, self, Moar)
        }
      }

  }

  private def sendLines(parent: ActorRef) {

    log.debug("Preparing event")

    val event = Event( sc.nextLine(), "stdin", (new Date()).getTime )

    log.debug(s"Sending Event $event")
    parent ! Events( Seq(event) )

    log.debug("Rescheduling Moar")
    scheduler.scheduleOnce(5 milli, self, Moar)

  }


  override def start() {
    log.info("Started")
    self ! Moar
    started = true
  }

}


