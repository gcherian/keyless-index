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

    val inputs =params(0).toInt
    val layers =if (params(1) != null) params(1).toInt else 3

    val main: Behavior[Unit] =
      Full {
        case Sig(context, PreStart) =>

          //Neurons

          val inputLayers: Array[ActorRef[NeuronSignal]] = Array.ofDim(inputs)
          for ( i<- 0 to inputs-1) {inputLayers(i)=context.spawn(InputNeuron.props(),"InputLayer"+i)}

          val hiddenLayers: Array[Array[ActorRef[NeuronSignal]]] = Array.ofDim(layers, inputs)
          for (i <- 0 to layers-1)
            for (j <- 0 to inputs-1) {
              hiddenLayers(i)(j) = context.spawn(Perceptron.props(),"HiddenLayer"+i+"_"+j)
            }


          val outputLayer = context.spawn(Perceptron.props(), "OutputLayer")

          val printerLayer = context.spawn(OutputNeuron.props(), "Printer")

          //Synapses
          val synapses: Array[Array[Array[ActorRef[SynapseSignal]]]] = Array.ofDim(layers,inputs, inputs)
          for (i <- 0 to layers-1)
            for (j<-0 to inputs-1)
              for (k <- 0 to inputs-1) {
                synapses(i)(j)(k)=context.spawn(SynapseTerminal.props(),"SynapseHidden"+i+"_"+j+"_"+k)
              }

          val outputSynapses:Array[ActorRef[SynapseSignal]] = Array.ofDim(inputs)
          for (i <- 0 to inputs-1)
            outputSynapses(i) = context.spawn(SynapseTerminal.props(),"SynapseOutput"+i)

          val printerSynapse = context.spawn(SynapseTerminal.props(), "SynapsePrinter")


          implicit val t = Timeout(20 seconds)
          val d = t.duration

          type ack = ActorRef[Ack.type]

          for (i <-0 to layers-1)
            for (j <- 0 to inputs-1)
              for (k <- 0 to inputs-1){
                Await.result(synapses(i)(j)(k) ? (SynapseInputSignal(inputLayers(j), _: ack)), d)
                Await.result(synapses(i)(j)(k) ? (SynapseOutputSignal(hiddenLayers(i)(j), _: ack)), d)
            }


          for (i<-0 to inputs-1){
            Await.result(outputSynapses(i)? (SynapseInputSignal(hiddenLayers(layers-1)(i),_:ack)),d)
            Await.result(outputSynapses(i) ? (SynapseOutputSignal(outputLayer, _: ack)), d)

          }

          Await.result(printerSynapse ? (SynapseInputSignal(outputLayer,_:ack)),d)
          Await.result(printerSynapse ? (SynapseOutputSignal(printerLayer, _: ack)), d)



          for (i <- 0 to inputs-1){
            val connections: Array[ActorRef[SynapseSignal]] = Array.ofDim(inputs)
            for (j <- 0 to inputs-1)
              connections(j)=synapses(0)(i)(j)
            val outputSignal = NeuronOutputSignal(connections, _: ack)
            Await.result(inputLayers(i)? outputSignal,d)
          }



          for (i <- 1 to layers-2)
            for (j <- 0 to inputs-1) {

              val forwardConnections: Array[ActorRef[SynapseSignal]] = Array.ofDim(inputs)

              val backwardConnections: Array[ActorRef[SynapseSignal]] = Array.ofDim(inputs)

              for (k <- 0 to inputs-1) {
                forwardConnections(k)=synapses(i)(j)(k)
                backwardConnections(k)=synapses(i+1)(j)(k)

              }
              val synapseInputSignal = NeuronInputSignal(forwardConnections, _: ack)
              val synapseOutputSignal = NeuronOutputSignal(backwardConnections, _: ack)

              Await.result(hiddenLayers(i)(j)?synapseInputSignal,d)
              Await.result(hiddenLayers(i)(j)? synapseOutputSignal,d)
            }


          for (i <-0 to inputs-1 ) {
            val outputConnections: Array[ActorRef[NeuronSignal]] = Array.ofDim(inputs)

            for (k <- 0 to inputs-1)
              outputConnections(k) = hiddenLayers(layers-1)(k)

            val neuronOutputSignal = NeuronInputSignal(outputConnections, _: ack)
            Await.result(outputLayer?neuronOutputSignal,d)


          }

          Await.result(printerLayer ? (NeuronInputSignal(Seq(printerSynapse), _: ack)), d)

          var i = 0
          scala.io.Source.fromFile("keyless-actor/src/main/resources/data.csv")
            .getLines()
            .foreach { l =>
              val splits = l.split(",")
              
              for (i <- 0 to inputs) {
                inputLayers(i) ! Input(splits(i).toDouble)
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

