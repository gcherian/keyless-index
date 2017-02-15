package keyless.actor.node

/**
  * Created by gcherian on 1/15/2017.
  */

import akka.typed.ScalaDSL._
import akka.typed.{ActorRef, Behavior}

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

trait NodeMessage

trait EdgeMessage

case object Ack

case class Input[M: ClassTag](feature: M) extends NodeMessage with EdgeMessage

case class WeightedInput[M: ClassTag](feature: M, weight: Double) extends NodeMessage

case class UpdateBias(bias: Double) extends NodeMessage


case class NodeInputMessage(inputs: Seq[ActorRef[Nothing]], replyTo: ActorRef[Ack.type]) extends NodeMessage

case class NodeOutputMessage(outputs: Seq[ActorRef[Input[_]]], replyTo: ActorRef[Ack.type]) extends NodeMessage





