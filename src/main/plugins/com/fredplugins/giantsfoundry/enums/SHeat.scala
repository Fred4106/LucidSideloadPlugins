package com.fredplugins.giantsfoundry.enums

import net.runelite.client.ui.ColorScheme

import java.awt.Color

enum SHeat(name: String, cs: Color) {
	case Low extends SHeat("Low", ColorScheme.PROGRESS_COMPLETE_COLOR)
	case Med extends SHeat("Medium", ColorScheme.PROGRESS_INPROGRESS_COLOR)
	case High extends SHeat("High", ColorScheme.PROGRESS_ERROR_COLOR)
	case None extends SHeat("Not in range", ColorScheme.LIGHT_GRAY_COLOR)
}
