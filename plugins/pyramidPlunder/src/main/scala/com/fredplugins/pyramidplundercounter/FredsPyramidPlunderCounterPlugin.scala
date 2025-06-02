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
import net.runelite.api.events.{GameObjectSpawned, GameTick, MenuEntryAdded, MenuOptionClicked, PostMenuSort}
import net.runelite.api.gameval.VarbitID
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

	val spawnedNPC = scala.collection.mutable.ListBuffer.empty[NPC]

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
			}).map(_.apply(_.append("[").append(b._1).append(": ")).andThen(_.append(ChatColorType.NORMAL).append("]\n")))
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
	}
	override protected def shutDown(): Unit = {
		overlayManager.remove(overlay)
		eventBus.unregister(overlay)
		clickedNpcs.clear()
		clickedTiles.clear()
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
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
	}

	@Subscribe def onStatChanged(statChanged: StatChanged): Unit = {
		if (isInPyramidPlunder)
			if (statChanged.getSkill eq Skill.THIEVING)
				if (usingSpearTrap) usingSpearTrap = false
				else if (usingChestOrSarco) {
					chestLooted += 1
					val chance = getCurrentFloor.map(_.percentageOds).getOrElse(0.0d) //sceptreChance.get(client.getVarbitValue(Varbits.PYRAMID_PLUNDER_ROOM))
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
		else if (usingChestOrSarco && (statChanged.getSkill eq Skill.STRENGTH)) {
				sarcoLooted += 1
				val chance = getCurrentFloor.map(_.percentageOds).getOrElse(0.0d)
				totalChance *= (1 - chance)
				dryChance = 1 - totalChance
				usingChestOrSarco = false
				savedOutside = false
			}
	}

	@Subscribe def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked): Unit = {
		if (isInPyramidPlunder) {
			val isCC_OP: Boolean = (menuOptionClicked.getMenuAction == MenuAction.CC_OP)
			(menuOptionClicked.getMenuTarget match {
				case PyramidPlunderHelper.GRAND_GOLD_CHEST_TARGET =>  (b: Boolean) => usingChestOrSarco = b
				case PyramidPlunderHelper.SARCOPHAGUS_TARGET =>    (b: Boolean)=> usingChestOrSarco = b
				case PyramidPlunderHelper.SPEAR_TRAP =>    (b: Boolean) => usingSpearTrap = b
				case j => (b: Boolean) => ()
			}).apply(isCC_OP)
		}
	}

	@Subscribe def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
		if (isInPyramidPlunder) {
			// GRAND CHEST looting was unsuccessful if a scarab swarm spawns and targets you. You still get a chance at the sceptre
			if (usingChestOrSarco && npcSpawned.getNpc.getName.equals("Scarab Swarm")) {
				spawnedNPC.addOne(npcSpawned.getNpc)
				swarmSpawned = true
			}
		}
	}

	@Subscribe def onInteractingChanged(interactingChanged: InteractingChanged): Unit = {
		if (isInPyramidPlunder) if (swarmSpawned && spawnedNPC.contains(interactingChanged.getSource) && interactingChanged.getTarget
																																																										 .equals(client
																																																				 .getLocalPlayer)) {
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

