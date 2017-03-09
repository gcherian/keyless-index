package keyless.actor.node

/**
  * Created by gcherian on 1/15/2017.
  */

import java.text.SimpleDateFormat
import java.util.Date

import akka.typed.ScalaDSL._
import akka.typed.{ActorRef, Behavior, Props}

import scala.reflect.ClassTag

class Neuron {


  object Dendrites {
    def addInputs[T](behavior: Seq[ActorRef[Nothing]] => Behavior[T]) = Partial[T] {
      case NeuronInputSignal(i, r) =>
        r ! Ack
        behavior(i)
    }
  }

  object Axons {
    def addOutputs[T](behavior: (Seq[ActorRef[Nothing]], Seq[ActorRef[Data]]) => Behavior[T], inputs: Seq[ActorRef[Nothing]]) = Partial[T] {
      case NeuronOutputSignal(o, r) =>
        r ! Ack
        behavior(inputs, o)
    }
  }

}

object InputNeuron extends Neuron {

  def props() = Props(receive)

  def receive = Axons.addOutputs(run, Seq())

  def run(inputs: Seq[ActorRef[Nothing]], outputs: Seq[ActorRef[Data]]): Behavior[NeuronSignal] = Partial[NeuronSignal] {
    case i: Data =>
      outputs.foreach(_ ! i)
      run(inputs, outputs)
  }
}

object OutputNeuron extends Neuron {


  def props() = Props(receive)

  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  def receive = Dendrites.addInputs(run(_, 0))

  def run(inputs: Seq[ActorRef[Nothing]], i: Int): Behavior[NeuronSignal] = Partial[NeuronSignal] {
    case Data(_,_,f, _) =>
      val time = new Date(System.currentTimeMillis())
      println(s"Input $i with result $f in ${format.format(time)}")
      run(inputs, i + 1)
  }
}

trait NeuronSignal

trait SynapseSignal

case object Ack

case class  Data(id:String, dimension:Int, feature:Double, weight:Double) extends NeuronSignal with SynapseSignal

case class UpdateBias(bias: Double) extends NeuronSignal

case class NeuronInputSignal(inputs: Seq[ActorRef[Nothing]], replyTo: ActorRef[Ack.type]) extends NeuronSignal

case class NeuronOutputSignal(outputs: Seq[ActorRef[Data]], replyTo: ActorRef[Ack.type]) extends NeuronSignal





