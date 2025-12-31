// Add Sonatype snapshots resolver for Scala Native snapshot plugin
resolvers += "Sonatype Snapshots" at "https://central.sonatype.com/repository/maven-snapshots"

addSbtPlugin("com.github.sbt"   % "sbt-github-actions" % "0.29.0")
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"       % "2.5.6")
addSbtPlugin("org.scala-native" % "sbt-scala-native"   % "0.5.9")
