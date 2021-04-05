package fpinscala.datastructures

sealed trait Tree[+A]
case class Leaf[A](value: A) extends Tree[A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

object Tree {

  def size[A](t: Tree[A]): Int = {
    t match {
      case Leaf(_)      => 1
      case Branch(l, r) => 1 + size(l) + size(r)
    }
  }

  def maxTree(t: Tree[Int]): Int =
    t match {
      case Leaf(a)      => a
      case Branch(l, r) => maxTree(l) max maxTree(r)
    }

  def map[A, B](t: Tree[A])(f: A => B): Tree[B] =
    t match {
      case Leaf(a)      => Leaf(f(a))
      case Branch(l, r) => Branch(map(l)(f), map(r)(f))
    }

  def depth[A](t: Tree[A]): Int =
    t match {
      case Leaf(_)      => 0
      case Branch(l, r) => 1 + (depth(l) max depth(r))
    }

  def fold[A, B](t: Tree[A])(f: A => B)(g: (B, B) => B): B =
    t match {
      case Leaf(a)      => f(a)
      case Branch(l, r) => g(fold(l)(f)(g), fold(r)(f)(g))
    }

  def sizeViaFold[A](t: Tree[A]): Int =
    fold(t)(_ => 1)(1 + _ + _)

  def maxTreeViaFold(t: Tree[Int]): Int =
    fold(t)(a => a)(_ max _)

  def depthViaFold[A](t: Tree[A]): Int =
    fold(t)(_ => 0)((l, r) => 1 + (l max r))

  def mapViaFold[A, B](t: Tree[A])(f: A => B): Tree[B] =
    fold(t)(x => Leaf(f(x)): Tree[B])(Branch(_, _))

  def main(args: Array[String]): Unit = {
    val t = Branch(Leaf(1), Branch(Leaf(2), Leaf(3)))
    println(t)
    println(size(t))
    println(maxTree(t))
    println(map(t)(_ * 2))
    println(depth(t))
    println(fold(t)(identity(_): Int)(_ + _))
    println(sizeViaFold(t))
    println(depthViaFold(t))
    println(mapViaFold(t)(_ * 2))
  }

}
