package com.fredplugins.kroovy.jfx.ui.components

import com.fredplugins.kroovy.KroovyPlugin
import com.fredplugins.kroovy.KroovyPlugin.KIcon
import net.runelite.client.util.SwingUtil

import scala.swing.{Dimension, ToggleButton}

class KToggleButton(offIcon: KIcon, onIcon: KIcon, str: String) extends ToggleButton(str) {
	def this(offIcon: KIcon, onIcon: KIcon) = this(offIcon, onIcon, "")
	SwingUtil.removeButtonDecorations (this.peer)
	focusable = false
	selectedIcon = onIcon.icon
	rolloverSelectedIcon = onIcon.icon_selected
	icon = offIcon.icon
	rolloverIcon = offIcon.icon_selected
	preferredSize = new Dimension(25, 25)
}

object KToggleButton {
	def checkbox(str: String): KToggleButton = {
		new KToggleButton(KroovyPlugin.CHECKBOX_ICON, KroovyPlugin.CHECKBOX_SELECTED, str)
	}
}
