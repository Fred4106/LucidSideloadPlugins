package com.fredplugins.pvmDebugger

import com.fredplugins.pvmDebugger.DebugPanel.{ClearEvent, DebugEventListModel, FreezeEvent, TableColumnHeaderSelected, UnFreezeEvent}
import net.runelite.api.{Client, GameState}

import java.awt.Color
import javax.swing.event.ListDataListener
import javax.swing.table.JTableHeader
import javax.swing.{AbstractListModel, BorderFactory, ComboBoxModel, DefaultListModel, ListModel}
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.swing.BorderPanel.Position
import scala.swing.Swing.{Embossing, Raised}
import swing.event.{ButtonClicked, Event, TableColumnsSelected, TableEvent, TableRowsSelected}
import scala.swing.{AbstractButton, Action, BorderPanel, BoxPanel, Button, Component, Label, ListView, Orientation, ScrollPane, SplitPane, Swing, Table, TextArea, ToggleButton, event}

object DebugPanel {
	sealed trait ControlEvent extends SEvent {}
	case object ClearEvent extends ControlEvent
	case object FreezeEvent extends ControlEvent
	case object UnFreezeEvent extends ControlEvent

	case class TableColumnHeaderSelected(override val source: Table, column: Int) extends TableEvent(source)
	private class DebugEventListModel() extends ListModel[DebugEvent] {
		private val delegate = new DefaultListModel[DebugEvent] {}
		def appendElement(element: DebugEvent): Unit = {
			delegate.addElement(element)
		}
		def prependElement(element: DebugEvent): Unit = {
			delegate.add(0, element)
		}
		def clearElements(): Unit = {
			delegate.clear()
		}

		override def getSize: Int = delegate.getSize
		override def getElementAt(index: Int): DebugEvent = delegate.getElementAt(index)
		override def addListDataListener(l: ListDataListener): Unit = delegate.addListDataListener(l)
		override def removeListDataListener(l: ListDataListener): Unit = delegate.removeListDataListener(l)
	}
}
class DebugPanel extends BorderPanel {
	private val eventsList: ListView[DebugEvent] = new ListView[DebugEvent]() {
		private var paused: Boolean = false

		reactions += {
			case `ClearEvent` => getModel.clearElements()
			case `FreezeEvent` => paused = true
			case `UnFreezeEvent` => paused = false
		}

		reactions += {
			case event: DebugEvent if !paused => {
//				val selectedIndicies = selection.indices.toList
				getModel.prependElement(event)
//				selection.indices.addAll(selectedIndicies)
			}
		}

		def getModel: DebugEventListModel = {
			peer.getModel match {
				case delm: DebugEventListModel => delm
				case other => {
					val backupList = (0 until other.getSize).reverse.map(other.getElementAt)
					new DebugEventListModel().tap(peer.setModel(_))
						.tap(newModel => {
							backupList.foreach(newModel.prependElement)
						})
				}
			}
		}

		override def listData_=(items: collection.Seq[DebugEvent]): Unit = {
			getModel.tap(_.clearElements()).pipe(mdl => items.foreach(mdl.prependElement))
		}

		//noinspection ConvertExpressionToSAM
		renderer = new ListView.Renderer[DebugEvent] {

//			def componentFor(a: Any): Component = {
//				def cleanup(input: Seq[Component | Seq[_]]): Seq[Component] = {
//					input.flatMap(e => e match {
//													case es: Seq[Component | Seq[_]] => cleanup(es)
//													case ei: Component => Seq(ei)
//													case _ => Seq()
//					})
//				}
//
//				a match {
//					case a: Product => {
//						(0 until a.productArity).map(ix => {
//							val nameLabel = a.productElementName(ix).pipe(pen => Label(pen))
//						})
//						val listOfComponentsStart = Seq[Component | Seq[_]](
//													Label(a.productPrefix),
//													Label("("),
//													Seq.empty[Component],
//													Label(")")
//												)
//						val listOfComponenets = cleanup(listOfComponentsStart)
//
//						new swing.BoxPanel(swing.Orientation.Horizontal) {
//							contents.addAll(listOfComponenets)
//							border = Swing.BeveledBorder(Swing.Lowered)
//						}
//					}
//					case b => Label(b.toString).tap(_.foreground = Color.ORANGE)
//				}
//			}
			override def componentFor(list: ListView[_ <: DebugEvent], isSelected: Boolean, focused: Boolean, a: DebugEvent, index: Int): Component = {
				(for {
					(idx, eName, eValue) <- (0 until a.productArity).flatMap(idx => Try { (idx, a.productElementName(idx), a.productElement(idx)) }.toOption)
				} yield {
					new swing.BoxPanel(swing.Orientation.Horizontal) {
						background = Color.DARK_GRAY
						this.contents += Label(eName).tap(_.foreground = Color.WHITE)
						this.contents += Label(": ").tap(_.foreground = Color.LIGHT_GRAY)
						this.contents += Label(s"${idx}").tap(_.foreground = Color.GREEN)
						this.contents += Label(" = ").tap(_.foreground = Color.LIGHT_GRAY)
						this.contents += Label(eValue.toString).tap(_.foreground = Color.ORANGE)//componentFor(eValue)
						border = Swing.BeveledBorder(Swing.Lowered)
					}
				}).pipe(elementsComponenets =>
					elementsComponenets.flatMap(e => if (elementsComponenets.last == e) Seq(e) else Seq(e, Label(", ").tap(_.foreground = Color.LIGHT_GRAY)))
						.prependedAll(Seq(
							Label(a.productPrefix).tap(_.foreground = Color.BLUE), Label("(").tap(_.foreground = Color.LIGHT_GRAY)
						))
						.appended(Label(")").tap(_.foreground = Color.LIGHT_GRAY))
				).pipe(lineContents => {
					new swing.BoxPanel(swing.Orientation.Horizontal) {
						contents.addAll(lineContents)
						border = Swing.BeveledBorder(Swing.Lowered)
						if (isSelected) {
							background = Color.BLUE
						}
					}
				})
			}
		}
	}

	private val clearBtn: AbstractButton = 			new Button("Clear"){
		action = Action("Clear") {
			eventsList.publish(ClearEvent)
		}
	}
	private val freezeBtn: AbstractButton = new ToggleButton("Freeze") {
		action = Action("Freeze") {
			eventsList.publish(if(selected) FreezeEvent else UnFreezeEvent)
		}
	}

	val headers: Seq[String]       = Array.tabulate(10) {"Col-" + _}.toSeq
	val rowData: Array[Array[Any]] = Array.tabulate[Any](10, 10) {"" + _ + ":" + _}

	val ui                         = new BoxPanel(Orientation.Vertical) {
		val output: TextArea = new TextArea(6, 40) { editable = false }
		val table : Table    = new Table(rowData, headers) {
			selection.elementMode = Table.ElementMode.Row
//			selection.intervalMode = Table.IntervalMode.

			val header: JTableHeader = {
				import java.awt.event.{MouseEvent, MouseAdapter}

				val makeHeaderEvent = TableColumnHeaderSelected(this, _:Int)
				val tableHeader = peer.getTableHeader
				tableHeader.addMouseListener(new MouseAdapter() {
					override def mouseClicked(e: MouseEvent): Unit = {
						selection.publish(makeHeaderEvent(tableHeader.columnAtPoint(e.getPoint)))
					}
				})
				tableHeader
			}
		}

		listenTo(table.selection)

		reactions += {
			case TableRowsSelected(source, range, false) =>
				outputSelection(source, "Rows selected, changes: %s" format range)
			case TableColumnsSelected(source, range, false) =>
				outputSelection(source, "Columns selected, changes: %s" format range)
			case TableColumnHeaderSelected(source, column) =>
				outputSelection(source, "Column header %s selected" format column)
//			case e => println("%s => %s" format(e.getClass.getSimpleName, e.toString))
		}

		contents += new ScrollPane(table)
		contents += new ScrollPane(output)

		def outputSelection(table: Table, msg: String): Unit = {
			val rowId = table.selection.rows.leadIndex
			val colId = table.selection.columns.leadIndex
			val rows = table.selection.rows.mkString(", ")
			val cols = table.selection.columns.mkString(", ")
			output.append("%s\n  Lead: %s, %s; Rows: %s; Columns: %s\n" format (msg, rowId, colId, rows, cols))
		}
	}

	add(
		new BoxPanel(Orientation.Horizontal) {
	//		val panelSelf = this
			contents += clearBtn
			contents += freezeBtn
		}, Position.North
	)//header panel
	add(new SplitPane(Orientation.Vertical, ui, new ScrollPane(eventsList)), Position.Center)
//	add(, Position.Center)


	eventsList.listenTo(this)
}
