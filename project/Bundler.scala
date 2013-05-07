import sbt._
import Keys._

object Bundler {
  
  implicit def stringToFile(s:String):java.io.File = new java.io.File(s)

  def apply() {
    println( "Bundling..." )
    clean()
    makeDirs()
    moveFiles()
    zipBundle()
    println( "Done" )
  }

  def clean() = {
    println( "Cleaning ..." )
    val bundle:File  = ( "target/bundle" )
    val zip:File     = ( "target/agent-bots.zip" )
    if( bundle.exists() )   IO.delete( bundle )
    if( zip.exists() )      IO.delete( zip )
  }

  def makeDirs() = {
    println( "Making directories ..." )
    IO.createDirectories( Seq( 
      ("target/bundle/agent-bots"), 
      ("target/bundle/agent-bots/lib"), 
      ("target/bundle/agent-bots/bin"),
      ("target/bundle/agent-bots/conf")
    ))
  }

  def moveFiles() { 

    println( "Moving files ..." )

    IO.copyFile(("src/main/bin/bash/agentbots"),    ("target/bundle/agent-bots/bin/agentbots"))
    IO.copyFile(("target/agent-bots.jar"),          ("target/bundle/agent-bots/lib/agent-bots.jar"))

    IO.copyFile(("doc/README.md"),                  ("target/bundle/agent-bots/README.md"))

    //IO.copyFile(("src/main/resources/avsl.prod.conf"),   ("target/bundle/agent-bots/conf/avsl.conf"))

    //Does not work because the zip file does not preserve the permissions.
    val sim:File = ("target/bundle/agent-bots/bin/agentbots")
    sim.setExecutable(true,false)

  }

  def zipBundle() {
    println( "Zipping Bundle ..." )
    IO.zip( filesIn("target/bundle/agent-bots", prune = "target/bundle/"), 
            "target/agent-bots.zip" ) 
  }

  /** Builds recursively  a `Seq` of `Tuple2[File,String]` for each file beneath basedir with the file in the filesystem and its path inside the zip file.
    * The path in the zip file is the original path minus the `prune` string if any.
    */
  def filesIn(basedir:File, prune:String = ""): Seq[(File,String)] = {
    assert( basedir.canRead, "Can't read "+basedir )
    def pruner(s:String) = s.replaceFirst(prune,"")
    val (dirs,files) = basedir.listFiles.toSeq.partition(_.isDirectory)
    val tuples = files.map( f => (f,pruner(f.getPath)) )
    // flatMap toma varias secuencias y las "aplasta". filesIn devuelve una secuencia y hay posiblemente varios dirs, hence map solo no sirve.
    (tuples ++ dirs.flatMap(filesIn(_,prune))) 
  }

}

    
