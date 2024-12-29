package com.fredplugins.pvmHelper

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.pvmHelper.scurrius.ScurriusLogic
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.*
import net.runelite.api.events.{AnimationChanged, GameStateChanged, GameTick, NpcDespawned, NpcSpawned, ProjectileMoved, VarbitChanged}
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
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
	name = "<html><font color=\"#A1004B\">Freds</font> PVM Helper</html>",
	description = "Provides some auto movement and prayer help for limited set of bosses",
	tags = Array("pvm", "scurrius", "prayer", "helper", "maps")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsPvmHelper() extends Plugin with BossToolTrait {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: FredsPvmHelperConfig = null
	@Inject val notifier: Notifier = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val configManager: ConfigManager = null
	private val panel: FredsPvmHelperPanel[FredsPvmHelper] = new FredsPvmHelperPanel(this) {}

	object ConfigCache {
		var debugRegions: Array[Int] = Array.empty[Int]
		var debugVarbits: Array[Int] = Array.empty[Int]
		var debugVarps: Array[Int] = Array.empty[Int]
	}

	case class State(regionId: Int = -1, projectiles: List[Projectile] = List.empty, npcAnimations: Map[NPC, Int] = Map.empty, playerAnimation: Int = -1) {
		def withRegionId(id: Int): State = if(id == regionId) this else copy(regionId = id)
		def withProjectile(p: Projectile): State = if(projectiles.contains(p)) this else copy(projectiles = projectiles :+ p)
		def withoutNpc(n: NPC): State = if(!npcAnimations.contains(n)) this else copy(npcAnimations = npcAnimations.removed(n))
		def withNpc(n: NPC): State = if(npcAnimations.contains(n)) this else copy(npcAnimations = npcAnimations.updated(n, n.getAnimation))
		def withNpcAnimation(n: NPC, aid: Int): State = if(npcAnimations.get(n).contains(aid)) this else copy(npcAnimations = npcAnimations.updated(n, aid))
		def withPlayerAnimation(aid: Int): State = if(playerAnimation == aid) this else copy(playerAnimation = aid)
	}
	private var state: State = State()
	private var oldState: State = state

	override def resetState(): Unit = {
		state = State()
		oldState = state
		ConfigCache.debugRegions = config.getDebugRegions.split(',').flatMap(x => x.toIntOption).filter(_ != -1)
		ConfigCache.debugVarbits = config.getDebugVarbits.split(',').flatMap(x => x.toIntOption).filter(_ != -1)
		ConfigCache.debugVarps = config.getDebugVarps.split(',').flatMap(x => x.toIntOption).filter(_ != -1)
	}

	//	@Inject private val overlay: FredsPvmHelperOverlay = null
	given Client = client

	@Provides
	def getConfig(configManager: ConfigManager): FredsPvmHelperConfig = {
		configManager.getConfig[FredsPvmHelperConfig](classOf[FredsPvmHelperConfig])
	}

	@Subscribe
	private def onConfigChanged(e: ConfigChanged): Unit = {
		if (e.getGroup == FredsPvmHelperConfig.GroupName) {
			e.getKey match {
				case "debugRegions" => ConfigCache.debugRegions = config.getDebugRegions.split(',').flatMap(x => x.toIntOption).filter(_ != -1)
				case "debugVarps" => ConfigCache.debugVarps = config.getDebugVarps.split(',').flatMap(x => x.toIntOption).filter(_ != -1)
				case "debugVarbits" => ConfigCache.debugVarbits = config.getDebugVarbits.split(',').flatMap(x => x.toIntOption).filter(_ != -1)
				case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
			}
		}
	}

	override def layoutPanel(): Seq[LayoutableRenderableEntity] = {
		Seq(
			LineComponent.builder
				.left("RegionId")
				.right(s"${state.regionId}")
				.build,
			LineComponent.builder
				.left("AnimationId")
				.right(s"${state.playerAnimation}")
				.build
		) ++
		state.npcAnimations.map(n => {
			val npc = n._1
			val anim = n._2
			LineComponent.builder
				.left(s"Npc{idx=${npc.getIndex}, id=${npc.getId}}")
				.right(s"${anim}")
				.build
		}).toList.prepended(TitleComponent.builder.text("Npc Animations").build()) ++
		state.projectiles.zipWithIndex.map(p => {
			val id = p._1.getId
			val interacting = if (p._1.getInteracting != null) {
				if (client.getLocalPlayer == p._1.getInteracting) "local" else p._1.getInteracting
			} else "null"
			val idx = p._2
			LineComponent.builder
				.left(s"${idx}")
				.right(s"${id} -> ${interacting}")
				.build
		}).prepended(TitleComponent.builder.text("Projectiles").build())
	}

	override def inArea(): Boolean = ConfigCache.debugRegions.contains(state.regionId)

	override protected def startUp(): Unit = {
		resetState()
		overlayManager.add(panel)
	}


	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
		resetState()
	}

	@Subscribe
	private def onGameTick(event:GameTick): Unit = {
		state = state
			.withRegionId(
				Try(WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1)
			)
		if (oldState != state) {
			log.debug("state changed from\n{}\nto\n{}", oldState, state)
		}
		oldState = state
	}

	@Subscribe
	private def onGameStateChanged(event: GameStateChanged): Unit = {
		log.debug("GameState changed to {}", event.getGameState)
		if (event.getGameState != GameState.LOGGED_IN) resetState()
	}

	@Subscribe
	private def onVarbitChanged(event: VarbitChanged): Unit = {
		if (!inArea()) return
		if(ConfigCache.debugVarps.contains(event.getVarpId) || ConfigCache.debugVarbits.contains(event.getVarbitId)) {
			log.debug(s"Varbit {}[{}]={}", event.getVarpId, event.getVarbitId, event.getValue)
		}
	}

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		log.debug(s"NpcSpawned: id={}, name={}", event.getNpc.getId, event.getNpc.getName)
		state = state.withNpc(event.getNpc)
	}

	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		log.debug(s"NpcDespawned: id={}, name={}", event.getNpc.getId, event.getNpc.getName)
		state = state.withoutNpc(event.getNpc)
	}

	@Subscribe
	private def onAnimationChanged(event: AnimationChanged): Unit = {
		if (!inArea() || event.getActor == null) return
		event.getActor match {
			case npc: NPC if state.npcAnimations.contains(npc) => {
				log.debug(s"onAnimationChanged: source=npc[{}], animationId={}", npc.getId, npc.getAnimation)
				state = state.withNpcAnimation(npc, npc.getAnimation)
			}
			case player: Player if (client.getLocalPlayer == player) => {
				log.debug(s"onAnimationChanged: source={}, animationId={}", "local", player.getAnimation)
				state = state.withPlayerAnimation(player.getAnimation)
			}
			case _ => {}
		}
	}

	@Subscribe
	private def onProjectileMoved(event: ProjectileMoved): Unit = {
		if (!inArea()) return
		if (event.getProjectile.getRemainingCycles == event.getProjectile.getEndCycle - event.getProjectile.getStartCycle) {
			log.debug(s"Projectile Spawned: id={}, event={}", event.getProjectile.getId, event)
			state = state.withProjectile(event.getProjectile)
		}
	}
}