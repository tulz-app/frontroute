package frontroute.internal

import org.scalajs.dom
import scala.scalajs.js

object UrlString {

  def unapply(url: String): Some[dom.Location] = {
    val l = dom.document.createElement("a").asInstanceOf[dom.HTMLAnchorElement]
    l.href = url
    Some(
      js.Dynamic
        .literal(
          hash = l.hash,
          protocol = l.protocol,
          search = l.search,
          href = l.href,
          hostname = l.hostname,
          port = l.port,
          pathname = l.pathname,
          host = l.host
        )
        .asInstanceOf[dom.Location]
    )
  }

}
