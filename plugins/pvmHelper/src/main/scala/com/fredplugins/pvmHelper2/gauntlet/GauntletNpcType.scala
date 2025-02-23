package com.fredplugins.pvmHelper2.gauntlet

import net.runelite.api.coords.WorldPoint
import net.runelite.api.{Actor, NPC, NpcID, NullNpcID}

import java.awt.Color
import scala.util.chaining.*

sealed trait GauntletNpcInstance {
	val wrapped: NPC
	val tpe: GauntletNpcType
//	export wrapped.{getWorldLocation, getIndex, getAnimation}
	def worldLocation: WorldPoint = wrapped.getWorldLocation
	def index: Int = wrapped.getIndex
	def animation: Int = wrapped.getAnimation
	def name: String = wrapped.getName

	override def toString: String = s"${tpe.name}(${index}, ${worldLocation})(${wrapped.getHash})"
}
sealed abstract class GauntletNpcType(val ids: Int *) {
	self: Product =>
	def name: String = this.productPrefix

	sealed class Instance(override val wrapped: NPC) extends GauntletNpcInstance {
		override val tpe: GauntletNpcType = GauntletNpcType.this
	}

	def unapply(in: NPC): Boolean = {
		ids.contains(in.getId)
	}
	object Instance {
		def unapply(in: NPC): Option[Instance] = Option.when(ids.contains(in.getId))(new Instance(in))
	}

//	def unapply(in: NPC): Option[Instance] = {
//		Option.when(ids.contains(in.getId))(new Instance(in))
//	}
}

sealed trait WeakType {
	self: GauntletNpcType =>
}

sealed trait StrongType {
	self: GauntletNpcType => }

sealed trait DemiType {
	self: GauntletNpcType =>
}

case object Bat extends GauntletNpcType(NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT) with WeakType {}
case object Rat extends GauntletNpcType(NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT) with WeakType {}
case object Spider extends GauntletNpcType(NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER) with WeakType {}
case object Scorpion extends GauntletNpcType(NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION) with StrongType {}
case object Unicorn extends GauntletNpcType(NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN) with StrongType {}
case object Wolf extends GauntletNpcType(NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF) with StrongType {}
case object Bear extends GauntletNpcType(NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR) with DemiType {}
case object DarkBeast extends GauntletNpcType(NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST) with DemiType {}
case object Dragon extends GauntletNpcType(NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON) with DemiType {}
case object Hunllef extends GauntletNpcType(NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022, NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024, NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036, NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038) {}
case object Tornado extends GauntletNpcType(NullNpcID.NULL_9025, NullNpcID.NULL_9039, NullNpcID.NULL_14142) {}
object GauntletNpcType {
	def color(npc: NPC): Color = {
		npc match {
			case Bat() => Color.YELLOW
			case Rat() => Color.YELLOW
			case Spider() => Color.YELLOW
			case Scorpion() => Color.ORANGE
			case Unicorn() => Color.ORANGE
			case Wolf() => Color.ORANGE
			case Bear() => Color.RED
			case Dragon() => Color.BLUE
			case DarkBeast() => Color.GREEN
			case Hunllef() => Color.WHITE
			case Tornado() => Color.BLUE
			case _ => Color.PINK
		}
	}
	def unapply(npc: NPC): Boolean = {
		color(npc) != Color.PINK
	}
	//	private val values: Seq[GauntletNpcType] = Seq(Bat, Rat, Spider, Scorpion, Unicorn, Wolf, Bear, DarkBeast, Dragon, Hunllef, Tornado)
//	def unapply(npc: Actor): Option[GauntletNpcType#Instance] = {
//		Option(npc).collect{
//			case n: NPC => values.flatMap(_.unapply(n)).headOption
//		}.flatten
////		values.flatMap(_.unapply(npc)).headOption
//	}
}
