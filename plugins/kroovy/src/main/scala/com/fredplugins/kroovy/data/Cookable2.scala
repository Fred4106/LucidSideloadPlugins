package com.fredplugins.kroovy.data

import com.fredplugins.kroovy.ActionEnum
import com.fredplugins.kroovy.managers.InventoryManager

import scala.jdk.StreamConverters.*
import scala.jdk.javaapi.StreamConverters
import scala.util.chaining.*
case class InternalData(cooked: Int, raw: Int, timings: Seq[Int]) {}
object InternalData {
	def unapply(arg: InternalData): ((Int, Int), Seq[Int]) = (arg.cooked, arg.raw) -> arg.timings

	given Ordering[Seq[Int]] {
		override def compare(x: Seq[Int], y: Seq[Int]): Int = {
			val maxTimeLength = math.max(x.length, y.length)

			def helper(ix: Int): Seq[Int] => Int = s => {
				if (ix < s.length) s(ix) else s.last
			}

			(for {
				i <- 0 until maxTimeLength
				(xi, yi) = (helper(i)).pipe(hi => (hi(x), hi(y)))
			} yield (xi, yi)).flatMap((u1, u2) => Option.when(u1 != u2)(u1 - u2))
				.headOption.getOrElse(0)
		}
	}
	given Ordering[InternalData] = {
		Seq(
			Ordering.by[InternalData, Int] { a => a.cooked },
			Ordering.by[InternalData, Int] { a => a.raw },
			Ordering.by[InternalData, Int] { a => a.timings.length },
			Ordering.by[InternalData, Seq[Int]] { a => a.timings }
		).reduce(_.orElse(_))
	}
}
object Cookable2 {
	import net.runelite.api.ItemID as Items
	private type IntOrIdsTpe = Int | Array[Int]
	private object IDsPair {
		inline transparent def helper(inline in: IntOrIdsTpe *) = {
			in.map {
				case i: Int => Array(i)//Set(i)
				case i: Array[Int] => i//java.util.stream.Stream.of(i.build() *)
			}.reduce((a, b) => a ++ b).pipe(j => j)
		}
//		def apply(raw: IntOrIdsTpe)(cooked: IntOrIdsTpe): (Set[Int], Set[Int]) = {
////			val cv = helper(raw *), helper(cooked *)
////			val  = helper(cooked *)
//			(helper(Seq(cooked)), helper(Seq(raw)))
//		}
		def apply(raw: IntOrIdsTpe *)(cooked: IntOrIdsTpe *): (Array[Int], Array[Int]) = {
//			val cv = helper(raw *), helper(cooked *)
//			val  = helper(cooked *)
			(helper(cooked *), helper(raw *))
		}
//		def matches(productId: Int)(pair: IDsPair): Boolean = pair.cooked.contains(productId)
	}

	object IDsSet {
		def apply(pairs: (Array[Int], Array[Int]) *)(action: ActionEnum): Seq[InternalData] = {
//			val t: Seq[Int] = action.getTickTimes.toSeq
			val data = for {
				p <- pairs
				cookedId: Int <- p._1
				rawId: Int <- p._2
			} yield (cookedId, rawId)

			data.map(d => InternalData(d._1, d._2, action.getTickTimes.toSeq))
		}
	}

	val fishIdSet: Seq[InternalData] =  IDsSet(
		IDsPair(Items.RAW_SARDINE)(Items.SARDINE),
		IDsPair(Items.RAW_SALMON)(Items.SALMON),
		IDsPair(Items.RAW_TROUT)(Items.TROUT),
		IDsPair(Items.RAW_COD)(Items.COD),
		IDsPair(Items.RAW_HERRING)(Items.HERRING),
		IDsPair(Items.RAW_PIKE)(Items.PIKE),
		IDsPair(Items.RAW_MACKEREL)(Items.MACKEREL),
		IDsPair(Items.RAW_TUNA)(Items.TUNA),
		IDsPair(Items.RAW_BASS)(Items.BASS),
		IDsPair(Items.RAW_SWORDFISH)(Items.SWORDFISH),
		IDsPair(Items.RAW_LOBSTER)(Items.LOBSTER),
		IDsPair(Items.RAW_SHARK)(Items.SHARK),
		IDsPair(Items.RAW_LAVA_EEL)(Items.LAVA_EEL),
		IDsPair(Items.RAW_MANTA_RAY)(Items.MANTA_RAY),
		IDsPair(Items.RAW_MONKFISH)(Items.MONKFISH),
		IDsPair(Items.RAW_DARK_CRAB)(Items.DARK_CRAB),
		IDsPair(Items.RAW_ANGLERFISH)(Items.ANGLERFISH),
		IDsPair(Items.RAW_KARAMBWAN)(Items.COOKED_KARAMBWAN, Items.POISON_KARAMBWAN),
		IDsPair(Items.RAW_SLIMY_EEL)(Items.COOKED_SLIMY_EEL),
		IDsPair(Items.RAW_RAINBOW_FISH)(Items.RAINBOW_FISH)
	)(ActionEnum.COOKING_14)
	val pieIdSet =  IDsSet(
		IDsPair(Items.UNCOOKED_APPLE_PIE)(Items.APPLE_PIE),
		IDsPair(Items.UNCOOKED_BERRY_PIE)(Items.REDBERRY_PIE),
		IDsPair(Items.UNCOOKED_MEAT_PIE)(Items.MEAT_PIE),
		IDsPair(Items.UNCOOKED_BOTANICAL_PIE)(Items.BOTANICAL_PIE),
		IDsPair(Items.UNCOOKED_MUSHROOM_PIE)(Items.MUSHROOM_PIE),
		IDsPair(Items.UNCOOKED_DRAGONFRUIT_PIE)(Items.DRAGONFRUIT_PIE),
		IDsPair(Items.RAW_ADMIRAL_PIE)(Items.ADMIRAL_PIE),
		IDsPair(Items.RAW_FISH_PIE)(Items.FISH_PIE),
		IDsPair(Items.RAW_GARDEN_PIE)(Items.GARDEN_PIE),
		IDsPair(Items.RAW_SUMMER_PIE)(Items.SUMMER_PIE),
		IDsPair(Items.RAW_WILD_PIE)(Items.WILD_PIE)
	)(ActionEnum.COOKING_14)
	val oddballSet = IDsSet(
		IDsPair(Items.THIN_SNAIL)(Items.THIN_SNAIL_MEAT),
		IDsPair(Items.LEAN_SNAIL)(Items.LEAN_SNAIL_MEAT),
		IDsPair(Items.FAT_SNAIL)(Items.FAT_SNAIL_MEAT),
		IDsPair(Items.RAW_CHICKEN)(Items.COOKED_CHICKEN),
		IDsPair(Items.RAW_CHICKEN_4289)(Items.COOKED_CHICKEN_4291),
		IDsPair(Items.RAW_RABBIT)(Items.COOKED_RABBIT),
		IDsPair(Items.UNCOOKED_PIZZA)(Items.PLAIN_PIZZA),
		IDsPair(Items.RAW_HARPOONFISH)(Items.HARPOONFISH)
	)(ActionEnum.COOKING_14)
	val cooking_134Map = IDsSet(
		IDsPair(Items.POTATO)(Items.BAKED_POTATO),
		IDsPair(Items.SWEETCORN)(Items.COOKED_SWEETCORN),
		IDsPair(Items.RAW_BEEF, Items.RAW_BEEF_4287, Items.RAW_RAT_MEAT, Items.RAW_BEAR_MEAT, Items.RAW_YAK_MEAT)(Items.COOKED_MEAT),
	)(ActionEnum.COOKING_134)

	val skewerMap = IDsSet(
		IDsPair(Items.SKEWERED_BIRD_MEAT)(Items.ROAST_BIRD_MEAT),
		IDsPair(Items.SKEWERED_BEAST)(Items.ROAST_BEAST_MEAT),
		IDsPair(Items.SKEWERED_CHOMPY)(Items.COOKED_CHOMPY),
		IDsPair(Items.SKEWERED_RABBIT)(Items.ROAST_RABBIT)
	).apply(ActionEnum.COOKING_3)


	val reducer: (Seq[InternalData], Seq[InternalData]) => Seq[InternalData] = (a, b)  => {
		(a ++ b).sorted
	}
	val values: Seq[InternalData] = Seq(skewerMap, cooking_134Map, oddballSet ,pieIdSet, fishIdSet).reduce(reducer)

	def filter(p: Int)(using inventoryManager: InventoryManager): Seq[(Int, (Int, Int), Seq[Int])] = {
		values.filter(_.cooked == p).groupMap {
			c => c.raw
		}{ c => c.timings }.flatMap{
			case (rid, tsh :: Nil) => Option((rid/*Int.box(rid)*/ -> tsh/*tsh.map(Int.box(_)).asJava*/))
			case (rid, umm) => umm.map(u => rid -> u)
		}.toSeq.flatMap {
			case (i, ints) => Option((p, (i, inventoryManager.getItemCountById(i)), ints)).filter(_._2._2 > 0)
//			case (a, b) => None
		}
		//		values.map(_.internalMap
//		values.flatMap(ids => ids.rawIds(p).zip(Option(ids.action))).flatMa	p(x=>x._1.map(_ -> x._2)).groupMap(_._1)(_._2).map(u => Int.box(u._1) -> u._2.toSet.asJava).asJava
	}

	def findMakeAmount(p: Int, inventoryManager: InventoryManager): (Int, Seq[Int]) = {
		given InventoryManager = inventoryManager
		val result = filter(p).groupMap(e => e._1 -> e._3)(e => e._2)
		val result2 = result.map {
			case ((productId, timing), rawInputs) => (productId, timing, rawInputs.map(ri => ri._2).sum)
		}
		result2.filter(_._1 == p).groupMap(e => (e._1 -> e._2))(_._3).map{
			case ((productId, tickTiming), raw) => ((productId, tickTiming), raw.sum)
		}.toList.find(_._1._1 == p).map(e => e._2 -> e._1._2)
			.orNull
	}
}