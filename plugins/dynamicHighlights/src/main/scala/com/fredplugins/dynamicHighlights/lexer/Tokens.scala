package com.fredplugins.dynamicHighlights.lexer

import java.lang.reflect.Type
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.util.parsing.input.{Position, Positional}
import scala.util.parsing.combinator.*
import scala.compiletime.uninitialized
import scala.quoted.{Expr, Type}
//import scala.quoted.runtime.Expr
import scala.reflect.{TypeTest, Typeable}
import compiletime.ops.any.*


object Tokens {
	sealed trait WorkflowToken extends Positional {
		def prettyPrint: String
	}

	sealed trait Comment {
		self: WorkflowToken =>
		type PREFIX <: (String & Singleton) : ValueOf
		type SUFFIX <: (String & Singleton): ValueOf
		def wrapped: String

		override lazy val prettyPrint: String = s"${valueOf[PREFIX]}${wrapped}${valueOf[SUFFIX]}"
	}

	private transparent trait CommentImpl[PRE <: String & Singleton : ValueOf, POST <: String & Singleton : ValueOf] extends WorkflowToken with Comment {
	}

	case class LineComment(wrapped: String) extends WorkflowToken with Comment {
		assert(!wrapped.contains('\n'))
		type SUFFIX = "/n"
		type PREFIX  = "//"
	}
	case class BlockComment(wrapped: String) extends WorkflowToken with Comment {
		type PREFIX ="/*"
		type SUFFIX = "*/"
	}

	case class Identifier(str: String) extends WorkflowToken {
		override def prettyPrint: String = str
	}

	sealed trait Literal[V] {
		self: WorkflowToken =>
		def value: V
		override def prettyPrint: String = s"${value}"
	}

	sealed trait Static {
		self: WorkflowToken =>
		def strFrag: String
	}

	sealed trait StaticStr[StrFrag <: (String & Singleton) : ValueOf] {
		self: Static =>
		assert(!self.isInstanceOf[Literal[?]])
		lazy val strFrag: String = valueOf[StrFrag]
		lazy val prettyPrint: String = strFrag
	}

	case class LitInt(value: Int) extends WorkflowToken with Literal[Int] {}

	case class LitStr(value: String) extends WorkflowToken with Literal[String] {
		override lazy val prettyPrint: String = super.prettyPrint.prepended('\"').appended('\"')
	}

	case class LitTrue() extends WorkflowToken with Literal[Boolean] with Static {
		override lazy val value: Boolean = true
		override lazy val strFrag: String = value.toString
	}

	case class LitFalse() extends WorkflowToken with Literal[Boolean] with Static {
//		override val strFrag: String = valueOf[ToString[false]]
		override lazy val value: Boolean = false
		override lazy val strFrag: String = value.toString
	}

	case class If() extends WorkflowToken with Static with StaticStr["if"]
	case class Apply() extends WorkflowToken with Static with StaticStr["apply"]
	case class Rule() extends WorkflowToken with Static with StaticStr["rule"]
	case class Meta() extends WorkflowToken with Static with StaticStr["meta"]

	case class StmtEnd() extends WorkflowToken with Static with StaticStr[";"]
	case class Colon() extends WorkflowToken with Static with StaticStr[":"] {}
	case class Comma() extends WorkflowToken with Static with StaticStr[","]
	case class Assign() extends WorkflowToken with Static with StaticStr["="]
	case class  Op_Eq() extends WorkflowToken with Static with StaticStr["=="]
	case class  Op_Gt() extends WorkflowToken with Static with StaticStr[">"]
	case class  Op_Lt() extends WorkflowToken with Static with StaticStr["<"]
	case class  Op_Gteq() extends WorkflowToken with Static with StaticStr[">="]
	case class  Op_Lteq() extends WorkflowToken with Static with StaticStr["<="]
	case class  Op_And() extends WorkflowToken with Static with StaticStr["&&"]
	case class  Op_Or() extends WorkflowToken with Static with StaticStr["||"]
	case class  Op_Not() extends WorkflowToken with Static with StaticStr["!"]
	case class  Expr_Start() extends WorkflowToken with Static with StaticStr["("]
	case class  Expr_End() extends WorkflowToken with Static with StaticStr[")"]
	case class  Block_Start() extends WorkflowToken with Static with StaticStr["{"]
	case class  Block_End() extends WorkflowToken with Static with StaticStr["}"]
	case class  List_Start() extends WorkflowToken with Static with StaticStr["["]
	case class  List_End() extends WorkflowToken with Static with StaticStr["]"]

	case class Preproc_Define() extends WorkflowToken with Static with StaticStr["#define"]

	case class Whitespace1() extends WorkflowToken with Static with StaticStr["\\\n"] {}
	case class Newline() extends WorkflowToken with Static with StaticStr["\n"] {}

}