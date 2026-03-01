package com.fredplugins.layouthelper

import com.fredplugins.common.utils.ShimUtils
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.util.matching.Regex
import scala.util.parsing.input.Positional

object LootBroadcastHelper {
	//<col=005f00>Iron4106 received a drop: 29 x Air rune</col> <col=106f10>(Brutus)</col>
	//<col=005f00>Iron4106 received a drop: 29 x Air rune</col> <col=106f10>(Brutus)</col>
//
//	sealed trait Token {}
//	case class Str(s: String) extends Token
//	case class ColorTag(color: String, frag: String) extends Token

	case class LootBroadcastMessage(name: String, qty: Int, item: String, boss: String)
	case class KillCountBroadcastMessage(boss: String, qty: Int)
	case class FightDurationBroadcastMessage(boss: String, qty: Int)

	private val TAG_REGEXP = raw"<[^>]*>".r
	def removeTags(str: String): String = TAG_REGEXP.replaceAllIn(str, "")

	def parse(s: String): LootBroadcastMessage = {
//		val r = """(?:<col=)([0-9a-fA-F]*)(?:>)([^<]*)(?:</col>|\Z)""".r
//
//		val cleaned = r.findAllMatchIn(s).map(m => {
//			(m.start, m.end, m.group(1), m.group(2))
//		}).toList.sliding(2).flatMap{
//			case a ::b :: Nil => List(ColorTag(a._3, a._4), Str(s.substring(a._2, b._1)), ColorTag(b._3, b._4))
//			case a :: Nil => List(ColorTag(a._3, a._4), Str(s.substring(a._2)))
//		}.collect {
//			case t@Str(s) if s.nonEmpty => s
//			case t@ColorTag(c, s) if s.nonEmpty => s
//		}.toList.foldLeft(Seq.empty[String])((a, b) => {
//			if(a.isEmpty || !a.last.equals(b)) a.appended(b)
//			else a
//		}).mkString("")
		val cleaned = removeTags(s)

		val toRet = if(cleaned.contains(" received a drop: ")) {
			val (user, suffix) = cleaned.split(" received a drop: ").pipe(x => x(0)->x(1))
//			val r3 = """ \(([^)]+)\)""".r
			val bossCharStart = suffix.lastIndexOf('(')
			val bossCharEnd = suffix.lastIndexOf(')')
			val boss = suffix.substring(bossCharStart+1, bossCharEnd)

			val r2 = """(\d+) x (.+)""".r
			suffix.substring(0, bossCharStart-1) match {
				case x@r2(q, item) => Some(LootBroadcastMessage(user, q.toInt, item, boss))
				case x => {
					Some(LootBroadcastMessage(user, 1, x, boss))
//					Option.empty[LootBroadcastMessage]
				}
			}
		} else {
			Option.empty[LootBroadcastMessage]
		}
		toRet.orNull
	}
}
