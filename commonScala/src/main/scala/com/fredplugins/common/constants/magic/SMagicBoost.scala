package com.fredplugins.common.constants.magic

import net.runelite.api.{Client, GameState}
import net.runelite.api.events.VarbitChanged
import net.runelite.api.gameval.VarbitID.{ARCEUUS_DEATH_CHARGE_ACTIVE, ARCEUUS_DEATH_CHARGE_COOLDOWN}
import net.runelite.api.gameval.VarbitID.{ARCEUUS_SHADOW_VEIL_ACTIVE, ARCEUUS_SHADOW_VEIL_COOLDOWN}
import net.runelite.api.gameval.VarbitID.{ARCEUUS_RESURRECTION_ACTIVE, ARCEUUS_RESURRECTION_COOLDOWN}
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait SMagicBoost(val activeVarbit: Int, val cooldownVarbit: Int) extends enumeratum.EnumEntry {}

object SMagicBoost extends enumeratum.Enum[SMagicBoost] {
	case object DeathCharge extends SMagicBoost(ARCEUUS_DEATH_CHARGE_ACTIVE, ARCEUUS_DEATH_CHARGE_COOLDOWN) {}
	case object ShadownVeil extends SMagicBoost(ARCEUUS_SHADOW_VEIL_ACTIVE, ARCEUUS_SHADOW_VEIL_COOLDOWN) {}
	case object SummonThrall extends SMagicBoost(ARCEUUS_RESURRECTION_ACTIVE, ARCEUUS_RESURRECTION_COOLDOWN) {}

	override def values: IndexedSeq[SMagicBoost] = findValues
//	def effectActiveVarbits: IndexedSeq[Int] = values.map(_.activeVarbit)
//	def cooldownVarbits: IndexedSeq[Int] = values.map(_.cooldownVarbit)
}