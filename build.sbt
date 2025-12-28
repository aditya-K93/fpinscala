name := "fpinscala"

ThisBuild / scalaVersion := "3.3.4"

// Enable Scala Native
enablePlugins(ScalaNativePlugin)

ThisBuild / javacOptions ++= Seq("-source", "21", "-target", "21")

// Ensure generated GitHub Actions install Temurin JDK 21 (needed for Scala 3.3.4 javac options)
// sbt-github-actions exposes a `githubWorkflowJavaVersions` setting which takes JavaSpec values.
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("21"))

// Configure GitHub Actions to test both JVM and Scala Native
ThisBuild / githubWorkflowBuildMatrixAdditions += "platform" -> List("jvm", "native")

// Setup steps for CI including LLVM for Scala Native and environment variables
ThisBuild / githubWorkflowBuildPreamble := Seq(
  WorkflowStep
    .Run(commands = List("echo \"SBT_JAVA_HOME=$JAVA_HOME\" >> $GITHUB_ENV"), name = Some("Export SBT_JAVA_HOME")),
  WorkflowStep.Run(
    name = Some("Install LLVM (for Scala Native)"),
    commands = List("sudo apt-get update", "sudo apt-get install -y clang libstdc++-12-dev"),
    cond = Some("matrix.platform == 'native' && matrix.os == 'ubuntu-latest'")
  )
)

// Customize build steps based on platform
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(name = Some("Test"), commands = List("test"), cond = Some("matrix.platform == 'jvm'")),
  WorkflowStep
    .Sbt(name = Some("Test (Scala Native)"), commands = List("test"), cond = Some("matrix.platform == 'native'"))
)

// Disable artifact upload since we're not publishing from this repo
ThisBuild / githubWorkflowArtifactUpload := false

ThisBuild / scalacOptions ++= List("-feature", "-deprecation", "-Ykind-projector:underscores", "-source:future")

ThisBuild / libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test

Global / onChangedBuildSource := ReloadOnSourceChanges
