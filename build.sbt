name := "fpinscala"

ThisBuild / scalaVersion := "3.3.4"

ThisBuild / javacOptions ++= Seq("-source", "21", "-target", "21")

ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(name = Some("Build project"), commands = List("test:compile")))

// Ensure generated GitHub Actions install Temurin JDK 21 (needed for Scala 3.3.4 javac options)
// sbt-github-actions exposes a `githubWorkflowJavaVersions` setting which takes JavaSpec values.
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("21"))

// Ensure SBT respects the JAVA_HOME set by actions/setup-java by exporting
// it into the Actions runtime environment for subsequent steps.
ThisBuild / githubWorkflowBuildPreamble := Seq(
	WorkflowStep.Run(
		commands = List("echo \"SBT_JAVA_HOME=$JAVA_HOME\" >> $GITHUB_ENV"),
		name = Some("Export SBT_JAVA_HOME")
	)
)

ThisBuild / scalacOptions ++= List("-feature", "-deprecation", "-Ykind-projector:underscores", "-source:future")

ThisBuild / libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test

Global / onChangedBuildSource := ReloadOnSourceChanges
