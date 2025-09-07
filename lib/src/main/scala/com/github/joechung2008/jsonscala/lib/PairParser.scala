package com.github.joechung2008.jsonscala.lib

import scala.util.matching.Regex

sealed trait PairParserMode
object PairParserMode {
  case object Scanning  extends PairParserMode
  case object Key       extends PairParserMode
  case object Delimiter extends PairParserMode
  case object Value     extends PairParserMode
  case object End       extends PairParserMode
}

object PairParser {
  private val DELIMITERS = "[ \\n\\r\\t\\},]".r

  private def getSkip(token: Token): Int = token match {
    case ArrayToken(s, _)     => s
    case FalseToken(s)        => s
    case NullToken(s)         => s
    case NumberToken(s, _, _) => s
    case ObjectToken(s, _)    => s
    case StringToken(s, _)    => s
    case TrueToken(s)         => s
  }

  def parse(s: String): Either[String, PairToken] = {
    def parseRec(mode: PairParserMode, pos: Int, key: Token, value: Token): Either[String, PairToken] =
      if (pos >= s.length || mode == PairParserMode.End) {
        Right(PairToken(pos, key, value))
      } else {
        val ch = s.charAt(pos)
        mode match {
          case PairParserMode.Scanning  =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1, key, value)
            } else {
              parseRec(PairParserMode.Key, pos, key, value)
            }
          case PairParserMode.Key       =>
            val slice = s.substring(pos)
            StringParser.parse(slice) match {
              case Right(k) =>
                parseRec(PairParserMode.Delimiter, pos + getSkip(k), k, value)
              case Left(e)  => Left(e)
            }
          case PairParserMode.Delimiter =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1, key, value)
            } else if (ch == ':') {
              parseRec(PairParserMode.Value, pos + 1, key, value)
            } else {
              Left(s"Expected ':', actual '$ch'")
            }
          case PairParserMode.Value     =>
            val slice = s.substring(pos)
            ValueParser.parse(slice, Some(DELIMITERS)) match {
              case Right(v) =>
                parseRec(PairParserMode.End, pos + getSkip(v), key, v)
              case Left(e)  => Left(e)
            }
          case PairParserMode.End       => parseRec(mode, pos, key, value)
        }
      }
    parseRec(PairParserMode.Scanning, 0, null, null)
  }
}
