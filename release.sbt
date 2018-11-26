name := "scalajs-routing"

normalizedName := "scalajs-routing"

organization := "app.tulz"

scalaVersion := "2.12.7"

crossScalaVersions := Seq("2.11.12", "2.12.7")

homepage := Some(url("https://github.com/tulz-app/scalajs-routing"))

licenses += ("MIT", url("https://github.com/tulz-app/scalajs-routing/blob/master/LICENSE.md"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/tulz-app/scalajs-routing"),
    "scm:git@github.com/tulz-app/scalajs-routing.git"
  )
)

developers := List(
  Developer(
    id = "yurique",
    name = "Iurii Malchenko",
    email = "i@yurique.com",
    url = url("http://tulz.app")
  )
)

sonatypeProfileName := "app.tulz"

publishMavenStyle := true

publishArtifact in Test := false

publishTo := sonatypePublishTo.value

releaseCrossBuild := true

pomIncludeRepository := { _ => false }

//useGpg := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

