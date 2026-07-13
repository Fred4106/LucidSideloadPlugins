package com.fredplugins.pvmDebugger.bmr

import com.fredplugins.common.api.WorldRegion.given_Conversion_WorldArea_WorldRegion
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.{Client, NPC, Prayer}
import net.runelite.api.coords.{Direction, WorldArea}
import net.runelite.api.events.GameTick
import net.runelite.client.eventbus.Subscribe
import net.runelite.api.gameval.AnimationID.{NPC_WYRD01_PUNCH_LEFT, NPC_WYRD01_PUNCH_RIGHT, NPC_WYRD01_SCREECH01, NPC_WYRD01_SCREECH02, NPC_WYRD01_SCREECH03, NPC_WYRD01_TANTRUM01, NPC_WYRD01_TANTRUM02, NPC_WYRD02_MELEE01}
import com.fredplugins.common.extensions.ActorExtensions
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.overlays
import net.runelite.api.coords.{Angle, Direction, LocalPoint, WorldArea, WorldPoint}
import com.fredplugins.common.utils.{ReflectionUtils, ShimUtils, TWorldPoint}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import net.runelite.api.events.*
import net.runelite.api.gameval.NpcID
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
import net.runelite.client.util.ColorUtil

import java.awt.{Color, Dimension, Graphics2D}
import scala.collection.mutable

class FredsWyrdHelper(parent: PvmDebuggerPlugin, config: FredsBmrConfig) extends ShimUtils.Logging() {
	private def clientThread = parent.getClientThread
	private def client = parent.getClient
	given Client = client

	def inRegion(wp: WorldPoint): Boolean = {
		Option(wp).map(TWorldPoint.get(_)).map(_.getRegionID).fold(false)(inRegion)
	}

	def inRegion(rid: Int): Boolean = {
		rid == 11892
	}

	private var curInRegion: Boolean = false

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

	def init(): Unit = {
		curInRegion = Option(client.getLocalPlayer).map(_.templateRegion).fold(false)(inRegion)//.map(inRegion).getOrElse(false)
		clearState()
	}

	def cleanup(): Unit = {
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

	private val npcEvents: mutable.ListBuffer[(NpcSpawned | NpcDespawned | ActorDeath | AnimationChanged) & HasGetActorMethod] = mutable.ListBuffer.empty

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		npcEvents.addOne(e)
	}

	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		npcEvents.addOne(e)
	}

	@Subscribe
	def onActorDeath(e: ActorDeath): Unit = {
		if (e.getAsNpc().isDefined) npcEvents.addOne(e)
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		if(e.getAsNpc().isDefined) npcEvents.addOne(e)
	}

	def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		if(curInRegion) {
			val regionLine: LayoutableRenderableEntity = LineComponent.builder().left("Region").right(s"${Option(client.getLocalPlayer).map(_.templateRegion).getOrElse(-1)}").rightColor(if (curInRegion) Color.GREEN else Color.RED).build
			val bossLines: Seq[LayoutableRenderableEntity] = {
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
			Seq(regionLine, bossLines).flatMap {
				case e: LayoutableRenderableEntity => Seq(e)
				case le: Seq[_] => le.collect {
					case e: LayoutableRenderableEntity => e
				}
			}
		} else {
			Seq.empty[LayoutableRenderableEntity]
		}
	}
	
	def renderOverlay(
		renderNpcOverlay: (NPC, Color) => Unit, 
		renderNpcText: (NPC, String, Color, Int) => Unit, 
		renderTile: (WorldPoint, Color) => Unit
	)(using g: Graphics2D): Unit = {
		if (curInRegion) {
			try {
				Option(bossNpc).foreach { boss =>
					val (a1, a2) = bossAttackIds.toList.map(ReflectionUtils.getAnimationName(_))
						.map(u => u.stripPrefix("NPC_WYRD01_"))
						.pipe(u => u.head -> u.tail.head)
	
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
		}
	}
}
