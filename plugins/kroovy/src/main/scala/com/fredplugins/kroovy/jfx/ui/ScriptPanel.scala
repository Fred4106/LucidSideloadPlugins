package com.fredplugins.kroovy.jfx.ui

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.KroovyPlugin

import java.awt.event.MouseEvent
import java.lang
import javax.swing.SwingUtilities
import com.fredplugins.kroovy.KroovyPlugin.EDIT_ICON_ON
import com.fredplugins.kroovy.data.*
import com.fredplugins.kroovy.jfx.{KFXWindowWrapper, RunnableImplicits, SNode}
import com.fredplugins.kroovy.jfx.ui.components.KToggleButton
import com.fredplugins.kroovy.jfx.ui.fx.{NodePane, PackageEditorPOC}
import net.runelite.client.ui.FontManager
import net.runelite.client.util.SwingUtil

import scala.swing.BorderPanel.Position.{Center, East, North, West}
import scala.swing.Orientation.Horizontal
import scala.swing.event.{ButtonClicked, MouseClicked}
import scala.swing.{Alignment, BorderPanel, BoxPanel, Label, PopupMenu}
import javax.swing.border.{EmptyBorder, MatteBorder}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.MapChangeListener

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.swing.Dialog
import RunnableImplicits.given
class ScriptPanel(val pkg: SPackage) extends BorderPanel with ShimUtils.Logging {
	val editor: PackageEditorPOC = new PackageEditorPOC(pkg)
	val window: KFXWindowWrapper[_] = new KFXWindowWrapper(editor)
	window.onFXThreadLaterCallback(b => {
		b.stage.setOnHiding(event => {
			System.out.println("Closing Stage")
			SwingUtilities.invokeLater(edit.selected = false)
		})
		b.stage.setOnShowing(event => {
			System.out.println("Opening Stage")
			SwingUtilities.invokeLater(edit.selected = true)
		})
	})
	window.setTitle(pkg.getName().getOrElse("UNKNOWN"))

	border = new MatteBorder(1, 1, 1, 1, KroovyPlugin.GREY_BACKGROUND_COLOR)
	background = KroovyPlugin.DARK_BACKGROUND_COLOR

	val addKeyNodeLabel = new Label("Add Key", KroovyPlugin.ADD_ICON.icon, Alignment.Left)
	val addEventNodeLabel = new Label("Add Event", KroovyPlugin.ADD_ICON.icon, Alignment.Left)
	val deleteScriptLabel = new Label("Delete Package", KroovyPlugin.DELETE_ICON.icon, Alignment.Left)

	val popupMenu = new PopupMenu {
		contents += addEventNodeLabel
		contents += addKeyNodeLabel
		contents += deleteScriptLabel


		reactions += {
			case e: MouseClicked if (e.source == addEventNodeLabel || e.source == addKeyNodeLabel || e.source == deleteScriptLabel) && e.peer.getButton == MouseEvent.BUTTON1 =>
				this.visible = false
		}
		reactions += {
			case e: MouseClicked if e.source == deleteScriptLabel && e.peer.getButton == MouseEvent.BUTTON1 =>
				val r = Dialog.showConfirmation(ScriptPanel.this, s"""Really delete package "${pkg.getName().getOrElse("ERROR")}"?""")
				r match {
					case scala.swing.Dialog.Result.Yes =>
						SwingUtilities.invokeLater(() => {
							pkg.kManager.removeScript(pkg)
							pkg.plugin.kPanel.foreach(_.revalidate())
							pkg.plugin.kPanel.foreach(_.repaint())
						})
					case _ =>
				}

			case e: MouseClicked if e.source == addEventNodeLabel && e.peer.getButton == MouseEvent.BUTTON1 =>
				val r = Dialog.showInput(ScriptPanel.this, "New node name", initial="")
				r match {
					case Some(str) => {
						if(pkg.containsNode(str)) {
							Dialog.showMessage(ScriptPanel.this, s"""Name "$str" is taken""")
						} else {
							SwingUtilities.invokeLater(() => {
								pkg.addNode(str, new SEventNodeImpl(pkg))
								pkg.plugin.kPanel.foreach(_.revalidate())
								pkg.plugin.kPanel.foreach(_.repaint())
							})
						}
					}
					case None =>
				}

			case e: MouseClicked if e.source == addKeyNodeLabel && e.peer.getButton == MouseEvent.BUTTON1 =>
				val r = Dialog.showInput(ScriptPanel.this, "New node name", initial="")
				r match {
					case Some(str) => {
						if(pkg.containsNode(str)) {
							Dialog.showMessage(ScriptPanel.this, s"""Name "$str" is taken""")
						} else {
							SwingUtilities.invokeLater(() => {
								pkg.addNode(str, new SKeyNodeImpl(pkg))
								pkg.plugin.kPanel.foreach(_.revalidate())
								pkg.plugin.kPanel.foreach(_.repaint())
							})
						}
					}
					case None =>
				}
		}
		listenTo(addEventNodeLabel.mouse.clicks)
		listenTo(addKeyNodeLabel.mouse.clicks)
		listenTo(deleteScriptLabel.mouse.clicks)
	}

	val nameLabel: Label = new Label {
		text = pkg.getName().getOrElse("NO_NAME!")
		font = FontManager.getRunescapeFont

		reactions += {
			case e: MouseClicked if e.source == this && e.peer.getButton == MouseEvent.BUTTON3 => {
				popupMenu.show(this, e.point.x, e.point.y)
			}
		}
		listenTo(mouse.clicks)
	}


	val scriptToggle: KToggleButton = new KToggleButton(KroovyPlugin.SLIDER_ICON_OFF, KroovyPlugin.SLIDER_ICON_ON) {
		SwingUtil.addModalTooltip(peer, "Stop package", "Start package")
		enabled = true
		selected = pkg.isRunning
	}

	val edit: KToggleButton = new KToggleButton(KroovyPlugin.EDIT_ICON_OFF, EDIT_ICON_ON) {
		SwingUtil.addModalTooltip(peer, "Close editor", "Open editor")
		enabled = true
		selected = false
	}

	val buttonPanel: BoxPanel = new BoxPanel(Horizontal) {
		contents += edit
		contents += scriptToggle
	}

	val headerPanel: BorderPanel = new BorderPanel() {
		add(nameLabel, West)
		add(buttonPanel, East)

		listenTo(scriptToggle)
		listenTo(edit)

//		case SPackageRunningChangedEvent(source, isRunning) if source.eq(pkg) =>
//		log.debug("SPackageRunningChangedEvent({}, {})", source.getName().getOrElse("NO_NAME!"), isRunning)
//		toggle.selected = isRunning


		pkg.runningProperty.addListener(new ChangeListener[lang.Boolean] {
			override def changed(observable: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
					log.debug("SPackageRunningChangedEvent({}, {})", pkg.getName().getOrElse("NO_NAME!"), newValue)
					SwingUtilities.invokeLater(() => {
						scriptToggle.selected = newValue
					})
			}
		} )

		reactions += {
			case ButtonClicked(component) if component == scriptToggle =>
				pkg.setRunning(scriptToggle.selected)
			case ButtonClicked(component) if component == edit =>
				if(window.isVisible) {
					window.hide()
				} else {
					window.show()
				}
		}
	}

	val bodyPanel: DynamicGridPanel = new DynamicGridPanel(0, 1)
	bodyPanel.border = new EmptyBorder(2, 2, 2, 2)
	bodyPanel.vGap = 2
	add(headerPanel, North)
	bodyPanel.contents.addAll(pkg.nodes.values.asScala.toList.sortWith((n1, n2) =>
		((n1.getDistinguishing != n2.getDistinguishing) && n1.getDistinguishing.contentEquals("key"))
			|| (n1.getName.getOrElse("NO_NAME").compareTo(n2.getName.getOrElse("NO_NAME")) < 0)).map(new NodePanel(_)))
	add(bodyPanel, Center)

	pkg.nodes.addListener(new MapChangeListener[String, SNode] {
		override def onChanged(change: MapChangeListener.Change[_ <: String, _ <: SNode]): Unit = {
			if(change.wasAdded()) {
				log.debug("SPackageNodeAddedEvent({}, {})", pkg.getName().getOrElse("NO_NAME!"), change.getValueAdded.getName.getOrElse(change.getKey))
				bodyPanel.contents.insert(pkg.nodes.values.asScala.toList.sortWith((n1, n2) =>
					((n1.getDistinguishing != n2.getDistinguishing) && n1.getDistinguishing.contentEquals("key"))
						|| (n1.getName.getOrElse("NO_NAME").compareTo(n2.getName.getOrElse("NO_NAME")) < 0)).indexOf(change.getValueAdded), new NodePanel(change.getValueAdded))
				editor.onFXThreadLaterCallback(e => {
					e.accordion.getPanes.add(new NodePane(change.getValueAdded))
					e.accordion.getPanes.sort((n1, n2) =>
						(n1, n2) match {
							case (p1: NodePane, p2: NodePane) =>
								if (p1.node.getDistinguishing != p2.node.getDistinguishing) (if (p1.node.getDistinguishing.contentEquals("key")) 1 else -1) else p1.getText.compareTo(p2.getText)
							case (_: NodePane, _)=> 1
							case (_, _: NodePane) => -1
							case _ => n1.getText.compareTo(n2.getText)
						})
				})
			} else {
				log.debug("SPackageNodeDeletedEvent({}, {})", pkg.getName().getOrElse("NO_NAME!"), change.getValueRemoved.getName.getOrElse(change.getKey))
				bodyPanel.contents.subtractAll(bodyPanel.contents.filter {
					case n: NodePanel if n.node.eq(change.getValueRemoved) => true
					case _ => false
				})
				editor.onFXThreadLaterCallback(e => e.accordion.getPanes.removeIf{
					case n: NodePane => n.node.equals(change.getValueRemoved)
					case _ => false
				})
			}
		}
	})
}
