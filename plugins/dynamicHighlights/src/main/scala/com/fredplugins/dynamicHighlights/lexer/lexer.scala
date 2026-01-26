package com.fredplugins.dynamicHighlights
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.util.parsing.input.Positional

package object lexer {
	sealed trait TknCategory extends enumeratum.EnumEntry {}

	sealed trait TokenType extends enumeratum.EnumEntry {
		def group: TknCategory
	}

	object TknCategorys extends enumeratum.Enum[TknCategory] {
		case object Formatting extends TknCategory {
			trait SubToken {
				self: TokenType =>
				override def group: TknCategory = Formatting
			}
		}

		case object Other extends TknCategory {
			trait SubToken {
				self: TokenType =>
				override def group: TknCategory = Other
			}
		}

		case object Keyword extends TknCategory {
			trait SubToken[STR <: String & Singleton : ValueOf] {
				self: TokenType =>
				def strFrag: String = valueOf[STR]
				override def group: TknCategory = Keyword
			}
		}
		case object Operators extends TknCategory {
			trait SubToken[STR <: String & Singleton : ValueOf] {
				self: TokenType =>
				def strFrag: String = valueOf[STR]
				override def group: TknCategory = Operators
			}
		}
		case object Punctuation extends TknCategory {
			trait SubToken[STR <: String & Singleton : ValueOf] {
				self: TokenType =>
				def strFrag: String = valueOf[STR]
				override def group: TknCategory = Punctuation
			}
		}
		case object Literal extends TknCategory {
			trait IntSubToken {
				self: TokenType =>
				type ET = Int
				override def group :TknCategory = Literal
			}
			trait BooleanSubToken {
				self: TokenType =>
				type ET = Boolean
				override def group: TknCategory = Literal
			}
			trait StringSubToken {
				self: TokenType =>
				type ET = String
				override def group: TknCategory = Literal
			}
		}

		override def values: IndexedSeq[TknCategory] = findValues
	}

	object TokenTypes extends enumeratum.Enum[TokenType] {
		import TknCategorys.{Other, Literal, Operators, Punctuation,Keyword,Formatting}
		case object SPACE extends TokenType with Formatting.SubToken
		case object NEWLINE extends TokenType with Formatting.SubToken

		case object DEFINE extends TokenType with Keyword.SubToken["#define"]
		case object IF extends TokenType  with Keyword.SubToken["if"]
		case object APPLY extends TokenType with Keyword.SubToken["apply"]
		case object RULE extends TokenType with Keyword.SubToken["rule"]
		case object META extends TokenType with Keyword.SubToken["meta"]

		case object OP_EQ extends TokenType with Operators.SubToken["=="]
		case object OP_GT extends TokenType with Operators.SubToken[">"]
		case object OP_LT extends TokenType with Operators.SubToken["<"]
		case object OP_GTEQ extends TokenType with Operators.SubToken[">="]
		case object OP_LTEQ extends TokenType with Operators.SubToken["<="]
		case object OP_AND extends TokenType with Operators.SubToken["&&"]
		case object OP_OR extends TokenType with Operators.SubToken["||"]
		case object OP_NOT extends TokenType with Operators.SubToken["!"]

		case object COLON extends TokenType with Punctuation.SubToken[":"]
		case object COMMA extends TokenType with Punctuation.SubToken[","]
		case object ASSIGN extends TokenType with Punctuation.SubToken["="]
		case object EXPR_START extends TokenType with Punctuation.SubToken["("]
		case object EXPR_END extends TokenType with Punctuation.SubToken[")"]
		case object BLOCK_START extends TokenType with Punctuation.SubToken["{"]
		case object BLOCK_END extends TokenType with Punctuation.SubToken["}"]
		case object LIST_START extends TokenType with Punctuation.SubToken["["]
		case object LIST_END extends TokenType with Punctuation.SubToken["]"]
		case object STMT_END extends TokenType with Punctuation.SubToken[";"]

		case object IDENTIFIER extends TokenType with Other.SubToken
		case object COMMENT extends TokenType with Other.SubToken

		case object LITERAL_BOOL extends TokenType with Literal.BooleanSubToken
		case object LITERAL_INT extends TokenType with Literal.IntSubToken
		case object LITERAL_STRING extends TokenType with Literal.StringSubToken

		override def values: IndexedSeq[TokenType] = findValues

		def getTokens(category: TknCategory): IndexedSeq[TokenType] = {
			values.filter(t => t.group == category)
		}
	}
}
