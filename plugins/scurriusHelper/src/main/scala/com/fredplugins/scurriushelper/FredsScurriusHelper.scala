package com.fredplugins.scurriushelper

import com.fredplugins.common.extensions.MenuExtensions
import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getTileObjectOpt, isNpcAction, isTileObjectAction}
import com.fredplugins.common.extensions.ObjectExtensions.{composition, impostorComposition, isImpostor, morphId, wrapped}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.scurriushelper.FredsScurriusHelper.{DURATION, FALLING_CEILING_GRAPHIC, SCURRIUS, SCURRIUS_PUBLIC}
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.{ChatMessageType, Client, GameState, GraphicsObject, InventoryID, Item, ItemContainer, NPC, Prayer, Projectile, TileObject}
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.events.{AnimationChanged, GameStateChanged, GameTick, GraphicsObjectCreated, ItemContainerChanged, MenuEntryAdded, MenuOptionClicked, NpcSpawned, ProjectileMoved, ScriptPostFired, VarbitChanged, WidgetClosed, WidgetLoaded}
import net.runelite.api.widgets.Widget
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
import scala.jdk.StreamConverters.StreamHasToScala
import java.util
import java.util.stream.Collectors
import java.util.{List, Optional}
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.IntAccumulator
import scala.jdk.OptionConverters.*
import scala.util.{Random, Try}
import scala.util.chaining.*

object FredsScurriusHelper {
	private val FALLING_CEILING_GRAPHIC: Int = 2644
	private val SCURRIUS: Int = 7222
	private val SCURRIUS_PUBLIC: Int = 7221
	private val DURATION: Int = 9
}

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Scurrius Helper V2</html>",
	description = "Dodges Scurrius' falling ceiling attack and re-attacks",
	tags =  Array("pvm", "scurrius", "prayer", "helper", "maps"),
	conflicts = Array("<html><font color=\"#32CD32\">Lucid </font>Scurrius Helper</html>")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsScurriusHelper() extends Plugin {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: FredsScurriusHelperConfig = null
	@Inject val notifier: Notifier = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	@Inject private val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val panel: FredsScurriusPanel = null
	@Inject private val overlay: FredsScurriusOverlay = null
//
//	object Cache {
//		var cachedFont: Font = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), config.getFontSize)
//		var countdownFont: Font = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), (config.getFontSize * 1.5).toInt)
//	}

	given Client = client

	@Provides
	def getConfig(configManager: ConfigManager): FredsScurriusHelperConfig = {
		configManager.getConfig[FredsScurriusHelperConfig](classOf[FredsScurriusHelperConfig])
	}
	private var bossNpc: NPC = null
	private var justDodged: Boolean = false
	private var lastDodgeTick: Int = 0
	private var lastRatTick: Int = 0
	private var lastActivateTick: Int = 0
	private val fallingCeilingToTicks = mutable.HashMap.empty[GraphicsObject, Int]//new HashMap<>();
	private val attacks: mutable.ListBuffer[Projectile] = mutable.ListBuffer.empty[Projectile]

	private def resetState(): Unit = {
		bossNpc = null
		justDodged = false
		lastDodgeTick = 0
		lastRatTick=0
		lastActivateTick=0
		attacks.clear()
		fallingCeilingToTicks.clear()
	}

	override protected def startUp(): Unit = {
		resetState()
		overlayManager.add(panel)
		overlayManager.add(overlay)
	}

	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
		overlayManager.remove(overlay)
		resetState()
	}

	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if(e.getGroup == FredsScurriusHelperConfig.GroupName) {
			e.getKey match {
				case "fontSize" | "fontBold" => {
					overlay.Cache.cachedFont = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), config.getFontSize)
					overlay.Cache.countdownFont = FontManager.getRunescapeFont.deriveFont((if (config.getFontBold) 1 else 0), (config.getFontSize * 1.5).toInt)
				}
				case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
			}
		}
	}

	def region: Int = Try(WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1)
	def inBossRoom: Boolean = region == 13210

	def getFallingCeilingToTicks: Map[GraphicsObject, Int] = fallingCeilingToTicks.toMap
	def getAttacks: Seq[Projectile] =  attacks.toList
	def getBoss: Option[NPC] = Option.apply(bossNpc)
	def getJustDodged: Boolean = justDodged
	def getLastDodgeTick: Int = lastDodgeTick
	def getLastRatTick: Int = lastRatTick
	def getLastActivateTick: Int = lastActivateTick

	@Subscribe
	private def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
		val graphicsObject = event.getGraphicsObject
		val id = graphicsObject.getId
		if (id == FALLING_CEILING_GRAPHIC) fallingCeilingToTicks.put(graphicsObject, DURATION)
	}

	@Subscribe private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (event.getNpc.getId == SCURRIUS || event.getNpc.getId == SCURRIUS_PUBLIC) if (config.attackOnSpawn) lastDodgeTick = client.getTickCount
	}

	@Subscribe private def onAnimationChanged(event: AnimationChanged): Unit = {
		if (!event.getActor.isInstanceOf[NPC]) return
		val npc = event.getActor.asInstanceOf[NPC]
		if (npc.getName != null && npc.getName == "Scurrius" && npc.getAnimation == 10705 && NpcUtils.getNearestNpc("Giant rat") == null) if (config.autoPray) CombatUtils.deactivatePrayers(false)
	}

	@Subscribe private def onProjectileMoved(event: ProjectileMoved): Unit = {
		val projectile = event.getProjectile
		if (projectile.getRemainingCycles != (projectile.getEndCycle - projectile.getStartCycle)) return
		if (projectile.getId != 2642 && projectile.getId != 2640) return
		val scurrius = NpcUtils.getNearestNpc("Scurrius")
		if (scurrius == null || (event.getProjectile.getInteracting ne client.getLocalPlayer)) return
		if (!attacks.contains(projectile)) {
			attacks.addOne(projectile)
			if (config.autoPray) CombatUtils.deactivatePrayer(Prayer.PROTECT_FROM_MELEE)
		}
	}

	@Subscribe private def onGameTick(event: GameTick): Unit = {
		val instancePoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation)
		if (instancePoint.getRegionID != 13210 || instancePoint.getRegionX < 23) return
		handlePrayers()
		attacks.filterInPlace((proj: Projectile) => proj.getRemainingCycles < 30)
		justDodged = false
		if (fallingCeilingToTicks.nonEmpty) {
			dodgeFallingCeiling()
			fallingCeilingToTicks.mapValuesInPlace((k: GraphicsObject, v: Int) => v - 1)
			fallingCeilingToTicks.filterInPlace((_: GraphicsObject, v: Int) => v <= 0)
		}
		val scurrius = NpcUtils.getNearestNpc("Scurrius")
		if (!justDodged) if (config.attackAfterDodge && (client.getLocalPlayer.getInteracting ne scurrius)) {
			val tSinceLastDodge = client.getTickCount - lastDodgeTick
			if (tSinceLastDodge < 3) if (scurrius != null) if (!config.prioritizeRats || getEligibleRat == null) NpcUtils.attackNpc(scurrius)
		}
		var attackRat = true
		if (scurrius != null) {
			val ratio = scurrius.getHealthRatio
			val scale = scurrius.getHealthScale
			val targetHpPercent = ratio.toDouble / scale.toDouble * 100
			if (targetHpPercent > 0) attackRat = false
		}
		if (justDodged) return
		if (config.attackRats && attackRat || config.prioritizeRats) {
			val giantRat = getEligibleRat
			if (giantRat != null && (giantRat ne client.getLocalPlayer.getInteracting)) {
				NpcUtils.attackNpc(giantRat)
				lastRatTick = client.getTickCount
			}
			else if (config.prioritizeRats && giantRat == null) {
				val tSinceLatRatHit = client.getTickCount - lastRatTick
				if (scurrius != null && tSinceLatRatHit < 8 && (client.getLocalPlayer.getInteracting ne scurrius)) NpcUtils.attackNpc(scurrius)
			}
		}
	}

	private def handlePrayers(): Unit = {
		if (!config.autoPray) return
		var prayer: Prayer = null
//		import scala.collection.JavaConversions._
		for (projectile <- attacks) {
			val cyclesToTicks = Math.floor(projectile.getRemainingCycles / 30.0F).toInt
			if (cyclesToTicks <= 1) if (projectile.getId == 2642) prayer = Prayer.PROTECT_FROM_MISSILES
			else prayer = Prayer.PROTECT_FROM_MAGIC
		}
		if (prayer != null) CombatUtils.activatePrayer(prayer)
		else {
			val targetingMe = NpcUtils.getNearestNpc((npc: NPC) => (npc.getName != null && npc.getName == "Giant rat") || (npc.getName != null && npc.getName == "Scurrius" && npc.getPoseAnimation == 10687 && npc.getAnimation != 10705))
			if (targetingMe != null) if (attacks.size == 0) {
				CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE)
				lastActivateTick = client.getTickCount
			}
			else {
				if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE) && client.getTickCount - lastActivateTick < 3 || attacks.size > 0) return
				CombatUtils.deactivatePrayers(true)
			}
		}
	}

	private def dodgeFallingCeiling(): Unit = {
		for (fallingCeiling <- fallingCeilingToTicks.toSet) {
			val unsafeTile = fallingCeiling._1.getLocation
			val playerTile = client.getLocalPlayer.getLocalLocation
			if (unsafeTile.getX == playerTile.getX && unsafeTile.getY == playerTile.getY) {
				val scurrius = NpcUtils.getNearestNpc("Scurrius")
				if (scurrius != null) {
					val unsafeTiles = fallingCeilingToTicks.keys.map(_.getLocation).toList
					var safeTile = Option.empty[WorldPoint]
					if (config.stayMelee) safeTile = SInteractionUtils.getClosestSafeLocationInNPCMeleeDistance(unsafeTiles.asJava, scurrius).toScala
					else safeTile = Option(InteractionUtils.getClosestSafeLocationNotInNPCMeleeDistance(unsafeTiles.asJava, scurrius))
					if (safeTile.isDefined) {
						InteractionUtils.walk(safeTile.get)
						justDodged = true
						lastDodgeTick = client.getTickCount
					}
				}
			}
		}
	}

	private def getEligibleRat: NPC = NpcUtils.getNearestNpc((npc: NPC) => {
			if (npc == null) false else {
				val ratio = npc.getHealthRatio
				val scale = npc.getHealthScale
				val targetHpPercent = ratio.toDouble / scale.toDouble * 100
				npc.getName != null && npc.getName == "Giant rat" && targetHpPercent > 0
			}
	})
}