name := "fpinscala"

ThisBuild / scalaVersion := "3.3.4"

ThisBuild / javacOptions ++= Seq("-source", "21", "-target", "21")

ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(name = Some("Build project"), commands = List("test:compile")))

// Ensure generated GitHub Actions install Temurin JDK 21 (needed for Scala 3.3.4 javac options)
// sbt-github-actions exposes a `githubWorkflowJavaVersions` setting which takes JavaSpec values.
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("21"))

ThisBuild / scalacOptions ++= List("-feature", "-deprecation", "-Ykind-projector:underscores", "-source:future")

ThisBuild / libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test

Global / onChangedBuildSource := ReloadOnSourceChanges
