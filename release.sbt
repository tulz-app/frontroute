name := "frontroute"

normalizedName := "frontroute"

organization := "app.tulz"

scalaVersion := "2.13.4"

crossScalaVersions := Seq("2.12.12", "2.13.4")

homepage := Some(url("https://github.com/tulz-app/frontroute"))

licenses += ("MIT", url("https://github.com/tulz-app/frontroute/blob/main/LICENSE.md"))

description := "Routing library based on Airstream for Laminar with DSL inspired by Akka HTTP."

scmInfo := Some(
  ScmInfo(
    url("https://github.com/tulz-app/frontroute"),
    "scm:git@github.com/tulz-app/frontroute.git"
  )
)

developers := List(
  Developer(
    id = "yurique",
    name = "Iurii Malchenko",
    email = "i@yurique.com",
    url = url("https://github.com/yurique")
  )
)

publishTo := sonatypePublishToBundle.value

sonatypeProfileName := "yurique"

publishMavenStyle := true

publishArtifact in Test := false

releaseCrossBuild := true

pomIncludeRepository := { _ =>
  false
}

publishArtifact in Test := false

releasePublishArtifactsAction := PgpKeys.publishSigned.value
