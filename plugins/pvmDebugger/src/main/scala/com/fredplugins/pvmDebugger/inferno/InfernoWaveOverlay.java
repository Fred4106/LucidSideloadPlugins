package com.fredplugins.pvmDebugger.inferno;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import lombok.AccessLevel;
import lombok.Setter;
import static com.fredplugins.pvmDebugger.inferno.InfernoWaveMappings.addWaveComponent;
import com.fredplugins.pvmDebugger.inferno.displaymodes.InfernoWaveDisplayMode;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;

@Singleton
public class InfernoWaveOverlay extends Overlay
{
	private final InfernoPlugin plugin;
	private final FredsInfernoConfig config;
	private final PanelComponent panelComponent;

	@Setter(AccessLevel.PACKAGE)
	private Color waveHeaderColor;

	@Setter(AccessLevel.PACKAGE)
	private Color waveTextColor;

	@Setter(AccessLevel.PACKAGE)
	private InfernoWaveDisplayMode displayMode;

	@Inject
	InfernoWaveOverlay(final InfernoPlugin plugin, final FredsInfernoConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		this.panelComponent = new PanelComponent();
		setPosition(OverlayPosition.TOP_RIGHT);
		setPriority(Overlay.PRIORITY_HIGHEST);
		panelComponent.setPreferredSize(new Dimension(160, 0));
	}

	public Dimension render(final Graphics2D graphics)
	{
		panelComponent.getChildren().clear();

		if (displayMode == InfernoWaveDisplayMode.CURRENT ||
			displayMode == InfernoWaveDisplayMode.BOTH)
		{
			addWaveComponent(
				config,
				panelComponent,
				"Current Wave (Wave " + plugin.getCurrentWaveNumber() + ")",
				plugin.getCurrentWaveNumber(),
				waveHeaderColor,
				waveTextColor
			);
		}

		if (displayMode == InfernoWaveDisplayMode.NEXT ||
			displayMode == InfernoWaveDisplayMode.BOTH)
		{
			addWaveComponent(
				config,
				panelComponent,
				"Next Wave (Wave " + plugin.getNextWaveNumber() + ")",
				plugin.getNextWaveNumber(),
				waveHeaderColor,
				waveTextColor
			);
		}

		return panelComponent.render(graphics);
	}
}
