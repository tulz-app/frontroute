import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {

  val airstream: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "com.raquo" %%% "airstream" % DependencyVersions.airstream
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
