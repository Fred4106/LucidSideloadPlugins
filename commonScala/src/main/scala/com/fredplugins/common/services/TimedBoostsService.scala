package com.fredplugins.common.services

import com.fredplugins.common.constants.magic.{SMagicBoost, STimedPotion}
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import net.runelite.api.*
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.{ItemContainerChanged, PostClientTick, VarbitChanged}
import net.runelite.api.gameval.{InventoryID, ItemID, VarbitID}
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}

import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

case class TimedPotionValueChanged(boost: STimedPotion, oldValue: Int, newValue: Int)
case class MagicBoostCooldownChanged(boost: SMagicBoost, oldValue: Int, newValue: Int)
case class MagicBoostActiveChanged(boost: SMagicBoost, oldValue: Int, newValue: Int)

@Singleton
class TimedBoostsService @Inject()(val client: Client, val clientThread: ClientThread, val eventBus: EventBus) extends ShimUtils.Logging("DEBUG") {
	given Client = client

	private case class MagicBoostData(active: Boolean, cooldown:Boolean) {}

	private object State {
		var active: Boolean = false

		var cachedMagicActive: Map[SMagicBoost, Int] = Map.empty
		var cachedMagicCooldowns: Map[SMagicBoost, Int] = Map.empty
		var cachedPotionTimes: Map[STimedPotion, Int] = Map.empty
	}

	def start(): Unit = {
		if (!State.active) {
			State.active = true
			State.cachedMagicActive =    clientThread.runOnClientThread(() => {SMagicBoost.values.map(b => b -> client.getVarbitValue(b.activeVarbit  ))}).toMap
			State.cachedMagicCooldowns = clientThread.runOnClientThread(() => {SMagicBoost.values.map(b => b -> client.getVarbitValue(b.cooldownVarbit))}).toMap
			State.cachedPotionTimes =    clientThread.runOnClientThread(() => {STimedPotion.values.map(b => b -> client.getVarbitValue(b.varbit))}).toMap
			eventBus.register(this)
		}
	}

	def stop(): Unit = {
		if (State.active) {
			eventBus.unregister(this)
			State.active = false
			State.cachedPotionTimes = Map.empty
			State.cachedMagicActive = Map.empty
			State.cachedMagicCooldowns =  Map.empty
		}
	}

	def checkBoost(boost: SMagicBoost): (active: Int, cooldown: Int) = clientThread.runOnClientThread(() => {
		client.getVarbitValue(boost.activeVarbit) -> client.getVarbitValue(boost.cooldownVarbit)
	})
	def checkTimer(boost: STimedPotion): Int = clientThread.runOnClientThread(() => {
		client.getVarbitValue(boost.varbit)
	})

	@Subscribe
	def onVarbitChanged(event: VarbitChanged): Unit = {
		if (!State.active) return

		STimedPotion.unapply(event.getVarbitId).flatMap(timedPotion => {
			val cachedValue = State.cachedPotionTimes.getOrElse(timedPotion, 0)
			val value = event.getValue
			State.cachedPotionTimes = State.cachedPotionTimes.updated(timedPotion, value)
			Option.when(cachedValue != value) (
				TimedPotionValueChanged(timedPotion, cachedValue, value)
			)
		}).foreach(eventBus.post(_))
		SMagicBoost.unapply(event.getVarbitId).flatMap((b, isActiveVarbit) => {
			val cachedValue = (if(isActiveVarbit) State.cachedMagicActive else State.cachedMagicCooldowns).get(b).getOrElse(0)
			val value = event.getValue
			if (isActiveVarbit)
				State.cachedMagicActive = State.cachedMagicActive.updated(b, value)
			else
				State.cachedMagicCooldowns = State.cachedMagicCooldowns.updated(b, value)

			Option.when(cachedValue != value) ({
				(if (isActiveVarbit) MagicBoostActiveChanged.apply else MagicBoostCooldownChanged.apply).apply(b, cachedValue, value)
			})
		}).foreach(eventBus.post)

	}
}
