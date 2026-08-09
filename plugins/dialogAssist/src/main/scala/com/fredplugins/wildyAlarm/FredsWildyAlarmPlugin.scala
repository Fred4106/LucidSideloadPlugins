package com.fredplugins.wildyAlarm

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{Inventory, TileItems}
import ethanApiPlugin.lucidplugins.api.utils.InteractionUtils
import net.runelite.api.TileItem.OWNERSHIP_OTHER
import net.runelite.api.events.{GameTick, PlayerDespawned}
import net.runelite.api.gameval.{InterfaceID, VarbitID}
import net.runelite.api.widgets.Widget
import net.runelite.api.{Client, Constants, Player, WorldType}
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import packets.TileItemPackets

import java.util.regex.{Matcher, Pattern}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.util.matching.Regex

@PluginDescriptor(
	name = "<html><font color=#6b8af6>[P]</font> Wilderness Player Alarm</html>",
	description = "Alerts you when another player is detected nearby in the wilderness",
	tags = Array("wilderness", "alert", "detector", "alarm", "rat", "pk", "players"),
	hidden = false)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsWildyAlarmPlugin  extends Plugin with ShimUtils.Logging("Debug") {
	@Inject private val client: Client = null
	@Inject private val config: FredsWildyAlarmConfig = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val overlay: AlarmOverlay = null

	private val playerToTimeInRange: mutable.Map[Player, Int] = scala.collection.mutable.HashMap.empty

	override protected def startUp(): Unit = {
		overlayManager.add(overlay)
		overlay.setLayer(config.flashLayer.getLayer)
	}

	override protected def shutDown(): Unit = {
		overlayManager.remove(overlay)
	}
	@Subscribe
	def onConfigChanged(event: ConfigChanged): Unit = {
		if(event.getGroup.equals("FredsWildyAlarm")) {
			overlay.setLayer(config.flashLayer.getLayer)
		}
	}
	@Subscribe
	def onPlayerDespawned(event: PlayerDespawned): Unit = {
		playerToTimeInRange.remove(event.getPlayer)
	}

	var shouldFlash: Boolean = false
	var playersToHighlight: Set[Player] = Set.empty
	var wildRange: Option[(Int, Int)] = Option.empty
	@Subscribe
	def onGameTick(event: GameTick): Unit = {
		if (client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) != 1 && !isInPvp) {
			playerToTimeInRange.clear()
			shouldFlash = false
			playersToHighlight = Set.empty
			wildRange = Option.empty
			return
		}
		wildRange = getWildernessRange
		// Keep track of how long players have been in range if timeout is enabled

		/*
		 *updatePlayersInRange()
		 */
		val currentPosition = client.getLocalPlayer.getWorldLocation
		val alarmRadius = config.alarmRadius
		val players = client.getTopLevelWorldView.players.asScala.toList
		val inRange: Seq[Player] = players.filter(_.getWorldLocation.distanceTo(currentPosition) <= alarmRadius)
		playerToTimeInRange.filterInPlace((p, v) => inRange.contains(p))
		inRange.foreach {
			playerToTimeInRange.updateWith(_)(_.map(_ + Constants.GAME_TICK_LENGTH).orElse(Option(0)))
		}
		//end updatePlayersInRange
		playersToHighlight = playerToTimeInRange.toMap.keySet.filter(shouldPlayerTriggerAlarm)
		shouldFlash = playersToHighlight.nonEmpty
		import net.runelite.api.TileItem.OWNERSHIP_GROUP
		import net.runelite.api.TileItem.OWNERSHIP_OTHER
		import net.runelite.api.TileItem.OWNERSHIP_SELF
		val gnomeTreeSeedTele: Widget = Inventory.search().withId(19564).first().toScala.orNull
		val valuableDrops = TileItems.search.withName("Voidwaker blade", "Dragon 2h sword", "Rune pickaxe", "Dragon pickaxe", "Skull of vet'ion", "Ring of the gods").result().asScala.toList
		if(shouldFlash && gnomeTreeSeedTele != null && valuableDrops.isEmpty) {
			if(currentPosition.getTemplate.getRegionID == 7604) {
				InteractionUtils.widgetInteract(gnomeTreeSeedTele, "Commune")
			}
		}
	}

	private def isInPvp: Boolean = {
		var pvp = WorldType.isPvpWorld(client.getWorldType) && (client.getVarbitValue(VarbitID.PVP_AREA_CLIENT) == 1)
		val widget = client.getWidget(InterfaceID.PvpIcons.WILDERNESSLEVEL)
		if (widget != null) {
			val widgetText = widget.getText
			pvp &= !widgetText.startsWith("Protection")
			pvp &= !widgetText.startsWith("Guarded")
			pvp &= !widgetText.startsWith("No PvP")
		}
		pvp
	}

	private val WILDERNESS_LEVEL_PATTERN: Regex = """^Level: (\d+)<br>(\d+)-(\d+)$""".r

//	private def getWildernessLevel: Int = {
//		Option(client.getWidget(InterfaceID.PvpIcons.WILDERNESSLEVEL)).map(_.getText).collect {
//			case WILDERNESS_LEVEL_PATTERN(lvl) => lvl.toInt
//		}.getOrElse(-1)
//	}

	private def getWildernessRange: Option[(Int, Int)] = {
		val cbLvl = Option(client.getLocalPlayer).map(_.getCombatLevel).getOrElse(0)
		val wl = Option(client.getWidget(InterfaceID.PvpIcons.WILDERNESSLEVEL))
			.map(_.getText)
			.collect {
				case WILDERNESS_LEVEL_PATTERN(lvl, min, max) => {
					log.debug(s"lvl is ${lvl} ${min} ${max}")
					lvl.toIntOption
				}
			}
			.flatten.map(wl => Math.max(0, cbLvl-wl) -> Math.min(126, cbLvl + wl))
		wl
	}

	def shouldPlayerTriggerAlarm(player: Player): Boolean = {
		given Client = client
		// Don't trigger for yourself
		// Don't trigger for clan members
		// Don't trigger for friends chat members
		// Don't trigger for friends
		// Don't trigger for players inside ferox enclave
		if (player.isLocal || player.isFriend || player.isClanMember || player.isFriendsChatMember || InsideFerox(player)) return false
//		val playerInCbrange = Option(getWildernessLevel).filter(_ != -1).map(wl => client.getLocalPlayer.getCombatLevel.pipe(cl => (-wl to wl).map(_ + cl))).exists(r => {
//			log.debug(s"range=${r.min} to ${r.max}")
//			r.contains(player.getCombatLevel)
//		})
		if(player.isInteracting && player.getInteracting == client.getLocalPlayer) return true

		//check if its outside of wilderness range
//		if(getWildernessRange.pipe((min, max) => player.getCombatLevel<min || player.getCombatLevel > max)) return false
		if(wildRange.isDefined && !wildRange.exists {
			case (min, max) => min to max contains player.getCombatLevel
		}) return false
		// Ignore players that have been on screen longer than the timeout
		if (config.timeoutToIgnore != 0) {
			if(playerToTimeInRange.getOrElse(player, 0) > config.timeoutToIgnore * 1000) return false
		}
		true
	}

	@Provides
	def provideConfig(configManager: ConfigManager): FredsWildyAlarmConfig = configManager.getConfig(classOf[FredsWildyAlarmConfig])
}
