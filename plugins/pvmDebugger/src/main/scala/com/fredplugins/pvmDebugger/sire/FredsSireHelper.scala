package com.fredplugins.pvmDebugger.sire

import com.fredplugins.attacktimer.{AttackStyle, LocalPlayerAttacked}
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.constants.magic.SMagicBoost.{DeathCharge, SummonThrall}
import com.fredplugins.common.constants.magic.STimedPotion.{Divine_combat, Prayer_regeneration}
import com.fredplugins.common.extensions.ActorExtensions
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.withAlpha
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.{ProjectileID, overlays}
import com.fredplugins.common.services.TimedBoostsService
import com.fredplugins.common.services.TimedBoostsService.{MagicBoostChanged, PotionEffectChanged, getCachedValue, isActive, isLocked}
import com.fredplugins.common.utils.{ReflectionUtils, SInteractionUtils}
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.{HelperModule, PvmDebuggerPlugin, WithOverlay, WithPanel}
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.collections.{Equipment, Inventory, NPCs, TileObjects}
import ethanApiPlugin.interactionApi.InventoryInteraction
import ethanApiPlugin.lucidplugins.api.utils.{CombatUtils, EquipmentUtils, InteractionUtils, InventoryUtils}
import ethanApiPlugin.services.localPlayer.events.{LocalDestinationChanged, LocalPositionChanged, LocalRegionChanged}
import net.runelite.api.coords.{Direction, LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.{AnimationChanged, GameObjectDespawned, GameObjectSpawned, GameStateChanged, GameTick, GraphicsObjectCreated, NpcDespawned, NpcSpawned, ProjectileMoved}
import net.runelite.api.gameval.ItemID.{BRACELET_OF_SLAUGHTER, HUNDRED_GAUNTLETS_LEVEL_10}
import net.runelite.api.gameval.ObjectID1.{GRYPHON_BOSS_WHIRLWIND_ACTIVE_1 as ACTIVE_1, GRYPHON_BOSS_WHIRLWIND_ACTIVE_2 as ACTIVE_2, GRYPHON_BOSS_WHIRLWIND_ACTIVE_3 as ACTIVE_3, GRYPHON_BOSS_WHIRLWIND_ACTIVE_4 as ACTIVE_4, GRYPHON_BOSS_WHIRLWIND_ACTIVE_5 as ACTIVE_5, GRYPHON_BOSS_WHIRLWIND_INITIAL_1 as INITIAL_1, GRYPHON_BOSS_WHIRLWIND_INITIAL_2 as INITIAL_2, GRYPHON_BOSS_WHIRLWIND_INITIAL_3 as INITIAL_3, GRYPHON_BOSS_WHIRLWIND_INITIAL_4 as INITIAL_4, GRYPHON_BOSS_WHIRLWIND_INITIAL_5 as INITIAL_5}
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID, ObjectID}
import net.runelite.api.{Actor, Client, EquipmentInventorySlot, GameObject, GameState, GraphicsObject, NPC, NPCComposition, Perspective, Player, Point, Prayer, Projectile, WorldView}
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, ProgressPieComponent, TitleComponent}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.util.ColorUtil

import java.awt.{Color, Dimension, Graphics2D, Polygon, Rectangle, Shape}
import java.util.concurrent.Callable
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class FredsSireHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsSireConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = ConfigDef.Group
	private def clientThread  = parent.getClientThread
	given Client = client

	private var curRegion: Int = -1

	override def init(): Unit = {
		curRegion = Option(client.getLocalPlayer).map(_.templateLocation).map(_.getRegionID).getOrElse(-1)
		for (npc <- client.getTopLevelWorldView.npcs.asScala) {
//			onNpcSpawned(new NpcSpawned(npc))
		}
	}

	override def cleanup(): Unit = {
		curRegion = -1
		clearState()
	}

	var prayerOnTick: Int = -1
	var offensivePrayer: Prayer = uninitialized

	private def clearState(): Unit = {
		prayerOnTick = -1
		offensivePrayer= null
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		if(!sireRegions.contains(curRegion)) return
//		boss = NPCs.search().withId(NpcID.MUSPAH_TELEPORT).first().toScala
		if(prayerOnTick == client.getTickCount && offensivePrayer != null) {
			CombatUtils.activatePrayers(offensivePrayer)
		}
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
		if(e.getCurRegion == 12106) {
			clearState()
		}
		curRegion = e.getCurRegion
	}

//	@Subscribe
//	def onGameObjectSpawned(e: GameObjectSpawned): Unit = {
//		if (e.getGameObject.getId >= INITIAL_1 && e.getGameObject.getId <= ACTIVE_5) {
//			whirlwinds.addOne(WhirlwindData(e.getGameObject, client.getTickCount))
//		}
//	}
//
//	@Subscribe
//	def onGameObjectDespawned(e: GameObjectDespawned): Unit = {
//		whirlwinds.filterInPlace(_.go != e.getGameObject)
//	}

//	@Subscribe
//	def onNpcSpawned(e: NpcSpawned): Unit = {
//		if(e.getNpc.templateRegion == ShellsbaneRegion) {
//			Shellsbane.tryBuild(e.getActor)
//				.foreach{sb =>
//					boss = sb.tap(_.spawnTick = client.getTickCount)
//					CombatUtils.activatePrayers(Prayer.PIETY, Prayer.PROTECT_FROM_MELEE)
//				}
//		}
//	}
//
//	@Subscribe
//	def onNpcDespawned(e: NpcDespawned): Unit = {
//		if(e.getNpc.templateRegion == ShellsbaneRegion) {
//			if(Option(boss).map(_.wrapped).contains(e.getNpc)){
//				if(InventoryUtils.contains(HUNDRED_GAUNTLETS_LEVEL_10)) {
//					InventoryUtils.wieldItem(HUNDRED_GAUNTLETS_LEVEL_10)
//					equipSlaughter = false
//				}
//				CombatUtils.deactivatePrayers(false)
//				clearState()
//			}
//		}
//	}

//	@Subscribe
//	def onProjectileMoved(e: ProjectileMoved): Unit = {
//		val projectile: Projectile = e.getProjectile
//		if (boss != null && !projectiles.contains(projectile) &&
//			(e
//				.getProjectile
//				.templateSourceLocation
//				.getRegionID == ShellsbaneRegion || e
//				.getProjectile
//				.templateTargetLocation
//				.getRegionID == ShellsbaneRegion
//			) &&
//			projectile.justSpawned
//		) {
//			projectiles = projectiles.appended(projectile)
//			if(projectile.getId == ProjectileID.GRYPHON_SPIT_PROJECTILE) {
//				moveBack = true
//			}
//		}
//	}

	@Subscribe
	def onLocalPlayerAttacked(e: LocalPlayerAttacked): Unit = {
		prayerOnTick = client.getTickCount + e.getAttackInterval - 1
		offensivePrayer =
			e.getStyle match {
				case AttackStyle.RANGING => Prayer.DEADEYE
				case AttackStyle.LONGRANGE => Prayer.DEADEYE
				case AttackStyle.CASTING => Prayer.MYSTIC_VIGOUR
				case AttackStyle.DEFENSIVE_CASTING => Prayer.MYSTIC_VIGOUR
				case _ => Prayer.PIETY
			}
		CombatUtils.deactivatePrayers(Prayer.PIETY, Prayer.MYSTIC_VIGOUR, Prayer.DEADEYE)
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		if(curRegion != 12106 && !sireRegions.contains(curRegion)) return Seq.empty[LayoutableRenderableEntity]

		val regionLine = LineComponent.builder().left("Region").right(s"$curRegion").rightColor(if(sireRegions.contains(curRegion)) Color.GREEN else if(curRegion == 12106) Color.YELLOW else Color.RED).build
		val myNpcs: List[(NpcType, NPC)] = NPCs.search().alive().result().asScala.toList.flatMap(NpcType.unapply).sortBy(_._1.pipe(NpcType.values.indexOf(_)))

		val npcLines = myNpcs.map {
			case (NpcType.Sire, n) => {
				val mode = SireMode.unapply(n).map(_._1).get
				val sireColor = mode match {
					case SireMode.Sleeping => config.sireSleepingColor
					case SireMode.Awake => config.sireAwakeColor
					case SireMode.Stunned => config.sireStunnedColor
					case SireMode.Puppet => config.sirePuppetColor
					case SireMode.Wandering => config.sireWanderingColor
					case SireMode.Panicking => config.sirePanickingColor
					case SireMode.Apocalypse => config.sireApocalypseColor
				}
				LineComponent.builder().left(s"Sire ${mode}[${n.getIndex}]").leftColor(sireColor) -> n
			}
			case (tpe, n) => {
				LineComponent.builder().left(s"${tpe}[${n.getIndex}]").leftColor(if(!n.isDead) Color.GREEN else Color.RED) -> n
			}
		}.map{
			case (lc, n) => {
				lc.right(s"a=${ReflectionUtils.getAnimationName(n.getAnimation)} @ ${n.getWorldLocation}").build()
			}
		}
		Seq(regionLine, npcLines).flatMap{
			case e: LayoutableRenderableEntity => Seq(e)
			case le: Seq[_] => le.collect{
				case e: LayoutableRenderableEntity => e
			}
		}
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		given Graphics2D = g
		given ModelOutlineRenderer = parent.getModelOutlineRenderer
		def renderNpcOverlay(npc: NPC, text: String)(zoffset: Int, color: Color, textColor: Color = Color.WHITE ): Unit = {
			var poly = npc.getConvexHull
			if(poly!= null) OverlayUtil.renderPolygon(g, poly, Color(0, 0, 0, 0), color.withAlpha(config.fillAlpha))
			parent.getModelOutlineRenderer.drawOutline(npc, 2, color, 2)
			//			val poly = npc.getCanvasTilePoly
			//			if (poly != null) OverlayUtil.renderPolygon(g, poly, fillColor)
			if(text != null && text.nonEmpty) {
				val textLocation = npc.getCanvasTextLocation(g, text, npc.getLogicalHeight + zoffset)
				if (textLocation != null) {
					val textBounds = g.getFontMetrics.getStringBounds(text, g)
					val offset = 4
					val textArea = Rectangle(textBounds.getX.toInt - offset, textBounds.getY.toInt - offset, textBounds.getWidth.toInt + offset + offset, textBounds.getHeight.toInt + offset + offset)
					//				val textArea = new Rectangle(textLocation.getX + (textBounds.getWidth / 2.0).toInt - , textLocation.getY, textBounds.getWidth.toInt, textBounds.getHeight.toInt)
					OverlayUtil.renderPolygon(g, textArea, new Color(255 - textColor.getRed, 255 - textColor.getGreen, 255 - textColor.getBlue, config.fillAlpha))
					OverlayUtil.renderTextLocation(g, textLocation, text, textColor)
				}
			}
		}

		if (curRegion == 12106 || sireRegions.contains(curRegion)) {
			NPCs.search().alive().filter(NpcType.Sire.unapply).nearestToPlayer().toScala.flatMap(SireMode.unapply)
				.foreach {(sireType, sire) =>
					val sireColor = sireType match {
						case SireMode.Sleeping => config.sireSleepingColor
						case SireMode.Awake => config.sireAwakeColor
						case SireMode.Stunned => config.sireStunnedColor
						case SireMode.Puppet => config.sirePuppetColor
						case SireMode.Wandering => config.sireWanderingColor
						case SireMode.Panicking => config.sirePanickingColor
						case SireMode.Apocalypse => config.sireApocalypseColor
					}

					renderNpcOverlay(sire, sireType.entryName + " | " + ReflectionUtils.getAnimationName(sire.getAnimation))(40, sireColor)
				}
			//			Option(boss)
//				.foreach {sb =>
//				renderNpcOverlay(sb, Color.CYAN)
//				renderNpcText(sb, s"T: ${client.getTickCount - sb.lastAttack}", Color.GRAY, 20)
//			}
		}
		null.asInstanceOf[Dimension]
	}
}
