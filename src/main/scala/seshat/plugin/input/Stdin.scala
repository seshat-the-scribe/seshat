package seshat.plugin.input

import seshat.plugin.{PluginConfig, InputPlugin}
import java.util.{Date, Scanner}
import concurrent.duration._
import seshat.processor.Processor.Common.Events
import seshat.Event
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.actor.ActorRef


class Stdin(config:PluginConfig) extends InputPlugin(config) {

  val scheduler = context.system.scheduler

  val sc = new Scanner(System.in)

  private case object Moar

  override def receive: Receive = defaultHandler orElse stdinHandler

  // Should provide another dispatcher for the blocking futures.
  implicit val exCtx = context.dispatcher

  def stdinHandler: Receive = {
    case Moar =>
      log.debug("Got Moar Msg")
      val parent = context.parent // fix reference
      Future {
        if( sc.hasNextLine ) {
          log.debug("Has Next Line")
          sendLines(parent)
        } else {
          log.debug("Scheduling Moar 500 millis ahead")
          scheduler.scheduleOnce(500 millis, self, Moar)
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


  def start() {
    log.info("Started")
    self ! Moar
  }

  def stop()      {???}
  def throttle()  {???}

}


