package keyless.actor.node

/**
  * Created by gcherian on 1/15/2017.
  */

import java.text.SimpleDateFormat
import java.util.Date

import akka.typed.ScalaDSL._
import akka.typed.{ActorRef, Behavior, Props}

import scala.reflect.ClassTag

class Node[M: ClassTag] {


  object NodeInputs {
    def addInput[T](behavior: Seq[ActorRef[Nothing]] => Behavior[T]) = Partial[T] {
      case NodeInputMessage(i, r) =>
        r ! Ack
        behavior(i)
    }
  }

  object NodeOutputs {
    def addOutput[T](behavior: (Seq[ActorRef[Nothing]], Seq[ActorRef[Input[_]]]) => Behavior[T], inputs: Seq[ActorRef[Nothing]]) = Partial[T] {
      case NodeOutputMessage(o, r) =>
        r ! Ack
        behavior(inputs, o)
    }
  }

}

object InputNode extends Node[Double] {

  def props() = Props(receive)

  def receive = NodeOutputs.addOutput(run, Seq())

  def run(inputs: Seq[ActorRef[Nothing]], outputs: Seq[ActorRef[Input[_]]]): Behavior[NodeMessage] = Partial[NodeMessage] {
    case i: Input[_] =>
      outputs.foreach(_ ! i)
      run(inputs, outputs)
  }
}

object OutputNode extends Node[Double] {


  def props() = Props(receive)

  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  def receive = NodeInputs.addInput(run(_, 0))

  def run(inputs: Seq[ActorRef[Nothing]], i: Int): Behavior[NodeMessage] = Partial[NodeMessage] {
    case WeightedInput(f, _) =>
      val time = new Date(System.currentTimeMillis())
      println(s"Input $i with result $f in ${format.format(time)}")
      run(inputs, i + 1)
  }
}

trait NodeMessage

trait EdgeMessage

case object Ack

case class Input[M: ClassTag](feature: M) extends NodeMessage with EdgeMessage

case class WeightedInput[M: ClassTag](feature: M, weight: Double) extends NodeMessage

case class UpdateBias(bias: Double) extends NodeMessage


case class NodeInputMessage(inputs: Seq[ActorRef[Nothing]], replyTo: ActorRef[Ack.type]) extends NodeMessage

case class NodeOutputMessage(outputs: Seq[ActorRef[Input[_]]], replyTo: ActorRef[Ack.type]) extends NodeMessage





