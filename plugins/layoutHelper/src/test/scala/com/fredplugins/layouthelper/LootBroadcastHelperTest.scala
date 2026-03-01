package com.fredplugins.layouthelper

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

import scala.compiletime.uninitialized

object LootBroadcastHelperTest extends App {

	val data = List(
		"Your Brutus kill count is: <col=ff0000>138</col>.",
		"Fight duration: <col=ff0000>0:18</col>. Personal best: 0:02",
		"<col=005f00>Iron4106 received a drop: 29 x Air rune</col> <col=106f10>(Brutus)</col>",
	).foreach(m => {
		println(LootBroadcastHelper.parse(m))
	})
//	println(LootBroadcastHelper.parse("<col=ef1020>You're assigned to kill </col>monkeys<col=ef1020>; only </col>20<col=ef1020> more to go."))
//	println(LootBroadcastHelper.parse("<col=005f00>Iron4106 received a drop: 29 x Air rune</col> <col=106f10>(Brutus)</col>"))
//	println(LootBroadcastHelper.parse("<col=005f00>Iron4106 received a drop: Cowhide</col> <col=106f10>(Brutus)</col>"))
}
