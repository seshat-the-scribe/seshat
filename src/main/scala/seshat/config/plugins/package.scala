package seshat.config

/**
 * This package contains the machinery to resolve plugins configs to plugins and ways to instantiate them.
 */
package object plugins {

  case class Plugins(
    inputs:   Set[PluginConfig[InputPlugin]],
    filters:  Set[PluginConfig[FilterPlugin]],
    outputs:  Set[PluginConfig[OutputPlugin]]
  )

  trait PluginConfig[T <: Plugin] {
    val name: String
    val clazz: Class[T]
  }

  trait Plugin

  trait InputPlugin   extends Plugin
  trait FilterPlugin  extends Plugin
  trait OutputPlugin  extends Plugin

}

