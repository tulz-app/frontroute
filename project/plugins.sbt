logLevel := Level.Warn

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.5.1")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.7")

addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.17")

addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.10.1")

addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.7")

addSbtPlugin("ch.epfl.scala" % "sbt-version-policy" % "1.0.0")

addSbtPlugin("com.yurique" % "sbt-embedded-files" % "0.2.0")
