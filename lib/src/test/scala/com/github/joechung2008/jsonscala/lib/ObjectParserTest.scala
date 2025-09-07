package com.github.joechung2008.jsonscala.lib

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ObjectParserTest extends AnyFlatSpec with Matchers {

  "ObjectParser" should "parse an empty object" in {
    val result = JSONParser.parse("{}")
    result should be (Right(ObjectToken(2, List())))
  }

  it should "parse an object with a single string value" in {
    val result = JSONParser.parse("{\"key\": \"value\"}")
    result should be (Right(ObjectToken(16, List(
      PairToken(14, StringToken(5, "key"), StringToken(8, "value"))
    ))))
  }

  it should "parse an object with a single number value" in {
    val result = JSONParser.parse("{\"age\": 25}")
    result should be (Right(ObjectToken(11, List(
      PairToken(9, StringToken(5, "age"), NumberToken(3, 25.0, "25"))
    ))))
  }

  it should "parse an object with multiple properties" in {
    val result = JSONParser.parse("{\"name\": \"John\", \"age\": 30}")
    result should be (Right(ObjectToken(27, List(
      PairToken(14, StringToken(6, "name"), StringToken(7, "John")),
      PairToken(9, StringToken(5, "age"), NumberToken(3, 30.0, "30"))
    ))))
  }

  it should "parse an object with boolean values" in {
    val result = JSONParser.parse("{\"active\": true, \"deleted\": false}")
    result should be (Right(ObjectToken(34, List(
      PairToken(14, StringToken(8, "active"), TrueToken(5)),
      PairToken(16, StringToken(9, "deleted"), FalseToken(6))
    ))))
  }

  it should "parse an object with null values" in {
    val result = JSONParser.parse("{\"data\": null}")
    result should be (Right(ObjectToken(14, List(
      PairToken(12, StringToken(6, "data"), NullToken(5))
    ))))
  }

  it should "parse an object with nested objects" in {
    val result = JSONParser.parse("{\"user\": {\"name\": \"John\"}}")
    result should be (Right(ObjectToken(26, List(
      PairToken(24, StringToken(6, "user"),
        ObjectToken(17, List(PairToken(14, StringToken(6, "name"), StringToken(7, "John")))))
    ))))
  }

  it should "parse an object with nested arrays" in {
    val result = JSONParser.parse("{\"numbers\": [1, 2, 3]}")
    result should be (Right(ObjectToken(22, List(
      PairToken(20, StringToken(9, "numbers"),
        ArrayToken(10, List(NumberToken(1, 1.0, "1"), NumberToken(1, 2.0, "2"), NumberToken(1, 3.0, "3"))))
    ))))
  }

  it should "handle whitespace in objects" in {
    val result = JSONParser.parse("{  \"key\"  :  \"value\"  }")
    result should be (Right(ObjectToken(23, List(
      PairToken(17, StringToken(5, "key"), StringToken(9, "value"))
    ))))
  }

  it should "parse complex nested structures" in {
    val result = JSONParser.parse("{\"data\": {\"items\": [{\"id\": 1}, {\"id\": 2}]}}")
    result shouldBe a [Right[?, ?]]
    // More detailed assertions would be complex due to nested structure
  }

  it should "fail on partial objects (without closing brace)" in {
    val result = JSONParser.parse("{\"key\": \"value\"")
    result shouldBe a [Left[?, ?]]
  }

  it should "parse empty object with correct skip" in {
    val result = JSONParser.parse("{}")
    result shouldBe Right(ObjectToken(2, List()))
  }

  it should "parse simple object with correct skip" in {
    val result = JSONParser.parse("{\"key\": \"value\"}")
    result shouldBe Right(ObjectToken(16, List(PairToken(14, StringToken(5, "key"), StringToken(8, "value")))))
  }

  it should "fail on objects with trailing comma" in {
    val result = JSONParser.parse("{\"key\": \"value\",}")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on objects without colons" in {
    val result = JSONParser.parse("{\"key\" \"value\"}")
    result shouldBe a [Left[?, ?]]
  }

  it should "fail on invalid object start" in {
    val result = JSONParser.parse("\"key\": \"value\"}")
    result shouldBe a [Left[?, ?]]
  }
}
