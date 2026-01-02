package fpinscala.exercises.errorhandling

import scala.util.control.NonFatal
// Hide std library `Either` since we are writing our own in this chapter
import scala.{ Either as _, Left as _, Right as _ }

enum Either[+E, +A]:

  case Left(get: E)
  case Right(get: A)

  def map[B](f: A => B): Either[E, B] = this match
    case Left(a)  => Left(a)
    case Right(b) => Right(f(b))

  def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B] = this match
    case Left(a)  => Left(a)
    case Right(b) => f(b)

  def orElse[EE >: E, B >: A](b: => Either[EE, B]): Either[EE, B] = this match
    case Left(_)  => b
    case Right(v) => Right(v)

  def map2[EE >: E, B, C](b: Either[EE, B])(f: (A, B) => C): Either[EE, C] =
    for
      a  <- this
      bb <- b
    yield f(a, bb)

object Either:

  def traverse[E, A, B](es: List[A])(f: A => Either[E, B]): Either[E, List[B]] = es
    .foldRight[Either[E, List[B]]](Right(Nil))((a, acc) => f(a).map2(acc)(_ :: _))

  def sequence[E, A](es: List[Either[E, A]]): Either[E, List[A]] = traverse(es)(identity)

  def mean(xs: IndexedSeq[Double]): Either[String, Double] =
    if xs.isEmpty then Left("mean of empty list!") else Right(xs.sum / xs.length)

  def safeDiv(x: Int, y: Int): Either[Throwable, Int] =
    try Right(x / y)
    catch case NonFatal(t) => Left(t)

  def catchNonFatal[A](a: => A): Either[Throwable, A] =
    try Right(a)
    catch case NonFatal(t) => Left(t)

  def map2All[E, A, B, C](a: Either[List[E], A], b: Either[List[E], B], f: (A, B) => C): Either[List[E], C] =
    (a, b) match
      case (Left(e), Right(_))    => Left(e)
      case (Right(_), Left(e))    => Left(e)
      case (Left(e1), Left(e2))   => Left(e1 ++ e2)
      case (Right(v1), Right(v2)) => Right(f(v1, v2))

  def traverseAll[E, A, B](as: List[A], f: A => Either[List[E], B]): Either[List[E], List[B]] = as
    .foldRight[Either[List[E], List[B]]](Right(Nil))((a, acc) => map2All(f(a), acc, _ :: _))

  def sequenceAll[E, A](as: List[Either[List[E], A]]): Either[List[E], List[A]] = traverseAll(as, identity)
