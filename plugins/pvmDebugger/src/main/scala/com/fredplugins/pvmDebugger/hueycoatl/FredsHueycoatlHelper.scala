package com.fredplugins.pvmDebugger.hueycoatl

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.amoxliatl.FredsAmoxliatlHelper.Amoxliatl
import com.fredplugins.pvmDebugger.amoxliatl.FredsAmoxliatlHelper.UnstableIce
import com.fredplugins.pvmDebugger.hueycoatl.HueycoatlData.*
import com.google.common.reflect.Reflection
import com.google.inject.Inject
import com.google.inject.Singleton
import com.sun.jna.internal.ReflectionUtils
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.services.localPlayer.events.LocalDestinationChanged
import ethanApiPlugin.services.localPlayer.events.LocalPositionChanged
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.Actor
import net.runelite.api.Client
import net.runelite.api.GameObject
import net.runelite.api.GraphicsObject
import net.runelite.api.NPC
import net.runelite.api.NPCComposition
import net.runelite.api.Perspective
import net.runelite.api.Player
import net.runelite.api.Point
import net.runelite.api.Prayer
import net.runelite.api.Projectile
import net.runelite.api.WorldView
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.AnimationChanged
import net.runelite.api.events.DecorativeObjectDespawned
import net.runelite.api.events.DecorativeObjectSpawned
import net.runelite.api.events.GameObjectDespawned
import net.runelite.api.events.GameObjectSpawned
import net.runelite.api.events.GameTick
import net.runelite.api.events.GraphicsObjectCreated
import net.runelite.api.events.NpcChanged
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.ProjectileMoved
import net.runelite.api.events.VarbitChanged
import net.runelite.api.gameval.AnimationID
import net.runelite.api.gameval.NpcID
import net.runelite.api.gameval.ObjectID1
import net.runelite.api.gameval.SpotanimID
import net.runelite.api.gameval.VarbitID
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.util.ColorUtil

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.Shape
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import scala.annotation.unused
import scala.collection.mutable
import scala.jdk.CollectionConverters.IterableHasAsScala
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
}

@Singleton
class FredsHueycoatlHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsHueycoatlConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsHueycoatlConfig.GROUP
	private def clientThread  = parent.getClientThread
	given Client = client

	private var curRegion: Int     = -1
	private var inFight  : Boolean = false

	private var incomingProjectile: Projectile = null
	private var pillars: Map[PillarTrait, Int] = Map.empty
//	private var body

	private def inRegion: Boolean = HueyRegion == curRegion

//	var currentRoom: Option[MoonRoomEnum] = None
//	var currentRoomChangedTick: Int = -1
	override def init(): Unit = {
		curRegion = Option(client.getLocalPlayer).map(_.templateLocation).map(_.getRegionID).getOrElse(-1)
		inFight = if (inRegion) (
			clientThread.runOnClientThread(() => client.getVarbitValue(VarbitID.HUEY_IN_AREA)) == 1
		) else false
		pillars = Map.empty

		incomingProjectile = null
	}

	override def cleanup(): Unit = {
		curRegion = -1
		inFight = false
		pillars = Map.empty
		incomingProjectile = null
	}

	@Subscribe
	def onGraphicsObjectCreated(e: GraphicsObjectCreated): Unit = {
		val graphicsObject = e.getGraphicsObject
		val name = spotAnimationIdToName.getOrElse(graphicsObject.getId, s"Unknown(${graphicsObject.getId})")
		if(name.startsWith("VFX_HUEY_TAIL_SLAM_SHOCKWAVE") || !name.startsWith("VFX_HUEY")) return
		if(graphicsObject.templateLocation.getRegionID == HueyRegion) {
			log.info(s"GraphicsObject \"${name}\" created on tick ${client.getTickCount} at ${graphicsObject.templateLocation} with animation ${Option(graphicsObject.getAnimation).map(a => a.getId -> a.getDuration).getOrElse(-1 -> 0)}")
		}
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		if(!inRegion) return

		if(incomingProjectile != null && incomingProjectile.hasHit) {
			incomingProjectile = null
		}

		pillars = Pillars.values.map { p =>
			p -> p.getLevel
		}.toMap

		if(inFight) {
			val npcs = client.getTopLevelWorldView.npcs().asScala.toList

			if(incomingProjectile != null && incomingProjectile.ticksRemaining <= 2) {
				HueyProjectileIds.get(incomingProjectile.getId).filterNot(client.isPrayerActive)
					.foreach(p => {CombatUtils.activatePrayer(p)})
				log.debug(s"pray ${HueyProjectileIds.get(incomingProjectile.getId)} against ${incomingProjectile.getId.pipe(p => spotAnimationIdToName.getOrElse(p, s"Unknown(${p})"))}")
			} else {
				Option(config.campPrayer.getPrayer).filterNot(client.isPrayerActive(_))
					.foreach(p => CombatUtils.activatePrayer(p))
			}
			//			ethanApiPlugin.collections.query.TileObjectQuery()
//			val lp = LocalPoint.fromWorld(client.getTopLevelWorldView, new WorldPoint(1505, 3289, 0))
//			log.debug(s"lp ${lp} is${if (lp.isInScene) " " else " not "}in scene")
//			Option.when(lp.isInScene)(client.getTopLevelWorldView.getScene.getTiles.apply(client.getTopLevelWorldView.getPlane).apply(lp.getSceneX).apply(lp.getSceneY))
//				.foreach(t => {
//					log.debug(s"found ${objectIdToName.getOrElse(t.getDecorativeObject.getId, s"Unknown(${t.getDecorativeObject.getId})")} at ${t.getWorldLocation.getTemplate}")
//				})
		}
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
		if(e.getOldRegion == HueyRegion) {
			pillars = Map.empty
			incomingProjectile = null
			inFight = false
		}
		curRegion = e.getCurRegion
	}

	@Subscribe
	def onVarbitChanged(e: VarbitChanged): Unit = {
		if(e.getVarbitId == -1) return
		val name = varbitIdToName.getOrElse(e.getVarbitId, s"Unknown(${e.getVarbitId})")
		if(name.startsWith("ENT") || name.startsWith("LEAGUE")) return;
		log.info(s"Varbit \"${name}\" changed to ${e.getValue}")
		if(e.getVarbitId == VarbitID.HUEY_IN_AREA) {
			inFight = e.getValue == 1
		}
	}

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		if (e.getNpc.templateLocation.getRegionID != HueyRegion) return
		if (!HueyNpcIds.contains(e.getNpc.getId)) return
		val name = e.getNpc.getId.pipe(n => npcIdToName.getOrElse(n, s"Unknown(${n})"))
		log.debug(s"Npc[${e.getNpc.getIndex}] \"${name}\" spawned at ${e.getNpc.templateLocation}")
	}
	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		if (e.getNpc.templateLocation.getRegionID != HueyRegion) return
		if (!HueyNpcIds.contains(e.getNpc.getId)) return
		val name = e.getNpc.getId.pipe(n => npcIdToName.getOrElse(n, s"Unknown(${n})"))
		log.debug(s"Npc[${e.getNpc.getIndex}] \"${name}\" despawned at ${e.getNpc.templateLocation}")
	}

	@Subscribe
	def onNpcChanged(e: NpcChanged): Unit = {
		if (e.getNpc.templateLocation.getRegionID != HueyRegion) return
		if (!HueyNpcIds.contains(e.getNpc.getId)) return
		val name = e.getNpc.getId.pipe(n => npcIdToName.getOrElse(n, s"Unknown(${n})"))
		log.debug(s"Npc[${e.getNpc.getIndex}] at ${e.getNpc.templateLocation} changed from \"${e.getOld.getId.pipe(n => npcIdToName.getOrElse(n, s"Unknown(${n})"))}\" to  \"${name}\"")
	}
//
//	@Subscribe
//	def onDecorativeSpawned(e: DecorativeObjectSpawned): Unit = {
//		if(!HueyDecorativeObjectIds.contains(e.getDecorativeObject.getId)) return;
//		log.debug(s"Decorative object ${e.getDecorativeObject} named ${objectIdToName.getOrElse(e.getDecorativeObject.getId, s"${e.getDecorativeObject.getId}")} spawned @ ${e.getDecorativeObject.getWorldLocation.getTemplate}")
//	}
//
//	@Subscribe
//	def onDecorativeDespawned(e: DecorativeObjectDespawned): Unit = {
//		if (!HueyDecorativeObjectIds.contains(e.getDecorativeObject.getId)) return;
//		log.debug(s"Decorative object ${e.getDecorativeObject} named ${objectIdToName.getOrElse(e.getDecorativeObject.getId, s"${e.getDecorativeObject.getId}")} despawned @ ${e.getDecorativeObject.getWorldLocation.getTemplate}")
//	}	@Subscribe
//	def onNpcChanged(e: NpcChanged): Unit = {
//		if (e.getNpc.templateLocation.getRegionID != HueyRegion) return
//		if (!HueyNpcIds.contains(e.getNpc.getId)) return
//		val name = e.getNpc.getId.pipe(n => npcIdToName.getOrElse(n, s"Unknown(${n})"))
//		log.debug(s"Npc[${e.getNpc.getIndex}] at ${e.getNpc.templateLocation} changed from \"${e.getOld.getId.pipe(n => npcIdToName.getOrElse(n, s"Unknown(${n})"))}\" to  \"${name}\"")
//	}
//
//	@Subscribe
//	def onDecorativeSpawned(e: DecorativeObjectSpawned): Unit = {
//		if(!HueyDecorativeObjectIds.contains(e.getDecorativeObject.getId)) return;
//		log.debug(s"Decorative object ${e.getDecorativeObject} named ${objectIdToName.getOrElse(e.getDecorativeObject.getId, s"${e.getDecorativeObject.getId}")} spawned @ ${e.getDecorativeObject.getWorldLocation.getTemplate}")
//	}
//
//	@Subscribe
//	def onDecorativeDespawned(e: DecorativeObjectDespawned): Unit = {
//		if (!HueyDecorativeObjectIds.contains(e.getDecorativeObject.getId)) return;
//		log.debug(s"Decorative object ${e.getDecorativeObject} named ${objectIdToName.getOrElse(e.getDecorativeObject.getId, s"${e.getDecorativeObject.getId}")} despawned @ ${e.getDecorativeObject.getWorldLocation.getTemplate}")
//	}

/*

	@Subscribe
	def onGameObjectSpawned(e: ObjectSpawned): Unit = {
		val go = e.getGameObject
		if(!HueyObjectIds.contains(e.getGameObject.getId)) return
		if(go.templateLocation.getRegionID == HueyRegion) {
			val name = go.getId.pipe(id => objectIdToName.getOrElse(id, s"Unknown(${id})"))
			log.debug(s"Object[${go}] \"${name}\" spawned at ${go.templateLocation}")
		}
	}

	@Subscribe
	def onGameObjectDespawned(e: GameObjectDespawned): Unit = {
		val go   = e.getGameObject
		if(!HueyObjectIds.contains(e.getGameObject.getId)) return
		if (go.templateLocation.getRegionID == HueyRegion) {
			val name = go.getId.pipe(id => objectIdToName.getOrElse(id, s"Unknown(${id})"))
			log.debug(s"Object[${go}] \"${name}\" despawned at ${go.templateLocation}")
		}
	}
*/
	@Subscribe
	def onProjectileMoved(e: ProjectileMoved): Unit = {
		val projectile: Projectile = e.getProjectile
		if (projectile.getTargetActor != client.getLocalPlayer) return
		val name = projectile.getId.pipe(p => spotAnimationIdToName.getOrElse(p, s"Unknown($p)"))
		if (projectile.templateSourceLocation.getRegionID == HueyRegion || projectile.templateTargetLocation.getRegionID == HueyRegion) {
			if(projectile.justSpawned) {
				log.debug(s"Projectile[${projectile}] \"${name}\" from ${projectile.getSourceActor} spawned at ${projectile.templateSourceLocation} with ${projectile.ticksRemaining} ticks remaining")
				incomingProjectile = projectile
			}
		}
	}

//	@Subscribe
//	def onAnimationChanged(e: AnimationChanged): Unit = {
////		if (inRegion && inFight) {
//		if(e.getActor.templateLocation.getRegionID == HueyRegion) {
//			val animName = e.getActor.getAnimation.pipe(a => animationIdToName.getOrElse(a, s"Unknown(${a})"))
//			e.getActor match {
//				case npc: NPC => ("Npc", npc.getId.pipe(n => npcIdToName.getOrElse(n, s"$n")))
//				case player: Player => (if(player == client.getLocalPlayer) "LocalPlayer" else "Player", s"${player.getName}")
//			} match {
//				case (group, name) => log.debug(s"$group \"$name\" changed animation to \"$animName\"")
//			}
//		}
////		}
//	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
			val regionLine = LineComponent.builder().left("Region").right(s"$curRegion").rightColor(if(inRegion) Color.GREEN else Color.RED).build
			val projectileLine = LineComponent.builder().left("Projectile").right(s"${Option(incomingProjectile).fold("Null")(_.getId.pipe(p => spotAnimationIdToName.getOrElse(p, s"Unknown(${p})")))}").rightColor(if(incomingProjectile != null) Color.GREEN else Color.RED).build
			val pillarsLines = pillars.map {
				case (p, l) => LineComponent.builder().left(p.entryName).leftColor(p.getColor).right(s"$l").rightColor(ColorUtil.colorLerp(Color.RED, Color.GREEN, (l/5.0d).pipe(v => Math.min(1.0d, Math.max(0.0d, v))))).build
			}.toList.prepended(TitleComponent.builder().text("Pillars").build())
			Seq(regionLine, projectileLine, pillarsLines, Seq.empty[LayoutableRenderableEntity]).flatMap{
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
		def renderNpcOverlay(n:NPC, text: String, color: Color, zoffset: Int): Unit = {
			parent.getModelOutlineRenderer.drawOutline(n, 2,  color, 4)
			val poly = n.getCanvasTilePoly
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
			val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
			if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}

//		def renderDangerousTile(tile: GObjectData, color: Color): Unit = {
//			val poly = Perspective.getCanvasTilePoly(client, tile.lp)
//			if (poly != null) OverlayUtil.renderPolygon(g, poly, ColorUtil.colorWithAlpha(color, 200))
//
//			val ppc = new ProgressPieComponent()
//			ppc.setBorderColor(Color.BLACK)
//			ppc.setFill(Color.RED)
//			ppc.setProgress((client.getGameCycle - tile.spawnedCycle).doubleValue / (30.0d * 3))
//			ppc.setDiameter(28)
//			val point = Perspective.localToCanvas(client, tile.lp, client.getTopLevelWorldView.getPlane, 20)
//			ppc.setPosition(point)
//			ppc.render(g)
//
//			val text = s"${tile.age}"
//
//			val fm      = g.getFontMetrics()
//			val bounds  = fm.getStringBounds(text, g)
//			val xOffset = point.getX() - (bounds.getWidth() / 2).toInt
//			val yOffset = point.getY() + (bounds.getHeight() / 2).toInt
//
//			val textPoint = new Point(xOffset, yOffset)
//			OverlayUtil.renderTextLocation(g, textPoint, text, Color.WHITE)
//		}

//		def renderIceTile(tile: GameObject, color: Color): Unit = {
//			val lp   = tile.getLocalLocation
//			val poly = Perspective.getCanvasTilePoly(client, lp)
//			if (poly != null) OverlayUtil.renderPolygon(g, poly, ColorUtil.colorWithAlpha(color, 200))
//
////			val text = s"${tile.age}"
//
////			val fm      = g.getFontMetrics()
////			val bounds  = fm.getStringBounds(text, g)
////			val xOffset = point.getX() - (bounds.getWidth() / 2).toInt;
////			val yOffset = point.getY() + (bounds.getHeight() / 2).toInt;
////
////			val textPoint = new Point(xOffset, yOffset)
////			OverlayUtil.renderTextLocation(g, textPoint, text, Color.BLACK)
//		}

		if(inRegion){
//			dangerousTiles.values.toList.foreach(dt => {
//				renderDangerousTile(dt, Color.ORANGE)
//			})
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
