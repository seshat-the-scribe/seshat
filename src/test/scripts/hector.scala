

import java.util

import me.prettyprint.cassandra.serializers._
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate
import me.prettyprint.hector.api.ddl.ComparatorType
import me.prettyprint.hector.api.factory._

import me.prettyprint.cassandra.service.ThriftKsDef

val cluster = HFactory.getOrCreateCluster("test-cluster","localhost:9160")



val cf1Def = HFactory.createColumnFamilyDefinition(
  "TestKS",
  "cf1",
  ComparatorType.UTF8TYPE
)

val cf2Def = HFactory.createColumnFamilyDefinition(
  "TestKS",
  "cf2",
  ComparatorType.UTF8TYPE
)


val ksDef = HFactory.createKeyspaceDefinition(
  "TestKS",
  ThriftKsDef.DEF_STRATEGY_CLASS,
  1,
  util.Arrays.asList(cf1Def,cf2Def)
)

Option( cluster describeKeyspace "TestKS" ) match {

  case  Some(ks)  =>
    val msg = "There seems to be a keyspace, beware"
    println( msg )

  case  None      =>
    cluster.addKeyspace(ksDef)

}

val ks = HFactory.createKeyspace("TestKS", cluster)





