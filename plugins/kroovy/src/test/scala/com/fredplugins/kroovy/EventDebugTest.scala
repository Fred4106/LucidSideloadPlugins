package com.fredplugins.kroovy

import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.events.{GameTick, NpcSpawned}

import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, ExecutionContextExecutorService}
import scala.swing.{BorderPanel, BoxPanel, ComboBox, Frame, Orientation, RichWindow, SwingApplication, TextArea}

object OtherOwner {

}
object EventDebugTest extends SwingApplication with ShimUtils.Logging {
	val ss: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
//
//	val r1 = bus.register[GameTick, 0, "TestGroup1"](this)((t: GameTick) => println(s"This - Gametick"))
//	val r2 = bus.register[GameTick, 4, "Other"](OtherOwner)((t: GameTick) => println(s"This is also a Gametick"))
//	val r3 = bus.register[NpcSpawned, 1, "Self"](OtherOwner)((t: NpcSpawned) => println(s"NpcService - NpcSpawned: ${t.getNpc.getId}, ${t.getNpc.getName}"))
//	val r4 = bus.register[NpcSpawned, 0, "Root"](this)((t: NpcSpawned) => println(s"This - NpcSpawned: ${t.getNpc.getId}, ${t.getNpc.getName}"))
//
//	var gameTickFuture: ScheduledFuture[?] = null

	override def startup(args: Array[String]): Unit = {
//		val sBusFrame = SEventBusFrame.get
//		sBusFrame.open()

//		gameTickFuture  = ss.scheduleAtFixedRate(() => {bus.post(GameTick())}, 1000, 600, TimeUnit.MILLISECONDS)

//		val f: Frame = new Frame() with RichWindow.Undecorated {
//			val textArea: TextArea = new TextArea(100, 200)  {
//				editable = false
//			}
//
//			contents = new BorderPanel() {
//				add(textArea, BorderPanel.Position.Center)
//				add(new BoxPanel(Orientation.Horizontal) {
//					contents += ComboBox[Class[?]](SEventBus.events().toSeq.sortBy(_.getName))
//				}, BorderPanel.Position.North)
//			}
//		}.tap(mf => {
//			mf.pack()
//			mf.centerOnScreen()
//			mf.open()
//		})
	}

	override def shutdown(): Unit = {
//		gameTickFuture.cancel(false)
		ss.shutdown()
		Try {
			ss.awaitTermination(4000, TimeUnit.MILLISECONDS)
		}.toEither match {
			case Left(value) => value.printStackTrace()
			case Right(value) =>
		}
		super.shutdown()
	}
}
