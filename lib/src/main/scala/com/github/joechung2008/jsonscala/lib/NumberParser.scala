package com.github.joechung2008.jsonscala.lib

import scala.util.matching.Regex

sealed trait NumberParserMode
object NumberParserMode {
  case object Scanning            extends NumberParserMode
  case object Characteristic      extends NumberParserMode
  case object CharacteristicDigit extends NumberParserMode
  case object DecimalPoint        extends NumberParserMode
  case object Mantissa            extends NumberParserMode
  case object Exponent            extends NumberParserMode
  case object ExponentSign        extends NumberParserMode
  case object ExponentFirstDigit  extends NumberParserMode
  case object ExponentDigits      extends NumberParserMode
  case object End                 extends NumberParserMode
}

object NumberParser {
  def parse(s: String, delimiters: Option[Regex]): Either[String, Token] = {
    def parseRec(mode: NumberParserMode, pos: Int, valueAsString: String): Either[String, Token] =
      if (pos >= s.length || mode == NumberParserMode.End) {
        mode match {
          case NumberParserMode.Characteristic | NumberParserMode.ExponentSign | NumberParserMode.ExponentFirstDigit =>
            Left(s"Incomplete expression, mode $mode")
          case _                                                                                                     =>
            try {
              val value = valueAsString.toDouble
              Right(NumberToken(pos, value, valueAsString))
            } catch {
              case _: NumberFormatException => Left("Invalid number format")
            }
        }
      } else {
        val ch = s.charAt(pos)
        mode match {
          case NumberParserMode.Scanning            =>
            if (ch.isWhitespace) {
              parseRec(mode, pos + 1, valueAsString)
            } else if (ch == '-') {
              parseRec(NumberParserMode.Characteristic, pos + 1, valueAsString + ch)
            } else {
              parseRec(NumberParserMode.Characteristic, pos, valueAsString)
            }
          case NumberParserMode.Characteristic      =>
            if (ch == '0') {
              parseRec(NumberParserMode.DecimalPoint, pos + 1, valueAsString + ch)
            } else if (ch.isDigit && ch != '0') {
              parseRec(NumberParserMode.CharacteristicDigit, pos + 1, valueAsString + ch)
            } else {
              Left(s"Expected digit, actual '$ch'")
            }
          case NumberParserMode.CharacteristicDigit =>
            if (ch.isDigit) {
              parseRec(mode, pos + 1, valueAsString + ch)
            } else if (delimiters.exists(_.matches(ch.toString))) {
              parseRec(NumberParserMode.End, pos, valueAsString)
            } else {
              parseRec(NumberParserMode.DecimalPoint, pos, valueAsString)
            }
          case NumberParserMode.DecimalPoint        =>
            if (ch == '.') {
              parseRec(NumberParserMode.Mantissa, pos + 1, valueAsString + ch)
            } else {
              parseRec(NumberParserMode.Exponent, pos, valueAsString)
            }
          case NumberParserMode.Mantissa            =>
            if (ch.isDigit) {
              parseRec(mode, pos + 1, valueAsString + ch)
            } else {
              parseRec(NumberParserMode.Exponent, pos, valueAsString)
            }
          case NumberParserMode.Exponent            =>
            if (ch == 'e' || ch == 'E') {
              parseRec(NumberParserMode.ExponentSign, pos + 1, valueAsString + "e")
            } else if (delimiters.exists(_.matches(ch.toString))) {
              parseRec(NumberParserMode.End, pos, valueAsString)
            } else {
              Left(s"Unexpected character '$ch'")
            }
          case NumberParserMode.ExponentSign        =>
            if (ch == '-' || ch == '+') {
              parseRec(NumberParserMode.ExponentFirstDigit, pos + 1, valueAsString + ch)
            } else {
              parseRec(NumberParserMode.ExponentFirstDigit, pos, valueAsString)
            }
          case NumberParserMode.ExponentFirstDigit  =>
            if (ch.isDigit) {
              parseRec(NumberParserMode.ExponentDigits, pos + 1, valueAsString + ch)
            } else {
              Left(s"Expected digit, actual '$ch'")
            }
          case NumberParserMode.ExponentDigits      =>
            if (ch.isDigit) {
              parseRec(mode, pos + 1, valueAsString + ch)
            } else if (delimiters.exists(_.matches(ch.toString))) {
              parseRec(NumberParserMode.End, pos, valueAsString)
            } else {
              Left(s"Expected digit, actual '$ch'")
            }
          case NumberParserMode.End                 => parseRec(mode, pos, valueAsString)
        }
      }
    parseRec(NumberParserMode.Scanning, 0, "")
  }
}
