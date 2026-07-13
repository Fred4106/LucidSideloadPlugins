package com.fredplugins.pvmDebugger.bmr

import com.fredplugins.common.api.WorldRegion.given_Conversion_WorldArea_WorldRegion
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.overlays
import com.fredplugins.common.utils.ReflectionUtils
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.common.utils.TWorldPoint
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.coords.Angle
import net.runelite.api.coords.Direction
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.api.coords.Direction
import net.runelite.api.coords.WorldArea
import net.runelite.api.events.*
import net.runelite.api.events.GameTick
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_PUNCH_LEFT
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_PUNCH_RIGHT
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_SCREECH01
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_SCREECH02
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_SCREECH03
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_TANTRUM01
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_TANTRUM02
import net.runelite.api.gameval.AnimationID.NPC_WYRD02_MELEE01
import net.runelite.api.gameval.NpcID
import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.Prayer
import net.runelite.api.Renderable
import net.runelite.api.gameval.AnimationID
import net.runelite.api.gameval.AnimationID.{NPC_LOWERNIEL_DRAKAN_IDLE_TO_TELEGRAPH_LOOP01, NPC_LOWERNIEL_DRAKAN_COMBO_TELEGRAPH_TO_READY01, LOWERNIEL_DRAKAN_STAB_HIT_TELEGRAPH01, LOWERNIEL_DRAKAN_SWIPE_HIT_TELEGRAPH01}
import net.runelite.client.callback.RenderCallback
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.game.NpcUtil
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.util.ColorUtil

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.Random
import scala.util.Try
case class DustTile(location: WorldPoint, spawnCycle: Int, finishedCycle: Int, spawnTick: Int)

class FredsDrakanHelper(parent: PvmDebuggerPlugin, config: FredsBmrConfig) extends RenderCallback with ShimUtils.Logging() {
	private def clientThread = parent.getClientThread
	private def client = parent.getClient
	given Client = client

	def inRegion(wp: WorldPoint): Boolean = {
		Option(wp).map(TWorldPoint.get(_)).map(_.getRegionID).fold(false)(inRegion)
	}

	def inRegion(rid: Int): Boolean = {
		rid == 14132 || rid == 10106
	}

	private var curInRegion: Boolean = false

	//	private val animations: mutable.Set[Int] = mutable.HashSet.empty
	private var bossNpc: NPC = uninitialized
	private var animations: mutable.ListBuffer[(Int, Int)] = mutable.ListBuffer.empty[(Int, Int)]
	private var spotAnimations: mutable.ListBuffer[(Int, String)] = mutable.ListBuffer.empty[(Int, String)]
	private var dustTiles: Seq[DustTile] = Seq.empty[DustTile]

	def safeTiles(): Seq[WorldArea] = {
		if(!client.isClientThread) parent.getClientThread.runOnClientThread(() => safeTiles())
//		Option(bossNpc).toList.flatMap(boss => {
//			val bossWorldArea = boss.getWorldArea
//			(bossAttackIds.collect {
//				case NPC_WYRD01_PUNCH_LEFT => (_: Direction).getRight
//				case NPC_WYRD01_PUNCH_RIGHT => (_: Direction).getLeft
//			}).map(_.apply(boss.direction)).map(q => bossWorldArea.edge(q, 1))
//		}).toList
		Seq.empty
	}
	private def clearState(): Unit = {
		//		animations.clear()
		bossNpc = null
		animations = mutable.ListBuffer.empty
		spotAnimations = mutable.ListBuffer.empty
		dustTiles = Seq.empty
	}

	def init(): Unit = {
		curInRegion = Option(client.getLocalPlayer).map(_.templateRegion).fold(false)(inRegion)//.map(inRegion).getOrElse(false)
		clearState()
		parent.getRenderCallbackManager.register(this)
	}

	def cleanup(): Unit = {
		parent.getRenderCallbackManager.unregister(this)
		curInRegion = false
		clearState()
	}
	@Subscribe
	def onGraphicsObjectCreated(e: GraphicsObjectCreated): Unit = {
		val graphicsObject = e.getGraphicsObject
		val name = ReflectionUtils.getSpotAnimationName(graphicsObject.getId)
		if (inRegion(graphicsObject.templateLocation)) {
			//ticksSinceDangerousTiles = 5
			Option.when(graphicsObject.getId == 2953){
					val goDuration = (graphicsObject.getStartCycle - client.getGameCycle)
					val fakeDuration = Math.min(120, goDuration)
					val fakeSpawnCycle = graphicsObject.getStartCycle - fakeDuration
					val fakeSpawnTick = ((goDuration - fakeDuration) / 30.0).floor.toInt + client.getTickCount
					DustTile(graphicsObject.templateLocation, fakeSpawnCycle, graphicsObject.getStartCycle, fakeSpawnTick)
			}.foreach(dt => {
				dustTiles = dustTiles.appended(dt)
			})
		}
		log.info(s"GraphicsObject \"${name}\" created on tick ${client.getTickCount} at ${graphicsObject.templateLocation} with start cycle ${graphicsObject.getStartCycle} on cycle ${client.getGameCycle} animation ${Option(graphicsObject.getAnimation).map(a => a.getId -> a.getDuration).getOrElse(-1 -> 0)}")
	}
	@Subscribe(priority = -10)
	def afterGameTick(gameTick: GameTick): Unit = {
		npcEvents.clear()
		if(bossNpc != null) {
			if(parent.getNpcUtil.isDying(bossNpc)) {
				CombatUtils.deactivatePrayers(false)
			} else {
				CombatUtils.activatePrayers(Prayer.PIETY, Prayer.PROTECT_FROM_MELEE)
			}
		}
	}

	@Subscribe(priority = 10)
	def beforeGameTick(gameTick: GameTick): Unit = {
		npcEvents.filterInPlace{
			_.getAsNpc().exists(
				n => n != null && n.getName.equalsIgnoreCase("Lowerniel Drakan") && inRegion(n.templateLocation)
			)
		}
		dustTiles = dustTiles.filter(_.finishedCycle > client.getGameCycle)

//		Option(bossNpc).filter(j => j.getAnimation == NPC_LOWERNIEL_DRAKAN_IDLE_TO_TELEGRAPH_LOOP01).foreach{boss=>
//			Option(boss.getGraphic).filter(_ != -1).map(ReflectionUtils.getSpotAnimationName).foreach(u => spotAnimations.addOne(client.getTickCount, u + ":L"))
//		}
//
//		Option(bossNpc).filter(j => j.getAnimation != NPC_LOWERNIEL_DRAKAN_COMBO_TELEGRAPH_TO_READY01).foreach { boss =>
//			Option(boss.getGraphic).filter(_ != -1).map(ReflectionUtils.getSpotAnimationName).foreach(u => spotAnimations.addOne(client.getTickCount, u + ":R"))
//		}
//
//		Option(bossNpc).filter(j => j.getAnimation == -1).foreach { boss =>
//			spotAnimations.clear()
////			Option(boss.getGraphic).filter(_ != -1).map(ReflectionUtils.getSpotAnimationName).foreach(u => spotAnimations.addOne(client.getTickCount, u))
//		}
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		npcEvents.foreach{
			case e: NpcSpawned => {
				bossNpc = e.getAsNpc().orNull
			}
			case e: NpcDespawned => {
				clearState()
			}
			case e: ActorDeath => {

			}
			case e: AnimationChanged => {
				e.getAsNpc().map(_.getAnimation)/*.filter(_ != -1)*/
					.tapEach(z => {
						log.debug("animation changed to {} on tick {}", ReflectionUtils.getAnimationName(z), client.getTickCount)
						animations.addOne(client.getTickCount -> z)
					})
					.foreach{
						case u => {
							//log.debug("Problem handling {} {} when bossAttackCount is {}", u, ReflectionUtils.getAnimationName(u), bossAttackCount)
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
			val animLines: Seq[LayoutableRenderableEntity] = animations.toList.reverse.take(10).reverse.zipWithIndex.map {
					case ((tick, animId), idx) =>
						LineComponent.builder().left(s"${idx} @ ${tick}").right(s"${animId} = ${if (animId != -1) ReflectionUtils.getAnimationName(animId) else "None"}").build
				}
				.pipe(tl => if (tl.isEmpty) tl else tl.prepended(TitleComponent.builder().text("Animations").color(Color.CYAN).build()))

			val spotAnimLines: Seq[LayoutableRenderableEntity] = spotAnimations.toList.zipWithIndex.map {
					case ((tick, animName), idx) =>
						LineComponent.builder().left(s"${idx} @ ${tick}").right(s"${animName}").build
				}
				.pipe(tl => if (tl.isEmpty) tl else tl.prepended(TitleComponent.builder().text("Spot").color(Color.CYAN).build()))

			val bossLines: Seq[LayoutableRenderableEntity] = {
				Option(bossNpc)
					.map{boss =>
						boss.getSpotAnims.asScala.toList.zipWithIndex.map(asa =>
							s"spotAnim[${asa._2}]" -> ReflectionUtils.getSpotAnimationName(asa._1.getId)
						).toSeq
					}
					.getOrElse(Seq.empty)
					.map {
						case (lbl, vlue) =>
							LineComponent.builder().left(lbl).right(s"${vlue}").build
					}
					.pipe(tl => if (tl.isEmpty) tl else tl.prepended(TitleComponent.builder().text("Boss").color(Color.CYAN).build()))
			}

			Seq(regionLine,animLines, spotAnimLines, bossLines).flatMap {
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
					renderNpcOverlay(boss, config.bossColor())
					renderNpcText(boss, s"Anim: ${ReflectionUtils.getAnimationName(boss.getAnimation)}", Color.GRAY, 20)
					animations.toList.reverse.take(10).reverse.zipWithIndex.foreach{
						case ((tick, animName), idx) =>
							renderNpcText(boss, s"idx: ${idx}, tick: ${tick}, anim: ${animName}", Color.LIGHT_GRAY, 40 + (idx * 16))
					}
				}
			} catch {
				ex => //log.error("problem rendering: {}", ex)
			}

			dustTiles.filter(_.spawnCycle > client.getGameCycle).foreach {
				case DustTile(location, spawnCycle, finishedCycle, spawnTick) => {
						//					val nSpawnCycle = (finishedCycle-120).max(spawnCycle)
						//					val spawnTickOffset = ((nSpawnCycle - spawnCycle)/30.0).floor.toInt
						//					val maxAge = ((finishedCycle - nSpawnCycle) / 30.0).floor.toInt
					renderTile(location, Color.CYAN.withAlpha(150))
				}
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

	override def addEntity(renderable: Renderable, ui: Boolean): Boolean = {
		Option(renderable).filter(_ => curInRegion).collect {
			case npc: NPC => npc.getName.equalsIgnoreCase("Lowerniel Drakan")
		}.getOrElse(super.addEntity(renderable, ui))
	}
}
