package com.fredplugins.superClickHelper

import com.fredplugins.common.{SceneUtils, ShimUtils}
import net.runelite.api.{Client, MenuEntry, NPC, Player, TileObject}
import net.runelite.api.events.{MenuEntryAdded, MenuOptionClicked}
import org.slf4j.Logger

sealed trait MenuEntryTarget {}
object MenuEntryTarget {
	import com.fredplugins.common.MenuExtensions._
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	case class TileObjectTarget(tileObject: TileObject) extends MenuEntryTarget
	case class NpcTarget(npc: NPC) extends MenuEntryTarget
	case class PlayerTarget(player: Player) extends MenuEntryTarget
	case class WidgetTargetOn(w: Int) extends MenuEntryTarget

	private def transformPF(using client: Client): PartialFunction[MenuEntry, Option[MenuEntryTarget]] = {
		case me if me.isTileObjectAction => SceneUtils.findTileObject(me.getParam0, me.getParam1, me.getIdentifier).map(to => TileObjectTarget(to))//.TileObjectTarget(me.getParam0, me.getParam1, me.getIdentifier)
		case me if me.isNpcAction => SceneUtils.findNpc(me.getIdentifier).map(npc => NpcTarget(npc))
		case me if me.isPlayerAction => SceneUtils.findPlayer(me.getIdentifier).map(player => PlayerTarget(player))
//		case me if me.isWidgetTargetOnAction => /*PlayerTarget(me.getParam0, me.getParam1, me.getIdentifier)*/
//		case me if me.isItemAction =>
	}
	def apply(menuEntry: MenuEntryAdded)(using client: Client): Option[MenuEntryTarget] = {
		val possibleResult = transformPF.lift.apply(menuEntry.getMenuEntry).flatten
		if(possibleResult.isEmpty) {
//			log.debug(s"Cant convert {} to MenuEntryTarget", menuEntry)
		}
		possibleResult
	}
	def apply(menuEntry: MenuOptionClicked)(using client: Client): Option[MenuEntryTarget] = {
		val possibleResult = transformPF.lift.apply(menuEntry.getMenuEntry).flatten
		if (possibleResult.isEmpty) {
			log.debug(s"Cant convert {} to MenuEntryTarget", menuEntry)
		}
		possibleResult
	}
}
