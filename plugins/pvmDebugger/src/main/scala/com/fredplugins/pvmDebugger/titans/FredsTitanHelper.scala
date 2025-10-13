package com.fredplugins.pvmDebugger.titans

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.titans.FredsTitanConfig.MagePrayer
import com.fredplugins.pvmDebugger.titans.FredsTitanConfig.MeleePrayer
import com.fredplugins.pvmDebugger.titans.FredsTitanConfig.RangePrayer
import com.google.inject.Inject
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.Equipment
import ethanApiPlugin.collections.Inventory
import ethanApiPlugin.collections.NPCs
import ethanApiPlugin.collections.query.EquipmentItemQuery
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.lucidplugins.api.utils.EquipmentUtils
import ethanApiPlugin.lucidplugins.api.utils.EquipmentUtils
import ethanApiPlugin.lucidplugins.api.utils.InventoryUtils
import ethanApiPlugin.services.localPlayer.LocalPlayerService
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.EquipmentInventorySlot
import net.runelite.api.GameState
import net.runelite.api.InventoryID
import net.runelite.api.ItemContainer
import net.runelite.api.NPC
import net.runelite.api.Perspective
import net.runelite.api.Prayer
import net.runelite.api.events.ActorDeath
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.GameTick
import net.runelite.api.events.ScriptPreFired
import net.runelite.api.gameval.NpcID
import net.runelite.client.chat.ChatMessageBuilder
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged
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
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps


//class FredsTitanConfigAccessor(config:FredsTitanConfig) {
//	def meleeWeaponIds: List[Int] = parseList(config.meleeWeaponIds())
//	def mageWeaponIds: List[Int] = parseList(config.meleeWeaponIds())
//	def rangeWeaponIds: List[Int] = parseList(config.meleeWeaponIds())
//}
class FredsTitanHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsTitanConfig, val localPlayerService: LocalPlayerService) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsTitanConfig.GROUP
	var region = -1
	var wepId = -1
	var wepName = "null"
	var setups: Map[SetupType, EquipmentSet] = Map.empty

	def currentSetup:Option[(SetupType, EquipmentSet)] = setups.find(_._2.weaponIds.contains(wepId))

	def getSetupFromConfig(tpe: SetupType): EquipmentSet = {
		def getPrayerFromConfig(tpe: SetupType): Option[Prayer] = (tpe match {
			case SetupTypes.Magic => Option(config.magePrayer().getPrayer)
			case SetupTypes.Melee => Option(config.meleePrayer().getPrayer)
			case SetupTypes.Range => Option(config.rangePrayer().getPrayer)
		})

		val (wepIds, gearIds) = (tpe match {
			case SetupTypes.Magic => (config.magicWeaponIds(), config.magicGearIds())
			case SetupTypes.Melee => 				(config.meleeWeaponIds(), config.meleeGearIds())
			case SetupTypes.Range => 				(config.rangeWeaponIds(), config.rangeGearIds())
		}).pipe(x => parseList(x._1) -> parseList(x._2))
		EquipmentSet(wepIds,gearIds, getPrayerFromConfig(tpe))
	}

	override def init(): Unit = {
		region = -1
		wepId = -1
		wepName = "null"

		setups = SetupTypes.values.map(t => t -> getSetupFromConfig(t)).toMap
	}

	override def cleanup(): Unit = {
		region = -1
		wepId = -1
		wepName = "null"
		setups = Map.empty[SetupType, EquipmentSet]
	}

//	private def getItemIdFromContainer(container: ItemContainer, slotID: Int): Int = {
//		if (container == null) return -1
//		val item = container.getItem(slotID)
//		if (item != null) item.getId
//		else -1
//	}

	@Subscribe
	private def onConfigChanged(event: ConfigChanged): Unit = {
		if (event.getGroup == FredsTitanConfig.GROUP){
			Option(event.getKey).collect {
				case "meleeWeaponIds" | "meleeGearIds" | "meleePrayer" => SetupTypes.Melee
				case "rangeWeaponIds" | "rangeGearIds" | "rangePrayer" => SetupTypes.Range
				case "magicWeaponIds" | "magicGearIds" | "magePrayer" => SetupTypes.Magic
			}.foreach(st => {
				setups = setups.updated(st, getSetupFromConfig(st))
			})
		}
	}

//	client.getTickCount
	@Subscribe
	def onGameTick(gameTick: GameTick): Unit= {
		val tRegion = localPlayerService.getCurrentRegionID
		val tWepId = Option(EquipmentUtils.getWepSlotItem).map(_.getId).getOrElse(-1)
		if(tWepId != wepId) {
			wepName = Option.when(tWepId > 0)(Try(client.getItemDefinition(tWepId).getName).toOption).flatten.getOrElse("null")
		}
		wepId = tWepId
		region = tRegion
		if(inTitansInstance) {
			val titans = NPCs.search().withId(NpcID.RT_ICE_KING, NpcID.RT_FIRE_QUEEN).result().asScala.toList
			val elementals = NPCs.search().withId(NpcID.RT_SUMMON_ELEMENTAL_FIRE, NpcID.RT_SUMMON_ELEMENTAL_ICE).result().asScala.toList

			if(titans.nonEmpty) {
				currentSetup.foreach((curType, curSetup) => {
					val toEquip: Seq[Int] = Inventory.search().withId(curSetup.gearIds *).result().asScala.toList.map(_.getItemId).distinct
					toEquip.forall(InventoryUtils.wieldItem)
					curSetup.prayer match {
						case Some(offensivePrayer) => CombatUtils.activatePrayer(offensivePrayer)
						case None => CombatUtils.deactivatePrayers(offensivePrayers *)
					}
				})
			} else {
				CombatUtils.deactivatePrayers(false)//offensivePrayers.appendedAll(Seq(Prayer.PROTECT_FROM_MAGIC, Prayer.PROTECT_FROM_MELEE, Prayer.PROTECT_FROM_MISSILES)) *)
			}
		}
	}

	def inTitansInstance: Boolean = {
		if(client.getGameState == GameState.LOGGED_IN && localPlayerService.getCurrentRegionID == TitansRegion) {
			client.getTopLevelWorldView.isInstance
//			val newRoomEnum = MoonRoomEnum.test(client)
//			val curRoom     = currentRoom.map(_.toString).getOrElse("Empty")
//			val newRoom     = newRoomEnum.map(_.toString).getOrElse("Empty")
//			Option.unless(newRoom.equals(curRoom))(
//				ChatMessageBuilder().append("Room changed from ").append(Color.PINK, curRoom).append(" to ").append(Color.YELLOW, newRoom)
//				).foreach(builder => printMessage(ChatMessageType.FRIENDSCHAT, "Moons Helper", "inMoons")(builder))
//			currentRoomChangedTick = client.getTickCount
//			currentRoom = newRoomEnum
		} else {
			false
		}
//		currentRoom.isDefined
	}

//	def inMoons: Boolean = Try(client.getLocalPlayer.getWorldLocation)
//		.map(wl => (wl.getRegionX, wl.getRegionY))
//		.toOption
//		.getOrElse((-1, -1))
//		.pipe {
//			case (rx, ry) => (21 to 23).contains(rx) && (149 to 151)
//				.contains(ry)
//		}

	def typeToColor(tpe: SetupType): Color = tpe match {
		case SetupTypes.Magic => Color.BLUE
		case SetupTypes.Melee => Color.ORANGE
		case SetupTypes.Range => Color.GREEN
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		Option.when(client.getGameState == GameState.LOGGED_IN) {
//			val title = TitleComponent.builder().text(configGroup).color(Color.GREEN).build()
			val regionLine  = LineComponent.builder().left("Region").right(s"${localPlayerService.getCurrentRegionID}").rightColor(
				if(client.getTopLevelWorldView.isInstance) Color.GREEN else Color.BLUE).build()

			val currentTypeLine = currentSetup.map(_._1).getOrElse(null).pipe(currentType =>
				LineComponent.builder().left("Current Type")
					.right(s"${currentType}")
					.rightColor(Option(currentType).map(typeToColor).getOrElse(Color.RED))
					.build()
			)
			val wepColor = wepId match {
				case i if parseList(config.meleeWeaponIds()).contains(i) => Color.ORANGE
				case i if parseList(config.rangeWeaponIds()).contains(i) => Color.GREEN
				case i if parseList(config.magicWeaponIds()).contains(i)  => Color.BLUE
				case _ => Color.RED
			}
			val wepIdLine   = LineComponent.builder().left("Weapon id").right(s"${wepId}").rightColor(wepColor).build()
			val wepNameLine = LineComponent.builder().left("Weapon name").right(s"${wepName}").rightColor(wepColor).build()



			val setupLines = setups.flatMap {
				case (st, es) => {
					val setupsTitle = TitleComponent.builder().text(s"${st}").color(typeToColor(st)).build()
					val weaponsLine = LineComponent.builder().left(s"Weapon")
						.right(s"${es.weaponIds.mkString("[", ", ", "]")}")
						.build()
					val gearLine = LineComponent.builder().left(s"Gear")
						.right(s"${es.gearIds.mkString("[", ", ", "]")}")
						.build()
					val prayerLine = LineComponent.builder().left(s"Prayer")
						.right(s"${es.prayer.map(_.name()).getOrElse("None")}")
						.rightColor(if(es.prayer.isDefined) Color.WHITE else Color.RED)
						.build()
					Seq(setupsTitle, weaponsLine, gearLine, prayerLine)
				}
			}.toSeq
			//				val wepName = we

			Seq(regionLine, currentTypeLine, wepIdLine, wepNameLine,setupLines)
				.flatMap {
					case e: LayoutableRenderableEntity => Seq(e)
					case le: Seq[_] => le.collect {
						case e: LayoutableRenderableEntity => e
					}
				}
		}.getOrElse(Seq.empty[LayoutableRenderableEntity])
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		if(inTitansInstance) {

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
					case NpcID.RT_ICE_KING | NpcID.RT_ICE_KING_INACTIVE => Some((n,  "king", new Color(0x3B95FC)))
					case NpcID.RT_SUMMON_ELEMENTAL_ICE=> Some((n, "ice",  new Color( 0x63F8FD)))
					case NpcID.RT_FIRE_QUEEN | NpcID.RT_FIRE_QUEEN_INACTIVE => Option((n, "queen", new Color(200, 0, 0)))
					case NpcID.RT_SUMMON_ELEMENTAL_FIRE => Some((n,  "fire" ,new Color(0xF03C3C)))
					case NpcID.ROYAL_TITANS_AREA_ATTACK_TIMER_HANDLER => Some((n,  "timer" , new Color(255, 0, 255)))
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
