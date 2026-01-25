package com.fredplugins.dynamicHighlights

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import lexer.Tokens.*

import scala.compiletime.uninitialized
import scala.util.parsing.combinator.RegexParsers

object LexerTest extends App {

	TestFilterStrings.preabmle

	val tokens:List[WorkflowToken] =List(
		LitStr("bob"),
		LitInt(5),
		Identifier("bob"),
		List_Start(),
		LitTrue(), Comma(), LitFalse(), Comma(), LitStr("bob"), Comma(), LitInt(7),
		List_End(),
		StmtEnd(),
		Newline()
	)

	tokens.foreach(t => {
		println(s"${t}.prettyPrint = ${t.prettyPrint}")
	})

	tokens.foldLeft("")((a, b) => {a.appendedAll(b.prettyPrint)}).pipe(_.appendedAll("\n\"").prependedAll("\"\n")).tap(println)
}