package com.fredplugins.demonicgorillaV2

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.Binder
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import interactionApi.NPCInteraction
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.Hitsplat
import net.runelite.api.HitsplatID
import net.runelite.api.NPC
import net.runelite.api.Player
import net.runelite.api.Projectile
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.GameTick
import net.runelite.api.events.HitsplatApplied
import net.runelite.api.events.InteractingChanged
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.OverheadTextChanged
import net.runelite.api.events.PlayerDespawned
import net.runelite.api.events.PlayerSpawned
import net.runelite.api.events.ProjectileMoved
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDependency
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.ui.overlay.OverlayManager

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Demonic Gorillas V2</html>",
	enabledByDefault = false,
	description = "Count demonic gorilla attacks and display their next possible attack styles",
	tags = Array("combat", "overlay", "pve", "pvm")
	)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class DemonicGorillaV2Plugin extends Plugin with ShimUtils.Logging("DEBUG") {
	@Inject() private val client        : Client                = null
	@Inject() private val clientThread  : ClientThread          = null
	@Inject() private val overlayManager: OverlayManager        = null
	@Inject() private val overlay       : DemonicGorillaOverlay = null

	private var gorillas          : Map[NPC, DemonicGorilla]     = Map.empty
	private var recentBoulders    : List[WorldPoint]             = List.empty[WorldPoint]
	private var pendingAttacks    : List[PendingGorillaAttack]   = List.empty
	private var memorizedPlayers  : Map[Player, MemorizedPlayer] = Map.empty
	private var gorillaProjectiles: List[Projectile]             = scala.collection.immutable.List.empty[Projectile]

	private var atGorillas: Boolean = false

	def getGorillas: List[DemonicGorilla] = {
		gorillas.values.toList
	}

	override def configure(binder: Binder): Unit = super.configure(binder)

	override protected def startUp(): Unit = {
		if ((client.getGameState == GameState.LOGGED_IN) && atDemonicGorillas) {
			init()
		}
	}
	override protected def shutDown(): Unit = {
		atGorillas = false
		overlayManager.remove(overlay)
		gorillas = Map.empty[NPC, DemonicGorilla]
		recentBoulders = List.empty[WorldPoint]
		pendingAttacks = List.empty
		memorizedPlayers = Map.empty
		gorillaProjectiles = List.empty
	}

	private def init(): Unit = {
		atGorillas = true
		overlayManager.add(overlay)
		gorillas = Map.empty[NPC, DemonicGorilla]
		recentBoulders = List.empty[WorldPoint]
		pendingAttacks = List.empty[PendingGorillaAttack]
		gorillaProjectiles = List.empty[Projectile]
		memorizedPlayers = Map.empty[Player, MemorizedPlayer]
		clientThread.invoke(() => this.reset()) // Updates the list of gorillas and players

	}

	private def reset(): Unit = {
		recentBoulders = List.empty[WorldPoint]
		pendingAttacks = List.empty[PendingGorillaAttack]
		gorillas = resetGorillas()
		memorizedPlayers = resetPlayers()
	}

	private def resetGorillas(): Map[NPC, DemonicGorilla] = {
		Option(client.getTopLevelWorldView).map(_.npcs().asScala.toList.collect {
			case g@IsNpcGorilla() => g -> DemonicGorilla(g)(using client)
		}
																						).map(_.toMap).getOrElse(Map.empty[NPC, DemonicGorilla])
	}
	private def resetPlayers(): Map[Player, MemorizedPlayer] = {
		Option(client.getTopLevelWorldView).map(_.players().asScala.toList.collect {
			case p => p -> MemorizedPlayer(p)
		}
																						).map(_.toMap).getOrElse(Map.empty[Player, MemorizedPlayer])
	}

	@Subscribe
	private def onOverheadTextChanged(event: OverheadTextChanged): Unit = {
		(event.getActor match {
			case npc: NPC => gorillas.get(npc)
			case _ => Option.empty[DemonicGorilla]
		}).foreach(gorilla => {
			log.debug(s"gorilla[${Integer.toHexString(gorilla.npc.hashCode())}] has overhead text: \"${event.getOverheadText}\"")
		}
							 )
	}

	@Subscribe
	private def onProjectileMoved(event: ProjectileMoved): Unit = {
		if (!atGorillas) return
		val projectile   = event.getProjectile
		val projectileId = projectile.getId
		if (!DEMONIC_PROJECTILES.contains(projectileId)) return
		if (gorillaProjectiles.contains(projectile)) return
		gorillaProjectiles = gorillaProjectiles :+ projectile
		val loc = WorldPoint.fromLocal(
			client.getTopLevelWorldView, projectile.getX1, projectile.getY1,
			client.getTopLevelWorldView.getPlane
			)
		if (projectileId == DEMONIC_GORILLA_BOULDER) {
			recentBoulders = recentBoulders :+ loc
		} else {
			gorillas.values.foreach(gorilla => {
				if (gorilla.getWorldLocation.distanceTo(loc) == 0) gorilla.setRecentProjectileId(projectile.getId)
			}
															)
		}
	}
	@Subscribe
	private def onHitsplatApplied(event: HitsplatApplied): Unit = {
		if (!atGorillas || gorillas.isEmpty || !event.getHitsplat.isMine) return
		event.getActor match {
			case player: Player => {
				memorizedPlayers.get(player).foreach(mp => {
					mp.hit(event.getHitsplat)
				})
			}
			case npc: NPC => {
				gorillas.get(npc).foreach(_.setTakenDamageRecently(true))
			}
		}
	}

	@Subscribe
	private def onGameStateChanged(event: GameStateChanged): Unit = {
		event.getGameState match {
			case GameState.LOGGED_IN => {
				if (atDemonicGorillas) {
					if (!atGorillas) {
						init()
					}
				}
			}
			case GameState.HOPPING =>
			case GameState.LOGGING_IN =>
			case GameState.CONNECTION_LOST =>
			case GameState.LOGIN_SCREEN => {
				if (atGorillas) shutDown
			}
			case _ =>
		}
	}

	@Subscribe
	private def onPlayerSpawned(event: PlayerSpawned): Unit = {
		if (!atGorillas || gorillas.isEmpty) return
		//		val player = event.getPlayer
		memorizedPlayers = memorizedPlayers.updated(event.getPlayer, MemorizedPlayer(event.getPlayer))
	}

	@Subscribe
	private def onPlayerDespawned(event: PlayerDespawned): Unit = {
		if (!atGorillas || gorillas.isEmpty) return
		memorizedPlayers = memorizedPlayers.removed(event.getPlayer)
	}

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!atGorillas) return
		Option(event.getNpc).collect {
			case npc@IsNpcGorilla() => npc
		}.foreach(npc => {
			gorillas = gorillas.tap(g => if (g.isEmpty) resetPlayers()).updated(npc, new DemonicGorilla(npc)(using client))
		}
							)
	}

	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
		if (atGorillas) {
			val (toKeep, toRemove) = gorillas.partition(_._1 != event.getNpc).pipe {
				case (keep, remove) => keep -> remove.values.headOption
			}
			currentTarget = currentTarget.zip(toRemove).collect {
				case (ct, tr) if ct != tr => ct
			}.orElse(Option.empty[DemonicGorilla])

			gorillas = toKeep
		}
	}

	@Subscribe(priority = 100)
	private def onGameTick(event: GameTick): Unit = {

		if (atGorillas) {
			pendingAttacks = checkGorillaAttacks(
				gorillas.values.toList,
				memorizedPlayers.values.toList,
				recentBoulders
				)(using client).prependedAll(pendingAttacks)

			pendingAttacks = checkPendingAttacks(pendingAttacks, memorizedPlayers.values.toList)(using client)
			memorizedPlayers.values.foreach(_.update()) //			updatePlayers
			recentBoulders = List.empty[WorldPoint]
			clearProjectileArray()
		}
	}
	private var currentTarget: Option[DemonicGorilla] = Option.empty[DemonicGorilla]

	@Subscribe
	private def onInteractingChanged(interactingChanged: InteractingChanged): Unit = {
		if (client.getLocalPlayer == interactingChanged.getTarget && IsNpcGorilla.unapply(interactingChanged.getSource)) {
			currentTarget = gorillas.get(interactingChanged.getSource.asInstanceOf[NPC])
		}
	}

	@Subscribe(priority = 0)
	private def onGameTick2(event: GameTick): Unit = {
		if (atGorillas) {
			gorillas.foreach(_._2.gameTick())

			currentTarget.foreach(target => {
				println(target.toString)
			}
														)
		}
	}

	private def clearProjectileArray(): Unit = {
		gorillaProjectiles = gorillaProjectiles.filterNot(p => p.getRemainingCycles <= 0)
	}

	private def atDemonicGorillas: Boolean = {
		REGION_IDS.contains(client.getLocalPlayer.getWorldLocation.getRegionID())
	}
}
