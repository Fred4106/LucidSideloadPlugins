package com.fredplugins.pvmDebugger

import com.fredplugins.common.utils.{TWorldPoint, WorldPointUtils}
import net.runelite.api.{Client, GameState, NPCComposition}
import net.runelite.api.coords.WorldPoint

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.swing.event.Event

transparent trait SEvent extends Event with Product {}

sealed trait DebugEvent extends SEvent {}

sealed trait SInvSlot {
	this: DebugEvent=>
	def index: Int
	def qty: Int
	def itemId: Int
}

case class SInvQtyChanged(index: Int, itemId: Int, qty: Int, delta: Int) extends DebugEvent with SInvSlot {}
case class SInvAdded(index: Int, itemId: Int, qty: Int)  extends DebugEvent with SInvSlot {}
case class SInvRemoved(index: Int, itemId: Int, qty: Int)  extends  DebugEvent with SInvSlot {}

case class SLocation(x: Int, y: Int, plane: Int)

object SLocation {
	def apply(worldPoint: WorldPoint): SLocation = {
		assert(worldPoint != null)
		SLocation(worldPoint.getX, worldPoint.getY, worldPoint.getPlane)
	}
}

case class SGameTick(tick: Int) extends DebugEvent {}
case class SNpcChanged(idx: Int, old: NPCComposition, current: NPCComposition) extends DebugEvent {}
case class SNpcSpawned(idx: Int, id: Int, location: SLocation) extends DebugEvent {}
case class SNpcDespawned(idx: Int, id: Int, location: SLocation) extends DebugEvent {}
case class SNpcAnimationChanged(idx: Int, id: Int, animationId:  Int, location: SLocation) extends DebugEvent {}
case class SGameStateChanged(previous: GameState, state: GameState) extends DebugEvent {}