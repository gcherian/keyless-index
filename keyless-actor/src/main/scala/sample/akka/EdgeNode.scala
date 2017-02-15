package sample.akka

/**
  * Created by gcherian on 2/1/2017.
  */

import akka.typed.ScalaDSL.Partial
import akka.typed.{Behavior, ActorRef, Props}
import keyless.actor.node._

object EdgeNode extends Edge[Double] {
  def props() = Props(receive)

  def receive = EdgeInputs.addInput(EdgeOutputs.addOutput(run(_, _, 0.3), _))

  def run(input: ActorRef[Nothing], output: ActorRef[WeightedInput[_]], weight: Double): Behavior[EdgeMessage] = Partial[EdgeMessage] {
    case Input(f) =>
      output ! WeightedInput(f, weight)
      run(input, output, weight)

    case UpdateWeight(newWeight) =>
      run(input, output, newWeight)
  }
}
