package frontroute.site

object TemplateVars {

  private val vars = Seq(
    "frontrouteVersion" -> "0.17.0-M10",
    "laminarVersion"    -> "15.0.0-M6",
    "scalajsVersion"    -> "1.13.0",
    "scala3version"     -> "3.2.1",
    "sitePrefix"        -> "/v/0.17.x"
  )

  def apply(s: String): String =
    vars.foldLeft(s) { case (acc, (varName, varValue)) =>
      acc.replace(s"{{${varName}}}", varValue)
    }

}
