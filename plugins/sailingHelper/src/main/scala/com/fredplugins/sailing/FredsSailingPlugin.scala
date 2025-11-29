package com.fredplugins.sailing

import com.google.inject.{Inject, Singleton}
import net.runelite.client.plugins.Plugin
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.utils.ShimUtils
import com.google.gson.{Gson, JsonObject, JsonParser, TypeAdapter}
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.{JsonReader, JsonWriter}
import com.google.inject.Binder
import ethanApiPlugin.EthanApiPlugin
import net.runelite.client.RuneLite
import net.runelite.api.coords.WorldPoint
import net.runelite.client.plugins.PluginDependency
import net.runelite.client.plugins.PluginDescriptor
import org.slf4j.Logger

import java.io.{IOException, InputStreamReader}
import scala.compiletime.constValue
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Failure, Random, Success, Try}
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import scala.compiletime.uninitialized

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Sailing</html>",
	description = "Sailing utilites",
	tags = Array(
		"sailing", "trials"
	)
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsSailingPlugin extends Plugin {
	@Inject
	private val componentManager: ComponentManager = null
	override def configure(binder: Binder): Unit = binder.install(new FredsSailingModule())

	override def startUp(): Unit = {
		componentManager.onPluginStart()
	}
	override def shutDown(): Unit = {
		componentManager.onPluginStop()
	}


}
