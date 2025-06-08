package com.fredplugins.pvmDebugger.moons

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.moons.FredsMoonConfig
import net.runelite.api.Client
import net.runelite.api.events.GameTick
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.EventBus
import org.slf4j.Logger

import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.Random
import scala.util.Try

class SudoMoonPlugin(val target: String)(val pvmDebuggerPlugin: PvmDebuggerPlugin, val client: Client, val config: FredsMoonConfig) {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

//	given Client = pvmDebuggerPlugin.getClient
//	given ClientThread = pvmDebuggerPlugin.getClientThread
//	def enable(): Unit = {}
//	def disable(): Unit = {}
//	def getConfig(): FredsMoonConfig
//	def getClient(): Client

	def inMoons: Boolean = Try(client.getLocalPlayer.getWorldLocation).map(wl => (wl.getRegionX, wl.getRegionY)).toOption.getOrElse((-1, -1)).pipe {
		case (rx, ry) => (21 to 23).contains(rx) && (149 to 151).contains(ry)
	}

	def onGameTick(event: GameTick): Unit = {
		log.debug("Testing123")
	}
}
