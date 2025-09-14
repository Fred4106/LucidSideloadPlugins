package com.fredplugins.pvmDebugger.hueycoatl

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.hueycoatl.HueycoatlData.*
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.NPCs
import ethanApiPlugin.collections.query.NPCQuery
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.Actor
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.NPC
import net.runelite.api.Perspective
import net.runelite.api.Player
import net.runelite.api.Point
import net.runelite.api.Prayer
import net.runelite.api.Projectile
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.AnimationChanged
import net.runelite.api.events.GameTick
import net.runelite.api.events.GraphicsObjectCreated
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.PostHealthBarConfig
import net.runelite.api.events.ProjectileMoved
import net.runelite.api.events.VarbitChanged
import net.runelite.api.gameval.NpcID
import net.runelite.api.gameval.ObjectID1
import net.runelite.api.gameval.SpotanimID
import net.runelite.api.gameval.VarbitID
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import packets.MovementPackets

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Rectangle
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import scala.annotation.unused
import scala.collection.mutable
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.jdk.OptionConverters.RichOptional
import scala.util.chaining.scalaUtilChainingOps

object HueycoatlData {
	val HueyRegion                          = 5939
	val HueyProjectileIds: Map[Int, Prayer] = List(
		SpotanimID.VFX_HUEY_ATTACK_RANGED_PROJANIM_01 -> Prayer.PROTECT_FROM_MISSILES,
		SpotanimID.VFX_HUEY_ATTACK_MAGIC_PROJANIM_01 -> Prayer.PROTECT_FROM_MAGIC,
		SpotanimID.VFX_HUEY_ATTACK_MELEE_PROJANIM_01 -> Prayer.PROTECT_FROM_MELEE
	).toMap
	val HueyObjectIds    : Seq[Int]         = List(
		ObjectID1.HUEY_P2_BARRIER_RESPAWN,
		ObjectID1.HUEY_P2_BARRIER,
		ObjectID1.HUEY_P2_BARRIER_RETRACTED,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_BRAZIER_OFF,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_BRAZIER_OFF,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_BRAZIER_OFF,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_BRAZIER_ON,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_BRAZIER_ON,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_BRAZIER_ON
	)
	val HueyDecorativeObjectIds: Seq[Int] = List(
			ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_EMPTY,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S1,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S2,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S3,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S4,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S5,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_EMPTY,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S1,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S2,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S3,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S4,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S5,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_EMPTY,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S1,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S2,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S3,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S4,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S5
	)


	val HueyNpcIds: Seq[Int] = List(
		NpcID.HUEY_BODY_PART, NpcID.HUEY_BODY_PART_BROKEN,
		NpcID.HUEY_HEAD, NpcID.HUEY_HEAD_INVULNERABLE, NpcID.HUEY_HEAD_DEFEATED, NpcID.HUEY_HEAD_RESPAWN_PLACEHOLDER,
		NpcID.HUEY_TAIL_BROKEN, NpcID.HUEY_TAIL,
	)

	val HueyHeadIds: Seq[Int] = List(
		NpcID.HUEY_HEAD
		, NpcID.HUEY_HEAD_INVULNERABLE
		, NpcID.HUEY_HEAD_DEFEATED
		, NpcID.HUEY_HEAD_RESPAWN_PLACEHOLDER
	)

	val varbitIdToName: Map[Int, String] = classOf[net.runelite.api.gameval.VarbitID].getDeclaredFields.toList
		.filter(_.getType == Integer.TYPE)
		.filter(_.getModifiers == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL))
//		.filterNot(_.getName.startsWith("ENT"))
//		.filterNot(_.getName.startsWith("LEAGUE"))
		.map(f => {
			f.getInt(null) -> f.getName
		}).toMap

	val npcIdToName: Map[Int, String] = classOf[net.runelite.api.gameval.NpcID].getDeclaredFields.toList
		.filter(_.getType == Integer.TYPE)
		.filter(_.getModifiers == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL))
		.map(f => {
			f.getInt(null) -> f.getName
		}).toMap

	val animationIdToName: Map[Int, String] = classOf[net.runelite.api.gameval.AnimationID].getDeclaredFields.toList
		.filter(_.getType == Integer.TYPE)
		.filter(_.getModifiers == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL))
		.map(f => {
			f.getInt(null) -> f.getName
		})
		.appended(-1 -> "IDLE").toMap

	val spotAnimationIdToName: Map[Int, String] = classOf[net.runelite.api.gameval.SpotanimID].getDeclaredFields.toList
		.filter(_.getType == Integer.TYPE)
		.filter(_.getModifiers == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL))
		.map(f => {
			f.getInt(null) -> f.getName
		}).toMap

	val objectIdToName: Map[Int, String] = {
		List.newBuilder[Field]
			.addAll(classOf[net.runelite.api.gameval.ObjectID].getDeclaredFields.toList)
			.addAll(classOf[net.runelite.api.gameval.ObjectID1].getDeclaredFields.toList)
			.result()
			.filter(_.getType == Integer.TYPE)
			.filter(_.getModifiers == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL))
			.map(f => {
				f.getInt(null) -> f.getName
			}).toMap
	}

	val HueyShockwaveIds = List(
	SpotanimID.VFX_HUEY_TAIL_SLAM_SHOCKWAVE_SOUTH,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_WEST,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_NORTH,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_EAST,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_SOUTHEAST,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_SOUTHWEST,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_NORTHWEST,
			SpotanimID.VFX_HUEY_TAIL_SLAM_SHOCKWAVE_NORTHEAST
	)

	sealed trait PillarTrait(val wp: WorldPoint, val scale: Seq[Int], val prayer: Prayer) extends enumeratum.EnumEntry {
		def getLevel(using client: Client): Int = {
			wp.getTile.map(_.getDecorativeObject.getId).map(scale.indexOf(_)).getOrElse(-1)
		}

		def getColor: Color = {
			Option(prayer).collect {
				case Prayer.PROTECT_FROM_MAGIC => Color.BLUE
				case Prayer.PROTECT_FROM_MISSILES => Color.GREEN
				case Prayer.PROTECT_FROM_MELEE => Color.RED
			}.getOrElse(Color.WHITE)
		}
	}
	object Pillars extends enumeratum.Enum[PillarTrait] {
		case object Mage extends PillarTrait(
			new WorldPoint(1504, 3287, 0),
			Seq(
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_EMPTY,
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S1,
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S2,
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S3,
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S4,
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S5
			),
			Prayer.PROTECT_FROM_MAGIC
		) {}

		case object Range extends PillarTrait(
			new WorldPoint(1505, 3289, 0),
			Seq(
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_EMPTY,
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S1,
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S2,
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S3,
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S4,
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S5
			),
			Prayer.PROTECT_FROM_MISSILES
		) {}

		case object Melee extends PillarTrait(
			new WorldPoint(1507, 3290, 0),
			Seq(
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_EMPTY,
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S1,
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S2,
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S3,
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S4,
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S5
			),
			Prayer.PROTECT_FROM_MELEE
		) {}

		override def values: IndexedSeq[PillarTrait] = findValues
	}

//	sealed trait HueyBody(val wp: WorldPoint) extends enumeratum.EnumEntry {
//		def getNpc(using client: Client): Option[NPC] = {
//			NPCs.search().withId(NpcID.HUEY_BODY_PART, NpcID.HUEY_BODY_PART_BROKEN).atLocation(wp).first().toScala
//		}
//
//		def isDead(using client: Client): Boolean = {
//			getNpc.exists(_.getId == NpcID.HUEY_BODY_PART_BROKEN)
//		}
//	}

	val HueyBodyLocations = Seq(
				WorldPoint(1520,3273, 0),
				WorldPoint(1524,3277, 0),
				WorldPoint(1527,3273, 0),
				WorldPoint(1530,3276, 0),
				WorldPoint(1524,3270, 0)
		)
}
trait DangerousTile{
	def location: WorldPoint
//	def dangerousOnTick: Int
	def spawnCycle: Int
	def finishedCycle: Int
}
case class WaveTile(location: WorldPoint, spawnCycle: Int, finishedCycle: Int) extends DangerousTile
case class LightingTile(location: WorldPoint, spawnCycle: Int, finishedCycle: Int) extends DangerousTile

case class HueycoatlState(projectile: Option[Projectile], pillars: Map[PillarTrait, Int], dangerousTiles: Seq[DangerousTile], npcs: Seq[NPC]) {
		lazy val hueyHead  : Option[NPC] = npcs.find(_.getId.pipe(HueyHeadIds.contains(_)))
		lazy val hueyBodies: Seq[NPC]    = npcs.filter(_.getId == NpcID.HUEY_BODY_PART)
		lazy val hueyTail  : Option[NPC] = npcs.find(_.getId.pipe(Seq(NpcID.HUEY_TAIL, NpcID.HUEY_TAIL_BROKEN).contains(_)))

		lazy val stage: Int = hueyHead.map(_.getId).collect {
			case NpcID.HUEY_HEAD_RESPAWN_PLACEHOLDER | NpcID.HUEY_HEAD_DEFEATED => 0
			case NpcID.HUEY_HEAD_INVULNERABLE => 3
			case NpcID.HUEY_HEAD if hueyBodies.nonEmpty => 1
			case NpcID.HUEY_HEAD if hueyTail.isEmpty => 2
			case NpcID.HUEY_HEAD if hueyTail.isDefined => 4
		}.getOrElse(0)
}

object HueycoatlStateFactory {
	def create(using client: Client): Option[HueycoatlState] = {
		Option.when(client.isClientThread && client.getGameState == GameState.LOGGED_IN) {
			HueycoatlState(
				client.getProjectiles.asScala.toList.filter(p => HueyProjectileIds.contains(p.getId) && Option(p.getTargetActor).contains(client.getLocalPlayer)).sortBy(_.getEndCycle).headOption,
				Pillars.values.map { p =>
					p -> p.getLevel
				}.toMap,
				client.getGraphicsObjects.asScala.toList.flatMap(go => {
					val swo = Option.when(HueyShockwaveIds.contains(go.getId)) {
						WaveTile(go.templateLocation, client.getGameCycle, go.getStartCycle)
					}
					val lso = Option.when(go.getId == SpotanimID.VFX_HUEYCOATL_PRAYER_02){
						LightingTile(go.templateLocation, client.getGameCycle, go.getStartCycle)
					}
					swo.orElse(lso)
				}),
				NPCs.search().withId(HueyNpcIds *).result().asScala.toList
			)
		}
	}
}

@Singleton
class FredsHueycoatlHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsHueycoatlConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsHueycoatlConfig.GROUP
	private def clientThread  = parent.getClientThread
	given Client = client

	private var state: Option[HueycoatlState] = Option.empty

	private var incomingProjectile: Projectile = null
	private var pillars: Map[PillarTrait, Int] = Map.empty
	private var stage: Int = 0
	private var dangerousTiles: Seq[WorldPoint] = Seq.empty
	private var dangerousSpawnCycle: Int  = 0
	private var dangerousSpawnTick: Int  = 0
	private var priorityTiles: Seq[WorldPoint] = Seq.empty
	private var trackedNpcs = Seq.empty[NPC]

	private def curRegion: Int = Option(client.getLocalPlayer).map(_.templateLocation).map(_.getRegionID).getOrElse(-1)
	private def inRegion: Boolean = HueyRegion == curRegion
	private def inFight: Boolean =
		if (inRegion) clientThread.runOnClientThread(() => client.getVarbitValue(VarbitID.HUEY_IN_AREA)) == 1
		else false

	def reset(): Unit= {
		pillars = Map.empty
		stage = 0
		incomingProjectile = null
		dangerousTiles = Seq.empty
		dangerousSpawnCycle = 0
		dangerousSpawnTick = 0
		priorityTiles = Seq.empty
		trackedNpcs = NPCs.search().withId(HueyNpcIds *).result().asScala.toList.filter(_.templateLocation.getRegionID == HueyRegion)
	}

	override def init(): Unit = {
//		curRegion = Option(client.getLocalPlayer).map(_.templateLocation).map(_.getRegionID).getOrElse(-1)
//		inFight = if (inRegion) (
//			clientThread.runOnClientThread(() => client.getVarbitValue(VarbitID.HUEY_IN_AREA)) == 1
//		) else false
		reset()
	}

	override def cleanup(): Unit = {
//		curRegion = -1
//		inFight = false
		reset()
	}

	@Subscribe
	def onGraphicsObjectCreated(e: GraphicsObjectCreated): Unit = {
		val graphicsObject = e.getGraphicsObject
		val name = spotAnimationIdToName.getOrElse(graphicsObject.getId, s"Unknown(${graphicsObject.getId})")
		if((name.startsWith("VFX_HUEY_TAIL_SLAM_SHOCKWAVE") && !name.contains("IMPACT")) || !name.startsWith("VFX_HUEY")) return
		if(graphicsObject.templateLocation.getRegionID == HueyRegion && graphicsObject.getId == SpotanimID.VFX_HUEYCOATL_PRAYER_02) {
			//ticksSinceDangerousTiles = 5
			dangerousSpawnTick = client.getTickCount
			dangerousSpawnCycle = client.getGameCycle
			dangerousTiles = dangerousTiles.appended(graphicsObject.templateLocation)
		}
		log.info(s"GraphicsObject \"${name}\" created on tick ${client.getTickCount} at ${graphicsObject.templateLocation} with start cycle ${graphicsObject.getStartCycle} on cycle ${client.getGameCycle} animation ${Option(graphicsObject.getAnimation).map(a => a.getId -> a.getDuration).getOrElse(-1 -> 0)}")
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		if(!inRegion) return
		pillars = Pillars.values.map { p =>
			p -> p.getLevel
		}.toMap

		val hueyHead                                         = NPCQuery(trackedNpcs *).withId(HueyHeadIds *).first().toScala
//		hueyHead.foreach(ReflectionUtils.getHealthbars(_))
		val (hueyBodies: Seq[NPC], deadHueyBodies: Seq[NPC]) = HueyBodyLocations.flatMap(bl => NPCQuery(trackedNpcs *).atLocation(bl).withId(NpcID.HUEY_BODY_PART_BROKEN, NpcID.HUEY_BODY_PART).first().toScala).partition(_.getId == NpcID.HUEY_BODY_PART)
		val hueyTails: Option[NPC]                           = NPCQuery(trackedNpcs *).withId(NpcID.HUEY_TAIL, NpcID.HUEY_TAIL_BROKEN).first().toScala
		stage = hueyHead.map(_.getId).collect{
			case NpcID.HUEY_HEAD_RESPAWN_PLACEHOLDER | NpcID.HUEY_HEAD_DEFEATED => 0
			case NpcID.HUEY_HEAD if hueyBodies.nonEmpty => 1
			case NpcID.HUEY_HEAD if hueyTails.isEmpty => 2
			case NpcID.HUEY_HEAD_INVULNERABLE => 3
			case NpcID.HUEY_HEAD if hueyTails.nonEmpty => 4
		}.getOrElse(0)

		if(dangerousTiles.nonEmpty) {
			val age = client.getTickCount - dangerousSpawnTick
			if(age == 4) {
				dangerousTiles = Seq.empty
			}
		}

		if(inFight) {
			val projectilePrayOpt = Option(incomingProjectile).filter(_.ticksRemaining <= 3)
				.flatMap(p => {
					if(p.hasHit && p.ticksRemaining < 1) {
						incomingProjectile = null
					}
					p.getId.pipe(HueyProjectileIds.get)
				})
			val campPrayerOpt = Option(config.campPrayer().getPrayer)

			val protectPrayer: Option[Prayer] = stage match {
				case 0 => Option.empty[Prayer]
				case 1 => projectilePrayOpt
				case 2 | 3 | 4 => projectilePrayOpt.orElse(campPrayerOpt)
			}

			if(stage == 0 && client.isPrayerActive(Prayer.PIETY)) {
				CombatUtils.deactivatePrayers(Prayer.PIETY)
			} else {
				Option.when(config.autoPrayPiety() && stage > 0)(Prayer.PIETY).filterNot(client.isPrayerActive).foreach(offensivePrayer => CombatUtils.activatePrayer(offensivePrayer))
			}

			priorityTiles = (stage match {
				case 0 => Seq.empty[WorldPoint]
				case 1 => {
					hueyBodies.flatMap(b => {
						val bArea         = b.templateLocation.toWorldArea//SInteractionUtils.offset(, 1)
						val bMeeleArea    = SInteractionUtils.offset(bArea, 1)
						val excludePoints = SInteractionUtils.worldAreaTiles(bArea).appendedAll(SInteractionUtils.worldAreaCorners(bMeeleArea))
						SInteractionUtils.worldAreaTiles(bMeeleArea).filterNot(excludePoints.contains(_))
					})
				}
				case 2 | 4 => {
					hueyHead.map(_.getWorldArea).fold(List.empty[WorldPoint])(wa => {
						val bMeeleArea    = SInteractionUtils.offset(wa, 1)
						val excludePoints = SInteractionUtils.worldAreaTiles(wa).appendedAll(SInteractionUtils.worldAreaCorners(bMeeleArea))
						SInteractionUtils.worldAreaTiles(bMeeleArea).filterNot(excludePoints.contains(_))
					})
				}
				case 3 => {
					hueyTails.map(_.getWorldArea).fold(List.empty[WorldPoint])(wa => {
						val bMeeleArea    = SInteractionUtils.offset(wa, 1)
						val excludePoints = SInteractionUtils.worldAreaTiles(wa).appendedAll(SInteractionUtils.worldAreaCorners(bMeeleArea))
						SInteractionUtils.worldAreaTiles(bMeeleArea).filterNot(excludePoints.contains(_))
					})
				}
			}).map(_.getTemplate)
				.filter(SInteractionUtils.isWalkable(_))

			protectPrayer match {
				case Some(pp) => CombatUtils.activatePrayer(pp)
				case None => CombatUtils.deactivatePrayers(true)
			}
			val destinationTile = Option(client.getLocalDestinationLocation).map(_.getTemplate).getOrElse(client.getLocalPlayer.templateLocation)
			if(dangerousTiles.contains(destinationTile)) {
				val playerPos = client.getLocalPlayer.templateLocation
				priorityTiles.filterNot(dangerousTiles.contains(_)).pipe(tl => if(tl.isEmpty) SInteractionUtils.reachableTiles.map(_.getTemplate).filterNot(dangerousTiles.contains(_)) else tl).flatMap(pt => {
						val pathResult: EthanApiPlugin.PathResult = EthanApiPlugin.canPathToTile(playerPos, pt)
						Option.when(pathResult.isReachable)(pt -> pathResult.getDistance)
					}).sortBy(_._2).map(_._1)
					.headOption.foreach(safeTile => {
						MovementPackets.queueMovement(safeTile)
						log.debug(s"moving to safe tile ${safeTile} from ${playerPos}")
					})
			}
		} else {
			CombatUtils.deactivatePrayers(false)
		}
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
		if(e.getOldRegion == HueyRegion) {
			reset()
		}
	}

	@Subscribe
	def onVarbitChanged(e: VarbitChanged): Unit = {
		if(e.getVarbitId == -1) return
		val name = varbitIdToName.getOrElse(e.getVarbitId, s"Unknown(${e.getVarbitId})")
		if(!name.contains("HUEY") && !name.startsWith("Unknown")) return
		log.info(s"Varbit \"${name}\" changed to ${e.getValue}")
		if(e.getVarbitId == VarbitID.HUEY_IN_AREA) {
			if(e.getValue == 0) {
				reset()
			}
		}
	}
	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		if (e.getNpc.templateLocation.getRegionID != HueyRegion) return
		if (!HueyNpcIds.contains(e.getNpc.getId)) return
		val name = e.getNpc.getId.pipe(n => npcIdToName.getOrElse(n, s"Unknown(${n})"))
		log.debug(s"Npc[${e.getNpc.getIndex}] \"${name}\" spawned at ${e.getNpc.templateLocation}")
		trackedNpcs = trackedNpcs.filterNot(_ == e.getNpc).appended(e.getNpc)
	}
	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		if (e.getNpc.templateLocation.getRegionID != HueyRegion) return
		if (!HueyNpcIds.contains(e.getNpc.getId)) return
		val name = e.getNpc.getId.pipe(n => npcIdToName.getOrElse(n, s"Unknown(${n})"))
		log.debug(s"Npc[${e.getNpc.getIndex}] \"${name}\" despawned at ${e.getNpc.templateLocation}")
		trackedNpcs = trackedNpcs.filterNot(_ == e.getNpc)
	}

	@Subscribe
	def onProjectileMoved(e: ProjectileMoved): Unit = {
		val projectile: Projectile = e.getProjectile
		if (projectile.getTargetActor != null && projectile.getTargetActor != client.getLocalPlayer) return
		if (!HueyProjectileIds.keySet.contains(projectile.getId)) return
		val name = projectile.getId.pipe(p => spotAnimationIdToName.getOrElse(p, s"Unknown($p)"))
		if (projectile.templateSourceLocation.getRegionID == HueyRegion || projectile.templateTargetLocation.getRegionID == HueyRegion) {
			if(projectile.justSpawned) {
				if(incomingProjectile == null) {
					log.debug(_: String)
				} else {
					log.warn(_: String)
				}.tap(x => {
					x.apply(s"Projectile[${projectile}] \"${name}\" from ${projectile.getTargetActor} spawned at ${projectile.templateSourceLocation} with ${projectile.ticksRemaining} ticks remaining")
					if(incomingProjectile!=null) {
						x.apply(s"Projectile[${incomingProjectile}] from ${incomingProjectile.getTargetActor} with ${incomingProjectile.ticksRemaining} ticks remaining and ${incomingProjectile.getRemainingCycles} cycles remaining")
					}
				})
				incomingProjectile = projectile
			}
		}
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
//		if (inRegion && inFight) {
		if(e.getActor.templateLocation.getRegionID != HueyRegion) return
		Option(e.getActor).collect {
			case npc: NPC if HueyNpcIds.contains(npc.getId) => npc.getId.pipe(n => npcIdToName.getOrElse(n, s"$n").pipe(s => s"Npc(${s})")) -> npc.getAnimation
			case player: Player if player == client.getLocalPlayer => "LocalPlayer" -> player.getAnimation
//			case player: Player => s"Player(${player.getName})" -> player.getAnimation
		}.map{
			case (actorName, actorAnimationId) => actorName -> actorAnimationId.pipe(a => animationIdToName.getOrElse(a, s"Unknown(${a})"))
		}.foreach{
			case (actorName, animationName) => log.debug(s"AnimationChanged[${client.getTickCount}]: \"$actorName\" animation changed to \"$animationName\"")
		}
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
			val regionLine = LineComponent.builder().left("Region").right(s"$curRegion").rightColor(if(inRegion) Color.GREEN else Color.RED).build
			val projectileLine = LineComponent.builder().left("Projectile").right(s"${Option(incomingProjectile).fold("Null")(_.getId.pipe(p => spotAnimationIdToName.getOrElse(p, s"Unknown(${p})")))}").rightColor(if(incomingProjectile != null) Color.GREEN else Color.RED).build
			val stageLine = LineComponent.builder().left("Stage").right(s"${stage}").build
			val pillarsLines = pillars.map {
				case (p, l) => LineComponent.builder().left(p.entryName).leftColor(p.getColor).right(s"$l").rightColor(
					Color.RED.interpolate(Color.GREEN, l/5.0)
				).build
			}.toList.prepended(TitleComponent.builder().text("Pillars").build())
			Seq(regionLine, projectileLine, stageLine, pillarsLines, Seq.empty[LayoutableRenderableEntity]).flatMap{
				case e: LayoutableRenderableEntity => Seq(e)
				case le: Seq[_] => le.collect{
					case e: LayoutableRenderableEntity => e
				}
			}
//		} else {
//			Seq.empty[LayoutableRenderableEntity]
//		}
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		def renderNpcOverlay(npc: NPC, text: String, zoffset: Int, color: Color, fillAlpha: Int, outlineWidth: Int = 4, textColor: Color = Color.WHITE ): Unit = {
			var poly = npc.getConvexHull
			if(poly!= null) OverlayUtil.renderPolygon(g, poly, Color(0, 0, 0, 0), color.withAlpha(fillAlpha))
			if(outlineWidth > 0) parent.getModelOutlineRenderer.drawOutline(npc, outlineWidth, color, 2)
//			val poly = npc.getCanvasTilePoly
//			if (poly != null) OverlayUtil.renderPolygon(g, poly, fillColor)
			if(text != null && text.nonEmpty) {
				val textLocation = npc.getCanvasTextLocation(g, text, npc.getLogicalHeight + zoffset)
				if (textLocation != null) {
					val textBounds = g.getFontMetrics.getStringBounds(text, g)
					val offset = 4
					val textArea = Rectangle(textBounds.getX.toInt - offset, textBounds.getY.toInt - offset, textBounds.getWidth.toInt + offset + offset, textBounds.getHeight.toInt + offset + offset)
	//				val textArea = new Rectangle(textLocation.getX + (textBounds.getWidth / 2.0).toInt - , textLocation.getY, textBounds.getWidth.toInt, textBounds.getHeight.toInt)
					OverlayUtil.renderPolygon(g, textArea, new Color(255 - textColor.getRed, 255 - textColor.getGreen, 255 - textColor.getBlue, fillAlpha))
					OverlayUtil.renderTextLocation(g, textLocation, text, textColor)
				}
			}
		}

		case class ProgressData(spawnTick: Int, spawnCycle: Int, maxAge: Int, pieChartColor: Color, textColor: Color){}
		def renderTile(tile: WorldPoint, color: Color, progress: ProgressData | Null = null): Unit = {
			val poly = Perspective.getCanvasTilePoly(client, tile.getLocalPoint)
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)

			Option(progress).foreach{
				case ProgressData(spawnTick, spawnCycle, maxAge, ppcColor, textColor) => {
					val ppc = new ProgressPieComponent()
					ppc.setBorderColor(Color.BLACK)
					ppc.setFill(ppcColor)
					ppc.setProgress((client.getGameCycle - spawnCycle).doubleValue / (30.0d * maxAge))
					ppc.setDiameter(28)
					val point = Perspective.localToCanvas(client, tile.getLocalPoint, client.getTopLevelWorldView.getPlane, 20)
					ppc.setPosition(point)
					ppc.render(g)

					val text = s"${client.getTickCount - spawnTick}"

					val fm      = g.getFontMetrics()
					val bounds  = fm.getStringBounds(text, g)
					val xOffset = point.getX() - (bounds.getWidth() / 2).toInt
					val yOffset = point.getY() + (bounds.getHeight() / 2).toInt

					val textPoint = new Point(xOffset, yOffset)
					OverlayUtil.renderTextLocation(g, textPoint, text, textColor)
				}
			}
		}

		if(inRegion){
			trackedNpcs.foreach { n =>
				//				val id = n.getId
				//				val name = npcIdToName(id)
				val color = n.getId match {
					case NpcID.HUEY_HEAD_RESPAWN_PLACEHOLDER | NpcID.HUEY_HEAD_DEFEATED => Color.GRAY
					case NpcID.HUEY_HEAD => Color.GREEN
					case NpcID.HUEY_HEAD_INVULNERABLE => Color.BLUE

					case NpcID.HUEY_TAIL_BROKEN => Color.BLUE
					case NpcID.HUEY_TAIL => Color.GREEN

					case NpcID.HUEY_BODY_PART => Color.GREEN
					case NpcID.HUEY_BODY_PART_BROKEN => Color.GRAY
					case _ => Color.PINK
				}

				renderNpcOverlay(n,
					npcIdToName(n.getId),
					0,
					color.withAlpha(150),
					100,
					4
				)
			}
			priorityTiles.foreach(
				renderTile(_, Color.CYAN.withAlpha( 150))
			)
			dangerousTiles.foreach(
				renderTile(_, Color.ORANGE.withAlpha(150), ProgressData(dangerousSpawnTick, dangerousSpawnCycle, 4, Color.RED.withAlpha(150), Color.WHITE))
			)
//			iceTiles.toList.foreach(iceTile => {
//				renderIceTile(iceTile, Color.RED)
//			})
//
//			bossData.foreach((wrapped, data) => {
//				val txt  = s"age: ${client.getTickCount - data.spawnedTick}, ticksSinceAttack: ${client.getTickCount - data.lastAttackTick}"
//				renderNpcOverlay(wrapped.wrapped, txt, Color.CYAN, 60)
//			})
//			iceBlocks.foreach((wrapped, data) => {
//				val txt = s"age: ${client.getTickCount - data.spawnedTick}"
//				renderNpcOverlay(wrapped.wrapped, txt, Color.BLUE, 60)
//			})
		}
		null.asInstanceOf[Dimension]
	}
}
