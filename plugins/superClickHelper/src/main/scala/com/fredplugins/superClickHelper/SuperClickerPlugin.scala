package com.fredplugins.superClickHelper

import com.fredplugins.common.MenuExtensions.{getNpcOpt, getWorldLocationOpt, isTileObjectAction, isNpcAction, isRuneliteAction}
import com.fredplugins.common.ShimUtils
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.{Client, DecorativeObject, GameObject, GroundObject, MenuAction, MenuEntry, NPC, Scene, Tile, TileObject, WallObject}
import net.runelite.api.events.PostMenuSort
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.events.{GameObjectSpawned, GameTick, MenuEntryAdded, MenuOptionClicked, PostMenuSort}
import net.runelite.api.widgets.Widget
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.menus.MenuManager
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import scala.collection.mutable
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Super Clicker</html>",
	description = "Adds various click menus to some tedious tasks",
	tags = Array("widget", "interface", "click", "helper", "fred4106"),
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class SuperClickerPlugin() extends Plugin {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	@Inject() private val eventBus: EventBus = null
	@Inject() private val client  : Client   = null
	given Client = client

	@Inject() private val menuManager   : MenuManager                   = null
	@Inject() private val overlayManager: OverlayManager                = null
	@Inject() private val config        : SuperClickHelperConfig  = null
	@Inject() private val overlay       : SuperClickHelperOverlay = null


	private val clickedTiles: mutable.ListBuffer[(Int, WorldPoint)] = mutable.ListBuffer.empty
	private val clickedNpcs: mutable.ListBuffer[(Int, NPC)] = mutable.ListBuffer.empty
	def getClickedState: (List[(Int, NPC)], List[(Int, WorldPoint)]) = {
		clickedNpcs.toList -> clickedTiles.toList
	}
	@Provides
	def getConfig(configManager: ConfigManager): SuperClickHelperConfig = {
		configManager.getConfig[SuperClickHelperConfig](classOf[SuperClickHelperConfig])
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
	def onGameTick(event: GameTick): Unit = {
		clickedTiles.flatMapInPlace {
			case (i, point) if i < 20 => Some((i + 1, point))
			case (_, point) => None
		}
		clickedNpcs.flatMapInPlace {
			case (i, point) if i < 20 => Some((i + 1, point))
			case (_, point) => None
		}
	}

	@Subscribe
	def onMenuEntryAdded(menuOptionAdded: MenuEntryAdded): Unit = {
		val targetOpt = MenuEntryTarget(menuOptionAdded)
		targetOpt.foreach(met => log.info("Transformed {} into {}", menuOptionAdded, met))
//		if (targetOpt.isEmpty) {
//			log.debug("Cant handle {} yet", menuOptionAdded)
//		}
	}

	@Subscribe(priority = -15)
	def onPostMenuSort(postMenuSort: PostMenuSort): Unit = {
		if (!client.isMenuOpen) {
			val menuEntries   : List[MenuEntry] = client.getMenuEntries.toList
			val (added, stock)                  = menuEntries.partition(e => {
//				e.getType == MenuAction.RUNELITE && (e.getOption.startsWith("Plant") || e.getOption.startsWith("Water"))
				false
			})
			val newMenuEntries: List[MenuEntry] = stock.appendedAll(added)
			client.setMenuEntries(newMenuEntries.toArray[MenuEntry])
		}
	}

	@Subscribe
	def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked): Unit = {
		val targetOpt = MenuEntryTarget(menuOptionClicked)
		targetOpt.foreach(met => log.info("Transformed {} into {}", menuOptionClicked, met))
		if(targetOpt.isEmpty) {
			log.debug("Cant handle {} yet", menuOptionClicked)
		}

		val me = menuOptionClicked.getMenuEntry
		me.getWorldLocationOpt.foreach(
			worldPoint => {
				clickedTiles.filterInPlace {
					case (_, point) => !point.equals(worldPoint)
				}.addOne((0, worldPoint))
			}
		)
		me.getNpcOpt.foreach(
			npc => {
				clickedNpcs.filterInPlace {
					case (_, n) => !n.equals(npc)
				}.addOne((0, npc))
			}
		)
	}

}

