package com.fredplugins.kroovy.events

import net.runelite.api.NPC

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait KEvent {
}
object KEvents {
	class NpcSpawned(wrapped: NPC)
}