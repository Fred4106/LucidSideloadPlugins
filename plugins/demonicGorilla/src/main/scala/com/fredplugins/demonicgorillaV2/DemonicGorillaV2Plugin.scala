package com.fredplugins.demonicgorillaV2

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.Binder
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import interactionApi.NPCInteraction
import net.runelite.api.AnimationID
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.HeadIcon
import net.runelite.api.Hitsplat
import net.runelite.api.HitsplatID
import net.runelite.api.NPC
import net.runelite.api.Player
import net.runelite.api.Projectile
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.AnimationChanged
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

import scala.collection.mutable
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

	private val gorillas          : mutable.Map[NPC, GorillaData]     = mutable.HashMap.empty[NPC, GorillaData]
	private val memorizedPlayers  : mutable.Map[Player, MemorizedPlayer] = mutable.HashMap.empty[Player, MemorizedPlayer]
	private val recentBoulders    : mutable.ListBuffer[WorldPoint]             = mutable.ListBuffer.empty[WorldPoint]
	private val pendingAttacks    : mutable.ListBuffer[PendingGorillaAttack]   = mutable.ListBuffer.empty
	private val gorillaProjectiles: mutable.ListBuffer[Projectile]             = mutable.ListBuffer.empty[Projectile]

	private var atGorillas: Boolean = false

	def getGorillas: List[(NPC,GorillaData)] = {
		gorillas.toList
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
		gorillas.clear()// = Map.empty[NPC, DemonicGorilla]
		recentBoulders.clear()
		pendingAttacks.clear()
		memorizedPlayers.clear()
		gorillaProjectiles.clear()
	}

	private def init(): Unit = {
		atGorillas = true
		overlayManager.add(overlay)
		recentBoulders.clear()
		pendingAttacks.clear()
		gorillaProjectiles.clear()
		gorillas.clear()
		memorizedPlayers.clear()
		clientThread.invoke(() => {

			Option(client.getTopLevelWorldView).map(_.npcs().asScala.toList.collect {
				case g@IsNpcGorilla() => g
			}).getOrElse(List.empty[NPC]).foreach(gNpc => {
				val protectedFromStyle = EthanApiPlugin.getHeadIcon(gNpc) match {
					case HeadIcon.MELEE => AttackStyle.Melee
					case HeadIcon.RANGED => AttackStyle.Ranged
					case HeadIcon.MAGIC => AttackStyle.Magic
				}
				gorillas.put(gNpc, GorillaData.apply(AttackStyle.RegularAttacks.toSet, protectedFromStyle))
			})

			Option(client.getTopLevelWorldView).map(_.players().asScala.toList).getOrElse(List.empty[Player]).foreach(p => {
				memorizedPlayers.put(p, new MemorizedPlayer(p)(using client))
			})
		}) // Updates the list of gorillas and players

	}

	@Subscribe
	private def onOverheadTextChanged(event: OverheadTextChanged): Unit = {
		Option(event.getActor).collect {
			case npc: NPC if IsNpcGorilla.unapply(npc) && gorillas.contains(npc) => npc
		}.foreach(gNpc => {
			val previousData = gorillas(gNpc)
			val newAttackStyle = if(previousData.attackStyle.size == 1)
				AttackStyle.RegularAttacks.filter(_ != previousData.attackStyle.head).toSet
			else AttackStyle.RegularAttacks.toSet
			val updatedData = previousData.copy(attackStyle = newAttackStyle, attacksUntilSwitch = 3)
			gorillas.update(gNpc, updatedData)
		})
	}

	@Subscribe
	private def onAnimationChanged(event: AnimationChanged): Unit = {
		Option(event.getActor).collect {
			case gNpc: NPC if IsNpcGorilla.unapply(gNpc) && gorillas.contains(gNpc) => {
				val previous = gorillas(gNpc)
				val animationId = gNpc.getAnimation

				if(previous.previousAnimationId != animationId) {
					Option(animationId).collect {
						case AnimationID.DEMONIC_GORILLA_MELEE_ATTACK => previous.copy(attackStyle = Set(AttackStyle.Melee), attacksUntilSwitch = previous.attacksUntilSwitch - 1)
						case AnimationID.DEMONIC_GORILLA_MAGIC_ATTACK => previous.copy(attackStyle = Set(AttackStyle.Magic), attacksUntilSwitch = previous.attacksUntilSwitch - 1)
						case AnimationID.DEMONIC_GORILLA_RANGED_ATTACK => previous.copy(attackStyle = Set(AttackStyle.Ranged), attacksUntilSwitch = previous.attacksUntilSwitch - 1)
					}.map(nAttackStyle => {
						nAttackStyle.copy(previousAnimationId = animationId)
					}).foreach(gorillas.update(gNpc, _))
				}
			}
		}
	}

	@Subscribe
	private def onProjectileMoved(event: ProjectileMoved): Unit = {
		if (!atGorillas) return
		val projectile   = event.getProjectile
		val projectileId = projectile.getId
		if (!DEMONIC_PROJECTILES.contains(projectileId)) return
		if (!gorillaProjectiles.contains(projectile)) {
			val loc = WorldPoint.fromLocal(
				client.getTopLevelWorldView, projectile.getX1, projectile.getY1,
				client.getTopLevelWorldView.getPlane)
			if (projectileId == DEMONIC_GORILLA_BOULDER) {
				recentBoulders.addOne(loc)
			} else {
//				gorillas.toList.filter(_._1.getWorldLocation.distanceTo(loc) == 0).foreach(gorilla => gorilla.setRecentProjectileId(projectileId))
				gorillas.keys.filter(_.getWorldLocation.distanceTo(loc) == 0).foreach(gNpc => {
					gorillas.updateWith(gNpc) {
						previous => previous.map(_.copy(previousProjectileId = projectileId))
					}
				})
			}
			gorillaProjectiles.addOne(projectile)
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
				gorillas.updateWith(npc)(_.map(previous => {
					previous.copy(takenDamageRecently = true)
				}))
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
		if (!atGorillas) return
		//		val player = event.getPlayer
		memorizedPlayers.put(event.getPlayer, new MemorizedPlayer(event.getPlayer)(using client))
	}

	@Subscribe
	private def onPlayerDespawned(event: PlayerDespawned): Unit = {
		if (!atGorillas) return
		memorizedPlayers.remove(event.getPlayer).foreach {
			removed => log.debug(s"Removed player ${Integer.toHexString(removed.hashCode())} -> ${removed}")
		}
	}

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!atGorillas) return
		Option(event.getNpc).collect {
			case npc@IsNpcGorilla() if !gorillas.contains(npc) => npc
		}.foreach(npc => {
			val protectedFromStyle = EthanApiPlugin.getHeadIcon(npc) match {
				case HeadIcon.MELEE => AttackStyle.Melee
				case HeadIcon.RANGED => AttackStyle.Ranged
				case HeadIcon.MAGIC => AttackStyle.Magic
			}
			gorillas.put(npc, GorillaData.apply(AttackStyle.RegularAttacks.toSet, protectedFromStyle))
		})
	}

	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
		if(!atGorillas) return
		Option(event.getNpc).collect {
			case npc@IsNpcGorilla() if gorillas.contains(npc) => npc
		}.foreach(npc => {
			gorillas.remove(npc)
		})
//		if (atGorillas) {
//			gorillas.remove(event.getNpc)
//			val (toKeep, toRemove) = gorillas.partition(_._1 != event.getNpc).pipe {
//				case (keep, remove) => keep -> remove.values.headOption
//			}
//			currentTarget = currentTarget.zip(toRemove).collect {
//				case (ct, tr) if ct != tr => ct
//			}.orElse(Option.empty[DemonicGorilla])
//
//			gorillas = toKeep
//		}
	}

	private var currentTarget: Option[NPC] = Option.empty[NPC]

	@Subscribe
	private def onInteractingChanged(interactingChanged: InteractingChanged): Unit = {
		if (client.getLocalPlayer == interactingChanged.getTarget && IsNpcGorilla.unapply(interactingChanged.getSource)) {
			currentTarget = Option(interactingChanged.getSource.asInstanceOf[NPC])
		}
	}
	@Subscribe(priority = 100)
	private def onGameTick(event: GameTick): Unit = {

		if (atGorillas) {
			gorillas.keys.toList.foreach(gNpc => {
				val previous = gorillas(gNpc)
				val updated = previous.copy(
					prayingAgainstStyle = EthanApiPlugin.getHeadIcon(gNpc) match {
						case HeadIcon.MELEE => AttackStyle.Melee
						case HeadIcon.RANGED => AttackStyle.Ranged
						case HeadIcon.MAGIC => AttackStyle.Magic
					},

					)
				if(updated != previous) {
					gorillas.update(gNpc, updated)
				}
			})
//			pendingAttacks = checkGorillaAttacks(
//				gorillas.values.toList,
//				memorizedPlayers.values.toList,
//				recentBoulders
//				)(using client).prependedAll(pendingAttacks)
//
//			pendingAttacks = checkPendingAttacks(pendingAttacks, memorizedPlayers.values.toList)(using client)
			memorizedPlayers.values.foreach(_.update()) //			updatePlayers
			recentBoulders.clear()
			gorillaProjectiles.filterInPlace(_.getRemainingCycles > 0)

			currentTarget.foreach(gNpc => {
				gorillas.get(gNpc).foreach(gData => {
					log.debug(s"Gorilla[${Integer.toHexString(gNpc.hashCode())}] = ${gData}")
				})
			})
		}
	}

	@Subscribe(priority = 0)
	private def onGameTick2(event: GameTick): Unit = {
		if (atGorillas) {
//			gorillas.foreach(_._2.gameTick())

				gorillas.toList.map{
					case (gNpc, gData@GorillaData(attackStyle, prayingAgainstStyle, attacksUntilSwitch, previousAnimationId, previousProjectileId, takenDamageRecently)) => {
						gNpc -> gData.copy(previousProjectileId = -1, takenDamageRecently = false)
					}
				}
		}
	}

//	private def clearProjectileArray(): Unit = {
//		gorillaProjectiles = gorillaProjectiles.filterNot(p => p.getRemainingCycles <= 0)
//	}

	private def atDemonicGorillas: Boolean = {
		REGION_IDS.contains(client.getLocalPlayer.getWorldLocation.getRegionID())
	}
}
