package com.github.jsonscala.lib

import scala.util.matching.Regex

sealed trait ValueParserMode
object ValueParserMode {
  case object Scanning extends ValueParserMode
  case object Array    extends ValueParserMode
  case object False    extends ValueParserMode
  case object Null     extends ValueParserMode
  case object Number   extends ValueParserMode
  case object Object   extends ValueParserMode
  case object String   extends ValueParserMode
  case object True     extends ValueParserMode
  case object End      extends ValueParserMode
}

object ValueParser {
  private def getSkip(token: Token): Int = token match {
    case ArrayToken(s, _)     => s
    case FalseToken(s)        => s
    case NullToken(s)         => s
    case NumberToken(s, _, _) => s
    case ObjectToken(s, _)    => s
    case StringToken(s, _)    => s
    case TrueToken(s)         => s
  }

  def parse(s: String, delimiters: Option[Regex]): Either[String, Token] = {
    def parseRec(mode: ValueParserMode, pos: Int): Either[String, Token] =
      if (pos >= s.length || mode == ValueParserMode.End) {
        Left("Unexpected end of input")
      } else {
        val ch = s.charAt(pos)
        mode match {
          case ValueParserMode.Scanning =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1)
            } else if (ch == '[') {
              val slice = s.substring(pos)
              ArrayParser.parse(slice) match {
                case Right(token) =>
                  token match {
                    case ArrayToken(s, e) => Right(ArrayToken(s + pos, e))
                    case _                => Left("Unexpected token type from ArrayParser")
                  }
                case Left(e)      => Left(e)
              }
            } else if (ch == 'f') {
              val slice = s.substring(pos, Math.min(pos + 5, s.length))
              if (slice == "false") {
                Right(FalseToken(pos + 5))
              } else {
                Left(s"Expected 'false', actual '$slice'")
              }
            } else if (ch == 'n') {
              val slice = s.substring(pos, Math.min(pos + 4, s.length))
              if (slice == "null") {
                Right(NullToken(pos + 4))
              } else {
                Left(s"Expected 'null', actual '$slice'")
              }
            } else if (ch.isDigit || ch == '-') {
              val slice = s.substring(pos)
              NumberParser.parse(slice, delimiters) match {
                case Right(token) =>
                  token match {
                    case NumberToken(s, v, vas) => Right(NumberToken(s + pos, v, vas))
                    case _                      => Left("Unexpected token type from NumberParser")
                  }
                case Left(e)      => Left(e)
              }
            } else if (ch == '{') {
              val slice = s.substring(pos)
              ObjectParser.parse(slice) match {
                case Right(token) =>
                  token match {
                    case ObjectToken(s, m) => Right(ObjectToken(s + pos, m))
                    case _                 => Left("Unexpected token type from ObjectParser")
                  }
                case Left(e)      => Left(e)
              }
            } else if (ch == '"') {
              val slice = s.substring(pos)
              StringParser.parse(slice) match {
                case Right(token) =>
                  token match {
                    case StringToken(s, v) => Right(StringToken(s + pos, v))
                    case _                 => Left("Unexpected token type from StringParser")
                  }
                case Left(e)      => Left(e)
              }
            } else if (ch == 't') {
              val slice = s.substring(pos, Math.min(pos + 4, s.length))
              if (slice == "true") {
                Right(TrueToken(pos + 4))
              } else {
                Left(s"Expected 'true', actual '$slice'")
              }
            } else if (delimiters.exists(_.matches(ch.toString))) {
              Left("Unexpected delimiter")
            } else {
              Left(s"Unexpected character '$ch'")
            }
          case _                        => Left(s"Unexpected mode $mode")
        }
      }
    parseRec(ValueParserMode.Scanning, 0)
  }
}
