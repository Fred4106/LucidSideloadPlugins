package com.fredplugins.pvmDebugger

import com.fredplugins.common.ProjectileID
import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.gameval.AnimationID

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

package object vorkath {
	val VorkathRegion = 9023

	sealed trait VorkAttack extends enumeratum.EnumEntry {
		def animationId: Int
	}
	sealed trait VorkMeleeAttack(val animationId: Int) extends VorkAttack {}
	sealed trait VorkRangedAttack(val animationId: Int, val projectileId: Int) extends VorkAttack {}
	
	object VorkAttacks extends enumeratum.Enum[VorkAttack] with ShimUtils.Logging() {
		/**
		 * Vorkath's melee attack (see VorkathPlugin#onAnimationChanged)
		 */
		case object Slash extends VorkMeleeAttack(AnimationID.DS2_VORKATH_ATTACK_MELEE)

		/**
		* Vorkath's dragon breath attack
		*/
		case object FireBreath extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_DRAGONBREATH)

		/**
		* Vorkath's dragon breath attack causing the player's active prayers to be deactivated
		*/
		case object PrayerBreath extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_PRAYER_DISABLE)

		/**
		* Vorkath's dragon breath attack causing the player to become poisoned with venom
		*/
		case object VenomBreath extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_VENOM)

		/**
		* Vorkath's ranged attack
		*/
		case object Spike extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_RANGED)

		/**
		* Vorkath's magic attack
		*/
		case object Ice extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_MAGIC)

		/**
		* Vorkath's aoe fire bomb attack (3x3 from where player was originally standing)
		*/
		case object FireBomb extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED_UP, ProjectileID.VORKATH_BOMB_AOE)

		/**
		* Vorkath's aoe acid attacking, spewing acid across the instance
		*/
		case object Acid extends VorkRangedAttack(AnimationID.DS2_VORKATH_ACID, ProjectileID.VORKATH_POISON_POOL_AOE)

		/**
		* Vorkath's fire ball attack that is fired during the acid phase, almost every tick for 25(?) attacks total
		*/
		case object FireBall extends VorkRangedAttack(AnimationID.DS2_VORKATH_ACID, ProjectileID.VORKATH_TICK_FIRE_AOE)

		/**
		* Vorkath's dragon breath attack causing the player to be frozen during Zombified Spawn phase
		*/
		case object FreezeBreath extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_ICE)

		/**
		* Vorkath's spawning of a Zombified Spawn
		*/
		case object ZombifiedSpawn extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED_UP, ProjectileID.VORKATH_SPAWN_AOE)

		override def values: IndexedSeq[VorkAttack] = findValues.pipe(x => {
			val uniqueProjectiles = x.collect {
				case attack: VorkRangedAttack => attack
			}.groupBy(_.projectileId)
			val meleeAttacks = x.collect {
				case attack: VorkMeleeAttack => attack
			}.groupBy(_.animationId)
			assert(uniqueProjectiles.forall(_._2.size == 1))
			assert(meleeAttacks.forall(_._2.size == 1))
			x
		})

		def getPossibleAttacks(animationId: Int): IndexedSeq[VorkAttack] = {
			values.filter(_.animationId == animationId)
		}

		def getAttackByProjectileId(projectileId: Int): Option[VorkRangedAttack] = {
			values.collectFirst {
				case attack: VorkRangedAttack if attack.projectileId == projectileId => attack
			}
		}
	}
}
