import keyless.Domain
import keyless.index.FullUniqueIndex
import keyless.rdd.KeylessRDD
import keyless.rdd.impl.KeylessRDDPartitionImpl
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.scalatest.FunSuite

/**
  * Created by georg on 12/25/2016.
  */
class KeylessRDDSuite extends FunSuite with SharedSparkContext {


  def create(rdd: RDD[Domain]) = {
    KeylessRDD[Domain, String](rdd, d => d.id)
  }

  def domains(sc: SparkContext, n: Int) = {
    create(sc.parallelize((0 to n).map(x => (new Domain("name-" + x))), 5))
  }

  test("get") {
    val rdd = domains(sc, 10)
    rdd.get(new Domain("name-0"))


  }



}
