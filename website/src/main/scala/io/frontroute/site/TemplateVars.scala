package io.frontroute.site

object TemplateVars {

  private val vars = Seq(
    "frontrouteVersion" -> "0.16.1",
    "laminarVersion"    -> "0.14.5",
    "scalajsVersion"    -> "1.11.0",
    "scala3version"     -> "3.2.1",
    "sitePrefix"        -> "/v/0.16.x"
  )

  def apply(s: String): String =
    vars.foldLeft(s) { case (acc, (varName, varValue)) =>
      acc.replace(s"{{${varName}}}", varValue)
    }

}
