/**
  * Created by georg on 12/26/2016.
  */

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite

/** Shares a local `SparkContext` between all tests in a suite and closes it at the end */
trait SharedSparkContext extends BeforeAndAfterAll {
  self: Suite =>

  @transient private var _sc: SparkContext = _

  def sc: SparkContext = _sc

  var conf = new SparkConf(false)

  override def beforeAll() {
    _sc = new SparkContext("local", "test", conf)
    super.beforeAll()
  }

  override def afterAll() {
    if (_sc != null) {
      _sc.stop()
    }
    // To avoid Akka rebinding to the same port, since it doesn't unbind immediately on shutdown
    System.clearProperty("spark.driver.port")
    _sc = null
    super.afterAll()
  }
}
