package com.fredplugins.kroovy

import com.fredplugins.common.MenuEntryShim
import ethanApiPlugin.collections.query.NPCQuery
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import net.runelite.api.Client
import net.runelite.api.ClientThreadInvoke
import net.runelite.api.Constants
import net.runelite.api.InventoryID
import net.runelite.api.Item
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.NPC
import net.runelite.api.Point
import net.runelite.api.Tile
import net.runelite.api.TileObject
import net.runelite.api.Varbits
import net.runelite.api.coords.WorldPoint
import net.runelite.api.widgets.WidgetInfo
import net.runelite.api.widgets.WidgetItem
import scala.Function0
import scala.Unit

import java.awt.Rectangle

abstract class GroovyContext
{
	abstract ClientThreadInvoke clientThread()
	abstract Client client()
	abstract ThreadSafeTrait threadSafe()

	abstract void hop(int i)
	abstract void doCloseNextMessageLayer()
	abstract void doSkipNextTabSwitch()

	abstract void queue(int delay, Function0<Unit> action)
	final void queue(int delay, @ClosureParams(value= SimpleType.class, options="") Closure<?> action) {
		queue(delay, new Function0<Unit>() {
			@Override Unit apply() {
				action.call()
				null
			}
		})
	}
	abstract void schedule(int delay, Function0<Unit> action)
	final void schedule(int delay, @ClosureParams(value= SimpleType.class, options="") Closure<?> action) {
		schedule(delay, new Function0<Unit>() {
			@Override Unit apply() {
				action.call()
				null
			}
		})
	}




	MenuEntryShim quickEntry(int opcode, int identifier, int param0, int param1)
	{
		new MenuEntryShim("", "", identifier, opcode, param0, param1, false);
	}

	int selectItem(int itemID, int slot = findItem(itemID)) {
		if (slot == -1) return -1
		client().setItemSelected(1)
		client().setSelectedItemWidget(WidgetInfo.INVENTORY.getId())
		client().setSelectedItemID(itemID)
		client().setSelectedItemSlot(slot)
		return slot
	}

//	abstract void sendK2Command(Address adr, String commandSrc, Closure onSuccessCallback)
//	abstract boolean sendK2Command(String target, String commandSrc)
//	abstract List<Address> getK2Clients()

	abstract void logMessage(String name, String message)
	abstract void logChat(String message)
	int random(int min, int max) {
		if(min < 0)
			min = 0
		if(max < 0)
			max = 0
		(Math.random() * (Math.max(min, max) - Math.min(min, max)) + Math.min(min, max))
	}

	Point inRect(Rectangle rect) {
		if(rect == null) {
			return null
		}
		int x = rect.getX() as int + random(0, rect.getWidth() as int)
		int y = rect.getY() as int + random(0, rect.getHeight() as int)
		return new Point(x, y)
	}

	int findItem(int itemID) {
		Item[] items = client().getItemContainer(InventoryID.INVENTORY)?.getItems()
		if(items == null) {
			return -1
		}
		for (i in items.indices) {
			int tempId = items[i]?.getId()?:-1
			if(itemID == tempId) {
				return i
			}
		}
		return -1
	}

	/**
	 * Find the nearest NPC to you based on name
	 * This will ignore NPCs at 0 health, useful for attacking again after killing stuff
	 */
	NPC findNearestNPC(String npcName) {
		return new NPCQuery().nameEquals(npcName).filter { NPC npc -> !npc.isDead() }.filter {it.getInteracting() == null}.result(client()).nearestTo(client().getLocalPlayer())
	}

	/**
	 * Find the nearest NPC to you based on id
	 * This will ignore NPCs at 0 health, useful for attacking again after killing stuff
	 */
	NPC findNearestNPC(int... npcIds) {
		return new NPCQuery().idEquals(npcIds).filter { NPC npc -> !npc.isDead() }.filter {it.getInteracting() == null}.result(client()).nearestTo(client().getLocalPlayer())
	}

	List<Tile> getTiles() {
		List<Tile> tilesList = new ArrayList<>()
		Tile[][][] tiles = client().getScene().getTiles()
		int z = client().getPlane()
		for (int x = 0; x < Constants.SCENE_SIZE; x++) {
			for (int y = 0; y <  Constants.SCENE_SIZE; y++) {
				if(tiles.length > z && tiles[z].length > x && tiles[z][x].length > y) {
					tilesList.add(tiles[z][x][y])
				}
			}
		}
		tilesList
	}
	TileObject findNearestObject(int... ids) {
		List<TileObject> gameObjects = new ArrayList<>()

		for (Tile tile in getTiles()) {
			if(tile != null) {
				if (tile.groundObject != null) gameObjects.add(tile.groundObject)
				for (gameObject in tile.gameObjects) if (gameObject != null) gameObjects.add(gameObject)
				if (tile.decorativeObject != null) gameObjects.add(tile.decorativeObject)
			}
		}
		List<TileObject> correctIdObjects = new ArrayList<>()

		for (gameObject in gameObjects) {
			if (ids.contains(gameObject.id)) {
				correctIdObjects.add(gameObject)
			}
		}
		WorldPoint localLoc = client().localPlayer?.worldLocation
		correctIdObjects.sort {a, b -> a.worldLocation.distanceTo(localLoc) <=> b.worldLocation.distanceTo(localLoc)}// { it.worldLocation.distanceTo(client.localPlayer?.worldLocation) }
		correctIdObjects.isEmpty() ? null : correctIdObjects.first()
	}

	List<Item> getInventoryItems() {
		client().getItemContainer(InventoryID.INVENTORY)?.items
	}

	boolean inventoryFull() {
		fullInventorySlots() == 28
	}

	int emptyInventorySlots() {
		(28 - fullInventorySlots())
	}

	int fullInventorySlots() {
		def items = getInventoryItems()
		if(items == null) {
			return -1
		}
		int fullSlots = 0
		for (item in items) if (item.id != -1) fullSlots++
		return fullSlots
	}

	int amountInInventory(int ... ids) {
		def items = getInventoryItems()
		if(items == null) return -1
		(items.findAll { ids.contains(it.id)}*.quantity as int[]).sum()
	}
}
