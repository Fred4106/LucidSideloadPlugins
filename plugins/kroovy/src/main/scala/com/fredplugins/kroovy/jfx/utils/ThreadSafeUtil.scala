package com.fredplugins.kroovy.jfx.utils

import com.fredplugins.kroovy.ThreadSafeTrait

import java.util.concurrent.{FutureTask, TimeUnit}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.widgets.Widget
import net.runelite.api.{ChatMessageType, Client, GameState, ItemComposition, NPCComposition, ObjectComposition, PacketBuffer, Prayer, SpritePixels, VarPlayer, VarbitComposition, Varbits}
import net.runelite.client.callback.ClientThread
import scalaz.Memo
//import scalaz.Memo

@Singleton
class ThreadSafeUtil extends  com.fredplugins.kroovy.ThreadSafeTrait() {

	@Inject override val client: Client = null
	@Inject override val clientThread: ClientThread = null
	val memoNpcDefinition: Int => NPCComposition = Memo.mutableHashMapMemo { npcId =>
		var npcDefinition: NPCComposition = null
		val task = new FutureTask(() => npcDefinition = client.getNpcDefinition(npcId))
		clientThread.invoke(task)
		try { task.get(500, TimeUnit.MILLISECONDS) }
		catch { case exception: Exception => exception.printStackTrace() }
		npcDefinition
	}

	val memoItemDefinition: Int => ItemComposition = Memo.mutableHashMapMemo { itemId =>
		var itemDefinition: ItemComposition = null
		val task = new FutureTask(() => itemDefinition = client.getItemDefinition(itemId))
		clientThread.invoke(task)
		try { task.get(500, TimeUnit.MILLISECONDS) }
		catch { case exception: Exception => exception.printStackTrace() }
		itemDefinition
	}

	val memoObjectDefinition: Int => ObjectComposition = Memo.mutableHashMapMemo { objectId =>
		var objectDefinition: ObjectComposition = null
		val task = new FutureTask(() => objectDefinition = client.getObjectDefinition(objectId))
		clientThread.invoke(task)
		try { task.get(500, TimeUnit.MILLISECONDS) }
		catch { case exception: Exception => exception.printStackTrace() }
		objectDefinition
	}

	val memoVarbitDefinition: Int => VarbitComposition = Memo.mutableHashMapMemo { varbitId =>
		var varDef: VarbitComposition = null
		val task = new FutureTask(() => varDef = client.getVarbit(varbitId))
		clientThread.invoke(task)
		try { task.get(500, TimeUnit.MILLISECONDS) }
		catch { case exception: Exception => exception.printStackTrace() }
		varDef
	}
	override def getVarbitValue(varbitId: Int): Int = {
		val varbitDefinition: VarbitComposition = memoVarbitDefinition(varbitId)
		if (varbitDefinition == null) return 0
		def mask(varbitDefinition: VarbitComposition): Int = {
			(1 << ((varbitDefinition.getMostSignificantBit - varbitDefinition.getLeastSignificantBit) + 1)) - 1
		}
		Option(client.getVarps) match {
			case None => 0
			case Some(varps) => (varps(varbitDefinition.getIndex) >> varbitDefinition.getLeastSignificantBit) & mask(varbitDefinition)
		}
	}

	def getNpcDefinition(npcId: Int): NPCComposition = memoNpcDefinition(npcId)
	def getItemDefinition(itemId: Int): ItemComposition = memoItemDefinition(itemId)
	def getObjectDefinition(objectId: Int): ObjectComposition = memoObjectDefinition(objectId)
	def getVarbitDefinition(varbitId: Int): VarbitComposition = memoVarbitDefinition(varbitId)
	override def isWidgetHidden(widget: Widget): Boolean = Option(widget).exists(_.isHidden)

//	override def spoofFocus(shouldFocus: Boolean): Unit = {
//		if (shouldFocus&& !client.hasFocus) {
//			client.setVolatileFocus(true)
//			if (!client.hadFocus) {
//				client.setHadFocus(true)
//				client.getPacketWriter.sendFocusPacket(true)
//			}
//		}
//		else if (!shouldFocus && client.hasFocus) {
//			client.setVolatileFocus(false)
//			if (client.hadFocus) {
//				client.setHadFocus(false)
//				client.getPacketWriter.sendFocusPacket(false)
//			}
//		}
//	}
}
