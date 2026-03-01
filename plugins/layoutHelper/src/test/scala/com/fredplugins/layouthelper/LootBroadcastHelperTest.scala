package com.fredplugins.layouthelper

import fastparse.Parsed

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object LootBroadcastHelperTest extends App {
	object Js {
		sealed trait Val {}
		case class Obj(value: (String, Val)*) extends Val
		case class Arr(value: Val*) extends Val

		case class Str(value: String) extends Val
		case class Num(value: Double) extends Val
		case object False extends Val {}
		case object True extends Val {}
		case object Null extends Val {}
	}

	object JsParser {
		import fastparse._, NoWhitespace._

		def stringChars(c: Char) = c != '\"' && c != '\\'

		def space[$: P] = P(CharsWhileIn(" \r\n", 0))
		def digits[$: P] = P(CharsWhileIn("0-9"))
		def exponent[$: P] = P(CharIn("eE") ~ CharIn("+\\-").? ~ digits)
		def fractional[$: P] = P("." ~ digits)
		def integral[$: P] = P("0" | CharIn("1-9") ~ digits.?)

		def number[$: P] = P(CharIn("+\\-").? ~ integral ~ fractional.? ~ exponent.?).!.map(
			x => Js.Num(x.toDouble)
		)

		def `null`[$: P] = P("null").map(_ => Js.Null)
		def `false`[$: P] = P("false").map(_ => Js.False)
		def `true`[$: P] = P("true").map(_ => Js.True)

		def hexDigit[$: P] = P(CharIn("0-9a-fA-F"))
		def unicodeEscape[$: P] = P("u" ~ hexDigit ~ hexDigit ~ hexDigit ~ hexDigit)
		def escape[$: P] = P("\\" ~ (CharIn("\"/\\\\bfnrt") | unicodeEscape))

		def strChars[$: P] = P(CharsWhile(stringChars))
		def string[$: P] =
			P(space ~ "\"" ~/ (strChars | escape).rep.! ~ "\"").map(Js.Str.apply)

		def array[$: P] =
			P("[" ~/ jsonExpr.rep(sep = ","./) ~ space ~ "]").map(Js.Arr(_: _*))

		def pair[$: P] = P(string.map(_.value) ~/ ":" ~/ jsonExpr)

		def obj[$: P] =
			P("{" ~/ pair.rep(sep = ","./) ~ space ~ "}").map(Js.Obj(_: _*))

		def jsonExpr[$: P]: P[Js.Val] = P(
			space ~ (obj | array | string | `true` | `false` | `null` | number) ~ space
		)
	}

	val jsonStr =
		"""
			|[
			|  {
			|    "id": 1,
			|    "name": "Fishing Guild",
			|    "x": 323,
			|    "y": 152,
			|    "width": 32,
			|    "height": 31,
			|    "spriteEnabled": -19730,
			|    "spriteHover": -19731,
			|    "spriteDisabled": -19730,
			|    "hotkey": {
			|      "x": 356,
			|      "y": 159
			|    }
			|  },
			|  {
			|    "id": 2,
			|    "name": "Mining Guild",
			|    "x": 412,
			|    "y": 157,
			|    "width": 31,
			|    "height": 31,
			|    "spriteEnabled": -19740,
			|    "spriteHover": -19741,
			|    "spriteDisabled": -19740,
			|    "hotkey": {
			|      "x": 444,
			|      "y": 163
			|    }
			|  },
			|  {
			|    "id": 3,
			|    "name": "Crafting Guild",
			|    "x": 383,
			|    "y": 188,
			|    "width": 31,
			|    "height": 31,
			|    "spriteEnabled": -19750,
			|    "spriteHover": -19751,
			|    "spriteDisabled": -19750,
			|    "hotkey": {
			|      "x": 412,
			|      "y": 196
			|    }
			|  },
			|  {
			|    "id": 4,
			|    "name": "Cooking Guild",
			|    "x": 439,
			|    "y": 128,
			|    "width": 27,
			|    "height": 29,
			|    "spriteEnabled": -19760,
			|    "spriteHover": -19761,
			|    "spriteDisabled": -19760,
			|    "hotkey": {
			|      "x": 467,
			|      "y": 133
			|    }
			|  },
			|  {
			|    "id": 5,
			|    "name": "Woodcutting Guild",
			|    "x": 122,
			|    "y": 134,
			|    "width": 26,
			|    "height": 30,
			|    "spriteEnabled": -19710,
			|    "spriteHover": -19711,
			|    "spriteDisabled": -19712,
			|    "hotkey": {
			|      "x": 149,
			|      "y": 139
			|    }
			|  },
			|  {
			|    "id": 6,
			|    "name": "Farming Guild",
			|    "x": 46,
			|    "y": 80,
			|    "width": 33,
			|    "height": 31,
			|    "spriteEnabled": -19720,
			|    "spriteHover": -19721,
			|    "spriteDisabled": -19722,
			|    "hotkey": {
			|      "x": 80,
			|      "y": 87
			|    }
			|  }
			|]""".stripMargin
	val Parsed.Success(value, idx) = fastparse.parse(jsonStr, JsParser.jsonExpr(using _))
	println(value -> idx)

	val data = List(
		"Your Brutus kill count is: <col=ff0000>138</col>.",
		"Fight duration: <col=ff0000>0:18</col>. Personal best: 0:02",
		"<col=005f00>Iron4106 received a drop: 29 x Air rune</col> <col=106f10>(Brutus)</col>",
	).foreach(m => {
		println(LootBroadcastHelper.parse(m))
	})
//	println(LootBroadcastHelper.parse("<col=ef1020>You're assigned to kill </col>monkeys<col=ef1020>; only </col>20<col=ef1020> more to go."))
//	println(LootBroadcastHelper.parse("<col=005f00>Iron4106 received a drop: 29 x Air rune</col> <col=106f10>(Brutus)</col>"))
//	println(LootBroadcastHelper.parse("<col=005f00>Iron4106 received a drop: Cowhide</col> <col=106f10>(Brutus)</col>"))
}
