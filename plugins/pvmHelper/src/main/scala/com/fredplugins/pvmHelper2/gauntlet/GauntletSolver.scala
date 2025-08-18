package com.fredplugins.pvmHelper2.gauntlet

import com.fredplugins.common.OldOverlayUtil
import com.fredplugins.common.utils.{ShimUtils, WorldPointUtils}
import com.fredplugins.pvmHelper2.ClientEvent.ServerTick
import com.fredplugins.pvmHelper2.gauntlet.{ArmedAttack, HalberdAttack, MageAttack, RangeAttack, UnarmedAttack}
import com.fredplugins.pvmHelper2.gauntlet.{RegularAttack, SwitchToMage, SwitchToRange, TornadoAttack}
import com.fredplugins.pvmHelper2.gauntlet.{EnterGauntlet, EnterHunllef, ExitGauntlet, ExitHunllef, HunllefAnimationEvents, TickHunllef}
import com.fredplugins.pvmHelper2.{ClientEvent, Extractors, NpcEvent, PlayerEvent, PvmEvent, PvmHelperOverlay, PvmHelperPanel, PvmModule, gauntlet}
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.lucidplugins.api.utils.{CombatUtils, NpcUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.query.NPCQuery
import com.fredplugins.common.interactionApi.PrayerInteraction
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.{Actor, Client, HeadIcon, NPC, Perspective, Player, Prayer}
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger

import scala.util.chaining.*
import net.runelite.api.events.{ActorDeath, AnimationChanged, InteractingChanged, NpcDespawned, NpcSpawned, VarbitChanged}
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayManager, OverlayPosition, OverlayUtil}
import net.runelite.client.util.ColorUtil

import java.awt.{BasicStroke, Color, Dimension, Font}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, IterableHasAsScala, IteratorHasAsScala, SeqHasAsJava}
import scala.jdk.OptionConverters.RichOptional
import scala.quoted.Type
import scala.reflect.Typeable
import scala.swing.Reactions
import scala.swing.Reactions.Reaction
import scala.swing.event.Event

sealed trait GauntletEvent {
	self: Product =>
}
sealed trait EnterExitEvent(val varbitId: Int, val targetValue: Int) extends GauntletEvent {
	self: Product =>
	def unapply(event: ClientEvent.VarbitChanged): Boolean = {
		event match {
			case ClientEvent.VarbitChanged(`varbitId`, _, `targetValue`) => true
			case _ => false
		}
	}
	def pvmEvent: ClientEvent.VarbitChanged = ClientEvent.VarbitChanged(varbitId, if(targetValue == 0) 1 else 0, targetValue)
}
sealed trait PlayerAnimation(val playerAttackType: PlayerAttackType, val animationIds: Int *) extends GauntletEvent {
	self: Product =>
	def unapply(event: PlayerEvent)(using client: Client): Option[PlayerAttackType] = {
		Option(event).collect {
			case PlayerEvent.AnimationChanged(LocalPlayer(_), o,  n) if animationIds.contains(n) => playerAttackType
		}
	}
//	def pvmEvents(using client: Client): Seq[PlayerEvent.AnimationChanged] = animationIds.map(aid => PlayerEvent.AnimationChanged(client.getLocalPlayer, -1, aid))
}
sealed trait HunllefAnimation extends GauntletEvent {
	self: Product =>
}

case class HunllefState(hun: Hunllef.Instance, attackCount: Int, playerAttackCount: Int, ticksTillNextAttack: Int, attackStyle: HunllefAttackStyle, prayerStyle: HunllefProtectStyle) {}
object HunllefState {
	private def prayerStyleForHeadIcon(hun: Hunllef.Instance): Option[HunllefProtectStyle] = {
		Option(EthanApiPlugin.getHeadIcon(hun.wrapped)).collect {
			case HunllefProtectStyle(u) => u
		}
	}

	def apply(npc: Hunllef.Instance): Option[HunllefState] = {
		prayerStyleForHeadIcon(npc).map(x =>
			HunllefState(npc, 4, 6, 0, HunllefAttackStyle.Range, x)
		)
	}

	def updateAttackCount(in: HunllefState): HunllefState = {
		in.copy(attackCount = Option(in.attackCount - 1).filter(_ > 0).getOrElse(4), ticksTillNextAttack = 6)
	}

	def tick(in: HunllefState): HunllefState = {
		in.copy(ticksTillNextAttack = Option(in.ticksTillNextAttack).filter(_ > 0).map(_ - 1).getOrElse(0))
	}

	def updatePlayerAttackCount(in: HunllefState): HunllefState = {
		val newPAttackCount = Option(in.playerAttackCount - 1).filter(_ > 0).getOrElse(6)
		in.copy(playerAttackCount = newPAttackCount, prayerStyle = if(newPAttackCount < 6) in.prayerStyle else prayerStyleForHeadIcon(in.hun).get)
	}

	def changeStyle(s: HunllefAttackStyle)(in: HunllefState): HunllefState = {
		in.copy(attackStyle = s, attackCount = 4)
	}
}
case object EnterHunllef  extends EnterExitEvent(9177, 1) {}
case object EnterGauntlet extends EnterExitEvent(9178, 1) {}
case object ExitHunllef   extends EnterExitEvent(9177, 0) {}
case object ExitGauntlet  extends EnterExitEvent(9178, 0) {}

case object TickHunllef extends GauntletEvent {}
//	case class HunllefAnimation(source: Hunllef.Instance, animation: HunllefAnimationTrait) extends GauntletEvent
//	case class PlayerAnimation(animation: PlayerAnimationTrait) extends GauntletEvent

case object HalberdAttack extends PlayerAnimation(PlayerAttackType.Melee, 428, 440) {}
case object UnarmedAttack extends PlayerAnimation(PlayerAttackType.Melee, 423, 422) {}
case object ArmedAttack extends PlayerAnimation(PlayerAttackType.Melee, 386, 390, 395, 400, 401) {}
case object RangeAttack extends PlayerAnimation(PlayerAttackType.Range, 426) {}
case object MageAttack extends PlayerAnimation(PlayerAttackType.Mage, 1167) {}

case object TornadoAttack extends HunllefAnimation {}
case object RegularAttack extends HunllefAnimation {}
case object SwitchToRange extends HunllefAnimation {}
case object SwitchToMage extends HunllefAnimation {}

//object PlayerAnimationEvents {
//	def unapply(in: Int): Option[PlayerAnimation] = {
//		Option(in).collect {
//			case 386 => ArmedAttack
//			case 390 => ArmedAttack
//			case 395 => ArmedAttack
//			case 400 => ArmedAttack
//			case 401 => ArmedAttack
//			case 428 => HalberdAttack
//			case 440 => HalberdAttack
//			case 423 => UnarmedAttack
//			case 422 => UnarmedAttack
//			case 426 => RangeAttack
//			case 1167 => MageAttack
//		}
//	}
//}
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
	def unapply(a: Option[Actor])(using client: Client): Option[Player] = {
		a match {
			case Some(player: Player) if (player.equals(client.getLocalPlayer)) => Some(player)
			case _ => None
		}
	}
	def unapply(a: Actor)(using client: Client): Option[Player] = {
		a match {
			case player: Player if (player.equals(client.getLocalPlayer)) => Some(player)
			case _ => None
		}
	}
}
@Singleton
class GauntletSolver @Inject()(val client: Client, val clientThread: ClientThread, val overlayManager: OverlayManager) extends PvmModule {
	private val log: Logger = ShimUtils.getLogger(classOf[GauntletSolver].getName, "TRACE")
	import PvmModule.conditionedPartialFunction
	//	var inHunllef: Boolean = false

	given Client = client
	given ClientThread = clientThread
	given OverlayManager = overlayManager

	var hunllef: Option[HunllefState] = Option.empty[HunllefState]
	var ticksTillNextAttack = 0
	var inGauntlet: Boolean = false
	val overlay: PvmHelperOverlay = PvmHelperOverlay.create("GauntletSolver")(g => {
		if(inGauntlet) {
			hunllef match {
				case Some(HunllefState(hun, attackCount, playerAttackCount, ticksTillNextAttack, attackStyle, prayerStyle)) => {
					val str = s"${attackCount} | ${playerAttackCount}"
					Option(hun.wrapped.getCanvasTextLocation(g, str, 30))
						.foreach(point => {

							val originalFont = g.getFont();

							g.setFont(new Font(Font.SANS_SERIF,
								Font.PLAIN, 22));

							OldOverlayUtil.renderTextLocation(g, point, str, attackStyle match {
								case HunllefAttackStyle.Mage => Color.CYAN
								case HunllefAttackStyle.Range => Color.GREEN
							})

							g.setFont(originalFont);
						})

					Option(hun.wrapped.getComposition)
						.flatMap(composition => {
							Option(Perspective.getCanvasTileAreaPoly(client, hun.wrapped.getLocalLocation(), composition.getSize))
						})
						.foreach(polygon => {
							PvmHelperOverlay.drawOutlineAndFill(Color.WHITE, new Color(255, 255, 255, 0),
								1, polygon)(g)
						})

				}
				case None => {
					client.getTopLevelWorldView.npcs().iterator().asScala.flatMap(n => GauntletNpcType.InstanceExtractor.unapply(n)).foreach(n => {
						val color = GauntletNpcType.color(n)
						Option(n.wrapped.getComposition)
							.flatMap(composition => {
								Option(Perspective.getCanvasTileAreaPoly(client, n.wrapped.getLocalLocation(), composition.getSize))
							})
							.foreach(polygon => {
								PvmHelperOverlay.drawOutlineAndFill(color, ColorUtil.colorWithAlpha(color, 32),
									1, polygon)(g)
							})

//						OldOverlayUtil.drawOutlineAndFill(g, ColorUtil.colorWithAlpha(color, 192), ColorUtil.colorWithAlpha(color, 128), 2, polygon)
//						OldOverlayUtil.renderTextLocation(g, Perspective.getCanvasTextLocation(client, g, lp, str, 50), str, Color.white)
					})
				}
			}
			val str = s"${ticksTillNextAttack}"
			Option(client.getLocalPlayer.getCanvasTextLocation(g, str, 0))
				.foreach(point => {

					val originalFont = g.getFont();

					g.setFont(new Font(Font.SANS_SERIF,
						Font.PLAIN, 22));

					OldOverlayUtil.renderTextLocation(g, point, str, Color.BLUE)

					g.setFont(originalFont);
				})

			Option(Perspective.getCanvasTileAreaPoly(client, client.getLocalPlayer.getLocalLocation(), 1))
				.foreach(polygon => {
					PvmHelperOverlay.drawOutlineAndFill(Color.PINK, ColorUtil.colorWithAlpha(Color.PINK, 32), 1, polygon)(g)
				})
		}
//		g.drawRoundRect(10, 20, 40, 70, 12, 12)
		null.asInstanceOf[Dimension]
	}).tap(_.setPosition(OverlayPosition.DYNAMIC)).tap(_.setLayer(OverlayLayer.ABOVE_SCENE))


	val panel: PvmHelperPanel = PvmHelperPanel.create("GauntletSolver"){
		Option.when(inGauntlet) {
			val seg1 = hunllef match {
				case Some(hs@HunllefState(hun, attackCount, playerAttackCount, ticksTillNextAttack, attackStyle, prayerStyle)) => {
					hs.productElementNames.zip(hs.productIterator).toSeq
						.map(d => LineComponent.builder.left(d._1).right(Option(d._2).map(_.toString).getOrElse("----")).build()).prepended(TitleComponent.builder.text("In Hunleff").build())
				}
				case None => {
					val titleElement = TitleComponent.builder.text("Npcs").build()
					val npcsElements = client.getTopLevelWorldView.npcs().iterator().asScala.flatMap(n => GauntletNpcType.InstanceExtractor.unapply(n))
						.map(n => {
							LineComponent.builder.left(n.tpe.name).leftColor(GauntletNpcType.color(n)).right(n.toString).build()
						}).toSeq

					Option.when(npcsElements.nonEmpty)(npcsElements.prepended(titleElement)).getOrElse(Seq.empty)
				}
			}

			val ticksTillNextElement = LineComponent.builder.left("Ticks Till Attack").right(s"${ticksTillNextAttack}").build()
			Seq(ticksTillNextElement) ++ seg1
		}.fold(Seq.empty[LayoutableRenderableEntity])(u => u)
	}.tap(p => {
//		p.setLayer(OverlayLayer.ABOVE_SCENE)
//		p.setPosition(OverlayPosition.BOTTOM_LEFT)
	})

	reactions += {
		case EnterGauntlet() if !inGauntlet => {
			inGauntlet = true
		}
		case ExitGauntlet() if inGauntlet => {
			inGauntlet = false
		}
		case EnterHunllef() if hunllef.isEmpty => {
			hunllef = ethanApiPlugin.collections.NPCs.search().withId(Hunllef.ids *).nearestToPlayer().toScala.flatMap(Hunllef.Instance.unapply(_)).flatMap(h => HunllefState.apply(h))
		}
		case ExitHunllef() if hunllef.nonEmpty => {
			hunllef = None
		}
	}

	reactions += {
		(conditionedPartialFunction(inGauntlet && hunllef.isEmpty)({
			case NpcEvent.InteractingChanged(     Bear(), None, u@LocalPlayer(pl)) => Prayer.PROTECT_FROM_MELEE -> true
			case NpcEvent.InteractingChanged(DarkBeast(), None, LocalPlayer(_)) => Prayer.PROTECT_FROM_MISSILES -> true
			case NpcEvent.InteractingChanged(   Dragon(), None, LocalPlayer(_)) => Prayer.PROTECT_FROM_MAGIC -> true
			case NpcEvent.InteractingChanged(     Bear(), LocalPlayer(_), None) => Prayer.PROTECT_FROM_MELEE -> false
			case NpcEvent.InteractingChanged(DarkBeast(), LocalPlayer(_), None) => Prayer.PROTECT_FROM_MISSILES -> false
			case NpcEvent.InteractingChanged(   Dragon(), LocalPlayer(_), None) => Prayer.PROTECT_FROM_MAGIC -> false
		}).andThen {
			case (p, bool) => PrayerInteraction.setPrayerState(p, bool)
		})
	}


	reactions += {
		(conditionedPartialFunction(inGauntlet) {
			case HalberdAttack(_) => 4
			case UnarmedAttack(_) => 4
			case RangeAttack(_) => 4
			case MageAttack(_) => 4
			case ArmedAttack(_) => 5
			case ClientEvent.ServerTick(_) => math.max(0, ticksTillNextAttack - 1)
		}).andThen(i => ticksTillNextAttack = i)
	}

	reactions += {
		object Valid {
				def unapply(arg: PlayerAttackType): Boolean = hunllef.fold(false)(_.prayerStyle.validAttacks.contains(arg))
		}
		(conditionedPartialFunction(hunllef.nonEmpty){
			case HalberdAttack(Valid()) => HunllefState.updatePlayerAttackCount(_)
			case ArmedAttack(Valid()) => HunllefState.updatePlayerAttackCount(_)
			case UnarmedAttack(Valid()) => HunllefState.updatePlayerAttackCount(_)
			case RangeAttack(Valid()) => HunllefState.updatePlayerAttackCount(_)
			case MageAttack(Valid()) => HunllefState.updatePlayerAttackCount(_)
			case NpcEvent.AnimationChanged(Hunllef(), _, HunllefAnimationEvents(`TornadoAttack`)) => HunllefState.updateAttackCount(_)
			case NpcEvent.AnimationChanged(Hunllef(), _, HunllefAnimationEvents(`RegularAttack`)) => HunllefState.updateAttackCount(_)
			case NpcEvent.AnimationChanged(Hunllef(), _, HunllefAnimationEvents(`SwitchToRange`)) => HunllefState.changeStyle(HunllefAttackStyle.Range)(_)
			case NpcEvent.AnimationChanged(Hunllef(), _, HunllefAnimationEvents(`SwitchToMage`)) =>  HunllefState.changeStyle(HunllefAttackStyle.Mage)(_)
			case ClientEvent.ServerTick(_) => {
				(a: HunllefState) => {
					HunllefState.tick(a).tap(s => {
//							log.debug("State = {}", s)
					})
				}
			}
		}).andThen(j => {
			hunllef = hunllef.map(j)
		})
	}

	override def onStart(): Unit = {
		overlayManager.add(overlay)
		overlayManager.add(panel)

		clientThread.runOnClientThread(() => {
			Seq(EnterGauntlet, EnterHunllef).flatMap(vid=> {
				val temp = client.getVarbitValue(vid.varbitId)
				if(temp == vid.targetValue) Option(vid.pvmEvent)
				else Option.empty
			})
		}).foreach(reactions(_))
	}
	override def onStop(): Unit = {
		overlayManager.remove(overlay)
		overlayManager.remove(panel)
		this.reactions.apply(ExitHunllef.pvmEvent)
		this.reactions.apply(ExitGauntlet.pvmEvent)
	}
}
