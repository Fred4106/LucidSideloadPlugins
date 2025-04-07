package com.fredplugins.kroovy

import com.fredplugins.kroovy.GauntletTags
import com.fredplugins.kroovy.GauntletTags.Demiboss

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object EnumTest extends App {
	Seq(
	GauntletTags.Weak,
		GauntletTags.Strong,
		GauntletTags.Demiboss,
		GauntletTags.Boss).foreach(gts => {
		println(gts.productPrefix)
		gts.values.foreach(gtsV => {
			println(s"  ${gtsV}")
		})
	})
}
