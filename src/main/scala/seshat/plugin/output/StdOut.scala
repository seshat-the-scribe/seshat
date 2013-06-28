package seshat.plugin.output

import seshat._
import seshat.plugin.{PluginConfig, OutputPlugin}
import seshat.processor.Processor

class Stdout(config: PluginConfig) extends OutputPlugin(config) {

  // Members declared in seshat.processor.AskAgainProtocol
  implicit val exCtx = context.dispatcher

  protected def performOutput(events:Seq[Event]) {
    events foreach { e =>
      println("-"*80)
      println(e)
      println("-"*80)
    }
  }

}

