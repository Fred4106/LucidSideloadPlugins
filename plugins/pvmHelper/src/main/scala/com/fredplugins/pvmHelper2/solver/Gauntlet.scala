package com.fredplugins.pvmHelper2.solver

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmHelper2.FredsPvmHelper2
import net.runelite.api.{NpcID, NullNpcID}
import net.runelite.api.events.{AnimationChanged, NpcDespawned, NpcSpawned, VarbitChanged}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger

import scala.compiletime.uninitialized
import java.util.function.Consumer
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

object Gauntlet {
//	val log: Logger = ShimUtils.getLogger(getClass.getName, "DEBUG")
	private val log: Logger = ShimUtils.getLogger(getClass.getName, "DEBUG")

	trait NpcFilterSubscription {
		self: NpcFilterTrait =>

		@Subscribe
		def onNpcSpawned(event: NpcSpawned): Unit = {
			self.filter(event).foreach(u => log.debug("{} Npc {} {} at {}", u._1, u._2.name, u._3.getName, u._3.getWorldLocation))
		}

		@Subscribe
		def onAnimationChanged(event:AnimationChanged): Unit = {
			self.filter(event).foreach {
				case u@(str, eventType, npc) =>
					log.debug("{} Npc {} {} to {} at {}", u._1, u._2.name, u._3.getName, u._3.getAnimation, u._3.getWorldLocation)
			}
		}

		@Subscribe
		def onNpcDespawned(event: NpcDespawned): Unit = {
			self.filter(event).foreach(u => log.debug("{} Npc {} {} at {}", u._1, u._2.name, u._3.getName, u._3.getWorldLocation))
		}
	}
	case object Weak extends NpcFilterSingle(
		NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT,
		NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT,
		NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER
	)
	case object Strong extends NpcFilterSingle(
		NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION,
		NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN,
		NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF
	)

	case object BearBoss extends NpcFilterSingle(
		NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR
	)
	case object DarkBeastBoss extends NpcFilterSingle(
		NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST
	)
	case object DragonBoss extends NpcFilterSingle(
		NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON
	)

	case object Tornado extends NpcFilterSingle(NullNpcID.NULL_9025, NullNpcID.NULL_9039, NullNpcID.NULL_14142) {}

	case object Hunllef extends NpcFilterSingle(
		NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022,
		NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024,
		NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036,
		NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038
	)

	case object BasicNpcs extends NpcFilterMulti(Weak, Strong) with NpcFilterSubscription{
	}

	case object DemiBoss extends NpcFilterMulti(DragonBoss, DarkBeastBoss, BearBoss)with NpcFilterSubscription{
	}

	case object BossFilter extends NpcFilterMulti(Tornado, Hunllef) with NpcFilterSubscription{
	}

	case object AllFilter extends NpcFilterMulti(BasicNpcs, DemiBoss, BossFilter) {}

	var sub: EventBus.Subscriber = uninitialized
	def init(plugin: FredsPvmHelper2): Unit = {
		sub = plugin.eventBus.register[VarbitChanged](classOf[VarbitChanged], (v: VarbitChanged) => {
			if(v.getVarbitId == 9178) {
				if(v.getValue == 1) {
					plugin.eventBus.register(BasicNpcs)
					plugin.eventBus.register(DemiBoss)
				} else {
					plugin.eventBus.unregister(BasicNpcs)
					plugin.eventBus.unregister(DemiBoss)
				}
			}
			if (v.getVarbitId == 9177) {
				if (v.getValue == 1) {
					plugin.eventBus.register(BossFilter)
				} else {
					plugin.eventBus.unregister(BossFilter)
				}
			}
		}, 0.0)

		plugin.clientThread.runOnClientThread(() => {
			val v = plugin.client.getVarbitValue(9178)
			log.debug("gauntlet init has starting varbit value of {}", v)
			val varbitChanged = new VarbitChanged()
			varbitChanged.setVarbitId(9178)
			varbitChanged.setValue(v)
			plugin.eventBus.post(varbitChanged)
		})
		plugin.clientThread.runOnClientThread(() => {
			val v = plugin.client.getVarbitValue(9177)
			log.debug("Hunllef init has starting varbit value of {}", v)
			val varbitChanged = new VarbitChanged()
			varbitChanged.setVarbitId(9177)
			varbitChanged.setValue(v)
			plugin.eventBus.post(varbitChanged)
		})
	}
	def teardown(plugin: FredsPvmHelper2): Unit = {
		plugin.eventBus.unregister(sub)
		sub = null
		plugin.eventBus.unregister(BossFilter)
		plugin.eventBus.unregister(BasicNpcs)
		plugin.eventBus.unregister(DemiBoss)
	}
}
