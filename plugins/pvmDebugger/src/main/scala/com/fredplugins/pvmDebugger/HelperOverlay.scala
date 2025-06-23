package com.fredplugins.pvmDebugger

import com.fredplugins.common.utils.ShimUtils
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, RenderableEntity}
import org.slf4j.Logger

import java.awt.{BasicStroke, Color, Dimension, Graphics2D, Shape}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.quoted.{Expr, Type}
//import scala.quoted.runtime.Expr
import scala.reflect.{TypeTest, Typeable}

sealed abstract class HelperOverlay() extends Overlay() {
	def log: Logger// = ShimUtils.getLogger(finalName, "DEBUG")
	setPosition(OverlayPosition.DYNAMIC)
}

object HelperOverlay {
	inline def drawOutlineAndFill(outlineColor: Color, fillColor: Color, strokeWidth: Float, shape: Shape)(g: Graphics2D): Unit = {
		val originalColor = g.getColor
		val originalStroke = g.getStroke

		g.setStroke(new BasicStroke(strokeWidth));
		g.setColor(outlineColor);
		g.draw(shape);

		g.setColor(fillColor);
		g.fill(shape);

		g.setColor(originalColor);
		g.setStroke(originalStroke);
	}
	def create(module: HelperModule)(op: Graphics2D => Dimension): HelperOverlay = {
		//		type MO = Overlay & PvmHelperOverlay
		val finalName = s"HelperOverlay[${module.configGroup}]"
		new HelperOverlay() {
			override val log: Logger = ShimUtils.getLogger(finalName, "DEBUG")
			override def getName: String = finalName
			override def render(graphics: Graphics2D): Dimension = op(graphics)
		}
	}
	//		new PvmHelperOverlayInstance[S](op)
}