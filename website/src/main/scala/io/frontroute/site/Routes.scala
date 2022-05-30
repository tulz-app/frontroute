package io.frontroute.site

import com.raquo.laminar.api.L._
import io.frontroute.BrowserNavigation
import io.frontroute.site.layout.PageWrap
import io.laminext.syntax.tailwind._
import io.laminext.tailwind.modal.ModalContent
import io.laminext.tailwind.theme.Modal
import io.laminext.tailwind.theme.Theme
import org.scalajs.dom
import io.frontroute._
import io.frontroute.Implicits.locationProvider

class Routes {

  private val mobileMenuContent = Var[Option[ModalContent]](None)

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

  private val mobileMenuModal: Modal = Theme.current.modal.customize(
    contentWrapTransition = _.customize(
      nonHidden = _ :+ "bg-gray-900"
    )
  )

  def start(): Unit = {
    val appContainer  = dom.document.querySelector("#app")
    val menuContainer = dom.document.querySelector("#menu-modal")

    appContainer.innerHTML = ""
    com.raquo.laminar.api.L.render(
      appContainer,
      div(
        cls := "contents",
        concat(
          pathEnd {
            PageWrap(Val(Some((Site.indexModule, Site.indexModule.index))), mobileMenuContent.writer)
          },
          (modulePrefix & pathEnd).signal { module =>
            PageWrap(module.map(m => Some((m, m.index))), mobileMenuContent.writer)
          },
          moduleAndPagePrefix.signal { moduleAndPage =>
            PageWrap(moduleAndPage.map(t => Some(t)), mobileMenuContent.writer)
          },
          div("Not Found")
        )
      )
    )
    com.raquo.laminar.api.L.render(menuContainer, TW.modal(mobileMenuContent.signal, mobileMenuModal))

    BrowserNavigation.emitPopStateEvent()
  }

}
