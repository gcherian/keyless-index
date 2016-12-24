package keyless.rdd

import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{TaskContext, Partition, OneToOneDependency}
import scala.reflect.ClassTag

/**
  * Created by georg on 12/19/2016.
  */
class KeylessRDD[T: ClassTag](
                               private val partitionsRDD: RDD[KeylessRDDPartition[T]]) extends RDD[T](partitionsRDD.context, List(new OneToOneDependency(partitionsRDD))) {

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


}
