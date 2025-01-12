package com.fredplugins.pvmHelper.config
import scala.util.matching.Regex
import scala.util.parsing.combinator.*
import scala.util.parsing.combinator.Parsers
import scala.util.chaining.*
abstract class ConfigParser extends RegexParsers {
	override val whiteSpace: Regex = raw"[ 	]+".r

	val nameParser: Parser[String] = raw"[a-zA-Z][a-zA-Z0-9]*".r
	val idParser:	 Parser[Int] = raw"(-[1-9][0-9]*|[0-9]+)".r ^^ {_.toInt}

	val commaParser: Parser[Unit] = (raw",".r ^^^ {()})
	val idArrayParser: Parser[Seq[Int]] = rep1sep(idParser, commaParser)

	val lineEndParser: Parser[Unit] = raw"\n".r ^^^ {()}

//	def word: Parser[String] = raw"[a-z]+".r.flatMap{_.toInt}

	def parseAll[X](parser: Parser[X])(instr: String): ParseResult[X] = {
			parse(phrase(parser), instr)
	}

}