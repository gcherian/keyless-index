package keyless.rdd.impl

import keyless.index.Index
import keyless.rdd.KeylessRDDPartition
import collection.JavaConversions._
import scala.reflect.ClassTag

/**
  * Created by georg on 12/23/2016.
  */
class KeylessRDDPartitionImpl[T: ClassTag]
(private val index: Index[T]) extends KeylessRDDPartition[T] {

  import java.util.function.{ Function ⇒ JFunction, Predicate ⇒ JPredicate, BiPredicate }


  implicit def toJavaFunction[A, B](f: Function1[A, B]) = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }


  implicit def toJavaPredicate[A](f: Function1[A, Boolean]) = new JPredicate[A] {
    override def test(a: A): Boolean = f(a)
  }


  override def size: Long = index.size()

  override def iterator: Iterator[T] = index.iterator()

  override def apply(value: T): Option[T] = Option.apply(index.get(value).asInstanceOf[T])

  override def get(v: T): T = index.get(v).asInstanceOf[T]

  override def put(v: T): Unit = index.put(v)

  override def map[U: ClassTag](func: (T) => U): KeylessRDDPartition[U] = new KeylessRDDPartitionImpl[U](index.map(toJavaFunction(func)))

  override def filter(pred: (T) => Boolean): KeylessRDDPartition[T] = new KeylessRDDPartitionImpl(index.filter(toJavaPredicate(pred)))


  object KeylessRDDPartitionImpl {
    def apply[V:ClassTag,K:ClassTag](iter:Iterator[V],keyFunction:V=>K):KeylessRDDPartitionImpl[V] = {

    }
  }
}
