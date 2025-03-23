package com.fredplugins.kroovy

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

import scala.compiletime.uninitialized
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.collections.TileObjects
import net.runelite.api.Client
import net.runelite.client.callback.ClientThread
import net.runelite.client.game.ItemManager
import net.runelite.client.ui.overlay.components.{ComponentOrientation, LayoutableRenderableEntity, LineComponent, SplitComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayPanel, OverlayPosition}
import org.slf4j.Logger

import java.awt.{Color, Dimension, Graphics2D}
import scala.Seq
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*

@Singleton
class NpcService {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: KroovyConfig = null
	@Inject val itemManager: ItemManager = null
}
