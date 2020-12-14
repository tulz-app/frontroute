enablePlugins(ScalaJSPlugin)

libraryDependencies ++= Seq(
  "com.raquo"   %%% "airstream" % "0.11.1",
  "com.raquo"   %%% "laminar"   % "0.11.0" % Test,
  "com.lihaoyi" %%% "utest"     % "0.7.5" % Test
)

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xlint:nullary-unit,inaccessible,infer-any,missing-interpolator,private-shadow,type-parameter-shadow,poly-implicit-overload,option-implicit,delayedinit-select,stars-align",
  "-Xcheckinit",
  "-Ywarn-value-discard",
  "-encoding",
  "utf8"
)

testFrameworks += new TestFramework("utest.runner.Framework")
