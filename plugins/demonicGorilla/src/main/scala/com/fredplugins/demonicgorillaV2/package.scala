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

	case class PendingGorillaAttack(attacker:       DemonicGorilla, attackStyle: AttackStyle, target: Player,
																	finishesOnTick: Int
																 ) {}

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

	def checkPendingAttacks(pending: List[PendingGorillaAttack], memorizedPlayers: List[MemorizedPlayer])(using
																																																				client: Client
	): List[PendingGorillaAttack] = {
		val tickCounter                = client.getTickCount
		val (retained, removedAttacks) = pending.partition(attack => attack.finishesOnTick > tickCounter)
		removedAttacks.foreach(attack => {
			if (memorizedPlayers.find(_.player == attack.target)
													.map(_.getRecentHitsplats)
													.forall(hp => hp.isEmpty || hp.exists(_.getHitsplatType == HitsplatID.BLOCK_ME))
			) {
				attack.attacker.tap(_.decrementAttacksUntilSwitch()).tap(checkGorillaAttackStyleSwitch(_))
			}
		})
		retained
	}

	def checkGorillaAttacks(gorillas:         List[DemonicGorilla],
													memorizedPlayers: List[MemorizedPlayer],
													recentBoulders:   List[WorldPoint]
												 )
												 (using client: Client)
	: List[PendingGorillaAttack] = {
		val nPendingAttacks = scala.collection.mutable.ListBuffer.empty[PendingGorillaAttack]
		val tickCounter = client.getTickCount
		def onGorillaAttack(gorilla: DemonicGorilla, attackStyle: AttackStyle): Unit = {
			gorilla.setInitiatedCombat(true)
			val target                      = gorilla.getInteracting
			val protectedStyle: AttackStyle = if (target != null) AttackStyle.getProtectedStyle(target).orNull else null

			val correctPrayer = target == null || (attackStyle != null && attackStyle.equals(protectedStyle)) // If player
			// is out of memory, assume prayer was correct

			if (attackStyle == AttackStyle.Bolder) {
				// The gorilla can't throw boulders when it's meleeing
				gorilla.setNextPosibleAttackStyles(gorilla.getNextPosibleAttackStyles.filter(_ != AttackStyle.Melee))

			} else {
				if (correctPrayer) {
					gorilla.setAttacksUntilSwitch(gorilla.getAttacksUntilSwitch - 1)
				} else {
					// We're not sure if the attack will hit a 0 or not,
					// so we don't know if we should decrease the counter or not,
					// so we keep track of the attack here until the damage splat
					// has appeared on the player.
					var damagesOnTick       = client.getTickCount
					val mp: MemorizedPlayer = memorizedPlayers.find(_.player == target).orNull
					if (attackStyle eq AttackStyle.Magic) {
						val lastPlayerArea = mp.getLastWorldArea
						if (lastPlayerArea != null) {
							val dist = gorilla.getWorldArea.distanceTo(lastPlayerArea)
							damagesOnTick += (dist + Projectile_Magic_Delay) / Projectile_Magic_Speed
						}
					}
					else if (attackStyle eq AttackStyle.Ranged) {
						val lastPlayerArea = mp.getLastWorldArea
						if (lastPlayerArea != null) {
							val dist = gorilla.getWorldArea.distanceTo(lastPlayerArea)
							damagesOnTick += (dist + Projectile_Ranged_Delay) / Projectile_Ranged_Speed
						}
					}
					nPendingAttacks.addOne(PendingGorillaAttack(gorilla, attackStyle, target, damagesOnTick))
				}

				gorilla.filterPossibleAttackStyles(_ == attackStyle)
				if (gorilla.getNextPosibleAttackStyles.isEmpty) {
					// Sometimes the gorilla can switch attack style before it's supposed to
					// if someone was fighting it earlier and then left, so we just
					// reset the counter in that case.
					gorilla.setNextPosibleAttackStyles(AttackStyle.RegularAttacks.filter(_ == attackStyle))
					gorilla.setAttacksUntilSwitch(Attacks_per_Switch - (if (correctPrayer) 1 else 0))
				}
			}
			checkGorillaAttackStyleSwitch(gorilla, protectedStyle)
//			val tickCounter = client.getTickCount
			gorilla.setNextAttackTick(tickCounter + Attack_Rate)
		}

		gorillas.foreach(gorilla => {
			//			val interacting: Player | Null          = gorilla.getInteracting
			val mp = memorizedPlayers.find(_.player == gorilla.getInteracting).orNull
			if (gorilla.getLastTickInteracting != null && gorilla.getInteracting == null) {
				gorilla.setInitiatedCombat(false)
			} else if (
				mp != null && mp.getLastWorldArea != null &&
					!gorilla.isInitiatedCombat &&
					tickCounter < gorilla.getNextAttackTick &&
					gorilla.getWorldArea.isInMeleeDistance(mp.getLastWorldArea)
			) {
				gorilla.setInitiatedCombat(true)
				gorilla.setNextAttackTick(tickCounter + 1)
			}

			//
			if (gorilla.isTakenDamageRecently && tickCounter >= gorilla.getNextAttackTick + 4) {
				// The gorilla was flinched, so its next attack gets delayed
				gorilla.setNextAttackTick(tickCounter + Attack_Rate / 2)
				gorilla.setInitiatedCombat(true)
				if (mp != null && mp.getLastWorldArea != null &&
					!gorilla.getWorldArea
									.isInMeleeDistance(
										mp.getLastWorldArea) && !gorilla.getWorldArea
																										.intersectsWith(mp
																																			.getLastWorldArea)) {
					// Gorillas stop meleeing when they get flinched
					// and the target isn't in melee distance
					gorilla.setNextPosibleAttackStyles(gorilla.getNextPosibleAttackStyles
																										.filter(_ != AttackStyle.Melee))
					if (gorilla.getInteracting != null) {
						val z = Seq(AttackStyle.Melee) ++ AttackStyle.getProtectedStyle(gorilla.getInteracting).iterator
						checkGorillaAttackStyleSwitch(gorilla, z *)
					}
				}
			} else if (gorilla.getAnimationId != gorilla.getLastTickAnimation) {
				gorilla.getAnimationId match {
					case AnimationID.DEMONIC_GORILLA_PUNCH => onGorillaAttack(gorilla, AttackStyle.Melee)
					case AnimationID.DEMONIC_GORILLA_MAGIC => onGorillaAttack(gorilla, AttackStyle.Magic)
					case AnimationID.DEMONIC_GORILLA_RANGE => onGorillaAttack(gorilla, AttackStyle.Ranged)
					case AnimationID.DEMONIC_GORILLA_SMASH_CHEST if gorilla.getInteracting != null && gorilla.getNextPosibleAttackStyles.exists(x => Set(AttackStyle.Magic, AttackStyle.Ranged).contains(x)) => {
						// Note that AoE animation is the same as prayer switch animation
						// so we need to check if the prayer was switched or not.
						// It also does this animation when it spawns, so
						// we need the interacting != null check.
						if (gorilla.getOverheadIcon == gorilla.getLastTickOverheadIcon) {
							// Confirmed, the gorilla used the AoE attack
							onGorillaAttack(gorilla, AttackStyle.Bolder)
						} else {
							if (tickCounter >= gorilla.getNextAttackTick) {
								// This part is more complicated because the gorilla may have
								// used an attack, but the prayer switch animation takes
								// priority over normal attack animations.
								val projectileId = gorilla.getRecentProjectileId
								if (projectileId == DEMONIC_GORILLA_MAGIC) {
									onGorillaAttack(gorilla, AttackStyle.Magic)
								} else if (projectileId == DEMONIC_GORILLA_RANGED) {
									onGorillaAttack(gorilla, AttackStyle.Ranged)
								} else if (mp != null) {
									val lastPlayerArea = mp.getLastWorldArea
									if (lastPlayerArea != null && recentBoulders.exists(x => x.distanceTo(lastPlayerArea) == 0)) {
										// A boulder started falling on the gorillas target,
										// so we assume it was the gorilla who shot it
										onGorillaAttack(gorilla, AttackStyle.Bolder)
									}
									else if (mp.getRecentHitsplats.nonEmpty) {
										// It wasn't any of the three other attacks,
										// but the player took damage, so we assume
										// it's a melee attack
										onGorillaAttack(gorilla, AttackStyle.Melee)
									}
								}
							}
							// The next attack tick is always delayed if the
							// gorilla switched prayer
							gorilla.setNextAttackTick(tickCounter + Attack_Rate)
						}
					}

				}
			}


			if (gorilla.getDisabledMeleeMovementForTicks > 0) {
				gorilla.decrementDisabledMeleeMovementForTicks()
			} else if(
				gorilla.isInitiatedCombat
					&& gorilla.getInteracting != null
					&& !gorilla.isChangedAttackStyleThisTick
					&& gorilla.getNextPosibleAttackStyles.size >= 2
					&& gorilla.getNextPosibleAttackStyles.contains(AttackStyle.Melee)
			) {
				// If melee is a possibility, we can check if the gorilla
				// is or isn't moving toward the player to determine if
				// it is actually attempting to melee or not.
				// We only run this check if the gorilla is in combat
				// because otherwise it attempts to travel to melee
				// distance before attacking its target.
				if (mp != null && mp.getLastWorldArea != null && gorilla.getLastWorldArea != null) {
					val predictedNewArea: WorldArea | Null = new WorldAreaUtil(gorilla.getLastWorldArea)
						.calculateNextTravellingPoint(client, mp.getLastWorldArea, true, (x: WorldPoint) => {
							val area1 = new WorldArea(x, 1, 1)
							val v1    = !gorillas.exists((y) => {
								if (y != gorilla) {
									val area2 = if (y.getIndex < gorilla.getIndex) y.getWorldArea else y.getLastWorldArea
									area2 != null && area1.intersectsWith(area2)
								} else {
									false
								}
							})
							val v2    = !memorizedPlayers.exists(y => {
								val area2 = y.getLastWorldArea
								area2 != null && area1.intersectsWith(area2)
							})
							v1 && v2
						})
					/*						.calculateNextTravellingPoint(client,
																										mp.getLastWorldArea, true,
																										(x) => {
																											def foo(x) = {
																												// Gorillas can't
																												// normally walk
																												// through other
																												// gorillas
																												// or other players
																												val area1 =
																													new Nothing(
																														x, 1, 1)
																												gorillas.values
																																.stream
																																.noneMatch(
																																	(y)
																																	=> {
																																		def
																																		foo
																																			(y) = {
																																			if
																																			(y
																																				eq gorilla) {
																																				return
																																					false
																																			}
																																			val area2 = if (y
																																				.getNpc
																																				.getIndex < gorilla
																																				.getNpc
																																				.getIndex) {
																																				y
																																					.getNpc
																																					.getWorldArea
																																			} else {
																																				y
																																					.getLastWorldArea
																																			}
																																			area2 != null &&
																																				area1
																																					.intersectsWith(
																																						area2)
																																		}

																																		foo(y)
																																	}) &&
																													memorizedPlayers
																														.values
																														.stream
																														.noneMatch((y) => {
																															val area2
																															= y
																																.getLastWorldArea
																															area2 != null &&
																																area1
																																	.intersectsWith(
																																		area2)

																														})
																												// There is a special
																												// case where if a
																												// player walked through
																												// a gorilla, or a
																												// player walked
																												// through another
																												// player,
																												// the tiles that were
																												// walked through
																												// becomes
																												// walkable, but I
																												// didn't feel like
																												// it's necessary to
																												// handle
																												// that special case as
																												// it should rarely
																												// happen.
																											}

																											foo(x)
																										})*/
					if (predictedNewArea != null) {
						val distance          = gorilla.getWorldArea.distanceTo(mp.getLastWorldArea)
						val predictedMovement = predictedNewArea.toWorldPoint
						if (distance <= Max_Attack_Range && mp.getLastWorldArea
																									.hasLineOfSightTo(client.getTopLevelWorldView,
																																		gorilla
																																			.getLastWorldArea)) {
							if (predictedMovement.distanceTo(gorilla.getLastWorldArea.toWorldPoint) != 0) {
								if (predictedMovement.distanceTo(gorilla.getWorldLocation) == 0) {
									gorilla.filterPossibleAttackStyles(_ == AttackStyle.Melee)
								} else {
									gorilla.filterPossibleAttackStyles(_ != AttackStyle.Melee)
								}
							} else if (tickCounter >= gorilla.getNextAttackTick && (gorilla.getRecentProjectileId == -1)
								&& recentBoulders.count(_.distanceTo(mp.getLastWorldArea) eq 0) == 0) {
								gorilla.filterPossibleAttackStyles(_ == AttackStyle.Melee)
							}
						}
					}
				}
			}

			if (gorilla.isTakenDamageRecently) gorilla.setInitiatedCombat(true)

			if (gorilla.getOverheadIcon != gorilla.getLastTickOverheadIcon) {
				if (gorilla.isChangedAttackStyleLastTick || gorilla.isChangedAttackStyleThisTick) {
					// Apparently if it changes attack style and changes
					// prayer on the same tick or 1 tick apart, it won't
					// be able to move for the next 2 ticks if it attempts
					// to melee
					gorilla.setDisabledMeleeMovementForTicks(2)
				}
				else {
					// If it didn't change attack style lately,
					// it's only for the next 1 tick
					gorilla.setDisabledMeleeMovementForTicks(1)
				}
			}
			gorilla.setLastTickAnimation(gorilla.getAnimationId)
			gorilla.setLastWorldArea(gorilla.getWorldArea)
			gorilla.setLastTickInteracting(gorilla.getInteracting)
			gorilla.setTakenDamageRecently(false)
			gorilla.setChangedAttackStyleLastTick(gorilla.isChangedAttackStyleThisTick)
			gorilla.setChangedAttackStyleThisTick(false)
			gorilla.setLastTickOverheadIcon(gorilla.getOverheadIcon)
			gorilla.setRecentProjectileId(-1)
		})
		nPendingAttacks.toList
	}

	def checkGorillaAttackStyleSwitch(gorilla: DemonicGorilla, protectedStyles: AttackStyle*): Unit = {
		if (gorilla.getAttacksUntilSwitch <= 0 || gorilla.getNextPosibleAttackStyles.isEmpty) {
			gorilla.setNextPosibleAttackStyles(
				AttackStyle.RegularAttacks.filterNot(x => protectedStyles.contains(x))
				)
			gorilla.setAttacksUntilSwitch(Attacks_per_Switch)
			gorilla.setChangedAttackStyleThisTick(true)
		}
	}
}
