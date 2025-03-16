package com.fredplugins.common.utils

import org.slf4j
import org.slf4j.{Logger, LoggerFactory}

import scala.util.chaining.*

object ShimUtils {
	trait Logging(level: String = "DEBUG") {
		val log: Logger = {
			import ch.qos.logback.classic.{Level => LogbackLevel, Logger => LogbackLogger}
			val temp: Logger = LoggerFactory.getLogger(this.getClass)
			temp.asInstanceOf[LogbackLogger].setLevel(LogbackLevel.toLevel(level))
			temp
		}
	}

	inline def getLogger(inline name: String, inline level: "ALL" | "TRACE" | "DEBUG" | "INFO" | "WARN" | "ERROR" | "OFF"): Logger = {
		import ch.qos.logback.classic.{Level as LogbackLevel, Logger as LogbackLogger}
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
