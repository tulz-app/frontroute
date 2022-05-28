package io.frontroute.site

import com.raquo.laminar.api.L._
import io.frontroute.BrowserNavigation
import io.frontroute.site.layout.PageWrap
import io.laminext.syntax.core._
import io.laminext.syntax.tailwind._
import io.laminext.tailwind.modal.ModalContent
import io.laminext.tailwind.theme.Modal
import io.laminext.tailwind.theme.Theme
import org.scalajs.dom

class Routes {

  private val dsl = io.frontroute.dsl[(SiteModule, Page)]
  import dsl._

  private val mobileMenuContent = Var[Option[ModalContent]](None)

  private def notFound: Route =
    complete {
//      $page.writer.onNext(Some(Page("", "Not Found", () => Left((404, "Not Found")))))
      dom.window.scrollTo(0, 0)
      div("Not Found")
      ???
    }

  private def modulePrefix =
    pathPrefix(segment).flatMap { moduleName =>
      provide(Site.findModule(moduleName)).collect { case Some(module) =>
        Tuple1(module)
      }
    }

  private def modulePagePrefix(module: SiteModule) =
    pathPrefix(segment).flatMap { pageName =>
      provide(module.findPage(pageName)).collect { case Some(page) =>
        Tuple1(page)
      }
    }

  private val route =
    concat(
      pathEnd {
        complete(Site.indexModule -> Site.indexModule.index)
      },
      modulePrefix { module =>
        concat(
          pathEnd {
            complete(module -> module.index)
          },
          modulePagePrefix(module) { page =>
            complete(module -> page)
          }
        )
      },
      notFound
    )

  implicit val owner: Owner = unsafeWindowOwner

  private val routeResult = runRoute(route)
  routeResult.foreach { _ =>
    mobileMenuContent.writer.onNext(None)
    dom.window.scrollTo(0, 0)
  }
  private val $module     = routeResult.optionMap(_._1)
  private val $page       = routeResult.optionMap(_._2)

  private val mobileMenuModal: Modal = Theme.current.modal.customize(
    contentWrapTransition = _.customize(
      nonHidden = _ :+ "bg-gray-900"
    )
  )

  def start(): Unit = {
    val appContainer  = dom.document.querySelector("#app")
    val menuContainer = dom.document.querySelector("#menu-modal")
    val appContent    = PageWrap($module.signal, $page.signal, mobileMenuContent.writer)

    appContainer.innerHTML = ""
    com.raquo.laminar.api.L.render(appContainer, appContent)
    com.raquo.laminar.api.L.render(menuContainer, TW.modal(mobileMenuContent.signal, mobileMenuModal))

    BrowserNavigation.emitPopStateEvent()
  }

}
