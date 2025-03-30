package com.fredplugins.kroovy

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.swing.MTableModel
import net.runelite.api.Client

import java.awt.Color
import scala.compiletime.uninitialized
import scala.swing.BorderPanel.Position
import scala.swing.Table.{ElementMode, IntervalMode}
import scala.swing.event.{ButtonClicked, FocusEvent, TableChange, TableChanged, TableEvent, UIEvent}
import scala.swing.{AbstractButton, Action, BorderPanel, BoxPanel, Button, Frame, Orientation, RichWindow, ScrollPane, Table, TextArea, UIElement}
import scala.util.chaining.*

object SEventBusFrame extends ShimUtils.Logging("Debug") {
	private var _frame: Frame = uninitialized

	type RowType = SEventBus.SubscriberType[?, ?, ?]
	import MTableModel.{getter2, setter2}
	val eventsTableModel = MTableModel[RowType]{
		getter2["Priority", java.lang.Integer, RowType].apply(_.priority)
		setter2["Enabled", java.lang.Boolean, RowType].apply(_.getEnabled, (r, v) => r.setEnabled_=(v))
		getter2["Event", Class[?], RowType].apply(_.eClazz)
		getter2["Owner", SEventBus.OwnerType, RowType].apply(_.owner)
		getter2["Group", String, RowType].apply(_.group)
		getter2["Callback", String, RowType].apply(_.hashCode().pipe(Integer.toHexString(_)))
	}

	def get(using bus: SEventBus): Frame = {
		import com.fredplugins.kroovy.swing.ButtonFactory.*
		import com.fredplugins.kroovy.swing.PanelFactory.*

		object ButtonBar extends BoxPanel(Orientation.Horizontal) {bar =>
			val clearBtn   : Button = TextButton( "Clear", tTip = "Clear all events from SEventBus")
			val addDummyBtn: Button = TextButton("AddDummy", tTip =  "Add a dummy event to SEventBus")
			val refreshBtn: Button      = IconButton(Icons.REFRESH_ICON, tTip = "Refresh")

			val all       : Seq[Button] = Seq(clearBtn,addDummyBtn, refreshBtn)
			contents ++= all
		}
		object SideButtonBar extends BoxPanel(Orientation.Vertical) {
			val unregisterBtn   : Button = IconButton(Icons.DELETE_ICON, tTip = "unregister")
			val priorityPlusBtn : Button = IconButton(Icons.ADD_ICON, tTip = "priority +")
			val priorityMinusBtn: Button = IconButton(Icons.MINUS_ICON, tTip = "priority -")

			val all: Seq[Button] = Seq(unregisterBtn, priorityPlusBtn, priorityMinusBtn)
			contents ++= all
		}
		object EventsTable extends Table(eventsTableModel) {
			selection.elementMode = ElementMode.Row
			selection.intervalMode = IntervalMode.Single
		}

		object OutputPanel extends TextArea(8, 200) {
			editable = false
		}
		val outputScroll = ScrollPane(OutputPanel)

		def record(str: String): Unit = {
			OutputPanel.text = OutputPanel.text.pipe(t => s"$t".appendedAll((if(t.isEmpty) "" else "\n")).appendedAll(s"${str}"))
		}

		def createPanel(): scala.swing.Panel = {
			new BorderPanel() {
				add(ButtonBar, Position.North)
				add(SideButtonBar, Position.West)
				add(ScrollPane(EventsTable), Position.Center)
				add(outputScroll, Position.South)

				listenTo((ButtonBar.all ++ SideButtonBar.all) *)
				listenTo(EventsTable)
				listenTo(bus)

				reactions += {
					case ButtonClicked(ButtonBar.`clearBtn`) => record("clear")
					case ButtonClicked(ButtonBar.`refreshBtn`) =>  bus.debug(record)
					case ButtonClicked(ButtonBar.`addDummyBtn`) =>record("addDummmy")
					case ButtonClicked(SideButtonBar.`priorityPlusBtn`) =>record("priority +")
					case ButtonClicked(SideButtonBar.`priorityMinusBtn`) =>record("priority - ")
					case ButtonClicked(SideButtonBar.`unregisterBtn`) =>record("unregister")
					case r@SEventBus.AddedSubs(added, allHandlers) => {
						record(r.toString)
						eventsTableModel.setData(allHandlers)
					}
					case r@SEventBus.DeletedSubs(removed, allHandlers) => {
						record(r.toString)
						eventsTableModel.setData(allHandlers)
					}
					case r@SEventBus.Record(str) => record(str)
					case te: TableEvent if te.source == EventsTable => record(s"TableEvent ${te}")
					case e: UIEvent => //ignored
					case x => record(s"panel reaction has value ${x.getClass}")
				}
			}
		}

		if(_frame == null) {
			_frame = new Frame()/* with RichWindow.Undecorated*/{
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
