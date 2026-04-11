package com.fredplugins.pyramidplundercounter

import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getWorldLocationOpt, isNpcAction, isRuneliteAction, isTileObjectAction}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pyramidplundercounter.PyramidPlunderHelper.{getCurrentFloor, getTileObjectsQuery, isInPyramidPlunder}
import com.fredplugins.pyramidplundercounter.RoomEnum.Lobby
import com.google.gson.Gson
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.TileObjects
import ethanApiPlugin.collections.query.TileObjectQuery
import net.runelite.api.ChatMessageType
import net.runelite.api.GameState
import net.runelite.api.Skill
import net.runelite.api.{Client, DecorativeObject, GameObject, GroundObject, MenuAction, MenuEntry, NPC, Scene, Tile, TileObject, WallObject}
import net.runelite.api.events.PostMenuSort
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.InteractingChanged
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.StatChanged
import net.runelite.api.events.VarbitChanged
import net.runelite.api.events.{GameObjectSpawned, GameTick, MenuEntryAdded, MenuOptionClicked, PostMenuSort}
import net.runelite.api.gameval.VarbitID
import net.runelite.api.gameval.VarbitID.{NTK_CURRENT_ROOM_LEVEL, NTK_DOOR1_STATE, NTK_DOOR2_STATE, NTK_DOOR3_STATE, NTK_DOOR4_STATE, NTK_GOLDEN_CHEST_STATE, NTK_OUTSIDE_DOOR1_STATE, NTK_OUTSIDE_DOOR2_STATE, NTK_OUTSIDE_DOOR3_STATE, NTK_OUTSIDE_DOOR4_STATE, NTK_PLAYED_BEFORE, NTK_PLAYER_TIMER_COUNT, NTK_ROOM_NUMBER, NTK_SARCOPHAGUS_PUSH, NTK_SARCOPHAGUS_STATE, NTK_TRAP_ACTIVE, NTK_URN10_STATE, NTK_URN11_STATE, NTK_URN12_STATE, NTK_URN13_STATE, NTK_URN14_STATE, NTK_URN15_STATE, NTK_URN1_STATE, NTK_URN2_STATE, NTK_URN3_STATE, NTK_URN4_STATE, NTK_URN5_STATE, NTK_URN6_STATE, NTK_URN7_STATE, NTK_URN8_STATE, NTK_URN9_STATE}
import net.runelite.api.widgets.Widget
import net.runelite.client.chat.ChatColorType
import net.runelite.client.chat.ChatMessageBuilder
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.menus.MenuManager
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.OverlayManager
import org.slf4j.Logger

import java.awt.{Color, Font}
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
	@Inject() private val overlayPanel  : FredsPyramidPlunderCounterPanel = null
	@Inject() private val GSON: Gson                                        = null

	var stateLines: List[(String, (Int, Int))] = List.empty
	var currentRoom: Option[RoomEnum] = Option.empty
	var objects: (List[GameObject], List[WallObject]) = (List.empty, List.empty)

	def getWallObjects: List[WallObject] = objects._2
	def getGameObjects: List[GameObject] = objects._1

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
		currentRoom = Option.empty
		objects=(List.empty[GameObject],List.empty[WallObject])
		stateLines = List.empty
		overlayManager.add(overlay)
		overlayManager.add(overlayPanel)
		//		overlay.updateConfig()


//		cachedVarbitValues = HashMap.empty
	}
	override protected def shutDown(): Unit = {
		overlayManager.remove(overlay)
		overlayManager.remove(overlayPanel)
		currentRoom = Option.empty
		stateLines = List.empty
		objects=(List.empty[GameObject],List.empty[WallObject])
	}

	object Cache {
		var cachedFont: Font = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, config.getFontSize.toFloat)
		var countdownFont: Font = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, (config.getFontSize * 1.5).toFloat)
	}


	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if(e.getGroup == FredsPyramidPlunderCounterConfig.GroupName) e.getKey match {
			case "fontSize" | "fontBold" => {
				Cache.cachedFont = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, config.getFontSize.toFloat)
				Cache.countdownFont = FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, (config.getFontSize * 1.5).toFloat)
			}
			case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
		}
	}

	@Subscribe
	def onMenuEntryAdded(event: MenuEntryAdded): Unit = {
		if (isInPyramidPlunder) {
			if (config.isDebugMenu) log.debug("added: {}", event.getMenuEntry)
		}
	}

	@Subscribe
	def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked): Unit = {
		if (isInPyramidPlunder) {
			if(config.isDebugMenu) log.debug("clicked: {}", menuOptionClicked.getMenuEntry)
			val isCC_OP: Boolean = (menuOptionClicked.getMenuAction == MenuAction.CC_OP)
			var temp = menuOptionClicked.getMenuAction
		}
	}
//
//	@Subscribe def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
//		if (isInPyramidPlunder) {
//			// GRAND CHEST looting was unsuccessful if a scarab swarm spawns and targets you. You still get a chance at the sceptre
//			if (usingChestOrSarco && npcSpawned.getNpc.getName.equals("Scarab Swarm")) {
//				spawnedNPC.addOne(npcSpawned.getNpc)
//				swarmSpawned = true
////				usingChestOrSarco = false
//			}
//		}
//	}

//	var queryOp: TileObjectQuery => TileObjectQuery = (a) => a

	@Subscribe
	def onGameTick(e: GameTick): Unit = {
		val oldFloor = currentRoom
		val newFloor = getCurrentFloor
//			log.debug("current floor is {}, but was {}", n, currentRoom)
		val didChange = Option((oldFloor, newFloor)).collect {
			case (Some(old), Some(newer)) if(old != newer) => s"Changed from ${old} to ${newer}"
			case (Some(old), None) => s"Exited from ${old}"
			case (None, Some(newer)) => s"Entered to ${newer}"
		}.tap(_.foreach(u =>
			log.debug(s"${u}")
		)).isDefined

		if(didChange) {
			currentRoom = newFloor
			objects = getTileObjectsQuery(TileObjects.search()).pipe(q => {
				(q.gameObjects().asScala.toList, q.wallObjects().asScala.toList)
			})
		}

	}
}

