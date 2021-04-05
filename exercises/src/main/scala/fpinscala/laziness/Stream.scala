package fpinscala.laziness

import Stream._
trait Stream[+A] {

  private def toList: List[A] = {
    @annotation.tailrec
    def go(s: Stream[A], acc: List[A]): List[A] = s match {
      case Cons(h, t) => go(t(), h() :: acc)
      case Empty      => acc

    }

    go(this, List()).reverse
  }

  def foldRight[B](
      z: => B
  )(
      f: (A, => B) => B
  ): B = // The arrow `=>` in front of the argument type `B` means that the function `f` takes its second argument by name and may choose not to evaluate it.
    this match {
      case Cons(h, t) =>
        f(
          h(),
          t().foldRight(z)(f)
        ) // If `f` doesn't evaluate its second argument, the recursion never occurs.
      case _ => z
    }

  def exists(p: A => Boolean): Boolean =
    foldRight(false)((a, b) =>
      p(a) || b
    ) // Here `b` is the unevaluated recursive step that folds the tail of the stream. If `p(a)` returns `true`, `b` will never be evaluated and the computation terminates early.

  @annotation.tailrec
  final def find(f: A => Boolean): Option[A] = this match {
    case Empty      => None
    case Cons(h, t) => if (f(h())) Some(h()) else t().find(f)
  }

  def take(n: Int): Stream[A] = this match {
    case Cons(h, t) if (n >= 1) => cons(h(), t().take(n - 1))
    case _                      => empty
  }

  def takeViaUnfold(n: Int): Stream[A] =
    unfold((this, n)) {
      case (Cons(h, t), n) if n >= 1 => Some(h(), (t(), n - 1))
      case _                         => None
    }

  def drop(n: Int): Stream[A] = this match {
    case Cons(_, t) if (n >= 1) => t().drop(n - 1)
    case _                      => this
  }

  def takeWhile(p: A => Boolean): Stream[A] = this match {
    case Cons(h, t) if p(h()) => cons(h(), t().takeWhile(p))
    case _                    => empty
  }

  def takeWhileViaFold(p: A => Boolean): Stream[A] =
    this.foldRight(empty[A])((a, b) => if (p(a)) cons(a, b) else empty)

  def takeWhileViaUnfold(p: A => Boolean): Stream[A] =
    unfold(this) {
      case Cons(h, t) if p(h()) => Some((h(), t()))
      case _                    => None
    }

  def forAll(p: A => Boolean): Boolean =
    this.foldRight(true)((a, b) => p(a) && b)

  def headOption: Option[A] =
    this.foldRight(None: Option[A])((a, _) => Some(a))

  // 5.7 map, filter, append, flatmap using foldRight. Part of the exercise is
  // writing your own function signatures.

  def map[B](f: A => B): Stream[B] =
    this.foldRight(empty[B])((a, b) => cons(f(a), b))

  def mapViaUnfold[B](f: A => B): Stream[B] =
    unfold(this) {
      case Cons(h, t) => Some((f(h()), t()))
      case _          => None
    }

  def filter(f: A => Boolean): Stream[A] =
    this.foldRight(empty[A])((a, b) => if (f(a)) cons(a, b) else b)

  def append[B >: A](s: => Stream[B]): Stream[B] =
    this.foldRight(s)((a, b) => cons(a, b))

  def flatMap[B](f: A => Stream[B]): Stream[B] =
    this.foldRight(empty[B])((a, b) => f(a).append(b))

  def zipWith[B, C](s2: Stream[B])(f: (A, B) => C): Stream[C] =
    unfold((this, s2)) {
      case (Cons(h1, t1), Cons(h2, t2)) => Some((f(h1(), h2()), (t1(), t2())))
      case _                            => None
    }

  def zip[B](s2: Stream[B]): Stream[(A, B)] =
    zipWith(s2)((_, _))

  def zipWithAll[B, C](
      s2: Stream[B]
  )(f: (Option[A], Option[B]) => C): Stream[C] =
    unfold((this, s2)) {
      case (Cons(h, t), Empty) => Some((f(Some(h()), None), (t(), empty)))
      case (Empty, Cons(h, t)) => Some((f(None, Some(h())), (empty, t())))
      case (Cons(h1, t1), Cons(h2, t2)) =>
        Some((f(Some(h1()), Some(h2())), (t1(), t2())))
      case (Empty, Empty) => None
    }

  def zipAll[B](s2: Stream[B]): Stream[(Option[A], Option[B])] =
    zipWithAll(s2)((_, _))

  def startsWith[B](s: Stream[B]): Boolean =
    ???

}
case object Empty extends Stream[Nothing]
case class Cons[+A](h: () => A, t: () => Stream[A]) extends Stream[A]

object Stream {
  def cons[A](hd: => A, tl: => Stream[A]): Stream[A] = {
    lazy val head = hd
    lazy val tail = tl
    Cons(() => head, () => tail)
  }

  def empty[A]: Stream[A] = Empty

  def apply[A](as: A*): Stream[A] =
    if (as.isEmpty) empty
    else cons(as.head, apply(as.tail: _*))

  val ones: Stream[Int] = Stream.cons(1, ones)

  val onesViaUnfold: Stream[Int] = unfold(1)(_ => Some((1, 1)))

  def constant[A](a: A): Stream[A] = {
    lazy val tail: Stream[A] = Cons(() => a, () => tail)
    tail
  }

  def constantViaUnfold[A](a: A): Stream[A] =
    unfold(a)(_ => Some((a, a)))

  def from(n: Int): Stream[Int] =
    Stream.cons(n, from(n + 1))

  def fromViaUnfold(n: Int): Stream[Int] =
    unfold(n)(n => Some((n, n + 1)))

  def fibStream: Stream[Int] = {
    def go(f0: Int, f1: Int): Stream[Int] =
      cons(f0, go(f1, f0 + f1))
    go(0, 1)
  }

  def fibStreamViaUnfold: Stream[Int] =
    unfold((0, 1)) { case (f0, f1) =>
      Some((f0, (f1, f0 + f1)))
    }

  def unfold[A, S](z: S)(f: S => Option[(A, S)]): Stream[A] =
    f(z) match {
      case Some((a, s)) => cons(a, unfold(s)(f))
      case None         => empty
    }

  def unfoldViaFold[A, S](z: S)(f: S => Option[(A, S)]): Stream[A] =
    f(z).fold(empty[A])(st => cons(st._1, unfoldViaFold(st._2)(f)))

  def unfoldViaMap[A, S](z: S)(f: S => Option[(A, S)]): Stream[A] =
    f(z).map(st => cons(st._1, unfoldViaMap(st._2)(f))).getOrElse(empty)

  def main(args: Array[String]): Unit = {
    val s = Stream(1, 2, 3, 4)
    val t = Stream(5, 6, 7, 8)

    println(s.toList)
    println(s.take(2).toList)
    println(s.drop(2).toList)
    println(s.takeWhile(_ < 4).toList)
    println(s.forAll(_ > 0))
    println(s.headOption)
    println(s.append(t).toList)
    println(from(12).take(3).toList)
    println(fibStream.take(10).toList)
    println(fibStreamViaUnfold.take(10).toList)
    println(fromViaUnfold(12).take(3).toList)
    println(constantViaUnfold(12).take(3).toList)
    println(s.takeViaUnfold(2).toList)
  }

}
