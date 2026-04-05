package com.fredplugins.mixology

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.mixology.AlchemyObject.{AGA_LEVER, AGITATOR, ALEMBIC, CONVEYOR_BELT, DIGWEED_NORTH_EAST, DIGWEED_NORTH_WEST, DIGWEED_SOUTH_EAST, DIGWEED_SOUTH_WEST, HOPPER, LYE_LEVER, MIXING_VESSEL, MOX_LEVER, RETORT}
import com.fredplugins.mixology.PotionComponent.{AGA, LYE, MOX}
import com.google.common.collect.{ImmutableList, ImmutableMap}
import net.runelite.api.coords.LocalPoint
import net.runelite.api.widgets.{Widget, WidgetPositionMode, WidgetTextAlignment, WidgetType}
import net.runelite.api.{Client, FontID, Tile, TileObject, WorldView}
import net.runelite.client.callback.ClientThread

import java.util.stream.{Collector, Collectors}
import scala.annotation.targetName
import scala.jdk.CollectionConverters.*
import scala.swing.Color
import scala.util.Try
import scala.util.chaining.*

trait MixologyStateWriter {
	def config: FredsMixologyConfig
	def alembicPotionType_=(value: PotionType): Unit
	def agitatorPotionType_=(value: PotionType): Unit
	def retortPotionType_=(value: PotionType): Unit

	def inLab_=(b: Boolean): Unit
	def clearPotionOrders(): Unit
	def setPotionOrders(o1: PotionOrder, o2: PotionOrder, o3: PotionOrder): Unit
	def clearHighlightObject(): Unit

	def highlightObject(alchemyObject: AlchemyObject, color: Color): Unit
	def unHighlightObject(alchemyObject: AlchemyObject): Unit

	def agitatorQuickActionTicks_=(value: Int): Unit
	def alembicQuickActionTicks_=(value: Int): Unit
	def handleAgitatorProgress(value: Int): Unit
	def handleAlembicProgress(value: Int): Unit

	private val stations = List(RETORT, ALEMBIC, AGITATOR)
	private val levers = List(LYE_LEVER, AGA_LEVER, MOX_LEVER)
	private val comps = List(LYE, AGA, MOX)
	private val digweeds = List(DIGWEED_NORTH_EAST, DIGWEED_SOUTH_EAST, DIGWEED_SOUTH_WEST, DIGWEED_NORTH_WEST)
	def highlightStation(station: AlchemyObject): Unit = if (stations.contains(station) && config.highlightStations) highlightObject(station, config.stationHighlightColor)
	def unHighlightAllStations(): Unit = stations.foreach(unHighlightObject(_))

	def highlightLevers(): Unit = if(config.highlightLevers()) levers.zip(comps.map(_.color)).foreach(highlightObject(_,_))
	def unHighlightLevers(): Unit = levers.foreach(unHighlightObject(_))

	def toggleDigweed(weed: AlchemyObject, v: Boolean): Unit = if (digweeds.contains(weed)) {
		unHighlightObject(weed)
		if(config.highlightDigWeed() && v) highlightObject(weed, config.digweedHighlightColor())
	}
	def unHighlightDigweeds(): Unit = {
		digweeds.foreach(unHighlightObject(_))
	}
}

class MixologyStateData(using client: Client, clientThread: ClientThread, config: FredsMixologyConfig) extends ShimUtils.Logging("DEBUG") {
	reader =>
	private[MixologyStateData] val _highlightedObjects: collection.mutable.Map[AlchemyObject, FredsMixologyPlugin.HighlightedObject] = collection.mutable.LinkedHashMap[AlchemyObject, FredsMixologyPlugin.HighlightedObject]()
	def highlightedObjects: Map[AlchemyObject, FredsMixologyPlugin.HighlightedObject] = _highlightedObjects.toMap
	def highlightedObjectsJava: java.util.Map[AlchemyObject, FredsMixologyPlugin.HighlightedObject] = {
		highlightedObjects.asJava
	}

	private[MixologyStateData] var _potionOrders: (PotionOrder, PotionOrder, PotionOrder) = null
	def potionOrders: Option[(PotionOrder, PotionOrder, PotionOrder)] = Option(_potionOrders)
	def potionOrdersAsJava: java.util.List[PotionOrder] = potionOrders.map(t => List(t._1, t._2, t._3)).map(_.asJava.stream().collect(Collectors.toUnmodifiableList)).getOrElse(ImmutableList.of())

	private[MixologyStateData] var _inLab: Boolean = false
	def inLab: Boolean = _inLab

	private var _alembicPotionType: PotionType = null
	private var _agitatorPotionType: PotionType = null
	private var _retortPotionType: PotionType = null
	private var _agitatorProgess: Int = 0
	private var _agitatorQuickActionTicks: Int = 0
	private var _alembicProgress: Int = 0
	private var _alembicQuickActionTicks: Int = 0
	private var _retortProgress: Int = 0

	def alembicPotionType: Option[PotionType] = Option(_alembicPotionType)
	def agitatorPotionType: Option[PotionType] = Option(_agitatorPotionType)
	def retortPotionType: Option[PotionType] = Option(_retortPotionType)

	def alembicPotionTypeJava: PotionType =alembicPotionType.orNull
	def agitatorPotionTypeJava: PotionType = agitatorPotionType.orNull
	def retortPotionTypeJava: PotionType = retortPotionType.orNull

	def agitatorProgess(): Int = _agitatorProgess
	def agitatorQuickActionTicks(): Int = _agitatorQuickActionTicks
	def alembicProgress(): Int = _alembicProgress
	def alembicQuickActionTicks(): Int = _alembicQuickActionTicks
	def retortProgress(): Int = _retortProgress

	def writer(): MixologyStateWriter = this.Writer

	object Writer extends MixologyStateWriter {
		override def config: FredsMixologyConfig = MixologyStateData.this.config
		def alembicPotionType_=(value: PotionType): Unit = _alembicPotionType = value
		def agitatorPotionType_=(value: PotionType): Unit = _agitatorPotionType = value
		def retortPotionType_=(value: PotionType): Unit = _retortPotionType = value

//		@targetName("setAgitatorProgess")
//		override def agitatorProgess_=(value: Int): Unit = _agitatorProgess = value
//		@targetName("setAgitatorQuickActionTicks")
		def agitatorQuickActionTicks_=(value: Int): Unit =
			_agitatorQuickActionTicks = value
//		@targetName("setAlembicProgress")
//		def alembicProgress_=(value: Int): Unit = _alembicProgress = value
//		@targetName("setAlembicQuickActionTicks")
		def alembicQuickActionTicks_=(value: Int): Unit =
			_alembicQuickActionTicks = value
//		@targetName("setRetortProgress")
//		def retortProgress_=(value: Int): Unit = _retortProgress = value

		def inLab_=(b: Boolean): Unit = _inLab = b

		def clearPotionOrders(): Unit = _potionOrders = null
		def setPotionOrders(o1: PotionOrder, o2: PotionOrder, o3: PotionOrder): Unit = {
			_potionOrders = (o1, o2, o3)
		}
		def clearHighlightObject(): Unit = {
			_highlightedObjects.clear()
		}

		def highlightObject(alchemyObject: AlchemyObject, color: Color): Unit = {
			val h = for{
				wv: WorldView <- Option(client.getTopLevelWorldView)
				lp: LocalPoint <- Option(LocalPoint.fromWorld(wv, alchemyObject.coordinate()))
				tile: Tile <- Try(wv.getScene.getTiles()(wv.getPlane)(lp.getSceneX)(lp.getSceneY)).toOption
				obj: TileObject <- tile.getGameObjects.toList.appended(tile.getDecorativeObject).filter(_ != null).find(_.getId == alchemyObject.objectId)
			} yield new FredsMixologyPlugin.HighlightedObject(obj, color, config.highlightBorderWidth(), config.highlightFeather())
			h.foreach(_highlightedObjects.put(alchemyObject, _))
		}

		def unHighlightObject(alchemyObject: AlchemyObject): Unit = {
			_highlightedObjects.remove(alchemyObject)
		}

		def handleAgitatorProgress(value: Int): Unit = {
			if(agitatorQuickActionTicks() == 2){
				highlightStation(AGITATOR)
				_agitatorQuickActionTicks = 0
			}
			if(agitatorQuickActionTicks() == 1) {
				_agitatorQuickActionTicks = 2
			}

			if (value < agitatorProgess()) {
				// progress was set back due to a quick action failure
				highlightStation(AGITATOR)
			}
			_agitatorProgess = value
		}

		def handleAlembicProgress(value: Int): Unit = {
			if (alembicQuickActionTicks() == 1) {
				highlightStation(ALEMBIC)
				_alembicQuickActionTicks = 0
			}

			if (value < alembicProgress()) {
				// progress was set back due to a quick action failure
				highlightStation(ALEMBIC)
			}
			_alembicProgress = value
		}
	}
}
