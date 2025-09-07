package com.github.joechung2008.jsonscala.lib

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JSONParserTest extends AnyFlatSpec with Matchers {

  "JSONParser" should "parse a simple string" in {
    val result = JSONParser.parse("\"hello\"")
    result should be (Right(StringToken(7, "hello")))
  }

  it should "parse a number" in {
    val result = JSONParser.parse("42")
    result should be (Right(NumberToken(2, 42.0, "42")))
  }

  it should "parse true" in {
    val result = JSONParser.parse("true")
    result should be (Right(TrueToken(4)))
  }

  it should "parse false" in {
    val result = JSONParser.parse("false")
    result should be (Right(FalseToken(5)))
  }

  it should "parse null" in {
    val result = JSONParser.parse("null")
    result should be (Right(NullToken(4)))
  }

  it should "parse an empty array" in {
    val result = JSONParser.parse("[]")
    result should be (Right(ArrayToken(2, List())))
  }

  it should "parse an array with elements" in {
    val result = JSONParser.parse("[1, \"test\"]")
    result should be (Right(ArrayToken(11, List(NumberToken(1, 1.0, "1"), StringToken(6, "test")))))
  }

  it should "parse an empty object" in {
    val result = JSONParser.parse("{}")
    result should be (Right(ObjectToken(2, List())))
  }

  it should "fail on invalid JSON" in {
    val result = JSONParser.parse("{invalid}")
    result shouldBe a [Left[?, ?]]
  }

  it should "parse JSON with trailing spaces" in {
    val result = JSONParser.parse("\"hello\" ")
    result should be (Right(StringToken(7, "hello")))
  }

  it should "parse JSON with trailing newlines" in {
    val result = JSONParser.parse("\"hello\"\n")
    result should be (Right(StringToken(7, "hello")))
  }

  it should "parse JSON with trailing tabs and spaces" in {
    val result = JSONParser.parse("\"hello\"\t ")
    result should be (Right(StringToken(7, "hello")))
  }

  it should "parse numbers with trailing whitespace" in {
    val result = JSONParser.parse("42 ")
    result should be (Right(NumberToken(2, 42.0, "42")))
  }

  it should "parse booleans with trailing whitespace" in {
    val result = JSONParser.parse("true\n")
    result should be (Right(TrueToken(4)))
  }

  it should "fail on JSON with trailing non-whitespace characters" in {
    val result = JSONParser.parse("\"hello\"world")
    result shouldBe a [Left[?, ?]]
    result.left.get should include ("Unexpected characters after JSON value")
  }

  it should "fail on numbers with trailing non-whitespace characters" in {
    val result = JSONParser.parse("42abc")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on booleans with trailing non-whitespace characters" in {
    val result = JSONParser.parse("truefalse")
    result shouldBe a [Left[?, ?]]
    result.left.get should include ("Unexpected characters after JSON value")
  }
}
