[![Join the chat at https://gitter.im/fpinscala/fpinscala](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/fpinscala/fpinscala?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 

This repository contains exercises, hints, and answers for the book
[Functional Programming in Scala](http://manning.com/bjarnason/). Along
with the book itself, it's the closest you'll get to having your own
private functional programming tutor without actually having one.

Here's how to use this repository:

Each chapter in the book develops a fully working library of functions
and data types, built up through a series of exercises and example code
given in the book text. The shell of this working library and exercise
stubs live in
`src/main/scala/fpinscala/exercises/<chapter-description>`, where
`<chapter-description>` is a package name that corresponds to the
chapter title (see below). When you begin working on a chapter, we
recommend you open the exercise file(s) for that chapter, and when you
encounter exercises, implement them in the exercises file and make sure
they work.

If you get stuck on an exercise, let's say exercise 4 in the chapter,
you can find hints in `answerkey/<chapter-description>/04.hint.md` (if
no hints are available for a problem, the file will just have a single
'-' as its contents) and the answer along with an explanation of the
answer and any variations in
`answerkey/<chapter-description>/04.answer.md`. The finished Scala
modules, with all answers for each chapter live in
`src/main/scala/fpinscala/answers/<chapter-description>`. Please feel
free to submit pull requests for alternate answers, improved hints, and
so on, so we can make this repo the very best resource for people
working through the book.

Chapter descriptions:

* Chapter 2: gettingstarted
* Chapter 3: datastructures
* Chapter 4: errorhandling
* Chapter 5: laziness
* Chapter 6: state
* Chapter 7: parallelism
* Chapter 8: testing
* Chapter 9: parsing
* Chapter 10: monoids
* Chapter 11: monads
* Chapter 12: applicative
* Chapter 13: iomonad
* Chapter 14: localeffects
* Chapter 15: streamingio

### Setup build environment

The project is setup to use [Scala CLI](https://scala-cli.virtuslab.org). First install Scala CLI by following the [installation instructions](https://scala-cli.virtuslab.org/install).

You'll also likely want an editor that's aware of Scala syntax. [VSCode](https://code.visualstudio.com) with the [Metals](https://scalameta.org/metals/docs/editors/vscode.html) extension works great.

### Building

To build the code for the first time, from the root directory of the project (i.e., the directory where this README.md is located):

    $ scala-cli compile .

This compiles all exercises and answers. You can also do:

    $ scala-cli console .

to get a Scala REPL (prompt `scala>`) with access to exercises and answers, and then for example:

    scala> import fpinscala.exercises.datastructures.List

to import the `List` data type.

To run the sample programs:

    $ scala-cli run .

This gives a list of possible main methods to execute. To run one of them:

    $ scala-cli run . --main-class fpinscala.answers.gettingstarted.printAbs

To run a single unit-test, you can specify the fully qualified class name and test name:

    $ scala-cli test . -- 'fpinscala.exercises.gettingstarted.GettingStartedSuite.MyProgram.factorial'

To run unit-tests for a package you can do:

    $ scala-cli test . -- 'fpinscala.exercises.gettingstarted.*'

To run all unit-tests:

    $ scala-cli test .

Note, running all tests will result in failures. As you solve exercises, the tests
will start to pass.

### SBT

Note: an [SBT](https://www.scala-sbt.org) build is also provided.

### Scala Native

This project supports [Scala Native](https://scala-native.org/) (version 0.5.9), enabling ahead-of-time compilation to native binaries with instant startup time. The project is tested on both JVM and Scala Native in CI.

To compile and test with Scala Native:

    $ sbt test

To create a native executable:

    $ sbt nativeLink

The native binary will be generated at `target/scala-3.3.4/fpinscala`.

**Requirements for Scala Native:**
- LLVM/Clang 6.0 or newer
- On macOS: `brew install llvm`
- On Ubuntu: `sudo apt-get install clang libstdc++-12-dev`

For more information, see the [Scala Native documentation](https://scala-native.org/).

### License

All code in this repository is
[MIT-licensed](http://opensource.org/licenses/mit-license.php). See the
LICENSE file for details.

Have fun, and good luck! Also be sure to check out [the community
wiki](https://github.com/fpinscala/fpinscala/wiki) for the **chapter
notes**, links to more reading, and more.

_Paul, Rúnar, and Michael_

