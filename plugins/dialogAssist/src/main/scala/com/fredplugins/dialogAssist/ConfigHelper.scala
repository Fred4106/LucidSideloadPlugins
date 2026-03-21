package com.fredplugins.dialogAssist

import com.fredplugins.common.utils.ReflectionUtils
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class ConfigHelper(config: FredsDialogueAssistantConfig) {
	def getHiddenMakeX: List[List[Int]] = {
		config.hiddenMakeXItems().linesIterator.map(line => {
			line.split(',').map(lf => lf.trim).filterNot(_.isBlank).toList.map(ReflectionUtils.getItemId(_))
		}).toList.tap(j => 
			println(s"getHiddenMakeX = ${j}")
		)
	}
	def getHiddenMakeXAsJava: java.util.List[java.util.List[Integer]] = {
		getHiddenMakeX.map(_.map(Integer.valueOf(_)).asJava).asJava
	}
	def setHiddenMakeX(list: List[List[Int]]): Unit = {
		config.setHiddenMakeXItems(list.map(sl => sl.map(ReflectionUtils.getItemName(_)).mkString(", ")).mkString("\n"));
	}

	def setHiddenMakeXAsJava(list: java.util.List[java.util.List[Integer]]): Unit = {
		list.asScala.toList.map(_.asScala.toList.map(_.intValue())).tap(setHiddenMakeX(_))
	}

	def getAutoMakeX: List[List[Int]] = {
		config.autoMakeXItems().linesIterator.map(line => {
			line.split(',').map(lf => lf.trim).filterNot(_.isBlank).toList.map(ReflectionUtils.getItemId(_))
		}).toList.tap(j =>
			println(s"getAutoMakeX = ${j}")
		)
	}
	def getAutoMakeXAsJava: java.util.List[java.util.List[Integer]] = {
		getAutoMakeX.map(_.map(Integer.valueOf(_)).asJava).asJava
	}
	def setAutoMakeX(list: List[List[Int]]): Unit = {
		config.setAutoMakeXItems(list.map(sl => sl.map(ReflectionUtils.getItemName(_)).mkString(", ")).mkString("\n"));
	}

	def setAutoMakeXAsJava(list: java.util.List[java.util.List[Integer]]): Unit = {
		list.asScala.toList.map(_.asScala.toList.map(_.intValue())).tap(setAutoMakeX(_))
	}
}
