package com.fredplugins.tempoross

import com.fredplugins.tempoross.Constants._
import net.runelite.api.{ChatMessageType, GameObject, GameState, InventoryID, ItemID, NPC, NpcID, NullObjectID}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.{ChatMessage, GameObjectDespawned, GameObjectSpawned, GameStateChanged, ItemContainerChanged, NpcDespawned, NpcSpawned, ScriptPreFired, VarbitChanged}
import net.runelite.client.eventbus.{EventBus, Subscribe}

import java.awt.Color
import java.time.Instant
import java.util
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

object FredsTemporossLogic {
	val gameObjects          : mutable.Map[GameObject, DrawObject] = mutable.WeakHashMap.empty[GameObject, DrawObject]
	val npcs                 : mutable.Map[NPC, Long]              = mutable.WeakHashMap.empty[NPC, Long]
	var waveIsIncoming                                             = false
	var nearRewardPool                                             = false
	var previousRegion                                             = 0
	var phase                                                      = 1
	var uncookedFish                                               = 0
	var cookedFish                                                 = 0
	var crystalFish                                                = 0
	var waveIncomingStartTime: Instant                             = uninitialized
	private var plugin: FredsTemporossPlugin = uninitialized
	def init(plugin: FredsTemporossPlugin): Unit = {
		this.plugin = plugin
	}
	def damage: Int = {
		uncookedFish * DAMAGE_PER_UNCOOKED + cookedFish * DAMAGE_PER_COOKED + crystalFish * DAMAGE_PER_CRYSTAL
	}
	@Subscribe
	def onGameObjectSpawned(gameObjectSpawned: GameObjectSpawned): Unit = {
		if (!FIRE_GAMEOBJECTS.concat(TETHER_GAMEOBJECTS).concat(DAMAGED_TETHER_GAMEOBJECTS).contains(gameObjectSpawned.getGameObject.getId)) {
		} else if (FIRE_GAMEOBJECTS.contains(gameObjectSpawned.getGameObject.getId)) {
			val duration = gameObjectSpawned.getGameObject.getId match {
				case NullObjectID.NULL_37582 => FIRE_SPREAD_MILLIS
				case NullObjectID.NULL_41006 => {
					if (plugin.config.fireStormNotification()) {
						plugin.notifier.notify("A strong wind blows as clouds roll in...")
					}
					FIRE_SPAWN_MILLIS
				}
				case NullObjectID.NULL_41007 => FIRE_SPREADING_SPAWN_MILLIS
			}
			gameObjects.put(gameObjectSpawned.getGameObject, new DrawObject(gameObjectSpawned.getTile, Instant.now, duration, plugin.config.fireColor))
		} else if (DAMAGED_TETHER_GAMEOBJECTS.contains(gameObjectSpawned.getGameObject.getId)) {
			//if it is not one of the above, it is a totem/mast and should be added to the totem map, with 7800ms duration, and the regular color totemMap.put(gameObjectSpawned.getGameObject, new Nothing(gameObjectSpawned.getTile, Instant.now, WAVE_IMPACT_MILLIS, config.waveTimerColor))
			gameObjects.put(gameObjectSpawned.getGameObject, new DrawObject(gameObjectSpawned.getTile, Instant.now, 0, plugin.config.poleBrokenColor()))
			//			if (waveIsIncoming) {
			//				gameObjects.put(gameObjectSpawned.getGameObject, new DrawObject(gameObjectSpawned.getTile, Instant.now, WAVE_IMPACT_MILLIS, Color.PINK))
			//				addTotemTimers()
			//			} else {
			//				gameObjects.put(gameObjectSpawned.getGameObject, new DrawObject(gameObjectSpawned.getTile, Instant.now, 0, Color.ORANGE))
			//			}
		} else if (TETHER_GAMEOBJECTS.contains(gameObjectSpawned.getGameObject.getId)) {
			//if it is not one of the above, it is a totem/mast and should be added to the totem map, with 7800ms duration, and the regular color totemMap.put(gameObjectSpawned.getGameObject, new Nothing(gameObjectSpawned.getTile, Instant.now, WAVE_IMPACT_MILLIS, config.waveTimerColor))
			gameObjects.put(gameObjectSpawned.getGameObject, new DrawObject(gameObjectSpawned.getTile, Instant.now, 0, Color.pink))
			plugin.addTotemTimers()
			//			if (waveIsIncoming) {
			//				gameObjects.put(gameObjectSpawned.getGameObject, new DrawObject(gameObjectSpawned.getTile, Instant.now, WAVE_IMPACT_MILLIS, Color.PINK))
			//				addTotemTimers()
			//			} else {
			//				gameObjects.put(gameObjectSpawned.getGameObject, new DrawObject(gameObjectSpawned.getTile, Instant.now, 0, Color.ORANGE))
			//			}
		}
	}
	@Subscribe
	def onGameObjectDespawned(gameObjectDespawned: GameObjectDespawned): Unit = {
		gameObjects.remove(gameObjectDespawned.getGameObject)
	}
	@Subscribe
	def onScriptPreFired(scriptPreFired: ScriptPreFired): Unit = {
		if (!plugin.config.stormIntensityNotification || (scriptPreFired.getScriptId != TEMPOROSS_HUD_UPDATE)) {
		} else {
			val stack = plugin.client.getIntStack
			if (stack(0) == STORM_INTENSITY) {
				val currentStormIntensity            = stack(1)
				val ninetyPercentOfMaxStormIntensity = (MAX_STORM_INTENSITY * .9).asInstanceOf[Int]
				// Compare to a 3 unit window. Seems to increase by 2 every tick, so this should make sure it only notifies once.
				if (currentStormIntensity > ninetyPercentOfMaxStormIntensity && currentStormIntensity < ninetyPercentOfMaxStormIntensity + 3) plugin.notifier.notify("You are running out of time!")
			}
		}
	}
	@Subscribe
	def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
		if (NpcID.FISHING_SPOT_10569 == npcSpawned.getNpc.getId) {
			if (plugin.config.highlightDoubleSpot) npcs.put(npcSpawned.getNpc, Instant.now.toEpochMilli)
			if (plugin.config.doubleSpotNotification) plugin.notifier.notify("A double Harpoonfish spot has appeared.")
		}
	}
	@Subscribe
	def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
		npcs.remove(npcDespawned.getNpc)
	}
	@Subscribe
	def onGameStateChanged(gameStateChanged: GameStateChanged): Unit = {
		if (gameStateChanged.getGameState == GameState.LOADING) {
			reset()
		}
		if (plugin.client.getLocalPlayer != null) {
			val region = WorldPoint.fromLocalInstance(plugin.client, plugin.client.getLocalPlayer.getLocalLocation).getRegionID
			if (region != TEMPOROSS_REGION && (previousRegion == TEMPOROSS_REGION)) reset()
			else if (region == TEMPOROSS_REGION && (previousRegion != TEMPOROSS_REGION)) plugin.redrawInfoBoxes()
			nearRewardPool = region == UNKAH_BOAT_REGION || region == UNKAH_REWARD_POOL_REGION

			if (nearRewardPool) plugin.addRewardInfoBox()
			else {
				plugin.infoBoxManager.removeInfoBox(plugin.rewardInfoBox)
				plugin.rewardInfoBox = null
			}

			previousRegion = region
		}
	}
	def reset(): Unit = {
		npcs.clear
		gameObjects.clear
		waveIsIncoming = false
		phase = 1
		updateFishCount(0, 0, 0)
	}
	def updateFishCount(uncooked: Int, cooked: Int, crystal: Int): Unit = {
		this.uncookedFish = uncooked
		this.cookedFish = cooked
		this.crystalFish = crystal
	}
	@Subscribe
	def onVarbitChanged(event: VarbitChanged): Unit = {
		if (event.getVarbitId == VARB_REWARD_POOL_NUMBER) if (nearRewardPool) plugin.addRewardInfoBox(event.getValue)
																											else if (event.getVarbitId == VARB_IS_TETHERED) {
																												// The varb is a bitfield that refers to what totem/mast the player is tethered to,
																												// with each bit corresponding to a different object, so when tethered, the totem color should update
																												plugin.addTotemTimers()
																											}
	}
	@Subscribe
	def onChatMessage(chatMessage: ChatMessage): Unit = {
		if (chatMessage.getType != ChatMessageType.GAMEMESSAGE) {
		} else {
			val message = chatMessage.getMessage.toLowerCase
			if (message.contains(WAVE_INCOMING_MESSAGE)) {
				waveIsIncoming = true
				waveIncomingStartTime = Instant.now
				plugin.addTotemTimers()
				if (plugin.config.waveNotification) plugin.notifier.notify("A colossal wave closes in...")
			} else if (message.contains(WAVE_END_SAFE) || message.contains(WAVE_END_DANGEROUS)) {
				waveIsIncoming = false
				plugin.removeTotemTimers()
			} else if (message.contains(TEMPOROSS_VULNERABLE_MESSAGE)) {
				phase += 1
				plugin.redrawInfoBoxes()
			}
		}
	}

	@Subscribe
	def onItemContainerChanged(event: ItemContainerChanged): Unit = {
		if ((event.getContainerId != InventoryID.INVENTORY.getId) || (!plugin.config.fishIndicator && !plugin.config.damageIndicator) || (plugin.fishInfoBox == null && plugin.damageInfoBox == null)) {
		} else {
			val inventory = event.getItemContainer
			updateFishCount(inventory.count(ItemID.RAW_HARPOONFISH), inventory.count(ItemID.HARPOONFISH), inventory.count(ItemID.CRYSTALLISED_HARPOONFISH))
			plugin.redrawInfoBoxes()
		}
	}
}
