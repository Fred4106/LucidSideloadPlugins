package com.fredplugins.pvmDebugger.muspah

import com.fredplugins.common.PrayerExtended
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.collections.NPCs
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.NPC
import net.runelite.api.Prayer
import net.runelite.api.Skill
import net.runelite.api.events.AnimationChanged
import net.runelite.api.events.GameTick
import net.runelite.api.gameval.NpcID
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import packets.MousePackets
import packets.WidgetPackets

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class FredsMuspahHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsMuspahConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsMuspahConfig.GROUP
	private def clientThread  = parent.getClientThread

	private def activatePrayer(prayer: Prayer): Unit = {
		if(client.getBoostedSkillLevel(Skill.PRAYER) <= 0) return

		for {
			p <- Option(prayer).filterNot(client.isPrayerActive)
			isDisabled = client.getVarbitValue(p.getVarbit()) == 0
			widget <- Option(client.getWidget(PrayerExtended.getWidgetId(p))) if isDisabled
		} yield {
			() => {
				client.setVarbit(p.getVarbit(), 1)
				MousePackets.queueClickPacket(widget)
				WidgetPackets.queueWidgetActionPacket(1, widget.getId(), -1, -1)
			}
		}.apply()
	}

	def getActivePrayers: List[Prayer] = {
		clientThread.runOnClientThread(() => {
			Prayer.values().toList.filterNot(p => {
				client.getVarbitValue(p.getVarbit) == 0
			})
		})
	}

	private def deactivatePrayer(prayer: Prayer): Unit = {
			if (client.getBoostedSkillLevel(Skill.PRAYER) <= 0) return
			for {
				p <- Option(prayer).filterNot(client.isPrayerActive)
				isDisabled = client.getVarbitValue(p.getVarbit()) == 0
				widget <- Option(client.getWidget(PrayerExtended.getWidgetId(p))) if !isDisabled
			} yield {
				() => {
					client.setVarbit(p.getVarbit(), 0)
					MousePackets.queueClickPacket(widget)
					WidgetPackets.queueWidgetActionPacket(1, widget.getId(), -1, -1)
				}
			}.apply()
	}

	def equipGear(ids: Int*): Unit = {}
	def equipGear(string: String): Unit = {}

	def meleePhase (): Unit = {
		assert(client.isClientThread)
		NPCs.search().withId(NpcID.MUSPAH_TELEPORT).first().toScala
			.foreach(teleportingMuspah => {
					if (config.tele()) {
						ticks = -1
						equipGear(config.rangeSwap())
						activatePrayer(Prayer.EAGLE_EYE)
						//					activatePrayer(config.rangeOffensivePrayer().prayer)
					}
					deactivatePrayer(Prayer.PROTECT_FROM_MELEE)
			})

		if(NPCs.search().withId(NpcID.MUSPAH_MELEE).first().toScala.isDefined) {
					if (!config.rangeOnly()) {
						equipGear(config.mageSwap())
						//					activatePrayer(config.rangeOffensivePrayer().prayer)
					}
					activatePrayer(Prayer.PROTECT_FROM_MELEE)
					activatePrayer(
						if (config.rangeOnly()) {
							Prayer.EAGLE_EYE
						} else {
							Prayer.MYSTIC_MIGHT
						}
					)
		}
	}
//	private fun rangePhase () {
//		if (NPCs.getFirst(NpcID.PHANTOM_MUSPAH_12082) != null) {
//			ticks = -1
//			if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES))
//				deactivatePrayer(Prayer.PROTECT_FROM_MISSILES)
//			if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC))
//				deactivatePrayer(Prayer.PROTECT_FROM_MAGIC)
//		}
//		if (NPCs.getFirst(NpcID.PHANTOM_MUSPAH) == null) return
//		equipRangeGear()
//		activatePrayer(config.rangeOffensivePrayer().prayer)
//		if (ticks == 1 && config.flickPrayer()) {
//			activatePrayer(Prayer.PROTECT_FROM_MISSILES)
//		}
//		if (ticks > 1 && config.flickPrayer()) {
//			if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES))
//				deactivatePrayer(Prayer.PROTECT_FROM_MISSILES)
//			if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC))
//				deactivatePrayer(Prayer.PROTECT_FROM_MAGIC)
//		}
//	}
//	private fun shieldPhase () {
//		if (NPCs.getFirst(NpcID.PHANTOM_MUSPAH_12079) == null) {
//			if (client.isPrayerActive(Prayer.SMITE)) {
//				activatePrayer(Prayer.PROTECT_FROM_MISSILES)
//			}
//		}
//		if (NPCs.getFirst(NpcID.PHANTOM_MUSPAH_12079) != null) {
//			if (ticks == 1)
//				activatePrayer(Prayer.PROTECT_FROM_MISSILES)
//			if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC) && ticks == 2 && config.smiteToggle())
//				activatePrayer(Prayer.SMITE)
//			if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE))
//				activatePrayer(Prayer.PROTECT_FROM_MISSILES)
//		}
//	}
	
	def finalPhase(): Unit = {
		NPCs.search().withId(NpcID.MUSPAH_FINAL).first().toScala.foreach(finalMuspah => {
		
		})
	}

	private var ticks         = -1

	val MUSPAH_IDS: Set[Int] = Set(NpcID.MUSPAH, NpcID.MUSPAH_MELEE, NpcID.MUSPAH_SOULSPLIT, NpcID.MUSPAH_FINAL, NpcID.MUSPAH_TELEPORT)

//	var currentRoom: Option[MoonRoomEnum] = None
//	var currentRoomChangedTick: Int = -1
	override def init(): Unit = {
//		currentRoom = None
//		currentRoomChangedTick = -1
	}

	override def cleanup(): Unit = {
//		currentRoom = None
//		currentRoomChangedTick = -1
	}
//
//	@Subscribe
//	def onGameTick(gameTick: GameTick): Unit= {
//		if (client.getGameState != GameState.LOGGED_IN || client.getLocalPlayer.isDead) {
//			CombatUtils.deactivatePrayers(false)
//			return
//		}
//
//		val muspah = client.getNpcs.stream.filter((x: NPC) => MUSPAH_IDS.contains(x.getId)).findFirst.orElse(null)
//		if (muspah == null || (muspah.isDead || (muspah.getHealthRatio eq 0))) {
//			CombatUtils.deactivatePrayers(false)
//		} else {
//			log.debug(s"found muspah ${muspah.getId}")
//
//			val offensive =Option(Prayer.EAGLE_EYE) /*Option((if ((muspah.getId eq NpcID.MUSPAH) || (muspah.getId eq NpcID.MUSPAH_SOULSPLIT) || (muspah.getId eq NpcID.MUSPAH_FINAL)) {
//					Prayer.RIGOUR
//				} else if (muspah.getId eq NpcID.MUSPAH_MELEE) {
//					Prayer.AUGURY
//				} else {
//					null
//				})).map(CombatUtils.checkPrayer(_))*/
//			val defensive = Option((if (muspah.getId eq NpcID.MUSPAH_MELEE) {
//					Prayer.PROTECT_FROM_MELEE
//				} else {
//					if(prayerMagicOnTick > 0) Prayer.PROTECT_FROM_MAGIC else Prayer.PROTECT_FROM_MISSILES
//				}))
//
//			val pToActivate = List(offensive, defensive).flatten
//
//			log.debug(s"desired prayers = ${pToActivate}")
//			CombatUtils.activatePrayers(pToActivate *)
//		}
//		prayerMagicOnTick = if(prayerMagicOnTick > 0) { prayerMagicOnTick - 1} else 0
//
////		if (muspah != null) {
////			if (EthanApiPlugin.isQuickPrayerEnabled) InteractionHelper.togglePrayer
////			InteractionHelper.togglePrayer
////		}
//	}
//
//	@Subscribe
//	def onAnimationChanged(e: AnimationChanged): Unit = {
//		Option(e.getActor).collect {
//			case npc: NPC if(MUSPAH_IDS.contains(npc.getId) && npc.getAnimation == 9918) => client.getTickCount
//		}.foreach(tc => prayerMagicOnTick = 4)
//	}

	def inMuspah: Boolean = {
		false
//		if(client.getTickCount != currentRoomChangedTick && client.getGameState == GameState.LOGGED_IN) {
//			val newRoomEnum = MoonRoomEnum.test(client)
//			val curRoom     = currentRoom.map(_.toString).getOrElse("Empty")
//			val newRoom     = newRoomEnum.map(_.toString).getOrElse("Empty")
//			Option.unless(newRoom.equals(curRoom))(
//				ChatMessageBuilder().append("Room changed from ").append(Color.PINK, curRoom).append(" to ").append(Color.YELLOW, newRoom)
//				).foreach(builder => printMessage(ChatMessageType.FRIENDSCHAT, "Moons Helper", "inMoons")(builder))
//			currentRoomChangedTick = client.getTickCount
//			currentRoom = newRoomEnum
//		}
//		currentRoom.isDefined
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		Option.when(inMuspah) {
//			val title = TitleComponent.builder().text(configGroup).color(Color.GREEN).build()
//			val roomLine  = currentRoom.map(room => {LineComponent.builder().left("Room").right(room.toString).rightColor(
//				room match {
//					case moons.MoonRoomEnum.EclipseRoom => Color.ORANGE
//					case moons.MoonRoomEnum.BloodRoom => Color.RED
//					case moons.MoonRoomEnum.BlueRoom => Color.BLUE
//					case moons.MoonRoomEnum.RewardsCavern => Color.GREEN
//					case _ => Color.WHITE
//				}
//			).build()}).get
			//Seq(roomLine)
			Seq.empty[LayoutableRenderableEntity]
		}.getOrElse(Seq.empty[LayoutableRenderableEntity])
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		if(inMuspah) { 
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

//					val poly =Perspective.getCanvasTileAreaPoly(client, n.getLocalLocation, 1, 1, client.getPlane, 0)
//					if (poly != null) OverlayUtil.renderPolygon(g, poly, c)

					parent.getModelOutlineRenderer.drawOutline(n, 2,  c, 4)

					def renderActorOverlay(text: String, color: Color, zoffset: Int): Unit = {
						val poly = n.getCanvasTilePoly
						if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
						val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
						if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, text, color)
					}
					renderActorOverlay(text, c, 40)
					renderActorOverlay(txt, c, 60)

//					val textLocation = Perspective.getCanvasTextLocation(client, g, n.getLocalLocation, txt, 10)///n.getCanvasTextLocation(g, txt, n.getLogicalHeight + 40)
//					val textLocation2 = Perspective.getCanvasTextLocation(client, g, n.getLocalLocation, text, 30)// n.getCanvasTextLocation(g, text, n.getLogicalHeight + 80)
//					if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, txt, Color.WHITE)
//					if (textLocation2 != null) OverlayUtil.renderTextLocation(g, textLocation2, text, Color.WHITE)
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
