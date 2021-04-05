package fpinscala.datastructures

import fpinscala.datastructures

sealed trait List[+A] // `List` data type, parameterized on a type, `A`
case object Nil
    extends List[
      Nothing
    ] // A `List` data constructor representing the empty list
/* Another data constructor, representing nonempty lists. Note that `tail` is another `List[A]`,
which may be `Nil` or another `Cons`.
 */
case class Cons[+A](head: A, tail: List[A]) extends List[A]

object List { // `List` companion object. Contains functions for creating and working with lists.
  def sum(ints: List[Int]): Int =
    ints match { // A function that uses pattern matching to add up a list of integers
      case Nil => 0 // The sum of the empty list is 0.
      case Cons(x, xs) =>
        x + sum(
          xs
        ) // The sum of a list starting with `x` is `x` plus the sum of the rest of the list.
    }

  def product(ds: List[Double]): Double =
    ds match {
      case Nil          => 1.0
      case Cons(0.0, _) => 0.0
      case Cons(x, xs)  => x * product(xs)
    }

  def apply[A](as: A*): List[A] = // Variadic function syntax
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))

  val x = List(1, 2, 3, 4, 5) match {
    case Cons(x, Cons(2, Cons(4, _)))          => x
    case Nil                                   => 42
    case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y
    case Cons(h, t)                            => h + sum(t)
    case _                                     => 101
  }
// 3 x+y where x=1 y=2

  def append[A](a1: List[A], a2: List[A]): List[A] =
    a1 match {
      case Nil        => a2
      case Cons(h, t) => Cons(h, append(t, a2))
    }

  def foldRight[A, B](
      as: List[A],
      z: B
  )(f: (A, B) => B): B = // Utility functions
    as match {
      case Nil         => z
      case Cons(x, xs) => f(x, foldRight(xs, z)(f))
    }

  def sum2(ns: List[Int]) =
    foldRight(ns, 0)((x, y) => x + y)

  def product2(ns: List[Double]) =
    foldRight(ns, 1.0)(
      _ * _
    ) // `_ * _` is more concise notation for `(x,y) => x * y`; see sidebar

  def tail[A](l: List[A]): List[A] = {
    l match {
      case Nil         => Nil
      case Cons(_, xs) => xs
    }
  }

  def setHead[A](l: List[A], h: A): List[A] = {
    l match {
      case Nil         => Nil
      case Cons(_, xs) => Cons(h, xs)
    }
  }

  @annotation.tailrec
  def drop[A](l: List[A], n: Int): List[A] = {
    l match {
      case Nil         => Nil
      case Cons(_, xs) => if (n <= 0) l else drop(xs, n - 1)
    }
  }

  @annotation.tailrec
  def dropWhile[A](l: List[A], f: A => Boolean): List[A] = {
    l match {
      case Cons(x, xs) if f(x) => dropWhile(xs, f)
      case _                   => l
    }

  }

  def init[A](l: List[A]): List[A] = {
    l match {
      case Nil          => Nil
      case Cons(_, Nil) => Nil
      case Cons(x, xs)  => Cons(x, init(xs))

    }
  }

  def length[A](l: List[A]): Int =
    foldRight(l, 0)((_, y) => y + 1)

  @annotation.tailrec
  def foldLeft[A, B](l: List[A], z: B)(f: (B, A) => B): B = {
    l match {
      case Nil         => z
      case Cons(x, xs) => foldLeft(xs, f(z, x))(f)
    }
  }

  def sum3(l: List[Int]): Int =
    foldLeft(l, 0)(_ + _)

  def product3(l: List[Double]): Double =
    foldLeft(l, 1.0)(_ * _)

  def length3[A](l: List[A]) =
    foldLeft(l, 0)((x, _) => x + 1)

  def reverse[A](l: List[A]) =
    foldLeft(l, Nil: List[A])((z, x) => Cons(x, z))

  def foldRightViaFoldLeft[A, B](l: List[A], z: B)(f: (A, B) => B): B =
    foldLeft(reverse(l), z)((b, a) => f(a, b))

  def append2[A](l1: List[A], l2: List[A]): List[A] =
//    foldRight(l1, l2)((a, b) => Cons(a, b))
    foldLeft(reverse(l1), l2)((b, a) => Cons(a, b))

  def concat[A](l: List[List[A]]): List[A] =
//    foldRight(l, Nil: List[A])((x, y) => append(x, y))
    foldLeft(l, Nil: List[A])(append)

  def add1(l: List[Int]): List[Int] =
    foldRight(l, Nil: List[Int])((x, y) => Cons(x + 1, y))

  def doubleToString(l: List[Double]): List[String] =
    foldRight(l, Nil: List[String])((x, y) => Cons(x.toString, y))

  def map[A, B](l: List[A])(f: A => B): List[B] =
//    foldRight(l, Nil: List[B])((x: A, y: List[B]) => Cons(f(x), y))
    foldLeft(reverse(l), Nil: List[B])((y, x) => Cons(f(x), y))

  def filter[A](l: List[A])(f: A => Boolean): List[A] =
    foldRightViaFoldLeft(l, Nil: List[A])((x, y) => if (f(x)) Cons(x, y) else y)

  def flatMap[A, B](l: List[A])(f: A => List[B]): List[B] =
    concat(map(l)(f))

  def filterViaFlatMap[A](l: List[A])(f: A => Boolean): List[A] =
    flatMap(l)(x => if (f(x)) Cons(x, Nil) else Nil)

  def addTwoList(l1: List[Int], l2: List[Int]): List[Int] =
    (l1, l2) match {
      case (Nil, _)                       => l2
      case (_, Nil)                       => l1
      case (Cons(x1, xs1), Cons(x2, xs2)) => Cons(x1 + x2, addTwoList(xs1, xs2))
    }

  def zipWith[A, B, C](l1: List[A], l2: List[B])(f: (A, B) => C): List[C] =
    (l1, l2) match {
      case (Nil, _) => Nil
      case (_, Nil) => Nil
      case (Cons(x1, xs1), Cons(x2, xs2)) =>
        Cons(f(x1, x2), zipWith(xs1, xs2)(f))
    }

  @annotation.tailrec
  def startsWith[A](l1: List[A], l2: List[A]): Boolean =
    (l1, l2) match {
      case (_, Nil)                                     => true
      case (Cons(x1, xs1), Cons(x2, xs2)) if (x1 == x2) => startsWith(xs1, xs2)
      case _                                            => false
    }

  @annotation.tailrec
  def hasSubsequence[A](sup: List[A], sub: List[A]): Boolean =
    sup match {
      case Nil                       => sub == Nil
      case _ if startsWith(sup, sub) => true
      case Cons(_, t)                => hasSubsequence(t, sub)
    }

  def main(args: Array[String]): Unit = {
    val l = List(1, 2, 3, 4, 5, 10, 11)
    val ld = List(1.0, 2.1, 3.2, 4.1, 5.1, 10.11, 11.12121)
    val m = List(21, 22, 23)
    val nested = List(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9))
    println(setHead(l, 10))
    println(dropWhile(l, (x: Int) => x < 4))
    println(init(l))
    println(length(l))
    println(reverse(l))
    println(append(l, m))
    println(concat(nested))
    println(add1(l))
    println(doubleToString(ld))
    println(map(l)(x => x * 2))
    println(filter(l)(x => x > 3))
    println(filterViaFlatMap(l)(x => x > 3))
    println(addTwoList(l, m))
    println(zipWith(l, m)(_ + _))
    println(startsWith(l, List(1, 2, 3)))
    println(hasSubsequence(l, List(5, 10, 11)))
  }

}
