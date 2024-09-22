package com.fredplugins.tempoross

import com.fredplugins.common.extensions.MenuExtensions
import com.fredplugins.common.extensions.MenuExtensions.{getTileObjectOpt, isTileObjectAction, isNpcAction, getNpcOpt}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.tempoross.FredsTemporossLogic.{inMinigame, inRewardArea}
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.Client
import net.runelite.api.events.{GameTick, MenuEntryAdded, MenuOptionClicked}
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import org.slf4j.Logger

import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.util.chaining.scalaUtilChainingOps

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Tempoross</html>",
	description = "Useful information and tracking for the Tempoross skilling boss",
	tags = Array(
		"fishing", "minigame"
		, "skilling"
	),
	conflicts = Array(
		"Tempoross",
	)
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsTemporossPlugin() extends Plugin {
	@Inject val client  : Client               = null
	@Inject val clientThread  : ClientThread         = null
	@Inject val config  : FredsTemporossConfig = null
	@Inject val notifier: Notifier             = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private   val eventBus      : EventBus              = null
	@Inject private   val overlayManager: OverlayManager        = null
	@Inject private val panel         : FredsTemporossPanel   = null
	@Inject private   val overlay       : FredsTemporossOverlay = null

	given Client = client

	@Provides
	def getConfig(configManager: ConfigManager): FredsTemporossConfig = {
		configManager.getConfig[FredsTemporossConfig](classOf[FredsTemporossConfig])
	}

	override protected def startUp(): Unit = {
		FredsTemporossLogic.init(this)
		eventBus.register(FredsTemporossLogic)
		overlayManager.add(panel)
		overlayManager.add(overlay)
	}

	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
		overlayManager.remove(overlay)
		eventBus.unregister(FredsTemporossLogic)
	}

	private var wasInMinigame = false
	private var wasInRewardArea = false
	@Subscribe
	def onGameTick(tick: GameTick): Unit = {
		Option((wasInRewardArea || wasInMinigame, inRewardArea || inMinigame)).foreach {
//			case (true, false) => overlayManager.remove(panel)
//			case (false, true) => overlayManager.add(panel)
			case (_, _) =>
		}
		Option((wasInMinigame, inMinigame)).foreach {
			case (true, false) => {
//				overlayManager.remove(overlay)
//				eventBus.unregister(FredsTemporossLogic)
				FredsTemporossLogic.reset()
			}
			case (false, true) => {
				FredsTemporossLogic.spawnLoc = client.getLocalPlayer.getWorldLocation
//				eventBus.register(FredsTemporossLogic)
//				overlayManager.add(overlay)
			}
			case (_, _) =>
		}
		wasInRewardArea = inRewardArea
		wasInMinigame = inMinigame

//		client.getVarbitValue(VARB_REWARD_POOL_NUMBER)
	}


	def onMenuEntryAdded(mea: MenuEntryAdded): Unit = {
		val me = mea.getMenuEntry
		val tlwv = client.getTopLevelWorldView
		val temp = Option(
			(me.getOption, me.getTarget, me.getType, me.getIdentifier, (me.getParam0, me.getParam1))
		)
		val toLog = temp.collect{
			case (opt, targ, tpe, ident, (x, y)) if(me.isTileObjectAction) => {
				Option(tlwv.getScene.getTiles.apply(tlwv.getPlane)(x)(y)).map(_.getWorldLocation).map(wp => (opt, targ, tpe, ident, (x, y), wp))
					.map {
						case (opt, _, tpe, ident, (x, y), wp) => s"Object(ident=${ident}, sLoc=($x, $y), wp=$wp, tpe=${tpe})"
					}.getOrElse(s"Error: tpe=${tpe}")
			}
			case (opt, targ, tpe, ident, (x, y)) if(me.isNpcAction) => s"NPC(index=$ident, id=${me.getNpc.getId}, sLoc=${(x, y)}, tpe=${tpe})"
//			case (opt, targ, tpe, ident, (x, y)) => s"opt=\"$opt\", targ=\"$targ\", ident=$ident, param=${(x, y)}, tpe=${tpe}"
		}.foreach(toLog=>{
			log.debug("Added: ({})",toLog)
		})
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

