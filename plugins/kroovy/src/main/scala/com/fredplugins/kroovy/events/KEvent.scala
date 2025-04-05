package com.fredplugins.kroovy.events

import net.runelite.api.coords.WorldPoint
import net.runelite.api.{NPC as RlNpc, Player as RlPlayer}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

//sealed trait KEvent {
//}
case class KNpc(wrapped: RlNpc) {
	def id: Int = wrapped.getId
	def index: Int = wrapped.getIndex
	override def toString: String = {
		s"KNpc[$index](id=$id, wrapped=${Integer.toHexString(wrapped.hashCode())})"
	}
}
case class KPlayer(wrapped: RlPlayer) {
	def id: Int = wrapped.getId
	override def toString: String = {
		s"KPlayer[$id](wrapped=${Integer.toHexString(wrapped.hashCode())})"
	}
}
enum KEvent {
	case KNpcSpawned(wrapped: KNpc) extends KEvent
	case KNpcDespawned(wrapped: KNpc) extends KEvent
	case KNpcAnimationChanged(wrapped: KNpc, old: Int, cur: Int) extends KEvent
	case KNpcChanged(wrapped: KNpc, old: Int, cur: Int) extends KEvent
	case KNpcDied(wrapped: KNpc) extends KEvent
	case KNpcMoved(wrapped: KNpc, delta: (Int, Int), cur: WorldPoint) extends KEvent
	case KPlayerSpawned(wrapped: KPlayer) extends KEvent
	case KPlayerDespawned(wrapped: KPlayer) extends KEvent
	case KPlayerAnimationChanged(wrapped: KPlayer, old: Int, cur: Int) extends KEvent
	case KPlayerDied(wrapped: KPlayer) extends KEvent
	case KPlayerMoved(wrapped: KPlayer, delta: (Int, Int), cur: WorldPoint) extends KEvent
	case KLocalPlayerAnimationChanged(old: Int, cur: Int) extends KEvent
	case KLocalPlayerDied extends KEvent
	case KLocalPlayerMoved(delta: (Int, Int), cur: WorldPoint) extends KEvent
//	case PlayerSpawned(wrapped: KPlayer) extends KEvent

//	inline def transform(inline source: rlEvents.PlayerSpawned | rlEvents.PlayerDespawned | rlEvents.PlayerChanged): KEvent = {
//		inline source match {
//			case e: rlEvents.AnimationChanged => {
//				e.getActor match {
//					case npc: RlNpc => KNpcAnimationChanged
//					case player: RlPlayer => ???
//				}
//			}
//			case e: rlEvents.PlayerSpawned => KPlayerSpawned(new KPlayer(e.getPlayer))
////			case e: rlEvents.PlayerChanged => e.
//			case e: rlEvents.PlayerDespawned =>
//			case e: rlEvents.NpcSpawned =>
//			case e: rlEvents.NpcDespawned =>
//			case e: rlEvents.NpcChanged =>
//		}
//	}
}