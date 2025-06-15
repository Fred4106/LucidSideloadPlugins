package com.fredplugins.pvmDebugger

import com.fredplugins.common.utils.ShimUtils
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.TitleComponent
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


sealed abstract class HelperOverlayPanel() extends OverlayPanel() {
	def log: Logger
	var titleColor: Color = Color.CYAN
	def title: String
	//	def backgroundColor: Color
	def elementsOp: () => Seq[LayoutableRenderableEntity]

	override def render(graphics: Graphics2D): Dimension = {
		val toRender: Seq[LayoutableRenderableEntity] = Option(elementsOp())
			.filter(_.nonEmpty)
			.map(_.prepended(TitleComponent.builder.text(title).color(titleColor).build))
			.getOrElse(Seq.empty)
		toRender.foreach(panelComponent.getChildren.add(_))
		super.render(graphics)
	}
}

object HelperOverlayPanel {
	def create(module: HelperModule)(op: => Seq[LayoutableRenderableEntity]): HelperOverlayPanel = {
		val finalName = s"HelperOverlayPanel[${module.configGroup}]"
		new HelperOverlayPanel() {
			setLayer(OverlayLayer.ABOVE_SCENE)
			setPosition(OverlayPosition.BOTTOM_LEFT)
			override val log  : Logger = ShimUtils.getLogger(finalName, "DEBUG")
			override val title: String = s"${module.configGroup}"
			override def getName: String = finalName
			titleColor = Color.CYAN
			override def elementsOp: () => Seq[LayoutableRenderableEntity] = () => {
				op
			}
		}
	}
	//		new PvmHelperOverlayInstance[S](op)
}