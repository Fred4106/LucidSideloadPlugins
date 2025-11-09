package com.fredplugins.pvmDebugger.inferno

import net.runelite.api.gameval.ItemID
import net.runelite.client.ui.overlay.infobox.InfoBox
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority

import java.awt.Color
import java.time.Instant
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class InfernoSpawnTimerInfobox(helper: FredsInfernoHelper) extends InfoBox(helper.parent.getItemManager.getImage(ItemID.INFERNOPET_ZUK), helper.parent) {
	setPriority(InfoBoxPriority.HIGH)

	private var running: Boolean = false
	private var timeRemaining: Long = InfernoData.SPAWN_DURATION
	private var startTime: Long = Instant.now().getEpochSecond()

	def run(): Unit = {
		startTime = Instant.now().getEpochSecond()
		running = true
	}

	def reset(): Unit = {
		running = false
		timeRemaining = InfernoData.SPAWN_DURATION
	}

	def pause(): Unit = {
		if(running) {
			running = false
			val timeElapsed = Instant.now().getEpochSecond() - startTime
			timeRemaining = Math.max(0, timeRemaining - timeElapsed) + InfernoData.SPAWN_DURATION_INCREMENT
		}
	}

	def seconds: Long = if(running) Math.max(0, timeRemaining - (Instant.now().getEpochSecond() - startTime)) else timeRemaining

	override def getText: String = {
		val (min, secs) = seconds.pipe(s => {
			((s % 3600 / 60), s % 60)
		})

		String.format("%02d:%02d", min, secs)
	}
	override def getTextColor: Color = {
		seconds match {
			case s if s <= InfernoData.SPAWN_DURATION_DANGER => Color.RED
			case s if s <= InfernoData.SPAWN_DURATION_WARNING => Color.ORANGE
			case s => Color.GREEN
		}
	}

	override def cull(): Boolean = {
		false
	}

	override def render(): Boolean = {
		helper.isInInferno
	}
}
