package com.fredplugins.tempoross

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.tempoross.Constants.*
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.{ChatMessage, GameObjectDespawned, GameObjectSpawned, GameStateChanged, ItemContainerChanged, NpcDespawned, NpcSpawned, ScriptPreFired, VarbitChanged}
import net.runelite.api.{ChatMessageType, Client, GameObject, GameState, InventoryID, ItemID, NPC, NpcID, NullObjectID, ObjectID}
import net.runelite.client.Notifier
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.game.ItemManager
import net.runelite.client.menus.MenuManager
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.ui.overlay.infobox.InfoBoxManager
import org.slf4j.Logger

import java.awt.Color
import java.awt.image.BufferedImage
import java.time.Instant
import java.util
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

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
  private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

  @Inject private val eventBus: EventBus = null
  @Inject val client: Client = null
  @Inject private val overlayManager: OverlayManager = null
  @Inject private val overlay: FredsTemporossOverlay = null
  @Inject private val itemManager: ItemManager = null

  @Inject val infoBoxManager: InfoBoxManager = null
  @Inject val config: FredsTemporossConfig = null
  @Inject val notifier: Notifier = null

  var rewardInfoBox: TemporossInfoBox = uninitialized
  var fishInfoBox: TemporossInfoBox = uninitialized
  var damageInfoBox: TemporossInfoBox = uninitialized
  var phaseInfoBox: TemporossInfoBox = uninitialized

  given Client = client

  @Provides
  def getConfig(configManager: ConfigManager): FredsTemporossConfig = {
    configManager.getConfig[FredsTemporossConfig](classOf[FredsTemporossConfig])
  }

  def redrawInfoBoxes(): Unit = {
    def addFishInfoBox(): Unit = {
      import FredsTemporossLogic.{uncookedFish, crystalFish, cookedFish}
      val text = (uncookedFish + crystalFish) + "/" + cookedFish + "\n" + (uncookedFish + cookedFish + crystalFish)
      val tooltip = "Uncooked Fish: " + (uncookedFish + crystalFish) + "</br>Cooked Fish: " + cookedFish + "</br>Total Fish: " + (uncookedFish + cookedFish + crystalFish)
      if (fishInfoBox == null) {
        fishInfoBox = createInfobox("fish", itemManager.getImage(FISH_IMAGE_ID), text, tooltip)
        infoBoxManager.addInfoBox(fishInfoBox)
      }
      else {
        fishInfoBox.setText(text)
        fishInfoBox.setTooltip(tooltip)
      }
    }

    def addDamageInfoBox(): Unit = {
      val text = Integer.toString(FredsTemporossLogic.damage)
      val tooltip = "Damage: " + FredsTemporossLogic.damage
      if (damageInfoBox == null) {
        damageInfoBox = createInfobox("damage", itemManager.getImage(DAMAGE_IMAGE_ID), text, tooltip)
        infoBoxManager.addInfoBox(damageInfoBox)
      }
      else {
        damageInfoBox.setText(text)
        damageInfoBox.setTooltip(tooltip)
      }
    }

    def addPhaseInfoBox(): Unit = {
      val text = Integer.toString(FredsTemporossLogic.phase)
      val tooltip = "Phase " + FredsTemporossLogic.phase
      if (phaseInfoBox == null) {
        phaseInfoBox = createInfobox("phase", PHASE_IMAGE, text, tooltip)
        infoBoxManager.addInfoBox(phaseInfoBox)
      }
      else {
        phaseInfoBox.setText(text)
        phaseInfoBox.setTooltip(tooltip)
      }
    }

    if (config.phaseIndicator) addPhaseInfoBox()
    if (config.damageIndicator) {
      addDamageInfoBox()
    }

    if (config.fishIndicator) {
      addFishInfoBox()
    }
  }

  def addRewardInfoBox(): Unit = {
    this.addRewardInfoBox(client.getVarbitValue(VARB_REWARD_POOL_NUMBER))
  }

  def addRewardInfoBox(rewardPoints: Int): Unit = {
    val text = Integer.toString(rewardPoints)
    val tooltip = rewardPoints + " Reward Point" + (if (rewardPoints == 1) ""
    else "s")
    if (rewardInfoBox == null) {
      rewardInfoBox = createInfobox("reward", itemManager.getImage(REWARD_POOL_IMAGE_ID), text, tooltip)
      infoBoxManager.addInfoBox(rewardInfoBox)
    }
    else {
      rewardInfoBox.setText(text)
      rewardInfoBox.setTooltip(tooltip)
    }
  }

  private def createInfobox(name: String, image: BufferedImage, text: String, tooltip: String) = {
    val infoBox = new TemporossInfoBox(image, this, name)
    infoBox.setText(text)
    infoBox.setTooltip(tooltip)
    infoBox
  }

  def addTotemTimers(): Unit = {
    val tethered = client.getVarbitValue(VARB_IS_TETHERED) > 0
    FredsTemporossLogic.gameObjects.toList.filter(j => DAMAGED_TETHER_GAMEOBJECTS.contains(j._1.getId) || TETHER_GAMEOBJECTS.contains(j._1.getId)).foreach((`object`, drawObject) => {
      val color =
        if (tethered) config.tetheredColor
        else `object`.getId match {
          case ObjectID.DAMAGED_MAST_40996 => config.poleBrokenColor
          case ObjectID.DAMAGED_MAST_40997 => config.poleBrokenColor
          case ObjectID.DAMAGED_TOTEM_POLE => config.poleBrokenColor
          case ObjectID.DAMAGED_TOTEM_POLE_41011 => config.poleBrokenColor
          case _ => config.waveTimerColor
        }
      if (FredsTemporossLogic.waveIsIncoming) {
        drawObject.setStartTime(FredsTemporossLogic.waveIncomingStartTime)
      }
      drawObject.setColor(color)
    })
  }

  def removeTotemTimers(): Unit = {
    FredsTemporossLogic.gameObjects.filterInPlace {
      case (go, doo) => !(Constants.TETHER_GAMEOBJECTS.contains(go.getId) || Constants.DAMAGED_TETHER_GAMEOBJECTS.contains(go.getId))
    }
  }

  override protected def startUp(): Unit = {
    FredsTemporossLogic.init(this)
    overlayManager.add(overlay)
    eventBus.register(FredsTemporossLogic)
  }

  override protected def shutDown(): Unit = {
    eventBus.unregister(FredsTemporossLogic)
    overlayManager.remove(overlay)
    List(fishInfoBox, damageInfoBox, phaseInfoBox, rewardInfoBox).foreach(infoBoxManager.removeInfoBox(_))
    fishInfoBox = null
    damageInfoBox = null
    phaseInfoBox = null
    rewardInfoBox = null
  }
}

