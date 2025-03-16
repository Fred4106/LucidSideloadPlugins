package com.fredplugins.kroovy.ui

import net.runelite.api.{ItemID, SpriteID}
import net.runelite.client.callback.ClientThread
import net.runelite.client.game.{ItemManager, SpriteManager}

import java.awt.image.BufferedImage
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait IconSource {}
case class SpriteIconSource(spriteId: Int, fileId: Int = 0) extends IconSource
case class ItemIconSource(itemId: Int, qty: Int = 1, stackable: Boolean = true) extends IconSource
object IconSource {
	val SPRITE_TOTAL: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_TOTAL, 0)
	val SPRITE_CRAFTING: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_CRAFTING, 0)
	val SPRITE_FISHING: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_FISHING, 0)
	val SPRITE_WOODCUTTING: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_WOODCUTTING, 0)
	val SPRITE_COOKING: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_COOKING, 0)
	val SPRITE_COOKING_FISH: ItemIconSource = ItemIconSource(ItemID.RAINBOW_FISH)
	val SPRITE_FLETCHING: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_FLETCHING, 0)
	val SPRITE_FIREMAKING: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_FIREMAKING, 0)
	val SPRITE_SMITHING: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_SMITHING, 0)
	val SPRITE_MAGIC: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_MAGIC, 0)
	val SPRITE_HERBLORE: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_HERBLORE, 0)
	val SPRITE_GUARDIAN_OF_THE_RIFT_REWARD: ItemIconSource = ItemIconSource(ItemID.ABYSSAL_PROTECTOR)
	val SPRITE_FARMING: SpriteIconSource = SpriteIconSource(SpriteID.SKILL_FARMING, 0)
	val SPRITE_BUCKET: ItemIconSource = ItemIconSource(ItemID.BUCKET)
	val SPRITE_SMELT_GOLD: ItemIconSource = ItemIconSource(ItemID.GOLD_BAR)
	val SPRITE_SMELT_SILVER: ItemIconSource = ItemIconSource(ItemID.SILVER_BAR)

	def toBufferedImage(iconSource: IconSource)(using clientThread: ClientThread, spriteManager: SpriteManager, itemManager: ItemManager): BufferedImage = {
		clientThread.runOnClientThread(() =>
			iconSource match {
				case SpriteIconSource(spriteId, fileId) => spriteManager.getSprite(spriteId, fileId)
				case ItemIconSource(itemId, itemQty, stackable) => itemManager.getImage(itemId,itemQty,stackable)
			}
		)
	}
}
