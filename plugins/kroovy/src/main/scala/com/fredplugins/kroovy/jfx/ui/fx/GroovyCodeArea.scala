package com.fredplugins.kroovy.jfx.ui.fx

import com.fredplugins.common.utils.ShimUtils

import java.io.StringReader
import java.time.Duration
import java.util
import java.util.Collections
import java.util.regex.Pattern
import groovyjarjarantlr4.v4.runtime.{CharStreams, CommonTokenStream, Token}
import javafx.application.Platform
import javafx.scene.input.{KeyCode, KeyEvent}
import org.apache.groovy.parser.antlr4.GroovyLangLexer
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.model.{PlainTextChange, StyleSpansBuilder}

object GroovyCodeArea {
//
	private val keyWords = List(7 , 8 , 9 , 10, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 37, 38, 39, 40, 41, 42, 43, 45, 46, 47, 48, 49, 50, 52, 53, 55, 59)
	private val paren = List(78, 79)
	private val bracket = List(82, 83)
	private val brace = List(80, 81)
	private val semi = List(84)
	private val string = List(1)
	private val boolean = List(58)
	private val number = List(56, 57)
	def getStyle(token: Token): Option[String] = {
		val i = token.getType
		val x = Option(token.getType match {
			case t if keyWords.contains(t) => "keyword"
			case t if paren.contains(t) => "paren"
			case t if bracket.contains(t) => "bracket"
			case t if brace.contains(t) => "brace"
			case t if semi.contains(t) => "semicolon"
			case t if string.contains(t) => "string"
			case t if boolean.contains(t) => "boolean"
			case t if number.contains(t) => "number"
			case _ => null
		})
		x
	}

	private val tokenTypeToName: Map[Int, String] = Map(
		1 -> "StringLiteral",
		2 -> "GStringBegin",
		3 -> "GStringEnd",
		4 -> "GStringPart",
		5 -> "GStringPathPart",
		6 -> "RollBackOne",
		7 -> "AS",
		8 -> "DEF",
		9 -> "IN",
		10 -> "TRAIT",
		11 -> "THREADSAFE",
		12 -> "VAR",
		13 -> "BuiltInPrimitiveType",
		14 -> "ABSTRACT",
		15 -> "ASSERT",
		16 -> "BREAK",
		17 -> "CASE",
		18 -> "CATCH",
		19 -> "CLASS",
		20 -> "CONST",
		21 -> "CONTINUE",
		22 -> "DEFAULT",
		23 -> "DO",
		24 -> "ELSE",
		25 -> "ENUM",
		26 -> "EXTENDS",
		27 -> "FINAL",
		28 -> "FINALLY",
		29 -> "FOR",
		30 -> "IF",
		31 -> "GOTO",
		32 -> "IMPLEMENTS",
		33 -> "IMPORT",
		34 -> "INSTANCEOF",
		35 -> "INTERFACE",
		36 -> "NATIVE",
		37 -> "NEW",
		38 -> "PACKAGE",
		39 -> "PRIVATE",
		40 -> "PROTECTED",
		41 -> "PUBLIC",
		42 -> "RETURN",
		43 -> "STATIC",
		44 -> "STRICTFP",
		45 -> "SUPER",
		46 -> "SWITCH",
		47 -> "SYNCHRONIZED",
		48 -> "THIS",
		49 -> "THROW",
		50 -> "THROWS",
		51 -> "TRANSIENT",
		52 -> "TRY",
		53 -> "VOID",
		54 -> "VOLATILE",
		55 -> "WHILE",
		56 -> "IntegerLiteral",
		57 -> "FloatingPointLiteral",
		58 -> "BooleanLiteral",
		59 -> "NullLiteral",
		60 -> "RANGE_INCLUSIVE",
		61 -> "RANGE_EXCLUSIVE",
		62 -> "SPREAD_DOT",
		63 -> "SAFE_DOT",
		64 -> "SAFE_CHAIN_DOT",
		65 -> "ELVIS",
		66 -> "METHOD_POINTER",
		67 -> "METHOD_REFERENCE",
		68 -> "REGEX_FIND",
		69 -> "REGEX_MATCH",
		70 -> "POWER",
		71 -> "POWER_ASSIGN",
		72 -> "SPACESHIP",
		73 -> "IDENTICAL",
		74 -> "NOT_IDENTICAL",
		75 -> "ARROW",
		76 -> "NOT_INSTANCEOF",
		77 -> "NOT_IN",
		78 -> "LPAREN",
		79 -> "RPAREN",
		80 -> "LBRACE",
		81 -> "RBRACE",
		82 -> "LBRACK",
		83 -> "RBRACK",
		84 -> "SEMI",
		85 -> "COMMA",
		86 -> "DOT",
		87 -> "ASSIGN",
		88 -> "GT",
		89 -> "LT",
		90 -> "NOT",
		91 -> "BITNOT",
		92 -> "QUESTION",
		93 -> "COLON",
		94 -> "EQUAL",
		95 -> "LE",
		96 -> "GE",
		97 -> "NOTEQUAL",
		98 -> "AND",
		99 -> "OR",
		100 -> "INC",
		101 -> "DEC",
		102 -> "ADD",
		103 -> "SUB",
		104 -> "MUL",
		105 -> "DIV",
		106 -> "BITAND",
		107 -> "BITOR",
		108 -> "XOR",
		109 -> "MOD",
		110 -> "ADD_ASSIGN",
		111 -> "SUB_ASSIGN",
		112 -> "MUL_ASSIGN",
		113 -> "DIV_ASSIGN",
		114 -> "AND_ASSIGN",
		115 -> "OR_ASSIGN",
		116 -> "XOR_ASSIGN",
		117 -> "MOD_ASSIGN",
		118 -> "LSHIFT_ASSIGN",
		119 -> "RSHIFT_ASSIGN",
		120 -> "URSHIFT_ASSIGN",
		121 -> "ELVIS_ASSIGN",
		122 -> "CapitalizedIdentifier",
		123 -> "Identifier",
		124 -> "AT",
		125 -> "ELLIPSIS",
		126 -> "WS",
		127 -> "NL",
		128 -> "SH_COMMENT",
		129 -> "UNEXPECTED_CHAR")
}

class GroovyCodeArea(text: String) extends CodeArea(text) with ShimUtils.Logging {
	setUseInitialStyleForInsertion(true)

	val textChangeSubscription = plainTextChanges.subscribe((tc: PlainTextChange) => {
		val removed = tc.getRemoved
		val inserted = tc.getInserted
		val position = tc.getPosition
		if (!removed.isEmpty && inserted.isEmpty) {
			// deletion
		}
		else if (!inserted.isEmpty && removed.isEmpty) {
			// insertion
		}
		else {
			// replacement
		}
	})

	val recomputeCST = multiPlainChanges.successionEnds(Duration.ofMillis(500)).subscribe((change: util.List[PlainTextChange]) => {
		this.computeHighlighting()
	})

	// auto-indent: insert previous line's indents on enter
	val whiteSpace: Pattern = Pattern.compile("^\\s+")
	addEventHandler(KeyEvent.KEY_PRESSED, (KE: KeyEvent) => {
		if (KE.getCode eq KeyCode.ENTER) {
			val caretPosition = getCaretPosition
			val currentParagraph = getCurrentParagraph
			val m0 = whiteSpace.matcher(getParagraph(currentParagraph - 1).getSegments.get(0))
			if (m0.find) Platform.runLater(() => insertText(caretPosition, m0.group))
		}
	})
	computeHighlighting()

	private def computeHighlighting(): Unit = {
		val t = getText()
		val charStream = CharStreams.fromReader(new StringReader(t))
		val lexer: GroovyLangLexer = new GroovyLangLexer(charStream)
		val tokensStream: CommonTokenStream = new CommonTokenStream(lexer)
		tokensStream.fill()
		import scala.jdk.CollectionConverters._
		val tokens: Seq[(Token, String)] = tokensStream.getTokens.asScala.toList.map(token => (token, GroovyCodeArea.getStyle(token))).filter(_._2.isDefined).map(f => (f._1, f._2.get))

		val spansBuilder = new StyleSpansBuilder[util.Collection[String]]
		spansBuilder.add(Collections.emptyList, tokens.headOption.map(_._1.getStartIndex).getOrElse(t.length))
		for(i <- tokens.indices) {
			val last: Token = if(i > 0) tokens(i-1)._1 else null
			val (t, style) = tokens(i)
			if(last != null) spansBuilder.add(Collections.emptyList, t.getStartIndex - (last.getStopIndex+1))
			spansBuilder.add(Collections.singleton(style), (t.getStopIndex+1)-t.getStartIndex)
		}
		spansBuilder.add(Collections.emptyList, tokens.lastOption.map(f => t.length - (f._1.getStopIndex+1)).getOrElse(0))
		Platform.runLater(() => setStyleSpans(0, spansBuilder.create()))
	}
}
