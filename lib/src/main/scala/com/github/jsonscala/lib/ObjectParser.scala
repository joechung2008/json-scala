package com.github.jsonscala.lib

import scala.util.matching.Regex

sealed trait ObjectParserMode
object ObjectParserMode {
  case object Scanning  extends ObjectParserMode
  case object Pair      extends ObjectParserMode
  case object Delimiter extends ObjectParserMode
  case object End       extends ObjectParserMode
}

object ObjectParser {
  private val DELIMITERS = "[ \\n\\r\\t\\},]".r

  def parse(s: String): Either[String, Token] = {
    def parseRec(mode: ObjectParserMode, pos: Int, members: List[PairToken]): Either[String, Token] =
      if (mode == ObjectParserMode.End) {
        Right(ObjectToken(pos, members.reverse))
      } else if (pos >= s.length) {
        Left("Incomplete object")
      } else {
        val ch = s.charAt(pos)
        mode match {
          case ObjectParserMode.Scanning  =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1, members)
            } else if (ch == '{') {
              parseRec(ObjectParserMode.Pair, pos + 1, members)
            } else {
              Left(s"Expected '{', actual '$ch'")
            }
          case ObjectParserMode.Pair      =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1, members)
            } else if (ch == '}') {
              if (members.nonEmpty) {
                Left("Unexpected ','")
              } else {
                parseRec(ObjectParserMode.End, pos + 1, members)
              }
            } else {
              val slice = s.substring(pos)
              PairParser.parse(slice) match {
                case Right(pair) =>
                  parseRec(ObjectParserMode.Delimiter, pos + pair.skip, pair :: members)
                case Left(e)     => Left(e)
              }
            }
          case ObjectParserMode.Delimiter =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1, members)
            } else if (ch == ',') {
              parseRec(ObjectParserMode.Pair, pos + 1, members)
            } else if (ch == '}') {
              parseRec(ObjectParserMode.End, pos + 1, members)
            } else {
              Left(s"Expected ',' or '}', actual '$ch'")
            }
          case ObjectParserMode.End       => parseRec(mode, pos, members)
        }
      }
    parseRec(ObjectParserMode.Scanning, 0, Nil)
  }
}
