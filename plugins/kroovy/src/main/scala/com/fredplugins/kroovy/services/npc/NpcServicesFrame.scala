package com.fredplugins.kroovy.services.npc

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.Icons
import com.fredplugins.kroovy.swing.MTableModel
import net.runelite.client.util.ColorUtil

import java.awt.Color
import javax.swing.SwingUtilities
import scala.compiletime.uninitialized
import scala.swing.BorderPanel.Position
import scala.swing.Table.{ElementMode, IntervalMode}
import scala.swing.event.{ButtonClicked, TableEvent, UIEvent}
import scala.swing.{BorderPanel, BoxPanel, Button, Component, Frame, Label, ListView, Orientation, ScrollPane, SplitPane, Swing, Table, TextArea}
import scala.util.chaining.*

object NpcServicesFrame extends ShimUtils.Logging("Debug") {
	private var _frame: Frame = uninitialized

	def get(using service: NpcService): Frame = {
		import com.fredplugins.kroovy.swing.ButtonFactory.*

		object ButtonBar extends BoxPanel(Orientation.Horizontal) {bar =>
			val clearBtn   : Button = TextButton( "Clear", tTip = "Clear all")
			val addDummyBtn: Button = TextButton("AddDummy", tTip =  "Add a dummy")
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

		object NpcFiltersPanel extends ListView[NpcFilteredListener[?]](service.all) {
			renderer = (list: ListView[_ <: NpcFilteredListener[_]], isSelected: Boolean, focused: Boolean, a: NpcFilteredListener[_], index: Int) => {
//				new Label(a.toString)
									val idsComponent       = a.ids.toSeq.sorted.map(id => swing.Label(s"$id"))
										.tapEach(l => l.foreground = Color.BLUE)
										.foldLeft[Seq[Component]](Seq.empty[Component])((u, v) => if (u.isEmpty) Seq(v) else u ++ Seq(Label(", "), v))
										.pipe(sc => sc.prepended(Label("Set(")).appended(Label(")")))
										.pipe(sc => {
											new swing.BoxPanel(swing.Orientation.Horizontal) {
												contents.addAll(sc)
											}
										})
									val eventTypeComponent = Seq(a.tag.runtimeClass.getSimpleName.pipe(tName => Label(tName))).tapEach(_.foreground = Color.ORANGE).prepended(Label("[")).appended(Label("]"))
										.pipe(sc => {
											new swing.BoxPanel(swing.Orientation.Horizontal) {
												contents.addAll(sc)
											}
										})

									new BorderPanel {
										add(eventTypeComponent, Position.West)
										add(idsComponent, Position.East)
										border = Swing.BeveledBorder(Swing.Lowered)
										if (isSelected) {
											background = background.brighter().brighter()
										}
									}
			}
//			renderer = ListView.Renderer[NpcFilteredListener[? <: SNpcEvent]] {
//				override def componentFor(list: ListView[NpcFilteredListener[_]], isSelected: Boolean, focused: Boolean, a: NpcFilteredListener[_], index: Int): Component = {

//				}
//				override def componentFor(list: ListView[_ <: NpcFilteredListener[_ <: SNpcEvent]], isSelected: Boolean, focused: Boolean, a: NpcFilteredListener[_ <: SNpcEvent], index: Int): Component = {
//					new Label(a.toString)
//				}
//			}
		}
		object OutputPanel extends TextArea(8, 200) {
			editable = false
		}

		def record(str: String): Unit = {
			SwingUtilities.invokeLater(() => {
				OutputPanel.text = OutputPanel.text.pipe(t => s"$t".appendedAll((if(t.isEmpty) "" else "\n")).appendedAll(s"${str}"))
			})
		}

		def createPanel(): scala.swing.Panel = {
			new BorderPanel() {
				add(ButtonBar, Position.North)
				add(SideButtonBar, Position.West)
				add(SplitPane(Orientation.Horizontal, ScrollPane(NpcFiltersPanel), ScrollPane(OutputPanel)), Position.Center)

				listenTo((ButtonBar.all ++ SideButtonBar.all) *)
				listenTo(NpcFiltersPanel)
				listenTo(OutputPanel)
				listenTo(service)

				reactions += {
					case ButtonClicked(ButtonBar.`clearBtn`) => SwingUtilities.invokeLater(() => OutputPanel.text = "")
					case ButtonClicked(ButtonBar.`refreshBtn`) =>  publish(NpcServiceApi.ListChanged)/*SwingUtilities.invokeLater(() => NpcFiltersPanel.listData = service.all)*/
					case ButtonClicked(ButtonBar.`addDummyBtn`) =>record("addDummmy")
					case ButtonClicked(SideButtonBar.`priorityPlusBtn`) =>record("priority +")
					case ButtonClicked(SideButtonBar.`priorityMinusBtn`) =>record("priority - ")
					case ButtonClicked(SideButtonBar.`unregisterBtn`) =>record("unregister")
					case NpcServiceApi.ListChanged => {
						SwingUtilities.invokeLater(() => NpcFiltersPanel.listData = service.all)
					}
					case NpcServiceApi.Log(msg) => record(msg)

//					case r@SEventBus.AddedSubs(added, allHandlers) => {
//						record(r.toString)
//						eventsTableModel.setData(allHandlers)
//					}
//					case r@SEventBus.DeletedSubs(removed, allHandlers) => {
//						record(r.toString)
//						eventsTableModel.setData(allHandlers)
//					}
//					case r@SEventBus.Record(str) => record(str)
//					case te: TableEvent if te.source == EventsTable => record(s"TableEvent ${te}")
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
