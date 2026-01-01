package com.fredplugins.pvmDebugger.shellsbane

import com.fredplugins.common.ProjectileID
import com.fredplugins.common.extensions.ActorExtensions
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
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
import net.runelite.api.Player
import net.runelite.api.Point
import net.runelite.api.Prayer
import net.runelite.api.Projectile
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
import net.runelite.api.events.ProjectileMoved
import net.runelite.api.gameval.AnimationID
import net.runelite.api.gameval.NpcID
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
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
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

class Shellsbane(val wrapped: NPC) {
	assert(wrapped != null && wrapped.getId == NpcID.GRYPHON_BOSS)
	var lastAttack: Int = -1
	var lastAnimation: Int = -1
	var spawnTick: Int = -1
}
object Shellsbane {
	def tryBuild(arg: Actor): Option[Shellsbane] = {
		Option(arg).collect {
			case npc: NPC if npc.getId == NpcID.GRYPHON_BOSS => Shellsbane(npc)
		}
	}
}
@Singleton
class FredsShellsbaneHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsShellsbaneConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsShellsbaneConfig.GROUP
	private def clientThread  = parent.getClientThread
	given Client = client

	val ShellsbaneRegion: Int = 12682
	case class WhirlwindData(spawnedTick: Int)

	//	private var ticks         = -1
	private var curRegion = -1;

	var boss: Shellsbane = uninitialized
	var projectiles: List[Projectile] = List.empty[Projectile]

	private def clearState(): Unit = {
		boss = null
		projectiles = List.empty[Projectile]
	}

	override def init(): Unit = {
		curRegion = Option(client.getLocalPlayer).map(_.templateLocation).map(_.getRegionID).getOrElse(-1)
		clearState()
//		currentRoom = None
//		currentRoomChangedTick = -1
	}

	override def cleanup(): Unit = {
		curRegion = -1
		clearState()
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		projectiles = projectiles.filterNot(_.hasHit)
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
		if(e.getOldRegion == ShellsbaneRegion) {
			clearState()
		}
		curRegion = e.getCurRegion
	}

	@Subscribe
	def onLocalDestinationChanged(e: LocalDestinationChanged): Unit = {
		if(curRegion == ShellsbaneRegion) log.info(s"Destination changed from ${e.getFrom} to ${e.getTo}")
	}

	@Subscribe
	def onLocalPositionChanged(e: LocalPositionChanged): Unit = {
		if(curRegion == ShellsbaneRegion) log.info(s"Position changed from ${e.getFrom} to ${e.getTo}")
	}

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		if(curRegion == ShellsbaneRegion) {
			Shellsbane.tryBuild(e.getActor)
				.foreach{sb =>
					boss = sb.tap(_.spawnTick = client.getTickCount)
				}
		}
	}

	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		if(curRegion == ShellsbaneRegion) {
			if(Shellsbane.tryBuild(e.getActor).map(_.wrapped).contains(boss.wrapped)) {
				clearState()
			}
		}
	}

	@Subscribe
	def onProjectileMoved(e: ProjectileMoved): Unit = {
		val projectile: Projectile = e.getProjectile
		if (boss != null && !projectiles.contains(projectile) &&
			(e
				.getProjectile
				.templateSourceLocation
				.getRegionID == ShellsbaneRegion || e
				.getProjectile
				.templateTargetLocation
				.getRegionID == ShellsbaneRegion) &&
			projectile.justSpawned
		) {
			projectiles = projectiles.appended(projectile)
		}
	}

//
//	@Subscribe
//	def onGameObjectSpawned(e: GameObjectSpawned): Unit = {
//		if (curRegion == ShellsbaneRegion) {
//			if (e.getGameObject.getId == 54279) {
//				iceTiles.add(e.getGameObject)
//			}
//		}
//	}
//
//	@Subscribe
//	def onGameObjectDespawned(e: GameObjectDespawned): Unit = {
//		if(curRegion == ShellsbaneRegion){
////			iceTiles.remove(e.getGameObject)
//		}
//	}

	def decodeAnimationId(id: Int): String = {
		id match {
			case -1 => "IDLE"
			case 12546 => "SPAWN"
			case 12548 => "IDLE"
			case 12550 => "WALK"
			case 12552 => "MELEE_ATTACK"
			case 12553 => "SPIT"
			case 12554 => "RANGED_ATTACK"
			case 12555 => "WHIRLWIND"
			case 12557 => "DEATH"
			case _ => s"UNKOWN(${id})"
		}
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		if (curRegion == ShellsbaneRegion) {
			if(
				Option(boss).exists(_.wrapped == e.getActor)
			) {
				val aid = boss.wrapped.getAnimation
				if(List(12552, 12553, 12554).contains(aid)) {
					boss.lastAttack = client.getTickCount
				}
				boss.lastAnimation = aid
				log.debug(s"Shellsbane's animation changed to ${decodeAnimationId(aid)} (${aid})")
			}
		}
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {

		//		if(curRegion == ShellsbaneRegion){
		val regionLine = LineComponent.builder().left("Region").right(s"$curRegion").rightColor(if(curRegion == ShellsbaneRegion) Color.GREEN else Color.RED).build
		//			val bossLine = bossData.headOption.map((amox, amoxdata) =>{
		//				LineComponent.builder().left(amox.toString).right(amoxdata.toString).rightColor(if(client.getTickCount - amoxdata.lastAttackTick > 6) Color.RED else Color.BLUE).build
		//			}).toList

		val bossLines = Option(boss)
			.map(b => {
				Seq(
					(Color.WHITE, "id", Color.BLUE, b.wrapped.getId),
					(Color.WHITE, "animation", Color.BLUE, decodeAnimationId(b.wrapped.getAnimation)),
					(Color.WHITE, "lastAnim", Color.BLUE, decodeAnimationId(b.lastAnimation)),
					(Color.WHITE, "idleTicks", Color.BLUE, client.getTickCount - b.lastAttack),
					(Color.WHITE, "tLoc", Color.BLUE, b.wrapped.templateLocation)
				)
			})
			.getOrElse(List.empty)
			.map{
				case (color, str, color1, i) =>
					LineComponent.builder()
						.leftColor(color).left(str)
						.right(str).rightColor(color1)
						.build
			}

//			val iceLines = iceBlocks.toList.zipWithIndex.map((b,idx) => {
//				LineComponent.builder().left(s"Ice[${idx.toString.padTo(2, ' ')}] ${b._1.getIndex}").right(b._2.toString).leftColor(
//					if(b._1.isDead) {
//						Color.RED
//					} else Color.GREEN
//				).rightColor(ColorUtil.colorLerp(Color.RED, Color.GREEN, Math.min(1.0d, Math.max(0.0d,(client.getTickCount - b._2.spawnedTick).toDouble/15.0d)))).build
//			}).pipe(ibl => if(ibl.nonEmpty) ibl.prepended(TitleComponent.builder().text("Unstable Ice").color(Color.CYAN).build()) else ibl)

			Seq(regionLine, bossLines).flatMap{
				case e: LayoutableRenderableEntity => Seq(e)
				case le: Seq[_] => le.collect{
					case e: LayoutableRenderableEntity => e
				}
			}
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		given Graphics2D = g
		given ModelOutlineRenderer = parent.getModelOutlineRenderer
		def renderNpcOverlay(n:Shellsbane, color: Color): Unit = {
			parent.getModelOutlineRenderer.drawOutline(n.wrapped, 2,  color, 4)
			val poly = n.wrapped.getCanvasTilePoly
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
//			val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
//			if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}

		def renderNpcText(n: Shellsbane, text: String, color: Color, zoffset: Int): Unit = {
			val textLocation = n.wrapped.getCanvasTextLocation(g, text, n.wrapped.getLogicalHeight + zoffset)
			if (textLocation != null)
				OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}

		def renderDangerousTile(lp: LocalPoint, color: Color, p: Double): Unit = {
			val poly = Perspective.getCanvasTilePoly(client, lp)
			if (poly != null) OverlayUtil.renderPolygon(g, poly, ColorUtil.colorWithAlpha(color, 200))

			val ppc = new ProgressPieComponent()
			ppc.setBorderColor(Color.BLACK)
			ppc.setFill(Color.RED)
			ppc.setProgress(p)
			ppc.setDiameter(28)
			val point = Perspective.localToCanvas(client, lp, client.getTopLevelWorldView.getPlane, 20)
			ppc.setPosition(point)
			ppc.render(g)

			val text = s"(x: ${lp.getSceneX}, y: ${lp.getSceneY})"

			val fm      = g.getFontMetrics()
			val bounds  = fm.getStringBounds(text, g)
			val xOffset = point.getX() - (bounds.getWidth() / 2).toInt
			val yOffset = point.getY() + (bounds.getHeight() / 2).toInt

			val textPoint = new Point(xOffset, yOffset)
			OverlayUtil.renderTextLocation(g, textPoint, text, Color.WHITE)
		}
//
//		def renderTile(tile: GameObject, color: Color): Unit = {
//			val lp   = tile.getLocalLocation
//			val poly = Perspective.getCanvasTilePoly(client, lp)
//			if (poly != null) OverlayUtil.renderPolygon(g, poly, ColorUtil.colorWithAlpha(color, 200))
//		}

		if(curRegion == ShellsbaneRegion){
//			dangerousTiles.values.toList.foreach(dt => {
//				renderDangerousTile(dt, Color.ORANGE)
//			})
//			iceTiles.toList.foreach(iceTile => {
//				renderIceTile(iceTile, Color.RED)
//			})

			Option(boss)
				.foreach {sb =>
				renderNpcOverlay(sb, Color.CYAN)
				renderNpcText(sb, s"T: ${client.getTickCount - sb.lastAttack}", Color.GRAY, 20)
			}

			projectiles
				.foreach {p =>
					import com.fredplugins.common.overlays

					Option(p.getId).collect {
						case ProjectileID.GRYPHON_SPIT_PROJECTILE => (Color.ORANGE, "Spit")
						case ProjectileID.GRYPHON_RANGED_PROJECTILE => (Color.BLUE, "Range")
						case x => (Color.PINK, s"Unkown${x}")
					}
						.zip(Option(p))
						.foreach{
							case ((color, txt), projectile) =>
								overlays.renderProjectileOverlay(projectile, txt)(2, 2, color)
								overlays.renderTileOverlay(projectile.getTargetPoint, s"${projectile.ticksRemaining}", color, false)
						}
				}
		}
		null.asInstanceOf[Dimension]
	}
}
