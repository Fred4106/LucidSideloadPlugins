package com.fredplugins.pvmDebugger.moons

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.moons
import com.google.inject.Inject
import ethanApiPlugin.collections.NPCs
import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.events.ActorDeath
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.GameTick
import net.runelite.api.gameval.NpcID
import net.runelite.client.chat.ChatMessageBuilder
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import org.slf4j.Logger

import java.awt.Color
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

class FredsMoonHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsMoonConfig) extends HelperModule with WithPanel {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	var currentRoom: Option[MoonRoomEnum] = None

	override def init(): Unit = {
		currentRoom = None
		log.debug("Initializing FredsMoonHelper")
	}

	override def cleanup(): Unit = {
		currentRoom = None
		log.debug("Cleaning up FredsMoonHelper")
	}

//	client.getTickCount
	@Subscribe
	def onGameTick(gameTick: GameTick): Unit={
		val newRoomOpt = MoonRoomEnum.test(client)
		val curRoom = currentRoom.map(_.toString).getOrElse("Empty")
		val newRoom  = newRoomOpt.map(_.toString).getOrElse("Empty")
		Option.unless(newRoom.equals(curRoom))(
			ChatMessageBuilder().append("Room changed from ").append(Color.PINK, curRoom).append(" to ").append(Color.YELLOW, newRoom)
		).foreach(builder => printMessage(ChatMessageType.GAMEMESSAGE, "", "")(builder))
		currentRoom = newRoomOpt
	}

	@Subscribe
	def onGameStateChanged(event: GameStateChanged): Unit = {
		printMessage(ChatMessageType.GAMEMESSAGE, "", "")(ChatMessageBuilder().append("Gamestate changed to ").append(Color.BLUE, event.getGameState.toString))
	}

//	@Subscribe
//	def onActorDied(event: ActorDeath): Unit = {
//		Option(event.getActor).collect {
//			case n: NPC => n
//		}.foreach(n => {
//			printMessage(ChatMessageType.GAMEMESSAGE, "", "")(ChatMessageBuilder().append("Actor ").append(Color.BLUE, n.getName).append(" with id ").append(Color.GREEN, n.getId.toString).append(" died in room ").append(Color.PINK, currentRoom.map(_.toString).getOrElse("Empty")))
//		})
//
////		val roomColor = currentRoom.collect {
////					case moons.MoonRoomEnum.EclipseRoom => Color.ORANGE
////					case moons.MoonRoomEnum.BloodRoom => Color.RED
////					case moons.MoonRoomEnum.BlueRoom => Color.BLUE
////					case moons.MoonRoomEnum.RewardsCavern => Color.GREEN
////		}.getOrElse(Color.WHITE)
////		val finishedRoom = currentRoom.collect {
////			case pvmDebugger.moons.MoonRoomEnum.EclipseRoom =>13012
////			case pvmDebugger.moons.MoonRoomEnum.BloodRoom => 13011
////			case pvmDebugger.moons.MoonRoomEnum.BlueRoom => 13013
////		}
////			.zip(Option(event.getActor).collect{
////				case n: NPC => n
////			})
////			.filter{
////				case (i, npc) => npc.getId.tap(npcId => log.debug(s"killed npc ${npc.getName} with id ${npcId}")) == i
////			}
////			.map(_._2 -> currentRoom.get)
////			.map {
////				case (i, room) =>(new ChatMessageBuilder).append("Finished room ").append(roomColor, room.toString).append(i.getName + "|" +i.getIndex)
////			}
////			.foreach(msg => printMessage(ChatMessageType.GAMEMESSAGE, "", "")(msg))
//	}

	def inMoons: Boolean = {
		currentRoom.isDefined
	}

//	def inMoons: Boolean = Try(client.getLocalPlayer.getWorldLocation)
//		.map(wl => (wl.getRegionX, wl.getRegionY))
//		.toOption
//		.getOrElse((-1, -1))
//		.pipe {
//			case (rx, ry) => (21 to 23).contains(rx) && (149 to 151)
//				.contains(ry)
//		}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		Option.when(inMoons) {
//			val title = TitleComponent.builder().text(configGroup).color(Color.GREEN).build()
			val roomLine  = currentRoom.map(room => {LineComponent.builder().left("Room").right(room.toString).rightColor(
				room match {
					case moons.MoonRoomEnum.EclipseRoom => Color.ORANGE
					case moons.MoonRoomEnum.BloodRoom => Color.RED
					case moons.MoonRoomEnum.BlueRoom => Color.BLUE
					case moons.MoonRoomEnum.RewardsCavern => Color.GREEN
					case _ => Color.WHITE
				}
			).build()}).get
			Seq(roomLine)
		}.getOrElse(Seq.empty[LayoutableRenderableEntity])
	}
}
