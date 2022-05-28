import sbt._

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {

  val airstream: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "com.raquo" %%% "airstream" % DependencyVersions.airstream
    )
  }

  val laminar: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "com.raquo" %%% "laminar" % DependencyVersions.laminar
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

  // wesite

  val laminext: Def.Initialize[Seq[ModuleID]] = Def.setting {
    Seq(
      "io.laminext" %%% "core"                   % DependencyVersions.laminext,
      "io.laminext" %%% "highlight"              % DependencyVersions.laminext,
      "io.laminext" %%% "markdown"               % DependencyVersions.laminext,
      "io.laminext" %%% "ui"                     % DependencyVersions.laminext,
      "io.laminext" %%% "tailwind"               % DependencyVersions.laminext,
      "io.laminext" %%% "util"                   % DependencyVersions.laminext,
      "io.laminext" %%% "fetch"                  % DependencyVersions.laminext,
      "io.laminext" %%% "tailwind-default-theme" % DependencyVersions.laminext
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

}
