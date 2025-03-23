package com.fredplugins.kroovy.swing

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.swing.{BoxPanel, Component, Orientation, UIElement}

object PanelFactory {
	abstract class BoxPanelT(o: Orientation.Value, elements: Component *) extends BoxPanel(o) {
		elements.foreach(e => {
			contents += e
			listenTo(e)
		})
	}
}
