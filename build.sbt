name := "fpinscala"

version := "0.1"

val commonSettings = Seq(
  scalaVersion := "2.13.5"
)

lazy val root = (project in file("."))
  .aggregate(exercises)
  .settings(commonSettings)
  .settings(
    name := "fpinscala"
  )

lazy val exercises = (project in file("exercises"))
  .settings(commonSettings)
  .settings(
    name := "exercises"
  )
