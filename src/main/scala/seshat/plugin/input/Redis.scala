package seshat.plugin.input

import seshat.plugin.{InputPlugin, PluginConfig}

class Redis(config:PluginConfig) extends InputPlugin(config) {
  def start(): Unit = ???
  def stop(): Unit = ???
  def throttle(): Unit = ???
}


