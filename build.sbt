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
    githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release"))),
    githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("test", "website/fastLinkJS"))),
    githubWorkflowEnv ~= (_ ++ Map(
      "PGP_PASSPHRASE"    -> s"$${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> s"$${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> s"$${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> s"$${{ secrets.SONATYPE_USERNAME }}"
    ))
  )
)

lazy val frontroute =
  project
    .in(file("modules/frontroute"))
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

lazy val website = project
  .in(file("website"))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(EmbeddedFilesPlugin)
  .settings(ScalaOptions.fixOptions)
  .settings(noPublish)
  .settings(
    githubWorkflowTargetTags := Seq.empty,
    publish / skip := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSLinkerConfig ~= { _.withESFeatures(_.withUseECMAScript2015(false)) },
    Compile / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    scalaJSUseMainModuleInitializer := true,
    //    scalaJSLinkerConfig ~= (_.withModuleSplitStyle(org.scalajs.linker.interface.ModuleSplitStyle.FewestModules)),
    libraryDependencies ++= Seq.concat(
      Dependencies.laminext.value,
      Dependencies.`embedded-files-macro`.value,
      Dependencies.sourcecode.value
    ),
    embedTextGlobs := Seq("**/*.md"),
    embedDirectories ++= (Compile / unmanagedSourceDirectories).value,
    (Compile / sourceGenerators) += embedFiles
  )
  .dependsOn(
    frontroute
  )

lazy val noPublish = Seq(
  publishLocal / skip := true,
  publish / skip := true,
  publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))
)

lazy val root = project
  .in(file("."))
  .settings(noPublish)
  .settings(
    name := "frontroute"
  )
  .aggregate(
    frontroute
  )
