package com.fredplugins.pvmHelper2.gauntlet

import net.runelite.api.{NpcID, NullNpcID}

sealed abstract class GauntletNpcType(val ids: Int *) {
	self: Product =>
	def name: String = this.productPrefix
}
object GauntletNpcType {
	trait WeakType {self: GauntletNpcType => }
	trait StrongType {self: GauntletNpcType => }
	trait DemiType {self: GauntletNpcType => }

	case object Bat extends GauntletNpcType(NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT) with WeakType {}
	case object Rat extends GauntletNpcType(NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT) with WeakType {}
	case object Spider extends GauntletNpcType(NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER) with WeakType {}
	case object Scorpion extends GauntletNpcType(NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION) with StrongType {}
	case object Unicorn extends GauntletNpcType(NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN) with StrongType {}
	case object Wolf extends GauntletNpcType(NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF) with StrongType {}
	case object Bear extends GauntletNpcType(NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR) with DemiType {}
	case object DarkBeast extends GauntletNpcType(NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST) with DemiType {}
	case object Dragon extends GauntletNpcType(NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON) with DemiType {}

	case object Hunllef extends GauntletNpcType(NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022, NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024, NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036, NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038) {}
	case object Tornado extends GauntletNpcType(NullNpcID.NULL_9025, NullNpcID.NULL_9039, NullNpcID.NULL_14142) {}

	val values: Seq[GauntletNpcType] = Seq(Bat, Rat, Spider, Scorpion, Unicorn, Wolf, Bear, DarkBeast, Dragon, Hunllef, Tornado)
}