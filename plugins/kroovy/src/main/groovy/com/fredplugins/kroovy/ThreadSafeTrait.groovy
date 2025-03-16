package com.fredplugins.kroovy

import com.fredplugins.common.MenuEntryShim
import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.ItemComposition
import net.runelite.api.MenuEntry
import net.runelite.api.NPCComposition
import net.runelite.api.ObjectComposition
import net.runelite.api.Point
import net.runelite.api.Prayer
import net.runelite.api.VarPlayer
import net.runelite.api.VarbitComposition
import net.runelite.api.Varbits
import net.runelite.api.widgets.Widget
import net.runelite.api.SpritePixels
import net.runelite.client.callback.ClientThread

import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

abstract class ThreadSafeTrait
{
	abstract Client client()
	abstract ClientThread clientThread()

	def addChatMessage(ChatMessageType messageType, String name, String message, String sender)
	{
		clientThread().invoke{ client().addChatMessage(messageType, name, message, sender) }
	}
	def setGameState(GameState gameState) { clientThread().invoke{client().setGameState(gameState) }}
	def setRSGameState(int gameState) { clientThread().invoke{ client().setGameState(GameState.of(gameState))} }

	SpritePixels createItemSprite(int itemID, int quantity, int border, int shadowColor, int stackable, boolean noted, int scale)
	{
		SpritePixels sprite= null
		FutureTask<Boolean> task = new FutureTask({
			sprite = client().createItemSprite(itemID, quantity, border, shadowColor, stackable, noted, scale)
			true})
		clientThread().invoke(task)
		try {
			task.get(500, TimeUnit.MILLISECONDS)
		}
		catch(Exception exception) {
			exception.printStackTrace()
		}
		return sprite
	}
	def sendPacket(int packetId, List<Byte> byteList)
	{
		clientThread().invoke{client().getPacketWriter().sendPacket(packetId,
			{buffer -> byteList.forEach{buffer.writeByte(it) }})}
	}

	def sendClickPacket() {
		sendClickPacket(0, client().getMouseCanvasPosition().getX(), client().getMouseCanvasPosition().getY())
	}
	def sendClickPacket(int mouseButton, int x, int y) {
		clientThread().invoke{client().getPacketWriter().sendClickPacket(mouseButton, x, y)}
	}
	def sendClickPacket(Point p) {
		if(p != null) sendClickPacket(0, p.x, p.y)
	}
	def sendResumeDialogCountPacket(int count) {
		clientThread().invoke{client().getPacketWriter().sendResumeDialogCountPacket(count)}
	}

	def sendWalkPacket(int worldX, int worldY, boolean run) {
		clientThread().invoke{
			client().showMouseCross(1)
			client().getPacketWriter().sendWalkPacket(worldX, worldY, run)
		}
	}
	def sendDialogPacket(int widgetId, int arg)
	{
		clientThread().invoke{client().getPacketWriter().sendDialogPacket(widgetId, arg)}
	}

	//TODO: Implement this
//	abstract void spoofFocus(boolean shouldFocus)
//	def pressKey(int keycode, int rsKey) {
//		clientThread().invoke{
//			if(!client().isKeyPressed(rsKey)) {
//				def e = new KeyEvent(client().getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keycode, KeyEvent.CHAR_UNDEFINED)
//				client().getCanvas().dispatchEvent(e)
//			}
//		}
//	}
//
//	def releaseKey(int keycode, int rsKey) {
//		clientThread().invoke {
//			if(client().isKeyPressed(rsKey)) {
//				def e = new KeyEvent(client().getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keycode, KeyEvent.CHAR_UNDEFINED)
//				client().getCanvas().dispatchEvent(e)
//				e = new KeyEvent(client().getCanvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, keycode, KeyEvent.CHAR_UNDEFINED)
//				client().getCanvas().dispatchEvent(e)
//			}
//		}
//	}

	def invokeMenuAction(MenuEntry entry, int x, int y) {
		clientThread().invoke{
			client().invokeMenuAction(entry.option, entry.target, entry.identifier, entry.type.getId(), entry.param0, entry.param1, x, y)
		}
	}
	def invokeMenuAction(MenuEntryShim entry, int x, int y) {
		clientThread().invoke{
			client().invokeMenuAction(entry.option, entry.target, entry.identifier, entry.opcode, entry.param1, entry.param2, x, y)
		}
	}
	def doAction(int opcode, int id, int param0, int param1, int x, int y) {
		clientThread().invoke{client().invokeMenuAction("", "", id, opcode, param0, param1, x, y)}
	}
	def sendInterfaceClosePacket() {
		clientThread().invoke{client().getPacketWriter().sendInterfaceClosePacket()}
	}

	int getVar(VarPlayer varPlayer) {
		client().getVar(varPlayer)
	}
	int getVar(Varbits varbit) {
		getVarbitValue(varbit.getId())
	}
	int getVarClientInt(int id) {
		int sprite=-1
		FutureTask<Boolean> task = new FutureTask({
			sprite = client().getVarcIntValue(id)
			true})
		clientThread().invoke(task)
		try {
			task.get(500, TimeUnit.MILLISECONDS)
		}
		catch(Exception exception) {
			exception.printStackTrace()
		}
		return sprite
	}
	abstract int getVarbitValue(int varbitId)
	abstract NPCComposition getNpcDefinition(int npcId)
	abstract ItemComposition getItemDefinition(int itemId)
	abstract ObjectComposition getObjectDefinition(int objectId)
	abstract VarbitComposition getVarbitDefinition(int varbitId)

	boolean isPrayerActive(Prayer prayer)
	{
		getVar(prayer.getVarbit()) == 1
	}
	Set<Prayer> getActivePrayers () { // can be moved out
		Prayer.values().findAll {getVar(it.getVarbit()) == 1}
	}
	//TODO: Implement
	abstract boolean isWidgetHidden(Widget widget)
}