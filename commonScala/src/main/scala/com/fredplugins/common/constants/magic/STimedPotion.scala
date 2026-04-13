package com.fredplugins.common.constants.magic

import net.runelite.api.gameval.VarbitID.{ARCEUUS_DEATH_CHARGE_ACTIVE, ARCEUUS_DEATH_CHARGE_COOLDOWN}
import net.runelite.api.gameval.VarbitID.{ARCEUUS_SHADOW_VEIL_ACTIVE, ARCEUUS_SHADOW_VEIL_COOLDOWN}
import net.runelite.api.gameval.VarbitID.{
	DIVINEATTACK_POTION_TIME,
	DIVINESTRENGTH_POTION_TIME,
	DIVINEDEFENCE_POTION_TIME,
	DIVINERANGE_POTION_TIME,
	DIVINEMAGIC_POTION_TIME,
	MOONLIGHT_POTION_TIME,
	GOADING_POTION_TIMER,
	PRAYER_REGENERATION_POTION_TIMER,
	DIVINECOMBAT_POTION_TIME,
	DIVINEBASTION_POTION_TIME,
	DIVINEBATTLEMAGE_POTION_TIME,
	STATRENEWAL_POTION_TIMER,
	SURGE_POTION_TIMER
}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}


import scala.compiletime.uninitialized


sealed trait STimedPotion(val varbit: Int) extends enumeratum.EnumEntry {}

object STimedPotion extends enumeratum.Enum[STimedPotion] {
	case object Divine_attack extends STimedPotion(DIVINEATTACK_POTION_TIME)
	case object Divine_strength extends STimedPotion(DIVINESTRENGTH_POTION_TIME)
	case object Divine_defence extends STimedPotion(DIVINEDEFENCE_POTION_TIME)
	case object Divine_range extends STimedPotion(DIVINERANGE_POTION_TIME)
	case object Divine_magic extends STimedPotion(DIVINEMAGIC_POTION_TIME)
	case object Divine_combat extends STimedPotion(DIVINECOMBAT_POTION_TIME)
	case object Divine_bastion extends STimedPotion(DIVINEBASTION_POTION_TIME)
	case object Divine_battlemage extends STimedPotion(DIVINEBATTLEMAGE_POTION_TIME)
	case object Goading extends STimedPotion(GOADING_POTION_TIMER)
	case object Prayer_regeneration extends STimedPotion(PRAYER_REGENERATION_POTION_TIMER)
	override def values: IndexedSeq[STimedPotion] = findValues

	private lazy val lookupMap: Map[Int, STimedPotion] = values.map(v => v.varbit -> v).toMap

	def unapply(varbitId:Int): Option[STimedPotion] = lookupMap.get(varbitId)
}