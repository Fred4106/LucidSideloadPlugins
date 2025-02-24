package com.fredplugins.pvmHelper2

import com.fredplugins.common.utils.ShimUtils
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, TitleComponent}
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPanel, OverlayPosition}
import org.slf4j.Logger

import java.awt.*
//import scala.quoted.runtime.Expr


sealed abstract class PvmHelperPanel() extends OverlayPanel() {
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

object PvmHelperPanel {
	inline def create[S <: String & Singleton : ValueOf](inline s: S)(op: => Seq[LayoutableRenderableEntity]): PvmHelperPanel = {
		val finalName = s"com.fredplugins.pvmHelper2.PvmHelperPanel[${valueOf[S]}]"
		new PvmHelperPanel() {
			setLayer(OverlayLayer.ABOVE_SCENE)
			setPosition(OverlayPosition.BOTTOM_LEFT)
			override val log: Logger = ShimUtils.getLogger(finalName, "DEBUG")
			override val title: String = s"${valueOf[S]}"
			override def getName: String = finalName
			titleColor = Color.CYAN
			override def elementsOp: () => Seq[LayoutableRenderableEntity] = () => {
				op
			}
		}
	}
	//		new PvmHelperOverlayInstance[S](op)
}