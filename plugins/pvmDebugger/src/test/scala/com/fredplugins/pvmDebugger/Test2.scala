package com.fredplugins.pvmDebugger

import com.fredplugins.pvmDebugger.SLocation
import com.fredplugins.pvmDebugger.DebugPanel
import net.runelite.api.GameState

import javax.swing.{JFrame, WindowConstants}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.swing.{Frame, MainFrame}

object Test2 extends App {
	val panel = new DebugPanel()

	val simulateEvents = new Runnable {
		import com.fredplugins.pvmDebugger.{SNpcSpawned, SGameStateChanged, SNpcDespawned, SGameTick}
			def publish(x: => DebugEvent): Unit = {
			panel.publish(x)
		}
		inline def gameStateFromIndex(s: Int): GameState = GameState.values.apply(GameState.values.length.pipe(gsvSize => (s +gsvSize)%gsvSize))
		override def run(): Unit = {
			Thread.sleep(1000)
			publish {
				SNpcSpawned(22, 3342, SLocation(232, 552, 0))
			}
			for(i <- 0 until 500) {
				Thread.sleep(200)
				publish {
					SGameTick(i)
				}

				publish {
					SGameStateChanged(gameStateFromIndex(i), gameStateFromIndex(i+1))
				}
			}
			publish {
				SNpcDespawned(26, 3342, SLocation(238, 542, 0))
			}
		}
	}

	new Frame() {
		var forceClose = 0
		this.contents = panel
		override def closeOperation(): Unit = {
			println(s"close Operation ${forceClose}")
			if(forceClose >= 4) System.exit(0)
			forceClose = forceClose + 1
		}
	}.tap(mf => {
		mf.peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
		mf.	pack()
		mf.centerOnScreen()
		mf.open()
	})
	new Thread(simulateEvents).run()
}
