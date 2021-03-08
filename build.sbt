inThisBuild(
  List(
    organization := "io.frontroute",
    homepage := Some(url("https://github.com/tulz-app/frontroute")),
    licenses := List("MIT" -> url("https://github.com/tulz-app/frontroute/blob/main/LICENSE.md")),
    ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/tulz-app/frontroute"), "scm:git@github.com/tulz-app/frontroute.git")),
    developers := List(Developer("yurique", "Iurii Malchenko", "i@yurique.com", url("https://github.com/yurique")))
//ThisBuild / sonatypeProfileName := "yurique"
//ThisBuild / description := "Router library based for Laminar with DSL inspired by Akka HTTP."
  )
)

inThisBuild(
  List(
    Test / publishArtifact := false,
    scalaVersion := ScalaVersions.v213,
    crossScalaVersions := Seq(
      ScalaVersions.v3RC1,
      ScalaVersions.v213,
      ScalaVersions.v212
    )
  )
)

lazy val root =
  project
    .in(file("."))
    .enablePlugins(ScalaJSPlugin, ScalaJSJUnitPlugin)
    .settings(
      name := "frontroute",
      libraryDependencies ++=
        Seq(
          ("com.raquo"  %%% "airstream"    % LibraryVersions.airstream).withDottyCompat(scalaVersion.value),
          "app.tulz"    %%% "tuplez-apply" % LibraryVersions.`tuplez-apply`,
          "com.lihaoyi" %%% "utest"        % LibraryVersions.utest % Test
        ),
      testFrameworks += new TestFramework("utest.runner.Framework"),
      scalacOptions ~= (
        _.filterNot(
          Set(
            "-Wdead-code",
            "-Ywarn-dead-code",
            "-Wunused:params",
            "-Ywarn-unused:params",
            "-Wunused:explicits"
          )
        )
      ),
      Test / scalacOptions ~= (_.filterNot(_.startsWith("-Wunused:")).filterNot(_.startsWith("-Ywarn-unused"))),
      (Compile / doc / scalacOptions) ~= (_.filterNot(
        Set(
          "-scalajs",
          "-deprecation",
          "-explain-types",
          "-explain",
          "-feature",
          "-language:existentials,experimental.macros,higherKinds,implicitConversions",
          "-unchecked",
          "-Xfatal-warnings",
          "-Ykind-projector",
          "-from-tasty",
          "-encoding",
          "utf8"
        )
      ))
    )
