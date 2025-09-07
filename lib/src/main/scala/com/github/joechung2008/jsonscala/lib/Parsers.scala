package com.github.joechung2008.jsonscala.lib

import scala.util.matching.Regex

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

object JSONParser {
  private val WHITESPACE = "[ \\n\\r\\t]".r

  def parse(s: String): Either[String, Token] =
    ValueParser.parse(s, Some(WHITESPACE)) match {
      case Right(token) =>
        if (token.skip == s.length) Right(token)
        else Left(s"Unexpected characters after JSON value at position ${token.skip}")
      case Left(e)      => Left(e)
    }
}

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
