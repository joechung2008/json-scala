package com.github.joechung2008.jsonscala.lib

sealed trait Token {
  val skip: Int
}

case class ArrayToken(skip: Int, elements: List[Token])                 extends Token
case class FalseToken(skip: Int)                                        extends Token
case class NullToken(skip: Int)                                         extends Token
case class NumberToken(skip: Int, value: Double, valueAsString: String) extends Token
case class ObjectToken(skip: Int, members: List[PairToken])             extends Token
case class StringToken(skip: Int, value: String)                        extends Token
case class TrueToken(skip: Int)                                         extends Token

case class PairToken(skip: Int, key: Token, value: Token)
