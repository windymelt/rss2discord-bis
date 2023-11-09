val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .dependsOn(rss)
  .settings(
    name := "rss2discord-bis",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.9.3"
    ),
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )

Compile / run / fork := true

lazy val rss = project
  .in(file("rss"))
  .settings(
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.rometools" % "rome" % "2.1.0"
    )
  )

lazy val discord = project
  .in(file("discord"))
  .settings(
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.7.3",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % "1.7.3",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.7.3",
      "org.http4s" %% "http4s-blaze-client" % "0.23.15"
    )
  )
