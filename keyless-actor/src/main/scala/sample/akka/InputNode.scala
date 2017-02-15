package sample.akka

/**
  * Created by gcherian on 1/15/2017.
  */

import akka.typed.ScalaDSL._
import akka.typed.{ActorRef, Behavior, Props}
import keyless.actor.node._

object InputNode extends Node[Double] {

  def props() = Props(receive)

  def receive = NodeOutputs.addOutput(run, Seq())

  def run(inputs: Seq[ActorRef[Nothing]], outputs: Seq[ActorRef[Input[_]]]): Behavior[NodeMessage] = Partial[NodeMessage] {
    case i: Input[_] =>
      outputs.foreach(_ ! i)
      run(inputs, outputs)
  }
}

