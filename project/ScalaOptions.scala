import sbt._
import sbt.Keys._

object ScalaOptions {
  val fixOptions = Seq(
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

}
