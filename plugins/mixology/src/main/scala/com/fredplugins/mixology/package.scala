package com.fredplugins


import net.runelite.api.{Client, TileObject}
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.mixology.FredsMixologyPlugin
import com.fredplugins.mixology.SMixType.{Aga, Lye, Mox}
import net.runelite.api.ItemID.{ALCOAUGMENTATOR, ANTILEECH_LOTION, AQUALUX_AMALGAM, AZURE_AURA_MIX, LIPLACK_LIQUOR, MAMMOTHMIGHT_MIX, MARLEYS_MOONLIGHT, MEGALITE_LIQUID, MIXALOT, MYSTIC_MANA_AMALGAM}
import org.slf4j.Logger

import scala.util.chaining.*
package object mixology {
	private val log: Logger = ShimUtils.getLogger("com.fredsplugins.mixology", "DEBUG")
	val PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDER = 7063

	val VARBIT_POTION_ORDER = List(11315, 11317, 11319)
	val VARBIT_POTION_MODIFIER= List(11316, 11318, 11320)

	val VARBIT_POTION_ORDER_1: Int     = 11315
	val VARBIT_POTION_MODIFIER_1: Int  = 11316
	val VARBIT_POTION_ORDER_2: Int     = 11317
	val VARBIT_POTION_MODIFIER_2: Int  = 11318
	val VARBIT_POTION_ORDER_3: Int    = 11319
	val VARBIT_POTION_MODIFIER_3: Int = 11320

	val VARP_LYE_RESIN: Int  = 4414
	val VARP_AGA_RESIN : Int = 4415
	val VARP_MOX_RESIN : Int = 4416

	val VARBIT_ALEMBIC_PROGRESS: Int   = 11328
	val VARBIT_AGITATOR_PROGRESS: Int  = 11329
	val VARBIT_AGITATOR_QUICKACTION: Int  = 11337
	val VARBIT_ALEMBIC_QUICKACTION: Int   = 11338

	val VARBIT_MIXING_VESSEL_POTION: Int = 11339
	val VARBIT_AGITATOR_POTION: Int      = 11340
	val VARBIT_RETORT_POTION: Int        = 11341
	val VARBIT_ALEMBIC_POTION: Int       = 11342
	val VARBIT_DIGWEED_NORTH_EAST : Int = 11330
	val VARBIT_DIGWEED_SOUTH_EAST: Int      = 11331
	val VARBIT_DIGWEED_SOUTH_WEST: Int = 11332
	val VARBIT_DIGWEED_NORTH_WEST: Int = 11333
	val SPOT_ANIM_AGITATOR       : Int = 2954
	val SPOT_ANIM_ALEMBIC        : Int    = 2955
	val COMPONENT_POTION_ORDERS_GROUP_ID: Int = 882
	val COMPONENT_POTION_ORDERS: Int = COMPONENT_POTION_ORDERS_GROUP_ID << 16 | 2


	sealed trait SProcessType {
		this: Product =>
	}
	object SProcessType {
		case object Retort extends SProcessType {}
		case object Agitator extends SProcessType {}
		case object Alembic extends SProcessType {}

		def fromToolBench(to: TileObject): Option[SProcessType] = {
//			log.debug("SProcessType fromToolBench: {}", to.getId)
			Option(to.getId - 55389).filter((0 to 2).contains(_)).map(List(Retort, Agitator, Alembic).apply(_))
		}

		def fromOrderValue(i: Int): Option[SProcessType] = {
//			log.debug("SProcessType fromOrderValue: {}", i)
			Option.when( 1 to 3 contains i){List(Agitator, Retort, Alembic)(i-1)}
		}
	}
	sealed trait SMixType {
		this: Product =>
		def letter: Char = productPrefix.head
	}
	object SMixType {
		case object Mox extends SMixType
		case object Lye extends SMixType
		case object Aga extends SMixType
		case object UNKNOWN extends SMixType

		def fromPedestal(to: TileObject)(using client: Client): Option[SMixType] =
			Option.when(to.morphId != -1 && (55392 to 55394).contains(to.getId)) {
				(to.morphId - (to.getId match {
					case 55392 => 54905
					case 55393 => 54908
					case 55394 => 54911
				}))
			}.collect {
				case 0 => Aga
				case 1 => Lye
				case 2 => Mox
			}
	}


	sealed trait SBrew(val unprocessedId: Int, val xp: Int) {
		this: Product =>
		val recipe: Map[SMixType, Int] = {
			this.productPrefix.collect[SMixType] {
				case 'M' => Mox
				case 'A' => Aga
				case 'L' => Lye
			}.toList.pipe(
				mList => mList.distinct.map(m => m -> mList.filter(_ == m).size)
			).toMap
		}.filter(_._2 > 0)

		val worth: Map[SMixType, Int] = recipe.collect {
			case (m, 1) => m -> 10
			case (m, c) if c >= 2 => m -> 20
		}

		def totalWorth: Int = worth.values.sum
	}
	object SBrew {
		case object MMM extends SBrew(MAMMOTHMIGHT_MIX, 190) {}
		case object MMA extends SBrew(MYSTIC_MANA_AMALGAM, 215) {}
		case object MML extends SBrew(MARLEYS_MOONLIGHT, 240) {}
		case object AAA extends SBrew(ALCOAUGMENTATOR, 190) {}
		case object ALA extends SBrew(AQUALUX_AMALGAM, 290) {}
		case object AAM extends SBrew(AZURE_AURA_MIX, 265) {}
		case object LLL extends SBrew(LIPLACK_LIQUOR, 190) {}
		case object ALL extends SBrew(ANTILEECH_LOTION, 340) {}
		case object MLL extends SBrew(MEGALITE_LIQUID, 315) {}
		case object MAL extends SBrew(MIXALOT, 365) {}

		def fromToolBench(to: TileObject)(using client: Client): Option[SBrew] = {
			Option(to).filter(_.morphId != -1).map(to =>
				to.morphId - (to.getId match {
					case 55390 => 54881
					case 55389 => 54870
					case 55391 => 54892
				})
			).collect[SBrew] {
				case 1 => MMM//"Mammoth-might mix"
				case 2 => MMA//"Mystic mana amalgam"
				case 3 => MML//"Marley's moonlight"
				case 4 => AAA//"Alco-augmentator"
				case 5 => ALA//"Aqualux amalgam"
				case 6 => AAM//"Azure aura mix"
				case 7 => LLL//"Liplack liquor"
				case 8 => ALL//"Anti-leech lotion"
				case 9 => MLL//"Megalite liquid"
				case 10 =>MAL//"Mixalot"
			}
		}
		def fromOrderValue(i: Int): Option[SBrew] = {
//			log.debug("SBrew fromOrderValue: {}", i)
			Option(i).collect[SBrew] {
				case 1 => MMM
				case 2 => MMA
				case 3 => MML
				case 4 => AAA
				case 5 => AAM
				case 6 => ALA
				case 7 => LLL
				case 8 => MLL
				case 9 => ALL
				case 10 =>MAL
			}
		}
	}
}
