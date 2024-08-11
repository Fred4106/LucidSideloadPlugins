
package com.fredplugins.mta;

import ethanApiPlugin.EthanApiPlugin;
import com.google.inject.Provides;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import com.fredplugins.mta.alchemy.AlchemyRoom;
import com.fredplugins.mta.enchantment.EnchantmentRoom;
import com.fredplugins.mta.graveyard.GraveyardRoom;
import com.fredplugins.mta.telekinetic.TelekineticRoom;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> MTA</html>",
	description = "Show helpful information for the Mage Training Arena minigame",
	tags = {"mta", "magic", "minigame", "overlay", "fred"},
	enabledByDefault = false,
	conflicts = {"Mage Training Arena"}
)
@PluginDependency(EthanApiPlugin.class)
@Singleton
public class FredsMTAPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private AlchemyRoom alchemyRoom;
	@Inject
	private GraveyardRoom graveyardRoom;
	@Inject
	private TelekineticRoom telekineticRoom;
	@Inject
	private EnchantmentRoom enchantmentRoom;

	@Inject
	private EventBus eventBus;
	@Inject
	private MTASceneOverlay sceneOverlay;
	@Inject
	private MTAItemOverlay itemOverlay;

	@Getter(AccessLevel.PROTECTED)
	private MTARoom[] rooms;

	@Provides
	public MTAConfig getConfig(ConfigManager manager)
	{
		return manager.getConfig(MTAConfig.class);
	}

	@Override
	public void startUp()
	{
		overlayManager.add(sceneOverlay);
		overlayManager.add(itemOverlay);

		this.rooms = new MTARoom[]{alchemyRoom, graveyardRoom, telekineticRoom, enchantmentRoom};

		for (MTARoom room : rooms)
		{
			eventBus.register(room);
			KeyListener[] listeners = room.keyListeners();
			for (KeyListener l : listeners) {
				keyManager.registerKeyListener(l);
			}
		}
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(sceneOverlay);
		overlayManager.remove(itemOverlay);

		for (MTARoom room : rooms)
		{
			eventBus.unregister(room);
			KeyListener[] listeners = room.keyListeners();
			for (KeyListener l : listeners) {
				keyManager.unregisterKeyListener(l);
			}
		}

		telekineticRoom.resetRoom();
	}

}
