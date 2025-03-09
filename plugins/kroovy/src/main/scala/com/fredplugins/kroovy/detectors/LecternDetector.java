package com.fredplugins.kroovy.detectors;

import com.fredplugins.kroovy.ActionEnum;
//import com.fredplugins.kroovy.data.Magic;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@Slf4j
public class LecternDetector extends ActionDetector
{
	static {
		((ch.qos.logback.classic.Logger) log).setLevel(ch.qos.logback.classic.Level.INFO);
	}

	private static final int VAR_MAKE_AMOUNT = 2224;

	private static final int WIDGET_LECTERN = 26411019;

	@Inject private Client client;

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked evt)
	{
		if (evt.getParam1() == -1 || evt.getMenuAction() != MenuAction.CC_OP) {
			return;
		}
		Widget widget = this.client.getWidget(evt.getParam1());
		if (widget == null || widget.getParentId() != WIDGET_LECTERN) {
			return;
		}
		int amount = this.client.getVarpValue(VAR_MAKE_AMOUNT);
//		for (Magic.LecternSpell item : Magic.LecternSpell.values()) {
//			if (evt.getMenuOption().toLowerCase().contains(item.getItemText().toLowerCase())) {
//				this.actionManager.setAction(ActionEnum.MAGIC_CREATE_TABLET, amount, item.getProduct());
//				break;
//			}
//		}
	}

	@Override
	public void setup() {

	}

	@Override
	public void shutDown() {

	}
}
