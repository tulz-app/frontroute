package frontroute

case class FrontrouteOptions(
  installHrefHandler: Boolean = true,
  processAnchorHref: Boolean = true,
  processLinkHref: Boolean = true,
  processImageSrc: Boolean = true,
  processScriptSrc: Boolean = true,
  processIframeSrc: Boolean = true,
)

object FrontrouteOptions {

  val default = FrontrouteOptions()

}
