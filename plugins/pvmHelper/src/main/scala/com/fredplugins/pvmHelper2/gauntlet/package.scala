package com.fredplugins.pvmHelper2

import net.runelite.api.HeadIcon

import scala.::
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

package object gauntlet {
	sealed trait PlayerAttackType {}
	sealed trait HunllefAttackStyle {}
	sealed trait HunllefProtectStyle(val validAttacks: Set[PlayerAttackType]) {}

	object PlayerAttackType {
		case object Mage extends PlayerAttackType
		case object Range extends PlayerAttackType
		case object Melee extends PlayerAttackType
	}

	object HunllefAttackStyle {
		case object Mage extends HunllefAttackStyle
		case object Range extends HunllefAttackStyle
		def toggle[A <: HunllefAttackStyle](in: A): HunllefAttackStyle = {
			in match {
				case Mage => Range
				case Range => Mage
			}
		}
	}
	object HunllefProtectStyle {
		case object None extends HunllefProtectStyle( Set(PlayerAttackType.Range, PlayerAttackType.Mage, PlayerAttackType.Melee) )
		case object Mage extends HunllefProtectStyle( Set(PlayerAttackType.Range, PlayerAttackType.Melee) )
		case object Range extends HunllefProtectStyle( Set(PlayerAttackType.Mage, PlayerAttackType.Melee) )
		case object Melee extends HunllefProtectStyle( Set(PlayerAttackType.Range, PlayerAttackType.Mage) )

		def unapply(headIcon: HeadIcon): Option[HunllefProtectStyle] = {
			if(headIcon == null) Some(None)
			else {
				Option(headIcon).collect {
					case HeadIcon.MAGIC => Mage
					case HeadIcon.RANGED => Range
					case HeadIcon.MELEE => Melee
				}
			}
		}
	}
}
