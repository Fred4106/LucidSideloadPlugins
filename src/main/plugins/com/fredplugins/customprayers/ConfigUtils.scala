package com.fredplugins.customprayers


import com.fredplugins.customprayers.{CustomPrayer, EventType, FredsCustomPrayersConfig, FredsCustomPrayersPlugin}
import org.slf4j.LoggerFactory

import java.util
import scala.util.Try
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*

object ConfigUtils {
	private val log: org.slf4j.Logger = LoggerFactory.getLogger(classOf[FredsCustomPrayersPlugin])
	def parsePrayers(config: FredsCustomPrayersConfig): util.Map[EventType, util.List[CustomPrayer]] = {
		val z=  (1 to 11).flatMap(parsePrayerSlot(config)(_)).groupMap((a, b) => a)((a, b) => b).map((a, b) => {
			a -> b.flatten
		})
		z.foreach((e, pl) => {
			log.info("Event {} creates prayers {}\n", e, pl.mkString("[\n", ",\n", "\n]"))
		})
		z.map((a, b) => (a, b.asJava)).asJava
	}

	private def parsePrayerSlot(config: FredsCustomPrayersConfig)(id: Int): Option[(EventType, List[CustomPrayer])] = {
		val x: Option[(EventType, List[CustomPrayer])] = (id match {
			case 1 if (config.activated1) => Some((config.pray1Ids(), config.pray1delays(), config.pray1choice(), config.eventType1(), config.toggle1(), config.ignoreNonTargetEvents1()))
			case 2 if (config.activated2) => Some((config.pray2Ids(), config.pray2delays(), config.pray2choice(), config.eventType2(), config.toggle2(), config.ignoreNonTargetEvents2()))
			case 3 if (config.activated3) => Some((config.pray3Ids(), config.pray3delays(), config.pray3choice(), config.eventType3(), config.toggle3(), config.ignoreNonTargetEvents3()))
			case 4 if (config.activated4) => Some((config.pray4Ids(), config.pray4delays(), config.pray4choice(), config.eventType4(), config.toggle4(), config.ignoreNonTargetEvents4()))
			case 5 if (config.activated5) => Some((config.pray5Ids(), config.pray5delays(), config.pray5choice(), config.eventType5(), config.toggle5(), config.ignoreNonTargetEvents5()))
			case 6 if (config.activated6) => Some((config.pray6Ids(), config.pray6delays(), config.pray6choice(), config.eventType6(), config.toggle6(), config.ignoreNonTargetEvents6()))
			case 7 if (config.activated7) => Some((config.pray7Ids(), config.pray7delays(), config.pray7choice(), config.eventType7(), config.toggle7(), config.ignoreNonTargetEvents7()))
			case 8 if (config.activated8) => Some((config.pray8Ids(), config.pray8delays(), config.pray8choice(), config.eventType8(), config.toggle8(), config.ignoreNonTargetEvents8()))
			case 9 if (config.activated9) => Some((config.pray9Ids(), config.pray9delays(), config.pray9choice(), config.eventType9(), config.toggle9(), config.ignoreNonTargetEvents9()))
			case 10 if (config.activated10) => Some((config.pray10Ids(), config.pray10delays(), config.pray10choice(), config.eventType10(), config.toggle10(), config.ignoreNonTargetEvents10()))
			case _ => None
		}).flatMap((str, str1, prayerChoice, tpe, toggle, ignoreNonTargetEvent) => {
			val ids                     = str.split(",").map(x => Try[Int](x.toInt).getOrElse(-1))
			val delays                  = str1.split(",").map(x => Try[Int](x.toInt).getOrElse(0))
			val cpl: List[CustomPrayer] = (
																		if (delays.length == 1) {
																			ids.map(i => (i, delays.head))
																		} else if (ids.length > delays.length) {
																			ids.zip(delays.padTo(ids.length, 0))
																		} else {
																			ids.zip(delays)
																		}).toList
				.map((i, d) => new CustomPrayer(i, prayerChoice, d, toggle, ignoreNonTargetEvent))
			Option.when(prayerChoice != null && tpe != null)(tpe -> cpl)
		})
		x
//		x.map((a, b) => (a, b))
	}
}
