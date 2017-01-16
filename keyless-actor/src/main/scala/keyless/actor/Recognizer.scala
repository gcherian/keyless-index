package keyless.actor

import keyless.rdd.KeylessRDD

import scala.reflect.ClassTag

/**
  * Created by gcherian on 1/16/2017.
  */
class Recognizer[T: ClassTag, K: ClassTag, I: ClassTag](val memory: KeylessRDD[T, K, I])(val invariantFunctions: Array[T => _]) {


  val id: Long = System.currentTimeMillis()

  val output: Boolean = false


}