import sbt._

import sbtassembly.Plugin._
import AssemblyKeys._

object SeshatBuild extends Build
{

  lazy val root =
    Project(
      "root", 
      file("."), 
      settings = Project.defaultSettings ++ assemblySettings ++ SeshatSettings.settings
    ) 

}

object SeshatSettings {

  lazy val settings = Seq( bundleTask )

  val bundle     = TaskKey[Unit]("bundle", "Bundles stuff")
  val bundleTask = bundle <<= (assembly) map { _ => Bundler() }

}

