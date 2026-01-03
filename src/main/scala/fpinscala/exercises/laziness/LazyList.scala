package fpinscala.exercises.laziness

import fpinscala.exercises.laziness.LazyList.unfold

enum LazyList[+A]:

  case Empty
  case Cons(h: () => A, t: () => LazyList[A])

  def toList: List[A] =
    @annotation.tailrec
    def go(ll: LazyList[A], acc: List[A]): List[A] = ll match
      case Cons(h, t) => go(t(), h() :: acc)
      case Empty      => acc.reverse
    go(this, Nil)

  def foldRight[B](z: => B)(
    f: (A, => B) => B
  ): B = // The arrow `=>` in front of the argument type `B` means that the function `f` takes its second argument by name and may choose not to evaluate it.
    this match
      case Cons(h, t) =>
        f(h(), t().foldRight(z)(f)) // If `f` doesn't evaluate its second argument, the recursion never occurs.
      case _ => z

  def exists(p: A => Boolean): Boolean =
    foldRight(false)((a, b) =>
      p(a) || b
    ) // Here `b` is the unevaluated recursive step that folds the tail of the lazy list. If `p(a)` returns `true`, `b` will never be evaluated and the computation terminates early.

  @annotation.tailrec
  final def find(f: A => Boolean): Option[A] = this match
    case Empty      => None
    case Cons(h, t) => if f(h()) then Some(h()) else t().find(f)

  def take(n: Int): LazyList[A] = this match
    case Cons(h, t) if n == 1 => LazyList.cons(h(), LazyList.empty)
    case Cons(h, t) if n > 1  => LazyList.cons(h(), t().take(n - 1))
    case _                    => LazyList.empty

  def drop(n: Int): LazyList[A] =
    @annotation.tailrec
    def go(ll: LazyList[A], n: Int): LazyList[A] = ll match
      case Cons(h, t) if n > 0  => go(t(), n - 1)
      case Cons(h, t) if n <= 0 => LazyList.cons(h(), t())
      case _                    => LazyList.empty
    go(this, n)

  def takeWhile(p: A => Boolean): LazyList[A] = this match
    case Cons(h, t) if p(h()) => LazyList.cons(h(), t().takeWhile(p))
    case _                    => LazyList.empty

  def forAll(p: A => Boolean): Boolean = foldRight(true)((a, b) => p(a) && b)

  def takeWhile_1(p: A => Boolean): LazyList[A] =
    foldRight(LazyList.empty)((a, b) => if p(a) then LazyList.cons(a, b) else LazyList.empty)

  def headOption: Option[A] = foldRight(None: Option[A])((h, _) => Some(h))

  // 5.7 map, filter, append, flatmap using foldRight. Part of the exercise is
  // writing your own function signatures.

  def map[B](f: A => B): LazyList[B] = foldRight(LazyList.empty)((a, acc) => LazyList.cons(f(a), acc))

  def filter(f: A => Boolean): LazyList[A] =
    foldRight(LazyList.empty)((a, acc) => if f(a) then LazyList.cons(a, acc) else acc)

  def append[B >: A](l2: => LazyList[B]): LazyList[B] = foldRight(l2)((a, acc) => LazyList.cons(a, acc))

  def flatMap[B](f: A => LazyList[B]): LazyList[B] = foldRight(LazyList.empty)((a, acc) => f(a).append(acc))

  def mapViaUnfold[B](f: A => B): LazyList[B] = LazyList.unfold(this):
    case Cons(h, t) => Some((f(h()), t()))
    case _          => None

  def takeViaUnfold(n: Int): LazyList[A] = LazyList.unfold((this, n)):
    case (Cons(h, t), 1)          => Some((h(), (LazyList.empty, 0)))
    case (Cons(h, t), n) if n > 1 => Some(h(), (t(), n - 1))
    case _                        => None

  def takeWhileViaUnfold(f: A => Boolean): LazyList[A] = LazyList.unfold(this):
    case Cons(h, t) if f(h()) => Some(h(), t())
    case _                    => None

  def zipAll[B](that: LazyList[B]): LazyList[(Option[A], Option[B])] = LazyList.unfold((this, that)):
    case (Empty, Empty)               => None
    case (Cons(h1, t1), Empty)        => Some((Some(h1()) -> None) -> (t1() -> Empty))
    case (Empty, Cons(h2, t2))        => Some((None -> Some(h2())) -> (Empty -> t2()))
    case (Cons(h1, t1), Cons(h2, t2)) => Some((Some(h1()) -> Some(h2())) -> (t1() -> t2()))

  def zipWith[B, C](that: LazyList[B])(f: (A, B) => C): LazyList[C] = LazyList.unfold((this, that)):
    case (Cons(h1, t1), Cons(h2, t2)) => Some((f(h1(), h2())) -> (t1() -> t2()))
    case _                            => None

  def scanRight[B](z: => B)(f: (A, => B) => B): LazyList[B] = foldRight(z -> LazyList(z)) { case (a, (b, acc)) =>
    lazy val b2 = f(a, b)
    (b2, LazyList.cons(b2, acc))
  }._2

  def startsWith[B >: A](prefix: LazyList[B]): Boolean = this.zipAll(prefix).takeWhile(_._2.isDefined)
    .forAll { case (h1, h2) => h1 == h2 }

  def tails: LazyList[LazyList[A]] = LazyList.unfold(this) {
    case Empty      => None
    case Cons(h, t) => Some(Cons(h, t), t())
  }.append(LazyList.empty)

  def hasSubsequence[B >: A](sub: LazyList[B]): Boolean = tails.exists(_.startsWith(sub))

object LazyList:

  def cons[A](hd: => A, tl: => LazyList[A]): LazyList[A] =
    lazy val head = hd
    lazy val tail = tl
    Cons(() => head, () => tail)

  def empty[A]: LazyList[A] = Empty

  def apply[A](as: A*): LazyList[A] = if as.isEmpty then empty else cons(as.head, apply(as.tail*))

  val ones: LazyList[Int] = LazyList.cons(1, ones)

  def continually[A](a: A): LazyList[A] =
    lazy val once: LazyList[A] = LazyList.cons(a, once)
    once

  def from(n: Int): LazyList[Int] = LazyList.cons(n, from(n + 1))

  lazy val fibs: LazyList[Int] =
    def go(a0: Int, a1: Int): LazyList[Int] = LazyList.cons(a0, go(a1, a0 + a1))
    go(0, 1)

  def unfold[A, S](state: S)(f: S => Option[(A, S)]): LazyList[A] = f(state) match
    case Some((a, s)) => LazyList.cons(a, unfold(s)(f))
    case None         => LazyList.empty

  lazy val fibsViaUnfold: LazyList[Int] = unfold(0, 1) { case (a0, a1) => Some((a0, (a1, a0 + a1))) }

  def fromViaUnfold(n: Int): LazyList[Int] = unfold(n)(n => Some(n, n + 1))

  def continuallyViaUnfold[A](a: A): LazyList[A] = unfold(a)(_ => Some((a, a)))

  lazy val onesViaUnfold: LazyList[Int] = unfold(1)(_ => Some((1, 1)))
