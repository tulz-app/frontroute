ThisBuild / scalaVersion := ScalaVersions.v213
ThisBuild / crossScalaVersions := Seq(
  /*
  ScalaVersions.v3RC1,
  ScalaVersions.v3M3,
   */
  ScalaVersions.v213,
  ScalaVersions.v212
)

lazy val root =
  project
    .in(file("."))
    .enablePlugins(ScalaJSPlugin, ScalaJSJUnitPlugin)
    .settings(
      name := "frontroute",
      libraryDependencies ++=
        Seq(
          "com.raquo"   %%% "airstream"    % BuildSettings.version.airstream,
          "app.tulz"    %%% "tuplez-apply" % BuildSettings.version.`tuplez-apply`,
          "com.raquo"   %%% "laminar"      % BuildSettings.version.laminar % Test,
          "com.lihaoyi" %%% "utest"        % BuildSettings.version.utest   % Test
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
      Test / scalacOptions := (Compile / scalacOptions).value.filterNot(_.startsWith("-Wunused:")).filterNot(_.startsWith("-Ywarn-unused")),
      scalacOptions in (Compile, doc) ~= (_.filterNot(
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
