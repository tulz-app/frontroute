package io.frontroute.site

//import io.frontroute.site.examples.CodeExample
//import io.frontroute.site.pages.CodeExamplePage
import io.frontroute.site.pages.CodeExamplePage
import io.frontroute.site.pages.DocumentationPage
import com.yurique.embedded.FileAsString
import io.frontroute.site.examples.CodeExample

object Site {

  private def examplePage(
    example: CodeExample
  ): Page = Page(example.id, example.title, CodeExamplePage(example))

  private def docPage(
    path: String,
    title: String,
    markdown: String
  ): Page = Page(path, title, DocumentationPage(title, markdown))

  val indexModule: SiteModule =
    SiteModule(
      path = "",
      index = docPage("", "frontroute", FileAsString("/doc/index.md"))
    )

  val modules: Seq[SiteModule] = Seq(
    indexModule,
    SiteModule(
      path = "overview",
      index = docPage("", "Overview", FileAsString("/doc/overview/index.md")),
      "Concepts"           -> Seq(
        docPage("route", "Route", FileAsString("/doc/overview/concepts/route.md")),
        docPage("directive", "Directive", FileAsString("/doc/overview/concepts/directive.md")),
        docPage("path-matcher", "Path-matching", FileAsString("/doc/overview/concepts/path-matcher.md")),
        docPage("location-provider", "Location Provider", FileAsString("/doc/overview/concepts/location-provider.md"))
      ),
      "Navigation"         -> Seq(
        docPage("navigation", "Browser navigation", FileAsString("/doc/overview/navigation.md")),
        docPage("link-handler", "Link handler", FileAsString("/doc/overview/link-handler.md"))
      ),
      "Utilities"          -> Seq(
        docPage("directive-utilities", "Directive Utilities", FileAsString("/doc/overview/directive-utilities.md")),
        docPage("debugging", "Debugging", FileAsString("/doc/overview/debugging.md"))
      ),
      "Extending"          -> Seq(
        docPage("custom-directives", "Custom Directives", FileAsString("/doc/overview/custom-directives.md"))
      )
    ),
    SiteModule(
      path = "reference",
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
      ),
      "Other Classes"      -> Seq(
        docPage("route-result", "Route Result", FileAsString("/doc/reference/route-result.md"))
      )
    ),
    SiteModule(
      path = "examples",
      index = docPage("", "Examples", FileAsString("/doc/examples/index.md")),
      ""                   -> Seq(
        examplePage(io.frontroute.site.examples.ex_basic.BasicRoutingExample),
        examplePage(io.frontroute.site.examples.ex_path_matching.PathMatchingExample),
        examplePage(io.frontroute.site.examples.ex_recursive_path_matching.RecursivePathMatchingExample),
        examplePage(io.frontroute.site.examples.ex_params.ParamsExample),
        examplePage(io.frontroute.site.examples.ex_custom_directives.CustomDirectivesExample),
        examplePage(io.frontroute.site.examples.ex_signal.SignalExample),
        examplePage(io.frontroute.site.examples.ex_memoize.MemoizeExample),
        examplePage(io.frontroute.site.examples.ex_auth.AuthExample),
        examplePage(io.frontroute.site.examples.ex_tabs.TabsExample)
      )
    )
  )

  def findModule(path: String): Option[SiteModule] =
    modules.find(_.path == path)

}
