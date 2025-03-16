package com.fredplugins.kroovy.jfx.ui

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.data.SPackage
import com.fredplugins.kroovy.{KManager, KroovyPlugin}

import java.awt.BorderLayout
import javafx.collections.MapChangeListener

import javax.inject.{Inject, Singleton}
import javax.swing.SwingUtilities
import javax.swing.border.EmptyBorder
import net.runelite.client.ui.{ColorScheme, FontManager, PluginPanel}

import scala.swing.BorderPanel.Position.{East, West}
import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, BoxPanel, Button, Dialog, Label, Orientation, Reactor}

@Singleton
class KPanel extends PluginPanel(true) with Reactor with ShimUtils.Logging {
	@Inject private val kManager: KManager = null
	
	val titlePanel: BorderPanel = new BorderPanel() {
		setBorder(new EmptyBorder(10, 10, 10, 10))
		val titleLabel: Label = new Label {
			text = "Kroovy2"
			font = FontManager.getRunescapeSmallFont
		}
		val add: Button = new Button("") {
			icon = KroovyPlugin.ADD_ICON.icon
			enabled = true
		}

		reactions += {
			case e: ButtonClicked if e.source == add =>
				val r = Dialog.showInput(titlePanel, "New package name", initial="")
				r match {
					case Some(str) => {
						if(kManager.containsPackage(str)) {
							Dialog.showMessage(titlePanel, s"Name '$str' is taken")
						} else {
							SwingUtilities.invokeLater(() => {
								kManager.registerNew(str, kManager.plugin.pkgProvider.get())
								revalidate()
								repaint()
							})
						}
					}
					case None =>
				}
		}
		listenTo(add)

		add(titleLabel, West)
		add(add, East)
	}

	val realPanel: BoxPanel = new BoxPanel(Orientation.Vertical)
	realPanel.background = KroovyPlugin.DARKER_BACKGROUND_COLOR
	buildPanel()
	def buildPanel(): Unit = {
		removeAll()
		setLayout(new BorderLayout(0, 10))
		setBackground(ColorScheme.DARK_GRAY_COLOR)
		add(titlePanel.peer, BorderLayout.NORTH)
		add(realPanel.peer, BorderLayout.CENTER)
		revalidate()
		repaint()
	}
	this.kManager.readOnlyScriptsProperty.addListener(new MapChangeListener[String, SPackage] {
		override def onChanged(change: MapChangeListener.Change[_ <: String, _ <: SPackage]): Unit = {
			if(change.wasRemoved()) {
				log.debug("onPkgUnloaded({})", change.getValueRemoved.getName().getOrElse(change.getKey))
				realPanel.contents.collect {
					case pnl: ScriptPanel if pnl.pkg.equals(change.getValueRemoved) => pnl
				}.foreach(toRemove => {
					realPanel.contents.subtractOne(toRemove)
				})
			} else {
				log.debug("onPkgLoaded({})", change.getValueAdded.getName().getOrElse(change.getKey))
				realPanel.contents += new ScriptPanel(change.getValueAdded)
			}
		}
	})
}
