package com.fredplugins.sailing

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.sailing.features.BarracudaTrialHelper
import com.fredplugins.sailing.features.BoatTracker
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import net.runelite.api.Client
import net.runelite.client.config.ConfigManager

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class FredsSailingModule extends AbstractModule with ShimUtils.Logging("DEBUG") {

	override def configure(): Unit = {
		bind(classOf[ComponentManager])
	}

	@Provides
	def lifecycleComponents(
		boatTracker: BoatTracker,
		barracudaTrialHelper: BarracudaTrialHelper
	): Set[PluginLifecycleComponent] = {
		Set(
			boatTracker,
			barracudaTrialHelper
		)
	}

	@Provides
	@Singleton
	def provideConfig(configManager: ConfigManager): FredsSailingConfig = {
		configManager.getConfig[FredsSailingConfig](classOf[FredsSailingConfig])
	}

	@Provides
	@Singleton
	def provideUtils(client: Client, config: FredsSailingConfig): SailingUtils = {
		SailingUtils(client, config)
	}
}
