package com.fredplugins.pvmHelper2

import com.fredplugins.common.utils.WorldPointUtils
import com.fredplugins.pvmHelper2.gauntlet.Hunllef
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.{Actor, Client, NPC, Player}
import net.runelite.client.callback.ClientThread

import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

transparent trait DeltaEvent {
	type T
	def cur: T
	def old: T
}
sealed trait PvmEvent extends scala.swing.event.Event {
	type A
	def source: A
}

transparent trait ActorEvent {
	self: PvmEvent =>
//	override def source: A
}
sealed trait NpcEvent extends PvmEvent with ActorEvent {
	type A = NPC
}
object NpcEvent {
	trait NpcFragEvent extends NpcEvent with DeltaEvent {}

	case class InteractingChanged(source: NPC, old: Option[Actor], cur: Option[Actor]) extends NpcFragEvent {
		type T = Option[Actor]
	}

	case class DeadChanged(source: NPC, old: Boolean, cur: Boolean) extends NpcFragEvent  {
		type T = Boolean
	}
	case class AnimationChanged(source: NPC, old: Int, cur: Int) extends NpcFragEvent {
		type T = Int
	}
	case class LocationChanged(source: NPC, old: (Int, Int, Int), cur: (Int, Int, Int)) extends NpcFragEvent {
		type T = (Int, Int, Int)
		def delta: (Int, Int) = (cur._1 - old._1, cur._2 - old._2)
	}
	case class NpcRecord(animationID: Int, interactingWith: Option[Actor], location: (Int, Int, Int), dead: Boolean) {}

	object NpcRecord {
		def delta(source: NPC)(old: NpcRecord, cur: NpcRecord): Seq[NpcFragEvent] = {
//			println(s"delta(${source})(${old}) ${cur})")
			val l1 = List(
				AnimationChanged(source, old.animationID, cur.animationID),
				InteractingChanged(source, old.interactingWith, cur.interactingWith),
				DeadChanged(source, old.dead, cur.dead),
				LocationChanged(source, old.location, cur.location)
			).filter {
				case LocationChanged(source, old, cur) => old != (-1, -1, -1) && cur != (-1, -1, -1) && old._3 == cur._3 && cur != old
				case p => p.old != p.cur
			}
//			val l2 = (old.location, cur.location) match {
//				case ((-1, -1, -1), (_, _, _)) => None
//				case ((_, _, _), (-1, -1, -1)) => None
//				case ((o1, o2, o3), (c1, c2, c3)) => Option.when(o3 == c3 && (o1 != c1 || o2 != c2))(LocationChanged.apply(source, (o1, o2, o3), (c1, c2, c3)))
//			}
//			val l3 = l1 :++ l2
//			l3.filter(d => d.old != d.cur)
			l1
		}
		def apply(source: NPC)(using client: Client): NpcRecord = {
			NpcRecord(
				source.getAnimation,
				Option(source.getInteracting),
				Try(WorldPointUtils.fromInstance(source.getWorldLocation)).toOption.map(wp => (wp.getX, wp.getY, wp.getPlane)).getOrElse((-1, -1, -1)),
				source.isDead
			)
		}
	}
	case class Spawned(source: NPC, record: NpcRecord) extends NpcEvent
	case class Despawned(source: NPC, record: NpcRecord) extends NpcEvent

	def unapply(npcEvent: NpcEvent): Option[NPC] = {
		Option(npcEvent).map(_.source)
	}
}
sealed trait PlayerEvent extends PvmEvent with ActorEvent {
	override type A = Player
}
object PlayerEvent {
	trait PlayerFragEvent extends PlayerEvent with DeltaEvent {}

	case class InteractingChanged(source: Player, old: Option[Actor], cur: Option[Actor]) extends PlayerFragEvent {
		type T = Option[Actor]
	}

	case class DeadChanged(source: Player, old: Boolean, cur: Boolean) extends PlayerFragEvent {
		type T = Boolean
	}
	case class AnimationChanged(source: Player, old: Int, cur: Int) extends PlayerFragEvent {
		type T = Int
	}
	case class LocationChanged(source: Player, old: (Int, Int, Int), cur: (Int, Int, Int)) extends PlayerFragEvent {
		type T = (Int, Int, Int)
		def delta: (Int, Int) = (cur._1 - old._1, cur._2 - old._2)
	}

	case class PlayerRecord(animationID: Int, interactingWith: Option[Actor], location: (Int, Int, Int), dead: Boolean){}
	object PlayerRecord {
		def delta(source: Player)(old: PlayerRecord, cur: PlayerRecord): Seq[PlayerFragEvent] = {
			//			println(s"delta(${source})(${old}) ${cur})")
			val l1 = List(
				AnimationChanged(source, old.animationID, cur.animationID),
				InteractingChanged(source, old.interactingWith, cur.interactingWith),
				DeadChanged(source, old.dead, cur.dead),
				LocationChanged(source, old.location, cur.location)
			).filter {
				case LocationChanged(source, old, cur) => old != (-1, -1, -1) && cur != (-1, -1, -1) && old._3 == cur._3 && cur != old
				case p => p.old != p.cur
			}
			//			val l2 = (old.location, cur.location) match {
			//				case ((-1, -1, -1), (_, _, _)) => None
			//				case ((_, _, _), (-1, -1, -1)) => None
			//				case ((o1, o2, o3), (c1, c2, c3)) => Option.when(o3 == c3 && (o1 != c1 || o2 != c2))(LocationChanged.apply(source, (o1, o2, o3), (c1, c2, c3)))
			//			}
			//			val l3 = l1 :++ l2
			//			l3.filter(d => d.old != d.cur)
			l1
		}
		def apply(source: Player)(using client: Client): PlayerRecord = {
			PlayerRecord(
				source.getAnimation,
				Option(source.getInteracting),
				Try(WorldPointUtils.fromInstance(source.getWorldLocation)).toOption.map(wp => (wp.getX, wp.getY, wp.getPlane)).getOrElse((-1, -1, -1)),
				source.isDead
			)
		}
	}
	case class Spawned(source: Player, record: PlayerRecord) extends PlayerEvent
	case class Despawned(source: Player, record: PlayerRecord) extends PlayerEvent
}
sealed trait ClientEvent extends PvmEvent {}
object ClientEvent {
	class VarbitChanged(val source: Int, val old: Int, val cur: Int) extends ClientEvent with DeltaEvent {
		type A = Int
		type T = Int
	}
	object VarbitChanged {
		def unapply(changed: VarbitChanged): Option[(Int, Int)] = changed match {
			case c: VarbitChanged => Some(c.source -> c.cur)
		}
	}
//	sealed class GameTick (val source: Int) extends ClientEvent {}
//	object VarbitChanged {
//		def apply(id: Int, last: Int)(using client: Client, clientThread: ClientThread): VarbitChanged = {
//			val cur = clientThread.runOnClientThread(() => {client.getVarbitValue(id)})
//			new VarbitChanged(id)(last, cur)
//		}
//		def unapply(vb: VarbitChanged): Option[(Int, Int)] ={
//			if(vb.source != -1 && vb.old != vb.cur) Some(vb.source -> vb.cur)
//			else None
//		}
//	}

//	object GameTick {
//		def apply()(using client: Client, clientThread: ClientThread): GameTick = new GameTick(clientThread.runOnClientThread(() => client.getTickCount))
//		def unapply(gt: GameTick): Option[Int] = Option(gt).map(_.source)
//	}
}

