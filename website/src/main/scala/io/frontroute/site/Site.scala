package io.frontroute.site

import io.frontroute.site.pages.CodeExamplePage
import io.frontroute.site.pages.DocumentationPage
import com.yurique.embedded.FileAsString
import io.frontroute.site.examples.CodeExample

object Site {

  val frontrouteVersion: String = "0.16.x"

  val thisVersionPrefix = s"/v/$frontrouteVersion/"

  def thisVersionHref(href: String): String =
    s"${thisVersionPrefix}${href.dropWhile(_ == '/')}"

  private def examplePage(
    example: CodeExample
  ): Page = Page(example.id, example.id + "/live", example.title, CodeExamplePage(example))

  private def docPage(
    path: String,
    title: String,
    markdown: String
  ): Page = Page(path, path, title, DocumentationPage(title, markdown))

  val indexModule: SiteModule =
    SiteModule(
      path = "",
      title = "frontroute",
      index = docPage("", "frontroute", FileAsString("/doc/index.md"))
    )

  val examples: Seq[CodeExample] =
    Seq(
      io.frontroute.site.examples.ex_basic.BasicRoutingExample,
      io.frontroute.site.examples.ex_path_matching.PathMatchingExample,
      io.frontroute.site.examples.ex_recursive_path_matching.RecursivePathMatchingExample,
      io.frontroute.site.examples.ex_params.ParamsExample,
      io.frontroute.site.examples.ex_custom_directives.CustomDirectivesExample,
      io.frontroute.site.examples.ex_signal.SignalExample,
      io.frontroute.site.examples.ex_auth.AuthExample,
      io.frontroute.site.examples.ex_tabs.TabsExample,
      io.frontroute.site.examples.ex_nested.NestedExample
    )

  val modules: Seq[SiteModule] = Seq(
    indexModule,
    SiteModule(
      path = "getting-started",
      title = "Getting started",
      index = docPage("", "Installation", FileAsString("/doc/getting-started/index.md")),
      "Introduction"       -> Seq(
        docPage("laminar-basics", "Laminar basics", FileAsString("/doc/getting-started/laminar-basics.md")),
        docPage("first-routes", "First routes", FileAsString("/doc/getting-started/first-routes.md")),
      )
    ),
    SiteModule(
      path = "overview",
      title = "Overview",
      index = docPage("", "Overview", FileAsString("/doc/overview/index.md")),
      "Concepts"           -> Seq(
        docPage("directive", "Directive", FileAsString("/doc/overview/concepts/directive.md")),
        docPage("path-matcher", "Path-matching", FileAsString("/doc/overview/concepts/path-matcher.md"))
      ),
      "Navigation"         -> Seq(
        docPage("navigation", "Browser navigation", FileAsString("/doc/overview/navigation.md")),
        docPage("link-handler", "Link handler", FileAsString("/doc/overview/link-handler.md"))
      ),
      "Utilities"          -> Seq(
        docPage("directive-utilities", "Directive utilities", FileAsString("/doc/overview/directive-utilities.md"))
      ),
      "Extending"          -> Seq(
        docPage("custom-directives", "Custom directives", FileAsString("/doc/overview/custom-directives.md"))
      ),
      "Under the hood"     -> Seq(
        docPage("route", "Route", FileAsString("/doc/overview/under-the-hood/route.md"))
      )
    ),
    SiteModule(
      path = "reference",
      title = "Reference",
      index = docPage("", "Reference", FileAsString("/doc/reference/index.md")),
      "Alternative Routes" -> Seq(
        docPage("concat", "concat", FileAsString("/doc/reference/concat.md"))
      ),
      "Directives"         -> Seq(
        docPage("directives", "Built-in directives", FileAsString("/doc/reference/built-in-directives.md")),
        docPage("signal-directive", ".signal directive", FileAsString("/doc/reference/signal-directive.md")),
        docPage("injecting-external-signal", "Injecting external signal", FileAsString("/doc/reference/injecting-external-signal.md")),
        docPage("conjunction", "conjunction (&)", FileAsString("/doc/reference/conjunction.md")),
        docPage("disjunction", "disjunction (|)", FileAsString("/doc/reference/disjunction.md"))
      ),
      "Path Matchers"      -> Seq(
        docPage("path-matchers", "Built-in path matchers", FileAsString("/doc/reference/built-in-path-matchers.md")),
        docPage("path-matcher-combinators", "Path matcher combinators", FileAsString("/doc/reference/path-matcher-combinators.md"))
      )
    ),
    SiteModule(
      path = "examples",
      title = "Examples",
      index = docPage("", "Examples", FileAsString("/doc/examples/index.md")),
      ""                   -> examples.map(ex => examplePage(ex))
    )
  )

  def findModule(path: String): Option[SiteModule] =
    modules.find(_.path == path)

}
