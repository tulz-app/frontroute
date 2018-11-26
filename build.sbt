enablePlugins(ScalaJSPlugin)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0-M4")

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.6",
  "com.raquo" %%% "airstream" % "0.4",
  "com.lihaoyi" %%% "utest" % "0.6.4" % Test
)

scalaJSUseMainModuleInitializer := true

emitSourceMaps := false

testFrameworks += new TestFramework("utest.runner.Framework")