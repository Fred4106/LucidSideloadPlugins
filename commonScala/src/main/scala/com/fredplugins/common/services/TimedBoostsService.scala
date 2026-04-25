package com.fredplugins.common.services

import com.fredplugins.common.constants.magic.SMagicBoost
import com.fredplugins.common.constants.magic.{SMagicBoost, STimedPotion}
import com.fredplugins.common.services.TimedBoostsService.{MagicBoostChanged, MagicBoostValue, State, getCachedValue}
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import net.runelite.api.*
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.{GameTick, ItemContainerChanged, PostClientTick, VarbitChanged}
import net.runelite.api.gameval.{InventoryID, ItemID, VarbitID}
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

object TimedBoostsService {
	case class MagicBoostChanged(boost: SMagicBoost, oldValue: MagicBoostValue, newValue: MagicBoostValue)

	type MagicBoostValue = (active: Int, cooldown: Int)

	private object State {
		var active: Boolean = false
		var cachedPotionTimes: Map[STimedPotion, Int] = Map.empty
		val cachedValues: mutable.Map[SMagicBoost, (MagicBoostValue)] = mutable.HashMap.empty
	}
	private val emptyBoostValue: MagicBoostValue = (0, 0)

	extension (mb: SMagicBoost) {
		def getCachedValue: MagicBoostValue = State.cachedValues.getOrElse(mb, emptyBoostValue)
		def isActive: Boolean = mb.getCachedValue.active == 1
		def isLocked: Boolean = mb.getCachedValue.cooldown == 1
	}
}

@Singleton
class TimedBoostsService @Inject()(val client: Client, val clientThread: ClientThread, val eventBus: EventBus) extends ShimUtils.Logging("DEBUG") {
	given Client = client
	given ClientThread = clientThread

	def start(): Unit = {
		if (!State.active) {
			State.active = true
			State.cachedValues.clear()
			State.cachedPotionTimes =    clientThread.runOnClientThread(() => {STimedPotion.values.map(b => b -> client.getVarbitValue(b.varbit))}).toMap
			eventBus.register(this)
		}
	}

	def stop(): Unit = {
		if (State.active) {
			eventBus.unregister(this)
			State.active = false
			State.cachedValues.clear()
			State.cachedPotionTimes = Map.empty
		}
	}

	@Subscribe(priority=20000.0f)
	def tick(mb: GameTick):Unit = {
		SMagicBoost.values.flatMap(mb => {
			val oldValue: MagicBoostValue = mb.getCachedValue
			val nValue: MagicBoostValue = (client.getVarbitValue(mb.activeVarbit), client.getVarbitValue(mb.cooldownVarbit))
			Option.when(oldValue!=nValue){MagicBoostChanged(mb, oldValue, nValue)}
		}).toList.tapEach(x => State.cachedValues.update(x.boost, x.newValue))
			.foreach(eventBus.post(_))
	}

//	def checkBoost(boost: SMagicBoost): (active: Int, cooldown: Int) = clientThread.runOnClientThread(() => {
//		client.getVarbitValue(boost.activeVarbit) -> client.getVarbitValue(boost.cooldownVarbit)
//	})
//
//	def checkTimer(boost: STimedPotion): Int = clientThread.runOnClientThread(() => {
//		client.getVarbitValue(boost.varbit)
//	})

//
//	@Subscribe
//	def onVarbitChanged(event: VarbitChanged): Unit = {
//		if (!State.active) return
//
//		STimedPotion.unapply(event.getVarbitId).flatMap(timedPotion => {
//			val cachedValue = State.cachedPotionTimes.getOrElse(timedPotion, 0)
//			val value = event.getValue
//			State.cachedPotionTimes = State.cachedPotionTimes.updated(timedPotion, value)
//			Option.when(cachedValue != value) (
//				TimedPotionValueChanged(timedPotion, cachedValue, value)
//			)
//		}).foreach(eventBus.post(_))
//		SMagicBoost.unapply(event.getVarbitId).flatMap{
//			case (b, isActiveVarbit) => {
//				val cachedValue = (if (isActiveVarbit) State.cachedMagicActive else State.cachedMagicCooldowns).getOrElse(b, 0)
//				val value = event.getValue
//				if (isActiveVarbit)
//					State.cachedMagicActive = State.cachedMagicActive.updated(b, value)
//				else
//					State.cachedMagicCooldowns = State.cachedMagicCooldowns.updated(b, value)
//
//				Option.when(cachedValue != value) {
//					(if (isActiveVarbit) MagicBoostActiveChanged(b) else MagicBoostCooldownChanged).apply(b, cachedValue, value)
//				}
//			}
//		}.foreach(eventBus.post)
//	}
}
