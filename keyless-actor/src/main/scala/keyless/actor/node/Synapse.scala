package keyless.actor.node

import akka.typed.ScalaDSL.Partial
import akka.typed.{ActorRef, Behavior, Props}


/**
  * Created by gcherian on 1/15/2017.
  */


class Synapse extends Neuron {


  object Dendrite {
    def addInput[T](behavior: ActorRef[Nothing] => Behavior[T]) = Partial[T] {
      case SynapseInputSignal(i, r) =>
        r ! Ack
        behavior(i)
    }
  }

  object Axon {


    def addOutput[T](behavior: (ActorRef[Nothing], ActorRef[Data]) => Behavior[T], input: ActorRef[Nothing]) = Partial[T] {
      case SynapseOutputSignal(o, r) =>
        r ! Ack
        behavior(input, o)
    }
  }




}
case class SynapseInputSignal(input: ActorRef[Nothing], replyTo: ActorRef[Ack.type]) extends SynapseSignal

case class SynapseOutputSignal(output: ActorRef[Data], replyTo: ActorRef[Ack.type]) extends SynapseSignal

case class UpdateWeight(weight: Double) extends SynapseSignal

object HiddenSynapse extends Synapse {
  def props() = Props(receive)

  def receive = Dendrite.addInput(Axon.addOutput(run(_, _, 0.3), _))

  def run(input: ActorRef[Nothing], output: ActorRef[Data], weight: Double): Behavior[SynapseSignal] = Partial[SynapseSignal] {
    case Data(id,di,f,w) =>
      output ! Data(id,di,f, weight)
      run(input, output, weight)

    case UpdateWeight(newWeight) =>
      run(input, output, newWeight)
  }
}
