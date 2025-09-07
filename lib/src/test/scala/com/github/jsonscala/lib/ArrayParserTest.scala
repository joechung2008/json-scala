package com.github.jsonscala.lib

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ArrayParserTest extends AnyFlatSpec with Matchers {

  "ArrayParser" should "parse an empty array" in {
    val result = JSONParser.parse("[]")
    result should be (Right(ArrayToken(2, List())))
  }

  it should "parse an array with a single number" in {
    val result = JSONParser.parse("[42]")
    result should be (Right(ArrayToken(4, List(NumberToken(2, 42.0, "42")))))
  }

  it should "parse an array with a single string" in {
    val result = JSONParser.parse("[\"hello\"]")
    result should be (Right(ArrayToken(9, List(StringToken(7, "hello")))))
  }

  it should "parse an array with multiple elements" in {
    val result = JSONParser.parse("[1, \"test\", true]")
    result should be (Right(ArrayToken(17, List(
      NumberToken(1, 1.0, "1"),
      StringToken(6, "test"),
      TrueToken(4)
    ))))
  }

  it should "parse an array with nested objects" in {
    val result = JSONParser.parse("[{\"key\": \"value\"}]")
    result should be (Right(ArrayToken(18, List(
      ObjectToken(16, List(PairToken(14, StringToken(5, "key"), StringToken(8, "value"))))
    ))))
  }

  it should "parse an array with nested arrays" in {
    val result = JSONParser.parse("[[1, 2], [3, 4]]")
    result should be (Right(ArrayToken(16, List(
      ArrayToken(6, List(NumberToken(1, 1.0, "1"), NumberToken(1, 2.0, "2"))),
      ArrayToken(6, List(NumberToken(1, 3.0, "3"), NumberToken(1, 4.0, "4")))
    ))))
  }

  it should "handle whitespace in arrays" in {
    val result = JSONParser.parse("[  1  ,  \"test\"  ]")
    result should be (Right(ArrayToken(18, List(
      NumberToken(1, 1.0, "1"),
      StringToken(6, "test")
    ))))
  }

  it should "parse arrays with null values" in {
    val result = JSONParser.parse("[null, 42]")
    result should be (Right(ArrayToken(10, List(
      NullToken(4),
      NumberToken(2, 42.0, "42")
    ))))
  }

  it should "parse arrays with false values" in {
    val result = JSONParser.parse("[false, true]")
    result should be (Right(ArrayToken(13, List(
      FalseToken(5),
      TrueToken(4)
    ))))
  }

  it should "parse empty array with correct skip" in {
    val result = JSONParser.parse("[]")
    result shouldBe Right(ArrayToken(2, List()))
  }

  it should "parse array with one element with correct skip" in {
    val result = JSONParser.parse("[1]")
    result shouldBe Right(ArrayToken(3, List(NumberToken(1, 1.0, "1"))))
  }

  it should "fail on arrays with trailing comma before closing bracket" in {
    val result = JSONParser.parse("[1, 2,")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on arrays with trailing comma" in {
    val result = JSONParser.parse("[1, 2,]")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on arrays with unclosed brackets" in {
    val result = JSONParser.parse("[1, 2")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on invalid array start" in {
    val result = JSONParser.parse("1, 2]")
    result shouldBe a [Left[?, ?]]
  }
}
