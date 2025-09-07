package com.github.joechung2008.jsonscala.lib

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ValueParserTest extends AnyFlatSpec with Matchers {

  it should "return TrueToken for 'true'" in {
    val result = JSONParser.parse("true")
    result should be (Right(TrueToken(4)))
  }

  it should "return FalseToken for 'false'" in {
    val result = JSONParser.parse("false")
    result should be (Right(FalseToken(5)))
  }

  it should "return NullToken for 'null'" in {
    val result = JSONParser.parse("null")
    result should be (Right(NullToken(4)))
  }

  it should "handle whitespace before values" in {
    val result = JSONParser.parse("  \"test\"")
    result should be (Right(StringToken(8, "test")))
  }

  it should "handle complex nested structures" in {
    val result = JSONParser.parse("{\"array\": [1, {\"nested\": true}]}")
    result shouldBe a [Right[?, ?]]
  }

  it should "fail on unexpected characters" in {
    val result = JSONParser.parse("invalid")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on incomplete true" in {
    val result = JSONParser.parse("tru")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on incomplete false" in {
    val result = JSONParser.parse("fals")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on incomplete null" in {
    val result = JSONParser.parse("nul")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on empty input" in {
    val result = JSONParser.parse("")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail when delimiters are encountered unexpectedly" in {
    val result = JSONParser.parse(",")
    result shouldBe a [Left[?, ?]]
  }
}
