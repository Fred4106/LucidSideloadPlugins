package ethanApiPlugin.lucidplugins.api.utils;

import ethanApiPlugin.collections.Equipment;
import ethanApiPlugin.collections.Inventory;
import ethanApiPlugin.interactionApi.PrayerInteraction;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import packetUtils.WidgetInfoExtended;
import packets.MousePackets;
import packets.WidgetPackets;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CombatUtils
{

    public static final int RIGOUR_UNLOCKED = 5451;
    public static final int AUGURY_UNLOCKED = 5452;
    public static final int CAMELOT_TRAINING_ROOM_STATUS = 3909;
    static Client client = RuneLite.getInjector().getInstance(Client.class);
    static ClientThread clientThread = RuneLite.getInjector().getInstance(ClientThread.class);

    public static Prayer prayerForName(String name)
    {
        String p = name.toUpperCase().replaceAll(" ", "_");
        for (Prayer prayer : Prayer.values())
        {
            if (prayer.name().equalsIgnoreCase(p))
            {
                return prayer;
            }
        }
        return null;
    }

    public static Skill skillForName(String name)
    {
        for (Skill skill : Skill.values())
        {
            if (skill.name().equalsIgnoreCase(name))
            {
                return skill;
            }
        }
        return null;
    }

    public static int getPrayerPoints() {
        final int prayerLevel = client.getRealSkillLevel(Skill.PRAYER);
        return client.getBoostedSkillLevel(Skill.PRAYER);
    }
    public static int getPrayerPointsMissing() {
        int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
        return maxPrayer - currentPrayer;
    }
    public static int getRestoreAmount(Widget w) {
        return Optional.ofNullable(w).map(Widget::getItemId).map(CombatUtils::getRestoreAmount).orElse(0);
    }
    public static int getRestoreAmount(int itemId) {
        if(itemId == -1) return 0;
        final int prayerLevel = client.getRealSkillLevel(Skill.PRAYER);
        boolean hasWrench = !Inventory.search().withId(ItemID.SKILLCAPE_PRAYER, ItemID.SKILLCAPE_PRAYER_TRIMMED, ItemID.SKILLCAPE_MAX, ItemID.SKILLCAPE_MAX_WORN, ItemID.DEAL_WRENCH_BLESSED, ItemID.NZONE_ROTG).result().isEmpty() || !Equipment.search().withId(ItemID.SKILLCAPE_PRAYER, ItemID.SKILLCAPE_PRAYER_TRIMMED, ItemID.SKILLCAPE_MAX, ItemID.SKILLCAPE_MAX_WORN, ItemID.DEAL_WRENCH_BLESSED, ItemID.NZONE_ROTG).result().isEmpty();

        if(List.of(ItemID.SANFEW_SALVE_4_DOSE, ItemID.SANFEW_SALVE_3_DOSE, ItemID.SANFEW_SALVE_2_DOSE, ItemID.SANFEW_SALVE_1_DOSE).contains(itemId)) {
            return 4 + (int) Math.floor(prayerLevel *  (hasWrench ? .32 : .30));
        } else if(List.of(
            ItemID._4DOSE2RESTORE, ItemID._3DOSE2RESTORE, ItemID._2DOSE2RESTORE, ItemID._1DOSE2RESTORE,
            ItemID.BLIGHTED_4DOSE2RESTORE, ItemID.BLIGHTED_3DOSE2RESTORE, ItemID.BLIGHTED_2DOSE2RESTORE,
            ItemID.BLIGHTED_1DOSE2RESTORE)
            .contains(itemId)) {
            return 8 + (int) Math.floor(prayerLevel *  (hasWrench ? .27 : .25));
        } else if (List.of(
            ItemID._4DOSEPRAYERRESTORE, ItemID._3DOSEPRAYERRESTORE, ItemID._2DOSEPRAYERRESTORE, ItemID._1DOSEPRAYERRESTORE, ItemID.GAUNTLET_POTION_4, ItemID.GAUNTLET_POTION_3, ItemID.GAUNTLET_POTION_2, ItemID.GAUNTLET_POTION_1)
            .contains(itemId)) {
            return 7 + (int) Math.floor(prayerLevel *  (hasWrench ? .27 : .25));
        } else {
            return 0;
        }
    }

    public static void activatePrayer(Prayer prayer)
    {
        if (client.getBoostedSkillLevel(Skill.HITPOINTS) == 0)
        {
            return;
        }

        if (client.getBoostedSkillLevel(Skill.PRAYER) == 0 || checkPrayer(prayer) == null)
        {
            return;
        }
        
        if (!client.isPrayerActive(checkPrayer(prayer)))
        {
            PrayerInteraction.togglePrayer(checkPrayer(prayer));
        }
    }
    
    public static boolean isActive(Prayer p)
    {
        return client.isPrayerActive(p);
    }

	public static void activatePrayers(Prayer ... prayers)
	{
		if (client.getBoostedSkillLevel(Skill.PRAYER) == 0)
		{
			return;
		}

		for (Prayer prayer : prayers)
		{
			if (!client.isPrayerActive(prayer))
			{
				PrayerInteraction.togglePrayer(prayer);
			}
		}
	}
    public static void deactivatePrayer(Prayer prayer)
    {
        if (client == null || checkPrayer(prayer) == null || client.getBoostedSkillLevel(Skill.PRAYER) == 0 || !client.isPrayerActive(checkPrayer(prayer)))
        {
            return;
        }

        PrayerInteraction.togglePrayer(checkPrayer(prayer));
    }

    public static List<Prayer> getActivePrayers() {
        return clientThread.runOnClientThread(() ->
            Arrays.stream(Prayer.values()).filter(p -> client.getVarbitValue(p.getVarbit()) == 1).collect(Collectors.toList()));
    }

    public static void deactivatePrayers(boolean protectionOnly)
    {
        if (client.getBoostedSkillLevel(Skill.PRAYER) == 0)
        {
            return;
        }

        if (protectionOnly)
        {
            Prayer overhead = getActiveOverhead();

            if (overhead != null)
            {
                PrayerInteraction.togglePrayer(overhead);
            }
        }
        else
        {
            for (Prayer prayer : Prayer.values())
            {
                if (client.isPrayerActive(prayer))
                {
                    PrayerInteraction.togglePrayer(prayer);
                }
            }
        }
    }
	public static void deactivatePrayers(Prayer ... prayers)
	{
		if (client.getBoostedSkillLevel(Skill.PRAYER) == 0)
		{
			return;
		}

		for (Prayer prayer : prayers)
		{
			if (client.isPrayerActive(prayer))
			{
				PrayerInteraction.togglePrayer(prayer);
			}
		}
	}
    public static void togglePrayer(Prayer prayer)
    {
        if (client.getBoostedSkillLevel(Skill.HITPOINTS) == 0)
        {
            return;
        }

        if (checkPrayer(prayer) == null)
        {
            return;
        }

        if (client.getBoostedSkillLevel(Skill.PRAYER) == 0 && !client.isPrayerActive(checkPrayer(prayer)))
        {
            return;
        }

        PrayerInteraction.togglePrayer(checkPrayer(prayer));
    }


    public static Prayer checkPrayer(Prayer prayer)
    {
        switch (prayer)
        {
            case AUGURY:
                if (client.getVarbitValue(AUGURY_UNLOCKED) != 1 || client.getRealSkillLevel(Skill.PRAYER) < 77)
                {
                    return Prayer.MYSTIC_MIGHT;
                }
                return Prayer.AUGURY;
            case RIGOUR:
                if (client.getVarbitValue(RIGOUR_UNLOCKED) != 1 || client.getRealSkillLevel(Skill.PRAYER) < 74)
                {
                    return Prayer.EAGLE_EYE;
                }
                return Prayer.RIGOUR;
            case PIETY:
                if (client.getVarbitValue(CAMELOT_TRAINING_ROOM_STATUS) < 8)
                {
                    return Prayer.SUPERHUMAN_STRENGTH;
                }
                if (client.getRealSkillLevel(Skill.PRAYER) < 70)
                {
                    return Prayer.CHIVALRY;
                }
                return Prayer.PIETY;
        }

        return prayer;
    }

    public static void toggleQuickPrayers()
    {
        if (client == null || (client.getBoostedSkillLevel(Skill.PRAYER) == 0 && !isQuickPrayersEnabled()))
        {
            return;
        }

        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId(), -1, -1);
    }

    public static void activateQuickPrayers()
    {
        if (client == null || (client.getBoostedSkillLevel(Skill.PRAYER) == 0 && !isQuickPrayersEnabled()))
        {
            return;
        }

        if (!isQuickPrayersEnabled())
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId(), -1, -1);
        }
    }

    public static Prayer getActiveOverhead()
    {
        final Prayer[] overheads = new Prayer[] {Prayer.PROTECT_FROM_MELEE, Prayer.PROTECT_FROM_MISSILES, Prayer.PROTECT_FROM_MAGIC};

        for (Prayer over : overheads)
        {
            if (client.isPrayerActive(over))
            {
                return over;
            }
        }

        return null;
    }

    public static Prayer getActiveOffense()
    {
        final Prayer[] offensives = new Prayer[] {Prayer.MYSTIC_LORE, Prayer.MYSTIC_MIGHT, Prayer.MYSTIC_VIGOUR, Prayer.AUGURY, Prayer.HAWK_EYE, Prayer.EAGLE_EYE, Prayer.DEADEYE, Prayer.RIGOUR, Prayer.ULTIMATE_STRENGTH, Prayer.CHIVALRY, Prayer.PIETY};

        for (Prayer off : offensives)
        {
            if (client.isPrayerActive(off))
            {
                return off;
            }
        }

        return null;
    }

    public static boolean isInMultiwayCombat()
    {
        return (!InteractionUtils.isWidgetHidden(161, 20) && InteractionUtils.getWidgetSpriteId(161, 20) == 442) || // Resizable mode
                (!InteractionUtils.isWidgetHidden(548, 36) && InteractionUtils.getWidgetSpriteId(548, 36) == 442);  // Fixed mode
    }

    public static boolean isQuickPrayersEnabled()
    {
        return client.getVarbitValue(Varbits.QUICK_PRAYER) == 1;
    }

    public static int getSpecEnergy()
    {
        return EquipmentUtils.contains("Soulreaper axe") ? client.getVarpValue(3784) : client.getVarpValue(300) / 10;
    }

    public static void toggleSpec()
    {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, WidgetInfoExtended.PACK(160, 36), -1, -1);
    }

    public static boolean isSpecEnabled() {
        return client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 1;
    }

}
