package com.fredplugins.giantsfoundry.enums

import com.fredplugins.giantsfoundry.enums.SCommissionType._
import com.fredplugins.giantsfoundry.enums.SMould.{BladeMould, ForteMould, SMouldTrait, TipMould}
import net.runelite.api.Client
import net.runelite.api.widgets.Widget
import org.slf4j.Logger

case class SMould(forte: Option[ForteMould], bladeMould: Option[BladeMould], tipMould: Option[TipMould]) {
	private val internal: List[SMouldTrait] = List(forte, bladeMould, tipMould).flatten
	val values: Map[SCommissionType, Int] = SCommissionType.values.map(t => t -> internal.map(_.value(t)).sum).filterNot(_._2 == 0).toMap
	def value(t: SCommissionType): Int = values.getOrElse(t, 0)//internal.map(_.value(t)).sum
//	def values: Map[String, Int] = List("broad" -> broad, " narrow" -> narrow, "heavy" -> heavy, "light" -> light,"spiked" -> spiked, "flat" -> flat).toMap
	def getScore(t1: SCommissionType, t2: SCommissionType): Int = value(t1) + value(t2)
}
object SMould {
	sealed trait SMouldTrait(val name: String, v: (SCommissionType, Int) *) {
		private val values: Map[SCommissionType, Int] = v.toMap
		def value(t: SCommissionType): Int = values.getOrElse(t, 0)
	}
	enum ForteMould(n: String, v: (SCommissionType, Int) *) extends SMouldTrait(n, v*) {
		case Spiker extends ForteMould("Spiker!", Narrow -> 1, Heavy -> 2, Spiked -> 22)
		case ChopperForte1 extends ForteMould("Chopper Forte +1", Broad -> 3, Light -> 4, Flat -> 18)
		case JuggernautForte extends ForteMould("Juggernaut Forte", Broad -> 4, Heavy -> 4, Spiked -> 16)
		case DefenderBase extends ForteMould("Defender Base", Broad -> 8, Heavy -> 10, Flat -> 8)
		case StilettoForte extends ForteMould("Stiletto Forte", Narrow -> 8, Light -> 10, Flat -> 8)
		case SerratedForte extends ForteMould("Serrated Forte", Narrow -> 8, Heavy -> 8, Spiked -> 6)
		case SerpentRicasso extends ForteMould("Serpent Ricasso", Narrow -> 6, Light -> 8, Flat -> 8)
		case MedusaRicasso extends ForteMould("Medusa Ricasso", Broad -> 8, Heavy -> 6, Flat -> 8)
		case DisarmingForte extends ForteMould("Disarming Forte", Narrow -> 4, Light -> 4, Spiked -> 4)
		case GaldiusRicasso extends ForteMould("Galdius Ricasso", Broad -> 4, Heavy -> 4, Flat -> 4)
		case ChopperForte extends ForteMould("Chopper Forte", Broad -> 4, Light -> 4, Flat -> 4)
	}
	enum BladeMould(n: String, v: (SCommissionType, Int) *) extends SMouldTrait(n, v*) {
		case Choppa extends BladeMould("Choppa!", Broad -> 1, Light -> 22, Flat -> 2)
		case FleurDeBlade extends BladeMould("Fleur de Blade", Broad -> 4, Heavy -> 18,    Spiked -> 1)
		case ClaymoreBlade extends BladeMould("Claymore Blade", Broad -> 16, Heavy -> 4, Flat -> 4)
		case SerpentBlade extends BladeMould("Serpent Blade", Narrow -> 10, Light -> 8, Flat -> 8)
		case FlambergeBlade extends BladeMould("Flamberge Blade", Narrow -> 8, Light -> 8,    Spiked -> 10)
		case GladiusEdge extends BladeMould("Gladius Edge", Narrow -> 6, Heavy -> 8, Flat -> 8)
		case StilettoBlade extends BladeMould("Stiletto Blade", Narrow -> 8, Light -> 6, Flat -> 8)
		case MedusaBlade extends BladeMould("Medusa Blade", Broad -> 8, Heavy -> 8, Flat -> 6)
		case FishBlade extends BladeMould("Fish Blade", Narrow -> 4, Light -> 4, Flat -> 4)
		case DefendersEdge extends BladeMould("Defenders Edge", Broad -> 4, Heavy -> 4,    Spiked -> 4)
		case SawBlade extends BladeMould("Saw Blade", Broad -> 4, Light -> 4,    Spiked -> 4)
	}
	enum TipMould(n: String, v: (SCommissionType, Int) *) extends SMouldTrait(n, v*) {
		case ThePoint extends TipMould("The Point!", Broad -> 2,Light -> 1, Flat -> 22)
		case NeedlePoint extends TipMould("Needle Point", Narrow -> 18, Light -> 3, Flat -> 4)
		case SerratedTip extends TipMould("Serrated Tip", Narrow -> 4, Light -> 16,Spiked -> 4)
		case DefendersTip extends TipMould("Defenders Tip", Broad -> 10, Heavy -> 8, Spiked -> 8)
		case CorruptedPoint extends TipMould("Corrupted Point", Narrow -> 8, Light -> 10, Spiked -> 8)
		case SawTip extends TipMould("Saw Tip", Broad -> 6, Heavy -> 8, Spiked -> 8)
		case GladiusPoint extends TipMould("Gladius Point", Narrow -> 8, Heavy -> 8, Flat -> 6)
		case SerpentsFang extends TipMould("Serpent's Fang", Narrow -> 8, Light -> 6, Spiked -> 8)
		case MedusasHead extends TipMould("Medusa's Head", Broad -> 4, Heavy -> 4, Spiked -> 4)
		case ChopperTip extends TipMould("Chopper Tip", Broad -> 4, Light -> 4, Spiked -> 4)
		case PeoplePokerPoint extends TipMould("People Poker Point", Narrow -> 4, Heavy -> 4, Flat -> 4)
	}

	val values: Array[SMouldTrait] = List(ForteMould.values, BladeMould.values, TipMould.values).flatMap(_.toList).toArray
	def forName(s: String): Option[SMouldTrait] = {
		values.find(_.name.equalsIgnoreCase(s))
	}

	def getSelected[T <: SMouldTrait](client: Client, varbit: Int, values: Array[T]): Option[T] = {
		Option(client.getVarbitValue(varbit)).filterNot(_ == 0).map(i => values.apply(i - 1))
	}

	def getOptions(client: Client): List[(SMouldTrait, Widget)] = {
		Option(client.getWidget(47054857))
			.flatMap(p => Option(p.getChildren))
			.map(
				_.drop(2).sliding(1, 17).flatten.toList
				 .filterNot(w => w.getTextColor == 0x9f9f9f)
				 .flatMap(w => forName(w.getText).map(v => v->w))
			)
			.getOrElse(List.empty)
//		filteredWidgets.map(fw => {
//			fw.flatMap(w => forName(w.getText).map(v => v -> w)).toMap
//		}).getOrElse(Map.empty)
	}

	def getSelected(client: Client): SMould = {
		SMould(
			getSelected(client, 13910, ForteMould.values),
			getSelected(client, 13911, BladeMould.values),
			getSelected(client, 13912, TipMould.values)
		)
	}

	def selectBest(client: Client): Option[Widget] = {
		SCommissionType
			.forVarbit(client.getVarbitValue(13907))
			.zip(
				SCommissionType.forVarbit(client.getVarbitValue(13908))
			)
			.map((tpe) => {
				val sorted = getOptions(client).maxBy((m, w) => m.value(tpe._1) + m.value(tpe._2))
//				sorted.foreach(s =>
//					println(s)
//				)
				println(sorted)
				sorted
			}).map(_._2)
	}

	def debug(log: Logger, client: Client): Unit = {
		log.info("options={}", getOptions(client).map(_._1))
	}
}
