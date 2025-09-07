package com.github.joechung2008.jsonscala.lib

sealed trait StringParserMode
object StringParserMode {
  case object Scanning    extends StringParserMode
  case object Char        extends StringParserMode
  case object EscapedChar extends StringParserMode
  case object Unicode     extends StringParserMode
  case object End         extends StringParserMode
}

object StringParser {
  def parse(s: String): Either[String, Token] = {
    def parseRec(mode: StringParserMode, pos: Int, value: String): Either[String, Token] =
      if (mode == StringParserMode.End) {
        Right(StringToken(pos, value))
      } else if (pos >= s.length) {
        Left("Incomplete string")
      } else {
        val ch = s.charAt(pos)
        mode match {
          case StringParserMode.Scanning    =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1, value)
            } else if (ch == '"') {
              parseRec(StringParserMode.Char, pos + 1, value)
            } else {
              Left(s"Expected '\"', actual '$ch'")
            }
          case StringParserMode.Char        =>
            if (ch == '\\') {
              parseRec(StringParserMode.EscapedChar, pos + 1, value)
            } else if (ch == '"') {
              parseRec(StringParserMode.End, pos + 1, value)
            } else if (ch != '\n' && ch != '\r') {
              parseRec(mode, pos + 1, value + ch)
            } else {
              Left(s"Unexpected character '$ch'")
            }
          case StringParserMode.EscapedChar =>
            if (ch == '\\' || ch == '"' || ch == '/') {
              parseRec(StringParserMode.Char, pos + 1, value + ch)
            } else if (ch == 'b') {
              parseRec(StringParserMode.Char, pos + 1, value + '\b')
            } else if (ch == 'f') {
              parseRec(StringParserMode.Char, pos + 1, value + '\f')
            } else if (ch == 'n') {
              parseRec(StringParserMode.Char, pos + 1, value + '\n')
            } else if (ch == 'r') {
              parseRec(StringParserMode.Char, pos + 1, value + '\r')
            } else if (ch == 't') {
              parseRec(StringParserMode.Char, pos + 1, value + '\t')
            } else if (ch == 'u') {
              parseRec(StringParserMode.Unicode, pos + 1, value)
            } else {
              Left(s"Unexpected escape character '$ch'")
            }
          case StringParserMode.Unicode     =>
            val slice = s.substring(pos, Math.min(pos + 4, s.length))
            if (slice.length < 4) {
              Left(s"Incomplete Unicode code '$slice'")
            } else {
              try {
                val hex  = Integer.parseInt(slice, 16)
                val char = hex.toChar.toString
                parseRec(StringParserMode.Char, pos + 4, value + char)
              } catch {
                case _: NumberFormatException => Left("Invalid Unicode code")
              }
            }
          case StringParserMode.End         => parseRec(mode, pos, value)
        }
      }
    parseRec(StringParserMode.Scanning, 0, "")
  }
}
