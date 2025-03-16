package com.fredplugins.kroovy.jfx.ui.fx

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.KroovyPlugin
import com.fredplugins.kroovy.jfx.KFXSceneProvider

import java.awt.BorderLayout
import javafx.embed.swing.JFXPanel

import javax.swing.SwingUtilities
import net.runelite.client.ui.PluginPanel

class FXPanel[T <: KFXSceneProvider](val plugin: KroovyPlugin, val sceneProvider: T, val callbackSwing: FXPanel[T] => Unit) extends PluginPanel(false) with ShimUtils.Logging {
	val realPanel: JFXPanel = new JFXPanel()
	this.setLayout(new BorderLayout())
	sceneProvider.onFXThreadLaterCallback(t => {
		realPanel.setScene(t.scene())
		SwingUtilities.invokeLater(() => {
			add(realPanel, BorderLayout.CENTER)
			callbackSwing(this)
		})
	})
}

