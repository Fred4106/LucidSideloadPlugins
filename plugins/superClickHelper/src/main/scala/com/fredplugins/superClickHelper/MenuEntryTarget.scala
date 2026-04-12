package com.fredplugins.superClickHelper

import com.fredplugins.common.utils.{SceneUtils, ShimUtils}
import net.runelite.api.{Client, MenuAction, MenuEntry, NPC, Player, TileObject}
import net.runelite.api.events.{MenuEntryAdded, MenuOptionClicked}
import net.runelite.api.widgets.Widget
import net.runelite.client.RuneLite
import org.slf4j.Logger

import scala.util.chaining.*

sealed trait MenuEntryTarget {
	def niceString: String
	override def toString: String = niceString
}

object MenuEntryTarget {
	import com.fredplugins.common.extensions.MenuExtensions._
	import com.fredplugins.common.extensions.ActorExtensions._
	import com.fredplugins.common.extensions.WidgetExtensions._
	import com.fredplugins.common.extensions.ObjectExtensions._
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "INFO")
	given Client = RuneLite.getInjector.getInstance(classOf[Client])

	sealed transparent trait SimpleTarget {
		this: MenuEntryTarget =>
	}

	case class TileObjectTarget(tileObject: TileObject) extends MenuEntryTarget with SimpleTarget {
		def niceString: String = s"${productPrefix}(${tileObject.niceString})"
	}
	case class NpcTarget(npc: NPC) extends MenuEntryTarget with SimpleTarget{
		def niceString: String = s"${productPrefix}(${npc.niceString})"
	}
	case class PlayerTarget(player: Player) extends MenuEntryTarget with SimpleTarget{
		def niceString: String = s"${productPrefix}(${player.niceString})"
	}
	case class WidgetTarget(widget: Widget) extends MenuEntryTarget with SimpleTarget {
		def niceString: String = s"${productPrefix}(${widget.niceString})"
	}
	case class WidgetOnTarget(w: Widget, other: SimpleTarget) extends MenuEntryTarget {
		def niceString: String = s"${productPrefix}(${w.niceString}, ${other})"
	}

	private def transformPF(using client: Client): PartialFunction[MenuEntry, Option[MenuEntryTarget]] = {
		case me if me.isTileObjectAction => SceneUtils.findTileObject(me.getParam0, me.getParam1, me.getIdentifier).map(to => TileObjectTarget(to))//.TileObjectTarget(me.getParam0, me.getParam1, me.getIdentifier)
		case me if me.isNpcAction        => SceneUtils.findNpc(me.getIdentifier).map(npc => NpcTarget(npc))
		case me if me.isPlayerAction     => SceneUtils.findPlayer(me.getIdentifier).map(player => PlayerTarget(player))
		case me if me.isWidgetTargetOnAction     => {
			Option(client.getSelectedWidget()).zip(
				Option(me.getType).flatMap[SimpleTarget] {
					case MenuAction.WIDGET_TARGET_ON_PLAYER => SceneUtils.findPlayer(me.getIdentifier).map(player => PlayerTarget(player))
					case MenuAction.WIDGET_TARGET_ON_NPC => SceneUtils.findNpc(me.getIdentifier).map(npc => NpcTarget(npc))
					case MenuAction.WIDGET_TARGET_ON_GAME_OBJECT => SceneUtils.findTileObject(me.getParam0, me.getParam1, me.getIdentifier).map(to => TileObjectTarget(to))
					case MenuAction.WIDGET_TARGET_ON_WIDGET => Option(me.getWidget).map(WidgetTarget(_))
					case _ => None
				}
			).map((selectedW, targ) => WidgetOnTarget(selectedW, targ))
		}
	}
	def apply(menuEntry: MenuEntryAdded)(using client: Client): Option[MenuEntryTarget] = {
		val possibleResult = transformPF.lift.apply(menuEntry.getMenuEntry).flatten
		if(possibleResult.isEmpty) {
			log.debug(s"Cant convert {} to MenuEntryTarget", menuEntry)
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
