package com.fredplugins


import net.runelite.api.{Client, TileObject}
import net.runelite.api.gameval.{VarbitID, VarPlayerID, InterfaceID}
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.mixology.FredsMixologyPlugin
import net.runelite.api.gameval.ItemID.{
	MM_POTION_MMM_UNFINISHED,
	MM_POTION_MMA_UNFINISHED,
	MM_POTION_MML_UNFINISHED,
	MM_POTION_AAA_UNFINISHED,
	MM_POTION_AAL_UNFINISHED,
	MM_POTION_AAM_UNFINISHED,
	MM_POTION_LLL_UNFINISHED,
	MM_POTION_LLA_UNFINISHED,
	MM_POTION_LLM_UNFINISHED,
	MM_POTION_MAL_UNFINISHED,
}
import org.slf4j.Logger

import java.awt.Color
import scala.util.chaining.*
package object mixology {
	private val log: Logger = ShimUtils.getLogger("com.fredsplugins.mixology", "DEBUG")
	inline def PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDERS: Int = 7063
	inline def PROC_MASTERING_MIXOLOGY_BUILD_REAGENTS: Int = 7064

	val LABS_REGION_ID    = 5521
	val LABS_REGION_PLANE = 0

	val FOUND_GEM = 2655

	val VARBIT_POTION_ORDER   : Seq[Int] = List(VarbitID.MM_LAB_ORDER_1_TYPE, VarbitID.MM_LAB_ORDER_2_TYPE, VarbitID.MM_LAB_ORDER_3_TYPE)//List(11315, 11317, 11319)
	val VARBIT_POTION_MODIFIER: Seq[Int] =  List(VarbitID.MM_LAB_ORDER_1_MODIFIER, VarbitID.MM_LAB_ORDER_2_MODIFIER, VarbitID.MM_LAB_ORDER_3_MODIFIER)//List(11316, 11318, 11320)

	val VARBIT_POTION_ORDER_1: Int     = VARBIT_POTION_ORDER(0)//11315
	val VARBIT_POTION_MODIFIER_1: Int  = VARBIT_POTION_MODIFIER(0)//11316
	val VARBIT_POTION_ORDER_2: Int     = VARBIT_POTION_ORDER(1)//11317
	val VARBIT_POTION_MODIFIER_2: Int  = VARBIT_POTION_MODIFIER(1)//11318
	val VARBIT_POTION_ORDER_3: Int    =  VARBIT_POTION_ORDER(2)//11319
	val VARBIT_POTION_MODIFIER_3: Int =  VARBIT_POTION_MODIFIER(2)//11320

	val VARP_LYE_RESIN: Int  = VarPlayerID.MIXOLOGY_LYE_POINTS//4414
	val VARP_AGA_RESIN : Int = VarPlayerID.MIXOLOGY_AGA_POINTS//4415
	val VARP_MOX_RESIN : Int = VarPlayerID.MIXOLOGY_MOX_POINTS//4416

	val VARBIT_RETORT_PROGRESS: Int   = 11327
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

	val COMPONENT_POTION_ORDERS_GROUP_ID = InterfaceID.MM_OVERLAY
	val COMPONENT_POTION_ORDERS_LAYER = InterfaceID.MmOverlay.UNIVERSE
	val COMPONENT_POTION_ORDERS          = InterfaceID.MmOverlay.CONTENT

	sealed trait SProcessType(val alchemyObject: AlchemyObject) extends enumeratum.EnumEntry {
		this: Product =>
	}
	object SProcessType extends enumeratum.Enum[SProcessType] {
		case object Homogenous extends SProcessType(AlchemyObject.AGITATOR) {}
		case object Concentrated extends SProcessType(AlchemyObject.RETORT) {}
		case object Crystalised extends SProcessType(AlchemyObject.ALEMBIC) {}

		def fromToolBench(to: TileObject): Option[SProcessType] = {
			log.debug("SProcessType fromToolBench: {}", to.getId)
			Option(to.getId - 55389).filter((0 to 2).contains(_)).map(List(Concentrated, Homogenous, Crystalised).apply(_))
		}

		def fromOrderValue(i: Int): Option[SProcessType] = {
			log.debug("SProcessType fromOrderValue: {}", i)
			Option.when( 1 to 3 contains i){List(Homogenous, Concentrated, Crystalised)(i-1)}
		}
		override def values: IndexedSeq[SProcessType] = findValues
	}


	sealed trait SBrew(val unprocessedId: Int, val xp: Int) extends enumeratum.EnumEntry {
		this: Product =>
		val components: Array[PotionComponent] = {
			this.productPrefix.map(c => PotionComponent.fromLetter(c)).toList.toArray
		}

		val recipe: Map[PotionComponent, Int] = {
			this.productPrefix.map(c => PotionComponent.fromLetter(c)).toList.pipe(
				mList => mList.distinct.map(m => m -> mList.count(_ == m))
			).toMap
		}.filter(_._2 > 0)

		val worth: Map[PotionComponent, Int] = recipe.collect {
			case (m, 1) => m -> 10
			case (m, c) if c >= 2 => m -> 20
		}

		def totalWorth: Int = worth.values.sum
		inline def processedId: Int = unprocessedId + 10
	}
	object SBrew extends enumeratum.Enum[SBrew] {
		override def values: IndexedSeq[SBrew] = findValues
		case object MMM extends SBrew(MM_POTION_MMM_UNFINISHED, 190) {}
		case object MMA extends SBrew(MM_POTION_MMA_UNFINISHED, 215) {}
		case object MML extends SBrew(MM_POTION_MML_UNFINISHED, 240) {}
		case object AAA extends SBrew(MM_POTION_AAA_UNFINISHED, 190) {}
		case object ALA extends SBrew(MM_POTION_AAL_UNFINISHED, 290) {}
		case object AAM extends SBrew(MM_POTION_AAM_UNFINISHED, 265) {}
		case object LLL extends SBrew(MM_POTION_LLL_UNFINISHED, 190) {}
		case object ALL extends SBrew(MM_POTION_LLA_UNFINISHED, 340) {}
		case object MLL extends SBrew(MM_POTION_LLM_UNFINISHED, 315) {}
		case object MAL extends SBrew(MM_POTION_MAL_UNFINISHED, 365) {}
//		inline def values: List[SBrew] = List(MMM, MMA, MML, AAA, ALA, AAM, LLL, ALL, MLL, MAL)
		def fromItemId(id: Int): Option[SBrew] = values.find(b => b.processedId == id || b.unprocessedId == id)
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
		def fromIdx(i: Int): Option[SBrew] = {
			log.debug("SBrew fromOrderValue: {}", i)
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

	class Order(val originalIdx: Int, val mod: SProcessType, val brew: SBrew) {
		private var fullfilled: Boolean = false
		def isFulfilled: Boolean = fullfilled
		def setFulfilled(b: Boolean): Unit = fullfilled = b
	}

	case class GoalComponent(curAmount: Int, baseGoalAmount: Int, rewardQty: Int) {
		assert(curAmount >= 0 && baseGoalAmount > 0 && rewardQty >= 0)
		val goalAmount: Int = baseGoalAmount * rewardQty
		lazy val affordableAmount: Int    = {
			curAmount / baseGoalAmount
		}
		lazy val percentageToGoal: Double = {
			if (goalAmount > 0) Math.min(curAmount.toDouble / goalAmount, 1.0d) else 1.0d
		}
	}

	class Goal(config: FredsMixologyConfig)(using client: Client) {
		val rewardItem: RewardItem = config.selectedReward()
		val rewardQty: Int = if(rewardItem.isRepeatable) config.rewardQuantity() else 1

		val componentMap: Map[PotionComponent, GoalComponent] =  (for {
			c <- PotionComponent.values()
			curAmount = client.getVarpValue(c.resinVarpId())
			baseAmount = rewardItem.componentCost(c)
		} yield c -> GoalComponent(curAmount, baseAmount, rewardQty)).toMap

		val itemsAffordable: Int = componentMap.toList.map(_._2.affordableAmount)
			.min
			.pipe(Math.min(_, rewardQty))

		val overallProgress : Double = componentMap.toList.map(_._2.percentageToGoal)
			.pipe(x => x.sum / x.length)
	}
}
