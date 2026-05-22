package com.fredplugins.pvmDebugger.thermy

import com.fredplugins.common.ProjectileID
import com.fredplugins.common.constants.magic.SMagicBoost.{DeathCharge, SummonThrall}
import com.fredplugins.common.constants.magic.STimedPotion.Divine_combat
import com.fredplugins.common.extensions.ActorExtensions
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*

import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.services.TimedBoostsService
import com.fredplugins.common.services.TimedBoostsService.{MagicBoostChanged, getCachedValue, isActive, isLocked}
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.{HelperModule, PvmDebuggerPlugin, WithOverlay, WithPanel}
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{Equipment, Inventory}
import ethanApiPlugin.interactionApi.InventoryInteraction
import ethanApiPlugin.lucidplugins.api.utils.{CombatUtils, EquipmentUtils, InteractionUtils, InventoryUtils}
import ethanApiPlugin.services.localPlayer.events.{LocalDestinationChanged, LocalPositionChanged, LocalRegionChanged}
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.*
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID}
import net.runelite.api.*
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, ProgressPieComponent, TitleComponent}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.util.ColorUtil

import ThermyProjectiles.{Mage, Range, Spec}

import java.awt.{Color, Dimension, Graphics2D, Polygon, Shape}
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class FredsThermyHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsThermyConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = "FredsThermyHelper"
	private def clientThread  = parent.getClientThread
	given Client = client

	private var curInRegion: Boolean = false;

	val projectiles: mutable.ListBuffer[(ThermyProjectile, Projectile)] = mutable.ListBuffer.empty
	var justSpawnedProjectiles: List[(ThermyProjectile, Projectile)] = List.empty

	private def clearState(): Unit = {
		projectiles.clear()
		justSpawnedProjectiles = List.empty
	}

	override def init(): Unit = {
		curInRegion = Option(client.getLocalPlayer).map(_.templateRegion).map(inRegion).getOrElse(false)
		clearState()
	}

	override def cleanup(): Unit = {
		curInRegion = false
		clearState()
	}

	@Subscribe
	def onMenuEntryAdded(me: MenuEntryAdded): Unit = {
		if(!curInRegion || me.getMenuEntry.getType != MenuAction.WALK || !config.resonanceWalkToOnly()) return
		val wv = client.getWorldView(me.getMenuEntry.getWorldViewId)
		if(wv == null) return
		val selectedTile = wv.getSelectedSceneTile();
		if(selectedTile == null) return

		val wp = WorldPoint.fromLocalInstance(client, selectedTile.getLocalLocation)
		if(wp == null) return

		if(isDangerousTile(wp))
			client.setMenuEntries(Array.copyOf(client.getMenuEntries, client.getMenuEntries.length - 1))
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		if(!curInRegion) return
		projectiles.appendAll(justSpawnedProjectiles)
		projectiles.filterInPlace(_._2.hasHit == false)
		justSpawnedProjectiles = List.empty

		projectiles.headOption.map(_._1).collect {
			case ThermyProjectiles.Range => Prayer.PROTECT_FROM_MISSILES
			case ThermyProjectiles.Mage => Prayer.PROTECT_FROM_MAGIC
		}.foreach(p => {
			CombatUtils.activatePrayer(p)
		})
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
		if(inRegion(e.getOldRegion) && !inRegion(e.getCurRegion)) {
			CombatUtils.deactivatePrayers(false)
			clearState()
			curInRegion = false
			return
		}
		if (!inRegion(e.getOldRegion) && inRegion(e.getCurRegion)) {
			curInRegion = true
			client.getNpcs.asScala.toList.map(n => {
				new NpcSpawned(n)
			}).foreach(e => onNpcSpawned(e))
			client.getProjectiles.asScala.toList.map(p => {
				new ProjectileMoved().tap(_.setProjectile(p))
			}).foreach(e => onProjectileMoved(e))
		}
	}

	@Subscribe
	def onLocalDestinationChanged(e: LocalDestinationChanged): Unit = {
		if(!curInRegion) return
		log.info(s"Destination changed from ${e.getFrom} to ${e.getTo}")
	}

	@Subscribe
	def onLocalPositionChanged(e: LocalPositionChanged): Unit = {
		if(!curInRegion) return
		log.info(s"Position changed from ${e.getFrom} to ${e.getTo}")
	}

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		if(!curInRegion) return
		val npc = e.getNpc
		if(npc == null || !inBossRoom(npc.templateLocation)) return
		log.info("Spawned {}", e.getNpc.niceString);
		if(npc.getId == NpcID.SMOKE_DEVIL_BOSS) {
			CombatUtils.activatePrayers(Prayer.MYSTIC_MIGHT, Prayer.PROTECT_FROM_MISSILES)
		}
	}

	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		if(!curInRegion) return
		val npc = e.getNpc
		if(npc == null || !inBossRoom(npc.templateLocation)) return
		log.info("Despawned {}", e.getNpc.niceString);

		if(npc.getId == NpcID.SMOKE_DEVIL_BOSS) {
			CombatUtils.deactivatePrayers(false)
		}
	}

	@Subscribe
	def onProjectileMoved(e: ProjectileMoved): Unit = {
		if(!curInRegion || projectiles.exists(_._2 == e.getProjectile) || justSpawnedProjectiles.exists(_._2 == e.getProjectile)) return
		ThermyProjectiles.unapply(e.getProjectile).foreach(j => {
			log.debug("Projectile spawned {}({})", j._1, j._2)
			justSpawnedProjectiles = justSpawnedProjectiles.appended(j)
		})
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		if (!curInRegion) return
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		val regionLine: LayoutableRenderableEntity = LineComponent.builder().left("Region").right(s"${Option(client.getLocalPlayer).map(_.templateRegion).getOrElse(-1)}").rightColor(if(curInRegion) Color.GREEN else Color.RED).build
		val bossLines: Seq[LayoutableRenderableEntity] = Seq.empty[LayoutableRenderableEntity]
			.pipe(tl => if (tl.isEmpty) tl else tl.prepended(TitleComponent.builder().text("Boss").color(Color.CYAN).build()))

		val projectileLines: Seq[LayoutableRenderableEntity] = projectiles.toList.sortBy(_._2.getRemainingCycles).map((tp, p) => {
			LineComponent.builder()
				.leftColor(Color.WHITE).left(s"${p.getRemainingCycles}|${p.ticksRemaining}")
				.right(tp.entryName)
				.rightColor(
					tp match {
						case ThermyProjectiles.Spec => config.specProjectileColor()
						case ThermyProjectiles.Range => config.rangedProjectileColor()
						case ThermyProjectiles.Mage => config.magicProjectileColor()
					}
				)
				.build()
		}).prepended(
			TitleComponent.builder().text("Projectiles").color(Color.CYAN).build()
		)
//			val iceLines = iceBlocks.toList.zipWithIndex.map((b,idx) => {
//				LineComponent.builder().left(s"Ice[${idx.toString.padTo(2, ' ')}] ${b._1.getIndex}").right(b._2.toString).leftColor(
//					if(b._1.isDead) {
//						Color.RED
//					} else Color.GREEN
//				).rightColor(ColorUtil.colorLerp(Color.RED, Color.GREEN, Math.min(1.0d, Math.max(0.0d,(client.getTickCount - b._2.spawnedTick).toDouble/15.0d)))).build
//			}).pipe(ibl => if(ibl.nonEmpty) ibl.prepended(TitleComponent.builder().text("Unstable Ice").color(Color.CYAN).build()) else ibl)

			Seq(regionLine, bossLines, projectileLines).flatMap{
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
		client.getNpcs.asScala.toList.foreach(n => {
			renderNpcOverlay(n, Color.CYAN)
			renderNpcText(n, s"id: ${n.getId}, a: ${n.getAnimation}", Color.GRAY, 20)
			renderNpcText(n, n.getName, Color.GRAY, 40)
		})

//		client.getProjectiles.asScala.toList.foreach(p => {
//			overlays.renderProjectileOverlay(p, s"id: ${p.getId}, tr: ${p.ticksRemaining}")(2, 2, Color.GRAY)
//			overlays.renderTileOverlay(p.worldLocation, s"${p.ticksRemaining}", Color.CYAN, false)
//		})
//		Option(boss)
//			.foreach {sb =>
//				renderNpcOverlay(sb.wrapped, Color.CYAN)
//				renderNpcText(sb.wrapped, s"T: ${client.getTickCount - sb.lastAttack}", Color.GRAY, 20)
//			}

		projectiles.toList
			.foreach {(tp, p) =>
				val color = tp match {
					case ThermyProjectiles.Spec => config.specProjectileColor()
					case ThermyProjectiles.Range => config.rangedProjectileColor()
					case ThermyProjectiles.Mage => config.magicProjectileColor()
				}
				overlays.renderProjectileOverlay(p, tp.entryName)(2, 2, color)
				overlays.renderTileOverlay(p.worldLocation, s"${p.ticksRemaining}", color, false)
			}
		null.asInstanceOf[Dimension]
	}
}
