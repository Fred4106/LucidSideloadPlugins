package com.fredplugins.sailing

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.sailing.features.BarracudaTrialHelper
import com.fredplugins.sailing.features.BoatTracker
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import net.runelite.api.Client
import net.runelite.client.config.ConfigManager
import net.runelite.client.config.ConfigSection

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
		val conf = configManager.getConfig[FredsSailingConfig](classOf[FredsSailingConfig])
		log.debug("barracuda_section: {}", conf.barracuda_section)

		val confData = conf.getClass.getInterfaces.toList.map(inf => {
			val infName = inf.getSimpleName
			val fields  = inf.getFields.toList
			val fData = fields.map(f => {
				val fName  = f.getName
				val fType  = f.getType.getSimpleName
//				val fValue = f.get(conf)
				(fName, fType)
			})
			(infName, fData)
		}).map(x => {
			val s1=s"\t${x._1}"
			val s2 = x._2.map(x2 => {
				s"\t\t${x2._1}: ${x2._2}"
			}).mkString("\n")
			s"${s1}\n${s2}"
		}).mkString("\n")
		log.debug(s"conf: ${conf.getClass.getSimpleName}\n${confData}")

//		val fields = conf.getClass.getInterfaces()(0).getFields.toList
//		fields.foreach(f => {
//			if(f.isAnnotationPresent(classOf[ConfigSection])) {
//				val cs: ConfigSection = f.getDeclaredAnnotation(classOf[ConfigSection])
////				val annotStrings = f.getDeclaredAnnotations.toList.map(a => {
////					a.annotationType.getSimpleName
////				})
//				val fName = f.getName
//				val fType = f.getType.getSimpleName
//				val fValue = f.get(conf)
//				log.debug("field({})\n\t(name: {}, tpe: {}, val: {})\n\t@ConfigSection(name={}, position={}, closedByDefault={})", f, fName, fType, fValue, cs.name(), cs.position(), cs.closedByDefault())
//			}
//		})
		conf
	}

	@Provides
	@Singleton
	def provideUtils(client: Client, config: FredsSailingConfig): SailingUtils = {
		SailingUtils(client, config)
	}
}
