package keyless.rdd.impl

import java.util

import keyless.index.{NonUniqueIndex, FullUniqueIndex, Index}
import keyless.rdd.{impl, KeylessRDDPartition}
import collection.JavaConversions._
import scala.reflect.ClassTag

/**
  * Created by gcherian on 12/23/2016.
  */


class KeylessRDDPartitionImpl[T: ClassTag, K: ClassTag, I: ClassTag]
(private val index: Index[T], keyFunction: T => K, indexFunction: T => I = null) extends KeylessRDDPartition[T] {


  import java.util.function.{Function ⇒ JFunction, Predicate ⇒ JPredicate}


  implicit def toJavaFunction[A, B](f: Function1[A, B]) = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }


  implicit def toJavaPredicate[A](f: Function1[A, Boolean]) = new JPredicate[A] {
    override def test(a: A): Boolean = f(a)
  }



  override def size: Long = index.size()

  override def iterator: Iterator[T] = index.iterator()

  override def get(v: T): T = index.get(v).asInstanceOf[T]

  override def put(v: T): Unit = index.put(v)

  override def filter(pred: (T) => Boolean): KeylessRDDPartition[T] = new KeylessRDDPartitionImpl(index.filter(toJavaPredicate(pred)), keyFunction, indexFunction)

  import scala.collection.JavaConversions._

  override def multiget(ks: List[T]): Iterator[T] = {
    var list = new util.ArrayList[T]()
    for (k <- ks) {
      val v: AnyRef = index.get(k)
      if (v != null) {
        if (v.isInstanceOf[FullUniqueIndex[T]]) {
          list.add(v.asInstanceOf[FullUniqueIndex[T]].getFirst.asInstanceOf[T])

        } else {
          list.add(v.asInstanceOf[T])
        }
      }
    }
    list.iterator()

  }

  override def multiput[U: ClassTag, K: ClassTag](ks: Iterator[(K, U)], z: (U) => T, f: (T, U) => T): KeylessRDDPartition[T] = {

    for (k <- ks) {
      if (index.get(z(k._2)) == null) index.put(z(k._2)) else index.put(f(z(k._2), k._2))
    }
    return new KeylessRDDPartitionImpl[T, K, I](index, keyFunction.asInstanceOf[T => K], indexFunction)

  }

}


object KeylessRDDPartitionImpl {


  import java.util.function.{Function ⇒ JFunction, Predicate ⇒ JPredicate}


  implicit def toJavaFunction[A, B](f: Function1[A, B]) = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }


  implicit def toJavaPredicate[A](f: Function1[A, Boolean]) = new JPredicate[A] {
    override def test(a: A): Boolean = f(a)
  }

  def apply[T: ClassTag, K: ClassTag, I: ClassTag](iter: Iterator[(K, T)], keyFunction: T => K) = {
    val index: Index[T] = new FullUniqueIndex[T](toJavaFunction(keyFunction))
    for (kts <- iter) {
      index.put(kts._2)
    }
    new KeylessRDDPartitionImpl[T, K, I](index, keyFunction)

  }

  def apply[T: ClassTag, K: ClassTag, I: ClassTag](iter: Iterator[(K, T)], keyFunction: T => K, indexFunction: T => I) = {
    val index: Index[T] = new NonUniqueIndex[T](toJavaFunction(keyFunction), toJavaFunction(indexFunction))
    for (kts <- iter) {
      index.put(kts._2)
    }
    new KeylessRDDPartitionImpl[T, K, I](index, keyFunction, indexFunction)

  }
}