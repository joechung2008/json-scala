package com.github.joechung2008.jsonscala.lib

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NumberParserTest extends AnyFlatSpec with Matchers {

  "NumberParser" should "parse zero" in {
    val result = JSONParser.parse("0")
    result should be (Right(NumberToken(1, 0.0, "0")))
  }

  it should "parse positive integers" in {
    val result = JSONParser.parse("42")
    result should be (Right(NumberToken(2, 42.0, "42")))
  }

  it should "parse large integers" in {
    val result = JSONParser.parse("123456789")
    result should be (Right(NumberToken(9, 123456789.0, "123456789")))
  }

  it should "parse negative integers" in {
    val result = JSONParser.parse("-42")
    result should be (Right(NumberToken(3, -42.0, "-42")))
  }

  it should "parse decimal numbers" in {
    val result = JSONParser.parse("3.14")
    result should be (Right(NumberToken(4, 3.14, "3.14")))
  }

  it should "parse negative decimal numbers" in {
    val result = JSONParser.parse("-2.5")
    result should be (Right(NumberToken(4, -2.5, "-2.5")))
  }

  it should "parse numbers with leading zero in decimal part" in {
    val result = JSONParser.parse("1.01")
    result should be (Right(NumberToken(4, 1.01, "1.01")))
  }

  it should "parse numbers starting with decimal point" in {
    val result = JSONParser.parse("0.5")
    result should be (Right(NumberToken(3, 0.5, "0.5")))
  }

  it should "parse scientific notation with positive exponent" in {
    val result = JSONParser.parse("1.23e4")
    result should be (Right(NumberToken(6, 12300.0, "1.23e4")))
  }

  it should "parse scientific notation with negative exponent" in {
    val result = JSONParser.parse("1.23e-4")
    result should be (Right(NumberToken(7, 0.000123, "1.23e-4")))
  }

  it should "parse scientific notation with uppercase E" in {
    val result = JSONParser.parse("2.5E3")
    result should be (Right(NumberToken(5, 2500.0, "2.5e3")))
  }

  it should "parse scientific notation with explicit positive exponent" in {
    val result = JSONParser.parse("1.5e+2")
    result should be (Right(NumberToken(6, 150.0, "1.5e+2")))
  }

  it should "parse integers in scientific notation" in {
    val result = JSONParser.parse("5e2")
    result should be (Right(NumberToken(3, 500.0, "5e2")))
  }

  it should "parse very small numbers" in {
    val result = JSONParser.parse("0.000001")
    result should be (Right(NumberToken(8, 0.000001, "0.000001")))
  }

  it should "parse very large numbers" in {
    val result = JSONParser.parse("1000000.0")
    result should be (Right(NumberToken(9, 1000000.0, "1000000.0")))
  }

  it should "handle numbers" in {
    val result = JSONParser.parse("42")
    result should be (Right(NumberToken(2, 42.0, "42")))
  }

  it should "handle numbers with whitespace" in {
    val result = JSONParser.parse("42")
    result should be (Right(NumberToken(2, 42.0, "42")))
  }

  it should "fail on numbers starting with multiple zeros" in {
    val result = JSONParser.parse("00")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on invalid characters in integer part" in {
    val result = JSONParser.parse("12a")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on numbers with multiple decimal points" in {
    val result = JSONParser.parse("1.2.3")
    result shouldBe a [Left[?, ?]]
  }

  it should "parse numbers ending with decimal point" in {
    val result = JSONParser.parse("1.")
    result should be (Right(NumberToken(2, 1.0, "1.")))
  }

  it should "fail on numbers with invalid exponent" in {
    val result = JSONParser.parse("1.2e")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on numbers with non-numeric exponent" in {
    val result = JSONParser.parse("1.2ea")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on empty string" in {
    val result = JSONParser.parse("")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on non-numeric input" in {
    val result = JSONParser.parse("abc")
    result shouldBe a [Left[?, ?]]
  }

  it should "parse number with correct skip" in {
    val result = JSONParser.parse("42")
    result shouldBe Right(NumberToken(2, 42.0, "42"))
  }
}
