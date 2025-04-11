package com.fredplugins.kroovy.api

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

enum KOrientation {
	case None
	case West
	case North
	case East
	case South
	case NorthWest
	case NorthEast
	case SouthEast
	case SouthWest
}
object KOrientation {
	inline def fromEncoded(in: Int): KOrientation = {
		Option(in).collect {
			case 1 => West
			case 2 => North
			case 4 => East
			case 8 => South
			case 16 => NorthWest
			case 32 => NorthEast
			case 64 => SouthEast
			case 128 => SouthWest
		}.getOrElse(None)
	}
}
