package com.fredplugins.gearSwapper

import net.runelite.client.config.Keybind

case class ExportableItem(enabled: Boolean, itemsString: String, hotkey: Keybind, equipFirstItem: Boolean, toggleSpecOnActivation: Boolean, specThreshold: Int)
class ExportableConfig {
	val swaps: Array[ExportableItem] = new Array[ExportableItem](6)
	def setSwap(index: Int, swapEnabled: Boolean, swapString: String, swapHotkey: Keybind, equipFirstItem: Boolean, toggleSpecOnActivation: Boolean, specThreshold: Int): Unit = {
		this.swaps(index) = ExportableItem(swapEnabled, swapString, swapHotkey, equipFirstItem, toggleSpecOnActivation, specThreshold)
	}

	def getSwap(i: Int): ExportableItem = this.swaps(i)

	def getSwapEnabled: Array[Boolean] = this.swaps.map(_.enabled)
	def getSwapString: Array[String] = this.swaps.map(_.itemsString)
	def getSwapHotkey: Array[Keybind] = this.swaps.map(_.hotkey)
	def getEquipFirstItem: Array[Boolean] = this.swaps.map(_.equipFirstItem)
	def getToggleSpecOnActivation: Array[Boolean] = this.swaps.map(_.toggleSpecOnActivation)
	def getSpecThreshold: Array[Int] = this.swaps.map(_.specThreshold)
}