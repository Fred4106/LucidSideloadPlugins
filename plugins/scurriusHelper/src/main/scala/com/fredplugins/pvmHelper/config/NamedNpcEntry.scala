package com.fredplugins.pvmHelper.config

import com.google.gson.Gson
import com.google.inject.Inject
import net.runelite.client.config.{ConfigSerializer, Serializer}
import scala.util.chaining.*
import java.lang.reflect.ParameterizedType
case class NamedNpcEntry(npcName: String, ids: Int *) {
}

object NamedNpcEntry extends ConfigParser {
//	private val regNameRemainder: Regex = raw"([a-zA-Z][a-zA-Z0-9]*) +([^;]+);".r

	val parser: Parser[NamedNpcEntry] = ((nameParser ~ idArrayParser)) ^^ {case names ~ ids => NamedNpcEntry(names, ids *) }


//	private val numberRegex: Regex = raw" *(-[1-9]+[0-9])".r
//	private val numberAndTrailingRegex: Regex = raw"(-?[1-9][0-9]*|[0-9]+), +([^\n]*)".r
//	private val : Regex = raw"([^\n]*)".r

	val empty: NamedNpcEntry = NamedNpcEntry("unknown", -1)

	def encode(str: String): Option[NamedNpcEntry] = {
		parse(phrase(parser), str) match {
			case Success(n, _) => Some(n)
			case NoSuccess(msg, _) => None
		}
	}

	def decode(namedNpcEntry: NamedNpcEntry): String = namedNpcEntry match {
		case NamedNpcEntry(npcName, ids *) => s"${npcName} ${ids.mkString(",")}"
	}

	def roundTrip(s: String): Option[NamedNpcEntry] = encode(s).flatMap(e => Option.when(decode(e) == s) {
		e
	})
}