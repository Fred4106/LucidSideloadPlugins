package com.fredplugins.tempoross

import net.runelite.api.{ItemID, NullObjectID, ObjectID}
import net.runelite.client.util.ImageUtil

import java.awt.image.BufferedImage

object Constants {
	val WAVE_INCOMING_MESSAGE        = "a colossal wave closes in..."
	val WAVE_END_SAFE                = "as the wave washes over you"
	val WAVE_END_DANGEROUS           = "the wave slams into you"
	val TEMPOROSS_VULNERABLE_MESSAGE = "tempoross is vulnerable"

	val VARB_IS_TETHERED        = 11895
	val VARB_REWARD_POOL_NUMBER = 11936

	val TEMPOROSS_REGION         = 12078
	val UNKAH_REWARD_POOL_REGION = 12588
	val UNKAH_BOAT_REGION        = 12332

	val DAMAGE_PER_UNCOOKED = 10
	val DAMAGE_PER_COOKED   = 15
	val DAMAGE_PER_CRYSTAL  = 10

	val REWARD_POOL_IMAGE_ID: Int = ItemID.TOME_OF_WATER
	val DAMAGE_IMAGE_ID     : Int = ItemID.DRAGON_HARPOON
	val FISH_IMAGE_ID       : Int = ItemID.HARPOONFISH

	val NET_IMAGE_ID: Int           = ItemID.TINY_NET
	val PHASE_IMAGE : BufferedImage = ImageUtil.loadImageResource(classOf[FredsTemporossPlugin], "phases.png")


	val FIRE_SPREAD_MILLIS          = 24000
	val FIRE_SPAWN_MILLIS           = 9600
	val FIRE_SPREADING_SPAWN_MILLIS = 1200
	val WAVE_IMPACT_MILLIS          = 7800
	val TEMPOROSS_HUD_UPDATE = 4075
	val STORM_INTENSITY      = 350
	val MAX_STORM_INTENSITY  = 350

	val FIRE_GAMEOBJECTS: Set[Int] = Set(NullObjectID.NULL_37582, NullObjectID.NULL_41006, NullObjectID.NULL_41007)

	val TETHER_GAMEOBJECTS: Set[Int] = Set(NullObjectID.NULL_41352, NullObjectID.NULL_41353, NullObjectID.NULL_41354, NullObjectID.NULL_41355)

	val DAMAGED_TETHER_GAMEOBJECTS: Set[Int] = Set(ObjectID.DAMAGED_MAST_40996, ObjectID.DAMAGED_MAST_40997, ObjectID.DAMAGED_TOTEM_POLE, ObjectID.DAMAGED_TOTEM_POLE_41011)

	val MAX_DISTANCE            = 3000
	val PIE_DIAMETER            = 20
	val DOUBLE_SPOT_MOVE_MILLIS = 24000f
}
