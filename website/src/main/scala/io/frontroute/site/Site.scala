package io.frontroute.site

import io.frontroute.site.pages.CodeExamplePage
import io.frontroute.site.pages.DocumentationPage
import com.yurique.embedded.FileAsString
import io.frontroute.site.examples.CodeExample

object Site {

  val frontrouteVersion: String = "0.17.x"

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
      io.frontroute.site.examples.ex_advanced_params.AdvancedParamsExample,
      io.frontroute.site.examples.ex_custom_directives.CustomDirectivesExample,
      io.frontroute.site.examples.ex_auth.AuthExample,
      io.frontroute.site.examples.ex_tabs.TabsExample,
      io.frontroute.site.examples.ex_nested.NestedExample,
      io.frontroute.site.examples.ex_effect.EffectExample,
      io.frontroute.site.examples.ex_extract_matched_path.ExtractMatchedPathExample,
      io.frontroute.site.examples.ex_matched_path.MatchedPathExample,
    )

  val modules: Seq[SiteModule] = Seq(
    indexModule,
    SiteModule(
      path = "getting-started",
      title = "Getting started",
      index = docPage("", "", FileAsString("/doc/getting-started/index.md")),
      ""                   -> Seq(
        docPage("laminar-basics", "Laminar basics", FileAsString("/doc/getting-started/laminar-basics.md")),
        docPage("first-routes", "First routes", FileAsString("/doc/getting-started/first-routes.md")),
        docPage("handling-not-found", "Handling 'Not Found'", FileAsString("/doc/getting-started/handling-not-found.md")),
        docPage("links-and-navigation", "Links and Navigation", FileAsString("/doc/getting-started/links-and-navigation.md")),
        docPage("building-routes", "Building routes", FileAsString("/doc/getting-started/building-routes.md")),
        docPage("nested-routes", "Nested routes", FileAsString("/doc/getting-started/nested-routes.md")),
      )
    ),
    SiteModule(
      path = "reference",
      title = "Reference",
      index = docPage("", "Reference", FileAsString("/doc/reference/index.md")),
      "Directives"         -> Seq(
        docPage("directives", "Built-in directives", FileAsString("/doc/reference/built-in-directives.md")),
        docPage("signal-directive", ".signal directive", FileAsString("/doc/reference/signal-directive.md")),
        docPage("conjunction", "conjunction (&)", FileAsString("/doc/reference/conjunction.md")),
        docPage("disjunction", "disjunction (|)", FileAsString("/doc/reference/disjunction.md")),
        docPage("directive-combinators", "Directive combinators", FileAsString("/doc/reference/directive-combinators.md")),
      ),
      "Path Matchers"      -> Seq(
        docPage("path-matchers", "Built-in path matchers", FileAsString("/doc/reference/built-in-path-matchers.md")),
        docPage("path-matcher-combinators", "Path matcher combinators", FileAsString("/doc/reference/path-matcher-combinators.md"))
      ),
      "Alternative Routes" -> Seq(
        docPage("first-match", "firstMatch", FileAsString("/doc/reference/first-match.md"))
      ),
      "Utilities"          -> Seq(
        docPage("navigation", "Browser navigation", FileAsString("/doc/reference/navigation.md")),
      ),
      "Extending"          -> Seq(
        docPage("custom-directives", "Custom directives", FileAsString("/doc/reference/custom-directives.md"))
      ),
      "Under the hood"     -> Seq(
        docPage("route", "Route", FileAsString("/doc/reference/under-the-hood/route.md")),
        docPage("directive", "Directive", FileAsString("/doc/reference/under-the-hood/directive.md")),
        docPage("path-matching", "Path-matching", FileAsString("/doc/reference/under-the-hood/path-matching.md")),
      ),
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
