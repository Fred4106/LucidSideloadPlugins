package com.fredplugins.tempoross

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.tempoross.Constants.*
import com.lucidplugins.api.utils.{InteractionUtils, NpcUtils}
import ethanApiPlugin.collections.TileObjects
import ethanApiPlugin.pathfinding.GlobalCollisionMap
import interactionApi.{NPCInteraction, TileObjectInteraction}
import net.runelite.api.{ChatMessageType, GameObject, GameState, InventoryID, ItemID, NPC, NpcID, NullObjectID, ObjectComposition, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.{ChatMessage, GameObjectDespawned, GameObjectSpawned, GameStateChanged, GameTick, ItemContainerChanged, NpcDespawned, NpcSpawned, ScriptPreFired, VarbitChanged}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger
import packets.{MousePackets, ObjectPackets}

import java.awt.Color
import java.time.Instant
import java.util
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.{Random, Try}
import scala.util.chaining.*

object FredsTemporossLogic {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	val fireObjects          : mutable.ListBuffer[(GameObject, Int)] = mutable.ListBuffer.empty
	val tetherObjects          : mutable.ListBuffer[GameObject] = mutable.ListBuffer.empty[GameObject]
	val npcs                 : mutable.ListBuffer[(NPC, WorldPoint, Int)] = mutable.ListBuffer.empty
//	var previousRegion                                             = 0
	var canAutoFishDouble: Boolean = true
	var phase                                                      = 1
	var uncookedFish                                               = 0
	var cookedFish                                                 = 0
	var crystalFish                                                = 0
	var waveIncomingStartTime: Instant                             = uninitialized
	var waveIncomingStartTick: Int                                 = -1

	var spawnLoc: WorldPoint = uninitialized

	private var plugin: FredsTemporossPlugin = uninitialized
	def init(plugin: FredsTemporossPlugin): Unit = {
		this.plugin = plugin
		reset()
	}

	def damage: Int = {
		uncookedFish * DAMAGE_PER_UNCOOKED + cookedFish * DAMAGE_PER_COOKED + crystalFish * DAMAGE_PER_CRYSTAL
	}

	def region: Int = Try(WorldPoint.fromLocalInstance(plugin.client, plugin.client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1)
	def inMinigame: Boolean = region == TEMPOROSS_REGION
	def inRewardArea: Boolean = UNKAH_REGIONS.contains(region)
	def ticksTillWave: Int = Option(waveIncomingStartTick).filter(_ != -1).map(wt => (wt + WAVE_IMPACT_TICKS) - plugin.client.getTickCount).getOrElse(-1)
	def expandTileObjToStr(to: Option[TileObject]): String = {
		to.collect {
			case go: GameObject => {
				val id            = go.getId
				val comp          = plugin.client.getObjectDefinition(id)
				val name          = comp.getName
				val actions       = comp.getActions.mkString("[", ", ", "]")
				val actions2Str   = Try(comp.getImpostor).map(_.getActions).map(iActions => s", impostorActions=${iActions.mkString("[", ", ", "]")}").getOrElse("")
				val impostorIdStr = Try(comp.getImpostor).map(_.getId).map(iid => s", impostorId=${iid}").getOrElse("")
				s"GameObject(id=${id}, name=${name}, actions=${actions}${actions2Str}${impostorIdStr})"
			}
			case j => {
				val bits          = j.getHash
				val id            = (bits >> 17 & 0xffffffff).toInt
				//						val wall = (bits >> 16 & 1).toInt
				val tpe           = (bits >> 14 & 3).toInt
				val sceneY        = (bits >> 7 & 127).toInt
				val sceneX        = (bits >> 0 & 127).toInt
				val comp          = plugin.client.getObjectDefinition(id)
				val actions       = comp.getActions.mkString("[", ", ", "]")
				val actions2Str   = Try(comp.getImpostor).map(_.getActions).map(iActions => s", impostorActions=${iActions.mkString("[", ", ", "]")}").getOrElse("")
				val impostorIdStr = Try(comp.getImpostor).map(_.getId).map(iid => s", impostorId=${iid}").getOrElse("")
				s"Other(id=${id}, tpe=$tpe, sLoc=${(sceneX, sceneY)}, actions=${actions}${actions2Str}${impostorIdStr})"
			}
		}.getOrElse("None")
	}
	def expandNpcToStr(npcOpt: Option[NPC]): String = {
		npcOpt.map(npc => {
			val id            = npc.getId
			val index         = npc.getIndex
			val comp          = npc.getComposition
			val name          = comp.getName
			val actions       = comp.getActions.mkString("[", ", ", "]")
			val actions2Str   = Option(npc.getTransformedComposition).filter(_ != comp).map(_.getActions).map(iActions => s", impostorActions=${iActions.mkString("[", ", ", "]")}").getOrElse("")
			val impostorIdStr = Option(npc.getTransformedComposition).filter(_ != comp).map(_.getId).map(iid => s", impostorId=${iid}").getOrElse("")
			s"NPC(id=${id}, index=$index, name=${name}, actions=${actions}${actions2Str}${impostorIdStr})"
		}).getOrElse("None")
	}

	@Subscribe
	def onGameObjectSpawned(gameObjectSpawned: GameObjectSpawned): Unit = {
		if(!inMinigame) return
		if (FIRE_GAMEOBJECTS.contains(gameObjectSpawned.getGameObject.getId)) {
			if(plugin.config.fireStormNotification() && gameObjectSpawned.getGameObject.getId == NullObjectID.NULL_41006) {
				plugin.notifier.notify("A strong wind blows as clouds roll in...")
			}
			fireObjects.addOne(gameObjectSpawned.getGameObject -> plugin.client.getTickCount)
		} else if (TETHER_GAMEOBJECTS.concat(DAMAGED_TETHER_GAMEOBJECTS).contains(gameObjectSpawned.getGameObject.getId)) {
			tetherObjects.addOne(gameObjectSpawned.getGameObject)
		}
	}
	def findAmmoCrate: Option[NPC] =  NpcUtils.search().filter(n => AMMO_NPCS_CRATE_IDS.contains(n.getId)).nearestToPlayer().toScala
	def findTetherSpot: Option[TileObject] =  TileObjects.search().filter(to => TETHER_GAMEOBJECTS.contains(to.getId)).withinDistance(15).nearestToPlayer().toScala
	@Subscribe
	def onGameObjectDespawned(gameObjectDespawned: GameObjectDespawned): Unit = {
		if(!inMinigame) return
		tetherObjects.filterInPlace(_ != gameObjectDespawned.getGameObject)
		fireObjects.filterInPlace(_._1 != gameObjectDespawned.getGameObject)
	}
	@Subscribe
	def onScriptPreFired(scriptPreFired: ScriptPreFired): Unit = {
		if (inMinigame && scriptPreFired.getScriptId == TEMPOROSS_HUD_UPDATE) {
			val stack = plugin.client.getIntStack
			if (stack(0) == STORM_INTENSITY) {
				val currentStormIntensity            = stack(1)
				val ninetyPercentOfMaxStormIntensity = (MAX_STORM_INTENSITY * .88).asInstanceOf[Int]
				// Compare to a 3 unit window. Seems to increase by 2 every tick, so this should make sure it only notifies once.
				val shouldAlarm = (currentStormIntensity > ninetyPercentOfMaxStormIntensity && currentStormIntensity < ninetyPercentOfMaxStormIntensity + 3)
				if(shouldAlarm) {
					if(plugin.config.stormIntensityNotification) plugin.notifier.notify("You are running out of time!")
//						plugin.clientThread.
					if(plugin.config.autoFill()) plugin.clientThread.runOnSeparateThread(() => {
//						var found = false
						var ammoCrateOpt: Option[NPC] = Option.empty
						var timeout = 15
						while(ammoCrateOpt.isEmpty && timeout > 0) {
							Thread.sleep((Random.nextDouble() * 200 + 200).toLong)
							ammoCrateOpt = findAmmoCrate
							if(ammoCrateOpt.isEmpty) {
								if(!InteractionUtils.isMoving) InteractionUtils.walk(spawnLoc)
							}
							timeout=timeout-1
						}
						if (ammoCrateOpt.isDefined) {
							log.debug(s"Should be auto filling {}", expandNpcToStr(ammoCrateOpt))
							NPCInteraction.interact(ammoCrateOpt.get, "Fill")
							//getObjectComposition(ammoCrateOpt.get.getId).getId
//							TileObjectInteraction.interact("Ammunition crate", "Fill")
						}
					})
				}
			}
		}
	}
	def getObjectComposition(id: Int): ObjectComposition = {
		val objectComposition = plugin.client.getObjectDefinition(id)
		if (objectComposition.getImpostorIds == null) objectComposition
		else objectComposition.getImpostor
	}
	@Subscribe
	def onGameTick(tick: GameTick): Unit = {
		if(inMinigame) {
			npcs.mapInPlace {
				case j@(npc, point, _) if npc.getWorldLocation == point => j
				case (npc, _, _) => (npc, npc.getWorldLocation, plugin.client.getTickCount)
			}
			if(ticksTillWave == 8) {
				val tetherOpt = findTetherSpot
				val expandedTetherStr = expandTileObjToStr(tetherOpt)
				if(plugin.config.autoTether()) plugin.clientThread.runOnSeparateThread(() => {
					Thread.sleep((Random.nextDouble() * 200).toLong + 200)
					if (tetherOpt.isDefined) {
						log.debug("Should be auto tethering to {}", expandedTetherStr)
						plugin.clientThread.invokeLater(() => {
							TileObjectInteraction.interact(tetherOpt.get, "Tether")
						})
					} else {
					}
				})
			}
		}
	}

	@Subscribe
	def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
		if (inMinigame && Constants.FISH_SPOTS.contains(npcSpawned.getNpc.getId)) {
			npcs.addOne((npcSpawned.getNpc, npcSpawned.getNpc.getWorldLocation, if(npcSpawned.getNpc.getWorldLocation.distanceTo(plugin.client.getLocalPlayer.getWorldLocation) < 14) plugin.client.getTickCount else -1))
			if(FISH_SPOTS.indexOf(npcSpawned.getNpc.getId) == 0) {
				if (plugin.config.doubleSpotNotification) plugin.notifier.notify("A double Harpoonfish spot has appeared.")
				if (plugin.client.getItemContainer(InventoryID.INVENTORY).pipe(ic => ic.size - ic.count) > 0) {
					if(canAutoFishDouble) plugin.clientThread.runOnSeparateThread(() => {
						log.debug(s"Should be auto fishing {}", expandNpcToStr(Option(npcSpawned.getNpc)))
						//						if (npcSpawned.getNpc) {
						Thread.sleep((Random.nextDouble() * 500 + 200).toLong)
						NPCInteraction.interact(npcSpawned.getNpc, "Harpoon")
						canAutoFishDouble = false
//						}
					})
				}
			}
		}
	}

	@Subscribe
	def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
		if(inMinigame) npcs.filterInPlace(_._1 != npcDespawned.getNpc)
	}

	def reset(): Unit = {
		canAutoFishDouble = plugin.config.autoFishDouble()
		npcs.clear
		fireObjects.clear
		tetherObjects.clear
		waveIncomingStartTime = null
		waveIncomingStartTick = -1
		phase = 1
		updateFishCount(0, 0, 0)
		spawnLoc = null
	}
	def updateFishCount(uncooked: Int, cooked: Int, crystal: Int): Unit = {
		this.uncookedFish = uncooked
		this.cookedFish = cooked
		this.crystalFish = crystal
	}

	@Subscribe
	def onChatMessage(chatMessage: ChatMessage): Unit = {
		if (inMinigame && chatMessage.getType == ChatMessageType.GAMEMESSAGE) {
			val message = chatMessage.getMessage.toLowerCase
			if (message.contains(WAVE_INCOMING_MESSAGE)) {
				waveIncomingStartTime = Instant.now
				waveIncomingStartTick = plugin.client.getTickCount
				if (plugin.config.waveNotification) plugin.notifier.notify("A colossal wave closes in...")
			} else if (message.contains(WAVE_END_SAFE) || message.contains(WAVE_END_DANGEROUS)) {
				waveIncomingStartTime = null
				waveIncomingStartTick = -1
			} else if (message.contains(TEMPOROSS_VULNERABLE_MESSAGE)) {
				phase += 1
			}
		}
	}

	@Subscribe
	def onItemContainerChanged(event: ItemContainerChanged): Unit = {
		if (inMinigame && event.getContainerId == InventoryID.INVENTORY.getId) {
			val inventory = event.getItemContainer
			updateFishCount(inventory.count(ItemID.RAW_HARPOONFISH), inventory.count(ItemID.HARPOONFISH), inventory.count(ItemID.CRYSTALLISED_HARPOONFISH))
		}
	}
}
