package com.fredplugins.dynamicHighlights.lexer

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

object FilterLexer extends RegexParsers {
	import Tokens.*

//	override def skipWhitespace = true
//	override val whiteSpace: Regex = """[ \t\r\f]+".r

	def WHITESPACE     : Parser[Whitespace]    = positioned { "\\\n"    ^^ (_ => Whitespace(1))     }
	def PREPROC_DEFINE : Parser[Define] = positioned { "#define" ^^ (_ => Define())  }
	def APPLY          : Parser[Apply]          = positioned { "apply"   ^^ (_ => Apply())           }
	def FALSE          : Parser[LitBool]       = positioned { "false"   ^^ (_ => LitBool(false))        }
	def TRUE           : Parser[LitBool]        = positioned { "true"    ^^ (_ => LitBool(true))         }
	def META           : Parser[Meta]           = positioned { "meta"    ^^ (_ => Meta())            }
	def RULE           : Parser[Rule]           = positioned { "rule"    ^^ (_ => Rule())            }
	def IF             : Parser[If]             = positioned { "if"      ^^ (_ => If())              }
	def OP_AND         : Parser[Op_And]         = positioned { "&&"      ^^ (_ => Op_And())          }
	def OP_OR          : Parser[Op_Or]          = positioned { "||"      ^^ (_ => Op_Or())           }
	def OP_GTEQ        : Parser[Op_Gteq]        = positioned { ">="      ^^ (_ => Op_Gteq())         }
	def OP_LTEQ        : Parser[Op_Lteq]        = positioned { "<="      ^^ (_ => Op_Lteq())         }
	def OP_EQ          : Parser[Op_Eq]          = positioned { "=="      ^^ (_ => Op_Eq())           }
	def OP_NOT         : Parser[Op_Not]         = positioned { "!"       ^^ (_ => Op_Not())          }
	def OP_GT          : Parser[Op_Gt]          = positioned { ">"       ^^ (_ => Op_Gt())           }
	def OP_LT          : Parser[Op_Lt]          = positioned { "<"       ^^ (_ => Op_Lt())           }
	def STMT_END       : Parser[StmtEnd]        = positioned { ";"       ^^ (_ => StmtEnd())         }
	def COLON          : Parser[Colon]          = positioned { ":"       ^^ (_ => Colon())           }
	def ASSIGN         : Parser[Assign]         = positioned { "="       ^^ (_ => Assign())          }
	def COMMA          : Parser[Comma]          = positioned { ","       ^^ (_ => Comma())           }
	def EXPR_START     : Parser[Expr_Start]     = positioned { "("       ^^ (_ => Expr_Start())      }
	def EXPR_END       : Parser[Expr_End]       = positioned { ")"       ^^ (_ => Expr_End())        }
	def BLOCK_START    : Parser[Block_Start]    = positioned { "{"       ^^ (_ => Block_Start())     }
	def BLOCK_END      : Parser[Block_End]      = positioned { "}"       ^^ (_ => Block_End())       }
	def LIST_START     : Parser[List_Start]     = positioned { "["       ^^ (_ => List_Start())      }
	def LIST_END       : Parser[List_End]       = positioned { "]"       ^^ (_ => List_End())        }
	def NEWLINE        : Parser[Newline]        = positioned {
		(literal("\n") | literal("\r")) ^^ (x => Newline())
	}

	val staticParsers: Parser[_ <: WorkflowToken] = Seq(
			WHITESPACE,
			APPLY,
			FALSE,
			TRUE,
			META,
			RULE,
			IF,
			OP_AND,
			OP_OR,
			OP_GTEQ,
			OP_LTEQ,
			OP_EQ,
			OP_NOT,
			OP_GT,
			OP_LT,
			STMT_END,
			COLON,
			ASSIGN,
			COMMA,
			EXPR_START,
			EXPR_END,
			BLOCK_START,
			BLOCK_END,
			LIST_START,
			LIST_END,
			NEWLINE,
	).foldLeft[Parser[WorkflowToken]](PREPROC_DEFINE)((a, b) => (a | b))


	/** Anything that is a valid Java identifier, according to
	* <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8">The Java Language Spec</a>.
	* Generally, this means a letter, followed by zero or more letters or numbers.
	*/
	def ident: Parser[Identifier] =
	positioned{
		(
		"" ~> // handle whitespace
			rep1(
				acceptIf(Character.isJavaIdentifierStart)("identifier expected but '" + _ + "' found"),
				elem("identifier part", Character.isJavaIdentifierPart(_: Char))) ^^ (_.mkString) ^^ (s => Identifier(s)))
	}

	/** An integer, without sign or with a negative sign. */
	def wholeNumber: Parser[LitInt] =
		positioned {
			("""-?\d+""".r ^^ {sn => LitInt(sn.toInt)})
		}

	/** Double quotes (`"`) enclosing a sequence of:
	*
	*  - Any character except double quotes, control characters or backslash (`\`)
	*  - A backslash followed by another backslash, a single or double quote, or one
	*    of the letters `b`, `f`, `n`, `r` or `t`
	*  - `\` followed by `u` followed by four hexadecimal digits
	*/
	def stringLiteral: Parser[LitStr] = positioned {
		("\"" + """([^"\x00-\x1F\x7F\\]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*""" + "\"").r ^^ {s =>
			Tokens.LitStr(s)
		}
	}

	def run(str: String): ParseResult[Seq[WorkflowToken]] = {
		parse(rep("" ~> (staticParsers | ident | stringLiteral | wholeNumber)), str)
	}
}