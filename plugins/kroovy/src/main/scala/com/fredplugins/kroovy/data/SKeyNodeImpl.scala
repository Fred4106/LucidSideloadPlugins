package com.fredplugins.kroovy.data

import com.fredplugins.kroovy.jfx.SKeyNode
import net.runelite.client.config.{Keybind, ModifierlessKeybind}
import net.runelite.client.util.HotkeyListener


final class SKeyNodeImpl(val pkg: SPackage) extends SKeyNode {
	private val listener = new HotkeyListener(() => new ModifierlessKeybind(keyCode.get(), 0)) {
		override def hotkeyPressed(): Unit = onPress.invoke()

		override def hotkeyReleased(): Unit = onRelease.invoke()
	}

	override def enabledCallback(): Unit = {
			pkg.keyManager.registerKeyListener(listener)
	}

	override def disabledCallback(): Unit = {
			pkg.keyManager.unregisterKeyListener(listener)
	}
}