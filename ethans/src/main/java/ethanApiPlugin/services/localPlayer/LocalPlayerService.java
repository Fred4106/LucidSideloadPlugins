package ethanApiPlugin.services.localPlayer;

//import com.github.calebwhiting.runelite.api.event.*
import ch.qos.logback.classic.Level;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ethanApiPlugin.EthanApiPlugin;
import ethanApiPlugin.services.localPlayer.events.LocalAnimationChanged;
import ethanApiPlugin.services.localPlayer.events.LocalInteractingChanged;
import ethanApiPlugin.services.localPlayer.events.LocalPositionChanged;
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.ui.ClientUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class LocalPlayerService {
	private final static Logger log;
	static {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LocalPlayerService.class)).setLevel(Level.DEBUG);
		log = LoggerFactory.getLogger(LocalPlayerService.class);
	}

	private int pRegionId;
	private int pAnimation;
	private Actor pInteracting;
	private WorldPoint wpDest;
	private WorldPoint wpPos;

	@Inject
	public LocalPlayerService() {
		reset();
	}

	public void reset() {
		this.pRegionId = -1;
		this.pAnimation = -1;
		this.pInteracting = null;
		this.wpDest = null;
		this.wpPos = null;
	}

	public int getCurrentRegionID() {
		return pRegionId;
	}

	public void onGameTick(Client client, EventBus eventBus) {
		Player me = client.getLocalPlayer();
		WorldPoint pos = Optional.ofNullable(me).map(Player::getWorldLocation).orElse(null);

		int regionId = Optional.ofNullable(pos).map(WorldPoint::getRegionID).orElse(-1);//.orElse(-1).intValue();
		if (regionId != this.pRegionId) {
			eventBus.post(new LocalRegionChanged(this.pRegionId, this.pRegionId));
		}

		if(isDifferent(pos, this.wpPos)) {
			eventBus.post(new LocalPositionChanged(this.wpPos, pos));
		}

		Actor curInteracting = Optional.ofNullable(me).map(Player::getInteracting).orElse(null);
		if(pInteracting != curInteracting){
			eventBus.post(new LocalInteractingChanged(pInteracting, curInteracting));
		}

		int curAnimation = Optional.ofNullable(me).map(Player::getAnimation).orElse(-1);//me.getAnimation();
		if(pAnimation != curAnimation){
			eventBus.post(new LocalAnimationChanged(pAnimation, curAnimation));
		}

		pRegionId = regionId;
		wpPos = pos;
		pInteracting = curInteracting;
		pAnimation = curAnimation;
	}

	private boolean isDifferent(LocalPoint first, LocalPoint second)
	{
		if (first == second) {
			return false;
		}
		return first == null || second == null || first.distanceTo(second) > 0;
	}
	private boolean isDifferent(WorldPoint first, WorldPoint second)
	{
		if (first == second) {
			return false;
		}
		return first == null || second == null || first.distanceTo(second) > 0;
	}
}