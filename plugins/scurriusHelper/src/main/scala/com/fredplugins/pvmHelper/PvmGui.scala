package com.fredplugins.pvmHelper

import com.fredplugins.pvmHelper.PvmGui.SEvent.*
import com.fredplugins.pvmHelper.PvmGui.{SEvent}
import net.runelite.api.{NPC, NPCComposition, Player}

import javax.swing.WindowConstants
import scala.swing.*
import scala.swing.BorderPanel.Position
import scala.swing.RichWindow.Undecorated
import scala.swing.event.{ActionEvent, Event}
import scala.util.Try
import scala.util.chaining.*

object PvmGui {
	extension [S <: Component](s: S) {

		def restrictMinHeight(i: Int = s.preferredSize.height): S = {
			s.minimumSize = s.minimumSize.pipe(d => new Dimension(d.width, i))
			s
		}
		def restrictMaxHeight(i: Int = s.preferredSize.height): S = {
			s.maximumSize = s.maximumSize.pipe(d => new Dimension(d.width, i))
			s
		}
		def restrictHeight(min: Int, max: Int): S = {
			assert(min <= max)
			restrictMinHeight(min)
			restrictMaxHeight(max)
			s
		}
	}

	transparent sealed trait SEventSourceTrait extends Publisher {
	}

	object SEvent {
		sealed transparent trait SEvent extends Event {
			val debugStr: String
		}
		case object SClose extends SEvent {
			override val debugStr: String = "SClose"
		}

		class SNpcSpawned(val index: Int, val id: Int, val animationId: Int, val composition: NPCComposition) extends SEvent {
			//			val index: Int = npc.getIndex
			//			val id: Int = npc.getId
			//      val animationId: Int = npc.getAnimation
			//			val composition: NPCComposition = npc.getComposition
			//			def this(npc: NPC) = this(npc.getIndex, npc.getId, npc.getAnimation, npc.getComposition)
			override val debugStr: String = s"SNpcSpawned(index=${index},id=${id},animationId=${animationId}, composition=${Try(composition.toString).getOrElse("null")})"
		}

		class SNpcDespawned(val index: Int, val id: Int, val composition: NPCComposition) extends SEvent {
			//				val index: Int = npc.getIndex
			//				val id: Int = npc.getId
			//				val composition: NPCComposition = npc.getComposition
			//				def this(npc: NPC) = this(npc.getIndex, npc.getId, npc.getComposition)
			override val debugStr: String = s"SNpcDespawned(index=${index},id=${id}, composition=${Try(composition.toString).getOrElse("null")})"
		}

		class SNpcAnimationChanged(val index: Int, val id: Int, val animationId: Int, val previousAnimationId: Int) extends SEvent {
			//				val index: Int = npc.getIndex
			//				val id: Int = npc.getId
			//				val animationId: Int = npc.getAnimation
			//				def this(npc: NPC, prev: Int) = this(npc.getIndex, npc.getId, npc.getAnimation, prev)
			override val debugStr: String = s"SNpcAnimationChanged(index=${index},id=${id}, previous=${previousAnimationId}, current=${animationId})"
		}

		class SNpcCompositionChanged(val index: Int, val id: Int, val composition: NPCComposition, val previousComposition: NPCComposition) extends SEvent {
			//				val index: Int = npc.getIndex
			//				val id: Int = npc.getId
			//				val composition: NPCComposition = npc.getComposition
			//				def this(npc: NPC, prev: NPCComposition) = this(npc.getIndex, npc.getId, npc.getComposition, prev)

			override val debugStr: String = s"SNpcCompositionChanged(index=${index},id=${id}, previous=${Try(previousComposition.getId).getOrElse(-1)}, current=${Option(composition).map(_.getId).getOrElse(-1)})"
		}
		//			class SPlayerAnimationChanged(player: Player, val previousAnimationId: Int) extends SEvent {
		//				val animationId: Int = player.getAnimation
		//				val id: Int = player.getId
		//				val name: String = player.getName
		//				override val debugStr: String = s"SPlayerAnimationChanged(id=${id}, name=${name}, previous=${previousAnimationId}, current=${animationId})"
		//			}
		//			class SRegionChanged(val previousRegionId: Int, val regionId: Int) extends SEvent {
		//				override val debugStr: String = s"SRegionChanged(previous=${previousRegionId},current=${regionId})"
		//			}
	}

}

class PvmGui() extends scala.swing.Frame {

	import PvmGui.{restrictMinHeight, restrictMaxHeight}

	title = "Hello world"

	override def closeOperation(): Unit = {
		if(peer.getState != java.awt.Frame.ICONIFIED)
			peer.setState(java.awt.Frame.ICONIFIED)
	}

	private val eventPanel: BoxPanel = new BoxPanel(Orientation.Vertical) {
	}
	private val eventPanelScroll = new ScrollPane(eventPanel).restrictMinHeight(200).tap(_.preferredSize = new Dimension(300, 300))

	private def addToEventPanel(c: Component): Unit = {
		eventPanel.contents.prepend(c)
		eventPanelScroll.revalidate()
	}

	private val headerPanel = new BorderPanel {
		add(
			new Label("Launch rainbows:"),
			Position.West
		)
		add(
			new Button("Click me") {
				private var count: Int = 0
				reactions += {
					case event.ButtonClicked(_) => {
						addToEventPanel(new Label(s"All the colours ${count}"))
						count = count + 1
					}
				}
			},
			Position.East
		)
	}.restrictMaxHeight()
	contents = new BoxPanel(Orientation.Vertical) {
		contents += headerPanel
		contents += eventPanelScroll
	}

	reactions += {
		case sEvent: SEvent => {
			addToEventPanel(Label(sEvent.debugStr))
		}
		//		case spawned: SNpcSpawned => {addToEventPanel(Label(spawned.debugStr))}
		//		case despawned: SNpcDespawned => {addToEventPanel(Label(despawned.debugStr))}
		//		case compositionChanged: SNpcCompositionChanged => {addToEventPanel(Label(compositionChanged.debugStr))}
		//		case animationChanged: SNpcAnimationChanged => {addToEventPanel(Label(animationChanged.debugStr))}
		//		case pAnimationChanged: SPlayerAnimationChanged => {addToEventPanel(Label(pAnimationChanged.debugStr))}
		//		case regionChanged: SRegionChanged => {addToEventPanel(Label(regionChanged.debugStr))}
	}


	peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
	pack()
	centerOnScreen()
	open()
}

