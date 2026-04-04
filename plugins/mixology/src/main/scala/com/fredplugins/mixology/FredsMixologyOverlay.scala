package com.fredplugins.mixology

import com.google.inject.Inject
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayPosition
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer

import java.awt.Dimension
import java.awt.Graphics2D
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class FredsMixologyOverlay @Inject()(val plugin: FredsMixologyPlugin, val modelOutlineRenderer: ModelOutlineRenderer) extends Overlay(plugin) {
	setPosition(OverlayPosition.DYNAMIC)
	setLayer(OverlayLayer.ABOVE_SCENE)

	override def render(graphics: Graphics2D): Dimension = {
		for ((alchobjEnum, highlightedObject) <- plugin.getHighlightedObjects) {
			modelOutlineRenderer.drawOutline(highlightedObject.`object`, highlightedObject.outlineWidth, highlightedObject.color, highlightedObject.feather)
		}

		null
	}
}
