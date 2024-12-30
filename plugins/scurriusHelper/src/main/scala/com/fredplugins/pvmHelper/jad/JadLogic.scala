package com.fredplugins.pvmHelper.jad

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.pvmHelper.jad.TzMobType.*
import com.fredplugins.pvmHelper.jad.{*, given}
import com.fredplugins.pvmHelper.{BossToolTrait, FredsPvmHelperConfig, FredsPvmHelperPanel}
import com.google.gson.JsonObject
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import net.runelite.api.*
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.*
import net.runelite.client.config.*
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
import com.fredplugins.common.Locatable.{given, *}
import java.awt.Color
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.{ChatMessageType, Client, GameState, GraphicsObject, InventoryID, Item, ItemContainer, NPC, Prayer, Projectile, TileObject}
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.OverlayManager
import org.slf4j.Logger

import java.awt.Font
import java.util
import java.util.stream.Collectors
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.IntAccumulator
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.StreamHasToScala
import scala.util.{Random, Try}
import scala.reflect.Selectable.reflectiveSelectable
import scala.util.chaining.*
@PluginDescriptor(
	name = "<html><font color=\"#A1004B\">Freds</font> PVM Helper - Jad</html>",
	description = "Auto prayers against monsters in the Fight Caves",
	tags = Array("pvm", "jad", "prayer", "helper", "maps")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class JadLogic() extends Plugin with BossToolTrait {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: JadConfig = null
	@Inject val notifier: Notifier = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
//	@Inject private val configManager: ConfigManager = null
	private val panel: FredsPvmHelperPanel[JadLogic] = new FredsPvmHelperPanel(this) {}

//	private var justDodged: Boolean = false
//	private var lastDodgeTick: Int = 0
//	private var lastRatTick: Int = 0
//	private var lastActivateTick: Int = 0
//	private var fallingCeilingToTicks: Map[GraphicsObject, Int] = Map.empty //new HashMap<>();
	private var monsters: List[TzMob] = List.empty

	given Client = client

	@Provides
	def getConfig(configManager: ConfigManager): JadConfig = {
		configManager.getConfig[JadConfig](classOf[JadConfig])
	}

	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if (e.getGroup == JadConfig.GroupName) {
			e.getKey match {
				case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
			}
		}
	}
	
		def isFightCavesActive: Boolean = client.getTopLevelWorldView.getMapRegions.contains(9551)
	def isInTzhaarArea: Boolean = client.getTopLevelWorldView.getMapRegions.contains(9808) && !client.getTopLevelWorldView.isInstance

	override def layoutPanel(): Seq[LayoutableRenderableEntity] = {
		Seq(
			LineComponent.builder
				.left("isFightCavesActive")
				.right(s"${isFightCavesActive}")
				.build,
			LineComponent.builder
				.left("isInTzhaarArea")
				.right(s"${isInTzhaarArea}")
				.build/*,
			LineComponent.builder
				.left("lastDodgeTick")
				.right(s"${lastDodgeTick}")
				.build,
			LineComponent.builder
				.left("lastRatTick")
				.right(s"${lastRatTick}")
				.build,
			LineComponent.builder
				.left("lastActivateTick")
				.right(s"${lastActivateTick}")
				.build*/
		).appendedAll(
			monsters.map(m => {
				val correctPrayer = Option(m.tpe).collect {
					case KetZek => Prayer.PROTECT_FROM_MAGIC
					case  TokXil => Prayer.PROTECT_FROM_MISSILES
					case YtMejKot => Prayer.PROTECT_FROM_MELEE
				}
				LineComponent.builder
					.left(s"${m.tpe} ${m.convert.distanceTo(localPlayer)}")
					.right(s"${correctPrayer.map(_.toString).getOrElse("None")}")
					.build
			})
		).appended(
			LineComponent.builder
			.left(s"Requested Prayer")
			.right(s"${requestedJadPrayer}")
			.build
		).appended(
		LineComponent.builder
			.left(s"Ticks Since")
			.right(s"${client.getTickCount - requestedJadPrayerTime}")
			.build
		)
	}

	override protected def startUp(): Unit = {
		resetState()
		overlayManager.add(panel)
		//		overlayManager.add(overlay)
	}

	override def resetState(): Unit = {
		monsters = List.empty
		requestedJadPrayerTime = client.getTickCount
		requestedJadPrayer = None
	}

	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
		//		overlayManager.remove(overlay)
		resetState()
	}

//	@Subscribe
//	private def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
//		if (!inArea()) return
//		val graphicsObject = event.getGraphicsObject
//		val id = graphicsObject.getId
//		if (id == FALLING_CEILING_GRAPHIC) {
//			fallingCeilingToTicks = fallingCeilingToTicks.updated(graphicsObject, DURATION)
//		}
//	}

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		log.debug(s"Spawned ${event.getNpc.pipe(p => p.getId -> p.getName)}")
		TzMob(event.getNpc).foreach(mob => {
				monsters = (monsters :+ mob).sortBy(_.wrapped.distanceTo(localPlayer))
		})
	}

	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		log.debug(s"Despawned ${event.getNpc.pipe(p => p.getId -> p.getName)}")
		monsters = monsters.filterNot(_.wrapped == event.getNpc)
	}

	var requestedJadPrayer = Option.empty[Prayer]
	var requestedJadPrayerTime: Int = -1
	@Subscribe
	private def onAnimationChanged(event: AnimationChanged): Unit = {
		if (!event.getActor.isInstanceOf[NPC]) return
		if (!inArea()) return
		val npc = event.getActor.asInstanceOf[NPC]
		val toActOn = monsters.find(_.wrapped == npc).filter(_.tpe == TzTokJad)
			if(toActOn.isDefined) {
				val jad = toActOn.get
				val toRequest = Option(jad.wrapped.getAnimation).collect {
					case AnimationID.TZTOK_JAD_MAGIC_ATTACK => Prayer.PROTECT_FROM_MAGIC
					case AnimationID.TZTOK_JAD_RANGE_ATTACK => Prayer.PROTECT_FROM_MISSILES
				}.tap(op => if(op.isEmpty && jad.wrapped.getAnimation != -1) {
					log.debug("Unknown animation id {}", jad.wrapped.getAnimation)
				})
				if(toRequest.isDefined) {
					requestedJadPrayer = toRequest
					requestedJadPrayerTime = client.getTickCount
				}
			}
	}


//	@Subscribe
//	private def onProjectileMoved(event: ProjectileMoved): Unit = {
//		val projectile = event.getProjectile
//		val scurrius = NpcUtils.getNearestNpc("Scurrius")
//		val local = client.getLocalPlayer
//		if (!isScurrius(scurrius) || (projectile.getInteracting != local) || (projectile.getRemainingCycles != (projectile.getEndCycle - projectile.getStartCycle))) return
//
//		log.info(s"Projectile {} has {} cycles remaining", projectile.getId, projectile.getRemainingCycles)
//		if (projectile.getId != 2642 && projectile.getId != 2640) return
//
//		if (!attacks.contains(projectile)) {
//			attacks = attacks.appended(projectile)
//			if (config.autoPray) CombatUtils.deactivatePrayer(Prayer.PROTECT_FROM_MELEE)
//		}
//	}

//	@Subscribe
//	private def onHitsplatApplied(event: HitsplatApplied): Unit = {
//		if(!inArea()) return
//		if(event.getActor != localPlayer) return
//		if(!event.getHitsplat.isMine) return
//		if(requestedJadPrayer.isDefined) {
//			requestedJadPrayer = None
//		}
//	}

	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
		if (!inArea()) return //instancePoint.getRegionID != 13210 || instancePoint.getRegionX < 23) return
//		monsters.foreach(m => log.debug(s"${m.getId}, ${m.getName}, ${m.asInstanceOf[NPC]}"))
		if(!monsters.exists(_.tpe== TzTokJad)) {
			handlePrayers()
		} else {
			val jad: TzMob = monsters.find(_.tpe == TzTokJad).get
			val timeSinceReqeuest = client.getTickCount - requestedJadPrayerTime
			def mageAndRangeProjectiles(prayer: Prayer): List[Projectile] = {
				Option(prayer).collect {
					case Prayer.PROTECT_FROM_MAGIC => 448
					case Prayer.PROTECT_FROM_MISSILES => 449
				}.map(id =>
					client.getTopLevelWorldView.getProjectiles.asScala.toList.filter(p => p.getId == id)
				).getOrElse(List.empty)
			}
			if(requestedJadPrayer.map(mageAndRangeProjectiles).exists(_.isEmpty) && timeSinceReqeuest > 3) {
				requestedJadPrayer = None
				requestedJadPrayerTime = client.getTickCount
			}


//			{
//				case m if m.getId == 448 =>
//				case m if m.getId == 449 =>
//			}
//
//			448 TzTok-Jad Magic Projectile
//			449 - TzTok-Jad Range Projectile

			requestedJadPrayer match {
				case Some(p) => CombatUtils.activatePrayer(p)
				case None => CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE)
			}

			if(jad.wrapped.isDead) {
				CombatUtils.deactivatePrayers(false)
			} else {
				CombatUtils.activatePrayer(Prayer.ULTIMATE_STRENGTH)
				CombatUtils.activatePrayer(Prayer.INCREDIBLE_REFLEXES)
			}
			//handle jad
		}
//		attacks = attacks.filter((proj: Projectile) => proj.getRemainingCycles > 30)
//		justDodged = false
//		if (fallingCeilingToTicks.nonEmpty) {
//			dodgeFallingCeiling()
//			fallingCeilingToTicks = (fallingCeilingToTicks.toList.map(in => in._1 -> (in._2 - 1)).filter(_._2 > 0)).toMap
//			//			fallingCeilingToTicks.filterInPlace((_: GraphicsObject, v: Int) => v > 0)
//		}
//		val scurrius = NpcUtils.getNearestNpc("Scurrius")
//		if (!justDodged) {
//			if (config.attackAfterDodge && (client.getLocalPlayer.getInteracting ne scurrius)) {
//				val tSinceLastDodge = client.getTickCount - lastDodgeTick
//				if (tSinceLastDodge < 3) if (scurrius != null) if (!config.prioritizeRats || getEligibleRat == null) NpcUtils.attackNpc(scurrius)
//			}
//		}
//		var attackRat = true
//		if (scurrius != null) {
//			val ratio = scurrius.getHealthRatio
//			val scale = scurrius.getHealthScale
//			val targetHpPercent = ratio.toDouble / scale.toDouble * 100
//			if (targetHpPercent > 0) attackRat = false
//		}
//		if (justDodged) return
//		if (config.attackRats && attackRat || config.prioritizeRats) {
//			val giantRat = getEligibleRat
//			if (giantRat != null && (giantRat ne client.getLocalPlayer.getInteracting)) {
//				NpcUtils.attackNpc(giantRat)
//				lastRatTick = client.getTickCount
//			}
//			else if (config.prioritizeRats && giantRat == null) {
//				val tSinceLatRatHit = client.getTickCount - lastRatTick
//				if (scurrius != null && tSinceLatRatHit < 8 && (client.getLocalPlayer.getInteracting ne scurrius)) NpcUtils.attackNpc(scurrius)
//			}
//		}
	}

//	def shouldPrayAgainst(tzMob: TzMob): Boolean = {
//		val v3 = (tzMob.tpe == KetZek && config.autoPrayMage() && tzMob.distanceTo(localPlayer) < 18)
//		val v1 = (tzMob.tpe == TokXil && config.autoPrayRange() && tzMob.distanceTo(localPlayer) < 18)
//		val v2 = (tzMob.tpe == YtMejKot && config.autoPrayMelee() && tzMob.distanceTo(localPlayer) < 3)
//		v1 || v2 || v3
//	}

	def shouldPrayAgainst(tzMob: TzMob): Option[Prayer] = {
		Option(tzMob).collect({
			case t if t.tpe == KetZek && t.distanceTo(localPlayer) < 20 && config.autoPrayMage() => Prayer.PROTECT_FROM_MAGIC
			case t if t.tpe == TokXil && t.distanceTo(localPlayer) < 20 && config.autoPrayRange() => Prayer.PROTECT_FROM_MISSILES
			case t if t.tpe == YtMejKot && t.distanceTo(localPlayer) < 4 && config.autoPrayMelee() => Prayer.PROTECT_FROM_MELEE
		})
	}
	private def handlePrayers(): Unit = {
		//		val toMatch = List(KetZek -> Prayer.PROTECT_FROM_MAGIC, TokXil ->Prayer.PROTECT_FROM_MISSILES, YtMejKot ->Prayer.PROTECT_FROM_MELEE)

		val monstersToPrayAgainst: List[TzMob] = monsters.filter(shouldPrayAgainst(_).isDefined).sortBy(_.distanceTo(localPlayer)).groupBy(_.tpe).toList.sortBy(_._1.ordinal).reverse.flatMap(_._2)

		monstersToPrayAgainst.flatMap(shouldPrayAgainst).headOption match {
			case Some(value) => CombatUtils.activatePrayer(value)
			case None => CombatUtils.deactivatePrayers(true)
		}
	}

	override def inArea(): Boolean =
		localPlayer.findWorldCord.exists(_.getRegionID == 9551)

	inline def localPlayer: Player = client.getLocalPlayer
	inline def getLocalPlayerWorldPoint: WorldPoint = WorldPoint.fromLocalInstance(client, localPlayer.getLocalLocation)
}
