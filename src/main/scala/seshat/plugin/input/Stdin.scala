package seshat.plugin.input

import seshat.plugin.{PluginConfig, InputPlugin}

class StdIn(config:PluginConfig) extends InputPlugin(config) {
  def start(): Unit = ???
  def stop(): Unit = ???
  def throttle(): Unit = ???
}


