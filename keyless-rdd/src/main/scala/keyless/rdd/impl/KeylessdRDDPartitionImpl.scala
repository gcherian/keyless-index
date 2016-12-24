package keyless.rdd.impl

import keyless.index.Index
import keyless.rdd.KeylessRDDPartition
import collection.JavaConversions._
import scala.reflect.ClassTag

/**
  * Created by georg on 12/23/2016.
  */
class KeylessdRDDPartitionImpl[T: ClassTag]
(private val index: Index[T]) extends KeylessRDDPartition[T] {
  override def size: Long = ???

  override def apply(): Option[Nothing] = ???

  override def iterator: Iterator[T] = index.iterator()
}
