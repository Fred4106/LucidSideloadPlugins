package com.fredplugins.pvmHelper.jad

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
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

import java.awt.Color
import scala.reflect.Selectable.reflectiveSelectable
import scala.util.Try
import scala.util.chaining.*
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
import scala.util.chaining.*
import scala.util.{Random, Try}

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
	private var monsters: List[NPC] = List.empty

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
			monsters.flatMap(m => {
				val correctPrayer = Option(m).collect {
					case kz if isKetZek(kz) => Prayer.PROTECT_FROM_MAGIC
					case kz if isTokXil(kz) => Prayer.PROTECT_FROM_MISSILES
					case kz if isYtMejKot(kz) => Prayer.PROTECT_FROM_MELEE
				}.map(p => m -> p)
				correctPrayer.toList
			}).map{
				case (m, p) =>					LineComponent.builder
					.left(s"${m.getName} ${m.getId}")
					.right(s"${p}")
					.build
			}
		)
	}

	override protected def startUp(): Unit = {
		resetState()
		overlayManager.add(panel)
		//		overlayManager.add(overlay)
	}

	override def resetState(): Unit = {
		monsters = List.empty
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

	//mage
	inline def isKetZek(npc: NPC): Boolean = npc != null && (npc.getId == 3125 || npc.getId == 3126)
	//range
	inline def isTokXil(npc: NPC): Boolean = npc != null && (npc.getId == 3121 || npc.getId == 3122)
	inline def isYtMejKot(npc: NPC): Boolean = npc != null && (npc.getId == 3123 || npc.getId == 3124)



	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		log.debug(s"Spawned ${event.getNpc.pipe(p => p.getId -> p.getName)}")
		monsters = monsters.appended(event.getNpc)
	}

	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		log.debug(s"Despawned ${event.getNpc.pipe(p => p.getId -> p.getName)}")
		monsters = monsters.filter(_ != event.getNpc)
	}

//	@Subscribe
//	private def onAnimationChanged(event: AnimationChanged): Unit = {
//		if (!event.getActor.isInstanceOf[NPC]) return
//		if (!inArea()) return
//		if (!inArea()) return
//		val npc = event.getActor.asInstanceOf[NPC]
//		if (isScurrius(npc) && npc.getAnimation == 10705 && NpcUtils.getNearestNpc("Giant rat") == null) {
//			if (config.autoPray) {
//				CombatUtils.deactivatePrayers(false)
//			}
//		}
//	}
//
//
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

	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
		if (!inArea()) return //instancePoint.getRegionID != 13210 || instancePoint.getRegionX < 23) return
		monsters.foreach(m => log.debug(s"${m.getId}, ${m.getName}, ${m.getWorldLocation}"))
		handlePrayers()
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

	private def handlePrayers(): Unit = {
		if (config.autoPrayMage() && monsters.filter(isKetZek(_)).nonEmpty) {
			CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MAGIC)
		} else if(config.autoPrayRange() && monsters.filter(isTokXil(_)).nonEmpty) {
			CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MISSILES)
		} else if(config.autoPrayMelee() && monsters.filter(isYtMejKot(_)).nonEmpty) {
			CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE)
		} else {
			CombatUtils.deactivatePrayers(true)
		}

		//		import scala.collection.JavaConversions._
//		for (projectile <- attacks) {
//			val cyclesToTicks = Math.floor(projectile.getRemainingCycles / 30.0F).toInt
//			if (cyclesToTicks <= 1) {
//				if (projectile.getId == 2642) {
//					prayer = Prayer.PROTECT_FROM_MISSILES
//				} else {
//					prayer = Prayer.PROTECT_FROM_MAGIC
//				}
//			}
//		}
//		if (prayer != null) {
//			CombatUtils.activatePrayer(prayer)
//		} else {
//			val targetingMe = NpcUtils.getNearestNpc((npc: NPC) => (npc.getName != null && npc.getName == "Giant rat") || (npc.getName != null && npc.getName == "Scurrius" && npc.getPoseAnimation == 10687 && npc.getAnimation != 10705))
//			if (targetingMe != null) {
//				if (3.size == 0) {
//					CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE)
//					lastActivateTick = client.getTickCount
//				}
//				else {
//					if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE) && client.getTickCount - lastActivateTick < 3 || attacks.size > 0) return
//					CombatUtils.deactivatePrayers(true)
//				}
//			}
//		}
	}

//	private def dodgeFallingCeiling(): Unit = {
//		for (fallingCeiling <- fallingCeilingToTicks.toSet) {
//			val unsafeTile = fallingCeiling._1.getLocation
//			val playerTile = client.getLocalPlayer.getLocalLocation
//			if (unsafeTile.getX == playerTile.getX && unsafeTile.getY == playerTile.getY) {
//				val scurrius = NpcUtils.getNearestNpc("Scurrius")
//				if (scurrius != null) {
//					val unsafeTiles = fallingCeilingToTicks.keys.map(_.getLocation).toList
//					var safeTile = Option.empty[WorldPoint]
//					if (config.stayMelee) {
//						safeTile = SInteractionUtils.getClosestSafeLocationInNPCMeleeDistance(unsafeTiles, scurrius)
//					} else {
//						safeTile = SInteractionUtils.getClosestSafeLocationNotInNPCMeleeDistance(unsafeTiles, scurrius)
//					}
//					if (safeTile.isDefined) {
//						InteractionUtils.walk(safeTile.get)
//						justDodged = true
//						lastDodgeTick = client.getTickCount
//					}
//				}
//			}
//		}
//	}

	override def inArea(): Boolean = {
		getLocalPlayerWorldPoint.pipe(x => x.getRegionID == 9551)
	}

	inline def getLocalPlayerWorldPoint: WorldPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation)
}
