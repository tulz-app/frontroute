import sbt.*
import sbt.Keys.*

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*

object Dependencies {

  val laminar: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "com.raquo" %%% "laminar" % DependencyVersions.laminar
    )
  }

  val `tuplez-apply`: Def.Initialize[Seq[ModuleID]] = Def.setting {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Seq(
          "app.tulz" %%% "tuplez-apply" % DependencyVersions.`tuplez-apply`
        )
      case _            =>
//        TODO: this is not needed in scala-3, but IDEA currently fails to resolve types with apply as an extension method
//        Seq.empty
        Seq(
          "app.tulz" %%% "tuplez-apply" % DependencyVersions.`tuplez-apply`
        )
    }
  }

  val domtestutils: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "com.raquo" %%% "domtestutils" % DependencyVersions.domtestutils % Test
    )
  }

  val scalatest: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "org.scalatest" %%% "scalatest" % DependencyVersions.scalatest % Test,
    )
  }

  // website

  val laminext: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "dev.laminext" %%% "core"                   % DependencyVersions.laminext,
      "dev.laminext" %%% "highlight"              % DependencyVersions.laminext,
      "dev.laminext" %%% "ui"                     % DependencyVersions.laminext,
      "dev.laminext" %%% "util"                   % DependencyVersions.laminext,
      "dev.laminext" %%% "tailwind-default-theme" % DependencyVersions.laminext
    )
  }

  val `embedded-files-macro`: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "com.yurique" %%% "embedded-files-macro" % DependencyVersions.`embedded-files-macro`
    )
  }

  val sourcecode: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "com.lihaoyi" %%% "sourcecode" % DependencyVersions.sourcecode
    )
  }

  val `scala-js-macrotask-executor`: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "org.scala-js" %%% "scala-js-macrotask-executor" % DependencyVersions.`scala-js-macrotask-executor`
    )
  }

}
