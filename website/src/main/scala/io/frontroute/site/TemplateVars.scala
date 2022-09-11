package io.frontroute.site

object TemplateVars {

  private val vars = Seq(
    "frontrouteVersion" -> "0.16.0",
    "laminarVersion"    -> "0.14.2",
    "scalajsVersion"    -> "1.10.1",
    "scala3version"     -> "3.2.0",
    "sitePrefix"        -> "/v/0.16.x"
  )

  def apply(s: String): String =
    vars.foldLeft(s) { case (acc, (varName, varValue)) =>
      acc.replace(s"{{${varName}}}", varValue)
    }

}
