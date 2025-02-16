package com.fredplugins.pvmHelper2

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.pvmHelper2.PvmEvent.{NpcEvent, PlayerEvent}
import com.fredplugins.pvmHelper2.gauntlet.{GauntletSolver, GauntletSolver2}
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.*
import net.runelite.api.events.{ActorDeath, AnimationChanged, GameStateChanged, GameTick, InteractingChanged, NpcChanged, NpcDespawned, NpcSpawned, PlayerDespawned, PlayerSpawned, ProjectileMoved, VarbitChanged}
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.{ConfigItem, ConfigManager}
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
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.IntAccumulator
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.StreamHasToScala
import scala.swing.Publisher
import scala.swing.event.Event
import scala.util.chaining.*
import scala.util.{Random, Try}

sealed trait PvmEvent extends scala.swing.event.Event with Product {

	//	def prefix: String = this.getClass.getSimpleName
	//	def elements: List[(String, Any)]
	def prefix: String
	def overrides: PartialFunction[Int, (String, String)]
	override def toString: String = {
		(for {
			i <- 0 until productArity
			(name, value) = overrides.applyOrElse(i, y => (productElementName(y), productElement(y).toString))
		} yield name -> value).map{
			case (elemName, elemValue) => s"$elemName: ${elemValue}"
		}.mkString(s"${prefix}.${this.productPrefix}(", ", ", ")")
	}
}
sealed trait ActorEvent extends PvmEvent {
	def source: Actor
}
object PvmEvent {
	sealed trait AnimationChangedEvent extends ActorEvent {
		def old: Int
		def current: Int
	}
	sealed trait SpawnedEvent extends ActorEvent {}
	sealed trait DespawnedEvent extends ActorEvent {}
	sealed trait DeathEvent extends ActorEvent {}
	sealed trait InteractingChangedEvent extends ActorEvent {
		this: Product =>
		def old: Actor | Null
		def current: Actor | Null

		override def overrides: PartialFunction[Int, (String, String)] = {
			case 0 => productElementName(0) -> (source match {
				case p: Player => s"Player(${p.getId})"
				case n: NPC => s"NPC(${n.getIndex})"
			})
			case 1 => {
				productElementName(1) -> (Option(old) match {
					case Some(player: Player) => s"Player(${player.getId})"
					case Some(npc: NPC) => s"NPC(${npc.getIndex})"
					case _ => "None"
				})
			}
			case 2 => {
				productElementName(2) -> (Option(current) match {
					case Some(player: Player) => s"Player(${player.getId})"
					case Some(npc: NPC) => s"NPC(${npc.getIndex})"
					case _ => "None"
				})
			}
		}
	}

	sealed trait NpcEvent extends ActorEvent {
		override def source: NPC
		override def prefix: String = s"NpcEvent"
	}
	sealed trait PlayerEvent extends ActorEvent {
		self: Product =>
		override def source: Player
		override def prefix: String = s"PlayerEvent"
	}
	object NpcEvent {
		case class Spawned(source: NPC) extends NpcEvent with SpawnedEvent {
			override def overrides: PartialFunction[Int, (String, String)] = {
				case 0 => (productElementName(0), s"NPC(${source.getIndex})")
			}
		}
		case class Despawned(source: NPC) extends NpcEvent with DespawnedEvent {
			override def overrides: PartialFunction[Int, (String, String)] = {
				case 0 => (productElementName(0), s"NPC(${source.getIndex})")
			}
		}
		case class CompositionChanged(source: NPC, old: NPCComposition, current: NPCComposition) extends NpcEvent {
			override def overrides: PartialFunction[Int, (String, String)] = {
				case 0 => (productElementName(0), s"NPC(${source.getIndex})")
			}
		}
		case class AnimationChanged(source: NPC, old: Int, current: Int) extends NpcEvent with AnimationChangedEvent {
			override def overrides: PartialFunction[Int, (String, String)] = {
				case 0 => (productElementName(0), s"NPC(${source.getIndex})")
			}
		}
		case class Death(source: NPC) extends NpcEvent with DeathEvent {
			override def overrides: PartialFunction[Int, (String, String)] = {
				case 0 => (productElementName(0), s"NPC(${source.getIndex})")
			}
		}
		case class InteractingChanged(source: NPC, old: Actor | Null, current: Actor | Null) extends NpcEvent with InteractingChangedEvent {}
	}
	object PlayerEvent {
		case class Spawned(source: Player) extends PlayerEvent with SpawnedEvent {
			override def overrides: PartialFunction[Int, (String, String)] = {
				case 0 => (productElementName(0), s"Player(${source.getId})")
			}
		}
		case class Despawned(source: Player) extends PlayerEvent with DespawnedEvent {
			override def overrides: PartialFunction[Int, (String, String)] = {
				case 0 => (productElementName(0), s"Player(${source.getId})")
			}
		}
		case class AnimationChanged(source: Player, old: Int, current: Int) extends PlayerEvent with AnimationChangedEvent {
			override def overrides: PartialFunction[Int, (String, String)] = {
				case 0 => (productElementName(0), s"Player(${source.getId})")
			}
		}
		case class Death(source: Player) extends PlayerEvent with DeathEvent {
			override def overrides: PartialFunction[Int, (String, String)] = {
				case 0 => (productElementName(0), s"Player(${source.getId})")
			}
		}
		case class InteractingChanged(source: Player, old: Actor | Null, current: Actor | Null) extends PlayerEvent with InteractingChangedEvent {}
	}
}

@PluginDescriptor(
	name = "<html><font color=\"#A1004B\">Freds</font> PVM Helper 2</html>",
	description = "Provides some auto movement and prayer help for limited set of bosses",
	tags = Array("pvm", "scurrius", "jad", "prayer", "helper", "maps")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsPvmHelper2() extends Plugin {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: FredsPvmHelperConfig2 = null
	@Inject val notifier: Notifier = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val configManager: ConfigManager = null
	@Inject private val panel: FredsPvmHelper2Panel = null

	private object ActorTracker extends scala.swing.Publisher {
		private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
		private val actorToCachedAnimationId: mutable.HashMap[Actor, Int] = mutable.HashMap.empty
		private val actorToCachedInteracting: mutable.HashMap[Actor, Actor | Null] = mutable.HashMap.empty

		//	private val npcToCachedAnimationId: mutable.HashMap[NPC, Int] = mutable.HashMap.empty
		//	private val playerToCachedAnimationId: mutable.HashMap[Player, Int] = mutable.HashMap.empty

		def onNpcCompositionChanged(n: NPC, old: NPCComposition): Unit = {
			publish(PvmEvent.NpcEvent.CompositionChanged(n, old, n.getComposition))
		}

		def onAnimationChanged(a: Actor): Unit = {
			actorToCachedAnimationId.get(a).map(old => {
				a match {
					case npc: NPC => PvmEvent.NpcEvent.AnimationChanged(npc, old, npc.getAnimation)
					case player: Player => PvmEvent.PlayerEvent.AnimationChanged(player, old, player.getAnimation)
				}
			}).filter(e => e.current != e.old).foreach(publish)
		}

		def onDespawn(a: Actor): Unit = {
			Option(a).collect {
				case npc: NPC => PvmEvent.NpcEvent.Despawned(npc)
				case player: Player => PvmEvent.PlayerEvent.Despawned(player)
			}.foreach(publish)
		}

		def onSpawn(a: Actor): Unit = {
			Option(a).collect {
				case npc: NPC => PvmEvent.NpcEvent.Spawned(npc)
				case player: Player => PvmEvent.PlayerEvent.Spawned(player)
			}.foreach(publish)
		}

		def onDeath(a: Actor): Unit = {
			Option(a).collect {
				case npc: NPC => PvmEvent.NpcEvent.Death(npc)
				case player: Player => PvmEvent.PlayerEvent.Death(player)
			}.foreach(publish)
		}

		def onInteractingChanged(source: Actor, target: Actor): Unit = {
			val old = actorToCachedInteracting.get(source).orNull
			Option(source match {
				case npc: NPC => PvmEvent.NpcEvent.InteractingChanged(npc, old, target)
				case player: Player => PvmEvent.PlayerEvent.InteractingChanged(player, old, target)
			}).filter(e => e.old != e.current).foreach(publish)
		}
		reactions += {
			case e: PvmEvent.SpawnedEvent => {
				actorToCachedAnimationId.put(e.source, e.source.getAnimation)
				actorToCachedInteracting.put(e.source, null)
			}
			case e: PvmEvent.DespawnedEvent => {
				actorToCachedAnimationId.remove(e.source)
				actorToCachedInteracting.remove(e.source)
			}
			case e: PvmEvent.AnimationChangedEvent => actorToCachedAnimationId.update(e.source, e.current)
			case e: PvmEvent.InteractingChangedEvent => actorToCachedInteracting.update(e.source, e.current)
		}
//		reactions += {
//			case e: PlayerEvent if e.source == client.getLocalPlayer => log.debug("Published event 1: {}", e)
//			case e: PlayerEvent =>
//			case p: PvmEvent => log.debug("Published event 2: {}", p)
////			case e: NpcEvent if e.source.getId == client.getLocalPlayer => log.debug("Published event {}", e)
//		}
	}

	@Subscribe
	private def onNpcChanged(event: NpcChanged): Unit = {
		ActorTracker.onNpcCompositionChanged(event.getNpc, event.getOld)
	}
	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		ActorTracker.onSpawn(event.getNpc)
	}
	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
		ActorTracker.onDespawn(event.getNpc)
	}
	@Subscribe
	private def onPlayerSpawned(event: PlayerSpawned): Unit = {
		ActorTracker.onSpawn(event.getPlayer)
	}
	@Subscribe
	private def  onPlayerDespawned(event: PlayerDespawned): Unit = {
		ActorTracker.onDespawn(event.getPlayer)
	}
	@Subscribe
	private def onAnimationChanged(event: AnimationChanged): Unit = {
		ActorTracker.onAnimationChanged(event.getActor)
	}
	@Subscribe
	private def onActorDeath(event: ActorDeath): Unit = {
		ActorTracker.onDeath(event.getActor)
	}

	@Subscribe
	private def onInteractingChanged(event: InteractingChanged): Unit = {
		ActorTracker.onInteractingChanged(event.getSource, event.getTarget)
	}

	@Subscribe
	private def onVarbitChanged(event: VarbitChanged): Unit = {
		if(event.getVarbitId == 9178) {
			Option(event.getValue) collect {
				case 0 => gauntletRoom2.deafTo(ActorTracker)
				case 1 => gauntletRoom2.listenTo(ActorTracker)
			}
		}
	}

	given Client = client
	given EventBus = eventBus

	@Provides
	def getConfig(configManager: ConfigManager): FredsPvmHelperConfig2 = {
		configManager.getConfig[FredsPvmHelperConfig2](classOf[FredsPvmHelperConfig2])
	}

//	@Inject val gauntletRoom: GauntletSolver = null
	@Inject val gauntletRoom2: GauntletSolver2 = null

	@Subscribe
	private def onConfigChanged(e: ConfigChanged): Unit = {
		if (e.getGroup == FredsPvmHelperConfig2.GroupName) {
			log.debug("config[{}] changed from {} to {}", e.getKey, e.getOldValue, e.getNewValue)
		}
	}

	//runs startup code
	//sends current scene data as new events
	//registers with regular event bus3
	override protected def startUp(): Unit = {
		log.debug("Staring up plugin")
//		gauntletRoom.startup()
		if(clientThread.runOnClientThread(() => Option(client.getVarbitValue(9178)).contains(1))) {
			gauntletRoom2.listenTo(ActorTracker)
		}

		overlayManager.add(panel)
//		eventBus.register(gauntletSolver)
//		gauntletSolver.startup();

//		println(s"\n${sEventBus.getKeys.map(_.toString).map(s => s"\t${s}").mkString("\n")}")
//		overlayManager.add(overlay)
	}


	override protected def shutDown(): Unit = {
		log.debug("Shutting down up plugin")
//		gauntletRoom.shutdown()
		gauntletRoom2.deafTo(ActorTracker)
		overlayManager.remove(panel)
//		overlayManager.remove(overlay)
	}


//	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
//		state = state
//			.withRegionId(
//				Try(WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1)
//			)
//		if (oldState != state) {
//			log.debug("state changed from\n{}\nto\n{}", oldState, state)
//		}
//		oldState = state
	}

//	@Subscribe
	private def onGameStateChanged(event: GameStateChanged): Unit = {
		log.debug("GameState changed to {}", event.getGameState)
//		if (event.getGameState != GameState.LOGGED_IN) resetState()
	}

//	@Subscribe
//	private def onVarbitChanged(event: VarbitChanged): Unit = {
//		if(event.getVarbitId == 9178) {
//			if(event.getValue == 0) {
//				eventBus.unregister(PvmSolver.Gauntlet)
//			} else if(event.getValue == 1) {
//				PvmSolver.Gauntlet.resetState()
//				eventBus.register(PvmSolver.Gauntlet)
//			}
//		}

//		if(ConfigCache.debugVarps.contains(event.getVarpId) || ConfigCache.debugVarbits.contains(event.getVarbitId)) {
//			log.debug(s"Varbit {}[{}]={}", event.getVarpId, event.getVarbitId, event.getValue)
//		}
//	}
//
//	@Subscribe
//	private def onProjectileMoved(event: ProjectileMoved): Unit = {
//		if (event.getProjectile.getRemainingCycles == event.getProjectile.getEndCycle - event.getProjectile.getStartCycle) {
//			log.debug(s"Projectile Spawned: id={}, event={}", event.getProjectile.getId, event)
////			state = state.withProjectile(event.getProjectile)
//		}
//	}
}