package interactionApi;

import ethanApiPlugin.EthanApiPlugin;
import net.runelite.api.widgets.Widget;
import packetUtils.WidgetInfoExtended;
import packets.MousePackets;
import packets.WidgetPackets;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.client.RuneLite;

import java.awt.*;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

public class PrayerInteraction {
    public static final HashMap<Prayer, WidgetInfoExtended> prayerMap = new HashMap<Prayer, WidgetInfoExtended>();
    static{
        prayerMap.put(Prayer.AUGURY,WidgetInfoExtended.PRAYER_AUGURY);
        prayerMap.put(Prayer.BURST_OF_STRENGTH,WidgetInfoExtended.PRAYER_BURST_OF_STRENGTH);
        prayerMap.put(Prayer.CHIVALRY,WidgetInfoExtended.PRAYER_CHIVALRY);
        prayerMap.put(Prayer.CLARITY_OF_THOUGHT,WidgetInfoExtended.PRAYER_CLARITY_OF_THOUGHT);
        prayerMap.put(Prayer.EAGLE_EYE,WidgetInfoExtended.PRAYER_EAGLE_EYE);
        prayerMap.put(Prayer.HAWK_EYE,WidgetInfoExtended.PRAYER_HAWK_EYE);
        prayerMap.put(Prayer.IMPROVED_REFLEXES,WidgetInfoExtended.PRAYER_IMPROVED_REFLEXES);
        prayerMap.put(Prayer.INCREDIBLE_REFLEXES,WidgetInfoExtended.PRAYER_INCREDIBLE_REFLEXES);
        prayerMap.put(Prayer.MYSTIC_MIGHT,WidgetInfoExtended.PRAYER_MYSTIC_MIGHT);
        prayerMap.put(Prayer.PIETY,WidgetInfoExtended.PRAYER_PIETY);
        prayerMap.put(Prayer.PRESERVE,WidgetInfoExtended.PRAYER_PRESERVE);
        prayerMap.put(Prayer.PROTECT_FROM_MAGIC,WidgetInfoExtended.PRAYER_PROTECT_FROM_MAGIC);
        prayerMap.put(Prayer.PROTECT_FROM_MELEE,WidgetInfoExtended.PRAYER_PROTECT_FROM_MELEE);
        prayerMap.put(Prayer.PROTECT_FROM_MISSILES,WidgetInfoExtended.PRAYER_PROTECT_FROM_MISSILES);
        prayerMap.put(Prayer.RETRIBUTION,WidgetInfoExtended.PRAYER_RETRIBUTION);
        prayerMap.put(Prayer.RIGOUR,WidgetInfoExtended.PRAYER_RIGOUR);
        prayerMap.put(Prayer.ROCK_SKIN,WidgetInfoExtended.PRAYER_ROCK_SKIN);
        prayerMap.put(Prayer.SHARP_EYE,WidgetInfoExtended.PRAYER_SHARP_EYE);
        prayerMap.put(Prayer.SMITE,WidgetInfoExtended.PRAYER_SMITE);
        prayerMap.put(Prayer.STEEL_SKIN,WidgetInfoExtended.PRAYER_STEEL_SKIN);
        prayerMap.put(Prayer.THICK_SKIN,WidgetInfoExtended.PRAYER_THICK_SKIN);
        prayerMap.put(Prayer.ULTIMATE_STRENGTH,WidgetInfoExtended.PRAYER_ULTIMATE_STRENGTH);
        prayerMap.put(Prayer.REDEMPTION,WidgetInfoExtended.PRAYER_REDEMPTION);
        prayerMap.put(Prayer.RAPID_RESTORE,WidgetInfoExtended.PRAYER_RAPID_RESTORE);
        prayerMap.put(Prayer.RAPID_HEAL,WidgetInfoExtended.PRAYER_RAPID_HEAL);
        prayerMap.put(Prayer.PROTECT_ITEM,WidgetInfoExtended.PRAYER_PROTECT_ITEM);
        prayerMap.put(Prayer.MYSTIC_LORE,WidgetInfoExtended.PRAYER_MYSTIC_LORE);
        prayerMap.put(Prayer.SUPERHUMAN_STRENGTH,WidgetInfoExtended.PRAYER_SUPERHUMAN_STRENGTH);
        prayerMap.put(Prayer.MYSTIC_WILL,WidgetInfoExtended.PRAYER_MYSTIC_WILL);
    }
    static Client client = RuneLite.getInjector().getInstance(Client.class);
    
    public static void togglePrayer(Prayer a){
        if(EthanApiPlugin.getClient().getVarbitValue(a.getVarbit())==0){
            EthanApiPlugin.getClient().setVarbit(a.getVarbit(),1);
        }else{
            EthanApiPlugin.getClient().setVarbit(a.getVarbit(),0);
        }
        WidgetInfoExtended prayerWidgetExtended = prayerMap.get(a);
        Rectangle bounds = Optional.ofNullable(client.getWidget(prayerWidgetExtended.getId())).map(Widget::getBounds).orElse(null);
        if(bounds != null) {
            Random r = new Random();
            Point center = new Point((int) bounds.getCenterX(), (int) bounds.getCenterY());
            int x = -1;
            int y = -1;
            int timeout = 15;
            while(!bounds.contains(x, y) || timeout > 0) {
                x = (int) (center.x + r.nextInt((int) bounds.getWidth()) - (bounds.getWidth() / 2));
                y = (int) (center.y + r.nextInt((int) bounds.getHeight()) - (bounds.getHeight() / 2));
                timeout--;
            }
            System.out.println(bounds);
            if(timeout > 0) {
                MousePackets.queueClickPacket(x, y);
            } else {
                MousePackets.queueClickPacket();
            }
        } else {
            MousePackets.queueClickPacket();
        }

        WidgetPackets.queueWidgetActionPacket(1, prayerWidgetExtended.getPackedId(), -1,-1);
    }
    public static void setPrayerState(Prayer prayer,boolean on){
        if(EthanApiPlugin.getClient().isPrayerActive(prayer) != on){
            togglePrayer(prayer);
        }
    }
    public static void flickPrayers(Prayer... prayers){
        prayerMap.forEach((prayer, widgetInfoExtended) -> {
            setPrayerState(prayer,false);
        });
        for(Prayer prayer : prayers){
            setPrayerState(prayer,true);
        }
    }
}
