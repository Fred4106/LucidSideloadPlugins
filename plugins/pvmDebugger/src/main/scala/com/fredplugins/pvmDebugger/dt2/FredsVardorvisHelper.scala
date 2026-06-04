package com.fredplugins.pvmDebugger.dt2

import com.fredplugins.common.ProjectileID
import com.fredplugins.common.constants.magic.SMagicBoost.{DeathCharge, SummonThrall}
import com.fredplugins.common.constants.magic.STimedPotion.Divine_combat
import com.fredplugins.common.extensions.ActorExtensions
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.services.TimedBoostsService
import com.fredplugins.common.services.TimedBoostsService.{MagicBoostChanged, getCachedValue, isActive, isLocked}
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.dt2.FredsVardorvisConfig.{DefensivePrayer, OffensivePrayer}
import com.fredplugins.pvmDebugger.{HelperModule, PvmDebuggerPlugin, WithOverlay, WithPanel}
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{Equipment, Inventory}
import ethanApiPlugin.interactionApi.InventoryInteraction
import ethanApiPlugin.lucidplugins.api.utils.{CombatUtils, EquipmentUtils, InteractionUtils, InventoryUtils}
import ethanApiPlugin.services.localPlayer.events.{LocalDestinationChanged, LocalPositionChanged, LocalRegionChanged}
import net.runelite.api.*
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.*
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID, SpotanimID}
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, ProgressPieComponent, TitleComponent}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.util.ColorUtil

import java.awt.{Color, Dimension, Graphics2D, Polygon, Shape}
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class FredsVardorvisHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsVardorvisConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = "FredsVardorvisHelper"
	private def clientThread  = parent.getClientThread
	given Client = client

	private var curInRegion: Boolean = false;

	val projectiles: mutable.ListBuffer[(VardorvisProjectile, Projectile)] = mutable.ListBuffer.empty
	var justSpawnedProjectiles: List[(VardorvisProjectile, Projectile)] = List.empty

	val spikeyBois: mutable.ListBuffer[(Int, WorldPoint)] = mutable.ListBuffer.empty


	private def clearState(): Unit = {
		projectiles.clear()
		justSpawnedProjectiles = List.empty

		spikeyBois.clear()
	}

	override def init(): Unit = {
		curInRegion = Option(client.getLocalPlayer).map(_.templateRegion).map(inRegion).getOrElse(false)
		clearState()
	}

	override def cleanup(): Unit = {
		curInRegion = false
		clearState()
	}
//
//	@Subscribe
//	def onMenuEntryAdded(me: MenuEntryAdded): Unit = {
//		if(!curInRegion || me.getMenuEntry.getType != MenuAction.WALK || !config.resonanceWalkToOnly()) return
//		val wv = client.getWorldView(me.getMenuEntry.getWorldViewId)
//		if(wv == null) return
//		val selectedTile = wv.getSelectedSceneTile();
//		if(selectedTile == null) return
//
//		val wp = WorldPoint.fromLocalInstance(client, selectedTile.getLocalLocation)
//		if(wp == null) return
//
//		if(isDangerousTile(wp))
//			client.setMenuEntries(Array.copyOf(client.getMenuEntries, client.getMenuEntries.length - 1))
//	}
//
	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		if(!curInRegion) return
		projectiles.appendAll(justSpawnedProjectiles)
		projectiles.filterInPlace(_._2.hasHit == false)
		justSpawnedProjectiles = List.empty

		spikeyBois.filterInPlace(x => {
			(client.getTickCount - x._1) < 2
		})

		val vardorvisOpt = ethanApiPlugin.collections.NPCs.search().withName("Vardorvis").nearestToPlayer().toScala
		val vardorvisPrayerOpt = vardorvisOpt.flatMap(v => Option.when(!v.isDead)(Prayer.PROTECT_FROM_MELEE))

		val projectilePrayerOpt = projectiles.headOption.filter(_._2.ticksRemaining <= 2).map(_._1).collect {
			case VardorvisProjectiles.Range => Prayer.PROTECT_FROM_MISSILES
			case VardorvisProjectiles.Mage => Prayer.PROTECT_FROM_MAGIC
		}

		val projectilePrayers = projectilePrayerOpt
			.orElse(vardorvisPrayerOpt)
			.toList
//			.foreach(p => {
//				CombatUtils.activatePrayer(p)
//			})
		val offensivePrayers = Option(config.offensivePrayer()).filter(_ => vardorvisOpt.isDefined).filter(_ != OffensivePrayer.NONE).map(_.getPrayer).toList
		val defensivePrayers = Option(config.defensivePrayer()).filter(_ => vardorvisOpt.isDefined).filter(_ != DefensivePrayer.NONE && !config.offensivePrayer().isDefensive).map(_.getPrayer).toList

		val enabledPrayers = projectilePrayers.appendedAll(offensivePrayers).appendedAll(defensivePrayers)
		CombatUtils.activatePrayers(enabledPrayers *)
//		if(config.offensivePrayer() != OffensivePrayer.NONE) {
//			config.offensivePrayer().getPrayer
//			if(!config.offensivePrayer().isDefensive && config.defensivePrayer() != DefensivePrayer.NONE) {
//				config.offensivePrayer().getPrayer, config.defensivePrayer().getPrayer)
//			}
//		}
	}
//
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

//	@Subscribe
//	def onChatMessage(event: ChatMessage): Unit = {
//		if(!curInRegion) return
//		if(event.getType == ChatMessageType.GAMEMESSAGE && event.getMessage.contains("entangles you in")) //solveDelay = 2
//	}

	@Subscribe
	def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
		if(!curInRegion) return
		if (event.getGraphicsObject.getId == SpotanimID.VFX_VARDORVIS_SPIKE_WARNING_01) {
			val wp = event.getGraphicsObject.getLocation.getTemplate
			spikeyBois.addOne(client.getTickCount -> wp)
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
//
	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		if(!curInRegion) return
		val npc = e.getNpc
		if(npc == null || !inBossArea(npc.templateLocation)) return

		if(npc.getId == NpcID.VARDORVIS_BIG_TENTACLE) {
			log.debug("Spawned axe on tick {} @ pos {}", client.getTickCount, npc.templateLocation)
		} else {
			log.info("Spawned {}", e.getNpc.niceString);
		}
	}

	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		if(!curInRegion) return
		val npc = e.getNpc
		if(npc == null || !inBossArea(npc.templateLocation)) return

		if(npc.getId == NpcID.VARDORVIS) {
			CombatUtils.deactivatePrayers(false)
		}
	}

	@Subscribe
	def onProjectileMoved(e: ProjectileMoved): Unit = {
		if(!curInRegion || projectiles.exists(_._2 == e.getProjectile) || justSpawnedProjectiles.exists(_._2 == e.getProjectile)) return
		VardorvisProjectiles.unapply(e.getProjectile).foreach(j => {
			log.debug("Projectile spawned {}({})", j._1, j._2)
			justSpawnedProjectiles = justSpawnedProjectiles.appended(j)
		})
	}
//
//	@Subscribe
//	def onAnimationChanged(e: AnimationChanged): Unit = {
//		if (!curInRegion) return
//	}

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
						case VardorvisProjectiles.Range => config.rangedProjectileColor()
						case VardorvisProjectiles.Mage => config.magicProjectileColor()
					}
				)
				.build()
		}).prepended(
			TitleComponent.builder().text("Projectiles").color(Color.CYAN).build()
		)

		val spikeyLines: Seq[LayoutableRenderableEntity] = spikeyBois.toList./*sortBy(_._1).*/map((tk, wp) => {
			val offset = client.getTickCount - tk
			LineComponent.builder()
				.leftColor(Color.WHITE).left(s"${wp}")
				.right(s"${offset}")
				.rightColor(
					offset match {
						case 0 => Color.RED
						case 1 => Color.ORANGE
						case 2 => Color.YELLOW
						case j if j > 2 => Color.CYAN
					}
				)
				.build()
		}).prepended(
			TitleComponent.builder().text("Spikes").color(Color.CYAN).build()
		)

			Seq(regionLine, bossLines, spikeyLines, projectileLines).flatMap{
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
					case VardorvisProjectiles.Range => config.rangedProjectileColor()
					case VardorvisProjectiles.Mage => config.magicProjectileColor()
				}
				overlays.renderProjectileOverlay(p, tp.entryName)(2, 2, color)
				overlays.renderTileOverlay(p.worldLocation, s"${p.ticksRemaining}", color, false)
			}
		null.asInstanceOf[Dimension]
	}
}
