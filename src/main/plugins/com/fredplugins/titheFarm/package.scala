package com.fredplugins

import net.runelite.api.{Client, MenuAction, MenuEntry}
import net.runelite.api.MenuAction.{EXAMINE_OBJECT, GAME_OBJECT_FIFTH_OPTION, GAME_OBJECT_FIRST_OPTION, GAME_OBJECT_FOURTH_OPTION, GAME_OBJECT_SECOND_OPTION, GAME_OBJECT_THIRD_OPTION, ITEM_USE_ON_GAME_OBJECT, WIDGET_TARGET_ON_GAME_OBJECT}
import net.runelite.api.coords.WorldPoint

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.chaining.*

package object titheFarm {
	private val gameObjectActions: List[MenuAction] = 	List(ITEM_USE_ON_GAME_OBJECT, WIDGET_TARGET_ON_GAME_OBJECT, EXAMINE_OBJECT, GAME_OBJECT_FIRST_OPTION, GAME_OBJECT_SECOND_OPTION, GAME_OBJECT_THIRD_OPTION, GAME_OBJECT_FOURTH_OPTION, GAME_OBJECT_FIFTH_OPTION)
	extension (e: MenuEntry) {
		def isGameObjectAction: Boolean = gameObjectActions.contains(e.getType)
		def isRuneliteAction: Boolean = e.getType == MenuAction.RUNELITE && e.getIdentifier == 2428

		def getWorldLocation(using c: Client): WorldPoint = {
			if(isGameObjectAction || isRuneliteAction) {
				if (!(e.getParam0 < 0 || e.getParam1 < 0 || e.getParam0 >= 104 || e.getParam1 >= 104)) {
					val wv     = c.getTopLevelWorldView
					val (x, y) = (wv.getBaseX + e.getParam0, wv.getBaseY + e.getParam1)
					new WorldPoint(x, y, wv.getPlane)
				} else null
			}
			else null
		}
	}

//	def extractWorldLocation(me: MenuEntry)(using client: Client): WorldPoint = {
//		(client.getTopLevelWorldView, (me.getParam0, me.getParam1))
//	}
}
