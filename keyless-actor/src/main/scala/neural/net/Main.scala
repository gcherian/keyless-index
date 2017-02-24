package neural.net

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


  override def main(params: Array[String]) = {

    val main: Behavior[Unit] =
      Full {
        case Sig(context, PreStart) =>

          val inputLayer1 = context.spawn(InputNeuron.props(), "highAmount")
          val inputLayer2 = context.spawn(InputNeuron.props(), "distantLocation")
          val inputLayer3 = context.spawn(InputNeuron.props(), "frequentTransaction")

          val hiddenLayer1 = context.spawn(Perceptron.props(), "hiddenLayer1")
          val hiddenLayer2 = context.spawn(Perceptron.props(), "hiddenLayer2")

          val outputLayer = context.spawn(Perceptron.props(), "Fraud")

          val printer = context.spawn(OutputNeuron.props(), "printer")

          val edgei1h1 = context.spawn(SynapseTerminal.props(), "edgei1h1")
          val edgei1h2 = context.spawn(SynapseTerminal.props(), "edgei1h2")
          val edgei2h1 = context.spawn(SynapseTerminal.props(), "edgei2h1")
          val edgei2h2 = context.spawn(SynapseTerminal.props(), "edgei2h2")
          val edgei3h1 = context.spawn(SynapseTerminal.props(), "edgei3h1")
          val edgei3h2 = context.spawn(SynapseTerminal.props(), "edgei3h2")

          val edgeh1o1 = context.spawn(SynapseTerminal.props(), "edgeh1o1")
          val edgeh2o1 = context.spawn(SynapseTerminal.props(), "edgeh2o1")

          val edgeo1p1 = context.spawn(SynapseTerminal.props(), "edgeo1p1")

          implicit val t = Timeout(10 seconds)
          val d = t.duration

          type ack = ActorRef[Ack.type]

          Await.result(edgei1h1 ? (SynapseInputSignal(inputLayer1, _: ack)), d)
          Await.result(edgei1h1 ? (SynapseOutputSignal(hiddenLayer1, _: ack)), d)

          Await.result(edgei1h2 ? (SynapseInputSignal(inputLayer1, _: ack)), d)
          Await.result(edgei1h2 ? (SynapseOutputSignal(hiddenLayer2, _: ack)), d)

          Await.result(edgei2h1 ? (SynapseInputSignal(inputLayer2, _: ack)), d)
          Await.result(edgei2h1 ? (SynapseOutputSignal(hiddenLayer1, _: ack)), d)

          Await.result(edgei2h2 ? (SynapseInputSignal(inputLayer2, _: ack)), d)
          Await.result(edgei2h2 ? (SynapseOutputSignal(hiddenLayer2, _: ack)), d)

          Await.result(edgei3h1 ? (SynapseInputSignal(inputLayer3, _: ack)), d)
          Await.result(edgei3h1 ? (SynapseOutputSignal(hiddenLayer1, _: ack)), d)

          Await.result(edgei3h2 ? (SynapseInputSignal(inputLayer3, _: ack)), d)
          Await.result(edgei3h2 ? (SynapseOutputSignal(hiddenLayer2, _: ack)), d)

          //Hidden layer to output layer edges.
          Await.result(edgeh1o1 ? (SynapseInputSignal(hiddenLayer1, _: ack)), d)
          Await.result(edgeh1o1 ? (SynapseOutputSignal(outputLayer, _: ack)), d)

          Await.result(edgeh2o1 ? (SynapseInputSignal(hiddenLayer2, _: ack)), d)
          Await.result(edgeh2o1 ? (SynapseOutputSignal(outputLayer, _: ack)), d)

          //Output layer to printer.
          Await.result(edgeo1p1 ? (SynapseInputSignal(outputLayer, _: ack)), d)
          Await.result(edgeo1p1 ? (SynapseOutputSignal(printer, _: ack)), d)

          //Linking edges to nodes.
          Await.result(inputLayer1 ? (NeuronOutputSignal(Seq(edgei1h1, edgei1h2), _: ack)), d)
          Await.result(inputLayer2 ? (NeuronOutputSignal(Seq(edgei2h1, edgei2h2), _: ack)), d)
          Await.result(inputLayer3 ? (NeuronOutputSignal(Seq(edgei3h1, edgei3h2), _: ack)), d)

          Await.result(hiddenLayer1 ? (NeuronInputSignal(Seq(edgei1h1, edgei2h1, edgei3h1), _: ack)), d)
          Await.result(hiddenLayer1 ? (NeuronOutputSignal(Seq(edgeh1o1), _: ack)), d)

          Await.result(hiddenLayer2 ? (NeuronInputSignal(Seq(edgei1h2, edgei2h2, edgei3h2), _: ack)), d)
          Await.result(hiddenLayer2 ? (NeuronOutputSignal(Seq(edgeh2o1), _: ack)), d)

          Await.result(outputLayer ? (NeuronInputSignal(Seq(edgeh1o1, edgeh2o1), _: ack)), d)
          Await.result(outputLayer ? (NeuronOutputSignal(Seq(edgeo1p1), _: ack)), d)

          Await.result(printer ? (NeuronInputSignal(Seq(edgeo1p1), _: ack)), d)

          var i = 0
          scala.io.Source.fromFile("keyless-actor/src/main/resources/data.csv")//TODO Read from Classpath
            .getLines()
            .foreach { l =>
              val splits = l.split(",")

              inputLayer1 ! Input(splits(0).toDouble)
              inputLayer2 ! Input(splits(1).toDouble)
              inputLayer3 ! Input(splits(2).toDouble)

              if (i == 2) {
                context.stop(hiddenLayer1)
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

