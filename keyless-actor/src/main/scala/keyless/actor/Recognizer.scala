package keyless.actor

import akka.actor.Actor
import keyless.actor.node.Neuron
import keyless.index.FullUniqueIndex

import scala.reflect.ClassTag

/**
  * Created by gcherian on 1/16/2017.
  */
class Recognizer[P: ClassTag](val name: String) extends Neuron {

  import java.util.function.{Function => JFunction, Predicate => JPredicate}


  implicit def toJavaFunction[A, B](f: Function1[A, B]) = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }


  implicit def toJavaPredicate[A](f: Function1[A, Boolean]) = new JPredicate[A] {
    override def test(a: A): Boolean = f(a)
  }

  val parents: FullUniqueIndex[Recognizer[_]] = new FullUniqueIndex[Recognizer[_]]((r: Recognizer[_]) => r.name)
  val children: FullUniqueIndex[Recognizer[_]] = new FullUniqueIndex[Recognizer[_]]((r: Recognizer[_]) => r.name)
  val memory: FullUniqueIndex[P] = new FullUniqueIndex[P]()




}
