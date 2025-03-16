package com.fredplugins.kroovy.jfx.ui.fx

import com.fredplugins.common.utils.ShimUtils
import javafx.event.ActionEvent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.{BorderPane, VBox}
import com.fredplugins.kroovy.data.SPackage
import com.fredplugins.kroovy.jfx.{KFXSceneProvider, KResource, SEventNode, SKeyNode, SNode}
import org.fxmisc.flowless.VirtualizedScrollPane

import scala.jdk.CollectionConverters.MapHasAsScala

class NodePane(val node: SNode) extends TitledPane() {
	val tabPane = new TabPane()
	tabPane.getTabs.add(new Tab("onEnabled", new VirtualizedScrollPane[GroovyCodeArea](new GroovyCodeArea(node.onEnabled.sourceOpt.getOrElse("")))))
	tabPane.getTabs.add(new Tab("onDisabled", new VirtualizedScrollPane[GroovyCodeArea](new GroovyCodeArea(node.onDisabled.sourceOpt.getOrElse("")))))
	node match {
		case node: SEventNode =>
			tabPane.getTabs.add(new Tab("onTriggered", new VirtualizedScrollPane[GroovyCodeArea](new GroovyCodeArea(node.onTrigger.sourceOpt.getOrElse("")))))
		case node: SKeyNode =>
			tabPane.getTabs.add(new Tab("onPress", new VirtualizedScrollPane[GroovyCodeArea](new GroovyCodeArea(node.onPress.sourceOpt.getOrElse("")))))
			tabPane.getTabs.add(new Tab("onRelease", new VirtualizedScrollPane[GroovyCodeArea](new GroovyCodeArea(node.onRelease.sourceOpt.getOrElse("")))))
	}
	setText(node.getName.getOrElse("NO_NAME"))
	setContent(tabPane)
}

class PackageEditorPOC(val pkg: SPackage) extends KFXSceneProvider with ShimUtils.Logging {
	var accordion: Accordion = null
	override def scene: () => Scene = {
		val scene_ = {
			val saveButton = new Button("Save all")

			val borderPane = new BorderPane
			borderPane.setTop(new VBox(saveButton))

			this.accordion = new Accordion()
			val packagePane: TitledPane = new TitledPane() {
				val tabPane = new TabPane()
				tabPane.getTabs.add(new Tab("onStart", new VirtualizedScrollPane[GroovyCodeArea](new GroovyCodeArea(pkg.onStart.sourceOpt.getOrElse("")))))
				tabPane.getTabs.add(new Tab("onStop", new VirtualizedScrollPane[GroovyCodeArea](new GroovyCodeArea(pkg.onStop.sourceOpt.getOrElse("")))))
				setText(pkg.getName().getOrElse("NO_NAME"))
				setContent(tabPane)
			}
			accordion.getPanes.add(packagePane)
			pkg.nodes.asScala.toList.sortWith((a, b) => a._1.compareTo(b._1) < 0).map(_._2).map(k => new NodePane(k)).foreach(accordion.getPanes.add)
			borderPane.setCenter(accordion)

			val scene = new Scene(borderPane, 800, 600)
			scene.getStylesheets.add(KResource.string("GroovyKeywords.css"))
			saveButton.setOnAction((ae: ActionEvent) => {
				Option(accordion).map(_.getExpandedPane) match {
					case Some(value) => {
						value match {
							case p: NodePane => {
								Option(p.tabPane).map(_.getSelectionModel).map(_.getSelectedItem).foreach(tab => {
									val codeArea = tab.getContent.asInstanceOf[VirtualizedScrollPane[GroovyCodeArea]].getContent
									val code = Option(codeArea.getText())
									val node = p.node
									(node, tab.getText) match {
										case (_, "onEnabled") => code.foreach(node.onEnabled.source.setValue)
										case (_, "onDisabled") => code.foreach(node.onDisabled.source.setValue)
										case (enode: SEventNode, "onTriggered") => code.foreach(enode.onTrigger.source.setValue)
										case (knode: SKeyNode, "onPress")  => code.foreach(knode.onPress.source.setValue)
										case (knode: SKeyNode, "onRelease") => code.foreach(knode.onRelease.source.setValue)
									}
								})
							}
							case p: TitledPane if p.equals(packagePane) && p.getContent.isInstanceOf[TabPane] => {
								Option(p.getContent.asInstanceOf[TabPane]).map(_.getSelectionModel).map(_.getSelectedItem).foreach(tab => {
									val codeArea = tab.getContent.asInstanceOf[VirtualizedScrollPane[GroovyCodeArea]].getContent
									val code = Option(codeArea.getText())
									tab.getText match {
										case "onStart" => code.foreach(pkg.onStart.source.setValue)
										case "onStop" => code.foreach(pkg.onStop.source.setValue)
									}
								})
							}
						}
						pkg.kManager.save(pkg)
					}
					case None =>
				}
			})

			scene
		}
		() => scene_
	}
}
