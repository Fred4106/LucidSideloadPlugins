package com.fredplugins.alchblocker

import com.fredplugins.alchblocker.FredsAlchBlockerConfig.ListType
import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.Client
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.gameval.{InterfaceID}
import net.runelite.client.callback.ClientThread
import net.runelite.client.util.Text

import java.util.stream.Collectors
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class SAlchUtils(client: Client, clientThread: ClientThread, plugin: FredsAlchBlockerPlugin) extends ShimUtils.Logging("DEBUG") {
	def postMenuSortJava(blockedItems: java.util.Set[Integer]): Unit = {
		blockedItems.stream().asJavaPrimitiveStream.toScala(Set).pipe(postMenuSort)
	}

	private def postMenuSort(blockedItems: Set[Int]): Unit = {
		clientThread.invokeAtTickEnd(new Runnable {
			override def run(): Unit = {
				val originalEntries = client.getMenu.getMenuEntries.toList
				val retainedMenuEntries = originalEntries.filter(e => {
					val menuTarget = Text.removeTags(e.getTarget).replace('\u00A0', ' ').trim
					val menuOption = Text.removeTags(e.getOption).replace('\u00A0', ' ').trim

					Option.when(e.getType == MenuAction.WIDGET_TARGET_ON_WIDGET && menuOption.equals("Cast") && menuTarget.stripPrefix("High").stripPrefix("Low").trim.startsWith("Level Alchemy") &&
							e.getParam0 >= 0 && e.getParam0 < 28 && e.getParam1 == InterfaceID.Inventory.ITEMS
						)(client.getWidget(InterfaceID.Inventory.ITEMS).getChild(e.getParam0))
						.orElse(
							Option.when(e.getType == MenuAction.CC_OP && menuOption.endsWith("-Alchemy") && e.getIdentifier == 1)(e.getWidget)
						)
						.filter(_.getItemId != -1)
						.forall(
							_.getItemId.pipe(blockedItems.contains) != (plugin.config.listType == ListType.BLACKLIST)
						)
				})
				val removedEntries = originalEntries.filterNot(retainedMenuEntries.contains(_))
				if(removedEntries.nonEmpty) {
					log.debug("Removed {} entries from menu on tick {}", removedEntries.length, client.getTickCount)
					client.getMenu.setMenuEntries(retainedMenuEntries.toArray[MenuEntry])
				}
			}
		})
	}
}
