package com.fredplugins.titheFarm2

import com.fredplugins.common.extensions.MenuExtensions.getWorldLocationOpt
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.titheFarm2
import com.fredplugins.titheFarm2.SPlantInfo.{DryPlantInfo, EmptyPlantInfo}
import com.fredplugins.titheFarm2.TitheFarmLookup.PlantData
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.InteractionUtils
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{Inventory, TileObjects}
import ethanApiPlugin.collections.query.ItemQuery
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.events.{GameObjectSpawned, GameTick, MenuEntryAdded, MenuOptionClicked, PostMenuSort}
import net.runelite.api.widgets.Widget
import net.runelite.api.{Client, GameObject, MenuAction, MenuEntry, TileObject}
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.menus.MenuManager
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger
import lombok.Getter

import java.awt.Color
import scala.collection.mutable
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Tithe Farm V2</html>",
	description = "Show timers for the farming patches within the Tithe Farm minigame",
	tags = Array(
		"farming", "minigame"
		, "overlay"
		, "skilling"
		, "timers"
	),
	conflicts = Array(
		"Tithe Farm",
		"<html><font color=\"#32C8CD\">Freds</font> Tithe Farm</html>"
	)
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsTitheFarmV2Plugin() extends Plugin {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	private val farmLookup: TitheFarmLookup = new TitheFarmLookup
//	private val plants: mutable.Map[WorldPoint, STitheFarmPlant] = mutable.HashMap.empty
	private val clickedTiles: mutable.ListBuffer[(Int, WorldPoint)] = mutable.ListBuffer.empty

	@Inject() private val eventBus: EventBus     = null
	@Inject() private val client        : Client = null
	given Client = client
	@Inject() private val menuManager: MenuManager       = null
	@Inject() private val overlayManager: OverlayManager               = null
	@Inject() private val config        : FredsTitheFarmV2PluginConfig = null
	@Inject() private val overlay       : SFredsTitheFarmV2PlantOverlay  = null


	@Provides
	def getConfig(configManager: ConfigManager): FredsTitheFarmV2PluginConfig = {
		configManager.getConfig[FredsTitheFarmV2PluginConfig](classOf[FredsTitheFarmV2PluginConfig])
	}
	override protected def startUp(): Unit = {
//		plants.clear()
		farmLookup.clear()
//		TitheFarmPatchLoc.values.foreach(loc => {
//			log.debug(s"scanning {}", loc)
//			loc.getGameObject.filter(_ != null).filter(_.getId != -1).foreach(go => {
//				log.debug("\t found {}@({}, {})", go.getId, loc.x, loc.y)
//			})
//		})
		clickedTiles.clear()
		eventBus.register(overlay)
		overlayManager.add(overlay)
		//		overlay.updateConfig()
	}
	override protected def shutDown(): Unit = {
		overlayManager.remove(overlay)
		eventBus.unregister(overlay)
		farmLookup.clear()
//		plants.clear()
		clickedTiles.clear()
	}

	@Subscribe
	def onGameTick(event: GameTick): Unit = {
		clickedTiles.flatMapInPlace {
			case (i, point) if i < 100 => Some((i + 1, point))
			case (_, point) => None
		}
		farmLookup.tick()
	}

	def getFarmLookup: TitheFarmLookup = farmLookup
	def getClickedTiles: List[(Int, WorldPoint)] = clickedTiles.toList

	@Subscribe
	def onGameObjectSpawned(event: GameObjectSpawned): Unit = {
		val  zzz = for {
			go <- Option(event.getGameObject)
			key <- Option(go.getWorldLocation)
			info <- SPlantInfo.lookup(go.getId) if key.getRegionID != 2227
		} farmLookup.putPlantInfo(key, go)
	}

	def wateringCan(query: ItemQuery): List[Widget] = query.withIdFilter {
		(value: Int) => (value >= 5333 && value <= 5340) || value == 13353
	}.result().asScala.toList
	def seed(query: ItemQuery): List[Widget] = {
		query.withIdFilter {
			(value: Int) => List(13423,13424,13425).contains(value)
		}.result().asScala.toList
	}

	import com.fredplugins.common.extensions.MenuExtensions.{getWorldLocation, isRuneliteAction, isTileObjectAction}



	@Subscribe
	def onMenuEntryAdded(menuEntryAdded: MenuEntryAdded): Unit = {

		def addWaterPart1(me: MenuEntry): Option[(Widget, TileObject)] = {
			SPlantInfo.lookup(me.getIdentifier).collect {
				case state@DryPlantInfo(plantType, 0) => {
					val wateringCanWidget = wateringCan(Inventory.search()).headOption
					val patchLocation     = me.getWorldLocation.dx(1).dy(1)
					farmLookup.getPlantData(patchLocation).collect {
						case PlantData(cachedInfo, go, composted, countdown) if (countdown < 100 && countdown > 100 - 3) => wateringCanWidget.map(wcw => wcw -> go)
					}.flatten
				}
			}.flatten
		}

		def plantSeedsInPatchPart1(me: MenuEntry): Option[(Widget, TileObject)] = {
			SPlantInfo.lookup(me.getIdentifier).collect {
				case state@EmptyPlantInfo => {
					val seedWidget   : Option[Widget] = seed(Inventory.search()).headOption
					val patchLocation: WorldPoint     = me.getWorldLocation.dx(1).dy(1) //
					farmLookup.getPlantData(patchLocation).collect {
						case PlantData(cachedInfo, go, _, _) if (cachedInfo == EmptyPlantInfo) => seedWidget.map(swo => swo -> go)
					}.flatten
				}
			}.flatten
		}

		if (menuEntryAdded.getMenuEntry.getType == MenuAction.EXAMINE_OBJECT) {
			val addWaterPatchEntry    : Option[Client => MenuEntry] = addWaterPart1(menuEntryAdded.getMenuEntry).map {
				case (w, patch) => {
					(c: Client) => {
						c.createMenuEntry(-1)
							.setOption("Water " + ColorUtil.wrapWithColorTag("Watering Can", Color.BLUE))
							.setTarget(ColorUtil.wrapWithColorTag(s"${c.getObjectDefinition(patch.getId).getName} patch", Color.YELLOW))
							.setType(MenuAction.RUNELITE)
							.setParam0(menuEntryAdded.getActionParam0)
							.setParam1(menuEntryAdded.getActionParam1)
							.setIdentifier(2428)
							.onClick((ee) => {
								InteractionUtils.useWidgetOnTileObject(w, patch)
							})
					}
				}
			}.headOption

			val plantSeedsInPatchEntry: Option[Client => MenuEntry] = plantSeedsInPatchPart1(menuEntryAdded.getMenuEntry).map {
				case (w, patch) => {
					(c: Client) => {
						val patchName = c.getObjectDefinition(patch.getId).getName
						c.createMenuEntry(-1)
							.setOption("Plant " + ColorUtil.wrapWithColorTag(s"${c.getItemDefinition(w.getItemId).getName}", Color.GREEN))
							.setTarget(ColorUtil.wrapWithColorTag(s"${patchName} patch", Color.YELLOW))
							.setType(MenuAction.RUNELITE)
							.setParam0(menuEntryAdded.getActionParam0)
							.setParam1(menuEntryAdded.getActionParam1)
							.setIdentifier(2428)
							.onClick((ee) => {
								InteractionUtils.useWidgetOnTileObject(w, patch)
							})
					}
				}
			}.headOption

			addWaterPatchEntry.zip(Option("add water")).orElse(plantSeedsInPatchEntry.zip(Option("plant seeds"))).map {
				case (clientToEntry, str) => {
					log.trace("Storing \"{}\" from entry \"{}\"", str, menuEntryAdded)
					clientToEntry.andThen(me => {
						log.trace("Added entry \"{}\"", me)
					})
				}
			}.foreach(_.apply(client))
		}
	}

	@Subscribe(priority = -10)
	def onPostMenuSort(postMenuSort: PostMenuSort): Unit = {
		if (!client.isMenuOpen) {
			val menuEntries: List[MenuEntry] = client.getMenuEntries.toList
			val (added, stock) = menuEntries.partition(e => {
				e.getType == MenuAction.RUNELITE && (e.getOption.startsWith("Plant") || e.getOption.startsWith("Water"))
			})
			val newMenuEntries: List[MenuEntry] = stock.appendedAll(added)
			client.setMenuEntries(newMenuEntries.toArray[MenuEntry])
		}
	}

	@Subscribe
	def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked): Unit = {
		val me = menuOptionClicked.getMenuEntry
		val worldPointOpt: Option[WorldPoint] =
			if(me.isRuneliteAction && me.getIdentifier == 2428) {
				me.setType(MenuAction.EXAMINE_OBJECT).getWorldLocationOpt
			} else if (me.isTileObjectAction) {
				me.getWorldLocationOpt
			} else Option.empty

		worldPointOpt.foreach(wp => {
			clickedTiles.filterInPlace {
				case (_, point) => !point.equals(wp)
			}.addOne((0, wp))
		})
	}

}
