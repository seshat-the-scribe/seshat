/**
 *
 * User: f
 * Date: 12/04/13
 * Time: 14:58
 */



import java.util

import me.prettyprint.hector.api._
import me.prettyprint.hector.api.ddl.ComparatorType
import me.prettyprint.hector.api.factory._

import me.prettyprint.cassandra.service.ThriftKsDef

val cluster=HFactory.getOrCreateCluster("test-cluster","localhost:9160")

val cfDef = HFactory.createColumnFamilyDefinition("TestKS","ColumnFamilyName", ComparatorType.BYTESTYPE)

val ksDef = HFactory.createKeyspaceDefinition("TestKS",
  ThriftKsDef.DEF_STRATEGY_CLASS,
  1,
  util.Arrays.asList(cfDef)
)


val ks = cluster.describeKeyspace("TestKS")

