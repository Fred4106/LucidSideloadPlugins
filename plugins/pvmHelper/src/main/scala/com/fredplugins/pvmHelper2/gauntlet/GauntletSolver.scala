package com.fredplugins.pvmHelper2.gauntlet

import com.fredplugins.common.utils.{ShimUtils, WorldPointUtils}
import com.fredplugins.pvmHelper2.{ClientEvent, NpcEvent, PlayerEvent, PvmEvent, PvmHelperOverlay, TypeName, gauntlet}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Actor, Client, NPC, Perspective, Player, Prayer}
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger

import scala.util.chaining.*
import net.runelite.api.events.{ActorDeath, AnimationChanged, InteractingChanged, NpcDespawned, NpcSpawned, VarbitChanged}
import net.runelite.client.ui.overlay.{OverlayManager, OverlayUtil}

sealed trait HunllefEvent extends scala.swing.event.Event {}

case class TornadoAttack(source: NPC) extends HunllefEvent {}
case class RegularAttack(source: NPC) extends HunllefEvent {}
case class SwitchToRange(source: NPC) extends HunllefEvent {}
case class SwitchToMage(source: NPC) extends HunllefEvent {}

object HunllefAnimation {
	def unapply(event: NpcEvent): Option[HunllefEvent] = {
		Option(event).collect {
			case e@NpcEvent.AnimationChanged(Hunllef(), o, 8418) => TornadoAttack(e.source)
			case e@NpcEvent.AnimationChanged(Hunllef(), o, 8419) => RegularAttack(e.source)
			case e@NpcEvent.AnimationChanged(Hunllef(), o, 8754) => SwitchToMage(e.source)
			case e@NpcEvent.AnimationChanged(Hunllef(), o, 8755) => SwitchToRange(e.source)
		}
//			case event: NpcEvent.NpcFragEvent => ???
//			case NpcEvent.Spawned(source, record) => ???
//			case NpcEvent.Despawned(source, record) => ???
	}
}

sealed trait GauntletEvent extends scala.swing.event.Event {}
case class EnablePrayer(prayer: Prayer) extends GauntletEvent {}
case class DisablePrayer(prayer: Prayer) extends GauntletEvent {}
case class EquipWeapon(weaponID: Int) extends GauntletEvent {}
case class EatPaddleFish() extends GauntletEvent {}
case class DrinkPotion() extends GauntletEvent {}
object GauntletEvent {
	def unapply(event: PvmEvent)(using client: Client): Option[GauntletEvent] = {
		Option(event).collect {
			case NpcEvent.InteractingChanged(src, o, LocalPlayer(c)) => {
				Option(src).collect {
					case Bear() => Some(EnablePrayer(Prayer.PROTECT_FROM_MELEE))
					case DarkBeast() => Some(EnablePrayer(Prayer.PROTECT_FROM_MISSILES))
					case Dragon() => Some(EnablePrayer(Prayer.PROTECT_FROM_MAGIC))
					case _ => None
				}.flatten
			}
			case NpcEvent.InteractingChanged(src, LocalPlayer(o), c) =>{
				Option(src).collect {
					case Bear() => Some(DisablePrayer(Prayer.PROTECT_FROM_MELEE))
					case DarkBeast() => Some(DisablePrayer(Prayer.PROTECT_FROM_MISSILES))
					case Dragon() => Some(DisablePrayer(Prayer.PROTECT_FROM_MAGIC))
					case _ => None
				}.flatten
			}
		}.flatten
//			case PlayerEvent.InteractingChanged(LocalPlayer(src), o, c)  if(o != c) => {
//				c match {
//					case Some(value@Hunllef()) =>
//					case Spome(value@Bear()) => value
//					case Some(value@DarkBeast()) => value
//					case Some(value@Dragon()) => value
//					case None => ???
//				}
//				Option(c).collect {
//					case 8418 => TornadoAttack(src)
//					case 8419 => RegularAttack(src)
//					case 8754 => SwitchToMage(src)
//					case 8755 => SwitchToRange(src)
//				}
//			}
		}
		//			case event: NpcEvent.NpcFragEvent => ???
		//			case NpcEvent.Spawned(source, record) => ???
		//			case NpcEvent.Despawned(source, record) => ???
//	}
}

object LocalPlayer {
	def unapply(a: Actor)(using client: Client): Option[Player] = {
//		val local =
		a match {
			case player: Player if(player.equals(client.getLocalPlayer)) => Some(player)
			case _ => None
		}
	}
	def unapply(a: Option[Actor])(using client: Client): Option[Player] = {
		//		val local =
		a match {
			case Some(player: Player) if (player.equals(client.getLocalPlayer)) => Some(player)
			case _ => None
		}
	}
}

@Singleton
class GauntletSolver @Inject()(val client: Client, val clientThread: ClientThread, val overlayManager: OverlayManager) extends scala.swing.Publisher {
	private val log: Logger = ShimUtils.getLogger(classOf[GauntletSolver].getName, "DEBUG")
	var inHunllef: Boolean = false
	var inGauntlet: Boolean = false
	given Client = client

	reactions += {
		case HunllefAnimation(a) => publish(a)
		case GauntletEvent(a) => publish(a)
	}
//	reactions += {
//		case x@NpcEvent.InteractingChanged(Dragon(), o, LocalPlayer(lp)) if inGauntlet && client.getLocalPlayer == lp => publish(GauntletEnablePrayer(Prayer.PROTECT_FROM_MAGIC))
//		case x@NpcEvent.InteractingChanged(DarkBeast(), o, Some(lp)) if inGauntlet && client.getLocalPlayer == lp => publish(GauntletEnablePrayer(Prayer.PROTECT_FROM_MISSILES))
//		case x@NpcEvent.InteractingChanged(Bear(), o, Some(lp))  if inGauntlet && client.getLocalPlayer == lp => publish(GauntletEnablePrayer(Prayer.PROTECT_FROM_MELEE))
//	}
	reactions += {
		case v@ClientEvent.VarbitChanged(9178, 1) if !inGauntlet => {inGauntlet = true; log.debug("inGauntlet={}", inGauntlet)}
		case v@ClientEvent.VarbitChanged(9178, 0) if inGauntlet => {inGauntlet = false; log.debug("inGauntlet={}", inGauntlet)}
		case v@ClientEvent.VarbitChanged(9177, 1) if !inHunllef  => {inHunllef = true; log.debug("inHunllef={}", inHunllef)}
		case v@ClientEvent.VarbitChanged(9177, 0) if inHunllef => {inHunllef = false; log.debug("inHunllef={}", inHunllef)}
	}
//	reactions += new PartialFunction[GauntletEvent, Unit] {
//		override def isDefinedAt(x: GauntletEvent): Boolean =
//		override def apply(v1: GauntletEvent): Unit = ???
//	}
	reactions += {
		case he: HunllefEvent => log.debug("HunllefEvent={}", he)
		case ge: GauntletEvent => log.debug("GauntletEvent={}", ge)
		case pvm: PvmEvent => log.debug("PvmEvent={}",pvm)
	}
}
