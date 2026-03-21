package com.fredplugins.dialogAssist

import net.runelite.api.Client
import net.runelite.api.ItemComposition
import net.runelite.api.gameval.InterfaceID
import net.runelite.api.gameval.InterfaceID.Skillmulti
import net.runelite.client.callback.ClientThread

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class SkillMultiHelper(client: Client, clientThread: ClientThread) {
	case class SkillMultiOption(widgetId: Int, itemId: Int) {
		lazy val itemDef: ItemComposition = clientThread.runOnClientThread(() => client.getItemDefinition(itemId))
	}

	case class SkillMultiOptions(options: SkillMultiOption *) {
		def asJavaList: java.util.List[SkillMultiOption] = options.asJava

		def find(pred: ItemComposition => Boolean): SkillMultiOption =
			options.find(_.itemDef.pipe(pred(_))).getOrElse(null)

		def filter(pred: ItemComposition => Boolean): SkillMultiOptions =
			options.filter(_.itemDef.pipe(pred(_))).pipe(nopts => SkillMultiOptions(nopts*))
	}

	def getSkillMultiOptions(): SkillMultiOptions = {
		if(!client.isClientThread)
			clientThread.runOnClientThread(() => getSkillMultiOptions())
		else {
			val r = for {
				button <- Skillmulti.A to Skillmulti.R map client.getWidget if button != null && !button.isSelfHidden
				parts = button.getChildren if parts != null
				part <- parts
				itemId = part.getItemId if itemId != -1 && itemId != 6512
			} yield SkillMultiOption(button.getId, itemId)
			SkillMultiOptions(r *)
		}
	}
}
