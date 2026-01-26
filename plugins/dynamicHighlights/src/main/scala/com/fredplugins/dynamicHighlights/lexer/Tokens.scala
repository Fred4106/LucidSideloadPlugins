package com.fredplugins.dynamicHighlights.lexer

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.util.parsing.input.{Position, Positional}
import scala.util.parsing.combinator.*
import scala.compiletime.uninitialized
import compiletime.ops.any.*

object Tokens {
	sealed trait WorkflowToken(val tpe: TokenType) extends Positional {}

	case class LineComment(comment: String) extends WorkflowToken(TokenTypes.COMMENT) {
		assert(comment.linesIterator.toList.size == 1)
	}
	case class BlockComment(comment: String) extends WorkflowToken(TokenTypes.COMMENT)
	case class Identifier(ident: String) extends WorkflowToken(TokenTypes.IDENTIFIER)
	case class LitInt(value: Int) extends WorkflowToken(TokenTypes.LITERAL_INT)
	case class LitStr(value: String) extends WorkflowToken(TokenTypes.LITERAL_STRING)
	case class LitBool(value: Boolean) extends WorkflowToken(TokenTypes.LITERAL_BOOL)

	case class If() extends WorkflowToken(TokenTypes.IF)
	case class Apply() extends WorkflowToken(TokenTypes.APPLY)
	case class Rule() extends WorkflowToken(TokenTypes.RULE)
	case class Meta() extends WorkflowToken(TokenTypes.META)

	case class StmtEnd() extends WorkflowToken(TokenTypes.STMT_END)
	case class Colon() extends WorkflowToken(TokenTypes.COLON)
	case class Comma() extends WorkflowToken(TokenTypes.COMMA)
	case class Assign() extends WorkflowToken(TokenTypes.ASSIGN)
	case class  Op_Eq() extends WorkflowToken(TokenTypes.OP_EQ)
	case class  Op_Gt() extends WorkflowToken(TokenTypes.OP_GT)
	case class  Op_Lt() extends WorkflowToken(TokenTypes.OP_LT)
	case class  Op_Gteq() extends WorkflowToken(TokenTypes.OP_GTEQ)
	case class  Op_Lteq() extends WorkflowToken(TokenTypes.OP_LTEQ)
	case class  Op_And() extends WorkflowToken(TokenTypes.OP_AND)
	case class  Op_Or() extends WorkflowToken(TokenTypes.OP_OR)
	case class  Op_Not() extends WorkflowToken(TokenTypes.OP_NOT)
	case class  Expr_Start() extends WorkflowToken(TokenTypes.EXPR_START)
	case class  Expr_End() extends WorkflowToken(TokenTypes.EXPR_END)
	case class  Block_Start() extends WorkflowToken(TokenTypes.BLOCK_START)
	case class  Block_End() extends WorkflowToken(TokenTypes.BLOCK_END)
	case class  List_Start() extends WorkflowToken(TokenTypes.LIST_START)
	case class  List_End() extends WorkflowToken(TokenTypes.LIST_END)
	case class Define() extends WorkflowToken (TokenTypes.DEFINE)
	case class Whitespace(count: Int) extends WorkflowToken(TokenTypes.SPACE)
	case class Newline() extends WorkflowToken(TokenTypes.NEWLINE)

}