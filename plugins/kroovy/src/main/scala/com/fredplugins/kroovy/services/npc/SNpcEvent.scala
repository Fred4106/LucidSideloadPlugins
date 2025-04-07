package com.fredplugins.kroovy.services.npc

import net.runelite.api.coords.WorldPoint
import net.runelite.api.{Client, NPC, NPCComposition}
import net.runelite.client.callback.ClientThread

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.quoted.Type
import scala.reflect.ClassTag

sealed trait SNpcEvent {
	def npc: NPC
	private val eventName: String = this.getClass.getName.stripPrefix("com.fredplugins.kroovy.services.npc.").replace('$', '.')
	protected def toStringSuffix: String = ""
	override def toString: String = s"${eventName}[id=${npc.getId}, index=${npc.getIndex}][${Integer.toHexString(npc.hashCode())}]${toStringSuffix}"
}
object SNpcEvent {
	class Spawned(val npc: NPC) extends SNpcEvent
	class Despawned(val npc: NPC) extends SNpcEvent
	class Died(val npc: NPC) extends SNpcEvent

	class AnimationChanged(val npc: NPC, val old: Int)(using clientThread: ClientThread) extends SNpcEvent {
		val cur: Int = clientThread.runOnClientThread(() => npc.getAnimation)
		override protected def toStringSuffix: String = s"(old = $old, cur = $cur)"
	}
	class CompositionChanged(val npc: NPC, val old: Int)(using clientThread: ClientThread) extends SNpcEvent {
		val cur: Int = clientThread.runOnClientThread(() => npc.getComposition.getId)
		override protected def toStringSuffix: String = s"(old = $old, cur = $cur)"
//		override def toString: String = s"${eventName}[${Integer.toHexString(npc.hashCode())}]"
	}

	class Moved(val npc: NPC, val old: WorldPoint)(using clientThread: ClientThread) extends SNpcEvent {
		val cur: WorldPoint = clientThread.runOnClientThread(() => npc.getWorldLocation)
		val delta: (Int, Int) = (cur.getX - old.getX) -> (cur.getY - old.getY)
		override val toStringSuffix: String = s"(delta = $delta)(old = $old, cur = $cur)"
//		override def toString: String = s"${eventName}[${Integer.toHexString(npc.hashCode())}](delta = $delta)(old = $old, cur = $cur)"
	}
}
