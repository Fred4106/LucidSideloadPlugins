package com.fredplugins.pvmDebugger

import net.runelite.api.NPC

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

package object yama {
	val YAMAS_DOMAIN_REGION_ID = 6045

	class State {
		var npc: NPC = null
		var phase: YamaPhase = null
	}
}
