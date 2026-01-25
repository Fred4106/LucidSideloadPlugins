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

	override def skipWhitespace = true
	override val whiteSpace: Regex = "[\t\r\f]+".r

	def WHITESPACE     : Parser[Whitespace1]    = positioned { "\\\n"    ^^ (_ => Whitespace1())     }
	def PREPROC_DEFINE : Parser[Preproc_Define] = positioned { "#define" ^^ (_ => Preproc_Define())  }
	def APPLY          : Parser[Apply]          = positioned { "apply"   ^^ (_ => Apply())           }
	def FALSE          : Parser[LitFalse]       = positioned { "false"   ^^ (_ => LitFalse())        }
	def TRUE           : Parser[LitTrue]        = positioned { "true"    ^^ (_ => LitTrue())         }
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
}