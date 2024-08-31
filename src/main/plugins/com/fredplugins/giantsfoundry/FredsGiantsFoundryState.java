package com.fredplugins.giantsfoundry;

import static com.fredplugins.giantsfoundry.MathUtil.max1;
import static com.fredplugins.giantsfoundry.FredsGiantsFoundryClientIDs.*;

import com.fredplugins.giantsfoundry.enums.SHeat;
import com.fredplugins.giantsfoundry.enums.SHeat$;
import com.fredplugins.giantsfoundry.enums.SStage;
import com.fredplugins.giantsfoundry.enums.SStage$;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class FredsGiantsFoundryState
{

	@Inject
	private Client client;

	@Setter
	@Getter
	private boolean enabled;

	private final List<SStage> stages = new ArrayList<>();

	public void reset()
	{
		stages.clear();
	}

	public int getHeatAmount()
	{
		return client.getVarbitValue(VARBIT_HEAT);
	}

	public int getProgressAmount()
	{
		return client.getVarbitValue(VARBIT_PROGRESS);
	}
	
	public List<SStage> getStages()
	{
		SStage$.MODULE$.fromWidget(client, stages);
		return stages;
	}

	public SStage getCurrentStage()
	{
		int index = (int) (getProgressAmount() / 1000d * getStages().size());
		if (index < 0 || index > getStages().size() - 1)
		{
			return null;
		}

		return getStages().get(index);
	}

	public SHeat getCurrentHeat()
	{
		int heat = getHeatAmount();
		double ratio = SHeat$.MODULE$.getHeatRangeRatio(client);
		return SHeat$.MODULE$.getRangeAt(heat, ratio);
	}

	public int getHeatChangeNeeded()
	{
		SHeat requiredHeat = getCurrentStage().heat();
		int heat = getHeatAmount();
		double ratio = SHeat$.MODULE$.getHeatRangeRatio(client);
		int[] range = requiredHeat.range(ratio);

		if (heat < range[0])
			return range[0] - heat;
		else if (heat > range[1])
			return range[1] - heat;
		else
			return 0;
	}

	public int getCrucibleCount()
	{
		int bronze = client.getVarbitValue(VARBIT_BRONZE_COUNT);
		int iron = client.getVarbitValue(VARBIT_IRON_COUNT);
		int steel = client.getVarbitValue(VARBIT_STEEL_COUNT);
		int mithril = client.getVarbitValue(VARBIT_MITHRIL_COUNT);
		int adamant = client.getVarbitValue(VARBIT_ADAMANT_COUNT);
		int rune = client.getVarbitValue(VARBIT_RUNE_COUNT);

		return bronze + iron + steel + mithril + adamant + rune;
	}

	public double getCrucibleQuality()
	{
		if (getCrucibleCount() == 0) return 0;

		int bronze = client.getVarbitValue(VARBIT_BRONZE_COUNT);
		int iron = client.getVarbitValue(VARBIT_IRON_COUNT);
		int steel = client.getVarbitValue(VARBIT_STEEL_COUNT);
		int mithril = client.getVarbitValue(VARBIT_MITHRIL_COUNT);
		int adamant = client.getVarbitValue(VARBIT_ADAMANT_COUNT);
		int rune = client.getVarbitValue(VARBIT_RUNE_COUNT);

		final int BRONZE_VALUE = 1;
		final int IRON_VALUE = 2;
		final int STEEL_VALUE = 3;
		final int MITHRIL_VALUE = 4;
		final int ADAMANT_VALUE = 5;
		final int RUNE_VALUE = 6;

		final double vB = (10 * BRONZE_VALUE * bronze) / 28.0;
		final double vI = (10 * IRON_VALUE * iron) / 28.0;
		final double vS = (10 * STEEL_VALUE * steel) / 28.0;
		final double vM = (10 * MITHRIL_VALUE * mithril) / 28.0;
		final double vA = (10 * ADAMANT_VALUE * adamant) / 28.0;
		final double vR = (10 * RUNE_VALUE * rune) / 28.0;

		return
			(10 * (vB + vI + vS + vM + vA + vR)
				+ (max1(vB) * max1(vI) * max1(vS) * max1(vM) * max1(vA) * max1(vR))) / 10.0;
	}

	/**
	 * Get the amount of progress each stage needs
	 */
	public double getProgressPerStage()
	{
		return 1000d / getStages().size();
	}

	public int getActionsLeftInStage()
	{
		int progress = getProgressAmount();
		double progressPerStage = getProgressPerStage();
		double progressTillNext = progressPerStage - progress % progressPerStage;

		SStage current = getCurrentStage();
		return (int) Math.ceil(progressTillNext / current.progressPerAction());
	}

	public int[] getCurrentHeatRange()
	{
		SStage cStage = getCurrentStage();
		if(cStage != null) {
			return cStage.heat().range(SHeat.getHeatRangeRatio(client));
		} else {
			return new int[]{0, 0};
		}
		//
	}

	/**
	 * Get the amount of current stage actions that can be
	 * performed before the heat drops too high or too low to
	 * continue
	 */
	public int getActionsForHeatLevel()
	{
		SHeat heatStage = getCurrentHeat();
		SStage stage = getCurrentStage();
		if (heatStage != stage.heat())
		{
			// not the right heat to start with
			return 0;
		}

		int[] range = getCurrentHeatRange();
		int actions = 0;
		int heat = getHeatAmount();
		while (heat > range[0] && heat < range[1])
		{
			actions++;
			heat += stage.heatChange();
		}

		return actions;
	}

	public HeatActionStateMachine heatingCoolingState = new HeatActionStateMachine();

}
