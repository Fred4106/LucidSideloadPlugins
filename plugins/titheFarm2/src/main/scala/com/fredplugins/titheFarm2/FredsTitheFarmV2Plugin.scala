package com.fredplugins.titheFarm2

import com.fredplugins.common.ShimUtils
import com.fredplugins.titheFarm2
import com.fredplugins.titheFarm2.SPlantInfo.{DryPlantInfo, EmptyPlantInfo}
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.InteractionUtils
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{Inventory, TileObjects}
import ethanApiPlugin.collections.query.ItemQuery
import net.runelite.api.coords.WorldPoint
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

import java.awt.Color
import java.time.Instant
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

	private val plants: mutable.Map[WorldPoint, STitheFarmPlant] = mutable.HashMap.empty
	private val clickedTiles: mutable.ListBuffer[(Int, WorldPoint)] = mutable.ListBuffer.empty

	@Inject() private val eventBus: EventBus     = null
	@Inject() private val client        : Client = null
	given Client = client
	@Inject() private val menuManager: MenuManager       = null
	@Inject() private val overlayManager: OverlayManager               = null
	@Inject() private val config        : FredsTitheFarmV2PluginConfig = null
	@Inject() private val overlay       : SFredsTitheFarmV2PlantOverlay  = null

	//	private val overlay: FredsTitheFarmPlantOverlay = FredsTitheFarmPlantOverlay(client, this, config)

	@Provides
	def getConfig(configManager: ConfigManager): FredsTitheFarmV2PluginConfig = {
		configManager.getConfig[FredsTitheFarmV2PluginConfig](classOf[FredsTitheFarmV2PluginConfig])
	}
	override protected def startUp(): Unit = {
		plants.clear()
		clickedTiles.clear()
		eventBus.register(overlay)
		overlayManager.add(overlay)
		//		overlay.updateConfig()
	}
	override protected def shutDown(): Unit = {
		overlayManager.remove(overlay)
		eventBus.unregister(overlay)
		plants.clear()
		clickedTiles.clear()
	}

	@Subscribe def onGameTick(event: GameTick): Unit = {
		clickedTiles.flatMapInPlace {
			case (i, point) if i < 100 => Some((i + 1, point))
			case (_, point) => None
		}
	}

	def getPlants: List[STitheFarmPlant] = plants.values.toList
	def getClickedTiles: List[(Int, WorldPoint)] = clickedTiles.toList

	@Subscribe
	def onGameObjectSpawned(event: GameObjectSpawned): Unit = {
		val gameObject: GameObject = event.getGameObject
		val key = gameObject.getWorldLocation
		SPlantInfo.lookup(event.getGameObject.getId).foreach(info =>{
			println(s"GameObjectSpawned[${event.getGameObject.getId}] = ${info}")
		})
		val transformResult@(removedPlantOpt, addedPlantOpt) = (SPlantInfo.lookup(gameObject.getId).collect {
			case info: SPlantInfo.NotEmptyInfo if plants.get(key).map(_.getAge).contains(info.age) => {
				STitheFarmPlant(info, gameObject, plants(key).planted, plants(key).plantedTick)
			}
			case info: SPlantInfo.NotEmptyInfo => {
				STitheFarmPlant(info, gameObject, Instant.now, client.getTickCount)
			}
		} match {
			case v@Some(value) => plants.put(key, value) -> v
			case None => plants.remove(key) -> None
		})
		Option(transformResult).collect{
			case (Some(removed), Some(added)) => s"Replaced ${removed.debugStr} with ${added.debugStr}"
			case (Some(removed), None) => s"Removed ${removed.debugStr}"
			case (None, Some(added)) => s"Added ${added.debugStr}"
		}.foreach{
			log.debug("{}", _)
		}
	}

	def wateringCan(query: ItemQuery): List[Widget] = query.withIdFilter {
		(value: Int) => (value >= 5333 && value <= 5340) || value == 13353
	}.result().asScala.toList
	def seed(query: ItemQuery): List[Widget] = {
		query.withIdFilter {
			(value: Int) => List(13423,13424,13425).contains(value)
		}.result().asScala.toList
	}

//	var toAddAboveWalkHere: Option[Client => MenuEntry] = Option.empty
	@Subscribe
	def onMenuEntryAdded(menuEntryAdded: MenuEntryAdded): Unit = {
		if (menuEntryAdded.getMenuEntry.getType == MenuAction.EXAMINE_OBJECT) {
			val addWaterPatchEntry    : Option[Client => MenuEntry] = SPlantInfo.lookup(menuEntryAdded.getIdentifier).collect {
				case info@DryPlantInfo(plantType, age) => {
					val wateringCanWidget = wateringCan(Inventory.search()).pipe(wcl => {
						wcl.find(_.getItemId == 13353).orElse(wcl.headOption)
					})
					val patchLocation     = menuEntryAdded.getMenuEntry.getWorldLocation.dx(1).dy(1) /* WorldPoint.fromScene(client, menuEntryAdded.getActionParam0, menuEntryAdded.getActionParam1, client.getPlane).dx(1).dy(1)*/
					//					plants.get(patchLocation)
					//						val patchObjDef = client.getObjectDefinition(plantType.getIdForState(state))
					val patchObject       = TileObjects.search().withId(info.id).atLocation(patchLocation).result().asScala.toList.headOption
					plants.get(patchLocation).map(tpl => if (tpl.getAge == 0) tpl.ticksSincePlanted(client) else -1).filter(j => (1 until 2).contains(j)).zip(wateringCanWidget.zip(patchObject)).map(_._2)
				}
			}.flatten.map {
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

			val plantSeedsInPatchEntry: Option[Client => MenuEntry] = SPlantInfo.lookup(menuEntryAdded.getIdentifier).collect {
				case info@EmptyPlantInfo => {
					val seedWidget   : Option[Widget] = seed(Inventory.search()).headOption
					val patchLocation: WorldPoint     = menuEntryAdded.getMenuEntry.getWorldLocation.dx(1).dy(1) //
					println(s"info: ${EmptyPlantInfo}, id: ${info.id}, identifier: ${menuEntryAdded.getIdentifier}")
					val patchObject: Option[TileObject] = TileObjects.search().withId(info.id).atLocation(patchLocation).result().asScala.toList.headOption
					println(s"seedWidget: ${seedWidget}, patchLocation: ${patchLocation}, patchObj: ${patchObject}")
					seedWidget.zip(patchObject)
				}
			}.flatten.map {
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
		log.info("menuOptionClicked: {}", menuOptionClicked)
		if (me.isGameObjectAction || me.isRuneliteAction) {
			me.getWorldLocation.tap(pt => {
				clickedTiles.filterInPlace {
					case (_, point) => !point.equals(pt)
				}.addOne((0, pt))
			})
		}
	}

}
