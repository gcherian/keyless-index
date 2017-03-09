package keyless.actor

import akka.typed.ScalaDSL.Partial
import akka.typed.{ActorRef, Behavior, Props}
import keyless.actor.node._
import keyless.index.{FullUniqueIndex, NonUniqueIndex}
import neural.net.ActiivationFunctions

import scala.reflect.ClassTag

/**
  * Created by gcherian on 1/16/2017.
  */
class Recognizer(val name: String) extends Synapse {

  import ActiivationFunctions._



  def behaviour = Dendrites.addInputs(Axons.addOutputs(feedForward(_, _, 0.2, sigmoid, Vector(), Vector()), _))

  private def allInputsAvailable(w: Vector[Double], f: Vector[Double], in: Seq[ActorRef[Nothing]]) =
    w.length == in.length && f.length == in.length

  def feedForward(
                   inputs: Seq[ActorRef[Nothing]],
                   outputs: Seq[ActorRef[Data]],
                   bias: Double,
                   activationFunction: Double => Double,
                   weights: Vector[Double],
                   features: Vector[Double]): Behavior[NeuronSignal] = Partial[NeuronSignal] {

    case Data(id,di,f: Double, w: Double) =>


      val data = new Data(id, di, f, w)

      memory.put(data)

      val featureGroup = features :+ f
      val weightGroup = weights :+ w

      if (allInputsAvailable(featureGroup, weightGroup, inputs)) {
        val activation = activationFunction(weightGroup.zip(featureGroup).map(x => x._1 * x._2).sum + bias)
        println(s"Activation $activation using features $featureGroup for Data : $id $di $f $w")
        outputs.foreach(_ ! Data(id,di,activation,w))

        feedForward(inputs, outputs, bias, activationFunction, Vector(), Vector())
      } else {
        feedForward(inputs, outputs, bias, activationFunction, weightGroup, featureGroup)
      }

    case UpdateBias(newBias) =>
      feedForward(inputs, outputs, newBias, activationFunction, weights, features)
  }

  import java.util.function.{Function => JFunction, Predicate => JPredicate}


  implicit def toJavaFunction[A, B](f: Function1[A, B]) = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }


  implicit def toJavaPredicate[A](f: Function1[A, Boolean]) = new JPredicate[A] {
    override def test(a: A): Boolean = f(a)
  }


  val memory:NonUniqueIndex[Data] = new NonUniqueIndex[Data]((d:Data)=>d.id,(d:Data)=>d.dimension)

  def props()= Props(behaviour)

}

object Recognizer {
  def apply(name:String) = {
    new Recognizer(name)
  }


}


