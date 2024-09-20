package com.fredplugins.common.extensions

import com.fredplugins.common.utils.SceneUtils
import net.runelite.api.MenuAction.*
import net.runelite.api.coords.WorldPoint
import net.runelite.api.*

object MenuExtensions {
	private val tileObjectActions: List[MenuAction] = List(GAME_OBJECT_FIRST_OPTION, GAME_OBJECT_SECOND_OPTION, GAME_OBJECT_THIRD_OPTION, GAME_OBJECT_FOURTH_OPTION, GAME_OBJECT_FIFTH_OPTION, EXAMINE_OBJECT)
	private val npcActions: List[MenuAction] = List(NPC_FIRST_OPTION, NPC_SECOND_OPTION, NPC_THIRD_OPTION, NPC_FOURTH_OPTION, NPC_FIFTH_OPTION, EXAMINE_NPC)
	private val playerActions: List[MenuAction] = List(PLAYER_FIRST_OPTION, PLAYER_SECOND_OPTION, PLAYER_THIRD_OPTION, PLAYER_FOURTH_OPTION, PLAYER_FIFTH_OPTION, PLAYER_SIXTH_OPTION, PLAYER_SEVENTH_OPTION, PLAYER_EIGHTH_OPTION)
	private val widgetTargetOnActions: List[MenuAction] = List(WIDGET_TARGET_ON_PLAYER, WIDGET_TARGET_ON_NPC, WIDGET_TARGET_ON_GAME_OBJECT)

	extension (e: MenuEntry) {
		def isTileObjectAction: Boolean = tileObjectActions.contains(e.getType)
		def isNpcAction: Boolean = npcActions.contains(e.getType)
		def isPlayerAction: Boolean = playerActions.contains(e.getType)
		def isWidgetTargetOnAction: Boolean = widgetTargetOnActions.contains(e.getType)
		def isRuneliteAction: Boolean = e.getType.getId >= RUNELITE.getId

		def getWorldLocation(using c: Client): WorldPoint = {
			getWorldLocationOpt.get
		}
		def getWorldLocationOpt(using c: Client): Option[WorldPoint] = {
			Option.when((isTileObjectAction) && !(e.getParam0 < 0 || e.getParam1 < 0 || e.getParam0 >= 104 || e.getParam1 >= 104)) {
				val wv     = c.getTopLevelWorldView
				val (x, y) = (wv.getBaseX + e.getParam0, wv.getBaseY + e.getParam1)
				new WorldPoint(x, y, wv.getPlane)
			}
		}
		def getTileObjectOpt(using client: Client): Option[TileObject] = {
			Option.when(isTileObjectAction) {
				SceneUtils.findTileObject(e.getParam0, e.getParam1, e.getIdentifier)
			}.flatten
		}
		def getNpcOpt(using client: Client): Option[NPC] = {
			Option.when(isNpcAction) {
				SceneUtils.findNpc(e.getIdentifier)
			}.flatten
		}
	}
}
