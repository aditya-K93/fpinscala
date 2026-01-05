package fpinscala.exercises.state

import javax.crypto.Mac

trait RNG:
  def nextInt: (Int, RNG) // Should generate a random `Int`. We'll later define other functions in terms of `nextInt`.

object RNG:
  // NB - this was called SimpleRNG in the book text

  case class Simple(seed: Long) extends RNG:

    def nextInt: (Int, RNG) =
      val newSeed = (seed * 0x5deece66dL + 0xbL) &
        0xffffffffffffL // `&` is bitwise AND. We use the current seed to generate a new seed.
      val nextRNG = Simple(newSeed) // The next state, which is an `RNG` instance created from the new seed.
      val n       = (newSeed >>> 16)
        .toInt // `>>>` is right binary shift with zero fill. The value `n` is our new pseudo-random integer.
      (n, nextRNG) // The return value is a tuple containing both a pseudo-random integer and the next `RNG` state.

  type Rand[+A] = RNG => (A, RNG)

  val int: Rand[Int] = _.nextInt

  def unit[A](a: A): Rand[A] = rng => (a, rng)

  def map[A, B](s: Rand[A])(f: A => B): Rand[B] = rng =>
    val (a, rng2) = s(rng)
    (f(a), rng2)

  def nonNegativeInt(rng: RNG): (Int, RNG) = rng.nextInt match // [0, Int.MaxValue],[-1,-(Int.MaxValue+1)]
    case (n, r) if n < 0 => (-(n + 1), r)
    case (n, r)          => (n, r)

  def double(rng: RNG): (Double, RNG) =
    val (n, r) = nonNegativeInt(rng)
    (n / (Int.MaxValue.toDouble + 1), r)

  def intDouble(rng: RNG): ((Int, Double), RNG) =
    val (n, r1) = rng.nextInt
    val (d, r2) = double(r1)
    ((n, d), r2)

  def doubleInt(rng: RNG): ((Double, Int), RNG) =
    val ((n, d), r) = intDouble(rng)
    ((d, n), r)

  def double3(rng: RNG): ((Double, Double, Double), RNG) =
    val (n1, r1) = double(rng)
    val (n2, r2) = double(r1)
    val (n3, r3) = double(r2)
    ((n1, n2, n3), r3)

  def ints(count: Int)(rng: RNG): (List[Int], RNG) =
    def go(n: Int, r: RNG, acc: List[Int]): (List[Int], RNG) =
      if n <= 0 then (acc, r)
      else
        val (i, r1) = r.nextInt
        go(n - 1, r1, i :: acc)
    go(count, rng, Nil)

  def doubleViaMap: Rand[Double] = map(nonNegativeInt)(_ / (Int.MaxValue.toDouble + 1))

  def map2[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] = rng =>
    val (n1, r1) = ra(rng)
    val (n2, r2) = rb(r1)
    (f(n1, n2), r2)

  def sequence[A](rs: List[Rand[A]]): Rand[List[A]] = rs
    .foldRight[Rand[List[A]]](unit(List.empty))((r, acc) => map2(r, acc)(_ :: _))

  def intsViaSequence(count: Int): Rand[List[Int]] = sequence(List.fill(count)(int))

  def flatMap[A, B](r: Rand[A])(f: A => Rand[B]): Rand[B] = rng =>
    val (a, r1) = r(rng)
    f(a)(r1)

  def nonNegativeLessThan(n: Int): Rand[Int] = flatMap(nonNegativeInt) { i =>
    val mod = i % n
    if i + (n - 1) - mod > 0 then unit(mod) else nonNegativeLessThan(n)
  }

  def mapViaFlatMap[A, B](r: Rand[A])(f: A => B): Rand[B] = flatMap(r)(a => unit(f(a)))

  def map2ViaFlatMap[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] =
    flatMap(ra)(a => flatMap(rb)(b => unit(f(a, b))))

opaque type State[S, +A] = S => (A, S)

object State:

  extension [S, A](underlying: State[S, A])

    def run(s: S): (A, S) = underlying(s)

    def map[B](f: A => B): State[S, B] = flatMap(a => unit(f(a)))

    def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
      for
        a <- underlying
        b <- sb
      yield f(a, b)

    def flatMap[B](f: A => State[S, B]): State[S, B] = s =>
      val (a, s1) = underlying(s)
      f(a)(s1)

  def apply[S, A](f: S => (A, S)): State[S, A] = f

  def unit[S, A](a: A): State[S, A] = s => (a, s)

  def sequence[S, A](states: List[State[S, A]]): State[S, List[A]] = states
    .foldRight(unit(Nil: List[A]))((s, accS) => s.map2(accS)(_ :: _))

  def traverse[S, A, B](as: List[A])(f: A => State[S, B]): State[S, List[B]] = as
    .foldRight(unit(Nil: List[B]))((a, accS) => f(a).map2(accS)(_ :: _))

  def get[S]: State[S, S] = s => (s, s)

  def set[S](s: S): State[S, Unit] = _ => ((), s)

  def modify[S](f: S => S): State[S, Unit] =
    for
      s <- get
      _ <- set(f(s))
    yield ()

enum Input:
  case Coin, Turn

case class Machine(locked: Boolean, candies: Int, coins: Int)

object Candy:

  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] =
    for
      _ <- State.traverse(inputs)(input => State.modify(stateUpdates(input)))
      s <- State.get
    yield (s.coins, s.candies)

  def stateUpdates(input: Input)(machine: Machine): Machine = input match
    case Input.Coin if machine.locked && machine.candies > 0  => machine.copy(locked = false, coins = machine.coins + 1)
    case Input.Turn if !machine.locked && machine.candies > 0 =>
      machine.copy(locked = true, candies = machine.candies - 1)
    case _                                                    => machine
