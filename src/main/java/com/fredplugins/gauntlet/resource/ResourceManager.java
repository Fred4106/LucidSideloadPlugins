package com.fredplugins.gauntlet.resource;

import com.fredplugins.gauntlet.FredGauntletConfig;
import com.fredplugins.gauntlet.FredGauntletPlugin;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class ResourceManager
{
    private static final int NORMAL_GAUNTLET_REGION_ID = 7512;
    private static final int CORRUPTED_GAUNTLET_REGION_ID = 7768;

    private static final String MESSAGE_UNTRADEABLE_DROP = "Untradeable drop: ";

    private static final Pattern PATTERN_RESOURCE_DROP = Pattern.compile("((?<quantity>\\d+) x )?(?<name>.+)");

    @Inject
    private Client client;

    @Inject
    private FredGauntletPlugin plugin;

    @Inject
    private FredGauntletConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private InfoBoxManager infoBoxManager;

    private final Map<com.fredplugins.gauntlet.resource.Resource, com.fredplugins.gauntlet.resource.ResourceCounter> resources = new HashMap();

    private Region region = Region.UNKNOWN;

    private String prefix;

    public void init()
    {
        prefix = isLootVarbitSet() ? MESSAGE_UNTRADEABLE_DROP : getNamedDropMessage();
        region = getRegion();
        createCustomCounters();
    }

    public void reset()
    {
        prefix = null;
        region = Region.UNKNOWN;

        resources.clear();

        infoBoxManager.removeIf(com.fredplugins.gauntlet.resource.ResourceCounter.class::isInstance);
    }

    public void parseChatMessage(String chatMessage)
    {
        if (config.resourceTracker() == FredGauntletConfig.ResourceFilter.OFF || region == Region.UNKNOWN || prefix == null)
        {
            return;
        }

        chatMessage = Text.removeTags(chatMessage);

        if (chatMessage.startsWith(prefix))
        {
            chatMessage = chatMessage.replace(prefix, "");

            processNpcResource(chatMessage);
        }
        else
        {
            processSkillResource(chatMessage);
        }
    }

    private void processNpcResource(final String chatMessage)
    {
        final Matcher matcher = PATTERN_RESOURCE_DROP.matcher(chatMessage);

        if (!matcher.matches())
        {
            return;
        }

        final String itemName = matcher.group("name");

        if (itemName == null)
        {
            return;
        }

        final com.fredplugins.gauntlet.resource.Resource resource = com.fredplugins.gauntlet.resource.Resource.fromName(itemName, region == Region.CORRUPTED);

        if (resource == null ||
                (config.resourceTracker() == FredGauntletConfig.ResourceFilter.CUSTOM && !resources.containsKey(resource)) ||
                (config.resourceTracker() == FredGauntletConfig.ResourceFilter.BASIC && isNonBasicResource(resource)))
        {
            return;
        }

        final String quantity = matcher.group("quantity");
        final int itemCount = quantity != null ? Integer.parseInt(quantity) : 1;

        processResource(resource, itemCount);
    }

    private void processSkillResource(final String chatMessage)
    {
        final Map<com.fredplugins.gauntlet.resource.Resource, Integer> mapping = com.fredplugins.gauntlet.resource.Resource.fromPattern(chatMessage, region == Region.CORRUPTED);

        if (mapping == null)
        {
            return;
        }

        final com.fredplugins.gauntlet.resource.Resource resource = mapping.keySet().iterator().next();

        if (config.resourceTracker() == FredGauntletConfig.ResourceFilter.CUSTOM && !resources.containsKey(resource))
        {
            return;
        }

        final int itemCount = mapping.get(resource);

        processResource(resource, itemCount);
    }

    private void processResource(final com.fredplugins.gauntlet.resource.Resource resource, final int itemCount)
    {
        if (!resources.containsKey(resource))
        {
            initResource(resource, itemCount);
        }
        else
        {
            com.fredplugins.gauntlet.resource.ResourceCounter counter = resources.get(resource);
            if (config.resourceTracker() == FredGauntletConfig.ResourceFilter.CUSTOM)
            {
                counter.decrementCount(itemCount);
            }
            else
            {
                counter.incrementCount(itemCount);
            }
        }
    }

    private void initResource(final com.fredplugins.gauntlet.resource.Resource resource, final int itemCount)
    {
        final com.fredplugins.gauntlet.resource.ResourceCounter counter = new com.fredplugins.gauntlet.resource.ResourceCounter(plugin, resource,
                itemManager.getImage(resource.getItemId()), itemCount);

        resources.put(resource, counter);
        infoBoxManager.addInfoBox(counter);
    }

    private void createCustomCounters()
    {
        if (config.resourceTracker() != FredGauntletConfig.ResourceFilter.CUSTOM || region == Region.UNKNOWN)
        {
            return;
        }

        final int ore = config.resourceOre();
        final int bark = config.resourceBark();
        final int tirinum = config.resourceTirinum();
        final int grym = config.resourceGrym();
        final int frame = config.resourceFrame();
        final int fish = config.resourcePaddlefish();
        final int shard = config.resourceShard();
        final boolean bowstring = config.resourceBowstring();
        final boolean spike = config.resourceSpike();
        final boolean orb = config.resourceOrb();

        final boolean corrupted = region == Region.CORRUPTED;

        if (ore > 0)
        {
            initResource(corrupted ? com.fredplugins.gauntlet.resource.Resource.CORRUPTED_ORE : com.fredplugins.gauntlet.resource.Resource.CRYSTAL_ORE, ore);
        }
        if (bark > 0)
        {
            initResource(corrupted ? com.fredplugins.gauntlet.resource.Resource.CORRUPTED_PHREN_BARK : com.fredplugins.gauntlet.resource.Resource.PHREN_BARK, bark);
        }
        if (tirinum > 0)
        {
            initResource(corrupted ? com.fredplugins.gauntlet.resource.Resource.CORRUPTED_LINUM_TIRINUM : com.fredplugins.gauntlet.resource.Resource.LINUM_TIRINUM, tirinum);
        }
        if (grym > 0)
        {
            initResource(corrupted ? com.fredplugins.gauntlet.resource.Resource.CORRUPTED_GRYM_LEAF : com.fredplugins.gauntlet.resource.Resource.GRYM_LEAF, grym);
        }
        if (frame > 0)
        {
            initResource(corrupted ? com.fredplugins.gauntlet.resource.Resource.CORRUPTED_WEAPON_FRAME : com.fredplugins.gauntlet.resource.Resource.WEAPON_FRAME, frame);
        }
        if (fish > 0)
        {
            initResource(com.fredplugins.gauntlet.resource.Resource.RAW_PADDLEFISH, fish);
        }
        if (shard > 0)
        {
            initResource(corrupted ? com.fredplugins.gauntlet.resource.Resource.CORRUPTED_SHARDS : com.fredplugins.gauntlet.resource.Resource.CRYSTAL_SHARDS, shard);
        }
        if (bowstring)
        {
            initResource(corrupted ? com.fredplugins.gauntlet.resource.Resource.CORRUPTED_BOWSTRING : com.fredplugins.gauntlet.resource.Resource.CRYSTALLINE_BOWSTRING, 1);
        }
        if (spike)
        {
            initResource(corrupted ? com.fredplugins.gauntlet.resource.Resource.CORRUPTED_SPIKE : com.fredplugins.gauntlet.resource.Resource.CRYSTAL_SPIKE, 1);
        }
        if (orb)
        {
            initResource(corrupted ? com.fredplugins.gauntlet.resource.Resource.CORRUPTED_ORB : com.fredplugins.gauntlet.resource.Resource.CRYSTAL_ORB, 1);
        }
    }

    private String getNamedDropMessage()
    {
        final Player player = client.getLocalPlayer();

        if (player == null)
        {
            return null;
        }

        return player.getName() + " received a drop: ";
    }

    private boolean isLootVarbitSet()
    {
        return client.getVarbitValue(5399) == 1 &&
                client.getVarbitValue(5402) == 1;
    }

    private Region getRegion()
    {
        final int regionId = client.getLocalPlayer().getWorldLocation().getRegionID();

        if (regionId == CORRUPTED_GAUNTLET_REGION_ID)
        {
            return Region.CORRUPTED;
        }

        if (regionId == NORMAL_GAUNTLET_REGION_ID)
        {
            return Region.NORMAL;
        }

        return Region.UNKNOWN;
    }

    private static boolean isNonBasicResource(final com.fredplugins.gauntlet.resource.Resource resource)
    {
        switch (resource)
        {
            case TELEPORT_CRYSTAL:
            case CORRUPTED_TELEPORT_CRYSTAL:
            case WEAPON_FRAME:
            case CORRUPTED_WEAPON_FRAME:
            case CRYSTALLINE_BOWSTRING:
            case CORRUPTED_BOWSTRING:
            case CRYSTAL_SPIKE:
            case CORRUPTED_SPIKE:
            case CRYSTAL_ORB:
            case CORRUPTED_ORB:
                return true;
            default:
                return false;
        }
    }

    private enum Region
    {
        UNKNOWN, NORMAL, CORRUPTED
    }
}