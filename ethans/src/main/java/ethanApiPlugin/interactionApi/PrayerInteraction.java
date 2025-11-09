package ethanApiPlugin.interactionApi;

import com.fredplugins.common.PrayerExtended;
import ethanApiPlugin.EthanApiPlugin;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InterfaceID.Quickprayer;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import packetUtils.WidgetInfoExtended;
import packets.MousePackets;
import packets.WidgetPackets;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.client.RuneLite;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

public class PrayerInteraction {
	static Client client = RuneLite.getInjector().getInstance(Client.class);

	public static void togglePrayer(Prayer a){
		Widget prayerWidget = Optional.ofNullable(a).map(aa -> PrayerExtended.getWidgetId(aa)).map(aa -> client.getWidget(aa)).orElse(null);
		if(prayerWidget != null) {
			boolean value = EthanApiPlugin.getClient().getVarbitValue(a.getVarbit()) == 0;
			EthanApiPlugin.getClient().setVarbit(a.getVarbit(), (value?1:0));
			MousePackets.queueClickPacket(prayerWidget);
			WidgetPackets.queueWidgetActionPacket(1, prayerWidget.getId(), -1,-1);
		}
	}

	public static void setPrayerState(Prayer prayer,boolean on){
		if(EthanApiPlugin.getClient().isPrayerActive(prayer) != on){
			togglePrayer(prayer);
		}
	}
	
	public static boolean isActive(Prayer p) {
		return EthanApiPlugin.getClient().isPrayerActive(p);
	}
	public static void flickPrayers(Prayer... prayers){
		Arrays.stream(Prayer.values()).forEach((prayer) -> {
			setPrayerState(prayer,false);
		});
		for(Prayer prayer : prayers){
			setPrayerState(prayer,true);
		}
	}
//
//
//	public static boolean checkIsQuickPraySet(Prayer prayer) {
//		int idx = PrayerExtended.getQuickPrayerIndex(prayer);
//        return (client.getVarbitValue(VarbitID.QUICKPRAYER_SELECTED) & (int) Math.pow(2, idx)) == Math.pow(2, idx);
//	}
//
//	public static void addToQuickPraySet(Prayer prayer) {
//		if(client.getWidget(InterfaceID.Quickprayer.BUTTONS) == null) {
//			Widget qpOrb = client.getWidget(InterfaceID.Orbs.PRAYERBUTTON);
//			if(qpOrb != null) {
//				MousePackets.queueClickPacket(qpOrb);
//				WidgetPackets.queueWidgetAction(qpOrb, "Setup");
//				forceTab = true
//			})
//		}
//		
//		Widget quickPrayersContainer = client.getWidget(Quickprayer.BUTTONS)
//		Widget prayerWidget = Optional.ofNullable(prayer).map(aa -> PrayerExtended.getWidgetId(aa)).map(aa -> client.getWidget(aa)).orElse(null);
//		if(prayerWidget != null) {
//			boolean value = EthanApiPlugin.getClient().getVarbitValue(a.getVarbit()) == 0;
//			EthanApiPlugin.getClient().setVarbit(a.getVarbit(), (value?1:0));
//			MousePackets.queueClickPacket(prayerWidget);
//			WidgetPackets.queueWidgetActionPacket(1, prayerWidget.getId(), -1,-1);
//		}
//		if(!checkIsQuickPraySet(prayer)) {
//			WidgetPackets.queueWidgetActionPacket(
//			MousePackets.queueClickPacket(
//			WidgetPackets.queueWidgetActionPacket(1, InterfaceID.Quickprayer.BUTTONS, -1,
//		}
//	}
//	public static void removeFromQuickPraySet(Prayer prayer) {
//		if(checkIsQuickPraySet(prayer)) {
//			WidgetPackets.queueWidgetActionPacket(1, InterfaceID.Quickprayer.BUTTONS, -1,
//		}
//	}
}
