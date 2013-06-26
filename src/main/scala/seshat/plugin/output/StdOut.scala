package seshat.plugin.output

import seshat._
import seshat.plugin.{PluginConfig, OutputPlugin}
import seshat.processor.Processor

class Stdout(config: PluginConfig) extends OutputPlugin(config) {

  // Members declared in seshat.processor.AskAgainProtocol
  implicit val exCtx = context.dispatcher

  override def receive: Receive = defaultHandler orElse stdoutHandler

  def stdoutHandler: Receive = {
    case Processor.Common.Events(events) =>
      log.debug(s"Received ${events.size} events : ${events} ")
      if ( events.size > 0  ) {
        performOutput(events)
        context.parent ! Processor.Common.GetEvents
      } else {
        reScheduleAsk(context.parent, Processor.Common.GetEvents)
      }


  }

  protected def performOutput(events:Seq[Event]) {
    events foreach { e =>
      println("-"*80)
      println(e)
      println("-"*80)
    }
  }

}

