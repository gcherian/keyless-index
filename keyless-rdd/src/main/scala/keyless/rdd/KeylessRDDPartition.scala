package keyless.rdd

import scala.reflect.ClassTag

/**
  * Created by georg on 12/19/2016.
  */
private[keyless] abstract class KeylessRDDPartition[T] {


  def size: Long

  def iterator: Iterator[T]

  def get(v: T): T

  def put(v: T)

  def getAll(): List[T]

  def filter(pred: (T) => Boolean): KeylessRDDPartition[T]

  def multiget(ks: List[T]): Iterator[T]

  def multiput[U: ClassTag, K: ClassTag](kvs: Iterator[(K, U)], z: (U) => T, f: (T, U) => T): KeylessRDDPartition[T] =
    throw new UnsupportedOperationException("Must be implemented in subclass.")

  def delete(ks: Iterator[T]): KeylessRDDPartition[T] =
    throw new UnsupportedOperationException("Must be implemented in subclass.")




}
