enablePlugins(ScalaJSPlugin)

libraryDependencies ++= Seq(
  "com.raquo" %%% "laminar" % "0.10.2",
  "com.lihaoyi" %%% "utest" % "0.7.4" % Test
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
