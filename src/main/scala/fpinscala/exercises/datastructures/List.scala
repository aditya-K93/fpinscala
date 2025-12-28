package fpinscala.exercises.datastructures

/** `List` data type, parameterized on a type, `A`. */
enum List[+A]:

  /** A `List` data constructor representing the empty list. */
  case Nil

  /**
   * Another data constructor, representing nonempty lists. Note that `tail` is
   * another `List[A]`, which may be `Nil` or another `Cons`.
   */
  case Cons(head: A, tail: List[A])

object List: // `List` companion object. Contains functions for creating and working with lists.

  def sum(ints: List[Int]): Int = ints match // A function that uses pattern matching to add up a list of integers
    case Nil         => 0           // The sum of the empty list is 0.
    case Cons(x, xs) => x + sum(xs) // The sum of a list starting with `x` is `x` plus the sum of the rest of the list.

  def product(doubles: List[Double]): Double = doubles match
    case Nil          => 1.0
    case Cons(0.0, _) => 0.0
    case Cons(x, xs)  => x * product(xs)

  def apply[A](as: A*): List[A] = // Variadic function syntax
    if as.isEmpty then Nil else Cons(as.head, apply(as.tail*))

  @annotation.nowarn // Scala gives a hint here via a warning, so let's disable that
  val result = List(1, 2, 3, 4, 5) match
    case Cons(x, Cons(2, Cons(4, _)))          => x
    case Nil                                   => 42
    case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y // correct answer is 3
    case Cons(h, t)                            => h + sum(t)
    case _                                     => 101

  def append[A](a1: List[A], a2: List[A]): List[A] = a1 match
    case Nil        => a2
    case Cons(h, t) => Cons(h, append(t, a2))

  def foldRight[A, B](as: List[A], acc: B, f: (A, B) => B): B = // Utility functions
    as match
      case Nil         => acc
      case Cons(x, xs) => f(x, foldRight(xs, acc, f))

  def sumViaFoldRight(ns: List[Int]): Int = foldRight(ns, 0, (x, y) => x + y)

  def productViaFoldRight(ns: List[Double]): Double =
    foldRight(ns, 1.0, _ * _) // `_ * _` is more concise notation for `(x,y) => x * y`; see sidebar

  def tail[A](l: List[A]): List[A] = l match
    case Nil        => sys.error("tail of empty list")
    case Cons(_, t) => t

  def setHead[A](l: List[A], h: A): List[A] = l match
    case Nil        => Cons(h, Nil)
    case Cons(_, t) => Cons(h, t)

  def drop[A](l: List[A], n: Int): List[A] =
    if n <= 0 then l
    else
      l match
        case Nil        => Nil
        case Cons(_, t) => drop(t, n - 1)

  def dropWhile[A](l: List[A], f: A => Boolean): List[A] = l match
    case Nil        => Nil
    case Cons(h, t) => if f(h) then dropWhile(t, f) else l

  def init[A](l: List[A]): List[A] = l match
    case Nil          => Nil
    case Cons(h, Nil) => Cons(h, Nil)
    case Cons(h, t)   => Cons(h, init(t))

  def length[A](l: List[A]): Int = foldRight(l, 0, (_, acc) => acc + 1)

  def foldLeft[A, B](l: List[A], acc: B, f: (B, A) => B): B = l match
    case Nil        => acc
    case Cons(h, t) => foldLeft(t, f(acc, h), f)

  def sumViaFoldLeft(ns: List[Int]): Int = foldLeft(ns, 0, _ + _)

  def productViaFoldLeft(ns: List[Double]): Double = foldLeft(ns, 1.0, _ * _)

  def lengthViaFoldLeft[A](l: List[A]): Int = foldLeft(l, 0, (acc, _) => acc + 1)

  def reverse[A](l: List[A]): List[A] = foldLeft(l, Nil, (acc: List[A], a) => Cons(a, acc))

  def foldRightInTermsFoldLeft[A, B](l: List[A], acc: B, f: (A, B) => B): B =
    foldLeft(reverse(l), acc: B, (b, a) => f(a, b))

  def appendViaFoldRight[A](l: List[A], r: List[A]): List[A] = foldRight(l, r, Cons(_, _))

  def appendF[A](l: List[A], r: List[A]): List[A] = appendViaFoldRight(l, r)

  def concat[A](l: List[List[A]]): List[A] = foldRight(l, Nil, (acc, l: List[A]) => appendViaFoldRight(acc, l))

  def incrementEach(l: List[Int]): List[Int] = foldRight(l, Nil: List[Int], (acc, t) => Cons(acc + 1, t))

  def doubleToString(l: List[Double]): List[String] = foldRight(l, Nil: List[String], (acc, t) => Cons(acc.toString, t))

  def map[A, B](l: List[A], f: A => B): List[B] = foldRight(l, Nil: List[B], (a, acc) => Cons(f(a), acc))

  def filter[A](as: List[A], f: A => Boolean): List[A] =
    foldRight(as, Nil: List[A], (a, acc) => if f(a) then Cons(a, acc) else acc)

  def flatMap[A, B](as: List[A], f: A => List[B]): List[B] = foldRight(as, Nil: List[B], (a, acc) => append(f(a), acc))

  def filterViaFlatMap[A](as: List[A], f: A => Boolean): List[A] = flatMap(as, a => if f(a) then Cons(a, Nil) else Nil)

  def addPairwise(a: List[Int], b: List[Int]): List[Int] = (a, b) match
    case (Nil, _)                     => Nil
    case (_, Nil)                     => Nil
    case (Cons(h1, t1), Cons(h2, t2)) => Cons(h1 + h2, addPairwise(t1, t2))

  def zipWith[A, B, C](l1: List[A], l2: List[B], f: (A, B) => C): List[C] = (l1, l2) match
    case (Nil, _)                     => Nil
    case (_, Nil)                     => Nil
    case (Cons(h1, t1), Cons(h2, t2)) => Cons(f(h1, h2), zipWith(t1, t2, f))

  def zipWithTailCall[A, B, C](l1: List[A], l2: List[B], f: (A, B) => C): List[C] =
    @annotation.tailrec
    def go(l1: List[A], l2: List[B], acc: List[C]): List[C] = (l1, l2) match
      case (Nil, _)                     => acc
      case (_, Nil)                     => acc
      case (Cons(h1, t1), Cons(h2, t2)) => go(t1, t2, Cons(f(h1, h2), acc))
    reverse(go(l1, l2, Nil))

  def hasSubsequence[A](sup: List[A], sub: List[A]): Boolean =
    @annotation.tailrec
    def startsWith(l: List[A], prefix: List[A]): Boolean = (l, prefix) match
      case (_, Nil)                     => true
      case (Cons(h1, t1), Cons(h2, t2)) => h1 == h2 && startsWith(t1, t2)
      case _                            => false

    @annotation.tailrec
    def go(sup: List[A]): Boolean = sup match
      case Nil        => sub == Nil
      case Cons(_, t) => if startsWith(sup, sub) then true else go(t)

    go(sup)
