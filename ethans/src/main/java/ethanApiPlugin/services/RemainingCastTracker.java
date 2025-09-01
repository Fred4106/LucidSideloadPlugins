package ethanApiPlugin.services;

import ch.qos.logback.classic.Level;
import com.fredplugins.common.extensions.MenuExtensions$;
import com.fredplugins.common.magic.RuneChanges;
import com.fredplugins.common.magic.RuneIds;
import com.fredplugins.common.magic.SpellIds;
import com.fredplugins.common.magic.SpellInfo;
import com.fredplugins.common.magic.events.BoltsEnchanted;
import com.fredplugins.common.magic.events.RunesChanged;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.*;
import java.util.List;

@Singleton
@Slf4j
public class RemainingCastTracker {

    private Map<SpellInfo, RemainingCastsInfoBox> castBoxes = new HashMap<>();
    private Queue<SpellInfo> spellQueue = new LinkedList<>();
    private Map<Integer, Integer> runeCount = new HashMap<>();
    private boolean active = false;
    private boolean otherItemContainerOpen = false;
    private int lastCastSpriteId = -1;
    private String lastCastSpellName;
    private Plugin plugin;

    private long lastSpellCastTime = 0;
    private long lastManualCastSpellTime = 0;
    private long lastManualAttackTime = 0;

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
	@Inject
    private EventBus eventBus;
    @Inject
    private Notifier notifier;
    @Inject
    private ChatMessageManager chatManager;
    @Inject
    private CastSuppliesTracker suppliesTracker;
    @Inject
    private SpriteManager spriteManager;
    @Inject
    private InfoBoxManager infoBoxManager;
    @Inject
    private ItemManager itemManager;
//    @Inject
//    private RemainingCastsConfig config;

    @Subscribe
    public void onClientTick(ClientTick tick)
    {
        if (!active)
            return;

        checkCastBoxExpiry();
    }

    @Subscribe
    public void onRunesChanged(RunesChanged event)
    {
        if (!active/* || (!config.enableInfoboxes() && !config.useChatWarnings())*/)
            return;

        final RuneChanges changes = event.getChanges();
        SpellInfo spellInfo = getLastSpellCasted(changes);

        if (otherItemContainerOpen || client.getGameState() != GameState.LOGGED_IN) // Ignore all item deposits
            spellInfo = null;

        runeCount = changes.getCurrentRunes();
        lastSpellCastTime = Instant.now().getEpochSecond();

//        if (config.enableInfoboxes())
            updateCastBoxes(spellInfo);

//        if (config.useChatWarnings())
            updateWarnings(spellInfo);

        if (/*!config.enableInfoboxes() || */spellInfo == null/* || isFiltered(spellInfo)*/)
            return;

        processCast(spellInfo);
    }
    public void start(Plugin plugin)
    {
		if(!active) {
			((ch.qos.logback.classic.Logger) log).setLevel(Level.DEBUG);
			active = true;
	        this.plugin = plugin;
			eventBus.register(this);
		}
    }

    @Subscribe
    public void onBoltsEnchanted(BoltsEnchanted event)
    {
        if (!active || /*(!config.enableInfoboxes() && !config.useChatWarnings()) || */client.getGameState() != GameState.LOGGED_IN)
            return;

        final RuneChanges changes = event.getChanges();
        SpellInfo enchant = event.getEnchantSpell();

        if (!matchesSpellInfo(enchant, changes) || otherItemContainerOpen)
            enchant = null;

        runeCount = changes.getCurrentRunes();

//        if (config.enableInfoboxes())
            updateCastBoxes(enchant);

//        if (config.useChatWarnings())
            updateWarnings(enchant);

        if (/*!config.enableInfoboxes() || */enchant == null /*||isFiltered(enchant)*/)
            return;

        processCast(enchant);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (!active)
            return;

        final MenuEntry entry = event.getMenuEntry();
        final String option = entry.getOption();
        final String target = entry.getTarget();
		final MenuAction action = event.getMenuAction();

        if (option.equals("Attack"))
        {
            lastManualAttackTime = Instant.now().getEpochSecond(); // Autocast attack option
            return;
        }
        if (!option.equals("Cast") && !target.contains("Teleport"))
            return;

        final Widget widget = entry.getWidget();
        final String spellName = widget != null ?  Text.removeFormattingTags(widget.getName())
                : Text.removeFormattingTags(target).replaceAll(" ->.*", "");
/*		if (option.equals("Cast") || target.contains("Teleport")) {
			log.debug("cast: option [{}], target [{}], spellName [{}], type [{}]", option, target, spellName, entry.getType());
		} else if(option.equals("Reanimate") && target.contains("Ensouled")) {
			log.debug("reanimate: option [{}], target [{}]", option, target);
		} else {
			log.debug(" failed: option [{}], target [{}], spellName [{}], type [{}]", option, target, spellName, entry.getType());
			return;
		}
*/
		log.debug("Clicked \"{}\" which found SpellInfo {}", spellName, SpellIds.getSpellByName(spellName));
        if (SpellIds.getSpellByName(spellName) != null)
            lastCastSpellName = spellName;

        if (widget == null || widget.getTargetVerb().isEmpty()) // Spell actually cast, not just selected
            lastManualCastSpellTime = Instant.now().getEpochSecond();

        if (widget == null)
            return;

        final int spriteId = widget.getSpriteId();
        if (SpellIds.getSpellBySpriteId(spriteId) != null)
            lastCastSpriteId = spriteId;
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        if (isOtherItemContainerWidget(event.getGroupId()))
        {
            otherItemContainerOpen = true;
            lastCastSpriteId = -1;
            lastCastSpellName = null;
        }
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event)
    {
        if (isOtherItemContainerWidget(event.getGroupId()))
        {
            otherItemContainerOpen = false;
        }
    }

    public void stop()
    {
		if(active) {
			eventBus.unregister(this);

			active = false;
			lastCastSpriteId = -1;
			lastCastSpellName = null;
			removeAllCastBoxes();
		}
    }

    private SpellInfo getLastSpellCasted(RuneChanges changes)
    {
        final boolean autoCastFirst = wasLastCastAutocast();

        if (autoCastFirst) // There is probably a cleaner way to do this
        {
            SpellInfo spellInfo = getSpellFromAutocast(changes);
            if (spellInfo != null)
                return spellInfo;

            spellInfo = getSpellFromManualCast(changes);
            if (spellInfo != null)
                return spellInfo;
        }
        else
        {
            SpellInfo spellInfo = getSpellFromManualCast(changes);
            if (spellInfo != null)
                return spellInfo;

            spellInfo = getSpellFromAutocast(changes);
            if (spellInfo != null)
                return spellInfo;
        }

        return null;
    }

    private boolean matchesSpellInfo(SpellInfo info, RuneChanges changes)
    {
        final Map<Integer, Integer> cost = changes.getInvertedChanges();
        return info != null && info.getSpellCost().matchesCost(cost, changes.getUnlimitedRunes());
    }

    private SpellInfo getSpellFromManualCast(RuneChanges changes) // Name lookup now default since resizable sprite changes
    {
        SpellInfo spellInfo = lastCastSpellName != null ? SpellIds.getSpellByName(lastCastSpellName) : null;
        if (matchesSpellInfo(spellInfo, changes))
            return spellInfo;

        spellInfo = lastCastSpriteId != -1 ? SpellIds.getSpellBySpriteId(lastCastSpriteId) : null;
        if (matchesSpellInfo(spellInfo, changes))
            return spellInfo;

        return null;
    }

    private SpellInfo getSpellFromAutocast(RuneChanges changes)
    {
        final Widget w = client.getWidget(WidgetInfo.COMBAT_SPELL_ICON);
        if (w == null)
            return null;

        final int spriteId = w.getSpriteId();
        final SpellInfo spellInfo = SpellIds.getSpellBySpriteId(spriteId);

        return matchesSpellInfo(spellInfo, changes) ? spellInfo : null;
    }

    private boolean wasLastCastAutocast()
    {
        if (lastManualAttackTime > lastManualCastSpellTime)
            return true;

        return lastManualCastSpellTime < lastSpellCastTime;
    }

    private void updateWarnings(SpellInfo recentCast)
    {
        if (recentCast == null)
            return;

        final List<Integer> thresholds = new ArrayList<>();
        final List<String> thresholdStrings = Text.fromCSV(/*config.chatWarningThresholds()*/"");
        thresholdStrings.forEach(ts -> thresholds.add(tryParseInt(ts, -1)));
        Collections.sort(thresholds);

        if (thresholds.size() == 0)
            return;

        final int remaining = recentCast.getSpellCost().getRemainingCasts(runeCount);

        for (int val : thresholds)
        {
            if (remaining == val)
            {
                final String msgContent = getWarningMessage(recentCast.getName(), val);

                final String msg = new ChatMessageBuilder()
                        .append(ChatColorType.NORMAL)
                        .append(Color.red, msgContent)
                        .build();

                chatManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.GAMEMESSAGE)
                        .name("Remaining Casts")
                        .runeLiteFormattedMessage(msg)
                        .build());

//                if (config.useChatWarningNotifications())
//                    notifier.notify(msgContent);

                break;
            }

        }
    }

    private String getWarningMessage(String spellName, int casts)
    {
        String message = spellName + " has " + (casts == 0 ? "no" : casts);
        message += (casts == 1 ? " cast " : " casts ") + "remaining.";

        return message;
    }

    private void updateCastBoxes(SpellInfo recentCast)
    {
        final int threshold = 0;//config.infoBoxThreshold();
        final List<SpellInfo> toRemove = new ArrayList<>();

        if (recentCast != null && castBoxes.containsKey(recentCast))
        {
            final RemainingCastsInfoBox infoBox = castBoxes.get(recentCast);
            infoBox.update(runeCount, true);
        }

        castBoxes.values().forEach(c ->
        {
            if (threshold != 0 && c.getRemainingCasts() > threshold)
                toRemove.add(c.getSpellInfo());
            else
                c.update(runeCount);
        });

        toRemove.forEach(this::removeCastBox);
    }

    private void processCast(SpellInfo spellInfo)
    {
        if (!castBoxes.containsKey(spellInfo))
            createRemainingCastsBox(spellInfo, false);
    }

    private RemainingCastsInfoBox createRemainingCastsBox(SpellInfo spellInfo, boolean isPinned)
    {
        final BufferedImage sprite = /*config.showInfoBoxSprites()
											? */spriteManager.getSprite(spellInfo.getSpriteId(), 0)
							/*                : getSpellBookSprite()*/;

        final int threshold = 0;//config.infoBoxThreshold();
        final int remainingCasts = spellInfo.getSpellCost().getRemainingCasts(runeCount);
        if (!isPinned && threshold != 0 && remainingCasts > threshold)
            return null;

        final RemainingCastsInfoBox infoBox = new RemainingCastsInfoBox(spellInfo, runeCount, sprite, plugin, itemManager);
        infoBox.setPinned(isPinned);
        infoBoxManager.addInfoBox(infoBox);
        castBoxes.put(spellInfo, infoBox);

        if (isPinned)
        {
            infoBox.resetActiveTime();
        }
        else
        {
            spellQueue.add(spellInfo);
        }

        checkCastBoxLimit();
        return infoBox;
    }

    private void checkCastBoxLimit()
    {
        if (spellQueue.isEmpty() /* || config.infoBoxSpellLimit() <= 0*/)
            return;

//        if (spellQueue.size() > config.infoBoxSpellLimit())
//        {
//            final SpellInfo oldest = spellQueue.peek();
//            removeCastBox(oldest);
//        }
    }

    private void checkCastBoxExpiry()
    {
        final int expirySeconds = 0;//config.infoBoxExpirySeconds();
        if (expirySeconds == 0)
            return;

        final List<SpellInfo> expired = new ArrayList<>();
        for (final SpellInfo spellInfo : castBoxes.keySet())
        {
            final RemainingCastsInfoBox box = castBoxes.get(spellInfo);
            if (!box.isPinned() && box.getActiveTime() > expirySeconds)
                expired.add(spellInfo);
        }

        expired.forEach(this::removeCastBox);
    }
//
//    private void updatePinnedSpells()
//    {
//        final String pinned = //config.pinnedSpells();
//        final List<String> spellNames = Text.fromCSV(pinned);
//
//        clientThread.invoke(() ->
//        {
//            castBoxes.values().forEach(cb -> cb.setPinned(false));
//
//            if (spellNames.size() == 0)
//                return;
//
//            spellNames.forEach(name ->
//            {
//                final SpellInfo info = SpellIds.getSpellByName(name);
//                if (info == null)
//                    return;
//
//                RemainingCastsInfoBox box = castBoxes.getOrDefault(info, null);
//                if (box != null)
//                {
//                    box.setPinned(true);
//                }
//                else
//                {
//                    box = createRemainingCastsBox(info, true);
//                    if (box != null)
//                        box.setPinned(true);
//                }
//            });
//        });
//    }

    private void removeCastBox(SpellInfo spellInfo)
    {
        if (!castBoxes.containsKey(spellInfo))
            return;

        final RemainingCastsInfoBox box = castBoxes.get(spellInfo);
        infoBoxManager.removeInfoBox(box);
        castBoxes.remove(spellInfo);
        spellQueue.remove(spellInfo);
    }

    private void removeAllCastBoxes()
    {
        infoBoxManager.removeIf(i -> i instanceof RemainingCastsInfoBox);
        castBoxes.clear();
        spellQueue.clear();
    }

    private boolean isFiltered(SpellInfo spellInfo)
    {
//        if (config.spellFilterOption() == SpellFilterOption.DISABLED)
//            return false;
//
//        final String filterList = config.filterList();
//        final List<String> spellNames = Text.fromCSV(filterList);
//
//        if (config.spellFilterOption() == SpellFilterOption.BLACKLIST)
//        {
//            return spellNames.stream().anyMatch(s -> spellInfo.getName().equalsIgnoreCase(s));
//        }
//        else if (config.spellFilterOption() == SpellFilterOption.WHITELIST)
//        {
//            return spellNames.stream().noneMatch(s -> spellInfo.getName().equalsIgnoreCase(s));
//        }

        return false;
    }

    private BufferedImage getSpellBookSprite()
    {
        final Widget spellBook = client.getWidget(WidgetInfo.RESIZABLE_VIEWPORT_MAGIC_ICON);
        return spellBook != null ? spriteManager.getSprite(spellBook.getSpriteId(), 0)
                : spriteManager.getSprite(SpriteID.TAB_MAGIC, 0);
    }

    private boolean isOtherItemContainerWidget(int groupId)
    {
        switch (groupId)
        {
            case WidgetID.BANK_GROUP_ID:
            case WidgetID.DEPOSIT_BOX_GROUP_ID:
            case WidgetID.TRADE_WINDOW_GROUP_ID:
            case WidgetID.GROUP_STORAGE_GROUP_ID:
                return true;
            default:
                return false;
        }
    }

    public int tryParseInt(String value, int defaultVal) {
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return defaultVal;
        }
    }


}