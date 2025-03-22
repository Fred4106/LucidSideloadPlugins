package com.fredplugins.kroovy

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.{Parent, Scene}
import javafx.stage.{Stage, WindowEvent}

import javax.swing.SwingUtilities

trait DemoJfxWindow {
	private var stage: Option[Stage] = None

	def init(stage: Stage): Unit

	final def getStage: Option[Stage] = stage

	final def getScene: Option[Scene] = getStage.map(_.getScene)

	final def getRoot: Option[Parent] = getScene.map(_.getRoot)

	final def launch(): Unit = {
		val s = new Stage()
		stage = Some(s)
		s.setOnCloseRequest(new EventHandler[WindowEvent]() {
			override def handle(windowEvent: WindowEvent): Unit = System.exit(0)
		})
		init(s)
		if (stage.isDefined && !stage.get.isShowing) {
			stage.get.show()
		}
	}

	def main(args: Array[String]): Unit = {
		Platform.startup(() => System.out.println("Hello from Javafx thread"))
		SwingUtilities.invokeLater(() => Platform.runLater(() => DemoJfxWindow.this.launch()))
	}
}
