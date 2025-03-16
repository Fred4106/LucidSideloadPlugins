package com.fredplugins.common;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;

public class MenuEntryShim
{
	public 	final String option;
	public final String target;
	public final int identifier;
	public final int opcode;
	public final int param1;
	public final int param2;
	public final boolean forceLeftClick;

	public MenuEntryShim(String option, String target, int identifier, int opcode, int param1, int param2, boolean forceLeftClick)
	{
		this.option = option;
		this.target = target;
		this.identifier = identifier;
		this.opcode = opcode;
		this.param1 = param1;
		this.param2 = param2;
		this.forceLeftClick = forceLeftClick;
	}
	public MenuEntryShim(String option, String target, int identifier, MenuAction action, int param1, int param2, boolean forceLeftClick)
	{
		this.option = option;
		this.target = target;
		this.identifier = identifier;
		this.opcode = action.getId();
		this.param1 = param1;
		this.param2 = param2;
		this.forceLeftClick = forceLeftClick;
	}
	@Override
	public String toString()
	{
		return "MenuEntryShim{" +
				"option='" + option + '\'' +
				", target='" + target + '\'' +
				", identifier=" + identifier +
				", opcode=" + getType().name() +
				", param1=" + param1 +
				", param2=" + param2 +
				", forceLeftClick=" + forceLeftClick +
				'}';
	}

	public MenuEntryShim setOption(String nOption)
	{
		return new MenuEntryShim(nOption, target, identifier, opcode, param1, param2, forceLeftClick);
	}
	public MenuEntryShim setTarget(String nTarget)
	{
		return new MenuEntryShim(option, nTarget, identifier, opcode, param1, param2, forceLeftClick);
	}
	public MenuEntryShim setIdentifier(int modifiedId)
	{
		return new MenuEntryShim(option, target, modifiedId, opcode, param1, param2, forceLeftClick);
	}
	public MenuEntryShim setParams(int a, int b)
	{
		return new MenuEntryShim(option, target, identifier, opcode, a, b, forceLeftClick);
	}
	public MenuEntryShim setParam1(int slot)
	{
		return new MenuEntryShim(option, target, identifier, opcode, slot, param2, forceLeftClick);
	}
	public MenuEntryShim setParam2(int slot)
	{
		return new MenuEntryShim(option, target, identifier, opcode, param1, slot, forceLeftClick);
	}

	public MenuAction getType()
	{
		return MenuAction.of(opcode);
	}

	public boolean equalsClicked(MenuOptionClicked event) {
		return (event.getId() == this.identifier && this.param1 == event.getParam0() && this.param2 == event.getParam1() && this.getType() == event.getMenuAction() && this.option.equals(event.getMenuOption()));
	}
}