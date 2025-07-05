package com.fredplugins.common.extensions

import com.fredplugins.common.utils.SceneUtils
import net.runelite.api.MenuAction.*
import net.runelite.api.coords.WorldPoint
import net.runelite.api.*
import net.runelite.client.util.Text

import java.awt.Color
import scala.util.Try
import scala.util.chaining.*

object MenuExtensions {
	private val tileObjectActions: List[MenuAction] = List(GAME_OBJECT_FIRST_OPTION, GAME_OBJECT_SECOND_OPTION, GAME_OBJECT_THIRD_OPTION, GAME_OBJECT_FOURTH_OPTION, GAME_OBJECT_FIFTH_OPTION, EXAMINE_OBJECT, WIDGET_TARGET_ON_GAME_OBJECT)
	private val npcActions: List[MenuAction] = List(NPC_FIRST_OPTION, NPC_SECOND_OPTION, NPC_THIRD_OPTION, NPC_FOURTH_OPTION, NPC_FIFTH_OPTION, EXAMINE_NPC)
	private val playerActions: List[MenuAction] = List(PLAYER_FIRST_OPTION, PLAYER_SECOND_OPTION, PLAYER_THIRD_OPTION, PLAYER_FOURTH_OPTION, PLAYER_FIFTH_OPTION, PLAYER_SIXTH_OPTION, PLAYER_SEVENTH_OPTION, PLAYER_EIGHTH_OPTION)
	private val widgetTargetOnActions: List[MenuAction] = List(WIDGET_TARGET_ON_PLAYER, WIDGET_TARGET_ON_NPC, WIDGET_TARGET_ON_GAME_OBJECT, WIDGET_TARGET_ON_WIDGET)
	private val widgetActions: List[MenuAction] = List(
			MenuAction.WIDGET_TYPE_1,
			MenuAction.WIDGET_TARGET,
			MenuAction.WIDGET_CLOSE,
			MenuAction.WIDGET_TYPE_4,
			MenuAction.WIDGET_TYPE_5,
			MenuAction.WIDGET_CONTINUE,
			MenuAction.WIDGET_FIRST_OPTION,
			MenuAction.WIDGET_SECOND_OPTION,
			MenuAction.WIDGET_THIRD_OPTION,
			MenuAction.WIDGET_FOURTH_OPTION,
			MenuAction.WIDGET_FIFTH_OPTION,
			MenuAction.CC_OP_LOW_PRIORITY,
			MenuAction.CC_OP
	)

	extension (e: MenuEntry) {
		def getSanitizedOption: String = Text.sanitize(e.getOption)
		def getSanitizedTarget: String = Text.sanitize(e.getTarget)

		def isTileObjectAction: Boolean = tileObjectActions.contains(e.getType)
		def isNpcAction: Boolean = npcActions.contains(e.getType)
		def isExamineAction: Boolean = Seq(EXAMINE_ITEM_GROUND, EXAMINE_OBJECT, EXAMINE_NPC, EXAMINE_ITEM).contains(e.getType)
		def isPlayerAction: Boolean = playerActions.contains(e.getType)
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

		def getParentMenu: Menu = {
			e.getClass.getDeclaredFields.toList.find(f => {
				val tpe = f.getType
				classOf[Menu].isAssignableFrom(tpe)
			}).flatMap[Menu](f => {
				f.setAccessible(true)
				val toRet = f.get(e)
				f.setAccessible(false)
				Try(classOf[Menu].cast(toRet)).toOption
			}).orNull
		}
		
		def prettyString(): String = {
			val paramColor   = new Color(0xC06A09)
			import TextExtensions.colored
			val messageParts = Seq(
				e.getType.name().colored(Color.blue).appendedAll("("),
				"id".colored(paramColor).appendedAll("=").appendedAll(s"${e.getIdentifier}".colored(Color.GREEN)).appendedAll(", "),
				"params".colored(paramColor).appendedAll("=(").appendedAll(s"${e.getParam0}".colored(Color.CYAN)).appendedAll(", ").appendedAll(s"${e.getParam1}".colored(Color.CYAN)).appendedAll("), "),
				"option".colored(paramColor).appendedAll("=").appendedAll(s"${Text.escapeJagex(e.getOption)}".colored(Color.MAGENTA)).appendedAll(", "),
				"target".colored(paramColor).appendedAll("=").appendedAll(s"${Text.escapeJagex(e.getTarget)}".colored(new Color(100, 100, 200))).appendedAll(")")
			)
			messageParts.fold("")(_.appendedAll(_))
		}
	}
}
