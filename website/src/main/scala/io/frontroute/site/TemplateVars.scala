package io.frontroute.site

object TemplateVars {

  val vars = Seq(
    "frontrouteVersion" -> "0.14.0"
  )

  def apply(s: String): String =
    vars.foldLeft(s) { case (acc, (varName, varValue)) =>
      acc.replace(s"{{${varName}}}", varValue)
    }

}
