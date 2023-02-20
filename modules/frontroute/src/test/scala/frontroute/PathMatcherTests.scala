package frontroute

import frontroute.internal.PathMatchResult
import frontroute.testing._
import scala.scalajs.js
import scala.scalajs.js.JSON

class PathMatcherTests extends TestBase {

  test("segment/empty input") {
    segment.apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("segment/non-empty input") {
    segment.apply(List("before"), List("a", "b")) shouldBe PathMatchResult.Match(
      "a",
      List("before", "a"),
      List("b")
    )
  }

  test("fixed segment/empty input") {
    segment("a").apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("fixed segment/matching prefix") {
    segment("a").apply(List("before"), List("a", "b")) shouldBe PathMatchResult.Match(
      (),
      List("before", "a"),
      List("b")
    )
  }

  test("fixed segment/non-matching input") {
    segment("a").apply(List.empty, List("c", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("regex/empty input") {
    frontroute.regex("[a-z]".r).apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("regex/matching prefix") {
    frontroute.regex("[a-z]".r).apply(List("before"), List("a", "b")) match {
      case PathMatchResult.Match(m, consumed, tail) =>
        m.group(0) shouldBe "a"
        consumed shouldBe List("before", "a")
        tail shouldBe List("b")
      case other                                    =>
        throw new java.lang.AssertionError(s"not a match: $other")
    }
  }

  test("regex/non-matching input") {
    frontroute.regex("[a-z]".r).apply(List.empty, List("1", "2")) shouldBe PathMatchResult.Rejected(
      List("2")
    )
  }

  test("oneOf segment/empty input") {
    segment(Seq("a", "z")).apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("oneOf segment/matching prefix") {
    segment(Seq("a", "z")).apply(List("before"), List("a", "b")) shouldBe PathMatchResult.Match(
      "a",
      List("before", "a"),
      List("b")
    )
    segment(Seq("a", "z")).apply(List("before"), List("z", "b")) shouldBe PathMatchResult.Match(
      "z",
      List("before", "z"),
      List("b")
    )
  }

  test("oneOf segment/non-matching input") {
    segment(Seq("a", "z")).apply(List.empty, List("c", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("long/empty input") {
    long.apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("long/matching prefix") {
    long.apply(List("before"), List("123", "b")) shouldBe PathMatchResult.Match(
      123,
      List("before", "123"),
      List("b")
    )
  }

  test("long/non-matching input") {
    long.apply(List.empty, List("ccc", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("double/empty input") {
    double.apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("double/matching prefix") {
    double.apply(List("before"), List("123", "b")) shouldBe PathMatchResult.Match(
      123,
      List("before", "123"),
      List("b")
    )
  }

  test("double/non-matching input") {
    double.apply(List.empty, List("ccc", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("fixed segment/empty input/recover") {
    segment("a").mapTo("a").recover("default").apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("fixed segment/non-matching input/recover") {
    segment("a").mapTo("a").recover("default").apply(List("before"), List("c", "b")) shouldBe PathMatchResult.Match(
      "default",
      List("before"),
      List("b")
    )
  }

  test("3x fixed segment/empty input") {
    (segment("a") / "b" / "c").apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("3x fixed segment/short input") {
    (segment("a") / "b" / "c").apply(List.empty, List("a", "b")) shouldBe PathMatchResult.NoMatch
  }

  test("3x fixed segment/matching input") {
    (segment("a") / "b" / "c").apply(List("before"), List("a", "b", "c", "d")) shouldBe PathMatchResult.Match(
      (),
      List("before", "a", "b", "c"),
      List("d")
    )
  }

  test("3x segment/empty input") {
    (segment / segment / segment).apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("3x segment/short input") {
    (segment / segment / segment).apply(List.empty, List("a", "b")) shouldBe PathMatchResult.NoMatch
  }

  test("3x segment/matching input") {
    (segment / segment / segment).apply(List("before"), List("a", "b", "c", "d")) shouldBe PathMatchResult.Match(
      ("a", "b", "c"),
      List("before", "a", "b", "c"),
      List("d")
    )
  }

  test("segment/empty input/map") {
    segment.map(_.toUpperCase).apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("segment/matching prefix/map") {
    segment.map(_.toUpperCase).apply(List("before"), List("a", "b")) shouldBe PathMatchResult.Match(
      "A",
      List("before", "a"),
      List("b")
    )
  }

  test("long/matching input/map") {
    long.map(_ * 2).apply(List("before"), List("123", "b")) shouldBe PathMatchResult.Match(
      246,
      List("before", "123"),
      List("b")
    )
  }

  test("long/non-matching input/map") {
    long.map(_ * 2).apply(List.empty, List("c", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("segment/empty input/filter") {
    segment.filter(_.forall(_.isDigit)).apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("segment/non-matching prefix/filter") {
    segment.filter(_.forall(_.isDigit)).apply(List.empty, List("a", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("segment/matching prefix/filter") {
    segment.filter(_.forall(_.isDigit)).apply(List("before"), List("123", "b")) shouldBe PathMatchResult.Match(
      "123",
      List("before", "123"),
      List("b")
    )
  }

  test("not fixed segment/empty input") {
    (!segment("a")).apply(List.empty, List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("not fixed segment/non-matching prefix") {
    (!segment("a")).apply(List.empty, List("a", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("not fixed segment/matching prefix") {
    (!segment("a")).apply(List("before"), List("c", "b")) shouldBe PathMatchResult.Match(
      (),
      List("before"),
      List("b")
    )
  }

}
