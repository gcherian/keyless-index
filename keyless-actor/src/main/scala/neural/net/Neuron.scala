package neural.net

/**
  * Created by gcherian on 1/15/2017.
  */

import scala.math.exp

object Neuron {
  val simple: Double => Double = x => if (x > 0.5) 1 else 0
  val sigmoid: Double => Double = x => 1 / (1 + exp(-x))
}
