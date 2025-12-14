package com.fredplugins.pvmDebugger.dks


import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.HelperOverlay
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.google.inject.Inject
import ethanApiPlugin.collections.Inventory
import ethanApiPlugin.lucidplugins.api.item.SlottedItem
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.lucidplugins.api.utils.EquipmentUtils
import ethanApiPlugin.lucidplugins.api.utils.InteractionUtils
import ethanApiPlugin.lucidplugins.api.utils.InventoryUtils
import ethanApiPlugin.lucidplugins.api.utils.NpcUtils
import net.runelite.api.Client
import net.runelite.api.Menu
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.NPC
import net.runelite.api.Prayer
import net.runelite.api.Skill
import net.runelite.api.events.ActorDeath
import net.runelite.api.events.AnimationChanged
import net.runelite.api.events.GameTick
import net.runelite.api.events.InteractingChanged
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.NpcChanged
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.PostMenuSort
import net.runelite.api.gameval.AnimationID
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.NpcID
import net.runelite.api.widgets.WidgetUtil
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.util.ColorUtil
import net.runelite.client.util.Text

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Rectangle
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.Random
import scala.util.Try

class DksHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: DksConfig) extends HelperModule with WithOverlay {
//	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	override val moduleName: String = "DksHelper"

	enum AttackStyle {
		case Range
		case Magic
		case Melee
	}

	sealed trait KingWrapper {
		var lastAttack: Int = -1
		var lastNpc: NPC = null

		def npcId: Int
		def attackAnimationId: Int
		def attackStyle: AttackStyle
		def weakAgainst: AttackStyle

		@Subscribe
		def onAnimationChanged(e: AnimationChanged): Unit = {
			Option(e.getActor).collect{
				case n: NPC if n.getId == npcId && n.getAnimation == attackAnimationId => {
					lastAttack = client.getTickCount
					lastNpc = n
				}
			}
		}

		@Subscribe
		def onInteractingChanged(e: InteractingChanged): Unit = {
			val npcOpt = Option(e.getSource).collect {
				case n: NPC if n.getId == npcId => n
			}
			npcOpt.foreach(n => {
				lastNpc = n
				if(n.getInteracting == null) lastAttack = -1
				else if(n.getInteracting == client.getLocalPlayer) lastAttack = client.getTickCount
			})
		}

		@Subscribe
		def onActorDeath(e: ActorDeath): Unit = {
			if(Option(e.getActor).collect {
				case n: NPC if n.getId == npcId => n
			}.isDefined) {
				lastAttack = -1
			}
		}

		@Subscribe
		def onNpcDespawned(e: NpcDespawned): Unit = {
			if (e.getNpc == lastNpc) {
				lastNpc = null
				lastAttack = -1
			}
		}
	}

	object Prime extends KingWrapper {
		override def npcId: Int = NpcID.DAGCAVE_RANGED_BOSS
		override def attackAnimationId: Int = AnimationID.DAGANNOTH_MEGANOTH_ATTACK_RANGE
		override def attackStyle: AttackStyle = AttackStyle.Range
		override def weakAgainst: AttackStyle = AttackStyle.Melee
	}

	object Rex extends KingWrapper {
		override def npcId: Int = NpcID.DAGCAVE_MELEE_BOSS
		override def attackAnimationId: Int = AnimationID.DAGANNOTH_MEGANOTH_ATTACK_MELEE
		override def attackStyle: AttackStyle = AttackStyle.Melee
		override def weakAgainst: AttackStyle = AttackStyle.Magic
	}

	object Supreame extends KingWrapper {
		override def npcId: Int = NpcID.DAGCAVE_MAGIC_BOSS
		override def attackAnimationId: Int = AnimationID.DAGANNOTH_MEGANOTH_ATTACK_MAGE
		override def attackStyle: AttackStyle = AttackStyle.Magic
		override def weakAgainst: AttackStyle = AttackStyle.Range
	}

	override def init(): Unit = {
		parent.getEventBus.register(Rex)
		parent.getEventBus.register(Prime)
		parent.getEventBus.register(Supreame)
	}

	override def cleanup(): Unit = {
		parent.getEventBus.unregister(Rex)
		parent.getEventBus.unregister(Prime)
		parent.getEventBus.unregister(Supreame)
	}

	@Subscribe(priority = -9)
	def onPostMenuSort(postMenuSort: PostMenuSort): Unit = {
//		if (!client.isMenuOpen) {
//			val menu   : Menu = client.getMenu
//			val menuEntries   : List[MenuEntry] = menu.getMenuEntries.toList
//			val (added, stock)                  = menuEntries.partition((e: MenuEntry) => {
//				e.getType == MenuAction.RUNELITE && e.getSanitizedOption.endsWith("Disturb")
//			})
//			added.foreach(a => log.debug("Added: {}", a))
//			val newMenuEntries: List[MenuEntry] = stock.appendedAll(added)
//			client.setMenuEntries(newMenuEntries.toArray[MenuEntry])
//		}
	}

	@Subscribe
	def onMenuEntryAdded(menuEntryAdded: MenuEntryAdded): Unit = {
//		val entry = menuEntryAdded.getMenuEntry
//		if(entry.isNpcAction) {
//			val npc = entry.getNpc
//			val explosivesWidgetOpt = Inventory.search().withId(ItemID.FISHING_EXPLOSIVE).first().toScala
//			if(addExplosiveMenu &&
//				npc.getId == NpcID.SLAYER_KRAKEN_BOSS_WHIRLPOOL &&
//				entry.getSanitizedOption.equals("Disturb") &&
//				explosivesWidgetOpt.isDefined) {
//				val explosivesW = explosivesWidgetOpt.get
//				val menu = entry.getParentMenu
//				menu.createMenuEntry(-1)
//						.setOption(ColorUtil.prependColorTag("Disturb", Color.BLUE))
//						.setType(MenuAction.RUNELITE)
//						.onClick(e => {
//							addExplosiveMenu = false
//							InteractionUtils.useWidgetOnNPC(explosivesW, npc)
////							setNpcHighlightColor(npc.getId(), c);
////							clientThread.invokeLater(this :: rebuild);
//						});
//				//add throw explosives entry
//			}
//		}
	}

//	var addExplosiveMenu: Boolean = false
//
//	var cooldown = 0
//	var countdownTillFirstAttack = -1
	@Subscribe
	def onGameTick(e: GameTick): Unit = {
//		if(cooldown > 0) {
//			cooldown = cooldown - 1
//		} else {
//			countdownTillFirstAttack = Math.max(countdownTillFirstAttack - 1, -1)
//			val krakenNpcOpt = NpcUtils.search().withId(NpcID.SLAYER_KRAKEN_BOSS_WHIRLPOOL, NpcID.SLAYER_KRAKEN_BOSS).first().toScala
//			if(krakenNpcOpt.isDefined) {
//				val localPlayer = client.getLocalPlayer
//				val kraken = krakenNpcOpt.get
//				val myHp = client.getBoostedSkillLevel(Skill.HITPOINTS)
//				if (countdownTillFirstAttack > 0) {
//
//				}
//				else if(countdownTillFirstAttack == 0) {
//					NpcUtils.attackNpc(kraken)
//					cooldown = 12
//				}
//				else if(myHp < config.lowerHpThreshold() && EquipmentUtils.getWepSlotItem.getId != ItemID.MAGICTRAINING_WAND_MASTER) {
//					cooldown = if(InventoryUtils.wieldItem(ItemID.MAGICTRAINING_WAND_MASTER)) 2 else 0
//				}
//				else if (myHp > config.upperHpThreshold() && EquipmentUtils.getWepSlotItem.getId != ItemID.WARPED_SCEPTRE) {
//					cooldown = if(InventoryUtils.wieldItem(ItemID.WARPED_SCEPTRE)) 2 else 0
//				}
//				else if (localPlayer.getInteracting == null || localPlayer.getInteracting != kraken) {
//					if(kraken.getId == NpcID.SLAYER_KRAKEN_BOSS) {
//						countdownTillFirstAttack = 1
//					} else if(config.autoExplosive() && InventoryUtils.contains(ItemID.FISHING_EXPLOSIVE) && localPlayer.getInteracting == null) {
//						InteractionUtils.useItemOnNPC(ItemID.FISHING_EXPLOSIVE, kraken)
//						countdownTillFirstAttack = Random.nextInt(3)+1
//						cooldown = 4
//					}
//				}
//			}
//		}
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		def renderNpcOverlay(npc: NPC, text: String, zoffset: Int, color: Color, fillAlpha: Int, outlineWidth: Int = 4, textColor: Color = Color.WHITE): Unit = {
			val poly = npc.getConvexHull
			if (poly != null) OverlayUtil.renderPolygon(g, poly, Color(0, 0, 0, 0), color.withAlpha(fillAlpha))
			if (outlineWidth > 0) parent.getModelOutlineRenderer.drawOutline(npc, outlineWidth, color, 2)

			if (text != null && text.nonEmpty) {
				val textLocation = npc.getCanvasTextLocation(g, text, npc.getLogicalHeight + zoffset)
				if (textLocation != null) {
					val textBounds = g.getFontMetrics.getStringBounds(text, g)
					val offset     = 4
					val textArea   = Rectangle(
						textBounds.getX.toInt - offset, textBounds.getY.toInt - offset, textBounds.getWidth.toInt + offset + offset,
						textBounds.getHeight.toInt + offset + offset)
					//				val textArea = new Rectangle(textLocation.getX + (textBounds.getWidth / 2.0).toInt - , textLocation.getY, textBounds.getWidth.toInt, textBounds.getHeight.toInt)
					OverlayUtil.renderPolygon(
						g, textArea, new Color(
							255 - textColor.getRed, 255 - textColor.getGreen, 255 - textColor.getBlue, fillAlpha))
					OverlayUtil.renderTextLocation(g, textLocation, text, textColor)
				}
			}
		}

		List(Supreame, Rex, Prime).filter(_.lastNpc != null).foreach(k => {
			val name = k.getClass.getSimpleName.stripSuffix("$")
			val tMsg = Option(k.lastAttack).filter(_ != -1).map(_ - client.getTickCount).map(4 - _).map(n => Math.max(0, Math.min(4, n)).toString).getOrElse("undef")
			val msg = s"${name}: ${tMsg}"
			val c = k.attackStyle match {
				case AttackStyle.Range => Color.GREEN
				case AttackStyle.Magic => Color.BLUE
				case AttackStyle.Melee => Color.RED
			}
			val fillA = if(k.lastNpc.getInteracting == client.getLocalPlayer) 64 else 32
			val outlineW = if(k.lastNpc.getInteracting == client.getLocalPlayer) 3 else 1
			renderNpcOverlay(k.lastNpc, msg, 0, c, fillA, outlineW, c)
		})
		null.asInstanceOf[Dimension]
	}
}
