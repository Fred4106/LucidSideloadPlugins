package com.fredplugins.kroovy

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.detectors.ActionDetector
import com.fredplugins.kroovy.events.{ActionStartedEvent, ActionStoppedEvent, DestinationChanged, Interrupt, ItemSelectionChanged, LocalAnimationChanged, LocalInteractingChanged, LocalPositionChanged, LocalRegionChanged}
import com.google.inject.{Inject, Provides, Singleton}
import lombok.Singular
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

@Singleton
@Inject
class DebugHandler {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	private inline def report[E](inline e: E): Unit = {
		log.debug("{}", e.toString)
	}

	@Subscribe
	def onActionStartedEvent(e: ActionStartedEvent): Unit = {
		report(e)
	}
	@Subscribe
	def onActionStoppedEvent(e: ActionStoppedEvent): Unit = {
		report(e)
	}
	@Subscribe
	def onDestinationChanged(e: DestinationChanged): Unit = {
		report(e)
	}
	@Subscribe
	def onInterrupt(e: Interrupt): Unit = {
		report(e)
	}
	@Subscribe
	def onItemSelectionChanged(e: ItemSelectionChanged): Unit = {
		report(e)
	}
	@Subscribe
	def onLocalAnimationChanged(e: LocalAnimationChanged): Unit = {
		report(e)
	}
	@Subscribe
	def onLocalInteractingChanged(e: LocalInteractingChanged): Unit = {
		report(e)
	}
	@Subscribe
	def onLocalPositionChanged(e: LocalPositionChanged): Unit = {
//		report(e)
	}
	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		report(e)
	}

}
