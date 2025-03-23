package com.fredplugins.kroovy.swing

import com.fredplugins.kroovy.api.KIcon

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.swing.Button

object ButtonFactory {
	def IconButton(kIcon: KIcon, label: String = "", tTip: String = ""): Button = {
		new Button(label) {
			icon = kIcon.icon
			rolloverIcon = kIcon.icon_selected
			if(tTip.nonEmpty) tooltip = tTip
		}
	}

	def TextButton(label: String = "", tTip: String = ""): Button = {
		new Button(label) {
			if (tTip.nonEmpty) tooltip = tTip
		}
	}
}
