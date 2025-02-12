package com.fredplugins.pvmHelper2

import com.fredplugins.common.utils.ShimUtils
import net.runelite.client.ui.overlay.{Overlay, OverlayLayer, OverlayPosition, RenderableEntity}
import org.slf4j.Logger

import java.awt.{Dimension, Graphics2D}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.quoted.{Expr, Type}
//import scala.quoted.runtime.Expr
import scala.reflect.{TypeTest, Typeable}


sealed abstract class PvmHelperOverlay() extends Overlay() {
	def log: Logger// = ShimUtils.getLogger(finalName, "DEBUG")
}

object PvmHelperOverlay {
//	abstract class PvmHelperOverlay(inline val ownerName: String) extends Overlay with PvmHelperOverlay {}

//	transparent class PvmHelperOverlayInstance[S <: String & Singleton: ValueOf](renderOp: Graphics2D => Dimension) extends Overlay() with PvmHelperOverlay {
//		inline def finalName = s"com.fredplugins.pvmHelper2.PvmHelperOverlay[${valueOf[S]}]"
//		override val log: Logger = ShimUtils.getLogger( finalName, "DEBUG")
//		override def getName: String = finalName
//		override def render(graphics: Graphics2D): Dimension = renderOp(graphics)
//2	}

	inline def create[S <: String & Singleton : ValueOf](inline s: S)(op: Graphics2D => Dimension): PvmHelperOverlay = {
		//		type MO = Overlay & PvmHelperOverlay
		val finalName = s"com.fredplugins.pvmHelper2.PvmHelperOverlay[${valueOf[S]}]"
		new PvmHelperOverlay() {
			override val log: Logger = ShimUtils.getLogger(finalName, "DEBUG")
			override def getName: String = finalName
			override def render(graphics: Graphics2D): Dimension = op(graphics)
		}
	}
	//		new PvmHelperOverlayInstance[S](op)
}