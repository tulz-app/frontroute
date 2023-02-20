package frontroute

import org.scalajs.dom
import scala.scalajs.js.URIUtils.encodeURIComponent
import scala.scalajs.js.URIUtils.decodeURIComponent

object LocationUtils {

  def parseLocationParams(location: dom.Location): Map[String, Seq[String]] = {
    val vars   = location.search.dropWhile(_ == '?').split('&')
    val result = scala.collection.mutable.Map[String, Seq[String]]()
    vars.foreach { entry =>
      entry.split('=') match {
        case Array(key, value) =>
          val decodedKey   = decodeURIComponent(key)
          val decodedValue = decodeURIComponent(value)
          result(decodedKey) = result.getOrElse(decodedKey, Seq.empty) :+ decodedValue
        case _                 =>
      }
    }
    result.toMap
  }

  def encodeLocationParams(params: Map[String, Seq[String]]): String =
    if (params.isEmpty) {
      ""
    } else {
      s"?${params
          .flatMap { case (name, values) =>
            values.map { value =>
              s"${encodeURIComponent(name)}=${encodeURIComponent(value)}"
            }
          }.mkString("&")}"
    }

}
