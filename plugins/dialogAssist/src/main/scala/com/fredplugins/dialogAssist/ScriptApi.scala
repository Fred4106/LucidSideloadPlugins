package com.fredplugins.dialogAssist

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object ScriptApi {
	case class Script(tpe: ScriptType, name: String) {
		def fullName: String = s"[${tpe.getFrag},${name}]"
	}

	sealed trait ScriptDefinition extends enumeratum.EnumEntry {
		val tpe: ScriptType
		lazy val scriptHeader: Script =  Script(tpe, this.getClass.getSimpleName.stripSuffix("$"))
		def intArgs: Seq[String]
		def objArgs: Seq[String]
		def intReturns: Seq[String]
		def objReturns: Seq[String]
	}
	sealed trait ClientDef(override val intArgs: Seq[String] = Seq.empty, override val objArgs: Seq[String]=  Seq.empty) extends ScriptDefinition {
		val tpe: ScriptType = ScriptType.CLIENT
		override val intReturns: Seq[String] = Seq.empty
		override val objReturns: Seq[String] = Seq.empty
	}
	sealed trait ProcDef(override val intArgs: Seq[String] = Seq.empty, override val objArgs: Seq[String]= Seq.empty, override val intReturns: Seq[String] = Seq.empty, override val objReturns: Seq[String] = Seq.empty) extends ScriptDefinition {
		val tpe: ScriptType = ScriptType.PROCEDURAL

	}
	object ScriptDefinitions extends enumeratum.Enum[ScriptDefinition] {
//		[clientscript,skillmulti_itembutton_key](char $char0, int $key1, obj $obj2, component $component3, string $string0, int $key4, int $int5, int $int6, int $int7)
		case object skillmulti_itembutton_key        extends ClientDef(Seq("eventChar", "eventKey", "itemId", "widgetId", "key", "op", "width", "height"), Seq("string0"))
		case object skillmulti_itembutton_op         extends ClientDef(Seq("menu_op", "itemId", "widgetId", "key", "op", "width", "height"), Seq("string0"))
		case object skillmulti_setup                 extends ClientDef(Seq("int0", "qty1", "id_a", "id_b", "id_c", "id_d", "id_e", "id_f", "id_g", "id_h", "id_i", "id_j", "id_k", "id_l", "id_m", "id_n", "id_o", "id_p", "id_q", "id_r", "qty2"), Seq("labelsStr"))
		case object skillmulti_itembutton_triggered  extends   ProcDef(Seq("itemId", "widgetId", "key", "op", "width", "height"), Seq("string0"))
//		case object skillmulti_wipebutton            extends   ProcDef(Seq("widgetId"))
//		case object deltooltip_action                extends   ProcDef(Seq("widgetId"))
//		case object on_enhanced_any                  extends   ProcDef()  has a return boolean
//		case object on_enhanced_desktop              extends   ProcDef()
//		case object on_enhanced_mobile               extends   ProcDef()
//		case object script632                        extends   ProcDef(objArgs = Seq("source"), objReturns = Seq("p1", "remainder"))
//		case object text_device                      extends   ProcDef(objArgs = Seq("desktopStr", "mobileStr"), objReturns = Seq("localized"))
//		case object on_mobile                        extends   ProcDef(intReturns =  Seq("isMobile"))
//		case object min                              extends   ProcDef(intArgs = Seq("v1", "v2"), intReturns = Seq("vMin"))
//		case object max                              extends   ProcDef(intArgs = Seq("v1", "v2"), intReturns = Seq("vMax"))
//		case object skillmulti_quantitybuttons_set   extends   ProcDef(intArgs = Seq("int0", "qty1"))
//		case object skillmulti_quantitybutton_setup  extends   ProcDef(intArgs = Seq("visible", "qty", "widgetId","i3", "i4", "i5"), intReturns = Seq("str?"))

//		case object skillmulti_quantitybutton_draw   extends   ProcDef()
//		case object beige_stone_button_out           extends   ProcDef()
//		case object create_graphic                   extends   ProcDef()
//		case object create_graphic_core              extends   ProcDef()
//		case object beige_stone_button_in            extends   ProcDef()

		case object skillmulti_itembutton_init       extends   ProcDef(intArgs = Seq("keyIdx", "itemId", "widgetId", "key", "opId", "width", "height"), objArgs = Seq("str?"))
//		case object skillmulti_itembutton_draw       extends   ProcDef(intArgs = Seq("visible", "itemId", "widgetId", "width", "height"))


		override def values: IndexedSeq[ScriptDefinition] = findValues
	}

	def getScriptDefinition(s: Script): Option[ScriptDefinition] = {
		ScriptDefinitions.values.find(_.scriptHeader == s)
	}
}
