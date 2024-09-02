package com.fredplugins.common

import org.slf4j.{Logger, LoggerFactory}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.chaining.*

object ShimUtils {
	inline def getLogger(inline name: String, inline level: "ALL" | "TRACE" | "DEBUG" | "INFO" | "WARN" | "ERROR" | "OFF"): Logger = {
		import ch.qos.logback.classic.{Level => LogbackLevel, Logger => LogbackLogger}
		val l: LogbackLevel = LogbackLevel.toLevel(level)
		LoggerFactory.getLogger(name).tap{ 
			case logback: LogbackLogger => logback.setLevel(l)
			case _ => 
		}
	}

	inline def getLogger(inline name: String): Logger = {
		LoggerFactory.getLogger(name)
	}
}
