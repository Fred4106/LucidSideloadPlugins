package com.fredplugins.kroovy.jfx.ui.fx

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.KroovyPlugin
import javafx.application.Platform
import javafx.embed.swing.{JFXPanel, SwingFXUtils}
import javafx.event.ActionEvent
import javafx.scene.Scene
import javafx.scene.control.{Button, MenuBar, MenuButton, ToolBar}
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane

import javax.swing.SwingUtilities
import com.fredplugins.kroovy.jfx.{KFXSceneProvider, KFXWindowWrapper}

import scala.jdk.CollectionConverters.*

class KroovyFXPanel(val plugin: KroovyPlugin) extends KFXSceneProvider with ShimUtils.Logging {
	val konsole: FXKonsole = new FXKonsole(plugin)
	val konsoleWindow: KFXWindowWrapper[_] = new KFXWindowWrapper(konsole)
	konsoleWindow.setTitle("Konsole")
	Platform.setImplicitExit(false)
	override def scene: () => Scene = {
		val scene_ = {
			val borderPane = new BorderPane
			val consoleImage = new ImageView(SwingFXUtils.toFXImage(KroovyPlugin.CONSOLE_ICON.bufImg, null))
			val consoleButton = new Button("Console", consoleImage)
			consoleButton.setOnAction((ae: ActionEvent) => {
				if (konsoleWindow.isVisible) konsoleWindow.hide()
				else konsoleWindow.show()
			})
			val toolBar = new ToolBar(consoleButton)
			borderPane.setTop(toolBar)
			val scene = new Scene(borderPane, 800, 600)
			scene
		}
		() => scene_
	}
}
