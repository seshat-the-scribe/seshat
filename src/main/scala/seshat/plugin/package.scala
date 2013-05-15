package seshat

import com.typesafe.config._
import collection.JavaConverters._
import seshat.config._

/**
 * This package contains the machinery to resolve plugin configs to plugins and ways to instantiate them.
 *
 */
package object plugin {

  case class Plugins(
    inputs:   Set[PluginDescriptor],
    filters:  Set[PluginDescriptor],
    outputs:  Set[PluginDescriptor]
  )

  case class PluginConfig(
    name:   String,
    config: Map[String, String]
   )

  case class  PluginDescriptor(
    name:   String,
    clazz:  Class[Plugin]
  )

  def resolvePlugins = {
    val config = ConfigFactory.parseResources("seshat-builtins.conf")
    Plugins(
      resolve(config,"input"),
      resolve(config,"filter"),
      resolve(config,"output")
    )
  }

  private def resolve(config:Config, kind: String): Set[PluginDescriptor] = 
    config.getObject("seshat.plugins."+kind).asScala
      .map{ case (k,v) => k -> v.toMap   }
      .map{ case (k,v) => 
        PluginDescriptor(
          k,
          Class.forName(v("className")).asInstanceOf[Class[Plugin]]
        )
      }.toSet


}

