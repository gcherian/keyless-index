package keyless.actor.node

import akka.typed.ScalaDSL.Partial
import akka.typed.{ActorRef, Behavior, Props}

import scala.reflect.ClassTag


/**
  * Created by gcherian on 1/15/2017.
  */


class Edge[M: ClassTag] extends Node[M] {


  object EdgeInputs {
    def addInput[T](behavior: ActorRef[Nothing] => Behavior[T]) = Partial[T] {
      case EdgeInputMessage(i, r) =>
        r ! Ack
        behavior(i)
    }
  }

  object EdgeOutputs {


    def addOutput[T](behavior: (ActorRef[Nothing], ActorRef[WeightedInput[_]]) => Behavior[T], input: ActorRef[Nothing]) = Partial[T] {
      case EdgeOutputMessage(o, r) =>
        r ! Ack
        behavior(input, o)
    }
  }

}


case class EdgeInputMessage(input: ActorRef[Nothing], replyTo: ActorRef[Ack.type]) extends EdgeMessage

case class EdgeOutputMessage(output: ActorRef[WeightedInput[_]], replyTo: ActorRef[Ack.type]) extends EdgeMessage

case class UpdateWeight(weight: Double) extends EdgeMessage
