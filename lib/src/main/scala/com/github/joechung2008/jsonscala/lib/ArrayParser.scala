package com.github.joechung2008.jsonscala.lib

import scala.util.matching.Regex

sealed trait ArrayParserMode
object ArrayParserMode {
  case object Scanning  extends ArrayParserMode
  case object Element   extends ArrayParserMode
  case object Delimiter extends ArrayParserMode
  case object End       extends ArrayParserMode
}

object ArrayParser {
  private val DELIMITERS = "[ \\n\\r\\t\\],]".r

  private def getSkip(token: Token): Int = token match {
    case ArrayToken(s, _)     => s
    case FalseToken(s)        => s
    case NullToken(s)         => s
    case NumberToken(s, _, _) => s
    case ObjectToken(s, _)    => s
    case StringToken(s, _)    => s
    case TrueToken(s)         => s
  }

  def parse(s: String): Either[String, Token] = {
    def parseRec(mode: ArrayParserMode, pos: Int, elements: List[Token]): Either[String, Token] =
      if (mode == ArrayParserMode.End) {
        Right(ArrayToken(pos, elements.reverse))
      } else if (pos >= s.length) {
        Left("Incomplete array")
      } else {
        val ch = s.charAt(pos)
        mode match {
          case ArrayParserMode.Scanning  =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1, elements)
            } else if (ch == '[') {
              parseRec(ArrayParserMode.Element, pos + 1, elements)
            } else {
              Left(s"Expected '[', actual '$ch'")
            }
          case ArrayParserMode.Element   =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1, elements)
            } else if (ch == ']') {
              if (elements.nonEmpty) {
                Left("Unexpected ','")
              } else {
                parseRec(ArrayParserMode.End, pos + 1, elements)
              }
            } else {
              val slice = s.substring(pos)
              ValueParser.parse(slice, Some(DELIMITERS)) match {
                case Right(element) =>
                  parseRec(ArrayParserMode.Delimiter, pos + getSkip(element), element :: elements)
                case Left(e)        => Left(e)
              }
            }
          case ArrayParserMode.Delimiter =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1, elements)
            } else if (ch == ',') {
              parseRec(ArrayParserMode.Element, pos + 1, elements)
            } else if (ch == ']') {
              parseRec(ArrayParserMode.End, pos + 1, elements)
            } else {
              Left(s"Expected ',' or ']', actual '$ch'")
            }
          case ArrayParserMode.End       => parseRec(mode, pos, elements)
        }
      }
    parseRec(ArrayParserMode.Scanning, 0, Nil)
  }
}
