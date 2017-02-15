package sample.akka

/**
  * Created by gcherian on 1/15/2017.
  */

import keyless.actor.node._
import akka.typed.{Behavior, ActorRef, Props}
import akka.typed.ScalaDSL._

object Perceptron extends Edge[Double] {


  import Neuron._

  def props() = Props(behaviour)

  def behaviour = NodeInputs.addInput(NodeOutputs.addOutput(feedForward(_, _, 0.2, sigmoid, Vector(), Vector()), _))

  private def allInputsAvailable(w: Vector[Double], f: Vector[Double], in: Seq[ActorRef[Nothing]]) =
    w.length == in.length && f.length == in.length

  def feedForward(
                   inputs: Seq[ActorRef[Nothing]],
                   outputs: Seq[ActorRef[Input[Double]]],
                   bias: Double,
                   activationFunction: Double => Double,
                   weightsT: Vector[Double],
                   featuresT: Vector[Double]): Behavior[NodeMessage] = Partial[NodeMessage] {

    case WeightedInput(f: Double, w: Double) =>
      val featuresTplusOne = featuresT :+ f
      val weightsTplusOne = weightsT :+ w

      if (allInputsAvailable(featuresTplusOne, weightsTplusOne, inputs)) {
        val activation = activationFunction(weightsTplusOne.zip(featuresTplusOne).map(x => x._1 * x._2).sum + bias)
        println(s"Activation $activation using features $featuresTplusOne")
        outputs.foreach(_ ! Input[Double](activation))

        feedForward(inputs, outputs, bias, activationFunction, Vector(), Vector())
      } else {
        feedForward(inputs, outputs, bias, activationFunction, weightsTplusOne, featuresTplusOne)
      }

    case UpdateBias(newBias) =>
      feedForward(inputs, outputs, newBias, activationFunction, weightsT, featuresT)
  }
}
