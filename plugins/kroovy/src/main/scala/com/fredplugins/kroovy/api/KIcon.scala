package com.fredplugins.kroovy.api

import com.fredplugins.kroovy.KroovyPlugin
import net.runelite.client.game.{ItemManager, SpriteManager}
import net.runelite.client.util.ImageUtil

import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import scala.util.chaining.*
import scala.compiletime.uninitialized
import java.util.concurrent.{TimeUnit, Callable as JCallable, FutureTask as JFutureTask}
import scala.concurrent.duration.MILLISECONDS
import scala.reflect.ClassTag

private trait lowPriorityGivens {
	given Conversion[KIcon, BufferedImage] {
		override def apply(x: KIcon): BufferedImage = x.bufImg
	}
}
case class KIcon(bufImg: BufferedImage) {
	val icon: ImageIcon = new ImageIcon(bufImg)
	lazy val icon_selected: ImageIcon = new ImageIcon(ImageUtil.alphaOffset(bufImg, 0.53f))

	def recolor(color: Color): KIcon = KIcon(ImageUtil.recolorImage(bufImg, color))
	def greyscale(): KIcon = KIcon(ImageUtil.grayscaleImage(bufImg))
	def luminanceScale(percentage: Float): KIcon = KIcon(ImageUtil.luminanceScale(bufImg, percentage))
	def flip(horizontal: Boolean, vertical: Boolean): KIcon = KIcon(ImageUtil.flipImage(bufImg, horizontal, vertical))
}

object KIcon  extends lowPriorityGivens {
	def fromSprite(spriteId: Int, fileO: Int = 0)(using spriteManager: SpriteManager): KIcon = {
		KIcon(spriteManager.getSprite(spriteId, fileO))
	}

	def fromItem(itemId: Int, count: Int, stackable: Boolean = false)(using itemManager: ItemManager): KIcon = {
		JFutureTask[KIcon](() => {
			var flag: Boolean = false
			val icon = KIcon(itemManager.getImage(itemId, count, stackable).tap(_.onLoaded(() => flag = true)))
			while(!flag) {Thread.sleep(10)}
			icon
		}).get(1000, MILLISECONDS)
	}

	def fromResource[E: ClassTag as ct](name: String): KIcon = KIcon(ImageUtil.loadImageResource(ct.runtimeClass, s"${name}.png"))
	def fromKroovy(name: String): KIcon = fromResource[KroovyPlugin](name)
}