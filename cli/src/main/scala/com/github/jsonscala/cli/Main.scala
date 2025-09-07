package com.github.jsonscala.cli

import com.github.jsonscala.lib._
import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {
    val input = Source.stdin.mkString
    JSONParser.parse(input) match {
      case Right(token) => println(prettyPrint(token))
      case Left(error)  => println(error)
    }
  }

  def prettyPrint(token: Token, indent: Int = 0): String = {
    val spaces = "  " * indent
    token match {
      case ArrayToken(_, elements)          =>
        if (elements.isEmpty) "[]"
        else s"[\n${elements.map(t => s"$spaces  ${prettyPrint(t, indent + 1)}").mkString(",\n")}\n$spaces]"
      case ObjectToken(_, members)          =>
        if (members.isEmpty) "{}"
        else
          s"{\n${members.map(p => s"$spaces  ${prettyPrint(p.key, indent + 1)}: ${prettyPrint(p.value, indent + 1)}").mkString(",\n")}\n$spaces}"
      case StringToken(_, value)            => s""""$value""""
      case NumberToken(_, _, valueAsString) => valueAsString
      case TrueToken(_)                     => "true"
      case FalseToken(_)                    => "false"
      case NullToken(_)                     => "null"
    }
  }
}
