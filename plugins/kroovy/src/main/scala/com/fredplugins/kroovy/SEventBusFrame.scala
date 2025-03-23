package com.fredplugins.kroovy

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.SEventBus.{Refresh, SEventBusEvent, SubscriberType}
import com.fredplugins.kroovy.swing.{MField, MTableModel}
import com.google.inject.Inject
import net.runelite.api.events.{NpcDespawned, NpcSpawned}
import net.runelite.api.{Client, GameState, NPC, Player}

import java.awt.Color
import java.util
import javax.inject.Singleton
import javax.swing.{DefaultListModel, WindowConstants}
import javax.swing.table.JTableHeader
import scala.collection.mutable.ArrayBuffer
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.reflect.ClassTag
import scala.swing.BorderPanel.Position
import scala.swing.Swing.{Embossing, Raised}
import scala.swing.Table.{ElementMode, IntervalMode}
import scala.swing.event.{ButtonClicked, Event, TableColumnsSelected, TableEvent, TableRowsSelected}
import scala.swing.{AbstractButton, Action, BorderPanel, BoxPanel, Button, Component, Frame, Label, ListView, Orientation, ScrollPane, SplitPane, Swing, Table, TextArea, ToggleButton, event}
import scala.util.chaining.*
import scala.util.{Random, Try}

object SEventBusFrame extends ShimUtils.Logging("Debug") {
	private var _frame: Frame = _

//	val rt: RType[SubscriberType[NpcSpawned, 3, "root"]] =  RType.of[SEventBus.SubscriberType[NpcSpawned, 3, "root"]]

	type RowType = SEventBus.SubscriberType[?, ?, ?]
	import MTableModel.{getter2, setter2}
	val eventsTableModel = MTableModel[RowType]{
		getter2["Priority", Int, RowType].apply(_.priority)
		setter2["Enabled", Boolean, RowType].apply(_.getEnabled, (r, v) => r.setEnabled_=(v))
		getter2["Event", Class[?], RowType].apply(_.eClazz)
		getter2["Owner", SEventBus.OwnerType, RowType].apply(_.owner)
		getter2["Group", String, RowType].apply(_.group)
		getter2["Callback", String, RowType].apply(_.hashCode().pipe(Integer.toHexString(_)))
	}

	def get(using client: Client, bus: SEventBus): Frame = {

		object ButtonBar extends BoxPanel(Orientation.Horizontal) {
			val clearBtn   : Button = new Button("Clear")
			val addDummyBtn: Button = new Button("AddDummy")
			val refreshBtn : Button = new Button("Refresh")

			contents += clearBtn
			contents += addDummyBtn
			contents += refreshBtn
		}
		object SideButtonBar extends BoxPanel(Orientation.Vertical) {
			val unregisterBtn   : Button = new Button().tap(b => {
				b.tooltip = "unregister"
				b.icon = Icons.DELETE_ICON.icon
				b.rolloverIcon = Icons.DELETE_ICON.icon_selected
			})
			val priorityPlusBtn : Button = new Button().tap(b => {
				b.tooltip = "priority +"
				b.icon = Icons.ADD_ICON.icon
				b.rolloverIcon = Icons.ADD_ICON.icon_selected
			})
			val priorityMinusBtn: Button = new Button().tap(b => {
				b.tooltip = "priority -"
				b.icon = Icons.MINUS_ICON.icon
				b.rolloverIcon = Icons.MINUS_ICON.icon_selected
			})

			contents += unregisterBtn
			contents += priorityPlusBtn
			contents += priorityMinusBtn
		}

		object EventsTable extends Table(eventsTableModel) {
			selection.elementMode = ElementMode.Row
			selection.intervalMode = IntervalMode.Single

		}

		def createPanel(): scala.swing.Panel = {
			new BorderPanel() {
				add(ButtonBar, Position.North)
				add(SideButtonBar, Position.West)
				add(ScrollPane(EventsTable), Position.Center)
				//		new BoxPanel(Orientation.Horizontal) {
				//	//		val panelSelf = this
				//			contents += clearBtn
				//			contents += freezeBtn
				//			contents += addDummy
				//		}, Position.North
				//	)//header panel
				//	add(new ScrollPane(eventsList), Position.Center)
				listenTo(bus)
				listenTo(ButtonBar)
				listenTo(SideButtonBar)
				listenTo(EventsTable)

				reactions += {
					case r@Refresh() => eventsTableModel.setData(bus.all())
					case x => log.debug(s"panel reaction has value ${x}")
				}
			}
		}

		if(_frame == null) {
			_frame = new Frame() {
				contents = createPanel()
			}.tap(mf => {
				mf.pack()
				mf.centerOnScreen()
			})
		}
		_frame
	}
//
//	def open(): Unit = {
//		assert(_frame != null)
//		_frame.open()
//	}
//	def close(): Unit = {
//		assert(_frame != null)
//		_frame.close()
//	}
}

//class SEventBusPanel(using client: Client, bus: SEventBus) extends BorderPanel {
//	private val eventsList: ListView[SEventBus.SubscriberType[?, ?, ?]] = new ListView[SEventBus.SubscriberType[?, ?, ?]]() {
//		renderer = new ListView.Renderer[SEventBus.SubscriberType[?, ?, ?]] {

//			override def componentFor(list: ListView[_ <: SEventBus.SubscriberType[?, ?, ?]], isSelected: Boolean, focused: Boolean, a: SEventBus.SubscriberType[?, ?, ?], index: Int): Component = {
//				val aa = CellComponent(a.eClazz.getSimpleName, a.priority, a.group, a.owner)
//				(for {
//					(idx, eName, eValue) <- {
//						(0 until aa.productArity).flatMap(idx => Try { (idx, aa.productElementName(idx), aa.productElement(idx)) }.toOption)
//					}
//				} yield {
//					new BoxPanel(swing.Orientation.Horizontal) {
//						background = Color.DARK_GRAY
//						this.contents += Label(eName).tap(_.foreground = Color.WHITE)
//						this.contents += Label(": ").tap(_.foreground = Color.LIGHT_GRAY)
//						this.contents += Label(s"${idx}").tap(_.foreground = Color.GREEN)
//						this.contents += Label(" = ").tap(_.foreground = Color.LIGHT_GRAY)
//						this.contents += Label(eValue.toString).tap(_.foreground = Color.ORANGE)//componentFor(eValue)
//						border = Swing.BeveledBorder(Swing.Lowered)
//					}
//				}).pipe(elementsComponenets =>
//					elementsComponenets.flatMap(e => if (elementsComponenets.last == e) Seq(e) else Seq(e, Label(", ").tap(_.foreground = Color.LIGHT_GRAY)))
//						.prependedAll(Seq(
//							Label(aa.productPrefix).tap(_.foreground = Color.BLUE), Label("(").tap(_.foreground = Color.LIGHT_GRAY)
//						))
//						.appended(Label(")").tap(_.foreground = Color.LIGHT_GRAY))
//				).pipe(lineContents => {
//					new BoxPanel(swing.Orientation.Horizontal) {
//						contents.addAll(lineContents)
//						border = Swing.BeveledBorder(Swing.Lowered)
//						if (isSelected) {
//							background = Color.BLUE
//						}
//					}
//				})
//			}
//		}
//	}
//
//	private val clearBtn: AbstractButton = Button("Clear"){
//		eventsList.listData = Seq.empty[SubscriberType[?, ?, ?]]
//	}
//	private val addDummy: AbstractButton = Button("addDummy") {
//		bus.register[NpcDespawned, 3, "Self"](SEventBusFrame.get){event =>
//			Option(event.getActor).collect{
//				case p: NPC => s"NPC(\"${p.getName}\", ${p.getId}, ${p.getIndex})"
//			}.map(s => s"${s} despawned")
//		}
//	}
//	private val freezeBtn: AbstractButton = Button("Refresh") {
//		this.publish(Refresh())
////		action = Action("Refresh") {
//			eventsList.listData = bus.all()
////		}
//	}
//	reactions += {
//		case event: Refresh => eventsList.listData = bus.all()
//	}
//
//	add(
//		new BoxPanel(Orientation.Horizontal) {
//	//		val panelSelf = this
//			contents += clearBtn
//			contents += freezeBtn
//			contents += addDummy
//		}, Position.North
//	)//header panel
//	add(new ScrollPane(eventsList), Position.Center)
//	//	add(, Position.Center)
//
//
//	eventsList.listenTo(this)
//
//	listenTo(bus)
//}
