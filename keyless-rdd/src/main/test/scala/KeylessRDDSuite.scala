import java.util

import keyless.Domain
import keyless.index.FullUniqueIndex
import keyless.rdd.KeylessRDD
import keyless.rdd.impl.KeylessRDDPartitionImpl
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.scalatest.FunSuite
import scala.collection.JavaConversions._
/**
  * Created by georg on 12/25/2016.
  */
class KeylessRDDSuite extends FunSuite with SharedSparkContext {


  def create(rdd: RDD[Domain]) = {
    KeylessRDD[Domain, String](rdd, d => d.id)
  }

  def domains(sc: SparkContext, n: Int) = {
    create(sc.parallelize((0 to n).map(x => (new Domain("name-" + x))), 1))
  }

  test("get") {
    var rdd = domains(sc, 1)
    println(rdd.count())
    val domains1: List[Domain] = List[Domain](new Domain("name-11"), new Domain("name-17"))
    print(rdd.toDebugString)
    rdd = rdd.multiput(domains1)
    println(rdd.toDebugString)
    val domain11 = rdd.get(new Domain("name-11"))
    println(domain11)
    rdd.foreach(d => println(d))


  }



}
