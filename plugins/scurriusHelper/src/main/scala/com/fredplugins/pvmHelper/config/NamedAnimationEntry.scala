package com.fredplugins.pvmHelper.config

import com.google.gson.Gson
import com.google.inject.Inject
import net.runelite.client.config.{ConfigSerializer, Serializer}

import java.lang.reflect.ParameterizedType
import scala.util.matching.Regex

case class NamedAnimationEntry(npcName: String, animationId: Int, animationName: String) {}

object NamedAnimationEntry extends ConfigParser {
//	private val date: Regex = raw"([a-zA-Z][a-zA-Z0-9]*) +(-?[1-9][0-9]*|[0-9]+) +([a-zA-Z][a-zA-Z0-9]*) *".r
	private val entryParser: Parser[NamedAnimationEntry] = (nameParser ~ idParser ~ nameParser).map((in) => NamedAnimationEntry(in._1._1, in._1._2, in._2))

	val empty: NamedAnimationEntry = NamedAnimationEntry("unknown", -1, "unknown")

	def encode(str: String): Option[NamedAnimationEntry] = {
		parse(phrase(entryParser), str) match {
			case Success(result, next) => Option(result)
			case NoSuccess(partial, input) =>  None
		}
	}

	def decode(namedAnimationEntry: NamedAnimationEntry): String = namedAnimationEntry match {
		case NamedAnimationEntry(npcName, animationId, animationName) => s"${npcName} ${animationId} ${animationName}"
	}

	def roundTrip(s: String): Option[NamedAnimationEntry] = encode(s).flatMap(e => Option.when(decode(e) == s) {
		e
	})
}