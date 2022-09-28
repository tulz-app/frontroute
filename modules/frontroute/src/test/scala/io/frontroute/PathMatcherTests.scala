package io.frontroute

import io.frontroute.internal.PathMatchResult
import io.frontroute.testing._
import scala.scalajs.js
import scala.scalajs.js.JSON

class PathMatcherTests extends TestBase {

  test("segment/empty input") {
    segment.apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("segment/non-empty input") {
    segment.apply(List("a", "b")) shouldBe PathMatchResult.Match(
      "a",
      List("b")
    )
  }

  test("fixed segment/empty input") {
    segment("a").apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("fixed segment/matching prefix") {
    segment("a").apply(List("a", "b")) shouldBe PathMatchResult.Match(
      (),
      List("b")
    )
  }

  test("fixed segment/non-matching input") {
    segment("a").apply(List("c", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("regex/empty input") {
    io.frontroute.regex("[a-z]".r).apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("regex/matching prefix") {
    io.frontroute.regex("[a-z]".r).apply(List("a", "b")) match {
      case PathMatchResult.Match(m, tail) =>
        m.group(0) shouldBe "a"
        tail shouldBe List("b")
      case other                          =>
        throw new java.lang.AssertionError(s"not a match: $other")
    }
  }

  test("regex/non-matching input") {
    io.frontroute.regex("[a-z]".r).apply(List("1", "2")) shouldBe PathMatchResult.Rejected(
      List("2")
    )
  }

  test("oneOf segment/empty input") {
    segment(Seq("a", "z")).apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("oneOf segment/matching prefix") {
    segment(Seq("a", "z")).apply(List("a", "b")) shouldBe PathMatchResult.Match(
      "a",
      List("b")
    )
    segment(Seq("a", "z")).apply(List("z", "b")) shouldBe PathMatchResult.Match(
      "z",
      List("b")
    )
  }

  test("oneOf segment/non-matching input") {
    segment(Seq("a", "z")).apply(List("c", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("long/empty input") {
    long.apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("long/matching prefix") {
    long.apply(List("123", "b")) shouldBe PathMatchResult.Match(
      123,
      List("b")
    )
  }

  test("long/non-matching input") {
    long.apply(List("ccc", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("double/empty input") {
    double.apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("double/matching prefix") {
    double.apply(List("123", "b")) shouldBe PathMatchResult.Match(
      123,
      List("b")
    )
  }

  test("double/non-matching input") {
    double.apply(List("ccc", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("fixed segment/empty input/recover") {
    segment("a").mapTo("a").recover("default").apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("fixed segment/non-matching input/recover") {
    segment("a").mapTo("a").recover("default").apply(List("c", "b")) shouldBe PathMatchResult.Match(
      "default",
      List("b")
    )
  }

  test("3x fixed segment/empty input") {
    (segment("a") / "b" / "c").apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("3x fixed segment/short input") {
    (segment("a") / "b" / "c").apply(List("a", "b")) shouldBe PathMatchResult.NoMatch
  }

  test("3x fixed segment/matching input") {
    (segment("a") / "b" / "c").apply(List("a", "b", "c", "d")) shouldBe PathMatchResult.Match(
      (),
      List("d")
    )
  }

  test("3x segment/empty input") {
    (segment / segment / segment).apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("3x segment/short input") {
    (segment / segment / segment).apply(List("a", "b")) shouldBe PathMatchResult.NoMatch
  }

  test("3x segment/matching input") {
    (segment / segment / segment).apply(List("a", "b", "c", "d")) shouldBe PathMatchResult.Match(
      ("a", "b", "c"),
      List("d")
    )
  }

  test("segment/empty input/map") {
    segment.map(_.toUpperCase).apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("segment/matching prefix/map") {
    segment.map(_.toUpperCase).apply(List("a", "b")) shouldBe PathMatchResult.Match(
      "A",
      List("b")
    )
  }

  test("long/matching input/map") {
    long.map(_ * 2).apply(List("123", "b")) shouldBe PathMatchResult.Match(
      246,
      List("b")
    )
  }

  test("long/non-matching input/map") {
    long.map(_ * 2).apply(List("c", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("segment/empty input/filter") {
    segment.filter(_.forall(_.isDigit)).apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("segment/non-matching prefix/filter") {
    segment.filter(_.forall(_.isDigit)).apply(List("a", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("segment/matching prefix/filter") {
    segment.filter(_.forall(_.isDigit)).apply(List("123", "b")) shouldBe PathMatchResult.Match(
      "123",
      List("b")
    )
  }

  test("not fixed segment/empty input") {
    (!segment("a")).apply(List.empty) shouldBe PathMatchResult.NoMatch
  }

  test("not fixed segment/non-matching prefix") {
    (!segment("a")).apply(List("a", "b")) shouldBe PathMatchResult.Rejected(
      List("b")
    )
  }

  test("not fixed segment/matching prefix") {
    (!segment("a")).apply(List("c", "b")) shouldBe PathMatchResult.Match(
      (),
      List("b")
    )
  }

}
