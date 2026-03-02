package frontroute.site

import com.raquo.laminar.api.L.*
import frontroute.site.layout.PageWrap
import org.scalajs.dom
import frontroute.*

class Routes {

  private val mobileMenuContent = Var[Option[Element]](None)

  private def modulePrefix: Directive[SiteModule] =
    pathPrefix(segment).flatMap { moduleName =>
      provide(Site.findModule(moduleName)).collect { case Some(module) =>
        module
      }
    }

  private def moduleAndPagePrefix: Directive[(SiteModule, Page)] =
    modulePrefix.flatMap { module =>
      pathPrefix(segment).flatMap { pageName =>
        provide(module.findPage(pageName)).collect { case Some(page) =>
          (module, page)
        }
      }
    }

  def start(): Unit = {
    val appContainer = dom.document.querySelector("#app-container")

    appContainer.innerHTML = ""
    val _ = com.raquo.laminar.api.L.render(
      appContainer,
      routes(s"/v/${Site.frontrouteVersion}")(
        div(
          cls := "contents",
          LinkHandler.bind,
          firstMatch(
            (
              pathEnd.mapTo(Some((Site.indexModule, Site.indexModule.index))) |
                (modulePrefix & pathEnd).map(m => Some((m, m.index))) |
                moduleAndPagePrefix.map(moduleAndPage => Some(moduleAndPage))
            ).signal { moduleAndPage =>
              PageWrap(moduleAndPage, mobileMenuContent)
            },
            div("Not Found"),
          ),
        )
      )
    )
  }

}
