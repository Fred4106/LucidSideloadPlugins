package com.fredplugins.pvmDebugger.wyrd

import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.constants.magic.SMagicBoost.{DeathCharge, SummonThrall}
import com.fredplugins.common.constants.magic.STimedPotion.Divine_combat
import com.fredplugins.common.extensions.ActorExtensions
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.services.TimedBoostsService
import com.fredplugins.common.services.TimedBoostsService.{MagicBoostChanged, getCachedValue, isActive, isLocked}
import com.fredplugins.common.utils.ReflectionUtils
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.{HelperModule, PvmDebuggerPlugin, WithOverlay, WithPanel}
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{Equipment, Inventory}
import ethanApiPlugin.interactionApi.InventoryInteraction
import ethanApiPlugin.lucidplugins.api.utils.{CombatUtils, EquipmentUtils, InteractionUtils, InventoryUtils}
import ethanApiPlugin.services.localPlayer.events.{LocalDestinationChanged, LocalPositionChanged, LocalRegionChanged}
import net.runelite.api.*
import net.runelite.api.coords.{Angle, Direction, LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.*
import net.runelite.api.gameval.AnimationID.{NPC_WYRD01_PUNCH_LEFT, NPC_WYRD01_PUNCH_RIGHT, NPC_WYRD01_SCREECH01, NPC_WYRD01_SCREECH02, NPC_WYRD01_SCREECH03, NPC_WYRD01_TANTRUM01, NPC_WYRD01_TANTRUM02, NPC_WYRD02_MELEE01}
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID}
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, ProgressPieComponent, TitleComponent}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.util.ColorUtil
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.api.WorldRegion.{given_Conversion_WorldArea_WorldRegion, *}

import java.awt.{Color, Dimension, Graphics2D, Polygon, Shape}
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.{Failure, Success, Try}
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class FredsWyrdHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsWyrdConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = "FredsWyrdHelper"
	private def clientThread  = parent.getClientThread
	given Client = client

	private var curInRegion: Boolean = false;

//	private val animations: mutable.Set[Int] = mutable.HashSet.empty
	private var bossNpc: NPC = uninitialized
	private var bossAttackCount: Int = 0
	private var bossLastAttackTick: Int = -1
	private val bossAttackIds: Array[Int] = Array.fill(2)(-1)
	def safeTiles(): Seq[WorldArea] = {
		if(!client.isClientThread) parent.getClientThread.runOnClientThread(() => safeTiles())
		Option(bossNpc).toList.flatMap(boss => {
			val bossWorldArea = boss.getWorldArea
			(bossAttackIds.collect {
				case NPC_WYRD01_PUNCH_LEFT => (_: Direction).getRight
				case NPC_WYRD01_PUNCH_RIGHT => (_: Direction).getLeft
			}).map(_.apply(boss.direction)).map(q => bossWorldArea.edge(q, 1))
		}).toList
	}
	def tickAttackCount(): Unit = {
		bossAttackCount = (bossAttackCount + 1) % 8
		bossLastAttackTick = client.getTickCount
	}
	private def clearState(): Unit = {
//		animations.clear()
		bossNpc = null
		bossAttackCount = 0
		(0 until 2).foreach(bossAttackIds(_) = -1)
		bossLastAttackTick = -1
	}

	override def init(): Unit = {
		curInRegion = Option(client.getLocalPlayer).map(_.templateRegion).fold(false)(inRegion)//.map(inRegion).getOrElse(false)
		clearState()
	}

	override def cleanup(): Unit = {
		curInRegion = false
		clearState()
	}

	@Subscribe(priority = -10)
	def afterGameTick(gameTick: GameTick): Unit = {
		npcEvents.clear()
		val screeches = Seq(NPC_WYRD01_SCREECH01, NPC_WYRD01_SCREECH02, NPC_WYRD01_SCREECH03)
		Option(bossNpc).filter(b => !screeches.contains(b.getAnimation)).foreach{boss =>
			CombatUtils.activatePrayers(Prayer.PIETY, Prayer.PROTECT_FROM_MELEE)
		}
	}
	@Subscribe(priority = 10)
	def beforeGameTick(gameTick: GameTick): Unit = {
		npcEvents.filterInPlace{
			_.getAsNpc().exists(
				n => n.getId == NpcID.SAFALAAN_WYRD && inRegion(n.templateLocation)
			)
		}
	}
	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		npcEvents.foreach{
			case e: NpcSpawned => {
				bossNpc = e.getAsNpc().orNull
				CombatUtils.activatePrayers(Prayer.PIETY, Prayer.PROTECT_FROM_MELEE)
			}
			case e: NpcDespawned => {
				clearState()
			}
			case e: ActorDeath => {
				CombatUtils.deactivatePrayers(false)
			}
			case e: AnimationChanged => {
				e.getAsNpc().map(_.getAnimation).filter(_ != -1)
//					.tapEach(z => log.debug("animation changed to {} on tick {}", ReflectionUtils.getAnimationName(z), client.getTickCount))
					.foreach{
						case z@(NPC_WYRD01_PUNCH_RIGHT | NPC_WYRD01_PUNCH_LEFT) if Seq(0, 1, 3, 4).contains(bossAttackCount) => {
							Option(bossAttackCount-3).filter(_ >= 0).filter(_ < 2).foreach(bossAttackIds(_) = z)
							tickAttackCount()
						}
						case z@(NPC_WYRD01_SCREECH02 | NPC_WYRD01_SCREECH01 | NPC_WYRD01_SCREECH03) if Seq(2, 5).contains(bossAttackCount) => {
							CombatUtils.deactivatePrayers(true)
							tickAttackCount()
						}
						case z@(NPC_WYRD01_TANTRUM02 | NPC_WYRD01_TANTRUM01) => {
//							Option(bossAttackCount - 6).filter(_ >= 0).filter(_ < 2).foreach(bossAttackIds(_) = -1)
							val dodgeIdIdx = bossAttackIds.indexWhere(_ != -1).pipe(j => if(j < 0) 1 else j)
							bossAttackCount = (6 + dodgeIdIdx)
							tickAttackCount()
							bossAttackIds(dodgeIdIdx) = -1
						}
						case u => {
							log.debug("Problem handling {} {} when bossAttackCount is {}", u, ReflectionUtils.getAnimationName(u), bossAttackCount)
						}
					}
			}
		}


	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
		if(inRegion(e.getOldRegion) && !inRegion(e.getCurRegion)) {
			curInRegion = false
			clearState()
			return
		}
		if (!inRegion(e.getOldRegion) && inRegion(e.getCurRegion)) {
			curInRegion = true
		}
	}

//	@Subscribe
//	def onLocalDestinationChanged(e: LocalDestinationChanged): Unit = {
//		if(!curInRegion) return
//		log.info(s"Destination changed from ${e.getFrom} to ${e.getTo}")
//	}
//
//	@Subscribe
//	def onLocalPositionChanged(e: LocalPositionChanged): Unit = {
//		if(!curInRegion) return
//		log.info(s"Position changed from ${e.getFrom} to ${e.getTo}")
//	}

	private val npcEvents: mutable.ListBuffer[(NpcSpawned | NpcDespawned | ActorDeath | AnimationChanged) & HasGetActorMethod] = mutable.ListBuffer.empty

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		npcEvents.addOne(e)
	}

	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		npcEvents.addOne(e)
	}

//	@Subscribe
//	def onNpcChanged(e: NpcChanged): Unit = {
//		npcEvents.addOne(e)
//	}

	@Subscribe
	def onActorDeath(e: ActorDeath): Unit = {
		if (e.getAsNpc().isDefined) npcEvents.addOne(e)
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		if(e.getAsNpc().isDefined) npcEvents.addOne(e)
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		val regionLine: LayoutableRenderableEntity = LineComponent.builder().left("Region").right(s"${Option(client.getLocalPlayer).map(_.templateRegion).getOrElse(-1)}").rightColor(if(curInRegion) Color.GREEN else Color.RED).build
		val bossLines: Seq[LayoutableRenderableEntity] = {
			//Seq.empty[LayoutableRenderableEntity]
			Seq(
				("count" -> bossAttackCount),
				("last tick" -> bossLastAttackTick),
				("attack 1" -> ReflectionUtils.getAnimationName(bossAttackIds(0))),
				("attack 2" -> ReflectionUtils.getAnimationName(bossAttackIds(1)))
			).map {
				case (lbl, vlue) =>
					LineComponent.builder().left(lbl).right(s"${vlue}").build
			}
			.pipe(tl => if (tl.isEmpty) tl else tl.prepended(TitleComponent.builder().text("Boss").color(Color.CYAN).build()))
		}

		//		val animationLines: Seq[LayoutableRenderableEntity] = animations.toList.sorted.map{aid =>
//			aid -> ReflectionUtils.getAnimationName(aid)
//		}.map((aId, aName) => {
//			LineComponent.builder()
//				.leftColor(Color.WHITE).left(s"${aId}")
//				.right(aName)
//				.rightColor(
//					aId match {
//						case NPC_WYRD01_PUNCH_RIGHT | NPC_WYRD01_PUNCH_LEFT => Color.BLUE
//						case o => Color.LIGHT_GRAY
//					}
//				)
//				.build()
//		}).prepended(
//			TitleComponent.builder().text("Animations").color(Color.CYAN).build()
//		)

//			val iceLines = iceBlocks.toList.zipWithIndex.map((b,idx) => {
//				LineComponent.builder().left(s"Ice[${idx.toString.padTo(2, ' ')}] ${b._1.getIndex}").right(b._2.toString).leftColor(
//					if(b._1.isDead) {
//						Color.RED
//					} else Color.GREEN
//				).rightColor(ColorUtil.colorLerp(Color.RED, Color.GREEN, Math.min(1.0d, Math.max(0.0d,(client.getTickCount - b._2.spawnedTick).toDouble/15.0d)))).build
//			}).pipe(ibl => if(ibl.nonEmpty) ibl.prepended(TitleComponent.builder().text("Unstable Ice").color(Color.CYAN).build()) else ibl)

			Seq(regionLine, bossLines/*, animationLines*/).flatMap{
				case e: LayoutableRenderableEntity => Seq(e)
				case le: Seq[_] => le.collect{
					case e: LayoutableRenderableEntity => e
				}
			}
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		given Graphics2D = g
		given ModelOutlineRenderer = parent.getModelOutlineRenderer
		import com.fredplugins.common.overlays

		def renderPie(diameter: Int, heightOffset: Int)(pieChartColor: Color, textColor: Color)(lp: LocalPoint, text: String, progress: Double): Unit = {
			val ppc = new ProgressPieComponent()
			ppc.setBorderColor(Color.BLACK)
			ppc.setFill(pieChartColor)
			ppc.setProgress(progress)
			ppc.setDiameter(diameter)
			val point = Perspective.localToCanvas(client, lp, client.getTopLevelWorldView.getPlane, heightOffset)
			ppc.setPosition(point)
			ppc.render(g)

			val fm = g.getFontMetrics()
			val bounds = fm.getStringBounds(text, g)
			val xOffset = point.getX() - (bounds.getWidth() / 2).toInt
			val yOffset = point.getY() + (bounds.getHeight() / 2).toInt

			val textPoint = new Point(xOffset, yOffset)
			OverlayUtil.renderTextLocation(g, textPoint, text, textColor)
		}

		def renderNpcOverlay(n:NPC, color: Color): Unit = {
			parent.getModelOutlineRenderer.drawOutline(n, 2,  color, 4)
			val poly = n.getCanvasTilePoly
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
//			val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
//			if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}

		def renderNpcText(n: NPC, text: String, color: Color, zoffset: Int): Unit = {
			val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
			if (textLocation != null)
				OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}
		def renderTile(tile: WorldPoint, color: Color): Unit = {
			val poly = Perspective.getCanvasTilePoly(client, tile.getLocalPoint)
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
		}
		if(!curInRegion) return null.asInstanceOf[Dimension]
		try {
			Option(bossNpc).foreach { boss =>
				val (a1, a2) = bossAttackIds.toList.map(ReflectionUtils.getAnimationName(_))
					.map(u => u.stripPrefix("NPC_WYRD01_"))
					.pipe(u => u.head->u.tail.head)

				renderNpcOverlay(boss, config.bossColor())
				renderNpcText(boss, s"T: ${bossAttackCount}, A1: ${a1}, A2: ${a2}", Color.GRAY, 20)
			}
		} catch {
			ex => //log.error("problem rendering: {}", ex)
		}

		try {
			safeTiles().zipWithIndex.foreach((wa, ix0) => {
				val c = Seq(Color.green, Color.blue, Color.yellow)(ix0)
				wa.polygons.foreach { p =>
					OverlayUtil.renderPolygon(summon[Graphics2D], p, c, ColorUtil.colorWithAlpha(c, 64), overlays.getStroke(2, true))
				}
				overlays.renderTileOverlay(wa.center, s"${ix0}", c, false)
			})
		} catch {
			ex => //log.error("problem rendering: {}", ex)
		}

		null.asInstanceOf[Dimension]
	}
}
