package com.fred4106.defaultMinimapZoom;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.applet.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
		name = "Default Minimap Zoom",
		description = "Allows the user to set the default minimap zoom.",
		tags = {"minimap,mini,map,zoom,default"}
)
public class DefaultMinimapZoomPlugin extends Plugin implements MouseListener {

	//Vars are quite heavily cached so could probably just config.configKey(). However, the best practice behavior in plugins is to have a bunch of variables to store the results of the config methods, and check it in startUp/onConfigChanged. It feels redundant, but it's better than hitting the reflective calls every frame. --LlemonDuck
	private static double zoomLevel;
	private static boolean zoomWhenStartingClient;
	private static boolean zoomWhenLogin;
	private static boolean zoomWhenHopping;
	private static boolean zoomWhenRightClick;
	private static boolean currentlyHopping; //Default of boolean = false
	private static boolean loggedInOnce; //Default of boolean = false
	private static Area preprocessedMinimapArea;
	private static Area processedMinimapArea;
	private static Rectangle previousMinimapBounds;
	private static Keybind dragHotkey;
	private static boolean inOverlayManagingMode; //Default of boolean = false
	private static int gameTickDelay = 2;
	private static ComponentListener componentListener;

	@Inject
	private Client client;

	@Inject
	private DefaultMinimapZoomConfig config;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@SuppressWarnings("removal")
	@Inject
	private Applet clientApp;

	@Override
	public void startUp() throws Exception {
		updateConfig();
		dragHotkey = configManager.getConfiguration("runelite", "dragHotkey", Keybind.class);
		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN) {
				loggedInOnce = true;
				if (zoomWhenRightClick) {
					getProcessedMinimapArea(); //If player is still hopping or on red login screen, it'll run getProcessedMinimapArea() in a bit again anyway.
				}
			}
		});
		mouseManager.registerMouseListener(this);
		keyManager.registerKeyListener(hotkeyListener);

		componentListener = new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent componentEvent) {
				//Seems to behave properly when opening/closing sidepanel in resizable mode unlike onCanvasSizeChanged(). Still not ideal since it's still triggers when opening the sidepanel (as expected), but solves that bug for now. Alternatively, switch back to onCanvasSizeChanged and just always delay by a gameTick. Edit: seems since flatlaf that it does not proc when opening the sidepanel anymore. It does still work perfectly, so maybe this even improved it a bit? If this turns out to be problematic at some point, replace with e.g. getting the top frame of client.getCanvas() as you've done in client-resizer.
				if (zoomWhenRightClick && client.getGameState() == GameState.LOGGED_IN) {
					checkIfMinimapChanged();
					gameTickDelay = 0;
				}
			}

			//These methods are unused but required to be present in a ComponentListener implementation
			@Override
			public void componentMoved(ComponentEvent componentEvent) {
			}

			@Override
			public void componentShown(ComponentEvent componentEvent) {
			}

			@Override
			public void componentHidden(ComponentEvent componentEvent) {
			}
		};
		clientApp.addComponentListener(componentListener);
	}

	@Override
	public void shutDown() {
		mouseManager.unregisterMouseListener(this);
		keyManager.unregisterKeyListener(hotkeyListener);
		clientApp.removeComponentListener(componentListener);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		String configGroupChanged = configChanged.getGroup();
		if (configGroupChanged.equals("DefaultMinimapZoom")) {
			updateConfig();
			if (configChanged.getKey().equals("zoomLevel") && client.isMinimapZoom() && client.getGameState() == GameState.LOGGED_IN) {
				client.setMinimapZoom(zoomLevel);
			}
			if (zoomWhenRightClick && configChanged.getKey().equals("zoomWhenRightClick") && client.getGameState() == GameState.LOGGED_IN) {
				getProcessedMinimapArea();
			}
		}
		if (configGroupChanged.equals("runelite") && configChanged.getKey().equals("dragHotkey")) {
			dragHotkey = configManager.getConfiguration("runelite", "dragHotkey", Keybind.class);
		}
	}

	private void updateConfig() {
		zoomLevel = config.zoomLevel().toZoomLevel();
		zoomWhenStartingClient = config.zoomWhenStartingClient();
		zoomWhenLogin = config.zoomWhenLogin();
		zoomWhenHopping = config.zoomWhenHopping();
		zoomWhenRightClick = config.zoomWhenRightClick();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		//Logged in fires when logged in and after hopping. Hopping fires before logged in when hopping, i.e.
		//fresh login: logged in fires, hopping does not
		//hop: hopping fires, later on logged in fires
		//Changing zoom level when GameState == HOPPING doesn't work, needs to be LOGGED_IN
		GameState gameState = gameStateChanged.getGameState();
		if (gameState == GameState.HOPPING) {
			currentlyHopping = true;
		}
		if (gameState == GameState.LOGGED_IN) {
			if (client.isMinimapZoom() &&
					((zoomWhenHopping && currentlyHopping) ||
					(zoomWhenLogin && !currentlyHopping) ||
					(zoomWhenStartingClient && !loggedInOnce))) {
				client.setMinimapZoom(zoomLevel);
			}
			loggedInOnce = true;
			currentlyHopping = false;
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded) { //Widget has not loaded yet while GameState == LOGGED IN, so get area when widget has loaded.
		if (zoomWhenRightClick && widgetLoaded.getGroupId() == InterfaceID.MINIMAP) { //Works for both fixed and the two resizable modes
			getProcessedMinimapArea();
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed) { //Widget area is incorrect on Login Click to Play Screen, so get area when that widget is closed.
		if (zoomWhenRightClick && widgetClosed.getGroupId() == InterfaceID.LOGIN_CLICK_TO_PLAY_SCREEN) {
			getProcessedMinimapArea();
		}
	}

	@Subscribe
	public void onFocusChanged(FocusChanged focusChanged) {
		if (zoomWhenRightClick && inOverlayManagingMode && !focusChanged.isFocused() && client.getGameState() == GameState.LOGGED_IN && client.isMinimapZoom() && client.isResized()) {
			inOverlayManagingMode = false;
			checkIfMinimapChanged();
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick) { //Delay by a GameTick to fix the problem of very quickly resizing the client (sometimes doesn't work, so check again a GameTick later)
		if (gameTickDelay < 2 && zoomWhenRightClick && client.getGameState() == GameState.LOGGED_IN && client.isMinimapZoom()) {
			checkIfMinimapChanged();
		}
		if (gameTickDelay < 2) {
			gameTickDelay++;
		}
	}

	@Override
	public MouseEvent mousePressed(MouseEvent mouseEvent) {
		if (zoomWhenRightClick && client.isMinimapZoom() && mouseEvent.getButton() == 3 && client.getGameState() == GameState.LOGGED_IN
				&& processedMinimapArea != null && processedMinimapArea.contains(mouseEvent.getPoint())) { //If right-clicked on minimap
			client.setMinimapZoom(zoomLevel);
			mouseEvent.consume();
		}
		//Doesn't seem to trigger while dragHotkey is being pressed (while inOverlayManagingMode), since the mouseEvent is probably consumed by OverlayRenderer.java
		//For that reason, let's use workaround by calling checkIfMinimapChanged() on hotkeyReleased() and onFocusChanged() if hotkey is currently down (might not catch hotkeyReleased event due to focus change)
		return mouseEvent;
	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> dragHotkey) {
		@Override
		public void hotkeyPressed() {
			if (zoomWhenRightClick && client.getGameState() == GameState.LOGGED_IN && client.isMinimapZoom() && client.isResized()) {
				inOverlayManagingMode = true;
			}
		}

		@Override
		public void hotkeyReleased() {
			//Account for dragging the minimap. Earlier experimentation with onDraggingWidgetChanged, getDraggedWidget, getDraggedOnWidget, client.isDraggingWidget was unsuccessful.
			if (zoomWhenRightClick && client.getGameState() == GameState.LOGGED_IN && client.isMinimapZoom() && client.isResized()) {
				inOverlayManagingMode = false;
				checkIfMinimapChanged();
			}
		}
	};

	private Widget getMinimapWidget() {
		if (client.isResized()) {
			if (client.getVarbitValue(Varbits.SIDE_PANELS) == 1) {
				return client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_MINIMAP_DRAW_AREA);
			}
			return client.getWidget(ComponentID.RESIZABLE_VIEWPORT_MINIMAP_DRAW_AREA);
		}
		return client.getWidget(ComponentID.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
	}

	private void getProcessedMinimapArea() {
		clientThread.invokeLater(() -> {
			Widget MinimapWidget = getMinimapWidget();
			if (MinimapWidget == null || MinimapWidget.isHidden()) {
				processedMinimapArea = null;
			} else {
				Rectangle minimapBounds = MinimapWidget.getBounds();
				previousMinimapBounds = minimapBounds;
				if (!client.isResized()) {
					//It looks like RL's rightclick area for resetting the zoom is bigger than the Ellipse in fixed mode, so Rectangle2d it is.
					preprocessedMinimapArea = new Area(new Rectangle2D.Double(minimapBounds.getX(), minimapBounds.getY(), minimapBounds.getWidth(), minimapBounds.getHeight()));
					//FIXED MODE: Run energy orb, special attack orb, wiki orb, and compass overlap with the preprocessedMinimapArea in fixed mode.
					//Hp orb, prayer orb, map orb and bonds orb don't overlap in fixed mode.
					Widget energyOrbMinimapWidget = client.getWidget(ComponentID.MINIMAP_TOGGLE_RUN_ORB); //Energy/run orb
					removeOrbArea(energyOrbMinimapWidget);
					Widget specOrbMinimapWidget = client.getWidget(InterfaceID.MINIMAP, 36); //Spec orb
					removeOrbArea(specOrbMinimapWidget);
					Widget specOrbTopMinimapWidget = client.getWidget(InterfaceID.MINIMAP, 37); //To also remove the top edge of the spec orb. The additionally removed part is purely visually (not part of the clickbox)
					removeOrbArea(specOrbTopMinimapWidget);
					//RuneLite's rightclick on minimap seems to cut into the click area from the wiki button a bit.
					//This means that a small part of the wiki button will reset the zoom to the wrong level, but so be it.
					Widget wikiOrbMinimapWidget = Objects.requireNonNull(client.getWidget(ComponentID.MINIMAP_WIKI_BANNER_PARENT)).getChild(0); //Wiki orb
					removeOrbArea(wikiOrbMinimapWidget);
					Widget compassMinimapWidget = client.getWidget(InterfaceID.FIXED_VIEWPORT, 23); //Compass
					removeOrbArea(compassMinimapWidget);
				} else {
					//For the resizable modes however, it looks to be closer to Ellipse2D!
					//There are a couple pixels on the edge of the Ellipse that RL does include but the Ellipse doesn't, but it's a tiny difference and very difficult to click on. Should be acceptable.
					preprocessedMinimapArea = new Area(new Ellipse2D.Double(minimapBounds.getX(), minimapBounds.getY(), minimapBounds.getWidth(), minimapBounds.getHeight()));
					//RESIZABLE CLASSIC (STONES DRAW AREA): since we use Ellipse, only the world map orb overlaps with the preprocessedMinimapArea
					//Hp orb, prayer orb, run energy orb, special attack orb, wiki orb, bond orb, activity tracker orb and compass don't overlap in resizable classic.
					//RESIZABLE MODERN (**NO** STONES DRAW AREA): since we use Ellipse, only the world map orb overlaps with the preprocessedMinimapArea
					//Hp orb, prayer orb, run energy orb, special attack orb, wiki orb, bond orb, activity tracker orb and compass don't overlap in resizable modern.
					Widget worldmapOrbMinimapWidget = client.getWidget(ComponentID.MINIMAP_WORLDMAP_OPTIONS); //World map orb
					removeOrbArea(worldmapOrbMinimapWidget);
				}
				processedMinimapArea = preprocessedMinimapArea;
			}
		});
	}

	private void removeOrbArea(Widget minimapWidget) {
		if (minimapWidget != null && !minimapWidget.isHidden()) {
			Rectangle minimapWidgetBounds = minimapWidget.getBounds();
			//The clickbox of all orbs seems to be rectangular (not an ellipse).
			Area OrbMinimapArea = new Area(new Rectangle2D.Double(minimapWidgetBounds.getX(), minimapWidgetBounds.getY(), minimapWidgetBounds.getWidth(), minimapWidgetBounds.getHeight()));
			preprocessedMinimapArea.subtract(OrbMinimapArea);
		}
	}

	private void checkIfMinimapChanged() {
		clientThread.invokeLater(() -> {
			if (getMinimapWidget() != null) {
				Rectangle currentMinimapBounds = getMinimapWidget().getBounds();
				if (previousMinimapBounds == null || !previousMinimapBounds.equals(currentMinimapBounds)) {
					getProcessedMinimapArea();
				}
			}
		});
	}

	//These methods are unused but required to be present in a MouseListener implementation
	// ------------- Unused MouseListener methods -------------
	@Override
	public MouseEvent mouseDragged(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseMoved(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseClicked(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseEntered(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseExited(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent mouseEvent) {
		return mouseEvent;
	}
	// ------------- End unused MouseListener methods -------------

	@Provides
	DefaultMinimapZoomConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(DefaultMinimapZoomConfig.class);
	}
}