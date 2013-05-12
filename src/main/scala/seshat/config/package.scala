package seshat

import com.typesafe.config.{ConfigFactory, ConfigValue}
import java.io.File
import seshat.plugin.PluginConfig

/**
 *   Utilities to make working with Config objects easier.
 */
package object config {

  /** Enhances a ConfigValue
   *
   * @param value the value to enhance.
   */
  implicit class EnhancedConfigValue(val value: ConfigValue) extends AnyVal {

    import java.util.HashMap
    import collection.JavaConversions._

    def toMap = value.unwrapped
                  .asInstanceOf[HashMap[String,String]]
                  .toMap

  }

  // TODO move to a config package
  //      with all config and plugin loading machinery.
  // TODO load plugins by reading plugins.conf
  //      and merging it with builtinPlugins.conf
  def buildConfig(name: String) = {

    import collection.JavaConversions._

    val file = new File(name)

    val config = ConfigFactory.parseFile(file)


    val ic          = config.getObject("input")
    val inputConfig = ic.keySet.map( k => ( PluginConfig(k, ic.get(k).toMap) ) ).toSet

    val fc              = config.getObject("filter")
    val filterConfig   = fc.keySet.map( k => ( PluginConfig(k, fc.get(k).toMap) ) ).toSet

    val oc            = config.getObject("output")
    val outputConfig  = fc.keySet.map( k => ( PluginConfig(k, oc.get(k).toMap) ) ).toSet

    SeshatConfig(
      file.getName,
      inputConfig,
      filterConfig,
      outputConfig
    )



  }


}
