import org.scalajs.linker.interface.ModuleSplitStyle
val scala3Version = "3.3.1"
val AIRFRAME_VERSION = "23.11.1"

lazy val root = project
  .in(file("."))
  .dependsOn(rss)
  .dependsOn(api.jvm)
  .dependsOn(discord)
  .settings(
    name := "rss2discord-bis",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.9.3",
      "org.wvlet.airframe" %% "airframe-http-netty" % AIRFRAME_VERSION
    ),
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )

Compile / run / fork := true

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*)         => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

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

// RPC API definition. This project should contain only RPC interfaces
lazy val api =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("api"))
    .settings(
      scalaVersion := scala3Version,
      libraryDependencies ++= Seq(
        "org.wvlet.airframe" %%% "airframe-http" % AIRFRAME_VERSION
      )
    )

// RPC client project (JVM and Scala.js)
lazy val frontend =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("frontend")) // CAVEAT: need exact same name with the directory
    .enablePlugins(AirframeHttpPlugin)
    .enablePlugins(ScalaJSPlugin)
    .jsSettings(
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= {
        _.withModuleKind(ModuleKind.ESModule)
          .withModuleSplitStyle(
            ModuleSplitStyle.SmallModulesFor(
              List("io.github.windymelt.rss2discordbis")
            )
          )
      }
    )
    .settings(
      scalaVersion := scala3Version,
      airframeHttpClients := Seq(
        // should define package that API is defined at
        "io.github.windymelt.rss2discordbis.api.v1:rpc:ServiceRPC"
      ),
      libraryDependencies ++= Seq(
        "com.raquo" %%% "laminar" % "16.0.0"
      )
    )
    .dependsOn(api)
