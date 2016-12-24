package keyless.rdd

/**
  * Created by georg on 12/19/2016.
  */
private[keyless] abstract class KeylessRDDPartition[V] {

  def size: Long

  def apply(): Option[V]

  def isDefined: Boolean = {
    apply().isDefined
  }

  def iterator: Iterator[V]



}
