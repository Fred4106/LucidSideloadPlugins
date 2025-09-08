package com.fredplugins.pvmDebugger.amoxliatl

import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.amoxliatl.FredsAmoxliatlHelper.Amoxliatl
import com.fredplugins.pvmDebugger.amoxliatl.FredsAmoxliatlHelper.UnstableIce
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.services.localPlayer.events.LocalDestinationChanged
import ethanApiPlugin.services.localPlayer.events.LocalPositionChanged
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.Actor
import net.runelite.api.Client
import net.runelite.api.GameObject
import net.runelite.api.GameState
import net.runelite.api.GraphicsObject
import net.runelite.api.NPC
import net.runelite.api.NPCComposition
import net.runelite.api.Perspective
import net.runelite.api.Point
import net.runelite.api.Prayer
import net.runelite.api.WorldView
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.AnimationChanged
import net.runelite.api.events.GameObjectDespawned
import net.runelite.api.events.GameObjectSpawned
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.GameTick
import net.runelite.api.events.GraphicsObjectCreated
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.gameval.AnimationID
import net.runelite.api.gameval.NpcID
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.util.ColorUtil

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.Shape
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.chaining.scalaUtilChainingOps

object FredsAmoxliatlHelper {

	sealed trait NpcWrapped(val wrapped: NPC) {
		def getId: Int = wrapped.getId
		def getName: String = wrapped.getName
		def getCombatLevel: Int = wrapped.getCombatLevel
		def getIndex: Int = wrapped.getIndex
		def getComposition: NPCComposition = wrapped.getComposition
		def getTransformedComposition: NPCComposition = wrapped.getTransformedComposition
		def getWorldView: WorldView = wrapped.getWorldView
		def isInteracting: Boolean = wrapped.isInteracting
		def getInteracting: Actor = wrapped.getInteracting
		def getHealthRatio: Int = wrapped.getHealthRatio
		def getHealthScale: Int = wrapped.getHealthScale
		def getWorldLocation: WorldPoint = wrapped.getWorldLocation
		def getLocalLocation: LocalPoint = wrapped.getLocalLocation
		def getOrientation: Int = wrapped.getOrientation
		def getCurrentOrientation: Int = wrapped.getCurrentOrientation
		def getAnimation: Int = wrapped.getAnimation
//		def getPoseAnimation: Int = wrapped.getPoseAnimation
//		def getPoseAnimationFrame: Int = wrapped.getPoseAnimationFrame
//		def getIdlePoseAnimation: Int = wrapped.getIdlePoseAnimation
//		def getIdleRotateLeft: Int = wrapped.getIdleRotateLeft
//		def getIdleRotateRight: Int = wrapped.getIdleRotateRight
//		def getWalkAnimation: Int = wrapped.getWalkAnimation
//		def getWalkRotateLeft: Int = wrapped.getWalkRotateLeft
//		def getWalkRotateRight: Int = wrapped.getWalkRotateRight
//		def getWalkRotate180: Int = wrapped.getWalkRotate180
//		def getRunAnimation: Int = wrapped.getRunAnimation
//		def getAnimationFrame: Int = wrapped.getAnimationFrame
		def getCanvasTilePoly: Polygon = wrapped.getCanvasTilePoly
		def getMinimapLocation: Point = wrapped.getMinimapLocation
		def getLogicalHeight: Int = wrapped.getLogicalHeight
		def getConvexHull: Shape = wrapped.getConvexHull
		def getWorldArea: WorldArea = wrapped.getWorldArea
		def getOverheadText: String = wrapped.getOverheadText
		def getOverheadCycle: Int = wrapped.getOverheadCycle
		def isDead: Boolean = wrapped.isDead
//		def getAnimationHeightOffset: Int = wrapped.getAnimationHeightOffset
		override def toString: String = s"${this.getClass.getSimpleName}(${wrapped})"

		override def hashCode(): Int = {
			wrapped.hashCode()
		}

		override def equals(obj: Any): Boolean = {
			if(!super.equals(obj)) {
				obj match {
					case wrapper: NpcWrapped if(wrapper.getClass == this.getClass) => wrapper.wrapped == wrapped
					case _ => false
				}
			} else {true}
		}
	}

	final class Amoxliatl protected[FredsAmoxliatlHelper](w: NPC) extends NpcWrapped(w) {}
	final class UnstableIce protected[FredsAmoxliatlHelper](w: NPC) extends NpcWrapped(w) {}

	object Amoxliatl {
		def unapply(arg: Actor): Option[Amoxliatl] = {
			Option(arg).collect {
				case npc: NPC if npc.getId == NpcID.AMOXLIATL => Amoxliatl(npc)
			}
		}
	}

	object UnstableIce {
		def unapply(arg: Actor): Option[UnstableIce] = {
			Option(arg).collect {
				case npc: NPC if npc.getId == NpcID.AMOXLIATL_ICE_BLOCK => UnstableIce(npc)
			}
		}
	}

	object NpcWrapped {
		def unapply(arg: Actor): Option[NpcWrapped] = {
			Option(arg).collect {
				case npc: NPC if npc.getId == NpcID.AMOXLIATL_ICE_BLOCK => new UnstableIce(npc)
				case npc: NPC if npc.getId == NpcID.AMOXLIATL => new Amoxliatl(npc)
			}
		}
	}
}

@Singleton
class FredsAmoxliatlHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsAmoxliatlConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsAmoxliatlConfig.GROUP
	private def clientThread  = parent.getClientThread

	case class AmoxliatlData(spawnedTick: Int, lastAttackTick: Int)
	case class IceData(spawnedTick: Int)

	//	private var ticks         = -1
	private val bossData: mutable.Map[Amoxliatl, AmoxliatlData] = mutable.HashMap.empty[Amoxliatl, AmoxliatlData]
	private val iceBlocks: mutable.Map[UnstableIce,IceData] =mutable.HashMap.empty[UnstableIce, IceData]
	private val iceTiles: mutable.Set[GameObject] = mutable.HashSet.empty[GameObject]
	private val dangerousTiles: mutable.HashMap[GraphicsObject, GObjectData] = mutable.HashMap.empty[GraphicsObject, GObjectData]

//	var currentRoom: Option[MoonRoomEnum] = None
//	var currentRoomChangedTick: Int = -1

	override def init(): Unit = {
		dangerousTiles.clear()
		iceTiles.clear()
		bossData.clear()
		iceBlocks.clear()
//		currentRoom = None
//		currentRoomChangedTick = -1
	}

	override def cleanup(): Unit = {
//		currentRoom = None
//		currentRoomChangedTick = -1
		dangerousTiles.clear()
		iceTiles.clear()
		bossData.clear()
		iceBlocks.clear()
	}

	case class GObjectData(spawnedTick: Int, spawnedCycle: Int, id: Int, lp: LocalPoint) {
		def age: Int = (client.getTickCount-spawnedTick)
	}

//	var graphicsObjects: List[GObjectData] = List.empty[GObjectData]
	@Subscribe
	def onGameStateChanged(event: GameStateChanged): Unit = {
		val gameState = event.getGameState
		if (gameState == GameState.LOADING) {
//			dangerousTiles.clear()
//			iceTiles.clear()
//			bossData.clear()
//			iceBlocks.clear()
		}
	}

	@Subscribe
	def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
		if (bossData.nonEmpty) {
			val graphicsObject = event.getGraphicsObject
			dangerousTiles.put(
				graphicsObject,
				GObjectData(
					client.getTickCount,
					client.getGameCycle,
					graphicsObject.getId,
					graphicsObject.getLocation
				)
			)
		}
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		if(bossData.nonEmpty && !(client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC) && client.isPrayerActive(Prayer.PIETY))) {
			CombatUtils.activatePrayers(Prayer.PROTECT_FROM_MAGIC, Prayer.PIETY)
		}
		if(bossData.isEmpty && (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC) || client.isPrayerActive(Prayer.PIETY))) {
			CombatUtils.deactivatePrayers(Prayer.PROTECT_FROM_MAGIC, Prayer.PIETY)
		}

		//dangerousTiles.toMap.keySet.filter(_.finished()).toList
		val toRemove: List[GraphicsObject] = dangerousTiles.toList.filter((k, d) => k.finished() || d.age >=4).map(_._1)
		val removed: List[GObjectData]                                  = toRemove.flatMap(k => dangerousTiles.remove(k))
		removed.groupBy(_.id).flatMap((k, v) => v.map(_.age).distinct.map(a => k -> a)).toList.sortBy(e => e._1 * 1000 + e._2)
			.foreach{
				(spotId, age) => log.debug(s"Removed spot ${spotId} after ${age} ticks")
			}
		dangerousTiles.values.toList
			.filter(x => x.spawnedTick == client.getTickCount)
			.foreach(go => {
				log.debug(s"Spawned spot ${go.id} @ ${go.lp.pipe(u => u.getSceneX -> u.getSceneY)}")
			})
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
	}

	@Subscribe
	def onLocalDestinationChanged(e: LocalDestinationChanged): Unit = {
		if(bossData.nonEmpty) log.info(s"Destination changed from ${e.getFrom} to ${e.getTo}")
	}

	@Subscribe
	def onLocalPositionChanged(e: LocalPositionChanged): Unit = {
		if(bossData.nonEmpty) log.info(s"Position changed from ${e.getFrom} to ${e.getTo}")
	}

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		e.getNpc match {
			case Amoxliatl(amox) => {
				log.debug(s"Amoxliatl ${amox} spawned at ${amox.getWorldLocation}")
				bossData.put(amox, AmoxliatlData(client.getTickCount, client.getTickCount))
			}
			case UnstableIce(ice) => {
				log.debug(s"UnstableIce ${ice} spawned at ${ice.getWorldLocation}")
				iceBlocks.put(ice, IceData(client.getTickCount))
			}
			case _ =>
		}
	}
	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		e.getNpc match {
			case Amoxliatl(amox) => {
				log.debug(s"Amoxliatl ${amox} despawned at ${amox.getWorldLocation}")
				bossData.remove(amox)
			}
			case UnstableIce(ice) => {
				log.debug(s"UnstableIce ${ice} despawned at ${ice.getWorldLocation}")
				iceBlocks.remove(ice)
			}
			case _ =>
		}
	}

	@Subscribe
	def onGameObjectSpawned(e: GameObjectSpawned): Unit = {
		if (e.getGameObject.getId == 54279) {
			iceTiles.add(e.getGameObject)
		}
	}

	@Subscribe
	def onGameObjectDespawned(e: GameObjectDespawned): Unit = {
		iceTiles.remove(e.getGameObject)
//		if(iceTiles.contains(e.getGameObject)) {
//			iceTiles.remove(e.getGameObject)
//		}
	}

	def decodeAnimationId(id: Int): String = {
		id match {
			case -1 => "IDLE"
			case 11527 => "AMOXLIATL_SPAWN"
			case 11528 => "AMOXLIATL_IDLE"
			case 11529 => "AMOXLIATL_WALK"
			case 11530 => "AMOXLIATL_ATTACK"
			case 11531 => "AMOXLIATL_POINT"
			case 11532 => "AMOXLIATL_DEATH"
			case 11533 => "AMOXLIATL_SUMMON"
			case 11534 => "AMOXLIATL_ICE_DESTROY"
			case 11535 => "AMOXLIATL_PET_ATTACK"
			case 11536 => "AMOXLIATL_UNSTABLE_ICE_SPAWN"
			case 11537 => "VFX_AMOXLIATL_ICE_BLOCK_PROJECTILE_IMPACT_01"
			case 11538 => "VFX_AMOXLIATL_ICE_BLOCK_PROJECTILE_01"
			case 11539 => "VFX_AMOXLIATL_CRACK_APPEAR"
			case 11540 => "VFX_AMOXLIATL_CRACK_DISAPPEAR"
			case 11541 => "VFX_AMOXLIATL_ICE_FLOOR_SPAWN_01"
			case 11542 => "VFX_AMOXLIATL_ICE_FLOOR_DESPAWN_01"
			case 11543 => "VFX_AMOXLIATL_ICE_FLOOR_COMBINED_01"
			case 11544 => "VFX_AMOXLIATL_CRACK_APPEAR_02"
			case 11545 => "VFX_AMOXLIATL_CRACK_APPEAR_03"
			case 11546 => "VFX_AMOXLIATL_CRACK_APPEAR_04"
			case 11547 => "VFX_AMOXLIATL_CRACK_DISAPPEAR_02"
			case 11548 => "VFX_AMOXLIATL_CRACK_DISAPPEAR_03"
			case 11549 => "VFX_AMOXLIATL_CRACK_DISAPPEAR_04"
			case 11550 => "VFX_AMOXLIATL_ICE_FLOOR_SINGLE"
			case _ => s"UNKOWN(${id})"
		}
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		Option(e.getActor).foreach {
			case Amoxliatl(amox) => {
				log.debug(s"Amoxliatl animation changed to ${decodeAnimationId(amox.getAnimation)}")
				Option(amox.getAnimation).foreach {
					case AnimationID.AMOXLIATL_SUMMON | AnimationID.AMOXLIATL_ATTACK | AnimationID.AMOXLIATL_POINT => bossData.updateWith(amox)(_.map(_.copy(lastAttackTick =  client.getTickCount)))
					case AnimationID.AMOXLIATL_SPAWN => bossData.updateWith(amox)(_.map(_.copy(spawnedTick = client.getTickCount)))
					case _ =>
				}
			}
			case UnstableIce(ice) => {
				log.debug(s"UnstableIce animation changed to ${decodeAnimationId(ice.getAnimation)}")
				Option(ice.getAnimation).foreach {
					case AnimationID.AMOXLIATL_UNSTABLE_ICE_SPAWN => iceBlocks.updateWith(ice)(_.map(_.copy(spawnedTick = client.getTickCount)))
					case _ =>
				}
			}
			case _ =>
		}
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		if(bossData.nonEmpty) {
			val (amox, amoxdata) = bossData.head
			val bossLine = LineComponent.builder().left(amox.toString).right(amoxdata.toString).rightColor(if(client.getTickCount - amoxdata.lastAttackTick > 6) Color.RED else Color.BLUE).build

			val iceLines = iceBlocks.toList.zipWithIndex.map((b,idx) => {
				LineComponent.builder().left(s"Ice[${idx.toString.padTo(2, ' ')}] ${b._1.getIndex}").right(b._2.toString).leftColor(
					if(b._1.isDead) {
						Color.RED
					} else Color.GREEN
				).rightColor(ColorUtil.colorLerp(Color.RED, Color.GREEN, Math.min(1.0d, Math.max(0.0d,(client.getTickCount - b._2.spawnedTick).toDouble/15.0d)))).build
			}).pipe(ibl => if(ibl.nonEmpty) ibl.prepended(TitleComponent.builder().text("Unstable Ice").color(Color.CYAN).build()) else ibl)

			Seq(bossLine, iceLines).flatMap{
				case e: LayoutableRenderableEntity => Seq(e)
				case le: Seq[_] => le.collect{
					case e: LayoutableRenderableEntity => e
				}
			}
		} else {
			Seq.empty[LayoutableRenderableEntity]
		}
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		def renderNpcOverlay(n:NPC, text: String, color: Color, zoffset: Int): Unit = {
			parent.getModelOutlineRenderer.drawOutline(n, 2,  color, 4)
			val poly = n.getCanvasTilePoly
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
			val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
			if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}

		def renderDangerousTile(tile: GObjectData, color: Color): Unit = {
			val poly = Perspective.getCanvasTilePoly(client, tile.lp)
			if (poly != null) OverlayUtil.renderPolygon(g, poly, ColorUtil.colorWithAlpha(color, 200))

			val ppc = new ProgressPieComponent()
			ppc.setBorderColor(Color.BLACK)
			ppc.setFill(Color.RED)
			ppc.setProgress((client.getGameCycle - tile.spawnedCycle).doubleValue / (30.0d * 4))
			ppc.setDiameter(28)
			val point = Perspective.localToCanvas(client, tile.lp, client.getTopLevelWorldView.getPlane, 20)
			ppc.setPosition(point)
			ppc.render(g)

			val text = s"${tile.age}"

			val fm      = g.getFontMetrics()
			val bounds  = fm.getStringBounds(text, g)
			val xOffset = point.getX() - (bounds.getWidth() / 2).toInt
			val yOffset = point.getY() + (bounds.getHeight() / 2).toInt

			val textPoint = new Point(xOffset, yOffset)
			OverlayUtil.renderTextLocation(g, textPoint, text, Color.WHITE)
		}

		def renderIceTile(tile: GameObject, color: Color): Unit = {
			val lp   = tile.getLocalLocation
			val poly = Perspective.getCanvasTilePoly(client, lp)
			if (poly != null) OverlayUtil.renderPolygon(g, poly, ColorUtil.colorWithAlpha(color, 200))

//			val text = s"${tile.age}"

//			val fm      = g.getFontMetrics()
//			val bounds  = fm.getStringBounds(text, g)
//			val xOffset = point.getX() - (bounds.getWidth() / 2).toInt;
//			val yOffset = point.getY() + (bounds.getHeight() / 2).toInt;
//
//			val textPoint = new Point(xOffset, yOffset)
//			OverlayUtil.renderTextLocation(g, textPoint, text, Color.BLACK)
		}

		if(bossData.nonEmpty) {
			dangerousTiles.values.toList.foreach(dt => {
				renderDangerousTile(dt, Color.ORANGE)
			})
			iceTiles.toList.foreach(iceTile => {
				renderIceTile(iceTile, Color.RED)
			})

			bossData.foreach((wrapped, data) => {
				val txt  = s"age: ${client.getTickCount - data.spawnedTick}, ticksSinceAttack: ${client.getTickCount - data.lastAttackTick}"
				renderNpcOverlay(wrapped.wrapped, txt, Color.CYAN, 60)
			})
			iceBlocks.foreach((wrapped, data) => {
				val txt = s"age: ${client.getTickCount - data.spawnedTick}"
				renderNpcOverlay(wrapped.wrapped, txt, Color.BLUE, 60)
			})
		}
		null.asInstanceOf[Dimension]
	}
}
