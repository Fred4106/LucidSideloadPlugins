package com.fredplugins.giantsfoundry;

import static com.fredplugins.giantsfoundry.FredsGiantsFoundryHelper.getHeatColor;
import com.fredplugins.giantsfoundry.enums.Heat;
import com.fredplugins.giantsfoundry.enums.Stage;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Singleton
public class FoundryOverlay2D extends OverlayPanel
{
	private static final int REGION_ID = 13491;
	private final Client client;
	private final FredsGiantsFoundryPlugin plugin;
	private final FredsGiantsFoundryState state;
	private final FredsGiantsFoundryConfig config;

	@Inject
	private FoundryOverlay2D(
		Client client,
		FredsGiantsFoundryPlugin plugin,
		FredsGiantsFoundryState state,
		FredsGiantsFoundryConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.state = state;
		this.config = config;
		this.setPosition(OverlayPosition.BOTTOM_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.getLocalPlayer().getWorldLocation().getRegionID() != REGION_ID)
		{
			return null;
		}
		boolean swordPickedUp = state.isEnabled() && state.getCurrentStage() != null;

		if (config.drawTitle())
		{
			panelComponent.getChildren().add(TitleComponent.builder().text("Freds Giant's Foundry").build());
		}

		if (swordPickedUp)
		{
			Heat heat = state.getCurrentHeat();
			Stage stage = state.getCurrentStage();

			if (config.drawHeatInfo())
			{
				panelComponent.getChildren().add(
					LineComponent.builder().left("Heat").right(heat.getName() + " (" + state.getHeatAmount() / 10 + "%)").rightColor(heat.getColor()).build()
				);
			}
			if (config.drawStageInfo())
			{
				panelComponent.getChildren().add(
					LineComponent.builder().left("Stage").right(stage.getName() + " (" + state.getProgressAmount() / 10 + "%)").rightColor(stage.getHeat().getColor()).build()
				);
			}

			int actionsLeft = state.getActionsLeftInStage();
			int heatLeft = state.getActionsForHeatLevel();

			if (config.drawActionsLeft())
			{
				panelComponent.getChildren().add(
					LineComponent.builder().left("Actions left").right(actionsLeft + "").build()
				);
			}
			if (config.drawHeatLeft())
			{
				panelComponent.getChildren().add(
					LineComponent.builder().left("Heat left").right(heatLeft + "").rightColor(getHeatColor(actionsLeft, heatLeft)).build()
				);
			}
		}
//
//		int points = plugin.getPointsTracker().getShopPoints();
		if (config.drawShopPoints())
		{
			panelComponent.getChildren().add(
				LineComponent.builder().left("Reputation").right(plugin.getReputation() + "").build()
			);
		}

		return super.render(graphics);
	}
}
