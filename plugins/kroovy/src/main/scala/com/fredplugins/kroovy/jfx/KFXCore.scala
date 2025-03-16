package com.fredplugins.kroovy.jfx

import com.fredplugins.common.utils.ShimUtils

import java.util.concurrent.FutureTask
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.stage.Stage

import scala.concurrent.duration.MILLISECONDS
import scala.reflect.ClassTag
import scala.util.Try

trait KFXCore {
	def onFXThreadLaterCallback(callback: this.type => Unit): Unit = {
		if(Platform.isFxApplicationThread) {
			callback(this)
		} else {
			import RunnableImplicits.runnable2
			Platform.runLater(callback(this))
		}
	}

	def onFXThreadLaterThunk(code: => Unit): Unit = {
		if(Platform.isFxApplicationThread) {
			code
		} else {
			Platform.runLater(RunnableImplicits.runnable2(code))
		}
	}
	def onFXThread[F](code: => F): Try[F] = {
		Try(if(Platform.isFxApplicationThread) {
			code
		} else {
			val futureStageTask = new FutureTask(RunnableImplicits.callable2(code))
			Platform.runLater(futureStageTask)
			futureStageTask.get(6000, MILLISECONDS)
		})
	}
}

trait KFXSceneProvider extends KFXCore {
	def scene: () => Scene
}

class KFXWindowWrapper[K <: KFXSceneProvider: ClassTag as ctag](val sceneProvider: K) extends KFXCore with ShimUtils.Logging {
	Platform.setImplicitExit(false)
	new JFXPanel()

	val stage: Stage = onFXThread {
		val tStage = new Stage()
		tStage.setTitle(this.getClass.getName)
		tStage.setScene(sceneProvider.scene())
		//tStage.setAlwaysOnTop(true) //commenting this out for now so windows can have toggles for it
		tStage
	}.get

	def show(): Unit = onFXThreadLaterThunk {stage.show()}
	def hide(): Unit = onFXThreadLaterThunk {stage.hide()}
	def isVisible: Boolean = onFXThread {stage.isShowing}.getOrElse(false)
	def isActive: Boolean = onFXThread {stage.isFocused}.getOrElse(false)

	def setTitle(title: String): Unit = onFXThreadLaterThunk(stage.setTitle(title))
	def setPosition(x: Int, y: Int): Unit = onFXThreadLaterThunk {stage.setX(x); stage.setY(y)}
	def setAlwaysOnTop(alwaysOnTop: Boolean): Unit = onFXThreadLaterThunk {stage.setAlwaysOnTop(alwaysOnTop)}
}