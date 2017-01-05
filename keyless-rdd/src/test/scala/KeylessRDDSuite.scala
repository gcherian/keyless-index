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


  def createFui(rdd: RDD[Domain], keyFunction: (Domain) => String = d => d.id) = {
    KeylessRDD[Domain, String](rdd, keyFunction)
  }

  def createNui(rdd: RDD[Domain], keyFunction: (Domain) => String = d => d.id, indexFunction: (Domain) => String = d => d.name) = {
    KeylessRDD[Domain, String, String](rdd, keyFunction, indexFunction)
  }


  test("fui") {
    var rdd = createFui(sc.parallelize((0 to 10000).map(x => (new Domain("name-" + x))), 10))
    println(rdd.count())
    val putDomain11: Domain = new Domain("name-11")
    val domains1: List[Domain] = List[Domain](putDomain11, new Domain("name-17"))
    print(rdd.toDebugString)
    rdd = rdd.multiput(domains1)
    println(rdd.toDebugString)
    val getDomain11: Domain = new Domain("name-11")
    getDomain11.id = putDomain11.id
    val domain11 = rdd.get(getDomain11)
    println(domain11)
    rdd.foreach(d => println(d))
    rdd.mapPartitionsWithIndex { (partitionIndex, dataIterator) => dataIterator.map(dataInfo => (dataInfo + " fui is located in  " + partitionIndex + " partition.")) }.foreach(println)
    assertResult(getDomain11.id)(domain11.id)
    println(rdd.count())

  }

  test("nui") {

    var rdd = createNui(sc.parallelize((0 to 10000).map(x => (new Domain("name-" + x % 10))), 10))
    val putDomain11: Domain = new Domain("name-11")
    val domains1: List[Domain] = List[Domain](putDomain11, new Domain("name-17"))
    rdd = rdd.multiput(domains1)
    val getDomain11: Domain = new Domain("name-11")
    getDomain11.id = putDomain11.id
    val domain11 = rdd.get(getDomain11)
    rdd.foreach(d => println(d))
    rdd.mapPartitionsWithIndex { (partitionIndex, dataIterator) => dataIterator.map(dataInfo => (dataInfo + " nui is located in  " + partitionIndex + " partition.")) }.foreach(println)
    assertResult(getDomain11.name)(domain11.name)
    println(rdd.count())



  }

  test("hier") {

    val house: List[Room] = List[Room](new Room("1024", "1", 1, "blue", 700), new Room("1024", "2", 4, "red", 1500))
    val floor = KeylessRDD[Room, String](sc.parallelize(house), r => r.getId())
    val tower = KeylessRDD[Room, String, String](sc.parallelize(floor.getAll()), floor.keyFunction, r => r.getFloor())
    tower.foreach(r => println(r))
  }



}

class Room(floor: String, id: String, noOfWindows: Int, color: String, area: Int) extends Serializable {
  def getId(): String = {
    id
  }

  def getFloor(): String = {
    floor
  }

}
