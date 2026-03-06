package frontroute

case class FrontrouteOptions(
  rewriteUrls: Boolean = true,
  rewriteAnchorHref: Boolean = true,
  rewriteLinkHref: Boolean = true,
  rewriteImageSrc: Boolean = true,
  rewriteScriptSrc: Boolean = true,
  rewriteIframeSrc: Boolean = true,
)

object FrontrouteOptions {

  val default = FrontrouteOptions()

}
