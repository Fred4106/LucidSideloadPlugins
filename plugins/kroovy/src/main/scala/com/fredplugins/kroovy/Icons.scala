package com.fredplugins.kroovy

import com.fredplugins.kroovy.api.KIcon

import java.awt.Color
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object Icons {
	val ADD_ICON         : KIcon = KIcon.fromKroovy("add_icon")
	val MINUS_ICON         : KIcon = KIcon.fromKroovy("minus_icon")
	val CHECKBOX_ICON    : KIcon = KIcon.fromKroovy("checkbox_icon")
	val CHECKBOX_SELECTED: KIcon = KIcon.fromKroovy("checkbox_selected_icon")
	val CONSOLE_ICON     : KIcon = KIcon.fromKroovy("console_icon")
	val COPY_ICON        : KIcon = KIcon.fromKroovy("copy_icon")
	val DELETE_ICON      : KIcon = KIcon.fromKroovy("delete_icon")

	val EDIT_ICON_ON     : KIcon = KIcon.fromKroovy("edit_icon").recolor(Color.green)
	val EDIT_ICON_OFF    : KIcon = EDIT_ICON_ON.greyscale().luminanceScale(.61f)

	val KEY_DOWN_ICON    : KIcon = KIcon.fromKroovy("key_down_icon")
	val KEY_UP_ICON      : KIcon = KIcon.fromKroovy("key_up_icon")
	val LOAD_ICON        : KIcon = KIcon.fromKroovy("load_icon")
	val LOGO_ICON        : KIcon = KIcon.fromKroovy("logo_icon")
	val FX_LOGO_ICON     : KIcon = LOGO_ICON.recolor(Color.RED)

	val PASTE_ICON       : KIcon = KIcon.fromKroovy("paste_icon")
	val REFRESH_ICON     : KIcon = KIcon.fromKroovy("refresh_icon")
	val RUN_ICON         : KIcon = KIcon.fromKroovy("run_icon")
	val SAVE_ICON        : KIcon = KIcon.fromKroovy("save_icon")
	val SCRATCHPAD_ICON  : KIcon = KIcon.fromKroovy("scratch_pad_icon")

	val SLIDER_ICON_ON   : KIcon = KIcon.fromKroovy("script_on").recolor(Color.green)
	val SLIDER_ICON_OFF  : KIcon = SLIDER_ICON_ON.greyscale().luminanceScale(.61f).flip(horizontal = true, vertical = false)
	val SETTINGS_ICON    : KIcon = KIcon.fromKroovy("settings_icon")
	val STOP_ICON        : KIcon = KIcon.fromKroovy("stop_icon")
}
