package com.fredplugins.giantsfoundry.enums

import net.runelite.api.Client
import net.runelite.client.ui.ColorScheme
import com.fredplugins.giantsfoundry.FredsGiantsFoundryClientIDs.{WIDGET_HEAT_PARENT,WIDGET_MED_HEAT_PARENT}
import java.awt.Color

enum SHeat(val name: String, val cs: Color, offset: Option[Int] = Option.empty[Int]) {
	case Low extends SHeat("Low", ColorScheme.PROGRESS_COMPLETE_COLOR, Some(1))
	case Med extends SHeat("Medium", ColorScheme.PROGRESS_INPROGRESS_COLOR, Some(3))
	case High extends SHeat("High", ColorScheme.PROGRESS_ERROR_COLOR, Some(5))
	case None extends SHeat("Not in range", ColorScheme.LIGHT_GRAY_COLOR)

	def range(ratio: Double): Array[Int] = {
		offset.map(o => {
			Array[Int](((o / 6d - ratio / 2) * 1000).toInt, ((o / 6d + ratio / 2) * 1000).toInt)
		}).getOrElse(Array[Int](0, 1000))
	}
}
object SHeat {
	def getHeatRangeRatio(client: Client): Double = {
		val heatWidget = client.getWidget(WIDGET_HEAT_PARENT)
		val medHeat    = client.getWidget(WIDGET_MED_HEAT_PARENT)
		if (medHeat == null || heatWidget == null) return 0
		medHeat.getWidth / heatWidget.getWidth.toDouble
	}

	def getRangeAt(heat: Int, ratio: Double): SHeat = {
		SHeat.values.filter(sh => heat > sh.range(ratio)(0) && heat < sh.range(ratio)(1)).head
	}
//	def getLowHeatRange: Array[Int] = Array[Int](((1 / 6d - getHeatRangeRatio / 2) * 1000).toInt, ((1 / 6d + getHeatRangeRatio / 2) * 1000).toInt)
//
//	def getMedHeatRange: Array[Int] = Array[Int](((3 / 6d - getHeatRangeRatio / 2) * 1000).toInt, ((3 / 6d + getHeatRangeRatio / 2) * 1000).toInt)
//
//	def getHighHeatRange: Array[Int] = Array[Int](((5 / 6d - getHeatRangeRatio / 2) * 1000).toInt, ((5 / 6d + getHeatRangeRatio / 2) * 1000).toInt)
}