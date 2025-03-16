package com.fredplugins.kroovy
import com.fredplugins.common.queries.BankItemQuery
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import net.runelite.api.GameObject
import net.runelite.api.InventoryID
import net.runelite.api.Item
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.NPC
import net.runelite.api.Point
import net.runelite.api.TileObject
import net.runelite.api.Varbits
import net.runelite.api.widgets.Widget
import net.runelite.api.widgets.WidgetInfo
import net.runelite.api.widgets.WidgetItem

class GroovyApi {
	@Delegate(interfaces = false) final GroovyContext $context

	boolean doClick = true
	Closure<Integer> delayProvider = {random(4, 8)}

	GroovyApi(GroovyContext ctx)
	{
		this.$context = ctx
	}

	protected Point click(Point p = null) {
		(p ?:client().getMouseCanvasPosition())?.tap {if(doClick) {
			logMessage("Action Context", "Clicking point ${it.toString()}")
			threadSafe().sendClickPacket(it)
		} else { null } }
	}

	protected Closure<Void> doMenu(MenuEntry entry) {
		{ Point it ->
			threadSafe().invokeMenuAction(entry, it.x, it.y)
			logMessage("Action Context", "Entry ${entry.toString()} was invoked at ${it.toString()}")
		}
	}

	void dispatchAction(@ClosureParams(value=SimpleType.class, options="net.runelite.api.Point") Closure<Void> action,
								  @ClosureParams(value=SimpleType.class, options="") Closure<Point> pointProducer = {client().getMouseCanvasPosition()}) {
		queue(delayProvider.call(), {
				click(pointProducer?.call())?.tap {
					action.call(it)
					logMessage("Action Context", "Action called ${it}")
				}
			}
		)
	}

	void walkToPoint(int x, int y, boolean run) {
		dispatchAction { threadSafe().sendWalkPacket(x, y, run) }
	}

	/**
	 * Sends a click on a game widget
	 * @param widget The widget to click
	 * @param identifier The identifier to send with the click. This is usually something like the index of available options (withdraw-1, withdraw-5, withdraw-x, etc)
	 */
	void ccOp(Widget widget, int identifier = 1, int dynamicIdx = -1) {
		if(widget != null) dispatchAction(doMenu(quickEntry(MenuAction.CC_OP.getId(), identifier, dynamicIdx, widget.getId())), {inRect(widget.getBounds())})
	}

	void ccOp(WidgetInfo widgetInfo, int identifier = 1, int dynamicIdx = -1) {
		if(widgetInfo != null) ccOp(client().getWidget(widgetInfo), identifier, dynamicIdx)
	}

	boolean doNpcOption(String option, NPC npc) {
		String[] actions = (npc != null) ? threadSafe().getNpcDefinition(npc.getId())?.getActions() : null
		if(actions != null) {
			for(int i = 0; i < actions.length; i++) {
				if(actions[i]?.contentEquals(option)?:false) {
					dispatchAction(doMenu(quickEntry(MenuAction.values()[MenuAction.NPC_FIRST_OPTION.ordinal() + i].getId(), npc.getIndex(), 0, 0)), {inRect(npc?.getConvexHull()?.getBounds())})
					return true
				}
			}
		}
		return false
	}

	boolean doNpcOption(String option, String npcName) {
		doNpcOption(option, findNearestNPC(npcName))
	}

	boolean doNpcOption(String option, int... ids) {
		doNpcOption(option, findNearestNPC(ids))
	}

	boolean doGameObjectOption(String option, int... ids) {
		TileObject obj = findNearestObject(ids)
		String[] actions = (obj != null) ? threadSafe().getObjectDefinition(obj.getId())?.getActions() : null
		if(actions != null) {
			for(int i = 0; i < actions.length; i++) {
				if(actions[i]?.contentEquals(option)?:false) {
					return doGameObjectOption(i, obj)
				}
			}
		}
		return false
	}

	boolean doGameObjectOption(int opIdx, TileObject obj) {
		if(obj != null && opIdx >= 0 && opIdx < 5) {
			Point p = (obj instanceof GameObject) ? new Point(obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()) : new Point(obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY())
			dispatchAction(doMenu(quickEntry(MenuAction.values()[MenuAction.GAME_OBJECT_FIRST_OPTION.ordinal() + opIdx].getId(), obj.getId(), p.getX(), p.getY())), {inRect(obj.getClickbox()?.getBounds())})
			return true
		}
		return false
	}

	boolean hasInvItem(int ... ids) {
		WidgetItem[] invItems = this.getInventoryItems().findAll({Item it ->
			ids.contains(it.getId())
		}).toArray()
//		WidgetItem[] invItems = new InventoryWidgetItemQuery().idEquals(ids).result(client()).toList()
		Tuple2<WidgetItem, Integer> found = invItems.findResult(null) {WidgetItem it ->
			String[] temp = threadSafe().getItemDefinition(it.getId())?.getInventoryActions()
			if(temp != null) {
				for (int i = 0; i < temp.length; i++) {
					if (temp[i]?.contentEquals(option) ?: false) {
						return new Tuple2<WidgetItem, Integer>(it, i)
					}
				}
			}
			return null
		}
		if(found != null) {
			dispatchAction(doMenu(quickEntry(MenuAction.values()[MenuAction.ITEM_FIRST_OPTION.ordinal() + found.v2].getId(), found.v1.id, found.v1.index, WidgetInfo.INVENTORY.getId())), {
				inRect(found.v1?.getCanvasBounds())
			})
			return true
		}
		return false
	}
//
//	boolean doInvItemOption(String option, int... ids) {
//		WidgetItem[] invItems = new InventoryWidgetItemQuery().idEquals(ids).result(client()).toList()
//		Tuple2<WidgetItem, Integer> found = invItems.findResult(null) {WidgetItem it ->
//			String[] temp = threadSafe().getItemDefinition(it.getId())?.getInventoryActions()
//			if(temp != null) {
//				for (int i = 0; i < temp.length; i++) {
//					if (temp[i]?.contentEquals(option) ?: false) {
//						return new Tuple2<WidgetItem, Integer>(it, i)
//					}
//				}
//			}
//			return null
//		}
//		if(found != null) {
//			dispatchAction(doMenu(quickEntry(MenuAction.values()[MenuAction.ITEM_FIRST_OPTION.ordinal() + found.v2].getId(), found.v1.id, found.v1.index, WidgetInfo.INVENTORY.getId())), {
//				inRect(found.v1?.getCanvasBounds())
//			})
//			return true
//		}
//		return false
//	}

//	/**
//	 * Use one inventory item on another using the passed IDs
//	 */
//	boolean useItemOnItem(int sourceItemID, int slot = findItem(sourceItemID), int targetItemID) {
//		if (slot != -1 && selectItem(sourceItemID, slot) != -1) {
//			dispatchAction(doMenu(quickEntry(MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId(), targetItemID, slot, WidgetInfo.INVENTORY.getId())), {inRect((new InventoryWidgetItemQuery()).idEquals(sourceItemID).indexEquals(client().getSelectedItemSlot()).result(client()).first()?.getCanvasBounds())})
//			return true
//		}
//		return false
//	}

	boolean useItemOnGameObject(int sourceItemID, int slot = findItem(sourceItemID), TileObject obj) {
		Point sceneLoc = (obj instanceof GameObject) ? obj.getSceneMinLocation() : obj?.getLocalLocation()?.with {new Point(it.getSceneX(), it.getSceneY()) }
		if (sceneLoc != null) {
			if(selectItem(sourceItemID, slot) != -1) {
				dispatchAction(doMenu(quickEntry(MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), obj.getId(), sceneLoc.getX(), sceneLoc.getY())), {inRect(obj?.getClickbox()?.getBounds())})
				return true
			}
		}
		return false
	}

	boolean isBankOpen() {
		client().getItemContainer(InventoryID.BANK) != null
	}

	void closeBank() {
		if(isBankOpen()) {
			dispatchAction(doMenu(quickEntry(MenuAction.CC_OP.getId(), 1, 11, 786434)))
		}
	}

//	boolean deposit(int itemID, int quantity) {
//		def bankInventory = client().getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER)
//		def invQuantity = amountInInventory(itemID)
//		if (bankInventory == null || invQuantity <= 0) return false
//		WidgetItem[] rawKaramArr = new InventoryWidgetItemQuery().idEquals(itemID).result(client()).toArray() as WidgetItem[]
//		WidgetItem rawKaram = rawKaramArr[random(0, rawKaramArr.length)]
//		if(rawKaram != null) {
//			if(quantity >= invQuantity) {
//				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP_LOW_PRIORITY.getId(), 8, rawKaram.getIndex(), bankInventory.getId())), {inRect(rawKaram.getCanvasBounds())})
//			} else if(quantity == threadSafe().getVar(Varbits.WITHDRAW_X_AMOUNT)) {
//				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP_LOW_PRIORITY.getId(), 6, rawKaram.getIndex(), bankInventory.getId())), {inRect(rawKaram.getCanvasBounds())})
//			} else if(quantity == 1) {
//				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP.getId(), 2, rawKaram.getIndex(), bankInventory.getId())), {inRect(rawKaram.getCanvasBounds())})
//			} else if(quantity == 5) {
//				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP.getId(), 4, rawKaram.getIndex(), bankInventory.getId())), {inRect(rawKaram.getCanvasBounds())})
//			} else if(quantity == 10) {
//				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP.getId(), 5, rawKaram.getIndex(), bankInventory.getId())), {inRect(rawKaram.getCanvasBounds())})
//			} else if(quantity >= 1 && quantity <= Integer.MAX_VALUE) {
//				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP_LOW_PRIORITY.getId(), 7, rawKaram.getIndex(), bankInventory.getId())), {inRect(rawKaram.getCanvasBounds())})
//				queue(delayProvider.call(), {
//					threadSafe().sendResumeDialogCountPacket(quantity)
//					doCloseNextMessageLayer()
//				})
//			} else {
//				return false
//			}
//			return true
//		}
//		return false
//	}

	boolean withdraw(int itemID, int quantity) {
		def bankInventory = client().getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER)
//		def invQuantity = amountInInventory(itemID)
		if (bankInventory == null) return false
		WidgetItem[] rawKaramArr = new BankItemQuery().idEquals(itemID).result(client()).toArray() as WidgetItem[]
		WidgetItem rawKaram = rawKaramArr[random(0, rawKaramArr.length)]
		if(rawKaram != null) {
			if(quantity == threadSafe().getVar(Varbits.WITHDRAW_X_AMOUNT)) {
				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP_LOW_PRIORITY.getId(), 5, rawKaram.getWidget().getIndex(),  rawKaram.getWidget().getId())), {inRect(rawKaram.getCanvasBounds())})
			} else if(quantity == 1) {
				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP.getId(), 2, rawKaram.getWidget().getIndex(), rawKaram.getWidget().getId())), {inRect(rawKaram.getCanvasBounds())})
			} else if(quantity == 5) {
				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP.getId(), 3, rawKaram.getWidget().getIndex(),  rawKaram.getWidget().getId())), {inRect(rawKaram.getCanvasBounds())})
			} else if(quantity == 10) {
				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP.getId(), 4, rawKaram.getWidget().getIndex(),  rawKaram.getWidget().getId())), {inRect(rawKaram.getCanvasBounds())})
			} else if(quantity >= 1 && quantity <= Integer.MAX_VALUE) {
				dispatchAction(doMenu(quickEntry(MenuAction.CC_OP_LOW_PRIORITY.getId(), 6, rawKaram.getWidget().getIndex(),  rawKaram.getWidget().getId())), {inRect(rawKaram.getCanvasBounds())})
				queue(delayProvider.call(), {
					threadSafe().sendResumeDialogCountPacket(quantity)
					doCloseNextMessageLayer()
				})
			} else {
				return false
			}
			return true
		}
		return false
	}


}
