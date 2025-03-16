package com.fredplugins.kroovy.jfx.ui.fx

import com.fredplugins.kroovy.jfx.DemoJfxWindow
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.concurrent.Worker
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.scene.input.{Clipboard, ClipboardContent, KeyCode, KeyCodeCombination, KeyCombination, KeyEvent}
import javafx.scene.web.WebView
import netscape.javascript.JSObject

import scala.jdk.CollectionConverters.*


object AceEditor extends DemoJfxWindow {
	private class AceAutocomplete() {
		def process(editor: JSObject, session: JSObject, pos: JSObject, prefix: String, callback: JSObject): Boolean = {
			System.out.println("row: " + pos.getMember("row"))
			System.out.println("column: " + pos.getMember("column"))
			System.out.println("prefix: " + prefix)
			callback.call("call", "bob", "bobSaysHello", "scala")
			true
		}
	}
	override def init(stage: Stage) = {
		val webView = new WebView
		val webEngine = webView.getEngine
		webEngine.getLoadWorker.stateProperty.addListener(new ChangeListener[Worker.State]() {
			def changed(observable: ObservableValue[_ <: Worker.State] , oldValue: Worker.State, newValue: Worker.State): Unit = {
				if (newValue != Worker.State.SUCCEEDED) return
				val window = webEngine.executeScript("window").asInstanceOf[JSObject]
				window.setMember("scala", new AceAutocomplete())
			}
		})
		webEngine.load(getClass.getResource("/ace/editor.html").toExternalForm)
		val copyCombo = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN)

		webView.addEventHandler[KeyEvent](KeyEvent.KEY_PRESSED, e => {
			if(copyCombo.`match`(e)) {
				val contentText = webView.getEngine.executeScript("var text = editor.getCopyText()\neditor.execCommand(\"copy\")\ntext").asInstanceOf[String]
				val clipboard = Clipboard.getSystemClipboard
				val content = new ClipboardContent
				content.putString(contentText)
				clipboard.setContent(content)
				System.out.println("Clipboard: " + clipboard.getString)
			}
		})
		val pane = new StackPane(webView)
		val scene = new Scene(pane, 600, 400)
		stage.setScene(scene)
		stage.setTitle("Editor Demo")
		stage.show()
	}
}
