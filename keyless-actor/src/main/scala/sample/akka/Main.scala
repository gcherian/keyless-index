package sample.akka

/**
  * Created by gcherian on 1/15/2017.
  */


import akka.util.Timeout
import keyless.actor.node._

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.typed._
import akka.typed.ScalaDSL._
import akka.typed.AskPattern._

object Main extends App {

  //**************************************
  // I-----
  //  \    |
  //   ----H
  //  / \ / \
  // I   -   O
  //  \ / \ /
  //   ----H
  //  /    |
  // I-----
  //**************************************
  override def main(params: Array[String]) = {

    val main: Behavior[Unit] =
      Full {
        case Sig(ctx, PreStart) =>

          //Input layer nodes.
          val inputLayer1 = ctx.spawn(InputNode.props(), "highAmount")
          val inputLayer2 = ctx.spawn(InputNode.props(), "distantLocation")
          val inputLayer3 = ctx.spawn(InputNode.props(), "frequentTransaction")

          //Hidden layer nodes.
          val hiddenLayer1 = ctx.spawn(Perceptron.props(), "hiddenLayer1")
          val hiddenLayer2 = ctx.spawn(Perceptron.props(), "hiddenLayer2")

          //Output layer nodes.
          val outputLayer = ctx.spawn(Perceptron.props(), "Fraud")

          //Printer.
          val printer = ctx.spawn(OutputNode.props(), "printer")

          //Edges.
          val edgei1h1 = ctx.spawn(EdgeNode.props(), "edgei1h1")
          val edgei1h2 = ctx.spawn(EdgeNode.props(), "edgei1h2")
          val edgei2h1 = ctx.spawn(EdgeNode.props(), "edgei2h1")
          val edgei2h2 = ctx.spawn(EdgeNode.props(), "edgei2h2")
          val edgei3h1 = ctx.spawn(EdgeNode.props(), "edgei3h1")
          val edgei3h2 = ctx.spawn(EdgeNode.props(), "edgei3h2")

          val edgeh1o1 = ctx.spawn(EdgeNode.props(), "edgeh1o1")
          val edgeh2o1 = ctx.spawn(EdgeNode.props(), "edgeh2o1")

          val edgeo1p1 = ctx.spawn(EdgeNode.props(), "edgeo1p1")

          implicit val t = Timeout(10 seconds)
          val d = t.duration

          type ack = ActorRef[Ack.type]

          Await.result(edgei1h1 ? (EdgeInputMessage(inputLayer1, _: ack)), d)
          Await.result(edgei1h1 ? (EdgeOutputMessage(hiddenLayer1, _: ack)), d)

          Await.result(edgei1h2 ? (EdgeInputMessage(inputLayer1, _: ack)), d)
          Await.result(edgei1h2 ? (EdgeOutputMessage(hiddenLayer2, _: ack)), d)

          Await.result(edgei2h1 ? (EdgeInputMessage(inputLayer2, _: ack)), d)
          Await.result(edgei2h1 ? (EdgeOutputMessage(hiddenLayer1, _: ack)), d)

          Await.result(edgei2h2 ? (EdgeInputMessage(inputLayer2, _: ack)), d)
          Await.result(edgei2h2 ? (EdgeOutputMessage(hiddenLayer2, _: ack)), d)

          Await.result(edgei3h1 ? (EdgeInputMessage(inputLayer3, _: ack)), d)
          Await.result(edgei3h1 ? (EdgeOutputMessage(hiddenLayer1, _: ack)), d)

          Await.result(edgei3h2 ? (EdgeInputMessage(inputLayer3, _: ack)), d)
          Await.result(edgei3h2 ? (EdgeOutputMessage(hiddenLayer2, _: ack)), d)

          //Hidden layer to output layer edges.
          Await.result(edgeh1o1 ? (EdgeInputMessage(hiddenLayer1, _: ack)), d)
          Await.result(edgeh1o1 ? (EdgeOutputMessage(outputLayer, _: ack)), d)

          Await.result(edgeh2o1 ? (EdgeInputMessage(hiddenLayer2, _: ack)), d)
          Await.result(edgeh2o1 ? (EdgeOutputMessage(outputLayer, _: ack)), d)

          //Output layer to printer.
          Await.result(edgeo1p1 ? (EdgeInputMessage(outputLayer, _: ack)), d)
          Await.result(edgeo1p1 ? (EdgeOutputMessage(printer, _: ack)), d)

          //Linking edges to nodes.
          Await.result(inputLayer1 ? (NodeOutputMessage(Seq(edgei1h1, edgei1h2), _: ack)), d)
          Await.result(inputLayer2 ? (NodeOutputMessage(Seq(edgei2h1, edgei2h2), _: ack)), d)
          Await.result(inputLayer3 ? (NodeOutputMessage(Seq(edgei3h1, edgei3h2), _: ack)), d)

          Await.result(hiddenLayer1 ? (NodeInputMessage(Seq(edgei1h1, edgei2h1, edgei3h1), _: ack)), d)
          Await.result(hiddenLayer1 ? (NodeOutputMessage(Seq(edgeh1o1), _: ack)), d)

          Await.result(hiddenLayer2 ? (NodeInputMessage(Seq(edgei1h2, edgei2h2, edgei3h2), _: ack)), d)
          Await.result(hiddenLayer2 ? (NodeOutputMessage(Seq(edgeh2o1), _: ack)), d)

          Await.result(outputLayer ? (NodeInputMessage(Seq(edgeh1o1, edgeh2o1), _: ack)), d)
          Await.result(outputLayer ? (NodeOutputMessage(Seq(edgeo1p1), _: ack)), d)

          Await.result(printer ? (NodeInputMessage(Seq(edgeo1p1), _: ack)), d)

          var i = 0
          scala.io.Source.fromFile("C:\\Users\\georg\\IdeaProjects\\keyless-index\\keyless-actor\\src\\main\\resources\\data.csv")
            .getLines()
            .foreach { l =>
              val splits = l.split(",")

              inputLayer1 ! Input(splits(0).toDouble)
              inputLayer2 ! Input(splits(1).toDouble)
              inputLayer3 ! Input(splits(2).toDouble)

              if (i == 2) {
                ctx.stop(hiddenLayer1)
              }

              i = i + 1
            }

          Same
        case Sig(_, Terminated(ref)) =>
          Stopped
      }

    val system = ActorSystem("akka", Props(main))
  }
}

