package com.fredplugins.titheFarm


import com.fredplugins.titheFarm
import com.fredplugins.titheFarm.SPlantState.{Grown, Unwatered, Watered}
import com.fredplugins.titheFarm.SPlantType.Empty
import com.fredplugins.titheFarm.TitheFarmPlugin.log
import com.google.common.base.MoreObjects
import com.google.inject.{Binder, Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{InteractionUtils, InventoryUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{Inventory, TileObjects}
import ethanApiPlugin.collections.query.{ItemQuery, TileObjectQuery}
import lombok.extern.slf4j.Slf4j
import net.codingwell.scalaguice.ScalaModule
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.{GameObjectSpawned, GameTick, MenuEntryAdded, MenuOpened, MenuOptionClicked, PostMenuSort}
import net.runelite.api.widgets.Widget
import net.runelite.api.{Client, GameObject, Item, ItemID, MenuAction, MenuEntry, TileObject}
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.{ConfigChanged, OverlayMenuClicked}
import net.runelite.client.menus.MenuManager
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.JagexColors
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.Color
import java.util
import java.util.function.IntPredicate
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.chaining.*

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Tithe Farm</html>",
	description = "Show timers for the farming patches within the Tithe Farm minigame",
	tags = Array(
		"farming", "minigame"
		, "overlay"
		, "skilling"
		, "timers"
	),
	conflicts = Array(
		"Tithe Farm"
	)
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsTitheFarmPlugin() extends Plugin {
	private val log: Logger = org.slf4j.LoggerFactory.getLogger(classOf[FredsTitheFarmPlugin]).asInstanceOf[ch.qos.logback.classic.Logger].tap(_.setLevel(ch.qos.logback.classic.Level.DEBUG)).asInstanceOf[Logger]

	private val plants: util.HashSet[TitheFarmPlant] = new util.HashSet[TitheFarmPlant]()

	@Inject() private val client        : Client         = null
	@Inject() private val menuManager: MenuManager       = null
	@Inject() private val overlayManager: OverlayManager = null
	@Inject() private val config        : FredsTitheFarmPluginConfig = null
	@Inject() private val overlay       : FredsTitheFarmPlantOverlay = null

	//	private val overlay: FredsTitheFarmPlantOverlay = FredsTitheFarmPlantOverlay(client, this, config)

	@Provides
	def getConfig(configManager: ConfigManager): FredsTitheFarmPluginConfig = {
		configManager.getConfig[FredsTitheFarmPluginConfig](classOf[FredsTitheFarmPluginConfig])
	}
	override protected def startUp(): Unit = {
		plants.clear()
		overlayManager.add(overlay)
		overlay.updateConfig()
	}
	override protected def shutDown(): Unit = {
		plants.clear()
		overlayManager.remove(overlay)
	}
	@Subscribe
	def onConfigChanged(event: ConfigChanged): Unit = {
		if (event.getGroup == ("fredsTitheFarmPlugin")) {
			this.overlay.updateConfig()
		}
	}

	@Subscribe def onGameTick(event: GameTick): Unit = {
		this.plants.removeIf((plant: TitheFarmPlant) => {
			plant.getPlantTimeRelative == 1.0
		})
	}

	@Subscribe def onGameObjectSpawned(event: GameObjectSpawned): Unit = {
		val gameObject: GameObject = event.getGameObject
		SPlantType.getState(gameObject.getId).collect {
			case (tpe, state) => {
				val newPlant: TitheFarmPlant = new TitheFarmPlant(state, tpe, gameObject)
				val oldPlant: TitheFarmPlant = this.getPlantFromCollection(gameObject)
				if (oldPlant == null && (newPlant.getType == SPlantType.Empty)) {
					log.debug("Added plant {}", newPlant)
					this.plants.add(newPlant)
				}
				else {
					if (oldPlant != null) {
						if (newPlant.getType == SPlantType.Empty) {
							log.debug("Removed plant {}", oldPlant)
							this.plants.remove(oldPlant)
						}
						else {
							if (oldPlant.getGameObject.getId != newPlant.getGameObject.getId) {
								if (!(oldPlant.getState.isInstanceOf[SPlantState.Watered]) && newPlant.getState.isInstanceOf[SPlantState.Watered]) {
									log.debug("Updated plant (watered)")
									newPlant.setPlanted(oldPlant.getPlanted)
									this.plants.remove(oldPlant)
									this.plants.add(newPlant)
								}
								else {
									log.debug("Updated plant")
									this.plants.remove(oldPlant)
									this.plants.add(newPlant)
								}
							}
						}
					}
				}
			}
		}
	}

	def wateringCan(query: ItemQuery): List[Widget] = query.withIdFilter {
		(value: Int) => (value >= 5333 && value <= 5340)
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
			val addWaterPatchEntry    : Option[Client => MenuEntry] = SPlantType.values.flatMap(p => p.getStateForId(menuEntryAdded.getIdentifier).map(s => p -> s).toList).collect {
				case (plantType, state@SPlantState.Unwatered(age)) => {
					val wateringCanWidget = wateringCan(Inventory.search()).headOption
					val patchLocation     = WorldPoint.fromScene(client, menuEntryAdded.getActionParam0, menuEntryAdded.getActionParam1, client.getPlane).dx(1).dy(1)
//						val patchObjDef = client.getObjectDefinition(plantType.getIdForState(state))
					val patchObject       = TileObjects.search().withId(plantType.getIdForState(state)).atLocation(patchLocation).result().asScala.toList.headOption
					wateringCanWidget.zip(patchObject)
				}
			}.flatten.map {
				case (w, patch) => {
					(c: Client) => {
						c.createMenuEntry(-1)
							.setOption("Water " + ColorUtil.wrapWithColorTag("Watering Can", Color.BLUE))
							.setTarget(ColorUtil.wrapWithColorTag(s"${c.getObjectDefinition(patch.getId).getName} patch", Color.YELLOW))
							.setType(MenuAction.RUNELITE)
							.onClick((ee) => {
								InteractionUtils.useWidgetOnTileObject(w, patch)
							})
					}
				}
			}.headOption
			val plantSeedsInPatchEntry: Option[Client => MenuEntry] = SPlantType.values.flatMap(p => p.getStateForId(menuEntryAdded.getIdentifier).map(s => p -> s).toList).collect {
				case (plantType@SPlantType.Empty, state@SPlantState.Grown) => {
					val seedWidget   : Option[Widget] = seed(Inventory.search()).headOption
					val patchLocation: WorldPoint = WorldPoint.fromScene(client, menuEntryAdded.getActionParam0, menuEntryAdded.getActionParam1, client.getPlane).dx(1).dy(1)
					val patchObject: Option[TileObject] = TileObjects.search().withId(plantType.getIdForState(state)).atLocation(patchLocation).result().asScala.toList.headOption
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
//		else if(menuEntryAdded.getMenuEntry.getType==MenuAction.WALK && toAddAboveWalkHere.isDefined) {
//			toAddAboveWalkHere.map {
//				case (clientToEntry) =>  {
//					clientToEntry.apply(client)
//				}
//			}.foreach(me => log.trace("Added entry \"{}\"", me))
//			toAddAboveWalkHere = Option.empty
//		}
	}

	@Subscribe(priority = -10)
	def onPostMenuSort(postMenuSort: PostMenuSort): Unit = { 
		// The menu is not rebuilt when it is open, so don't swap or else it will
		// repeatedly swap entries
		if (!client.isMenuOpen) {
			val menuEntries: List[MenuEntry] = client.getMenuEntries.toList
			val (added, stock) = menuEntries.partition(e => {
				e.getType == MenuAction.RUNELITE && (e.getOption.startsWith("Plant") || e.getOption.startsWith("Water"))
			})
			val newMenuEntries: List[MenuEntry] = stock.appendedAll(added)
/*			val oldPrettyPrintStr = menuEntries.zipWithIndex.map((e, i) => {
				s"oldEntries[$i][${newMenuEntries.indexOf(e)}] = ${if(added.contains(e)) "*" else " "} ${e}"
			}).mkString("oldPostMenuSort{\n  ", "\n  ", "\n}")

			val newPrettyPrintStr = newMenuEntries.zipWithIndex.map((e, i) => {
				s"newEntries[$i][${menuEntries.indexOf(e)}] = ${if (added.contains(e)) "*" else " "} ${e}"
			}).mkString("newPostMenuSort{\n  ", "\n  ", "\n}")
			println(oldPrettyPrintStr)
			println(newPrettyPrintStr)*/
			
			client.setMenuEntries(newMenuEntries.toArray[MenuEntry])
		}
//		MenuEntry[] menuEntries = client.getMenuEntries();
//
//		// Build option map for quick lookup in findIndex
//		int idx = 0;
//		optionIndexes.clear();
//		for (MenuEntry entry : menuEntries)
//		{
//			String option = Text.removeTags(entry.getOption()).toLowerCase();
//			optionIndexes.put(option, idx++);
//		}
//
//		// Perform swaps
//		idx = 0;
//		for (MenuEntry entry : menuEntries)
//		{
//			swapMenuEntry(null, menuEntries, idx++, entry);
//		}
//
//		if (config.removeDeadNpcMenus())
//		{
//			removeDeadNpcs();
//		}
	}

	def onMenuOpened(menuOpened: MenuOpened): Unit = {
		println(s"First Entry: ${menuOpened.getFirstEntry}")
		menuOpened.getMenuEntries.toList.zipWithIndex.foreach(me =>
			println(s"  [${me._2}] = ${me._1}")
		)
		def filter(a: MenuEntry): Boolean = {
			(a.getType.getId == MenuAction.RUNELITE.getId && (a.getOption.startsWith("Plant") || a.getOption.startsWith("Water")))
		}
//		val ourIdx = menuOpened.getMenuEntries.indexWhere(filter)
		val (added, stock) = menuOpened.getMenuEntries.toList.partition(filter)
		stock.appendedAll(added).zipWithIndex.foreach {
			case (entry, i) => menuOpened.getMenuEntries(i) = entry
		}
	}

	@Subscribe
	def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked): Unit = {
		log.info("menuOptionClicked: {}", menuOptionClicked)
	}

	def getPlantFromCollection(gameObject: GameObject): TitheFarmPlant = {
		val gameObjectLocation: WorldPoint                    = gameObject.getWorldLocation
		val atLocationList = this.plants.asScala.toList.filter(_.getWorldLocation == gameObjectLocation)
		(if(atLocationList.size != 1) {
			log.warn("expected 1 plant at location {}, but found {}{}", gameObjectLocation, atLocationList.size, atLocationList.toArray)
			Option.empty[TitheFarmPlant]
		} else {
			atLocationList.headOption
		}).orNull.tap(ot => {
			log.debug("found {} at location {}", ot, gameObjectLocation)
		})
	}

	def getPlants: util.Set[TitheFarmPlant] = plants
}
