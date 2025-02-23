package com.fredplugins.pvmHelper2.gauntlet

import com.fredplugins.common.OldOverlayUtil
import com.fredplugins.common.utils.{ShimUtils, WorldPointUtils}
import com.fredplugins.pvmHelper2.ClientEvent.ServerTick
import com.fredplugins.pvmHelper2.gauntlet.HunPrayStyle.Melee
import com.fredplugins.pvmHelper2.gauntlet.{ArmedAttack, HalberdAttack, MageAttack, RangeAttack, UnarmedAttack}
import com.fredplugins.pvmHelper2.gauntlet.{RegularAttack, SwitchToMage, SwitchToRange, TornadoAttack}
import com.fredplugins.pvmHelper2.gauntlet.{DisablePrayer, EnablePrayer, EnterGauntlet, EnterHunllef, ExitGauntlet, ExitHunllef, HunllefAnimationEvents, PlayerAnimationEvents, TickHunllef}
import com.fredplugins.pvmHelper2.{ClientEvent, NpcEvent, PlayerEvent, PvmEvent, PvmHelperOverlay, TypeName, gauntlet}
import com.google.inject.{Inject, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, NpcUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.query.NPCQuery
import interactionApi.PrayerInteraction
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.{Actor, Client, HeadIcon, NPC, Perspective, Player, Prayer}
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger

import scala.util.chaining.*
import net.runelite.api.events.{ActorDeath, AnimationChanged, InteractingChanged, NpcDespawned, NpcSpawned, VarbitChanged}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayManager, OverlayPosition, OverlayUtil}
import net.runelite.client.util.ColorUtil

import java.awt.{BasicStroke, Color, Dimension}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, IterableHasAsScala, IteratorHasAsScala, SeqHasAsJava}
import scala.jdk.OptionConverters.RichOptional

sealed trait GauntletEvent {}
sealed trait PlayerAnimation extends GauntletEvent {}
sealed trait HunllefAnimation extends GauntletEvent {}
sealed trait HunAttackStyle {}
object HunAttackStyle {
	case object Mage extends HunAttackStyle
	case object Range extends HunAttackStyle
}

sealed trait HunPrayStyle {}
object HunPrayStyle {
	case object None extends HunPrayStyle
	case object Mage extends HunPrayStyle
	case object Range extends HunPrayStyle
	case object Melee extends HunPrayStyle
}

case class HunllefState(hun: Hunllef.Instance, attackCount: Int, playerAttackCount: Int, ticksTillNextAttack: Int, attackStyle: HunAttackStyle, prayerStyle: HunPrayStyle) {
	def headIcon(using client: Client): Option[HeadIcon] = {
		Option(EthanApiPlugin.getHeadIcon(hun.wrapped))
	}
}
object HunllefState {
	private def prayerStyleForHeadIcon(hi: HeadIcon): HunPrayStyle = {
		Option(hi).collect({
			case HeadIcon.MELEE => HunPrayStyle.Melee
			case HeadIcon.MAGIC => HunPrayStyle.Mage
			case HeadIcon.RANGED => HunPrayStyle.Range
		}).getOrElse(HunPrayStyle.None)
	}

	def apply(npc: Hunllef.Instance)(using client: Client): HunllefState = {
		HunllefState(npc, 4, 6, 0, HunAttackStyle.Range, prayerStyleForHeadIcon(EthanApiPlugin.getHeadIcon(npc.wrapped)))
	}
	def updateAttackCount(in: HunllefState): HunllefState = {
//		in.copy(attackCount = if (in.attackCount > 1) (in.attackCount - 1) else 4, ticksTillNextAttack = 6)
		in.copy(attackCount = Option(in.attackCount - 1).filter(_ > 0).getOrElse(4), ticksTillNextAttack = 6)
	}
	def tick(in: HunllefState): HunllefState = {
		in.copy(ticksTillNextAttack = Option(in.ticksTillNextAttack).filter(_ > 0).map(_ - 1).getOrElse(0))
	}

	def updatePlayerAttackCount(in: HunllefState)(using client: Client): HunllefState = {
		val newPAttackCount = Option(in.playerAttackCount - 1).filter(_ > 0).getOrElse(6)
		in.copy(playerAttackCount = newPAttackCount, prayerStyle = if(newPAttackCount < 6) in.prayerStyle else prayerStyleForHeadIcon(in.headIcon.orNull))
	}

	def changeStyle(in: HunllefState)(s: HunAttackStyle): HunllefState = {
		in.copy(attackStyle = s, attackCount = 4)
	}
}
case object EnterHunllef extends GauntletEvent
case object EnterGauntlet extends GauntletEvent
case object ExitHunllef extends GauntletEvent
case object ExitGauntlet extends GauntletEvent
case class EnablePrayer(prayer: Prayer) extends GauntletEvent {}
case class DisablePrayer(prayer: Prayer) extends GauntletEvent {}
case class EquipWeapon(weaponID: Int) extends GauntletEvent {}
case class EatPaddleFish() extends GauntletEvent {}
case class DrinkPotion() extends GauntletEvent {}

case object TickHunllef extends GauntletEvent {}
//	case class HunllefAnimation(source: Hunllef.Instance, animation: HunllefAnimationTrait) extends GauntletEvent
//	case class PlayerAnimation(animation: PlayerAnimationTrait) extends GauntletEvent

case object HalberdAttack extends PlayerAnimation {}
case object UnarmedAttack extends PlayerAnimation {}
case object RangeAttack extends PlayerAnimation {}
case object MageAttack extends PlayerAnimation {}
case object ArmedAttack extends PlayerAnimation {}
case object TornadoAttack extends HunllefAnimation {}
case object RegularAttack extends HunllefAnimation {}
case object SwitchToRange extends HunllefAnimation {}
case object SwitchToMage extends HunllefAnimation {}

object PlayerAnimationEvents {
	def unapply(in: Int): Option[PlayerAnimation] = {
		Option(in).collect {
			case 386 => ArmedAttack
			case 390 => ArmedAttack
			case 395 => ArmedAttack
			case 400 => ArmedAttack
			case 401 => ArmedAttack
			case 428 => HalberdAttack
			case 440 => HalberdAttack
			case 423 => UnarmedAttack
			case 422 => UnarmedAttack
			case 426 => RangeAttack
			case 1167 => MageAttack
		}
	}
}
object HunllefAnimationEvents {
	def unapply(in: Int): Option[HunllefAnimation] = {
		Option(in).collect {
			case 8418 => TornadoAttack
			case 8419 => RegularAttack
			case 8754 => SwitchToMage
			case 8755 => SwitchToRange
		}
	}
}
object LocalPlayer {
	def unapply(a: Actor)(using client: Client): Option[Player] = {
//		val local =
		a match {
			case player: Player if(player.equals(client.getLocalPlayer)) => Some(player)
			case _ => None
		}
	}
	def unapply(a: Option[Actor])(using client: Client): Option[Player] = {
		//		val local =
		a match {
			case Some(player: Player) if (player.equals(client.getLocalPlayer)) => Some(player)
			case _ => None
		}
	}
}

@Singleton
class GauntletSolver @Inject()(val client: Client, val clientThread: ClientThread, val overlayManager: OverlayManager) extends scala.swing.Reactor {
	private val log: Logger = ShimUtils.getLogger(classOf[GauntletSolver].getName, "TRACE")
//	var inHunllef: Boolean = false

	var hunllef: Option[HunllefState] = Option.empty[HunllefState]
//	def inHunllef(): Boolean = hunllef.isDefined
	var inGauntlet: Boolean = false
	given Client = client

	val overlay: PvmHelperOverlay = PvmHelperOverlay.create("GauntletSolver")(g => {
		client.getTopLevelWorldView.npcs().iterator().asScala.flatMap(n => GauntletNpcType.InstanceExtractor.unapply(n)).foreach(n =>{
//			val y: WorldPoint = n.wrapped.getWorldLocation()
			val color = GauntletNpcType.color(n)
			val lp = n.worldLocation.pipe(LocalPoint.fromWorld(client.getTopLevelWorldView, _))
			val polygon = Perspective.getCanvasTilePoly(client, lp)
			val str = n.toString
			OldOverlayUtil.drawOutlineAndFill(g, ColorUtil.colorWithAlpha(color, 192), ColorUtil.colorWithAlpha(color, 128), 2, polygon)
			OldOverlayUtil.renderTextLocation(g, Perspective.getCanvasTextLocation(client, g, lp, str, 20), str, Color.white)
			//			val poly2 = n.wrapped.getCanvasTilePoly
//			OverlayUtil.renderActorOverlay(g, n.wrapped, n.tpe.name, GauntletNpcType.color(n))
//			val poly = Perspective.getCanvasTilePoly(client, n.wrapped.getLocalLocation, 30)
//			OverlayUtil.renderPolygon(g, polygon, GauntletNpcType.color(n), Color.BLACK, new BasicStroke(4))
		})
//		g.drawRoundRect(10, 20, 40, 70, 12, 12)

		null.asInstanceOf[Dimension]
	}).tap(_.setPosition(OverlayPosition.DYNAMIC)).tap(_.setLayer(OverlayLayer.ABOVE_SCENE))

	def handle(e: GauntletEvent): Unit = {
//		case o =>
		e match {
			case EnterHunllef => {
				hunllef = ethanApiPlugin.collections.NPCs.search().withId(Hunllef.ids *).nearestToPlayer().toScala.flatMap(Hunllef.Instance.unapply(_))
					.map(h => HunllefState.apply(h))
			}
			case EnterGauntlet => {
				overlayManager.add(overlay)
				inGauntlet = true
			}
			case ExitHunllef => hunllef = None
			case ExitGauntlet => {
				overlayManager.remove(overlay)
				inGauntlet = false
			}
			case EnablePrayer(prayer) => PrayerInteraction.setPrayerState(prayer, true)
			case DisablePrayer(prayer) => PrayerInteraction.setPrayerState(prayer, false)
			case TornadoAttack if(hunllef.nonEmpty) => hunllef = hunllef.map(HunllefState.updateAttackCount)
			case RegularAttack if(hunllef.nonEmpty) => hunllef = hunllef.map(HunllefState.updateAttackCount)
			case SwitchToRange if(hunllef.nonEmpty) => hunllef = hunllef.map(HunllefState.changeStyle(_)(HunAttackStyle.Range))
			case SwitchToMage  if(hunllef.nonEmpty) => hunllef = hunllef.map(HunllefState.changeStyle(_)(HunAttackStyle.Mage))
			case UnarmedAttack if(hunllef.map(_.prayerStyle).exists(_ != HunPrayStyle.Melee)) => hunllef = hunllef.map(HunllefState.updatePlayerAttackCount)
			case ArmedAttack   if(hunllef.map(_.prayerStyle).exists(_ != HunPrayStyle.Melee)) => hunllef = hunllef.map(HunllefState.updatePlayerAttackCount)
			case HalberdAttack if(hunllef.map(_.prayerStyle).exists(_ != HunPrayStyle.Melee)) => hunllef = hunllef.map(HunllefState.updatePlayerAttackCount)
			case RangeAttack   if(hunllef.map(_.prayerStyle).exists(_ != HunPrayStyle.Range)) => hunllef = hunllef.map(HunllefState.updatePlayerAttackCount)
			case MageAttack    if(hunllef.map(_.prayerStyle).exists(_ != HunPrayStyle.Mage )) => hunllef = hunllef.map(HunllefState.updatePlayerAttackCount)
			case TickHunllef if(hunllef.nonEmpty) => {
				hunllef = hunllef.map(HunllefState.tick)
				log.debug("State = {}", hunllef.get)
			}
			case u => log.debug("unhandled event {}", u)
		}
	}
	reactions += {
		case ClientEvent.VarbitChanged(9178, 1) if(!inGauntlet) => handle(EnterGauntlet)
		case ClientEvent.VarbitChanged(9178, 0) if(inGauntlet) => handle(ExitGauntlet)
		case ClientEvent.VarbitChanged(9177, 1) if(hunllef.isEmpty) => handle(EnterHunllef)
		case ClientEvent.VarbitChanged(9177, 0) if(hunllef.nonEmpty) => handle(ExitHunllef)
	}
	reactions += {
		case NpcEvent.InteractingChanged(     Bear(), None, LocalPlayer(_)) if(inGauntlet && hunllef.isEmpty) => handle(EnablePrayer(Prayer.PROTECT_FROM_MELEE))
		case NpcEvent.InteractingChanged(DarkBeast(), None, LocalPlayer(_)) if(inGauntlet && hunllef.isEmpty) => handle(EnablePrayer(Prayer.PROTECT_FROM_MISSILES))
		case NpcEvent.InteractingChanged(   Dragon(), None, LocalPlayer(_)) if(inGauntlet && hunllef.isEmpty) => handle(EnablePrayer(Prayer.PROTECT_FROM_MAGIC))
		case NpcEvent.InteractingChanged(     Bear(), LocalPlayer(_), None) if(inGauntlet && hunllef.isEmpty) => handle(DisablePrayer(Prayer.PROTECT_FROM_MELEE))
		case NpcEvent.InteractingChanged(DarkBeast(), LocalPlayer(_), None) if(inGauntlet && hunllef.isEmpty) => handle(DisablePrayer(Prayer.PROTECT_FROM_MISSILES))
		case NpcEvent.InteractingChanged(   Dragon(), LocalPlayer(_), None) if(inGauntlet && hunllef.isEmpty) => handle(DisablePrayer(Prayer.PROTECT_FROM_MAGIC))
	}
	reactions += {
		case NpcEvent.AnimationChanged(Hunllef(), _, HunllefAnimationEvents(ha)) if(hunllef.nonEmpty) => handle(ha)
		case PlayerEvent.AnimationChanged(LocalPlayer(_), o, PlayerAnimationEvents(pa)) if(inGauntlet) => handle(pa)
		case ClientEvent.ServerTick(_) if(hunllef.nonEmpty) => {
			handle(TickHunllef)
//			log.debug("hunllefState = {}", hunllef.get)
		}
	}
}
