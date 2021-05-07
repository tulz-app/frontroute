inThisBuild(
  List(
    organization := "io.frontroute",
    homepage := Some(url("https://github.com/tulz-app/frontroute")),
    licenses := List("MIT" -> url("https://github.com/tulz-app/frontroute/blob/main/LICENSE.md")),
    scmInfo := Some(ScmInfo(url("https://github.com/tulz-app/frontroute"), "scm:git@github.com/tulz-app/frontroute.git")),
    developers := List(Developer("yurique", "Iurii Malchenko", "i@yurique.com", url("https://github.com/yurique"))),
    description := "Router library based for Laminar with DSL inspired by Akka HTTP.",
    Test / publishArtifact := false,
    versionScheme := Some("early-semver"),
    scalaVersion := ScalaVersions.v213,
    crossScalaVersions := Seq(
      ScalaVersions.v3RC3,
      ScalaVersions.v213,
      ScalaVersions.v212
    ),
    versionPolicyIntention := Compatibility.BinaryCompatible,
    githubWorkflowJavaVersions := Seq("openjdk@1.11.0"),
//    githubWorkflowBuild += WorkflowStep.Sbt(List("versionPolicyCheck")),
    githubWorkflowTargetTags ++= Seq("v*"),
    githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v"))),
    githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("test", "doc"))),
    githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release"))),
    githubWorkflowEnv ~= (_ ++ Map(
      "PGP_PASSPHRASE"    -> s"$${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> s"$${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> s"$${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> s"$${{ secrets.SONATYPE_USERNAME }}"
    ))
  )
)

lazy val root =
  project
    .in(file("."))
    .enablePlugins(ScalaJSPlugin, ScalaJSJUnitPlugin)
    .settings(
      name := "frontroute",
      libraryDependencies ++=
        Seq.concat(
          Dependencies.airstream.value,
          Dependencies.`tuplez-apply`.value,
          Dependencies.utest.value
        ),
      testFrameworks += new TestFramework("utest.runner.Framework"),
      ScalaOptions.fixOptions
    )
