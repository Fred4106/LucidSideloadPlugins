package com.fredplugins.kroovy.jfx.ui

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.jfx.ui.components.KToggleButton
import com.fredplugins.kroovy.jfx.{EventType, SEventNode, SKeyNode}

import java.awt.event.{KeyEvent, MouseEvent}
import java.lang
import javafx.beans.value.{ChangeListener, ObservableValue}

import javax.swing.SwingUtilities
import javax.swing.border.MatteBorder
//import net.runelite.api.events
import com.fredplugins.kroovy.KroovyPlugin
import com.fredplugins.kroovy.jfx.SNode

import net.runelite.client.util.SwingUtil

import scala.swing.BorderPanel.Position.North
import scala.swing.{Alignment, BorderPanel, BoxPanel, Button, ComboBox, Component, Dialog, Dimension, FlowPanel, Label, ListView, PopupMenu}
import scala.swing.BorderPanel.Position.{Center, East, West}
import scala.swing.Swing.EmptyIcon
import scala.swing.event.{ButtonClicked, KeyTyped, MouseClicked, SelectionChanged}

class NodePanel(val node: SNode) extends BorderPanel with ShimUtils.Logging {
	border = new MatteBorder(1, 1, 1, 1, KroovyPlugin.GREY_BACKGROUND_COLOR)
	background = KroovyPlugin.DARKER_BACKGROUND_COLOR

	val deleteScriptLabel = new Label("Delete Node", KroovyPlugin.DELETE_ICON.icon, Alignment.Left)

	val popupMenu = new PopupMenu {
		contents += deleteScriptLabel
		reactions += {
			case e: MouseClicked if e.source == deleteScriptLabel && e.peer.getButton == MouseEvent.BUTTON1 =>
				this.visible = false
				val r = Dialog.showConfirmation(NodePanel.this, s"""Really delete node "${node.getName.getOrElse("ERROR")}"?""")
				r match {
					case scala.swing.Dialog.Result.Yes =>
						SwingUtilities.invokeLater(() => {
							val pnl = node.pkg.plugin.kPanel
							node.getName.foreach(node.pkg.deleteNode)
							pnl.foreach(_.revalidate())
							pnl.foreach(_.repaint())
						})
					case _ =>
				}
		}
		listenTo(deleteScriptLabel.mouse.clicks)
	}

	val nmLable = new Label(node.getName.getOrElse("NO_NAME"), node match {
		case _: SEventNode => KroovyPlugin.RUN_ICON.icon
		case _: SKeyNode => KroovyPlugin.KEY_DOWN_ICON.icon
	}, Alignment.Left) {
		reactions += {
			case e: MouseClicked if e.source == this && e.peer.getButton == MouseEvent.BUTTON3 => {
				popupMenu.show(this, e.point.x, e.point.y)
			}
		}
		listenTo(mouse.clicks)
	}


	val toggle: KToggleButton = new KToggleButton(KroovyPlugin.SLIDER_ICON_OFF, KroovyPlugin.SLIDER_ICON_ON) {
		SwingUtil.addModalTooltip(peer, "Disable node", "Enable node")
		enabled = node.pkg.isRunning
		selected = node.isEnabled
	}
	node match {
		case node: SEventNode => {
			val cb = new ComboBox(KroovyPlugin.allClassesList) {
				renderer = new ListView.Renderer[EventType] {
//					override def componentFor(list: ListView[node.clazzType], isSelected: Boolean, focused: Boolean, a: node.clazzType, index: Int): Component = {
//						new Label(a.clazz.getSimpleName, EmptyIcon, Alignment.Left)
//					}
//					override def componentFor(list: ListView[_ <: EventType[_]], isSelected: Boolean, focused: Boolean, a: EventType[_], index: Int): Component = {
//						new Label(a.clazz.getSimpleName, EmptyIcon, Alignment.Left)
//					}
					override def componentFor(list: ListView[EventType], isSelected: Boolean, focused: Boolean, a: EventType, index: Int): Component = {
						new Label(a.clazz.getSimpleName, EmptyIcon, Alignment.Left)
					}
				}
			}
			node.eventType.get().foreach(k => cb.selection.item = k)
			listenTo(cb.selection)
			reactions += {
				case SelectionChanged(source) =>
					log.debug("SelectionChanged: {}, {}", source, cb.selection.item.clazz.getSimpleName)
					node.eventType.setValue(
						Option(cb.selection.item)
//							.asInstanceOf[
					)
			}
			node.eventType.addListener(new ChangeListener[Option[EventType]] {
				override def changed(observable: ObservableValue[_ <: Option[EventType]], oldValue: Option[EventType], newValue: Option[EventType]): Unit = {
					if(newValue != oldValue) {
						SwingUtilities.invokeLater(() => {
							cb.selection.item = newValue.getOrElse(EventType(classOf[Nothing]))
						})
					}
				}
			})
			val tPnl = new BorderPanel() {
				add(nmLable, West)
				add(toggle, East)
			}
			tPnl.background = background
			add(tPnl, North)
			add(cb, Center)
		}
		case node: SKeyNode => {
			val btn = new Button(node.keyString)
			btn.maximumSize = new Dimension(68, 25)
			btn.preferredSize = new Dimension(68, 25)
			listenTo(btn)
			listenTo(btn.keys)
			node.keyCode.addListener(new ChangeListener[Number] {
				override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
					SwingUtilities.invokeLater(() => {
						btn.text = node.keyString
					})
				}
			})

			reactions += {
//				case SNodeHotkeyChanged(source, kc) if source == node =>
//					btn.text = node.keyString
				case ButtonClicked(component) if component == btn =>
					node.keyCode.set(KeyEvent.VK_UNDERSCORE)
				case KeyTyped(source, char, modifiers, location) if source == btn => {
					val charI: Int = char
					val keyEventCode: Int = (KeyEvent.getExtendedKeyCodeForChar(charI))
					node.keyCode.set(keyEventCode)
				}
			}
			add(nmLable, West)
			val keyPanel = new FlowPanel(btn, toggle)
			keyPanel.background = background
			add(keyPanel, East)
		}
	}

	listenTo(toggle)
	node.pkg.runningProperty.addListener(new ChangeListener[lang.Boolean]() {
		override def changed(observable: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
			log.debug("SPackageRunningChangedEvent({}, {})", node.pkg.getName().getOrElse("NO_NAME!"), newValue)
			SwingUtilities.invokeLater(() => {
				//					toggle.selected = node.isEnabled
				toggle.enabled = newValue
			})
		}
	})
//		override def changed(observable: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
//			log.debug("SPackageRunningChangedEvent({}, {})", pkg.getName().getOrElse("NO_NAME!"), newValue)
//				SwingUtilities.invokeLater(() => {
//					log.debug("SNodeEnabledStateChanged({}, {})", node.getName.getOrElse("NO_NAME!"), newValue)
////					toggle.selected = node.isEnabled
//					toggle.enabled = newValue
//				})
//		}
	reactions += {
		case ButtonClicked(component) if component == toggle =>
			 node.enabled.setValue(toggle.selected)
	}
}
