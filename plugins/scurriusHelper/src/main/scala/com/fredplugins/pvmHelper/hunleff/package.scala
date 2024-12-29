package com.fredplugins.pvmHelper

import com.fredplugins.common.Locatable
//import com.fredplugins.pvmHelper.hunleff.Hunllef.{ATTACK_TICK_SPEED, AttackPhase, MAX_ATTACK_COUNT, MAX_PLAYER_ATTACK_COUNT}
//import com.fredplugins.pvmHelper.hunleff.ProjectileClass.{Magic, Prayer, Ranged}
import net.runelite.api.{Client, NPC, NpcID, NullNpcID, Projectile, Skill}
import net.runelite.client.game.SkillIconManager
import net.runelite.client.util.ImageUtil

import scala.compiletime.uninitialized

import scala.util.chaining.*
import java.awt.image.BufferedImage
import scala.util.Try

package object hunleff {
//
//
//	enum ProjectileClass {
//		case Ranged
//		case Magic
//		case Prayer
//
//		def loadOriginalIcon(using skillIconManager: SkillIconManager): BufferedImage = {
//			Try(
//				this match {
//					case Ranged => skillIconManager.getSkillImage(Skill.RANGED)
//					case Magic => skillIconManager.getSkillImage(Skill.MAGIC)
//					case Prayer => skillIconManager.getSkillImage(Skill.PRAYER)
//				}
//			).get
//		}
//	}
//
//	enum ProjectileDef(val id: Int, val projClass: ProjectileClass) {
//		case HUNLLEF_MAGE_ATTACK extends ProjectileDef(1707, Magic)
//		case HUNLLEF_CORRUPTED_MAGE_ATTACK extends ProjectileDef(1708, Magic)
//		case HUNLLEF_RANGE_ATTACK extends ProjectileDef(1711, Ranged)
//		case HUNLLEF_CORRUPTED_RANGE_ATTACK extends ProjectileDef(1712, Ranged)
//		case HUNLLEF_PRAYER_ATTACK extends ProjectileDef(1713, Prayer)
//		case HUNLLEF_CORRUPTED_PRAYER_ATTACK extends ProjectileDef(1714, Prayer)
//
//		def icon(using skillIconManager: SkillIconManager): BufferedImage = projClass.loadOriginalIcon
//
//		def matches(p: Projectile): Boolean = p.getId == id
//	}
//
//	object Missile {
//		def apply(projectile: Projectile)(using skillIconManager: SkillIconManager): Option[Missile] = {
//			ProjectileDef
//				.values
//				.find(_.matches(projectile))
//				.map(d => new Missile(d, d.icon)(projectile))
//		}
//	}
//
//	class Missile(val projectileDef: ProjectileDef, val originalIcon: BufferedImage)(val projectile: Projectile) extends Locatable(projectile) {
//		assert(projectileDef.matches(projectile))
//		private var iconV: BufferedImage = null
//		private var iconSizeV: Int = 12
//
//		def icon(): BufferedImage = {
//			if (iconV == null) {
//				iconV = ImageUtil.resizeImage(originalIcon, iconSizeV, iconSizeV)
//			}
//			iconV
//		}
//
//		def iconSize_=(i: Int): Unit = {
//			iconSizeV = i
//			iconV = null
//		}
//
//		override def toString: String = {
//			s"Missile($projectileDef)($projectile)"
//		}
//	}
//
//	object Tornado {
//		val validIds: Set[Int] = Set(NullNpcID.NULL_9025, NullNpcID.NULL_9039, NullNpcID.NULL_14142)
//		val TICK_DURATION = 21
//	}
//
//	class Tornado(toWrap: NPC) extends Locatable(toWrap) {
//
//		import com.fredplugins.pvmHelper.hunleff.Tornado.{TICK_DURATION, validIds}
//
//		assert(validIds.contains(toWrap.getId))
//		private var timeLeftV: Int = TICK_DURATION
//
//		def npc: NPC = toWrap
//
//		def timeLeft: Int = timeLeftV
//
//		def tick(): Unit = if (timeLeft >= 0) timeLeftV = timeLeftV - 1
//
//		override def toString: String = s"Tornado($npc)(timeLeft=$timeLeft)"
//	}
//
//	object Hunllef {
//		val validIds: Set[Int] = Set(NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022,
//			NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024,
//			NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036,
//			NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038)
//
//		private val ATTACK_TICK_SPEED: Int = 4
//
//		private val MAX_ATTACK_COUNT: Int = 4
//		private val MAX_PLAYER_ATTACK_COUNT: Int = 6
//
//		enum AttackPhase(val prayer: net.runelite.api.Prayer) {
//			case Magic extends AttackPhase(net.runelite.api.Prayer.PROTECT_FROM_MAGIC)
//			case Range extends AttackPhase(net.runelite.api.Prayer.PROTECT_FROM_MISSILES)
//		}
//	}
//
//	class Hunllef(toWrap: NPC)(val originalMagicIcon: BufferedImage, val originalRangeIcon: BufferedImage, initialIconSize: Int) extends Locatable(toWrap) {
//
//		import com.fredplugins.pvmHelper.hunleff.Hunllef.{validIds}
//
//		assert(validIds.contains(toWrap.getId))
//		private var attackCountV = MAX_ATTACK_COUNT
//		private var playerAttackCountV = MAX_PLAYER_ATTACK_COUNT
//		private var ticksUntilNextAttackV = 0
//		private var attackPhaseV: Hunllef.AttackPhase = AttackPhase.Range
//
//		def npc: NPC = toWrap
//
//		private var rangeIconV: BufferedImage = null
//		private var magicIconV: BufferedImage = null
//		private var iconSizeV: Int = initialIconSize
//
//		def icon(): BufferedImage = {
//			if (rangeIconV == null) {
//				rangeIconV = ImageUtil.resizeImage(originalRangeIcon, iconSizeV, iconSizeV)
//			}
//			if (magicIconV == null) {
//				magicIconV = ImageUtil.resizeImage(originalMagicIcon, iconSizeV, iconSizeV)
//			}
//			attackPhaseV match {
//				case AttackPhase.Magic => magicIconV
//				case AttackPhase.Range => rangeIconV
//			}
//		}
//		def iconSize_=(i: Int): Unit = {
//			iconSizeV = i
//			rangeIconV = null
//			magicIconV = null
//		}
//
//		def decrementTicksUntilNextAttack(): Unit = {
//			if (ticksUntilNextAttackV > 0) ticksUntilNextAttackV -= 1
//		}
//		def updatePlayerAttackCount(): Unit = {
//			playerAttackCountV = if(playerAttackCountV <= 1) MAX_PLAYER_ATTACK_COUNT else (playerAttackCountV - 1)
//		}
//		def updateAttackCount(): Unit = {
//			ticksUntilNextAttackV = ATTACK_TICK_SPEED
//			attackCountV = if(attackCountV <= 1) MAX_ATTACK_COUNT else (attackCountV - 1)
//		}
//		def toggleAttackHunllefAttackStyle(): Unit = {
//			attackPhaseV =
//				if (attackPhaseV == AttackPhase.Range) AttackPhase.Magic
//				else AttackPhase.Range
//			attackCountV = MAX_ATTACK_COUNT
//		}
//		def getAttackPhase:AttackPhase = this.attackPhaseV
//
//		override def toString: String = s"Hunllef($npc)($attackCountV, ${playerAttackCountV}, $ticksUntilNextAttackV, $attackPhaseV)"
//	}
}
