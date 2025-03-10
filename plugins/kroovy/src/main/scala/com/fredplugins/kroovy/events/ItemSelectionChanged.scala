package com.fredplugins.kroovy.events

import com.fredplugins.kroovy.managers.InventoryManager
import net.runelite.api.{Item, ItemContainer}

sealed trait SlottedItem {//def getIdx: Int
}

object SlottedItem {
	case object Empty extends SlottedItem {
//		override def getIdx: Int = -1
	}
	case class SlotItem(item: Item, idx: Int) extends SlottedItem {
//		override def getIdx: Int = idx//: Option[Item] = Option(Item(id, qty))
	}

	def apply(container: ItemContainer, slot: Int): SlottedItem = {
		if(container == null || container.size() < slot || slot == -1) Empty
		else SlotItem(container.getItem(slot), slot)
	}
}
case class ItemSelectionChanged(from: SlottedItem, to: SlottedItem) {

}
