package com.fredplugins.pvmDebugger.inferno

import com.fredplugins.pvmDebugger.inferno.displaymodes.InfernoPrayerDisplayMode
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.SpriteID
import net.runelite.client.ui.overlay.components.ComponentConstants
import net.runelite.client.ui.overlay.infobox.InfoBox
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority

import java.awt.Color
import java.awt.image.BufferedImage
import java.time.Instant
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.Random
import scala.util.Try

class InfernoJadInfobox(helper: FredsInfernoHelper) extends InfoBox(helper.parent.getItemManager.getImage(ItemID.INFERNOPET_ZUK), helper.parent) {
	setPriority(InfoBoxPriority.HIGH)
	var lastAttackType: InfernoNpcAttack = InfernoNpcAttack.UNKNOWN
//	lazy val prayMeleeSprite: BufferedImage = helper.parent.getSpriteManager.getSprite(, 0)
//	lazy val prayRangeSprite: BufferedImage = helper.parent.getSpriteManager.getSprite(SpriteID.Prayeron.PROTECT_FROM_MISSILES, 0)
//	lazy val prayMagicSprite: BufferedImage = helper.parent.getSpriteManager.getSprite(SpriteID.Prayeron.PROTECT_FROM_MAGIC, 0)

	override def getText: String = {
		Option(helper.closestAttack).getOrElse(InfernoNpcAttack.UNKNOWN).name()
	}
	override def getTextColor: Color = {
		Option(helper.closestAttack).map(_.getPrayer).map(p =>
			if (helper.client.isPrayerActive(p)) ComponentConstants.STANDARD_BACKGROUND_COLOR else new Color(150, 0, 0, 150)
		).getOrElse(Color.GRAY)
	}

	override def cull(): Boolean = {
		false
	}

	override def render(): Boolean = {
		val attackType = Option(helper.closestAttack).getOrElse(InfernoNpcAttack.UNKNOWN)
		if(lastAttackType != attackType) {
			lastAttackType = attackType
			val sid = attackType match {
				case InfernoNpcAttack.RANGED => SpriteID.Prayeron.PROTECT_FROM_MISSILES
				case InfernoNpcAttack.MAGIC => SpriteID.Prayeron.PROTECT_FROM_MAGIC
				case InfernoNpcAttack.MELEE => SpriteID.Prayeron.PROTECT_FROM_MELEE
				case InfernoNpcAttack.UNKNOWN => SpriteID.Prayeron.PROTECT_ITEM
			}
			setImage(helper.parent.getSpriteManager.getSprite(sid, 0))
		}
		helper.config.prayerDisplayMode() != InfernoPrayerDisplayMode.PRAYER_TAB
	}
}
