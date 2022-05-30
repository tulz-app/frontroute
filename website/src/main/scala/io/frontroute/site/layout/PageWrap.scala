package io.frontroute.site.layout

import io.frontroute.site.Page
import io.frontroute.site.Site
import io.frontroute.site.SiteModule
import io.frontroute.site.Styles
import com.raquo.laminar.api.L._
import io.laminext.syntax.core._
import io.laminext.syntax.tailwind._
import com.raquo.laminar.nodes.ReactiveHtmlElement

object PageWrap {

  def apply(
    $page: Signal[Option[(SiteModule, Page)]],
    menuObserver: Observer[Option[ModalContent]]
  ): ReactiveHtmlElement.Base = {
    div(
      linkTag(
        rel := "stylesheet",
        href <-- Styles.highlightStyle.signal.map(s => s"/stylesheets/highlightjs/${s}.css")
      ),
      div(
        cls := "h-screen flex flex-col",
        PageHeader($page, menuObserver),
        noScript(
          div(
            cls := "max-w-5xl border-l-4 border-red-400 bg-red-50 text-red-900 mx-auto p-4 font-condensed",
            "Your browser does not support JavaScript: some features of this site may not work properly."
          )
        ),
        div(
          cls := "flex-1 flex overflow-hidden",
          PageNavigation($page).hiddenIf($page.optionMap(_._1).optionContains(Site.indexModule)),
          div(
            cls := "flex-1 bg-gray-200 overflow-auto md:p-4",
            div(
              cls := "lg:container lg:mx-auto lg:max-w-4xl lg:p-8 p-4 bg-white min-h-full",
              child.maybe <-- $page.optionMap { case (_, page) => page.render() }
            )
          )
        ),
        PageFooter()
      )
    )
  }

}
