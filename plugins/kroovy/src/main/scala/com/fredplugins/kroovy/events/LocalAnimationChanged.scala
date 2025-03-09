package com.fredplugins.kroovy.events

import net.runelite.api.Player

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

	case class LocalAnimationChanged(from: Int, to: Int)(val player: Player) {

}
