package keyless.rdd

import keyless.rdd.impl.KeylessRDDPartitionImpl
import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{HashPartitioner, OneToOneDependency, Partition, TaskContext}

import scala.reflect.ClassTag

/**
  * Created by georg on 12/19/2016.
  */
class KeylessRDD[T: ClassTag, K: ClassTag, I: ClassTag]
(private val partitionsRDD: RDD[KeylessRDDPartition[T]], private val keyFunction: T => K, private val indexFunction: T => I = null) extends RDD[T](partitionsRDD.context, List(new OneToOneDependency(partitionsRDD))) {

  require(partitionsRDD.partitioner.isDefined)
  override val partitioner = partitionsRDD.partitioner

  @DeveloperApi
  override def compute(split: Partition, context: TaskContext): Iterator[T] = {
    firstParent[KeylessRDDPartition[T]].iterator(split, context).next.iterator
  }

  override protected def getPartitions: Array[Partition] = partitionsRDD.partitions

  override protected def getPreferredLocations(s: Partition): Seq[String] =
    partitionsRDD.preferredLocations(s)

  override def persist(newLevel: StorageLevel): this.type = {
    partitionsRDD.persist(newLevel)
    this
  }

  override def unpersist(blocking: Boolean = true): this.type = {
    partitionsRDD.unpersist(blocking)
    this
  }

  override def setName(_name: String): this.type = {
    partitionsRDD.setName(_name)
    this
  }

  override def count(): Long = {
    partitionsRDD.map(_.size).reduce(_ + _)
  }

  def get(k: T): T = {
    val result = multiget(List(k))
    if (result.length > 0) result(0) else k
  }

  def multiget(ks: List[T]): List[T] = {
    val ksByPartition = ks.groupBy(k => partitioner.get.getPartition(k))
    val partitions = ksByPartition.keys.toSeq
    val results: Array[Array[T]] = context.runJob(partitionsRDD,
      (context: TaskContext, partIter: Iterator[KeylessRDDPartition[T]]) => {
        if (partIter.hasNext && ksByPartition.contains(context.partitionId)) {
          val part = partIter.next()
          val ksForPartition = ksByPartition.get(context.partitionId).get
          part.multiget(ksForPartition).toArray
        } else {
          Array.empty
        }
      }, partitions)
    results.flatten.toList
  }


  def put(k: T): KeylessRDD[T, K, I] = multiput(List(k))

  def multiput(ts: List[T]): KeylessRDD[T, K, I] = {
    val updates = context.parallelize(ts.toSeq).keyBy(keyFunction).partitionBy(partitioner.get)
    val zipped = zipPartitionsWithOther(updates)(new MultiputZipper(a => a, (a, b) => b))
    zipped
  }

  private def zipPartitionsWithOther[V: ClassTag]
  (other: RDD[(K, V)])(f: OtherZipPartitionsFunction[V, T]): KeylessRDD[T, K, I] = {
    val partitioned = other.partitionBy(partitioner.get)
    val newPartitionsRDD = partitionsRDD.zipPartitions(partitioned, true)(f)
    new KeylessRDD(newPartitionsRDD, keyFunction, indexFunction)
  }

  private type OtherZipPartitionsFunction[V, U] = Function2[Iterator[KeylessRDDPartition[T]], Iterator[(K, V)], Iterator[KeylessRDDPartition[U]]]


  private class MultiputZipper[U: ClassTag](z: (U) => T, f: (T, U) => T)
    extends OtherZipPartitionsFunction[U, T] with Serializable {
    def apply(thisIter: Iterator[KeylessRDDPartition[T]], otherIter: Iterator[(K, U)]): Iterator[KeylessRDDPartition[T]] = {
      val thisPart = thisIter.next()
      val partPut = thisPart.multiput(otherIter, z, f)
      Iterator(partPut)
    }

  }


}

object KeylessRDD {

  def apply[T: ClassTag, K: ClassTag](elements: RDD[T], keyFunction: T => K): KeylessRDD[T, K, Nothing] = {

    val elemPartitioned: RDD[(K, T)] = elements.keyBy(keyFunction).partitionBy(new HashPartitioner(elements.partitions.size))
    val partitions = elemPartitioned.mapPartitions[KeylessRDDPartition[T]](iter => Iterator(KeylessRDDPartitionImpl[T, K, Nothing](iter, keyFunction)), preservesPartitioning = true)
    new KeylessRDD[T, K, Nothing](partitions, keyFunction)
  }

  def apply[T: ClassTag, K: ClassTag, I: ClassTag](elements: RDD[T], keyFunction: T => K, indexFunction: T => I): KeylessRDD[T, K, I] = {

    val elemPartitioned: RDD[(K, T)] = elements.keyBy(keyFunction).partitionBy(new HashPartitioner(elements.partitions.size))
    val partitions = elemPartitioned.mapPartitions[KeylessRDDPartition[T]](iter => Iterator(KeylessRDDPartitionImpl[T, K, I](iter, keyFunction, indexFunction)), preservesPartitioning = true)
    new KeylessRDD[T, K, I](partitions, keyFunction, indexFunction)
  }


}
