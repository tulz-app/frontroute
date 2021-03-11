import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import dotty.tools.sbtplugin.DottyPlugin.autoImport._

object Dependencies {

  val airstream: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      ("com.raquo" %%% "airstream" % DependencyVersions.airstream).withDottyCompat(scalaVersion.value)
    )
  }

  val `tuplez-apply`: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "app.tulz" %%% "tuplez-apply" % DependencyVersions.`tuplez-apply`
    )
  }

  val utest: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "com.lihaoyi" %%% "utest" % DependencyVersions.utest % Test
    )
  }

}
