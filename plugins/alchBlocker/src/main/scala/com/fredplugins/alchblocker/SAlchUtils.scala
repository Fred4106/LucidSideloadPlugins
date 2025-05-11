package com.fredplugins.alchblocker

import com.fredplugins.alchblocker.FredsAlchBlockerConfig.ListType
import net.runelite.api.Client
import net.runelite.api.Item
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.gameval.InterfaceID.Inventory
import net.runelite.api.gameval.InventoryID
import net.runelite.client.callback.ClientThread
import net.runelite.client.util.Text
import net.runelite.client.util.WildcardMatcher
import packetUtils.WidgetInfoExtended

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class SAlchUtils(client: Client, clientThread: ClientThread, plugin: FredsAlchBlockerPlugin) {
	def postMenuSort(): Unit = {
		clientThread.invokeAtTickEnd(new Runnable {
			override def run(): Unit = {
//				val (added, stock) = menuEntries.partition(e => {
//				e.getType == MenuAction.RUNELITE && (e.getOption.startsWith("Plant") || e.getOption.startsWith("Water"))
//			})
//			val newMenuEntries: List[MenuEntry] = stock.appendedAll(added)
//			client.setMenuEntries(newMenuEntries.toArray[MenuEntry])
				val retainedMenuEntries = client.getMenu.getMenuEntries.toList.filter(e => {
					if(e.getType == MenuAction.WIDGET_TARGET_ON_WIDGET && e.getOption.equals("Cast") && Text.standardize(e.getTarget).stripPrefix("high").stripPrefix("low").trim.startsWith("level alchemy")) {
						if(e.getParam0 >= 0 && e.getParam0 < 28 && e.getParam1 == WidgetInfoExtended.INVENTORY.getId) {
							val invItem = client.getWidget(WidgetInfoExtended.INVENTORY.getId).getChild(e.getParam0)
							val itemName = Text.standardize(invItem.getName())
							if(plugin.config.listType == ListType.BLACKLIST) {
								!plugin.itemList.asScala.toList.exists(blockedItem => WildcardMatcher.matches(blockedItem, itemName))
							} else {
								plugin.itemList.asScala.toList.exists(whitelistItem => WildcardMatcher.matches(whitelistItem, itemName))
							}
						} else {
							false
						}
					} else if(e.getType == MenuAction.CC_OP && e.getOption.endsWith("-Alchemy") && e.getIdentifier == 1) {
						val itemName = Text.standardize(e.getTarget)
						if (plugin.config.listType == ListType.BLACKLIST) {
							!plugin.itemList.asScala.toList.exists(blockedItem => WildcardMatcher.matches(blockedItem, itemName))
						} else {
							plugin.itemList.asScala.toList.exists(whitelistItem => WildcardMatcher.matches(whitelistItem, itemName))
						}
					} else {
						true
					}
				})
				client.getMenu.setMenuEntries(retainedMenuEntries.toArray[MenuEntry])
			}
		})
	}
}
