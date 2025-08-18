package ethanApiPlugin.services;

import com.fredplugins.common.magic.RuneIds;
import com.fredplugins.common.magic.SpellCost;
import com.fredplugins.common.magic.SpellInfo;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
public class RemainingCastsInfoBox extends InfoBox
{
	private final SpellInfo spellInfo;
	private final SpellCost spellCost;
	final String[] runeNames;
	private String tooltip = "";
	private Map<Integer, Integer> runeCount;
	private int remainingCasts;
	private long lastUpdated;
	private boolean advTooltipDisabled = false;
	@Setter
	private boolean isPinned;

	public RemainingCastsInfoBox(SpellInfo spellInfo, Map<Integer, Integer> runeCount, BufferedImage image, Plugin plugin, ItemManager itemManager)
	{
		super(image, plugin);
		this.spellInfo = spellInfo;
		this.spellCost = spellInfo.getSpellCost();
		this.lastUpdated = Instant.now().getEpochSecond();
		this.runeCount = runeCount;
		this.remainingCasts = this.spellCost.getRemainingCasts(runeCount);
//        this.config = config;

		final int[] runes = spellCost.getRunes();
		this.runeNames = new String[runes.length];
		for (int i = 0; i < runes.length; i++)
		{
			this.runeNames[i] = itemManager.getItemComposition(runes[i]).getName();
			if (this.runeNames[i] == null || this.runeNames[i].equals(""))
			{
				advTooltipDisabled = true;
				break;
			}
		}

		buildTooltip();
	}

	@Override
	public String getText()
	{
		return getRemainingCastsString();
	}

	@Override
	public String getTooltip()
	{
		return tooltip;
	}

	@Override
	public Color getTextColor()
	{
		return Color.white;//config.infoBoxTextColor();
	}

	public void update(Map<Integer, Integer> runeCount)
	{
		update(runeCount, false);
	}

	public void update(Map<Integer, Integer> runeCount, boolean wasCast)
	{
		this.runeCount = runeCount;
		remainingCasts = spellCost.getRemainingCasts(runeCount);
		buildTooltip();

		if (wasCast)
			lastUpdated = Instant.now().getEpochSecond();
	}

	public long getActiveTime()
	{
		return (Instant.now().getEpochSecond() - lastUpdated);
	}

	public void resetActiveTime()
	{
		this.lastUpdated = 0;
	}

	private String getRemainingCastsString()
	{
		if (remainingCasts == -1)
			return "N/A";

		return "" + remainingCasts + "";

//        return /*config.shortenCastAmounts() ? CastUtils.getShortenedAmount(remainingCasts) : CastUtils.formatCastAmount(remainingCasts)*/;
	}

	private void buildTooltip()
	{
		String base = spellInfo.getName() + " - " + (remainingCasts != Integer.MAX_VALUE ? remainingCasts : "Unlimited");
		base += (remainingCasts != 1 ? " casts " : " cast ") + "remaining";

		if (advTooltipDisabled)
		{
			tooltip = base;
			return;
		}

		StringBuilder advTooltip = new StringBuilder(base + "</br>");
		final int[] runes = spellCost.getRunes();
		final int[] quantities = spellCost.getQuantities();
		for (int i = 0; i < runes.length; i++)
		{
			final int runeId = runes[i];
			final int quantity = quantities[i];
			final int available = runeCount.getOrDefault(runeId, 0);
			final int floorDiv = getCostDivision(runeId, quantity, available);
			final String runeName = runeNames[i];
			if (runeName == null || runeName.equals(""))
			{
				tooltip = base;
				return;
			}

			advTooltip.append("</br>")
					.append(runeName)
					.append(": ")
					.append(available != Integer.MAX_VALUE ? available : "*")
					.append("/")
					.append(quantity);

			if (available != Integer.MAX_VALUE && floorDiv != Integer.MAX_VALUE)
			{
				advTooltip.append(" (")
					.append(floorDiv)
					.append(")");
			}
		}

		tooltip = advTooltip.toString();
	}

	private int getCostDivision(int itemId, int cost, int available)
	{
		if (available == Integer.MAX_VALUE || (available >= 1 && RuneIds.isReqStaff(itemId)))
			return Integer.MAX_VALUE;

		return cost != 0 && available != 0 ? Math.floorDiv(available, cost) : 0;
	}
	@Override
	public String getName()
	{
		return super.getName() + "["+getSpellInfo().getName().replace(' ', '_') + "]";
	}
}