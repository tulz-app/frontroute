
name := "laminar-router"

normalizedName := "laminar-router"

organization := "app.tulz"

scalaVersion := "2.13.3"

crossScalaVersions := Seq("2.12.10", "2.13.3")

homepage := Some(url("https://github.com/tulz-app/laminar-router"))

licenses += ("MIT", url("https://github.com/tulz-app/laminar-router/blob/master/LICENSE.md"))

description := "Routing library for Laminar with DSL inspired by Akka HTTP."

scmInfo := Some(
  ScmInfo(
    url("https://github.com/tulz-app/laminar-router"),
    "scm:git@github.com/tulz-app/laminar-router.git"
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

//publishTo := {
//  val nexus = "https://oss.sonatype.org/"
//  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
//  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
//}

//credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials_app_tulz")

sonatypeProfileName := "app.tulz"

publishMavenStyle := true

publishArtifact in Test := false

releaseCrossBuild := true

pomIncludeRepository := { _ => false }

publishArtifact in Test := false

releasePublishArtifactsAction := PgpKeys.publishSigned.value


