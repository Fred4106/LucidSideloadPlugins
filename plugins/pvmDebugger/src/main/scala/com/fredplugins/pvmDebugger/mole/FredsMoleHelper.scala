package com.fredplugins.pvmDebugger.mole

import com.fredplugins.attacktimer.LocalPlayerAttacked
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.constants.magic.STimedPotion
import com.fredplugins.common.constants.magic.STimedPotion.{Divine_range, Stamina}
import com.fredplugins.common.extensions.ActorExtensions
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.services.TimedBoostsService
import com.fredplugins.common.services.TimedBoostsService.{PotionEffectChanged, getCachedValue, isActive, isLocked, niceName}
import com.fredplugins.common.utils.{ReflectionUtils, SInteractionUtils, WorldAreaExtended, WorldAreaExtended$}
import com.fredplugins.common.{ProjectileID, overlays}
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.{HelperModule, PvmDebuggerPlugin, WithOverlay, WithPanel}
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.collections.{Equipment, Inventory, TileObjects}
import ethanApiPlugin.interactionApi.InventoryInteraction
import ethanApiPlugin.lucidplugins.api.utils.{CombatUtils, EquipmentUtils, InteractionUtils, InventoryUtils, Reachable}
import ethanApiPlugin.services.localPlayer.events.{LocalDestinationChanged, LocalPositionChanged, LocalRegionChanged}
import net.runelite.api.coords.{Direction, LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.{ActorDeath, AnimationChanged, GameObjectDespawned, GameObjectSpawned, GameStateChanged, GameTick, GraphicsObjectCreated, NpcDespawned, NpcSpawned, ProjectileMoved}
import net.runelite.api.gameval.AnimationID.{MOLE_ATTACK, MOLE_BURROW_DOWN, MOLE_BURROW_UP, MOLE_DEATH, MOLE_DEFEND, MOLE_MUD_CLOUD, MOLE_MUD_HOLE, MOLE_MUD_HOLE_UP, MOLE_MUD_SPLAT, MOLE_MUD_SPLAT_INTERFACE, MOLE_READY, MOLE_WALK}
import net.runelite.api.gameval.ItemID.{_1DOSEDIVINERANGE, _1DOSESTAMINA, _2DOSEDIVINERANGE, _2DOSESTAMINA, _3DOSEDIVINERANGE, _3DOSESTAMINA, _4DOSEDIVINERANGE, _4DOSESTAMINA}
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID, ObjectID}
import net.runelite.api.{Actor, Client, EquipmentInventorySlot, GameObject, GameState, GraphicsObject, NPC, NPCComposition, Perspective, Player, Point, Prayer, Projectile, Skill, WorldView}
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, ProgressPieComponent, TitleComponent}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.util.ColorUtil

import java.awt.{Color, Dimension, Graphics2D, Polygon, Shape}
import java.util.concurrent.Callable
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

class Mole(val wrapped: NPC) {
	assert(wrapped != null && wrapped.getId == NpcID.MOLE_GIANT)
	var lastAttack: Int = -1
	var spawnTick: Int = -1
}
object Mole {
	def tryBuild(arg: Actor): Option[Mole] = {
		Option(arg).collect {
			case npc: NPC if npc.getId == NpcID.MOLE_GIANT => Mole(npc)
		}
	}
}
@Singleton
class FredsMoleHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsMoleConfig, val timedBoostsService: TimedBoostsService) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsMoleConfig.GROUP
	private def clientThread  = parent.getClientThread
	given Client = client
	val PrayerPotIds = List(ItemID._4DOSEPRAYERRESTORE, ItemID._3DOSEPRAYERRESTORE, ItemID._2DOSEPRAYERRESTORE, ItemID._1DOSEPRAYERRESTORE)
	def prayerPotDose(id: Int): Int = PrayerPotIds.indexOf(id)
	val MoleRegion: Set[Int] = Set(6992, 6993)
	private var curRegion = -1;

	var boss: Mole = uninitialized
	var oldBoss: Mole = uninitialized
	var drinkPotionAt: Int = 0
	private var prayerOnTick = -1

	private def clearState(): Unit = {
		boss = null
		oldBoss = null
		prayerOnTick = -1
	}

	override def init(): Unit = {
		curRegion = Option(client.getLocalPlayer).map(_.templateLocation).map(_.getRegionID).getOrElse(-1)
		for (npc <- client.getTopLevelWorldView.npcs.asScala) {
			onNpcSpawned(new NpcSpawned(npc))
		}
	}

	override def cleanup(): Unit = {
		curRegion = -1
		clearState()
	}
	def inBowfaRange(using client: Client): Boolean = {
		if(boss == null) return false
		if(oldBoss != null) return false
		Option(client.getLocalPlayer).exists { lp =>
			val wa = lp.getWorldArea
			wa.hasLineOfSightTo(lp.getWorldView, boss.wrapped.getWorldArea) && wa.distanceTo(boss.wrapped.getWorldArea) <= 10
		}
	}

	def inMeleeRange: Boolean = {
		if (boss == null) return false
		if (oldBoss != null) return false
		val lpa = client.getLocalPlayer.getWorldArea
		val curArea = boss.wrapped.getWorldArea
		val nextArea = WorldAreaExtended.calculateNextTravellingPoint(client, curArea, lpa, true)
		List(boss.wrapped.getWorldArea, nextArea).exists(_.isInMeleeDistance(lpa))
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		if(boss == null) return

		val prayerPotWidget = Inventory.search()
			.withId(PrayerPotIds *)
			.result().asScala.toList.maxByOption(w => prayerPotDose(w.getItemId)).orNull
		if (prayerPotWidget != null && config.prayerPotEnabled() && (client.getTickCount - drinkPotionAt) > 5 && CombatUtils.getRestoreAmount(prayerPotWidget) < CombatUtils.getPrayerPointsMissing + 10) {
			InteractionUtils.widgetInteract(prayerPotWidget, "drink")
			drinkPotionAt = client.getTickCount
		}


		if (config.divineRangeEnabled() && (client.getTickCount - drinkPotionAt) > 5 && Divine_range.getCachedValue < 15) {
			Inventory.search()
				.withId(_4DOSEDIVINERANGE, _3DOSEDIVINERANGE, _2DOSEDIVINERANGE, _1DOSEDIVINERANGE)
				.result().asScala.toList.maxByOption(w => w.getItemId)
				.foreach { divinePotionWidget =>
					InteractionUtils.widgetInteract(divinePotionWidget, "drink")
					drinkPotionAt = client.getTickCount
				}
		}

		if (config.staminaEnabled() && (client.getTickCount - drinkPotionAt) > 5 && Stamina.getCachedValue < 2 && InteractionUtils.getRunEnergy < 60) {
			Inventory.search()
				.withId(_4DOSESTAMINA, _3DOSESTAMINA, _2DOSESTAMINA, _1DOSESTAMINA)
				.result().asScala.toList.maxByOption(w => w.getItemId)
				.foreach { staminaWidget =>
					InteractionUtils.widgetInteract(staminaWidget, "drink")
					drinkPotionAt = client.getTickCount
				}
		}

		if (config.deadeyeEnabled()) {
			if (inBowfaRange) {
				if (!config.deadeyeFlick() || client.getTickCount >= prayerOnTick) CombatUtils.activatePrayer(Prayer.DEADEYE)
			} else {
				CombatUtils.deactivatePrayer(Prayer.DEADEYE)
			}
		}

		if (config.protectFromMeleeEnabled()) {
			(if (inMeleeRange) CombatUtils.activatePrayer else CombatUtils.deactivatePrayer)(Prayer.PROTECT_FROM_MELEE)
		}
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
		if(MoleRegion.contains(e.getOldRegion) && !MoleRegion.contains(e.getCurRegion)) {
			clearState()
		}
		curRegion = e.getCurRegion
	}

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		if(MoleRegion.contains(e.getNpc.templateRegion)) {
			Mole.tryBuild(e.getActor).tapEach(_.spawnTick = if(oldBoss != null) oldBoss.spawnTick else client.getTickCount)
				.foreach{ boss = _ }
			oldBoss = null
		}
	}

	@Subscribe
	def onNpcDespawned(e: NpcSpawned): Unit = {
		if(boss != null && e.getNpc == boss.wrapped) {
			val oldBoss = boss
		}
	}

	@Subscribe
	def onActorDeath(e: ActorDeath): Unit = {
		e.getActor.getAsNpc().foreach{npc =>
			if (MoleRegion.contains(npc.templateRegion)) {
				if (Option(boss).map(_.wrapped).contains(npc)) {
					CombatUtils.deactivatePrayers(List(
							Option.when(config.deadeyeEnabled)(Prayer.DEADEYE),
							Option.when(config.protectFromMeleeEnabled)(Prayer.PROTECT_FROM_MELEE))
						.flatMap(_.toList)
						.filter(CombatUtils.isActive) *
					)
					clearState()
				}
			}
		}
	}

	def decodeAnimationId(id: Int): String = {
		id match {
			case MOLE_READY => "Ready"
			case MOLE_DEATH => "Death"
			case MOLE_DEFEND => "Defend"
			case MOLE_ATTACK => "Attack"
			case MOLE_WALK => "Walk"
			case MOLE_BURROW_DOWN => "Burrow Down"
			case MOLE_BURROW_UP => "Burrow Up"
			case MOLE_MUD_SPLAT => "Splat"
			case MOLE_MUD_CLOUD => "Cloud"
			case MOLE_MUD_HOLE => "Hole Down"
			case MOLE_MUD_HOLE_UP => "Hole Up"
			case MOLE_MUD_SPLAT_INTERFACE => "Splat Interface"
			case -1 => "IDLE"
			case _ => s"UNKOWN(${id})"
		}
	}

	@Subscribe
	def onLocalPlayerAttacked(e: LocalPlayerAttacked): Unit = {
		prayerOnTick = client.getTickCount + e.getAttackInterval - 1
		if(config.deadeyeFlick()) CombatUtils.deactivatePrayers(Prayer.DEADEYE)
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		if (MoleRegion.contains(e.getActor.templateRegion)) {
			if(
				Option(boss).exists(_.wrapped == e.getActor)
			) {
				val aid = boss.wrapped.getAnimation
				if(aid == MOLE_ATTACK) {
					boss.lastAttack = client.getTickCount
				}
				log.debug(s"Mole's animation changed to ${decodeAnimationId(aid)} (${aid})")
			}
		}
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		def potionLine(max: Int, potion: STimedPotion): LineComponent = {
			val v = potion.getCachedValue
			val percentage = Math.min(1.0d, Math.max(0.0d, (max - v).toDouble / max.toDouble))

			LineComponent.builder().left(potion.niceName).right(s"${v}").rightColor(
				ColorUtil.colorLerp(Color.YELLOW, Color.RED, percentage)
			).build
		}
//		if(!MoleRegion.contains(curRegion)) return Seq.empty

		val regionLine = LineComponent.builder().left("Region").right(s"$curRegion").rightColor(if(MoleRegion.contains(curRegion)) Color.GREEN else Color.RED).build
		val divineRangeLine =  potionLine(500, Divine_range)
		val staminaLine = potionLine(200, Stamina)

		val bossLines = Option(boss)
			.map(b => {
				Seq(
					(Color.WHITE, "id", Color.BLUE, b.wrapped.getId),
					(Color.WHITE, "animation", Color.BLUE, decodeAnimationId(b.wrapped.getAnimation)),
					(Color.WHITE, "age", Color.BLUE, client.getTickCount - b.spawnTick),
					(Color.WHITE, "idleTicks", Color.BLUE, client.getTickCount - b.lastAttack),
					(Color.WHITE, "tLoc", Color.BLUE, b.wrapped.templateLocation)
				)
			})
			.getOrElse(List.empty)
			.map{
				case (color, str, color1, i) =>
					LineComponent.builder()
						.leftColor(color).left(str)
						.right(s"${i}").rightColor(color1)
						.build
			}

			Seq(regionLine, divineRangeLine, staminaLine, bossLines).flatMap{
				case e: LayoutableRenderableEntity => Seq(e)
				case le: Seq[_] => le.collect{
					case e: LayoutableRenderableEntity => e
				}
			}
	}

	override def renderOverlay(g: Graphics2D): Dimension = {
		given Graphics2D = g
		given ModelOutlineRenderer = parent.getModelOutlineRenderer
		def renderNpcOverlay(n:Mole, color: Color): Unit = {
			parent.getModelOutlineRenderer.drawOutline(n.wrapped, 2,  color, 4)
			val poly = n.wrapped.getCanvasTilePoly
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
//			val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
//			if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}

		def renderNpcText(n: Mole, text: String, color: Color, zoffset: Int): Unit = {
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

		def renderTile(tile: GameObject, color: Color): Unit = {
			val lp   = tile.getLocalLocation
			val poly = Perspective.getCanvasTilePoly(client, lp)
			if (poly != null) OverlayUtil.renderPolygon(g, poly, ColorUtil.colorWithAlpha(color, 200))
		}

		if(MoleRegion == MoleRegion){
			Option(boss)
				.foreach {sb =>
				renderNpcOverlay(sb, config.moleColor())
				renderNpcText(sb, s"T: ${client.getTickCount - sb.lastAttack}", Color.GRAY, 20)
			}
		}
		null.asInstanceOf[Dimension]
	}
}
