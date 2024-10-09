package com.fredplugins.mixology

import com.fredplugins.common.extensions.MenuExtensions
import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getTileObjectOpt, isNpcAction, isTileObjectAction}
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.TileObjects
import net.runelite.api.{Client, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.{GameTick, MenuEntryAdded, MenuOptionClicked}
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import org.slf4j.Logger
import scala.jdk.StreamConverters.StreamHasToScala;
import java.util
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.IntAccumulator
import scala.jdk.OptionConverters.*
import scala.util.{Random, Try}
import scala.util.chaining.*
@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Mixology</html>",
	description = "Useful information and tracking for the Mixology minigame",
	tags = Array(
		"herblore", "minigame"
		, "skilling"
	)
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsMixologyPlugin() extends Plugin {
	@Inject val client  : Client               = null
	@Inject val clientThread  : ClientThread         = null
	@Inject val config  : FredsMixologyConfig = null
	@Inject val notifier: Notifier             = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private   val eventBus      : EventBus              = null
	@Inject private   val overlayManager: OverlayManager = null
	@Inject private val panel         : FredsMixologyPanel = null
//	@Inject private   val overlay       : FredsTemporossOverlay = null

	given Client = client

	@Provides
	def getConfig(configManager: ConfigManager): FredsMixologyConfig = {
		configManager.getConfig[FredsMixologyConfig](classOf[FredsMixologyConfig])
	}

	override protected def startUp(): Unit = {
//		FredsTemporossLogic.init(this)
//		eventBus.register(FredsTemporossLogic)
		overlayManager.add(panel)
//		overlayManager.add(overlay)
	}

	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
//		overlayManager.remove(overlay)
//		eventBus.unregister(FredsTemporossLogic)
	}
	sealed trait CachedBox[P](supplier: () => P) {
		var previousValue: P = _
		var cached       : P = _
		def previous: P = previousValue
		var cachedTick: Int = -1
		def value: P = cached
		def map[B](func: P=>B): CachedBox[B] = new CachedBoxTransform(this)(func)
	}

	class CachedBoxImpl[A](s:() => A) extends CachedBox[A](s) {
//		def map[B](f: A: A => B => CachedBox[B] = new CachedBoxTransform()
//		override def dirty: Boolean = ???
//		override def previous: A = ???
//		override inline type P = A

		def update(): Unit = {
			if (cachedTick != client.getTickCount) {
				cachedTick = client.getTickCount
				previousValue = cached
				cached = s()
			}
		}
//		var cachedTick: Int = -1
//		override def dirty: Boolean =
	}
	class CachedBoxTransform[A, B](parent: CachedBox[A])(func: A => B) extends CachedBox[B](() => func(parent.value)) {
		def update(): Unit = {
			if (client.getTickCount != cachedTick) {
				cachedTick = client.getTickCount
				previousValue = cached
				cached = func(parent.value)
			}
		}
	}
//	def region: Int = Try(WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1)

	val region    : CachedBox[Int] = new CachedBoxImpl(
		() => Try(WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1)
	)

	val isInRegion: CachedBox[Boolean] =region.map(_ != 5512)
	val realValueObjIds: List[Int] = MixType.realValues.stream().flatMap(x => x.pedestal_ids.stream()).toScala(IntAccumulator).toList

	def pedistals: CachedBoxImpl[List[(MixType, Int)]] = new CachedBoxImpl[List[(MixType, Int)]](() => {
		TileObjects.search().withId(realValueObjIds *).result().asScala.toList.groupMap(to => MixType.values().find(_.pedestal_ids.contains(to.getId)).get)(v => v).map {
			case (mix, objects) => mix -> objects.size
		}.toList
	}) {

	}
	//	private var cachedInRegion: Boolean = false
//	private var tickInRegionCachedAt: Int = -1
//
//	def inRegion: Boolean = {
//		val tc = client.getTickCount
//		if(tc != tickInRegionCachedAt) {
//			cachedInRegion = region == 5512
//			tickInRegionCachedAt = tc
//		}
//		cachedInRegion
//	}


	@Subscribe
	def onGameTick(tick: GameTick): Unit = {
		Option(isInRegion.value, isInRegion.previous).foreach {
			case (true, false) => {
				//now in region
				//set state
			}
			case (false, true) => {
				//clear state
			}
			case (_, _) =>
		}
	}

	@Subscribe
	def onMenuOptionClicked(mec: MenuOptionClicked): Unit = {
		val me = mec.getMenuEntry
		val tlwv = client.getTopLevelWorldView
		val temp = Option(
			(me.getOption, me.getTarget, me.getType, me.getIdentifier, (me.getParam0, me.getParam1))
		)
		val toLog = temp.map{
			case (opt, targ, tpe, ident, (x, y)) if(me.isTileObjectAction) => {
				Option(tlwv.getScene.getTiles.apply(tlwv.getPlane)(x)(y)).map(_.getWorldLocation).map(wp => (opt, targ, tpe, ident, (x, y), wp))
					.map {
						case (opt, _, tpe, ident, (x, y), wp) => s"Object(opt=\"$opt\", targ=\"$targ\", id=${ident}, sLoc=($x, $y), wp=$wp, tpe=${tpe})"
					}.getOrElse(s"Error: tpe=${tpe}")
			}
			case (opt, targ, tpe, ident, (x, y)) if(me.isNpcAction) => s"NPC(opt=\"$opt\", targ=\"$targ\", index=$ident, id=${me.getNpc.getId}, sLoc=${(x, y)}, tpe=${tpe})"
			case (opt, targ, tpe, ident, (x, y)) => s"opt=\"$opt\", targ=\"$targ\", ident=$ident, param=${(x, y)}, tpe=${tpe}"
		}.get
		log.debug("Clicked: ({})",toLog)
	}
}

