package com.fredplugins.sailing.features

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.sailing.PluginLifecycleComponent
import com.fredplugins.sailing.SailingUtils
import com.fredplugins.sailing.model.Boat
import com.fredplugins.sailing.model.CargoHoldTier
import com.fredplugins.sailing.model.HelmTier
import com.fredplugins.sailing.model.HullTier
import com.fredplugins.sailing.model.SailTier
import com.fredplugins.sailing.model.SalvagingHookTier
import com.google.inject.Inject
import net.runelite.api.Client
import net.runelite.api.coords.LocalPoint
import net.runelite.api.events.GameObjectDespawned
import net.runelite.api.events.GameObjectSpawned
import net.runelite.api.events.GameTick
import net.runelite.api.events.WorldEntityDespawned
import net.runelite.api.events.WorldEntitySpawned
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.TitleComponent

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import java.awt.{Color, Dimension, Graphics2D}
import scala.collection.mutable

class BoatTracker @Inject()(
	private val client: Client,
	private val utils: SailingUtils
) extends OverlayPanel() with PluginLifecycleComponent with ShimUtils.Logging("TRACE") {

	val trackedBoats: mutable.Map[Int, Boat] = mutable.HashMap.empty[Int, Boat]
	private var currentSpeed: Int = 0
	private var lastValidAngle: Int   = -1
	private var lastPoint: LocalPoint = uninitialized

	override def shutDown(): Unit = {
		trackedBoats.clear()
	}

	@Subscribe
	def onWorldEntitySpawned(e: WorldEntitySpawned): Unit = {
		val we = e.getWorldEntity
		if (Set(1,2,3).contains(we.getConfig.getId)) {
			val wvId = we.getWorldView.getId
			log.trace("tracking boat in wv {}", wvId)
			trackedBoats.put(wvId, new Boat(wvId, we))
		}
	}

	@Subscribe
	def onWorldEntityDespawned(e: WorldEntityDespawned): Unit = {
		if (trackedBoats.remove(e.getWorldEntity.getWorldView.getId) != null) log.trace("removed tracking boat from wv {}", e.getWorldEntity.getWorldView.getId)
	}

	@Subscribe
	def onGameObjectSpawned(e: GameObjectSpawned): Unit = {
		val o    = e.getGameObject
		getBoat(o.getWorldView.getId)
			.foreach{boat =>
				if (HullTier.fromGameObjectId(o.getId) != null) {
					boat.setHull(o)
					log.trace(
						"found hull {}={}+{} for boat in wv {}", o.getId, boat.getHullTier, boat.getSizeClass, boat.getWorldViewId)
				}
				if (SailTier.fromGameObjectId(o.getId) != null) {
					boat.setSail(o)
					log.trace("found sail {}={} for boat in wv {}", o.getId, boat.getSailTier, boat.getWorldViewId)
				}
				if (HelmTier.fromGameObjectId(o.getId) != null) {
					boat.setHelm(o)
					log.trace("found helm {}={} for boat in wv {}", o.getId, boat.getHelmTier, boat.getWorldViewId)
				}
				if (SalvagingHookTier.fromGameObjectId(o.getId) != null) {
					boat.setSalvagingHook(o)
					log.trace("found salvaging hook {}={} for boat in wv {}", o.getId, boat.getSalvagingHookTier, boat.getWorldViewId)
				}
				if (CargoHoldTier.fromGameObjectId(o.getId) != null) {
					boat.setCargoHold(o)
					log.trace("found cargo hold {}={} for boat in wv {}", o.getId, boat.getCargoHoldTier, boat.getWorldViewId)
				}
			}
	}

	@Subscribe
	def onGameObjectDespawned(e: GameObjectDespawned): Unit = {
		val o    = e.getGameObject
		getBoat(o.getWorldView.getId)
			.foreach{boat =>
				if (boat.getHull eq o) {
					boat.setHull(null)
					log.trace("unsetting hull for boat in wv {}", boat.getWorldViewId)
				}
				if (boat.getSail eq o) {
					boat.setSail(null)
					log.trace("unsetting sail for boat in wv {}", boat.getWorldViewId)
				}
				if (boat.getHelm eq o) {
					boat.setHelm(null)
					log.trace("unsetting helm for boat in wv {}", boat.getWorldViewId)
				}
				if (boat.getSalvagingHook eq o) {
					boat.setSalvagingHook(null)
					log.trace("unsetting salvaging hook for boat in wv {}", boat.getWorldViewId)
				}
				if (boat.getCargoHold eq o) {
					boat.setCargoHold(null)
					log.trace("unsetting cargo hold for boat in wv {}", boat.getWorldViewId)
				}
			}
	}
	@Subscribe
	def onGameTick(e: GameTick): Unit = {
		Option.when(utils.isSailing)(getBoat).flatten match {
			case Some(boat) => {
				val current = utils.getTopLevelLocalPoint
				if(current != null && lastPoint != null) {
					Option(current.distanceTo(lastPoint))
						.filter(_ <= 1500)
						.foreach(newSpeed => {
							currentSpeed = newSpeed
							val dx = current.getX - lastPoint.getX
							val dy = current.getY - lastPoint.getY
							if (dx != 0 || dy != 0) {
								lastValidAngle = Math.toDegrees(Math.atan2(dy, dx)).toInt
							}
						})
				}
				lastPoint = current
			}
			case None => {
				lastPoint = null
				currentSpeed = 0
				lastValidAngle = -1
			}
		}
	}

	def angle: Int = lastValidAngle
	def speed: Int = currentSpeed

	def getBoat(wvId: Int): Option[Boat] = {
		Option.when(wvId != -1 && trackedBoats.contains(wvId)){
			trackedBoats(wvId)
		}
	}
	def getBoat: Option[Boat] = getBoat(client.getLocalPlayer.getWorldView.getId)
	override def render(graphics: Graphics2D): Dimension = {
		def add(c: LayoutableRenderableEntity): Unit = {
			panelComponent.getChildren.add(c)
		}
		getBoat.fold(null.asInstanceOf[Dimension])(boat => {
			add(TitleComponent.builder.text("Ship Tracker").color(Color.CYAN).build)
			Option(boat.getHullTier)
				.map(hull => {
					LineComponent.builder
						.left("Hull")
						.right(s"${hull}")
						.build
				})
				.foreach(add)
			Option(boat.getHelmTier)
				.map(helm => {
					LineComponent.builder
						.left("Helm")
						.right(s"${helm}")
						.build
				})
				.foreach(add)
			Option(boat.getSailTier)
				.map(sail => {
					LineComponent.builder
						.left("Sail")
						.right(s"${sail}")
						.build
				})
				.foreach(add)
			Option(boat.getCargoHoldTier)
				.map(cargo => {
					LineComponent.builder
						.left("Cargo")
						.right(s"${cargo}")
						.build
				})
				.foreach(add)
			Option(boat.getSalvagingHookTier)
				.map(hook => {
					LineComponent.builder
						.left("Hook")
						.right(s"${hook}")
						.build
				})
				.foreach(add)

			add(
				LineComponent.builder
					.left("Speed")
					.right(s"${speed}/${boat.getMaxSpeed()}")
					.build
			)
			add(
				LineComponent.builder
					.left("Angle")
					.right(s"${angle}")
					.build
			)
			super.render(graphics)
		})
	}
}

