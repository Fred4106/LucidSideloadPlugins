package com.fredplugins.common.utils

import org.slf4j
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try
import scala.util.chaining.*

object ShimUtils {
	trait Logging(level: String = "DEBUG") {
		protected val log: Logger = {
			Try{
				import ch.qos.logback.classic.{Level => LogbackLevel, Logger => LogbackLogger}
				val temp: Logger = LoggerFactory.getLogger(this.getClass)
				temp.asInstanceOf[LogbackLogger].setLevel(LogbackLevel.toLevel(level))
				temp
			}.toOption.getOrElse(LoggerFactory.getLogger(this.getClass))
		}
	}

	inline def getLogger(inline name: String, inline level: "ALL" | "TRACE" | "DEBUG" | "INFO" | "WARN" | "ERROR" | "OFF"): Logger = {
		val loggerb = LoggerFactory.getLogger(name)
		Try{
			import ch.qos.logback.classic.{Level as LogbackLevel, Logger as LogbackLogger}
			val l: LogbackLevel = LogbackLevel.toLevel(level)
			loggerb match {
				case logback: LogbackLogger => logback.setLevel(l)
				case _ =>
			}
			loggerb
		}.toEither.fold(t => {t.printStackTrace(); loggerb}, a => a)
	}

	inline def getLogger(inline name: String): Logger = {
		LoggerFactory.getLogger(name)
	}
}
