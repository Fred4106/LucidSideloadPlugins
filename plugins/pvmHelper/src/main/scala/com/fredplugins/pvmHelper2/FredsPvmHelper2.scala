package com.fredplugins.pvmHelper2

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.pvmHelper2.NpcEvent.NpcRecord
import com.fredplugins.pvmHelper2.PlayerEvent.PlayerRecord
import com.fredplugins.pvmHelper2.gauntlet.GauntletSolver
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
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
import scala.jdk.CollectionConverters.{IteratorHasAsScala, ListHasAsScala}
//import scala.jdk.CollectionConverters.*
import scala.jdk.IntAccumulator
import scala.jdk.OptionConverters.*
import scala.swing.Publisher
import scala.swing.event.Event
import scala.util.chaining.*
import scala.util.{Random, Try}

//sealed trait PvmEvent extends scala.swing.event.Event with Product {
//
//	//	def prefix: String = this.getClass.getSimpleName
//	//	def elements: List[(String, Any)]
//	def prefix: String
//	def overrides: PartialFunction[Int, (String, String)]
//	override def toString: String = {
//		(for {
//			i <- 0 until productArity
//			(name, value) = overrides.applyOrElse(i, y => (productElementName(y), productElement(y).toString))
//		} yield name -> value).map{
//			case (elemName, elemValue) => s"$elemName: ${elemValue}"
//		}.mkString(s"${prefix}.${this.productPrefix}(", ", ", ")")
//	}
//}
//sealed trait ActorEvent extends PvmEvent {
//	def source: Actor
//}
//object PvmEvent {
//	sealed trait AnimationChangedEvent extends ActorEvent {
//		def old: Int
//		def current: Int
//	}
//	sealed trait SpawnedEvent extends ActorEvent {}
//	sealed trait DespawnedEvent extends ActorEvent {}
//	sealed trait DeathEvent extends ActorEvent {}
//	sealed trait InteractingChangedEvent extends ActorEvent {
//		this: Product =>
//		def old: Actor | Null
//		def current: Actor | Null
//
//		override def overrides: PartialFunction[Int, (String, String)] = {
//			case 0 => productElementName(0) -> (source match {
//				case p: Player => s"Player(${p.getId})"
//				case n: NPC => s"NPC(${n.getIndex})"
//			})
//			case 1 => {
//				productElementName(1) -> (Option(old) match {
//					case Some(player: Player) => s"Player(${player.getId})"
//					case Some(npc: NPC) => s"NPC(${npc.getIndex})"
//					case _ => "None"
//				})
//			}
//			case 2 => {
//				productElementName(2) -> (Option(current) match {
//					case Some(player: Player) => s"Player(${player.getId})"
//					case Some(npc: NPC) => s"NPC(${npc.getIndex})"
//					case _ => "None"
//				})
//			}
//		}
//	}
//
//	sealed trait NpcEvent extends ActorEvent {
//		override def source: NPC
//		override def prefix: String = s"NpcEvent"
//	}
//	sealed trait PlayerEvent extends ActorEvent {
//		self: Product =>
//		override def source: Player
//		override def prefix: String = s"PlayerEvent"
//	}
//	object NpcEvent {
//		case class Spawned(source: NPC) extends NpcEvent with SpawnedEvent {
//			override def overrides: PartialFunction[Int, (String, String)] = {
//				case 0 => (productElementName(0), s"NPC(${source.getIndex})")
//			}
//		}
//		case class Despawned(source: NPC) extends NpcEvent with DespawnedEvent {
//			override def overrides: PartialFunction[Int, (String, String)] = {
//				case 0 => (productElementName(0), s"NPC(${source.getIndex})")
//			}
//		}
//		case class CompositionChanged(source: NPC, old: NPCComposition, current: NPCComposition) extends NpcEvent {
//			override def overrides: PartialFunction[Int, (String, String)] = {
//				case 0 => (productElementName(0), s"NPC(${source.getIndex})")
//			}
//		}
//		case class AnimationChanged(source: NPC, old: Int, current: Int) extends NpcEvent with AnimationChangedEvent {
//			override def overrides: PartialFunction[Int, (String, String)] = {
//				case 0 => (productElementName(0), s"NPC(${source.getIndex})")
//			}
//		}
//		case class Death(source: NPC) extends NpcEvent with DeathEvent {
//			override def overrides: PartialFunction[Int, (String, String)] = {
//				case 0 => (productElementName(0), s"NPC(${source.getIndex})")
//			}
//		}
//		case class InteractingChanged(source: NPC, old: Actor | Null, current: Actor | Null) extends NpcEvent with InteractingChangedEvent {}
//	}
//	object PlayerEvent {
//		case class Spawned(source: Player) extends PlayerEvent with SpawnedEvent {
//			override def overrides: PartialFunction[Int, (String, String)] = {
//				case 0 => (productElementName(0), s"Player(${source.getId})")
//			}
//		}
//		case class Despawned(source: Player) extends PlayerEvent with DespawnedEvent {
//			override def overrides: PartialFunction[Int, (String, String)] = {
//				case 0 => (productElementName(0), s"Player(${source.getId})")
//			}
//		}
//		case class AnimationChanged(source: Player, old: Int, current: Int) extends PlayerEvent with AnimationChangedEvent {
//			override def overrides: PartialFunction[Int, (String, String)] = {
//				case 0 => (productElementName(0), s"Player(${source.getId})")
//			}
//		}
//		case class Death(source: Player) extends PlayerEvent with DeathEvent {
//			override def overrides: PartialFunction[Int, (String, String)] = {
//				case 0 => (productElementName(0), s"Player(${source.getId})")
//			}
//		}
//		case class InteractingChanged(source: Player, old: Actor | Null, current: Actor | Null) extends PlayerEvent with InteractingChangedEvent {}
//	}
//}

@PluginDescriptor(
	name = "<html><font color=\"#A1004B\">Freds</font> PVM Helper 2</html>",
	description = "Provides some auto movement and prayer help for limited set of bosses",
	tags = Array("pvm", "scurrius", "jad", "prayer", "helper", "maps")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsPvmHelper2() extends Plugin with scala.swing.Publisher {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: FredsPvmHelperConfig2 = null
	@Inject val notifier: Notifier = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val configManager: ConfigManager = null
	@Inject private val panel: FredsPvmHelper2Panel = null

	private val cachedVarbitValues: mutable.Map[Int, Int] = mutable.HashMap.empty[Int, Int]

	@Provides
	def getConfig(configManager: ConfigManager): FredsPvmHelperConfig2 = {
		configManager.getConfig[FredsPvmHelperConfig2](classOf[FredsPvmHelperConfig2])
	}

//	@Inject val gauntletRoom: GauntletSolver = null
	@Inject val gauntletRoom2: GauntletSolver = null

	@Subscribe
	private def onConfigChanged(e: ConfigChanged): Unit = {
		if (e.getGroup == FredsPvmHelperConfig2.GroupName) {
			log.debug("config[{}] changed from {} to {}", e.getKey, e.getOldValue, e.getNewValue)
		}
	}
	given FredsPvmHelper2 = this

	//runs startup code
	//sends current scene data as new events
	//registers with regular event bus3
	override protected def startUp(): Unit = {
		log.debug("Staring up plugin")
		cachedVarbitValues.clear
		lastTickNpcRecordOpt = Option.empty[Map[NPC, NpcRecord]]
		lastTickPlayersRecordOpt = Option.empty[Map[Player, PlayerRecord]]
//		import com.fredplugins.pvmHelper2.PvmModule.{*, given}
		gauntletRoom2.enable()

//		clientThread.runOnClientThread(() => {
//			(0 until 5000).flatMap(id => Try(client.getVarbitValue(id)).toOption.filter(_ != 0).map(v => id -> v))
//				.map{
//					case (id, value) => new VarbitChanged().tap(_.setVarbitId(id)).tap(_.setValue(value))
//				}.foreach(onVarbitChanged)
//		})

		overlayManager.add(panel)
//		eventBus.register(gauntletSolver)
//		gauntletSolver.startup();

//		println(s"\n${sEventBus.getKeys.map(_.toString).map(s => s"\t${s}").mkString("\n")}")
//		overlayManager.add(overlay)
	}


	override protected def shutDown(): Unit = {
		log.debug("Shutting down up plugin")
//		gauntletRoom.shutdown()
		gauntletRoom2.disable()
		overlayManager.remove(panel)
//		overlayManager.remove(overlay)
	}

	@Subscribe
	private def onVarbitChanged(event: VarbitChanged): Unit = {
		//		client.getVarps
		//		client.getVarbit
		if (event.getVarbitId != -1) {
			val id = event.getVarbitId
			val old = cachedVarbitValues.get(id)
			val cur = event.getValue
			if(!old.contains(cur)) {
				publish(ClientEvent.VarbitChanged(id, old.getOrElse(0), cur))
			}
			cachedVarbitValues.put(id, cur)
//			publish(
//				ClientEvent.VarbitChanged(id, old)
//					.tap(x => {
//						cachedVarbitValues.put(x.source, x.cur)
//					})
//			)
		}
	}

	private var lastTickNpcRecordOpt: Option[Map[NPC, NpcRecord]] = Option.empty[Map[NPC, NpcRecord]]
	private var lastTickPlayersRecordOpt: Option[Map[Player, PlayerRecord]] = Option.empty[Map[Player, PlayerRecord]]
	@Subscribe(priority = 1000)
	private def onGameTick(event: GameTick): Unit = {
		def updateNpcs(): Seq[NpcEvent] = {
			val nMap: Map[NPC, NpcRecord] = client.getTopLevelWorldView.npcs().iterator().asScala.toList.map(n => {
				val key = n
				val record: NpcRecord = NpcRecord(n)(using client)
				(key,record)
			}).toMap

			val toBuildWith = (lastTickNpcRecordOpt.map(lastTickNpcRecord => {
				val sameNpcs = nMap.keySet.intersect(lastTickNpcRecord.keySet)
				val removedNpcs = lastTickNpcRecord.keySet.diff(sameNpcs)
				val newNpcs = nMap.keySet.diff(sameNpcs)
				(Option(lastTickNpcRecord), sameNpcs, removedNpcs, newNpcs)
			}).getOrElse{
				(Option.empty[Map[NPC, NpcRecord]], Set.empty[NPC], Set.empty[NPC], nMap.keySet)
			})

			val eventsToHandle: List[NpcEvent] = toBuildWith.pipe{
				case (None, _, _, added: Set[NPC]) => {
					val x = added.toList.map(n => NpcEvent.Spawned.apply(n, nMap(n)))
					x
				}
				case (Some(lastTickMap: Map[NPC, NpcRecord]), same: Set[NPC], removed: Set[NPC], added: Set[NPC]) => {
					val buildDelta = (n: NPC) => {
						NpcRecord.delta(n)(lastTickMap(n), nMap(n)).collect {
							case event: NpcEvent.NpcFragEvent if event.cur != event.old => event
						}
					}

					val x = added.toList.map(n =>NpcEvent.Spawned.apply(n, nMap(n)))
					val y = removed.toList.map(n => NpcEvent.Despawned.apply(n, lastTickMap(n)))
					val d = same.toList.flatMap(n => buildDelta(n))
					val toRet = y ++ d ++ x
					toRet
				}
			}
			lastTickNpcRecordOpt = Option(nMap)
			eventsToHandle
		}
		def updatePlayers(): Seq[PlayerEvent] = {
			val nMap: Map[Player, PlayerRecord] = client.getTopLevelWorldView.players().iterator().asScala.toList.map(n => {
				val key = n
				val record: PlayerRecord = PlayerRecord(n)(using client)
				(key, record)
			}).toMap

			val toBuildWith = (lastTickPlayersRecordOpt.map(lastTickPlayerRecord => {
				val sameNpcs = nMap.keySet.intersect(lastTickPlayerRecord.keySet)
				val removedNpcs = lastTickPlayerRecord.keySet.diff(sameNpcs)
				val newNpcs = nMap.keySet.diff(sameNpcs)
				(Option(lastTickPlayerRecord), sameNpcs, removedNpcs, newNpcs)
			}).getOrElse {
				(Option.empty[Map[Player, PlayerRecord]], Set.empty[Player], Set.empty[Player], nMap.keySet)
			})

			val eventsToHandle: List[PlayerEvent] = toBuildWith.pipe {
				case (None, _, _, added: Set[Player]) => {
					val x = added.toList.map(n => PlayerEvent.Spawned.apply(n, nMap(n)))
					x
				}
				case (Some(lastTickMap: Map[Player, PlayerRecord]), same: Set[Player], removed: Set[Player], added: Set[Player]) => {
					val buildDelta = (n: Player) => {
						PlayerRecord.delta(n)(lastTickMap(n), nMap(n)).collect {
							case event: PlayerEvent.PlayerFragEvent if event.cur != event.old => event
						}
					}

					val x = added.toList.map(n => PlayerEvent.Spawned.apply(n, nMap(n)))
					val y = removed.toList.map(n => PlayerEvent.Despawned.apply(n, lastTickMap(n)))
					val d = same.toList.flatMap(n => buildDelta(n))
					val toRet = y ++ d ++ x
					toRet
				}
			}
			lastTickPlayersRecordOpt = Option(nMap)
			eventsToHandle
		}
		val eventsToHandle = updateNpcs() ++ updatePlayers() :+ ClientEvent.ServerTick(client.getTickCount)
		eventsToHandle.foreach(publish)
//		publish(ClientEvent.ServerTick(client.getTickCount))
	}

//	@Subscribe
	private def onGameStateChanged(event: GameStateChanged): Unit = {
//		log.debug("GameState changed to {}", event.getGameState)
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