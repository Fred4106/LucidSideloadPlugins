package com.fredplugins.pvmHelper

import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.Client
import net.runelite.api.events.NpcSpawned
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import org.slf4j.Logger

trait BossToolTrait {
//	def log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
//	def name: String = this.getClass.getSimpleName.stripSuffix("Logic")

	def resetState(): Unit = {}
	def inArea(): Boolean
	def layoutPanel(): Seq[LayoutableRenderableEntity] = List.empty
}

