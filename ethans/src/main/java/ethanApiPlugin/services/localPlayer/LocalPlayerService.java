package ethanApiPlugin.services.localPlayer;


import com.fredplugins.common.utils.WorldPointUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ethanApiPlugin.EthanApiPlugin;
import ethanApiPlugin.services.localPlayer.events.LocalAnimationChanged;
import ethanApiPlugin.services.localPlayer.events.LocalDestinationChanged;
import ethanApiPlugin.services.localPlayer.events.LocalInteractingChanged;
import ethanApiPlugin.services.localPlayer.events.LocalPositionChanged;
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged;
import ethanApiPlugin.utility.WorldPointUtility;
import jdk.jfr.Event;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import java.util.Optional;

@Singleton
@Slf4j
public class LocalPlayerService {
	private int pRegionId;
	private int pAnimation;
	private Actor pInteracting;
	private WorldPoint wpDest;
	private WorldPoint wpPos;


//	@Inject private Client client = null;
//	@Inject private ClientThread clientThread = null;
//	@Inject private EventBus eventBus = null;//plugin.getInjector.getInstance(classOf[ModelOutlineRenderer])

	Client client = RuneLite.getInjector().getInstance(Client.class);
	EventBus eventBus = RuneLite.getInjector().getInstance(EventBus.class);

	@Inject
	public LocalPlayerService() {}

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

	@Subscribe
	public void onGameTick(GameTick gt) {
		Player me = EthanApiPlugin.getClient().getLocalPlayer();
		WorldPoint pos = Optional.ofNullable(me).map(Player::getWorldLocation).orElse(null);
		WorldPoint dest = Optional.ofNullable(client.getLocalDestinationLocation()).map(ldest -> WorldPoint.fromLocalInstance(client, ldest)).orElse(null);

		int regionId = Optional.ofNullable(pos).map(WorldPoint::getRegionID).orElse(-1);//.orElse(-1).intValue();
		if (regionId != this.pRegionId) {
			eventBus.post(new LocalRegionChanged(this.pRegionId, regionId));
		}

		if(isDifferent(pos, this.wpPos)) {
			eventBus.post(new LocalPositionChanged(this.wpPos, pos));
		}

		if(isDifferent(dest, this.wpDest)) {
			eventBus.post(new LocalDestinationChanged(this.wpDest, dest));
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
		wpDest = dest;
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