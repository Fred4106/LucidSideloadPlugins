package com.fredplugins.mixology

import com.fredplugins.common.extensions.MenuExtensions
import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getTileObjectOpt, isNpcAction, isTileObjectAction}
import com.fredplugins.common.extensions.ObjectExtensions.morphId
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.{ChatMessageType, Client, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.{GameTick, MenuEntryAdded, MenuOptionClicked, ScriptPostFired}
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import org.slf4j.Logger

import scala.jdk.StreamConverters.StreamHasToScala
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
	@Inject val client      : Client              = null
	@Inject val clientThread: ClientThread        = null
	@Inject val config      : FredsMixologyConfig = null
	@Inject val notifier    : Notifier            = null
	private         val log           : Logger             = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private val eventBus      : EventBus           = null
	@Inject private val overlayManager: OverlayManager     = null
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

	var region    : Int    = -1
	var previousRegion: Int = -1
	var isInRegion: Boolean = false
	var previousIsInRegion: Boolean =false
	var pedestals: List[MixType] = List.empty
	var previousPedistals: List[MixType] = List.empty

	//	val toolBenchBrews: CachedBox[List[(TileObject, Brew)]] = cachedBox[List[(ProcessesType, Brew)]]()
	//	Brew(None)(a => {
	//		a.pip
	//	})

	var toolBenches: List[(ProcessesType, (TileObject, Option[Brew]))] = List.empty
	var previousToolBenches: List[(ProcessesType, (TileObject, Option[Brew]))] = List.empty
	@Subscribe
	def onGameTick(tick: GameTick): Unit = {
		previousRegion = region
		previousIsInRegion = isInRegion
		region = Try(WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1)
		isInRegion = region == 5521


		(isInRegion, previousIsInRegion) match {
			case (true, false) => {
				//now in region
				//set state
			}
			case (false, true) => {
				//clear state
			}
			case (_, _) =>
		}

		if(isInRegion) {
			previousPedistals = pedestals
			pedestals = TileObjects.search().withId(55392, 55393, 55394).result().asScala.toList.sortBy(_.getId).flatMap {
				to => MixType.fromId(to.morphId).toScala
			}
			previousToolBenches = toolBenches
			toolBenches = for {
				mode <- ProcessesType.values().toList
				to <- TileObjects.search().withId(mode.baseId).nearestToPlayer().toScala
				brew = Brew.values.find(b => to.morphId - mode.emptyId - 1 == b.ordinal())
			} yield (mode, (to, brew))
		}
		
//		clientThread.invoke(new Runnable {
//			override def run(): Unit = {
//				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "", "mixology", false)
//			}
//		})
//
//		log.warn("isInRegion {}, region {}", isInRegion, region)
	}
	@Subscribe
	def onScriptPostFired(event: ScriptPostFired): Unit = {
		if (event.getScriptId != PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDER) return
		val baseWidget = client.getWidget(COMPONENT_POTION_ORDERS)
		if (baseWidget == null) return
		val textComponents = Widgets.search().withId(baseWidget.getId)
//		if (textComponents.size < 4) return
//		for (order <- potionOrders) {
//			// The first text widget is always the interface title 'Potion Orders'
//			appendPotionRecipe(textComponents.get(order.idx), order.idx, bestPotionOrderIdx == order.idx, order.fulfilled)
//		}
	}

	def getBrewType(idx: Int): (ProcessesType, Brew) = {
		(ProcessesType.values()(client.getVarbitValue(VARBIT_POTION_MODIFIER(idx)) - 1),
		Brew.values()(client.getVarbitValue(VARBIT_POTION_ORDER(idx)) - 1))
	}
	@Subscribe
	def onMenuOptionClicked(mec: MenuOptionClicked): Unit = {
		val me = mec.getMenuEntry
		val tlwv = client.getTopLevelWorldView
		val temp = Option(
			(me.getOption, me.getTarget, me.getType, me.getIdentifier, (me.getParam0, me.getParam1))
		)
		val toLog = temp.map{
			case (opt, targ, tpe, ident, (x, y)) if	(me.isTileObjectAction) => {
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

