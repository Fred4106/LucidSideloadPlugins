package com.fredplugins.pyramidplundercounter

import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getWorldLocationOpt, isNpcAction, isRuneliteAction, isTileObjectAction}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pyramidplundercounter.PyramidPlunderHelper.getCurrentFloor
import com.fredplugins.pyramidplundercounter.PyramidPlunderHelper.isInPyramidPlunder
import com.google.gson.Gson
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.ChatMessageType
import net.runelite.api.GameState
import net.runelite.api.Skill
import net.runelite.api.{Client, DecorativeObject, GameObject, GroundObject, MenuAction, MenuEntry, NPC, Scene, Tile, TileObject, WallObject}
import net.runelite.api.events.PostMenuSort
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.events.InteractingChanged
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.StatChanged
import net.runelite.api.events.VarbitChanged
import net.runelite.api.events.{GameObjectSpawned, GameTick, MenuEntryAdded, MenuOptionClicked, PostMenuSort}
import net.runelite.api.gameval.VarbitID
import net.runelite.api.gameval.VarbitID.{NTK_PLAYER_TIMER_COUNT, NTK_CURRENT_ROOM_LEVEL,NTK_ROOM_NUMBER, NTK_DOOR1_STATE, NTK_DOOR2_STATE, NTK_DOOR3_STATE, NTK_DOOR4_STATE, NTK_GOLDEN_CHEST_STATE, NTK_OUTSIDE_DOOR1_STATE, NTK_OUTSIDE_DOOR2_STATE, NTK_OUTSIDE_DOOR3_STATE, NTK_OUTSIDE_DOOR4_STATE, NTK_PLAYED_BEFORE, NTK_SARCOPHAGUS_PUSH, NTK_SARCOPHAGUS_STATE, NTK_TRAP_ACTIVE, NTK_URN10_STATE, NTK_URN11_STATE, NTK_URN12_STATE, NTK_URN13_STATE, NTK_URN14_STATE, NTK_URN15_STATE, NTK_URN1_STATE, NTK_URN2_STATE, NTK_URN3_STATE, NTK_URN4_STATE, NTK_URN5_STATE, NTK_URN6_STATE, NTK_URN7_STATE, NTK_URN8_STATE, NTK_URN9_STATE}
import net.runelite.api.widgets.Widget
import net.runelite.client.chat.ChatColorType
import net.runelite.client.chat.ChatMessageBuilder
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.menus.MenuManager
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.Color
import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*


@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Pyramid Plunder Counter</html>",
	description = "This plugin will display how many golden chests and sarcophagi you have successfully opened in your session.",
	tags = Array("pyramid", "plunder", "pyramid plunder", "fred4106"),
	)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsPyramidPlunderCounterPlugin() extends Plugin {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	@Inject() private val eventBus: EventBus = null
	@Inject() private val client  : Client   = null
	given Client = client

	@Inject() private val menuManager   : MenuManager                  = null
	@Inject() private val overlayManager: OverlayManager                    = null
	@Inject() private val config        : FredsPyramidPlunderCounterConfig  = null
	@Inject() private val overlay       : FredsPyramidPlunderCounterOverlay = null
	@Inject() private val GSON: Gson                                        = null

	private val clickedTiles: mutable.ListBuffer[(Int, WorldPoint)] = mutable.ListBuffer.empty
	private val clickedNpcs : mutable.ListBuffer[(Int, NPC)]        = mutable.ListBuffer.empty

	var savedOutside  = false
	var loadedSession = false


	var chestLooted: Int = 0
	var sarcoLooted: Int = 0
	var totalChance: Double = 1
	var dryChance: Double = 0
//	var totalPetChance: Double = 1
//	var petDryChance: Double = 0

	var usingChestOrSarco: Boolean = false
	var usingSpearTrap   : Boolean = false
	var swarmSpawned     : Boolean = false

	val spawnedNPC: mutable.ListBuffer[NPC] = scala.collection.mutable.ListBuffer.empty[NPC]

	def getClickedState: (List[(Int, NPC)], List[(Int, WorldPoint)]) = {
		clickedNpcs.toList -> clickedTiles.toList
	}

	@Provides
	def getConfig(configManager: ConfigManager): FredsPyramidPlunderCounterConfig = {
		configManager.getConfig[FredsPyramidPlunderCounterConfig](classOf[FredsPyramidPlunderCounterConfig])
	}

	def buildMessage(header: String, parts: (String, Any)*): String = {
		def add(func: ChatMessageBuilder => ChatMessageBuilder)(chatMessageBuilder: ChatMessageBuilder => ChatMessageBuilder) = chatMessageBuilder
			.andThen(func)

		parts.foldLeft(new ChatMessageBuilder().append(ChatColorType.NORMAL).append(header + "\n"))((a, b) => {
			(b._2 match {
				case (c: Color, s: Any) => Option(add(_.append(c, s.toString)))
				case null => Option.empty
				case x => Option(add(_.append(Color.BLUE, x.toString)))
			}).map(_.apply(_.append("[").append(b._1).append(": ")).andThen(_.append(ChatColorType.NORMAL).append("]")))
				.map(_.apply(a))
				.getOrElse(a)
		}).build().stripTrailing().stripSuffix(",").stripSuffix("<br>")
	}

	override protected def startUp(): Unit = {
		clickedTiles.clear()
		clickedNpcs.clear()
		eventBus.register(overlay)
		overlayManager.add(overlay)
		//		overlay.updateConfig()


		spawnedNPC.clear()
		cachedVarbitValues = HashMap.empty
	}
	override protected def shutDown(): Unit = {
		overlayManager.remove(overlay)
		eventBus.unregister(overlay)
		clickedNpcs.clear()
		clickedTiles.clear()
	}

	var cachedVarbitValues: Map[Int, Int]= HashMap.empty
	val toMonitor  : List[Int]        = List(
		//NTK_SARCOPHAGUS_PUSH, NTK_TRAP_ACTIVE,
		NTK_PLAYER_TIMER_COUNT,
		NTK_ROOM_NUMBER,
		NTK_CURRENT_ROOM_LEVEL,
																					 NTK_SARCOPHAGUS_STATE, NTK_GOLDEN_CHEST_STATE,
																					 NTK_DOOR1_STATE, NTK_DOOR2_STATE, NTK_DOOR3_STATE, NTK_DOOR4_STATE,
//																					 NTK_OUTSIDE_DOOR1_STATE, NTK_OUTSIDE_DOOR2_STATE, NTK_OUTSIDE_DOOR3_STATE, NTK_OUTSIDE_DOOR4_STATE
																					 )
	var stateLines : List[(String, (Int, Int))] = List.empty
	val varbitNames: Map[Int, String] = toMonitor.map(vid => classOf[VarbitID].getDeclaredFields.find(f => vid == f.get(null)).map(f => (vid, f.getName.stripPrefix("NTK_"))).getOrElse((vid, s"${vid}"))).toMap

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		cachedVarbitValues = toMonitor.flatMap(vid => {
			val toUpdate = Option(vid).map(j => (j, client.getVarbitValue(j), cachedVarbitValues.applyOrElse(j, _ => 0)))
																.filter{
																	case (_, nVal, oVal) => nVal != oVal
																}
			toUpdate
//																.filter(j => cachedVarbitValues.contains(j._1) && j._2 != cachedVarbitValues(j._1))
//			toUpdate.foreach{
//				case (vid, o, n) => {
//					cachedVarbitValues = cachedVarbitValues.updated(vid, n)
//				}
//			}
//			oldOpt.map(o => (o, client.getVarbitValue(vid))).filter{}
//			val nValue =
//			cachedVarbitValues = cachedVarbitValues.updated(vid, @nValue)
		}).foldLeft(cachedVarbitValues){
			case (acm, (vid, nVal, oVal)) => {
				val message = buildMessage(
					s"${varbitNames(vid)}",
					("vbitId", vid),
					("delta", (oVal -> nVal))
				)
				val line    = client.addChatMessage(ChatMessageType.CLAN_GUEST_CHAT, "Gametick", message, "PPC")
				acm.updated(vid, nVal)
			}
		}


//		if (cachedVarbitValues.keySet.toList.filterNot().isEmpty) {
//			cachedVarbitValues = toMonitor.map(vid => (vid, client.getVarbitValue(vid))).toMap
//		}
		if (!loadedSession && client.getGameState == GameState.LOGGED_IN) {
//			importData1
			loadedSession = true
		}
		if (!savedOutside) {
			val username = client.getLocalPlayer.getName
			if (username != null) {
				//exportData(new File(DATA_FOLDER, username + ".json"))
				savedOutside = true
			}
		}
		stateLines = varbitNames.toList.map {
			case (vid, vStr) => (vStr, (vid, cachedVarbitValues(vid)))
		}.sortBy(_._1)
//		toMonitor.map(i =
//
	}

	@Subscribe def onStatChanged(statChanged: StatChanged): Unit = {
		if (isInPyramidPlunder) {
			if (statChanged.getSkill eq Skill.THIEVING) {
				if (usingSpearTrap) usingSpearTrap = false
				else if (usingChestOrSarco) {
					chestLooted += 1
					val chance = getCurrentFloor.map(_.percentageOds)
																			.getOrElse(0.0d) //sceptreChance.get(client.getVarbitValue(Varbits.PYRAMID_PLUNDER_ROOM))
					totalChance *= (1 - chance)
					dryChance = 1 - totalChance
					val baseChanceModifier = client.getRealSkillLevel(Skill.THIEVING) * 25
					//					val realPetChance      = petBaseChance.get(client.getVarbitValue(Varbits
					//																																										 .PYRAMID_PLUNDER_ROOM)) - baseChanceModifier
					//					val petChance          = 1.0D / realPetChance
					//					totalPetChance *= (1 - petChance)
					//					petDryChance = 1 - totalPetChance
					usingChestOrSarco = false
					savedOutside = false
				}
			} else if (usingChestOrSarco && (statChanged.getSkill eq Skill.STRENGTH)) {
				sarcoLooted += 1
				val chance = getCurrentFloor.map(_.percentageOds).getOrElse(0.0d)
				totalChance *= (1 - chance)
				dryChance = 1 - totalChance
				usingChestOrSarco = false
				savedOutside = false
			}
		}
	}

	@Subscribe def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked): Unit = {
		if (isInPyramidPlunder) {
			val isCC_OP: Boolean = (menuOptionClicked.getMenuAction == MenuAction.CC_OP)
			var temp = menuOptionClicked.getMenuAction
			(menuOptionClicked.getMenuTarget match {
				case PyramidPlunderHelper.GRAND_GOLD_CHEST_TARGET =>  usingChestOrSarco = true
				case PyramidPlunderHelper.SARCOPHAGUS_TARGET =>   usingChestOrSarco = true
				case PyramidPlunderHelper.SPEAR_TRAP =>    usingSpearTrap = true
				case j => {
					if(!isCC_OP && usingChestOrSarco) {
						usingChestOrSarco = false
					}
				}
			})
		}
	}

	@Subscribe def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
		if (isInPyramidPlunder) {
			// GRAND CHEST looting was unsuccessful if a scarab swarm spawns and targets you. You still get a chance at the sceptre
			if (usingChestOrSarco && npcSpawned.getNpc.getName.equals("Scarab Swarm")) {
				spawnedNPC.addOne(npcSpawned.getNpc)
				swarmSpawned = true
//				usingChestOrSarco = false
			}
		}
	}

	@Subscribe
	def onVarbitChanged(event: VarbitChanged): Unit = {
		val vbitId: Int = event.getVarbitId
		if(vbitId != -1 && toMonitor.contains(vbitId) && cachedVarbitValues.contains(vbitId)) {
			val oldValue = cachedVarbitValues(vbitId)
			val newValue = event.getValue
			if(oldValue != newValue) {
				cachedVarbitValues = cachedVarbitValues.updated(vbitId, newValue)
					val message = buildMessage(
						s"${varbitNames(vbitId)}",
						("vbit", vbitId),
						("delta", (oldValue, newValue))
					)
					val line    = client.addChatMessage(ChatMessageType.CLAN_GUEST_CHAT, "VbitChanged", message, "PPC")
//					val line    = client.addChatMessage(ChatMessageType.GAMEMESSAGE, "PP Counter", message, "")
			}
		}
	}
	@Subscribe
	def onInteractingChanged(interactingChanged: InteractingChanged): Unit = {
		if (isInPyramidPlunder) {
			if (swarmSpawned &&
				spawnedNPC.contains(interactingChanged.getSource) &&
				(interactingChanged.getTarget == null || interactingChanged.getTarget.equals(client.getLocalPlayer))
			) {
				swarmSpawned = false
				chestLooted += 1
				val chance = PyramidPlunderHelper.getCurrentFloor.map(f => f.percentageOds).getOrElse(0.0d)
				totalChance = totalChance * (1 - chance)
				dryChance = 1 - totalChance
	//			val baseChanceModifier = client.getRealSkillLevel(Skill.THIEVING) * 25
	//			val realPetChance      = petBaseChance.get(client.getVarbitValue(Varbits
	//																																								 .PYRAMID_PLUNDER_ROOM)) - baseChanceModifier
	//			val petChance          = 1.0D / realPetChance
	//			totalPetChance *= (1 - petChance)
	//			petDryChance = 1 - totalPetChance
				spawnedNPC.clear
			}
		}
	}

//
//	@Subscribe
//	def onGameTick(event: GameTick): Unit = {
//		clickedTiles.flatMapInPlace {
//			case (i, point) if i < 20 => Some((i + 1, point))
//			case (_, point) => None
//		}
//		clickedNpcs.flatMapInPlace {
//			case (i, point) if i < 20 => Some((i + 1, point))
//			case (_, point) => None
//		}
//	}
//
//	@Subscribe
//	def onMenuEntryAdded(menuOptionAdded: MenuEntryAdded): Unit = {
//		//		val targetOpt = MenuEntryTarget(menuOptionAdded)
//		//		targetOpt.foreach(met => log.info("Transformed {} into {}", menuOptionAdded, met))
//		//		if (targetOpt.isEmpty) {
//		//			log.debug("Cant handle {} yet", menuOptionAdded)
//		//		}
//	}
//
//	@Subscribe(priority = -15)
//	def onPostMenuSort(postMenuSort: PostMenuSort): Unit = {
//		if (!client.isMenuOpen) {
//			val menuEntries   : List[MenuEntry] = client.getMenuEntries.toList
//			val (added, stock)                  = menuEntries.partition(e => {
//				//				e.getType == MenuAction.RUNELITE && (e.getOption.startsWith("Plant") || e.getOption.startsWith("Water"))
//				false
//			})
//			val newMenuEntries: List[MenuEntry] = stock.appendedAll(added)
//			client.setMenuEntries(newMenuEntries.toArray[MenuEntry])
//		}
//	}
//
//	@Subscribe
//	def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked): Unit = {
//		if (config.isDebugMenu) {
//			log.debug(
//				"Clicked {}(option=\"{}\", target=\"{}\")", menuOptionClicked.getMenuAction, menuOptionClicked
//					.getMenuOption, menuOptionClicked.getMenuTarget)
//			val message = buildMessage(
//				menuOptionClicked.getMenuAction.toString,
//				("option", menuOptionClicked.getMenuOption),
//				("target", menuOptionClicked.getMenuTarget),
//				("params", (menuOptionClicked.getParam0, menuOptionClicked.getParam1)),
//				)
//			val line    = client.addChatMessage(ChatMessageType.GAMEMESSAGE, "SuperClicker", message, "")
//		}
//		//		val targetOpt = MenuEntryTarget(menuOptionClicked)
//		//		targetOpt.foreach(met => log.info("Transformed {} into {}", menuOptionClicked, met))
//		//		if(targetOpt.isEmpty) {
//		//			log.debug("Cant handle {} yet", menuOptionClicked)
//		//		}
//
//		val me = menuOptionClicked.getMenuEntry
//		me.getWorldLocationOpt.foreach(
//			worldPoint => {
//				clickedTiles.filterInPlace {
//					case (_, point) => !point.equals(worldPoint)
//				}.addOne((0, worldPoint))
//			}
//			)
//		me.getNpcOpt.foreach(
//			npc => {
//				clickedNpcs.filterInPlace {
//					case (_, n) => !n.equals(npc)
//				}.addOne((0, npc))
//			}
//			)
//	}
}

