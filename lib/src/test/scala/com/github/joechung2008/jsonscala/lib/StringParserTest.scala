package com.github.joechung2008.jsonscala.lib

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StringParserTest extends AnyFlatSpec with Matchers {

  "StringParser" should "parse a simple string" in {
    val result = JSONParser.parse("\"hello\"")
    result should be (Right(StringToken(7, "hello")))
  }

  it should "parse an empty string" in {
    val result = JSONParser.parse("\"\"")
    result should be (Right(StringToken(2, "")))
  }

  it should "parse a string with spaces" in {
    val result = JSONParser.parse("\"hello world\"")
    result should be (Right(StringToken(13, "hello world")))
  }

  it should "parse a string with numbers and special characters" in {
    val result = JSONParser.parse("\"test123!@#\"")
    result should be (Right(StringToken(12, "test123!@#")))
  }

  it should "handle escaped backslash" in {
    val result = JSONParser.parse("\"test\\\\path\"")
    result should be (Right(StringToken(12, "test\\path")))
  }

  it should "handle escaped quote" in {
    val result = JSONParser.parse("\"He said \\\"hello\\\"\"")
    result should be (Right(StringToken(19, "He said \"hello\"")))
  }

  it should "handle escaped forward slash" in {
    val result = JSONParser.parse("\"path\\/to\\/file\"")
    result should be (Right(StringToken(16, "path/to/file")))
  }

  it should "handle escaped backspace" in {
    val result = JSONParser.parse("\"test\\b\"")
    result should be (Right(StringToken(8, "test\b")))
  }

  it should "handle escaped form feed" in {
    val result = JSONParser.parse("\"test\\f\"")
    result should be (Right(StringToken(8, "test\f")))
  }

  it should "handle escaped newline" in {
    val result = JSONParser.parse("\"test\\n\"")
    result should be (Right(StringToken(8, "test\n")))
  }

  it should "handle escaped carriage return" in {
    val result = JSONParser.parse("\"test\\r\"")
    result should be (Right(StringToken(8, "test\r")))
  }

  it should "handle escaped tab" in {
    val result = JSONParser.parse("\"test\\t\"")
    result should be (Right(StringToken(8, "test\t")))
  }

  it should "handle Unicode escape sequences" in {
    val result = JSONParser.parse("\"\\u0041\\u0042\"")
    result should be (Right(StringToken(14, "AB")))
  }

  it should "handle mixed escape sequences" in {
    val result = JSONParser.parse("\"\\u0041\\n\\u0042\"")
    result should be (Right(StringToken(16, "A\nB")))
  }

  it should "handle multiple escape sequences in one string" in {
    val result = JSONParser.parse("\"\\\\\\\"\\t\\u0041\"")
    result should be (Right(StringToken(14, "\\\"\tA")))
  }

  it should "fail on unterminated strings" in {
    val result = JSONParser.parse("\"hello")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on string with literal newline" in {
    val result = JSONParser.parse("\"hello\nworld\"")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on string with literal carriage return" in {
    val result = JSONParser.parse("\"hello\rworld\"")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on invalid escape sequence" in {
    val result = JSONParser.parse("\"test\\z\"")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on incomplete Unicode escape" in {
    val result = JSONParser.parse("\"\\u004\"")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on invalid Unicode escape" in {
    val result = JSONParser.parse("\"\\uGGGG\"")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on string not starting with quote" in {
    val result = JSONParser.parse("hello\"")
    result shouldBe a [Left[?, ?]]
  }

  it should "parse string with correct skip" in {
    val result = JSONParser.parse("\"hello\"")
    result shouldBe Right(StringToken(7, "hello"))
  }
}
