package com.fredplugins.pvmDebugger.moons

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.moons
import com.google.inject.Inject
import ethanApiPlugin.collections.NPCs
import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.NPC
import net.runelite.api.events.ActorDeath
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.GameTick
import net.runelite.api.gameval.NpcID
import net.runelite.client.chat.ChatMessageBuilder
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.util.stream.Collectors
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

class FredsMoonHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsMoonConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = "FredsMoonHelper"
	var currentRoom: Option[MoonRoomEnum] = None
	var currentRoomChangedTick: Int = -1
	override def init(): Unit = {
		currentRoom = None
		currentRoomChangedTick = -1
	}

	override def cleanup(): Unit = {
		currentRoom = None
		currentRoomChangedTick = -1
	}

//	client.getTickCount
	@Subscribe
	def onGameTick(gameTick: GameTick): Unit={

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
		if(client.getTickCount != currentRoomChangedTick && client.getGameState == GameState.LOGGED_IN) {
			val newRoomEnum = MoonRoomEnum.test(client)
			val curRoom     = currentRoom.map(_.toString).getOrElse("Empty")
			val newRoom     = newRoomEnum.map(_.toString).getOrElse("Empty")
			Option.unless(newRoom.equals(curRoom))(
				ChatMessageBuilder().append("Room changed from ").append(Color.PINK, curRoom).append(" to ").append(Color.YELLOW, newRoom)
				).foreach(builder => printMessage(ChatMessageType.GAMEMESSAGE, "", "")(builder))
			currentRoomChangedTick = client.getTickCount
			currentRoom = newRoomEnum
		}
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
	override def renderOverlay(g: Graphics2D): Dimension = {
		if(inMoons) {

//			val npcsToHighlight: List[NPC] = currentRoom.collect {
//				case pvmDebugger.moons.MoonRoomEnum.EclipseRoom => 13012
//				case pvmDebugger.moons.MoonRoomEnum.BloodRoom => 13011
//				case pvmDebugger.moons.MoonRoomEnum.BlueRoom => 13013
//			}.map(highlightId => {
//				client.getTopLevelWorldView.npcs().asScala.toList.filter(n => {
//					n.getId == highlightId
//				})
//			}).getOrElse(List.empty[NPC])

			client.getTopLevelWorldView.npcs().asScala.toList.flatMap(n => {
				n.getId match {
					case NpcID.PMOON_BOSS_BLOOD_MOON_VIS |
						NpcID.PMOON_BOSS_BLOOD_MOON => Some((n,  "blood", new Color(224, 61, 61)))
					case NpcID.PMOON_BOSS_BLUE_MOON_VIS |
						NpcID.PMOON_BOSS_BLUE_MOON => Some((n, "blue", new Color(56, 56, 255)))
					case NpcID.PMOON_BOSS_ECLIPSE_MOON_VIS |
						NpcID.PMOON_BOSS_ECLIPSE_MOON => Some((n,  "eclipse" ,new Color(255, 147, 0)))
					case NpcID.PMOON_BOSS_ECLIPSE_MOON_SHIELD=> Some((n, "shield",  new Color(245, 119, 119)))
					case NpcID.PMOON_BOSS_ECLIPSE_CLONE=> Some((n, "clone",  new Color(245, 119, 119)))
					case NpcID.PMOON_BOSS_JAGUAR=> Some((n, "jaguar",  new Color(245, 119, 119)))
					case NpcID.PMOON_BOSS_WINTER_STORM => Some((n, "storm",  new Color(245, 119, 119)))
					case NpcID.PMOON_BOSS_ICICLE_1 =>						Some((n, "1",  new Color(181, 0, 190)))
					case NpcID.PMOON_BOSS_ICICLE_2 =>						Some((n, "2",  new Color(181, 0, 190)))
					case NpcID.PMOON_BOSS_ICICLE_3 =>						Some((n, "3",  new Color(181, 0, 190)))
					case NpcID.PMOON_BOSS_ICICLE_UNCRACKED => Some((n, "uncracked", new Color(0, 255, 0)))
					case NpcID.PMOON_BOSS_ICICLE_CRACKED =>  Some((n, "cracked", new Color(220, 255, 30)))
					case NpcID.MOTH_MOONLIGHT_LOW_WANDER | NpcID.MOTH_MOONLIGHT => Some((n, "Moth",  new Color(119, 245, 228)))
					case _ => Option.empty[(NPC, String, Color)]
				}
			}).foreach {
				case (n, text, c) => {
					//						val x   = point.getX - client.getTopLevelWorldView.getBaseX
					//						val y   = point.getY - client.getTopLevelWorldView.getBaseY
					val txt  = s"${n.getIndex.toString.padTo(4, ' ')}(${n.getLocalLocation.getSceneX},${n.getLocalLocation.getSceneY}) = ${n.getName}"

					val poly = n.getCanvasTilePoly
					if (poly != null) OverlayUtil.renderPolygon(g, poly, c)

					parent.getModelOutlineRenderer.drawOutline(n, 2,  c, 4)

					val textLocation = n.getCanvasTextLocation(g, txt, n.getLogicalHeight + 40)
					val textLocation2 = n.getCanvasTextLocation(g, text, n.getLogicalHeight + 80)
					if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, txt, Color.WHITE)
					if (textLocation2 != null) OverlayUtil.renderTextLocation(g, textLocation2, text, Color.WHITE)
				}
			}

//			tiles.foreach {
//				case (i, point) => {
//					val x   = point.getX - client.getTopLevelWorldView.getBaseX
//					val y   = point.getY - client.getTopLevelWorldView.getBaseY
//					val txt = s"${i.toString.padTo(4, ' ')}($x,$y)"
//					renderTileOverlay(point, txt, ColorUtil.colorWithAlpha(Color.BLUE, 128 - ((112d / 100) * i).toInt), true)
//				}
		}
		null.asInstanceOf[Dimension]
	}
}
