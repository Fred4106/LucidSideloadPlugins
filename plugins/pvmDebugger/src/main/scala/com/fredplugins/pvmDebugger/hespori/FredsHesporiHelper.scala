package com.fredplugins.pvmDebugger.hespori

import com.fredplugins.common.ProjectileID
import com.fredplugins.common.constants.magic.SMagicBoost.{DeathCharge, SummonThrall}
import com.fredplugins.common.constants.magic.STimedPotion.Divine_combat
import com.fredplugins.common.extensions.ActorExtensions
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.services.TimedBoostsService.{MagicBoostChanged, getCachedValue, isActive, isLocked}
import com.fredplugins.common.services.TimedBoostsService
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{Equipment, Inventory}
import ethanApiPlugin.interactionApi.InventoryInteraction
import ethanApiPlugin.lucidplugins.api.utils.{CombatUtils, EquipmentUtils, InteractionUtils, InventoryUtils}
import ethanApiPlugin.services.localPlayer.events.LocalDestinationChanged
import ethanApiPlugin.services.localPlayer.events.LocalPositionChanged
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.{Actor, Client, EquipmentInventorySlot, GameObject, GameState, GraphicsObject, HeadIcon, NPC, NPCComposition, Perspective, Player, Point, Prayer, Projectile, WorldView}
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
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID}
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

//	/**
//	 * Hespori
//	 */
//	public static final int HESPORI = 8583;
//
//	/**
//	 * Flower
//	 */
//	public static final int HESPORI_HEALER_ACTIVE = 8584;
//
//	/**
//	 * &#60;col=00ffff&#62;Flower&#60;/col&#62;
//	 */
//	public static final int HESPORI_HEALER_INACTIVE = 8585;

@Singleton
class FredsHesporiHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsHesporiConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = "FredsHesporiHelper"
	private def clientThread  = parent.getClientThread
	given Client = client

	class Hespori(val wrapped: NPC) {
		assert(wrapped != null && Seq(NpcID.HESPORI, NpcID.LEAGUE_6_HESPORI).contains(wrapped.getId()))
		var lastAttack: Int = -1
		var lastAnimation: Int = -1
		var spawnTick: Int = -1
	}

	class Flower(val wrapped: NPC) {
		assert(wrapped != null && Seq(NpcID.HESPORI_HEALER_INACTIVE, NpcID.HESPORI_HEALER_ACTIVE).contains(wrapped.getId()))
		var overhead: HeadIcon = uninitialized
	}

	def hespori(arg: Actor): Option[Hespori] = {
		if (arg.isInstanceOf[NPC])
			Try(new Hespori(arg.asInstanceOf[NPC])).toOption
		else
			Option.empty
	}
	def flower(arg: Actor): Option[Flower] = {
		if (arg.isInstanceOf[NPC])
			Try(new Flower(arg.asInstanceOf[NPC])).toOption
		else
			Option.empty
	}

	val HesporiRegion: Int = 5021
//	case class WhirlwindData(spawnedTick: Int)

	//	private var ticks         = -1
	private var curRegion = -1;

	var boss: Hespori = uninitialized
	var flowers: List[Flower] = List.empty[Flower]
	var projectiles: List[Projectile] = List.empty[Projectile]

	private def clearState(): Unit = {
		boss = null
		flowers = List.empty[Flower]
		projectiles = List.empty[Projectile]
	}

	override def init(): Unit = {
		curRegion = Option(client.getLocalPlayer).map(_.templateLocation).map(_.getRegionID).getOrElse(-1)
		clearState()
	}

	override def cleanup(): Unit = {
		curRegion = -1
		clearState()
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		if(curRegion != HesporiRegion) return
		if(boss == null) {
			CombatUtils.deactivatePrayers(false)
			return
		}
		if(projectiles.map(_.getId).contains(ProjectileID.HESPORI_MAGIC_PROJ)) {
			CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MAGIC)
		} else {
			CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MISSILES)
		}

		val vineProjectileOpt: Option[Projectile]  = projectiles.find(_.getId == ProjectileID.HESPORI_VINE_PROJ)
		if(vineProjectileOpt.isDefined) log.debug("Vine projectile exists!!!")

		projectiles = projectiles.filterNot(_.hasHit)
//		if (castDeathCharge == false && !DeathCharge.isActive && !DeathCharge.isLocked) {
//			InteractionUtils.widgetInteract(InterfaceID.MagicSpellbook.DEATH_CHARGE, "Cast")
//			castDeathCharge = true
//		}
//
//		if (castThrall == false && !SummonThrall.isActive && !SummonThrall.isLocked) {
//			InteractionUtils.widgetInteract(InterfaceID.MagicSpellbook.RESURRECT_GREATER_ZOMBIE, "Cast")
//			castThrall = true
//		}
//
//		val handItem = EquipmentUtils.getItemInSlot(EquipmentInventorySlot.GLOVES)
//		if (boss.wrapped.healthPercent < 25
//			&& handItem.getId != ItemID.BRACELET_OF_SLAUGHTER
//			&& InventoryUtils.contains(ItemID.BRACELET_OF_SLAUGHTER)
//		) {
//			InventoryUtils.wieldItem(ItemID.BRACELET_OF_SLAUGHTER)
//		}
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
		if(e.getOldRegion == HesporiRegion) {
			clearState()
		}
		curRegion = e.getCurRegion
		if(curRegion == HesporiRegion) {
			client.getNpcs.asScala.toList.map(n =>  {
				new NpcSpawned(n)
			}).foreach(e => onNpcSpawned(e))
			client.getProjectiles.asScala.toList.map(p => {
				new ProjectileMoved().tap(_.setProjectile(p))
			}).foreach(e => onProjectileMoved(e))
		}
	}

	@Subscribe
	def onLocalDestinationChanged(e: LocalDestinationChanged): Unit = {
		if(curRegion == HesporiRegion) log.info(s"Destination changed from ${e.getFrom} to ${e.getTo}")
	}

	@Subscribe
	def onLocalPositionChanged(e: LocalPositionChanged): Unit = {
		if(curRegion == HesporiRegion) log.info(s"Position changed from ${e.getFrom} to ${e.getTo}")
	}

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		if(curRegion == HesporiRegion) {
			log.info("Spawned {}", e.getNpc.niceString);
			hespori(e.getActor).tapEach(_.spawnTick = client.getTickCount).foreach(u =>
				boss = u
			)
			flower(e.getActor).tapEach(f => {
					f.overhead = EthanApiPlugin.getHeadIcon(f.wrapped)
				}).foreach(f => {
					flowers = flowers.appended(f)
				})
//
//			WrappedNpc.tryBuild(e.getActor)
//				.foreach{
//					case sb: Hespori => boss = sb.tap(_.spawnTick = client.getTickCount)
//					case sb: Flower => flowers = flowers.appended(sb.tap(_.overhead_=(EthanApiPlugin.getHeadIcon(sb.wrapped))))
//				}
		}
	}

	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		if(curRegion == HesporiRegion) {
//			if(Option(boss).map(_.wrapped).contains(e.getNpc)){
//				clearState()
//			}
		}
	}

	@Subscribe
	def onProjectileMoved(e: ProjectileMoved): Unit = {
		val projectile: Projectile = e.getProjectile
		if (boss != null && !projectiles.contains(projectile) &&
			(e
				.getProjectile
				.templateSourceLocation
				.getRegionID == HesporiRegion || e
				.getProjectile
				.templateTargetLocation
				.getRegionID == HesporiRegion) &&
			projectile.justSpawned
		) {
			projectiles = projectiles.appended(projectile)
		}
	}

//	@Subscribe
//	def onMagicBoostChanged(e: MagicBoostChanged): Unit = {
//		log.debug(s"MagicBoostChanged {}",e)
//		if(e.oldValue.active == 1 && e.newValue.active == 0){
//			if(e.boost == DeathCharge) castDeathCharge = false
//			if(e.boost == SummonThrall) castThrall = false
//		}
//	}

//	@Subscribe
//	def onMagicBoostActiveChanged(e: MagicBoostActiveChanged): Unit = {
//		log.debug(s"MagicBoost {}'s active changed from {} to {}", e.boost, e.oldValue, e.newValue)
//		if(e.boost ==DeathCharge && e.newValue == 1 && e.oldValue == 0) {
//			castDeathCharge = false
//		}
//		if (e.boost == SummonThrall && e.newValue == 1 && e.oldValue == 0) {
//			castThrall = false
//		}
//	}
//	@Subscribe
//	def onTimedPotionChanged(e:TimedPotionValueChanged): Unit = {
//		log.debug(s"TimedPotion {}'s value changed from {} to {}", e.boost, e.oldValue, e.newValue)
//		if (e.boost == Divine_combat && e.newValue > e.oldValue) {
//			drinkCombatPotion = false
//		}
//	}

//	@Subscribe
//	def onGameObjectSpawned(e: GameObjectSpawned): Unit = {
//		if (curRegion == HesporiRegion) {
//			if (e.getGameObject.getId == 54279) {
//				iceTiles.add(e.getGameObject)
//			}
//		}
//	}
//
//	@Subscribe
//	def onGameObjectDespawned(e: GameObjectDespawned): Unit = {
//		if(curRegion == HesporiRegion){
////			iceTiles.remove(e.getGameObject)
//		}
//	}

	def decodeAnimationId(id: Int): String = {
		id match {
			case -1 => "IDLE"
			case 8221 => "SPAWN"
			case 8223 => "MAGE_ATTACK"
			case 8224 => "RANGE_ATTACK"
			case 8225 => "DEATH"
			case _ => s"UNKOWN(${id})"
		}
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		if (curRegion == HesporiRegion) {
			if(
				Option(boss).exists(_.wrapped == e.getActor)
			) {
				val aid = boss.wrapped.getAnimation
				if(List(8223, 8224).contains(aid)) {
					boss.lastAttack = client.getTickCount
				}
				boss.lastAnimation = aid
				log.debug(s"Hespori's animation changed to ${decodeAnimationId(aid)} (${aid})")
			}
		}
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {

		//		if(curRegion == HesporiRegion){
//		val divineCombatV = timedBoostsService.checkTimer(Divine_combat)
		val regionLine = LineComponent.builder().left("Region").right(s"$curRegion").rightColor(if(curRegion == HesporiRegion) Color.GREEN else Color.RED).build
//		val deathChargeLine = LineComponent.builder().left("Death Charge").right(s"${deathChargeV}").rightColor(if(deathChargeV.active == 1) Color.GREEN else (if(deathChargeV.cooldown == 1) Color.RED else Color.BLUE)).build
//		val thrallLine = LineComponent.builder().left("Thall").right(s"${thrallV}").rightColor(if(thrallV.active == 1) Color.GREEN else (if(thrallV.cooldown == 1) Color.RED else Color.BLUE)).build
//		val divineCombatLine = LineComponent.builder().left("Divine Combat").right(s"${divineCombatV}").rightColor(if(divineCombatV > 200) Color.GREEN else ColorUtil.colorLerp(Color.YELLOW, Color.RED,
//			Math.min(1.0d, Math.max(0.0d,(200 - divineCombatV).toDouble/200.0d))
//		)).build
		//			val bossLine = bossData.headOption.map((amox, amoxdata) =>{
		//				LineComponent.builder().left(amox.toString).right(amoxdata.toString).rightColor(if(client.getTickCount - amoxdata.lastAttackTick > 6) Color.RED else Color.BLUE).build
		//			}).toList

		val flowerLines = flowers.sortBy(_.wrapped.getIndex).map(f => {

			LineComponent.builder()
				.leftColor(Color.WHITE).left(s"F[${f.wrapped.getIndex}]@${f.wrapped.templateLocation}")
				.right(Option(f.overhead).fold("null")(_.name())).rightColor(if(f.overhead == null) Color.RED else Color.GREEN)
				.build
		}).prepended(
			TitleComponent.builder().text("Flowers").color(Color.CYAN).build()
		)

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

			Seq(regionLine, bossLines, flowerLines).flatMap{
				case e: LayoutableRenderableEntity => Seq(e)
				case le: Seq[_] => le.collect{
					case e: LayoutableRenderableEntity => e
				}
			}
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		given Graphics2D = g
		given ModelOutlineRenderer = parent.getModelOutlineRenderer
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

		if(curRegion == HesporiRegion){
//			dangerousTiles.values.toList.foreach(dt => {
//				renderDangerousTile(dt, Color.ORANGE)
//			})
//			iceTiles.toList.foreach(iceTile => {
//				renderIceTile(iceTile, Color.RED)
//			})

			Option(boss)
				.foreach {sb =>
					renderNpcOverlay(sb.wrapped, Color.CYAN)
					renderNpcText(sb.wrapped, s"T: ${client.getTickCount - sb.lastAttack}", Color.GRAY, 20)
				}

			projectiles
				.foreach {p =>
					import com.fredplugins.common.overlays

					Option(p.getId).collect {
							case ProjectileID.HESPORI_MAGIC_PROJ => (config.magicProjectileColor(), "Magic")
							case ProjectileID.HESPORI_RANGE_PROJ => (config.rangedProjectileColor(), "Range")
							case ProjectileID.HESPORI_VINE_PROJ => (config.aoeProjectileColor(), "Aoe")
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
