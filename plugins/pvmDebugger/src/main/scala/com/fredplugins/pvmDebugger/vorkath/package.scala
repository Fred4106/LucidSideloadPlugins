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

//	enum Phase {
//		UNKNOWN
//		,
//		ACID
//		,
//		FIRE_BALL
//		,
//		SPAWN
//	}

	sealed trait VorkPhase extends enumeratum.EnumEntry {}
	object VorkPhases extends enumeratum.Enum[VorkPhase] with ShimUtils.Logging() {
		case object Unknown extends VorkPhase
		case object Acid extends VorkPhase
		case object FireBall extends VorkPhase
		case object Spawn extends VorkPhase

		override def values: IndexedSeq[VorkPhase] = findValues
	}

	sealed trait VorkAttack extends enumeratum.EnumEntry {
		self: VorkSpecialAttack | VorkBasicAttack =>
		def animationId: Int
	}
	sealed transparent trait VorkBasicAttack {
		self: VorkAttack =>
		assert(!self.isInstanceOf[VorkSpecialAttack])
	}
	sealed transparent trait VorkSpecialAttack {
		self: VorkAttack =>
		assert(!self.isInstanceOf[VorkBasicAttack])
	}
	sealed transparent trait VorkMeleeAttack(val animationId: Int) extends VorkAttack {
		self: VorkSpecialAttack | VorkBasicAttack =>
	}
	sealed transparent trait VorkRangedAttack(val animationId: Int, val projectileId: Int) extends VorkAttack {
		self: VorkSpecialAttack | VorkBasicAttack =>
	}

	object VorkAttacks extends enumeratum.Enum[VorkAttack] with ShimUtils.Logging() {
		object BasicAttack {
			def unapply(v: VorkAttack): Option[VorkBasicAttack] = {
				Option(v).collect{
					case va: VorkBasicAttack => va
				}
			}
		}

		object SpecialAttack {
			def unapply(v: VorkAttack): Option[VorkSpecialAttack] = {
				Option(v).collect{
					case va: VorkSpecialAttack => va
				}
			}
		}

		/**
		 * Vorkath's melee attack (see VorkathPlugin#onAnimationChanged)
		 */
		case object Slash extends VorkMeleeAttack(AnimationID.DS2_VORKATH_ATTACK_MELEE) with VorkBasicAttack

		/**
		* Vorkath's dragon breath attack
		*/
		case object FireBreath extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_DRAGONBREATH) with VorkBasicAttack

		/**
		* Vorkath's dragon breath attack causing the player's active prayers to be deactivated
		*/
		case object PrayerBreath extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_PRAYER_DISABLE) with VorkBasicAttack

		/**
		* Vorkath's dragon breath attack causing the player to become poisoned with venom
		*/
		case object VenomBreath extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_VENOM) with VorkBasicAttack

		/**
		* Vorkath's ranged attack
		*/
		case object Spike extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_RANGED) with VorkBasicAttack

		/**
		* Vorkath's magic attack
		*/
		case object Ice extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_MAGIC) with VorkBasicAttack

		/**
		* Vorkath's aoe fire bomb attack (3x3 from where player was originally standing)
		*/
		case object FireBomb extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED_UP, ProjectileID.VORKATH_BOMB_AOE) with VorkBasicAttack

		/**
		* Vorkath's aoe acid attacking, spewing acid across the instance
		*/
		case object Acid extends VorkRangedAttack(AnimationID.DS2_VORKATH_ACID, ProjectileID.VORKATH_POISON_POOL_AOE) with VorkSpecialAttack

		/**
		* Vorkath's fire ball attack that is fired during the acid phase, almost every tick for 25(?) attacks total
		*/
		case object FireBall extends VorkRangedAttack(AnimationID.DS2_VORKATH_ACID, ProjectileID.VORKATH_TICK_FIRE_AOE)with VorkSpecialAttack

		/**
		* Vorkath's dragon breath attack causing the player to be frozen during Zombified Spawn phase
		*/
		case object FreezeBreath extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED, ProjectileID.VORKATH_ICE)with VorkSpecialAttack

		/**
		* Vorkath's spawning of a Zombified Spawn
		*/
		case object ZombifiedSpawn extends VorkRangedAttack(AnimationID.DS2_VORKATH_RANGED_UP, ProjectileID.VORKATH_SPAWN_AOE)with VorkSpecialAttack

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
