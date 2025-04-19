package com.fredplugins

import com.fredplugins.common.WorldAreaUtil
import net.runelite.api.Actor
import net.runelite.api.Client
import net.runelite.api.HeadIcon
import net.runelite.api.HitsplatID
import net.runelite.api.NPC
import net.runelite.api.Player
import net.runelite.api.World
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.api.gameval.AnimationID
import net.runelite.api.gameval.NpcID

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

package object demonicgorillaV2 {
	val Max_Attack_Range        = 10 // Needs <= 10 tiles to reach target
	val Attack_Rate             = 5 // 5 ticks between each attack
	val Attacks_per_Switch      = 3 // 3 unsuccessful attacks per style switch
	val Projectile_Magic_Speed  = 8 // Travels 8 tiles per tick
	val Projectile_Ranged_Speed = 6 // Travels 6 tiles per tick
	val Projectile_Magic_Delay  = 12 // Requires an extra 12 tiles
	val Projectile_Ranged_Delay = 9 // Requires an extra 9 tiles

	val REGION_IDS                 = Set(8280, 8536)
	val DEMONIC_GORILLA_AOE_ATTACK = 7228

	val DEMONIC_GORILLA_RANGED  = 1302
	val DEMONIC_GORILLA_MAGIC   = 1304
	val DEMONIC_GORILLA_BOULDER = 856
	val DEMONIC_PROJECTILES     = Set(DEMONIC_GORILLA_RANGED,
																		DEMONIC_GORILLA_MAGIC,
																		DEMONIC_GORILLA_BOULDER)

	sealed trait RegularAttack {
		self: AttackStyle =>}

	enum AttackStyle {
		case Magic extends AttackStyle with RegularAttack
		case Ranged extends AttackStyle with RegularAttack
		case Melee extends AttackStyle with RegularAttack
		case Bolder extends AttackStyle
	}
	object AttackStyle {
		val RegularAttacks: List[AttackStyle & RegularAttack] = values.toList.collect {
			case r: RegularAttack => r
		}
		def getProtectedStyle(player: Player): Option[AttackStyle] = {
			Option(player.getOverheadIcon).collect {
				case HeadIcon.MELEE => AttackStyle.Melee
				case HeadIcon.RANGED => AttackStyle.Ranged
				case HeadIcon.MAGIC => AttackStyle.Magic
			}
		}
	}

	case class GorillaData(
		attackStyle: Set[AttackStyle & RegularAttack],
		prayingAgainstStyle: AttackStyle & RegularAttack,
		attacksUntilSwitch: Int = Attacks_per_Switch,
		previousAnimationId: Int = -1,
		previousProjectileId: Int = -1,
		takenDamageRecently: Boolean = false
	)
	case class PendingGorillaAttack(attacker: NPC, attackStyle: AttackStyle, target: Player, finishesOnTick: Int) {}

	object IsNpcGorilla {
		private val GorillaIds = Set(
			NpcID.MM2_DEMON_GORILLA_1_MELEE,
			NpcID.MM2_DEMON_GORILLA_1_RANGED,
			NpcID.MM2_DEMON_GORILLA_1_MAGIC,
			NpcID.MM2_DEMON_GORILLA_2_MELEE,
			NpcID.MM2_DEMON_GORILLA_2_RANGED,
			NpcID.MM2_DEMON_GORILLA_2_MAGIC)

		def unapply(arg: Actor): Boolean = {
			Option(arg).collect {
				case n: NPC if GorillaIds.contains(n.getId) => true
			}.getOrElse(false)
		}
	}
}
