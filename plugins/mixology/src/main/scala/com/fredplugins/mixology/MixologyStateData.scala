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
import scala.::
import scala.jdk.CollectionConverters.*
import scala.swing.Color
import scala.util.Try
import scala.util.chaining.*

trait MixologyStateWriter {
	def alembicPotionType_=(value: PotionType): Unit
	def agitatorPotionType_=(value: PotionType): Unit
	def retortPotionType_=(value: PotionType): Unit

	def agitatorProgess_=(value: Int): Unit
	def agitatorQuickActionTicks_=(value: Int): Unit
	def alembicProgress_=(value: Int): Unit
	def alembicQuickActionTicks_=(value: Int): Unit
	def retortProgress_=(value: Int): Unit

	def inLab_=(b: Boolean): Unit
	def clearPotionOrders(): Unit
	def setPotionOrders(o1: PotionOrder, o2: PotionOrder, o3: PotionOrder): Unit
	def clearHighlightObject(): Unit

	def highlightObject(alchemyObject: AlchemyObject, color: Color): Unit
	def unHighlightObject(alchemyObject: AlchemyObject): Unit
}

class MixologyStateData(using client: Client, clientThread: ClientThread, config: FredsMixologyConfig) extends ShimUtils.Logging("DEBUG") {
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

	private[MixologyStateData] var _alembicPotionType: PotionType = null
	private[MixologyStateData] var _agitatorPotionType: PotionType = null
	private[MixologyStateData] var _retortPotionType: PotionType = null
	private[MixologyStateData] var _agitatorProgess: Int = 0
	private[MixologyStateData] var _agitatorQuickActionTicks: Int = 0
	private[MixologyStateData] var _alembicProgress: Int = 0
	private[MixologyStateData] var _alembicQuickActionTicks: Int = 0
	private[MixologyStateData] var _retortProgress: Int = 0

	def alembicPotionType: Option[PotionType] = Option(_alembicPotionType)
	def agitatorPotionType: Option[PotionType] = Option(_agitatorPotionType)
	def retortPotionType: Option[PotionType] = Option(_retortPotionType)
	def agitatorProgess: Int = _agitatorProgess
	def agitatorQuickActionTicks: Int = _agitatorQuickActionTicks
	def alembicProgress: Int = _alembicProgress
	def alembicQuickActionTicks: Int = _alembicQuickActionTicks
	def retortProgress: Int = _retortProgress

	def getWriter(): MixologyStateWriter = this.Writer

	object Writer extends MixologyStateWriter {
		def alembicPotionType_=(value: PotionType): Unit = _alembicPotionType = value
		def agitatorPotionType_=(value: PotionType): Unit = _agitatorPotionType = value
		def retortPotionType_=(value: PotionType): Unit = _retortPotionType = value

		def agitatorProgess_=(value: Int): Unit = _agitatorProgess = value
		def agitatorQuickActionTicks_=(value: Int): Unit = _agitatorQuickActionTicks = value
		def alembicProgress_=(value: Int): Unit = _alembicProgress = value
		def alembicQuickActionTicks_=(value: Int): Unit = _alembicQuickActionTicks = value
		def retortProgress_=(value: Int): Unit = _retortProgress = value

		def inLab_=(b: Boolean): Unit = _inLab = b

		def clearPotionOrders(): Unit = _potionOrders = null
		def setPotionOrders(o1: PotionOrder, o2: PotionOrder, o3: PotionOrder): Unit = {
			_potionOrders = (o1, o2, o3)
		}
		def clearHighlightObject(): Unit = {
			_highlightedObjects.clear()
		}

		def highlightObject(alchemyObject: AlchemyObject, color: Color): Unit = {
			(for{
				wv: WorldView <- Option(client.getTopLevelWorldView)
				lp: LocalPoint <- Option(LocalPoint.fromWorld(wv, alchemyObject.coordinate()))
				tile: Tile <- Try(wv.getScene.getTiles()(wv.getPlane)(lp.getSceneX)(lp.getSceneY)).toOption
				obj: TileObject <- tile.getGameObjects.toList.appended(tile.getDecorativeObject).filter(_ != null)
				if(obj.getId == alchemyObject.objectId)
				highlighted = new FredsMixologyPlugin.HighlightedObject(obj, color, config.highlightBorderWidth(), config.highlightFeather())
			} yield highlighted).headOption
				.foreach(ho => _highlightedObjects.put(alchemyObject, ho))
		}

		def unHighlightObject(alchemyObject: AlchemyObject): Unit = {
			_highlightedObjects.remove(alchemyObject)
		}
	}
}
