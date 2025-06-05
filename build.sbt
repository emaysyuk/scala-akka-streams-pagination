ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

val versionPekko = "1.0.1"
val versionPekkoHttp = "1.0.0"
val versionPekkoHttpCirce = "3.2.0"
val versionCirce = "0.14.3"
val versionLogback = "1.4.7"

lazy val root = (project in file("."))
  .settings(
    name := "akka-streams-pagination",
    scalacOptions ++= Seq("-Ymacro-annotations"),
    libraryDependencies += "org.apache.pekko" %% "pekko-stream-typed" % versionPekko,
    libraryDependencies += "org.apache.pekko" %% "pekko-stream-testkit" % versionPekko,
    libraryDependencies += "org.apache.pekko" %% "pekko-http" % versionPekkoHttp,
    libraryDependencies += "org.apache.pekko" %% "pekko-http-core" % versionPekkoHttp,
    libraryDependencies += "com.github.pjfanning" %% "pekko-http-circe" % versionPekkoHttpCirce,
    libraryDependencies += "io.circe" %% "circe-core" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-parser" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-generic" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-generic-extras" % versionCirce,
    libraryDependencies += "ch.qos.logback" % "logback-classic" % versionLogback
  )
