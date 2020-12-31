enablePlugins(ScalaJSPlugin)

name := "frontroute"

scalaVersion := "2.13.4"

crossScalaVersions := Seq("2.12.12", "2.13.4")

libraryDependencies ++= Seq(
  "app.tulz"    %%% "tuplez-full"  % "0.3.0",
  "app.tulz"    %%% "tuplez-apply" % "0.3.0",
  "app.tulz"    %%% "tuplez-tuple" % "0.3.0",
  "com.raquo"   %%% "airstream"    % "0.11.1",
  "com.raquo"   %%% "laminar"      % "0.11.0" % Test,
  "com.lihaoyi" %%% "utest"        % "0.7.5"  % Test
)

lazy val adjustScalacOptions = { options: Seq[String] =>
  options.filterNot(
    Set(
      "-Wdead-code"
    )
  )
}

scalacOptions ~= adjustScalacOptions

publishArtifact in Test := false

testFrameworks += new TestFramework("utest.runner.Framework")

description := "Routing library based on Airstream for Laminar with DSL inspired by Akka HTTP."

scmInfo := Some(
  ScmInfo(
    url("https://github.com/tulz-app/frontroute"),
    "scm:git@github.com/tulz-app/frontroute.git"
  )
)

ThisBuild / organization := "io.frontroute"
ThisBuild / homepage := Some(url("https://github.com/tulz-app/frontroute"))
ThisBuild / licenses += ("MIT", url("https://github.com/tulz-app/frontroute/blob/main/LICENSE.md"))
ThisBuild / developers := List(
  Developer(
    id = "yurique",
    name = "Iurii Malchenko",
    email = "i@yurique.com",
    url = url("https://github.com/yurique")
  )
)
ThisBuild / releasePublishArtifactsAction := PgpKeys.publishSigned.value
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / sonatypeProfileName := "yurique"
ThisBuild / publishMavenStyle := true
ThisBuild / releaseCrossBuild := true
