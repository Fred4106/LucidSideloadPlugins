package com.fredplugins.kroovy.eventbus

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.Icons
import com.fredplugins.kroovy.swing.MTableModel

import scala.compiletime.uninitialized
import scala.swing.BorderPanel.Position
import scala.swing.Table.{ElementMode, IntervalMode}
import scala.swing.event.{ButtonClicked, TableEvent, UIEvent}
import scala.swing.{BorderPanel, BoxPanel, Button, Frame, Orientation, ScrollPane, Table, TextArea}
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
}
