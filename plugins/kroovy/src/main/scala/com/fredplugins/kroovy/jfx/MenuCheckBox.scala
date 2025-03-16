package com.fredplugins.kroovy.jfx

import javafx.event.ActionEvent
import javafx.scene.control.{CheckBox, CustomMenuItem, Menu}
import javafx.scene.paint.Paint
import net.runelite.client.config.ConfigManager

object MenuCheckBox {
	def apply(label: String, configString: String, configManager: ConfigManager,
		parentMenu: Menu, selectedByDefault: Boolean = false): MenuCheckBox = {
		val checkBox = new CheckBox(label)
		checkBox.setTextFill(Paint.valueOf("black"))
		new MenuCheckBox(checkBox, configString, configManager, parentMenu, selectedByDefault)
	}
}

class MenuCheckBox(val checkBox: CheckBox, val configString: String, val configManager: ConfigManager,
	parentMenu: Menu, val selectedByDefault: Boolean) extends
	CustomMenuItem(checkBox, false) {
	var onEvent: () => Unit = null
	loadFromConfig()
	checkBox.setOnAction((ae: ActionEvent) => {
		saveToConfig()
		if (onEvent != null) onEvent()
	})
	parentMenu.getItems.add(this)
	def isSelected: Boolean = checkBox.isSelected

	def doIfSelected(action: () => Unit): Unit = if (isSelected) action()

	def setSelected(selected: Boolean): Unit = {
		checkBox.setSelected(selected)
		saveToConfig()
	}

	def loadFromConfig(): Unit = {
		setSelected(Option[Boolean](configManager.getConfiguration("kroovy2", configString, classOf[Boolean]))
			.getOrElse(selectedByDefault))
	}

	def saveToConfig(): Unit = {
		configManager.setConfiguration("kroovy2", configString, isSelected)
	}
}