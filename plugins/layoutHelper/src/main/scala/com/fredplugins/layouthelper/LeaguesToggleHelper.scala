package com.fredplugins.layouthelper

import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.WidgetExtensions.*
import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.{ChatMessageType, Client, ScriptEvent}
import net.runelite.api.events.{ClientTick, MenuOptionClicked, VarbitChanged}
import net.runelite.api.widgets.{JavaScriptCallback, Widget}
import net.runelite.api.gameval.InterfaceID
import net.runelite.client.eventbus.{EventBus, Subscribe}

import javax.inject.Inject
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class LeaguesToggleHelper @Inject()(val client: Client, val eventBus:EventBus) extends ShimUtils.Logging("TRACE") {
	val opListener = new JavaScriptCallback {
		override def run(e: ScriptEvent): Unit = {
			val s = e.getSource
			val spriteW = s.getParent.getChild(s.getIndex - 4)
			log.debug("scriptEvent - op: {}, w: {}, spriteId: {}", e.getOp, Option(s).map(_.niceString).getOrElse("null"), Option(spriteW).map(_.getSpriteId).getOrElse(0))
				// Spells can be shared between spellbooks, so we can't assume spellBookEnum is the current spellbook.
		}
	}
	@Subscribe
	def onClientTick(t: ClientTick): Unit = {
//		client.getWidget(InterfaceID.LeagueRelics.DESCRIPTION_HEADER).getChild(3)
		val relics: List[Widget] = Option(client.getWidget(InterfaceID.BuffBar.BUFF_DISPLAY)).flatMap(u => Option(u.getDynamicChildren()).map(_.toList.filter(_ != null)))
			.getOrElse(List.empty).filter(x => x.getType == 5 && x.isHidden ==false)

//		relics.find(_.getSpriteId == 8290)
//		log.debug(e.getMenuEntry.niceString())



		log.debug("found these relics {}", relics.map(_.niceString))
		for{
			r <- relics
			sid = r.getSpriteId
			r2 <- Option(r.getParent.getChild(r.getIndex + 4))
		} {
			Option(sid).collect {
				case 8290 => "harvest" -> Seq("toggle")
				case 8301 => "transmute" -> Seq("bank", "toggle")
				case 8295 => "woodsman" -> Seq("bank", "burn")
			}.toList.flatMap {
				case (a, b) => b.map(_.prependedAll(a.appendedAll(" ")))
			}.zipWithIndex.foreach{
				case (a, b) => {
					if(r2.getOnOpListener == null)
						r2.setOnOpListener(opListener)
					log.debug("setting action {} of widget {} to {}", b, r2.niceString, a)
					r2.setAction(b, a)
				}
			}
			if(r2.getActions != null){
				log.debug(s"r2 ${r2.niceString} has actions = ${r2.getActions.toList}")
			}
		}
//		val maps: Map[Int, Widget] = relics.map(r => r.getSpriteId -> r.getParent.getChild(r.getIndex + 4)).filter(_._2 != null)
	}

	@Subscribe
	def onMenuOptionClicked(e: MenuOptionClicked): Unit = {
		if(e.getMenuEntry.getWidget != null && e.getMenuEntry.getWidget.getId == InterfaceID.BuffBar.BUFF_DISPLAY) {
			log.debug(e.getMenuEntry.niceString())
//			val w = e.getMenuEntry.getWidget
			Option(e.getMenuOption).collect {
				case "harvest toggle" =>   1722 -> (0, 0)
				case "transmute bank" =>   6446 -> (1, 6)
				case "transmute toggle" => 6446 -> (0, 5)
				case "woodsman bank" =>    6440 -> (0, 3)
				case "woodsman burn" =>    6440 -> (0, 4)
			}.foreach{
				case (struct, (offVal, varpIdx)) => {
					val mask = 1 << (varpIdx)

					val varps = client.getVarps
					val oldValue = varps(5514)
					val newValue = oldValue ^ mask
					varps(5514) = newValue
					client.queueChangedVarp(5514)
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Changed VarPlayer " + 5514 + " from " + oldValue + " to " + newValue, null)
					val varbitChanged = new VarbitChanged
					varbitChanged.setVarpId(5514)
					varbitChanged.setValue(newValue)
					eventBus.post(varbitChanged) // fake event
					e.consume()
//					client.createScriptEventBuilder(9411, struct, 1, 0, 16513013, 16019731).build.run()
				}
			}

//			9411, 6440, 1, 0, 16513013, 16019731
		}
	}
//	option='Toggle', target='<col=ff9040>Effect</col>', id=1, opcode=57, param0=3, param1=42926118, itemId=-1
}
