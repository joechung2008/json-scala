package com.github.joechung2008.jsonscala.lib

import scala.util.matching.Regex

object JSONParser {
  private val WHITESPACE = "[ \\n\\r\\t]".r

  def parse(s: String): Either[String, Token] =
    ValueParser.parse(s, Some(WHITESPACE)) match {
      case Right(token) =>
        if (token.skip == s.length) Right(token)
        else {
          // Check if remaining characters are only whitespace
          val remaining = s.substring(token.skip)
          if (remaining.forall(_.isWhitespace)) Right(token)
          else Left(s"Unexpected characters after JSON value at position ${token.skip}")
        }
      case Left(e)      => Left(e)
    }
}
