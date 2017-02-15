package sample.akka

/**
  * Created by gcherian on 1/15/2017.
  */

import java.text.SimpleDateFormat
import java.util.Date

import akka.typed.{Behavior, ActorRef, Props}
import akka.typed.ScalaDSL._
import keyless.actor.node._

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