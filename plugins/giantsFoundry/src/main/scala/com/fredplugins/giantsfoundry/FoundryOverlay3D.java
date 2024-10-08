package com.fredplugins.giantsfoundry;

import static com.fredplugins.giantsfoundry.FredsGiantsFoundryClientIDs.VARBIT_GAME_STAGE;
import static com.fredplugins.giantsfoundry.FredsGiantsFoundryClientIDs.WIDGET_PROGRESS_PARENT;
import static com.fredplugins.giantsfoundry.FredsGiantsFoundryHelper.getHeatColor;
import static com.fredplugins.giantsfoundry.MouldHelper.SWORD_TYPE_1_VARBIT;
import static com.fredplugins.giantsfoundry.MouldHelper.SWORD_TYPE_2_VARBIT;
//import com.fredplugins.giantsfoundry.enums.CommissionType;
import com.fredplugins.giantsfoundry.enums.SCommissionType;
import com.fredplugins.giantsfoundry.enums.SCommissionType$;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.inject.Inject;

import com.fredplugins.giantsfoundry.enums.SHeat;
import com.fredplugins.giantsfoundry.enums.SStage;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import org.apache.commons.lang3.StringUtils;

public class FoundryOverlay3D extends Overlay
{

	private static final int HAND_IN_WIDGET = 49414221;
	private final ModelOutlineRenderer modelOutlineRenderer;

	GameObject tripHammer;
	GameObject grindstone;
	GameObject polishingWheel;
	GameObject lavaPool;
	GameObject waterfall;
	GameObject mouldJig;
	GameObject crucible;
	NPC kovac;

	private final Client client;
	private final FredsGiantsFoundryState state;
	private final FredsGiantsFoundryConfig config;

	@Inject
	private FoundryOverlay3D(
		Client client,
		FredsGiantsFoundryState state,
		FredsGiantsFoundryConfig config,
		ModelOutlineRenderer modelOutlineRenderer)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.state = state;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
	}

	private Color getObjectColor(SStage stage, SHeat heat)
	{
		if (stage.heat() != heat)
		{
			return config.toolBad();
		}

		if (BonusWidget.isActive(client))
		{
			return config.toolBonus();
		}

		int actionsLeft = state.getActionsLeftInStage();
		int heatLeft = state.getActionsForHeatLevel();
		if (actionsLeft <= 1 || heatLeft <= 1)
		{
			return config.toolCaution();
		}

		return config.toolGood();
	}

	private GameObject getStageObject(SStage stage)
	{
		switch (stage.name())
		{
			case "Hammer":
				return tripHammer;
			case "Grind":
				return grindstone;
			case "Polish":
				return polishingWheel;
		}
		return null;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!state.isEnabled())
		{
			return null;
		}

		if (config.highlightKovac())
		{
			drawKovacIfHandIn(graphics);
		}

		if (state.getCurrentStage() == null)
		{
			if (config.highlightMould())
			{
				drawMouldIfNotSet(graphics);
			}
			if (config.highlightCrucible())
			{
				drawCrucibleIfMouldSet(graphics);
			}
			return null;
		}

		SStage stage = state.getCurrentStage();
		GameObject stageObject = getStageObject(stage);
		if (stageObject == null)
		{
			return null;
		}

		drawHeatingActionOverlay(graphics, stageObject);

		SHeat heat = state.getCurrentHeat();
		Color color = getObjectColor(stage, heat);
		// TODO Config
		if (config.highlightStyle() == HighlightStyle.HIGHLIGHT_CLICKBOX)
		{
			drawObjectClickbox(graphics, stageObject, color);
		}
		else
		{
			drawObjectOutline(graphics, stageObject, color);
		}

		if ((stage.heat() != heat || !state.heatingCoolingState.isIdle()) && config.highlightWaterAndLava())
		{
			drawHeatChangers(graphics);
		}

		if (state.heatingCoolingState.isCooling())
		{
			drawHeatingActionOverlay(graphics, waterfall, false);
		}
		if (state.heatingCoolingState.isHeating())
		{
			drawHeatingActionOverlay(graphics, lavaPool, true);
		}


		return null;
	}

	private void drawObjectClickbox(Graphics2D graphics, GameObject stageObject, Color color)
	{
		Shape objectClickbox = stageObject.getClickbox();
		if (objectClickbox != null && config.highlightTools())
		{
			Point mousePosition = client.getMouseCanvasPosition();
			if (objectClickbox.contains(mousePosition.getX(), mousePosition.getY()))
			{
				graphics.setColor(color.darker());
			}
			else
			{
				graphics.setColor(color);
			}
			graphics.draw(objectClickbox);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(objectClickbox);
		}
	}

	private void drawObjectOutline(Graphics2D graphics, GameObject stageObject, Color color)
	{
		Color _color = new Color(color.getRed(), color.getGreen(), color.getBlue(), config.borderAlpha());
		modelOutlineRenderer.drawOutline(stageObject, config.borderThickness(), _color, config.borderFeather());
	}

	private void drawHeatingActionOverlay(
		Graphics2D graphics,
		GameObject stageObject,
		boolean isLava /* and not cooling */)
	{
		if (!config.drawLavaWaterInfoOverlay())
		{
			return;
		}

		if (state.heatingCoolingState.isIdle())
		{
			return;
		}

		String text;
		if (isLava)
		{
			// %d heats or %d dunks
			text = String.format("%d %s",
				state.heatingCoolingState.getRemainingDuration(),
				state.heatingCoolingState.getActionName()
			);
		}
		else
		{
			// %d cools
			text = String.format("%d %s",
				state.heatingCoolingState.getRemainingDuration(),
				state.heatingCoolingState.getActionName()
			);
		}

		LocalPoint stageLoc = stageObject.getLocalLocation();
		stageLoc = new LocalPoint(stageLoc.getX(), stageLoc.getY());

		Point pos = Perspective.getCanvasTextLocation(client, graphics, stageLoc, text, 50);
		Color color = config.lavaWaterfallColour();

		OverlayUtil.renderTextLocation(graphics, pos, text, color);
	}

	private void drawHeatChangers(Graphics2D graphics)
	{
		int change = state.getHeatChangeNeeded();
		Shape shape = null;

		if (change < 0 || state.heatingCoolingState.isCooling())
		{
			shape = waterfall.getClickbox();
		}
		else if (change > 0 || state.heatingCoolingState.isHeating())
		{
			shape = lavaPool.getClickbox();
		}
		if (shape != null)
		{
			Point mousePosition = client.getMouseCanvasPosition();
			Color color = config.lavaWaterfallColour();
			if (shape.contains(mousePosition.getX(), mousePosition.getY()))
			{
				graphics.setColor(color.darker());
			}
			else
			{
				graphics.setColor(color);
			}
			graphics.draw(shape);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(shape);
		}
	}

	static final int CRUCIBLE_CAPACITY = 28;

	private void drawCrucibleContent(Graphics2D graphics)
	{
		if (!config.drawCrucibleInfoOverlay())
		{
			return;
		}
		String text = String.format("%d/%d quality: %d", state.getCrucibleCount(), CRUCIBLE_CAPACITY, (int)state.getCrucibleQuality());

		LocalPoint crucibleLoc = crucible.getLocalLocation();
		crucibleLoc = new LocalPoint(crucibleLoc.getX() - 100, crucibleLoc.getY());

		Point pos = Perspective.getCanvasTextLocation(client, graphics, crucibleLoc, text, 200);
		Color color;
		if (state.getCrucibleCount() == CRUCIBLE_CAPACITY)
		{
			color = config.toolGood();
		}
		else
		{
			color = config.generalHighlight();
		}
		OverlayUtil.renderTextLocation(graphics, pos, text, color);
	}


	private void drawCrucibleIfMouldSet(Graphics2D graphics)
	{
		if (client.getVarbitValue(SWORD_TYPE_1_VARBIT) == 0)
		{
			return;
		}
		if (client.getVarbitValue(VARBIT_GAME_STAGE) != 1)
		{
			return;
		}

		drawCrucibleContent(graphics);

		if (config.highlightStyle() == HighlightStyle.HIGHLIGHT_CLICKBOX)
		{
			Shape shape = crucible.getConvexHull();
			if (shape != null)
			{
				Color color = config.generalHighlight();
				if (state.getCrucibleCount() == CRUCIBLE_CAPACITY)
				{
					graphics.setColor(config.toolGood());
				}
				else
				{
					graphics.setColor(config.generalHighlight());
				}
				graphics.draw(shape);
				graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
				graphics.fill(shape);
			}
		}
		else if (config.highlightStyle() == HighlightStyle.HIGHLIGHT_BORDER)
		{
			Color color;
			if (state.getCrucibleCount() == CRUCIBLE_CAPACITY)
			{
				color = config.toolGood();
			}
			else
			{
				color = config.generalHighlight();
			}
			drawObjectOutline(graphics, crucible, color);
		}
	}

	private void drawMouldIfNotSet(Graphics2D graphics)
	{
		if (client.getWidget(WIDGET_PROGRESS_PARENT) != null
			|| client.getVarbitValue(SWORD_TYPE_1_VARBIT) == 0
			|| (client.getVarbitValue(VARBIT_GAME_STAGE) != 0
			&& client.getVarbitValue(VARBIT_GAME_STAGE) != 2))
		{
			return;
		}
		if (config.highlightStyle() == HighlightStyle.HIGHLIGHT_CLICKBOX)
		{
			Shape shape = mouldJig.getConvexHull();
			if (shape != null)
			{
				Color color = config.generalHighlight();
				graphics.setColor(color);
				graphics.draw(shape);
				graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
				graphics.fill(shape);
			}
		}
		else if (config.highlightStyle() == HighlightStyle.HIGHLIGHT_BORDER)
		{
			drawObjectOutline(graphics, mouldJig, config.generalHighlight());
		}

		if (config.drawMouldInfoOverlay())
		{
			SCommissionType type1 = SCommissionType$.MODULE$.forVarbit(client.getVarbitValue(SWORD_TYPE_1_VARBIT)).getOrElse(null);
			SCommissionType type2 = SCommissionType$.MODULE$.forVarbit(client.getVarbitValue(SWORD_TYPE_2_VARBIT)).getOrElse(null);
			if(type1 == null || type2 == null) {
				return;
			}
			String text = StringUtils.capitalize(type1.toString().toLowerCase()) + " " + StringUtils.capitalize(type2.toString().toLowerCase());
			LocalPoint textLocation = mouldJig.getLocalLocation();
			textLocation = new LocalPoint(textLocation.getX(), textLocation.getY());
			Point canvasLocation = Perspective.getCanvasTextLocation(client, graphics, textLocation, text, 100);
			canvasLocation = new Point(canvasLocation.getX(), canvasLocation.getY() + 10);
			OverlayUtil.renderTextLocation(graphics, canvasLocation, text, config.generalHighlight());
		}
	}

	private void drawKovacIfHandIn(Graphics2D graphics)
	{
		Widget handInWidget = client.getWidget(HAND_IN_WIDGET);
		if (handInWidget != null && !handInWidget.isHidden())
		{
			Shape shape = kovac.getConvexHull();
			if (shape != null)
			{
				Color color = config.generalHighlight();
				graphics.setColor(color);
				graphics.draw(shape);
				graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
				graphics.fill(shape);
			}
		}
	}

	private void drawHeatingActionOverlay(Graphics2D graphics, GameObject gameObject)
	{
		int actionsLeft = state.getActionsLeftInStage();
		int heatLeft = state.getActionsForHeatLevel();

		// Draw heat left
		if (config.drawHeatLeftOverlay())
		{
			String text = "Heat left: " + heatLeft;
			LocalPoint textLocation = gameObject.getLocalLocation();
			textLocation = new LocalPoint(textLocation.getX(), textLocation.getY());
			Point canvasLocation = Perspective.getCanvasTextLocation(client, graphics, textLocation, text, 250);
			OverlayUtil.renderTextLocation(graphics, canvasLocation, text, getHeatColor(actionsLeft, heatLeft));
		}
		if (config.drawActionLeftOverlay())
		// Draw actions left
		{
			String text = "Actions left: " + actionsLeft;
			LocalPoint textLocation = gameObject.getLocalLocation();
			textLocation = new LocalPoint(textLocation.getX(), textLocation.getY());
			Point canvasLocation = Perspective.getCanvasTextLocation(client, graphics, textLocation, text, 250);
			canvasLocation = new Point(canvasLocation.getX(), canvasLocation.getY() + 10);
			OverlayUtil.renderTextLocation(graphics, canvasLocation, text, getHeatColor(actionsLeft, heatLeft));
		}
	}
}
