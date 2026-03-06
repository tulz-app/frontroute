package frontroute

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import frontroute.testing.*
import org.scalajs.dom

class HrefHandlerTests extends TestBase {

  test("href-s don't change without baseName") {
    routeTestDom(
      route = path("a") {
        L.a(
          href   := "/root-path",
          idAttr := "root-anchor",
        )
      },
      init = locationProvider => {
        locationProvider.path("a")
      },
      baseName = ""
    ) { root =>
      val anchors = root.getElementsByTagName("a")
      val anchor  = anchors.find(_.id == "root-anchor").value.asInstanceOf[dom.HTMLAnchorElement]
      anchor.getAttribute("href") shouldBe "/root-path"
    }
  }

  test("relative href-s work without baseName") {
    routeTestDom(
      route = pathPrefix("a") {
        path("b") {
          L.a(
            href   := "sub-path",
            idAttr := "sub-anchor",
          )
        }
      },
      init = locationProvider => {
        locationProvider.path("a", "b")
      },
      baseName = ""
    ) { root =>
      val anchors = root.getElementsByTagName("a")
      val anchor  = anchors.find(_.id == "sub-anchor").value.asInstanceOf[dom.HTMLAnchorElement]
      anchor.getAttribute("href") shouldBe "/a/b/sub-path"
    }
  }

  test("relative .. href-s work without baseName") {
    routeTestDom(
      route = pathPrefix("a") {
        path("b") {
          L.a(
            href   := "../sibling-path",
            idAttr := "sibling-anchor",
          )
        }
      },
      init = locationProvider => {
        locationProvider.path("a", "b")
      },
      baseName = ""
    ) { root =>
      val anchors = root.getElementsByTagName("a")
      val anchor  = anchors.find(_.id == "sibling-anchor").value.asInstanceOf[dom.HTMLAnchorElement]
      anchor.getAttribute("href") shouldBe "/a/sibling-path"
    }
  }

  test("relative href-s are ignored when requested, without baseName") {
    routeTestDom(
      route = pathPrefix("a") {
        path("b") {
          L.a(
            href                   := "../sibling-path-dont-touch-me",
            idAttr                 := "sibling-anchor",
            dataAttr("fr-rewrite") := "ignore"
          )
        }
      },
      init = locationProvider => {
        locationProvider.path("a", "b")
      },
      baseName = ""
    ) { root =>
      val anchors = root.getElementsByTagName("a")
      val anchor  = anchors.find(_.id == "sibling-anchor").value.asInstanceOf[dom.HTMLAnchorElement]
      anchor.getAttribute("href") shouldBe "../sibling-path-dont-touch-me"
    }
  }

  test("href-s do change with baseName") {
    routeTestDom(
      route = path("a") {
        L.a(
          href   := "/root-path",
          idAttr := "root-anchor",
        )
      },
      init = locationProvider => {
        locationProvider.path("test-base-name", "a")
      },
      baseName = "/test-base-name"
    ) { root =>
      val anchors = root.getElementsByTagName("a")
      val anchor  = anchors.find(_.id == "root-anchor").value.asInstanceOf[dom.HTMLAnchorElement]
      anchor.getAttribute("href") shouldBe "/test-base-name/root-path"
    }
  }

  test("relative href-s work with baseName") {
    routeTestDom(
      route = pathPrefix("a") {
        path("b") {
          L.a(
            href   := "sub-path",
            idAttr := "sub-anchor",
          )
        }
      },
      init = locationProvider => {
        locationProvider.path("test-base-name", "a", "b")
      },
      baseName = "/test-base-name"
    ) { root =>
      val anchors = root.getElementsByTagName("a")
      val anchor  = anchors.find(_.id == "sub-anchor").value.asInstanceOf[dom.HTMLAnchorElement]
      anchor.getAttribute("href") shouldBe "/test-base-name/a/b/sub-path"
    }
  }

  test("relative .. href-s work with baseName") {
    routeTestDom(
      route = pathPrefix("a") {
        path("b") {
          L.a(
            href   := "../sibling-path",
            idAttr := "sibling-anchor",
          )
        }
      },
      init = locationProvider => {
        locationProvider.path("test-base-name", "a", "b")
      },
      baseName = "/test-base-name"
    ) { root =>
      val anchors = root.getElementsByTagName("a")
      val anchor  = anchors.find(_.id == "sibling-anchor").value.asInstanceOf[dom.HTMLAnchorElement]
      anchor.getAttribute("href") shouldBe "/test-base-name/a/sibling-path"
    }
  }

  test("relative href-s are ignored when requested, with baseName") {
    routeTestDom(
      route = pathPrefix("a") {
        path("b") {
          L.a(
            href                   := "../sibling-path-dont-touch-me",
            idAttr                 := "sibling-anchor",
            dataAttr("fr-rewrite") := "ignore"
          )
        }
      },
      init = locationProvider => {
        locationProvider.path("test-base-name", "a", "b")
      },
      baseName = "/test-base-name"
    ) { root =>
      val anchors = root.getElementsByTagName("a")
      val anchor  = anchors.find(_.id == "sibling-anchor").value.asInstanceOf[dom.HTMLAnchorElement]
      anchor.getAttribute("href") shouldBe "../sibling-path-dont-touch-me"
    }
  }

}
