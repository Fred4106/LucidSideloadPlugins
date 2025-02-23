package com.fredplugins.pvmHelper2

import net.runelite.api.Client
import net.runelite.client.callback.ClientThread
import net.runelite.client.ui.overlay.OverlayManager

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.swing.event.Event

trait PvmModule extends scala.swing.Reactor {
	def client: Client
	def clientThread: ClientThread
	def overlayManager: OverlayManager

	def onStart(): Unit
	def onStop(): Unit
}
object PvmModule {
	def conditionedPartialFunction[B](condition: => Boolean)(func: PartialFunction[PvmEvent, B]): PartialFunction[Event, B] = {
		new PartialFunction[Event, B] {
			override def isDefinedAt(x: Event): Boolean = {
				x match {
					case pvmEvent: PvmEvent if (condition) => func.isDefinedAt(pvmEvent)
					case _ => false
				}
			}
			override def apply(x: Event): B = {
				x match {
					case pvmEvent: PvmEvent => func.apply(pvmEvent)
				}
			}
		}
	}
	extension (pvmModule: PvmModule)(using publisher: FredsPvmHelper2) {
		def enable(): Unit = {
			pvmModule.onStart()
			pvmModule.listenTo(publisher)
		}
		def disable(): Unit = {
			pvmModule.deafTo(publisher)
			pvmModule.onStop()
		}
	}
}
