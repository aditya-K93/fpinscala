name := "fpinscala"

ThisBuild / scalaVersion := "3.3.4"

ThisBuild / javacOptions ++= Seq("-source", "21", "-target", "21")

ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(name = Some("Build project"), commands = List("test:compile")))

ThisBuild / scalacOptions ++= List("-feature", "-deprecation", "-Ykind-projector:underscores", "-source:future")

ThisBuild / libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test

Global / onChangedBuildSource := ReloadOnSourceChanges
