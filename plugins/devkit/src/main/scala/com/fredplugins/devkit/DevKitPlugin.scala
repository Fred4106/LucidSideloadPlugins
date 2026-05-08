package com.fredplugins.devkit

import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.extensions.MenuExtensions
import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getTileObjectOpt, isNpcAction, isTileObjectAction}
import com.fredplugins.common.utils.ShimUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.Client
import net.runelite.api.events.{GameTick, MenuEntryAdded, MenuOptionClicked}
import net.runelite.client.Notifier
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Devkit</html>",
	description = "Useful developer tools",
	tags = Array(
		"development", "tool"
		, "utility"
	)
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class DevKitPlugin() extends Plugin {
	@Inject val client  : Client               = null
	@Inject val clientThread  : ClientThread         = null
	@Inject val notifier: Notifier             = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private   val eventBus      : EventBus              = null
	@Inject private   val overlayManager: OverlayManager        = null
	@Inject private   val configManager: ConfigManager        = null
	@Inject private   val config: DevkitConfig        = null
	@Inject private   val regionsOverlay: RegionsOverlay        = null
	@Inject private   val regionsOverlayPanel: RegionsOverlayPanel        = null
	given Client = client

	@Provides
	def getDevkitConfig: DevkitConfig = configManager.getConfig(classOf[DevkitConfig])

	private var regionsMap: Map[String, WorldRegion] = Map.empty
	def regions: List[(String, WorldRegion)] = regionsMap.toList

//	def xteaKeys: Map[Int, Array[Int]] = {
//		Try {
//			val XTEA_CACHE = new File(RuneLite.CACHE_DIR, "xtea.json")
//			val in      = new FileInputStream(XTEA_CACHE)
//			val channel = in.getChannel
//			val reader  = new InputStreamReader(in, StandardCharsets.UTF_8)
//			val z = try {
//				channel.lock(0, Long.MaxValue, true)
//				gson.fromJson(reader, new TypeToken[Map[Int, Array[Int]]]() {}.getType)
//			} catch {
//				case ex: FileNotFoundException =>
//					Map.empty[Int, Array[Int]]
//				case e@(_: IOException | _: JsonSyntaxException) =>
//					log.debug("error loading xteas", e)
//					Map.empty[Int, Array[Int]]
//			} finally {
//				if (in != null) in.close()
//				if (channel != null) channel.close()
//				if (reader != null) reader.close()
//			}
//			z
//		}.getOrElse(Map.empty[Int, Array[Int]])
//	}
//	var keys: Set[RegionKey] = Set.empty[RegionKey]

	var keys: Set[RegionKey] = Set.empty[RegionKey]
	override protected def startUp(): Unit = {
		Thread(() => {
			keys = getKeys(config.cacheId)
		}).start()
		regionsMap = WorldRegionParser(config.regions)
		overlayManager.add(regionsOverlay)
		overlayManager.add(regionsOverlayPanel)


//		FredsTemporossLogic.init(this)
//		eventBus.register(FredsTemporossLogic)
//		overlayManager.add(panel)
//		overlayManager.add(overlay)
	}

	override protected def shutDown(): Unit = {
		keys = Set.empty
		configManager.setConfiguration(GROUP, "regions", WorldRegionParser.stringify(regionsMap))
		configManager.setConfiguration(GROUP, "testRegionResult", "")
		overlayManager.remove(regionsOverlay)
		overlayManager.remove(regionsOverlayPanel)
//		overlayManager.remove(panel)
//		overlayManager.remove(overlay)
//		eventBus.unregister(FredsTemporossLogic)
	}

	@Subscribe
	def onConfigChanged(event: ConfigChanged): Unit = {
		if(!event.getGroup.equals(GROUP)) return

		ConfigKeyParser(event.getKey) match {
			case "testRegionResult" :: Nil =>
			case "testRegion" :: Nil =>
				val found: Option[RegionKey] = keys.find(k => k.mapsquare == config.testRegion)
				configManager.setConfiguration(GROUP, "testRegionResult", found.toString)
				found.foreach(rk => {
					val indexVarbits = client.getIndex(rk.archive);
					indexVarbits.getFileIds(rk.archive).tap(x => log.debug("{}", x.toList))
				})
			case "regions" :: Nil => regionsMap = WorldRegionParser(event.getNewValue)
			case _ =>
		}

		log.debug(
			"Config (group: {}, address: {}) changed from {} to {}.",
			event.getGroup,
			ConfigKeyParser(event.getKey).pipe {
				case head :: Nil => head
				case seq => seq.mkString(".")
			},
			event.getOldValue,
			event.getNewValue
		)
	}
}
