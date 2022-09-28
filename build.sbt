import org.scalajs.linker.interface.ESVersion

val disableWebsiteOnCI = false

inThisBuild(
  List(
    organization                        := "io.frontroute",
    homepage                            := Some(url("https://github.com/tulz-app/frontroute")),
    licenses                            := List("MIT" -> url("https://github.com/tulz-app/frontroute/blob/main/LICENSE.md")),
    scmInfo                             := Some(ScmInfo(url("https://github.com/tulz-app/frontroute"), "scm:git@github.com/tulz-app/frontroute.git")),
    developers                          := List(Developer("yurique", "Iurii Malchenko", "i@yurique.com", url("https://github.com/yurique"))),
    description                         := "Router library for Laminar with DSL inspired by Akka HTTP.",
    Test / publishArtifact              := false,
    scalafmtOnCompile                   := true,
    versionScheme                       := Some("early-semver"),
    scalaVersion                        := ScalaVersions.v213,
    crossScalaVersions                  := Seq(
      ScalaVersions.v3,
      ScalaVersions.v213
    ),
    versionPolicyIntention              := Compatibility.BinaryCompatible,
    githubWorkflowJavaVersions          := Seq(JavaSpec.temurin("17")),
//    githubWorkflowBuild += WorkflowStep.Sbt(List("versionPolicyCheck")),
    githubWorkflowTargetTags ++= Seq("v*"),
    githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v"))),
    githubWorkflowPublish               := Seq(WorkflowStep.Sbt(List("ci-release"))),
    githubWorkflowBuild                 := Seq(WorkflowStep.Sbt(List("test") ++ List("website/fastLinkJS").filterNot(_ => disableWebsiteOnCI))),
    githubWorkflowEnv ~= (_ ++ Map(
      "PGP_PASSPHRASE"    -> s"$${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> s"$${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> s"$${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> s"$${{ secrets.SONATYPE_USERNAME }}"
    )),
    githubWorkflowGeneratedUploadSteps ~= { steps =>
      if (disableWebsiteOnCI) {
        steps.map {
          case run: WorkflowStep.Run =>
            run.copy(commands = run.commands.map { command =>
              if (command.startsWith("tar cf targets.tar")) {
                command.replace("website/target", "")
              } else {
                command
              }

            })
          case other                 => other
        }
      } else {
        steps
      }
    }
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
          Dependencies.utest.value,
          Dependencies.`scala-js-macrotask-executor`.value.map(_ % Test)
        ),
      testFrameworks += new TestFramework("utest.runner.Framework"),
      ScalaOptions.fixOptions,
      scalacOptions ++= {
        val sourcesGithubUrl  = s"https://raw.githubusercontent.com/tulz-app/frontroute/${git.gitHeadCommit.value.get}/"
        val sourcesOptionName = CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, _)) => "-P:scalajs:mapSourceURI"
          case Some((3, _)) => "-scalajs-mapSourceURI"
          case _            => throw new RuntimeException(s"unexpected scalaVersion: ${scalaVersion.value}")
        }
        val moduleSourceRoot  = file("").toURI.toString
        Seq(
          s"$sourcesOptionName:$moduleSourceRoot->$sourcesGithubUrl"
        )
      }
    )

lazy val website = project
  .in(file("website"))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(EmbeddedFilesPlugin)
  .settings(ScalaOptions.fixOptions)
  .settings(noPublish)
  .settings(
    githubWorkflowTargetTags        := Seq.empty,
    publish / skip                  := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSLinkerConfig ~= { _.withESFeatures(_.withESVersion(ESVersion.ES5_1)) },
    Compile / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    scalaJSUseMainModuleInitializer := true,
    //    scalaJSLinkerConfig ~= (_.withModuleSplitStyle(org.scalajs.linker.interface.ModuleSplitStyle.FewestModules)),
    libraryDependencies ++= Seq.concat(
      Dependencies.laminext.value,
      Dependencies.`embedded-files-macro`.value,
      Dependencies.sourcecode.value
    ),
    embedTextGlobs                  := Seq("**/*.md"),
    embedDirectories ++= (Compile / unmanagedSourceDirectories).value,
    (Compile / sourceGenerators) += embedFiles
  )
  .dependsOn(
    frontroute
  )

lazy val noPublish = Seq(
  publishLocal / skip := true,
  publish / skip      := true,
  publishTo           := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))
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
