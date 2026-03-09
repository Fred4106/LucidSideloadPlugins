package ethanApiPlugin.services.localPlayer;


import com.fredplugins.common.utils.TWorldPoint;
import com.fredplugins.common.utils.WorldPointUtils;
import com.fredplugins.common.extensions.ActorExtensions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ethanApiPlugin.EthanApiPlugin;
import ethanApiPlugin.services.localPlayer.events.ActorPositionChanged;
import ethanApiPlugin.services.localPlayer.events.LocalAnimationChanged;
import ethanApiPlugin.services.localPlayer.events.LocalDestinationChanged;
import ethanApiPlugin.services.localPlayer.events.LocalInteractingChanged;
import ethanApiPlugin.services.localPlayer.events.LocalPositionChanged;
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged;
import ethanApiPlugin.utility.WorldPointUtility;
import jdk.jfr.Event;
import lombok.Data;
import lombok.Value;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Slf4j
public class LocalPlayerService {
	private int pRegionId;
	private int pAnimation;
	private Actor pInteracting;
	private WorldPoint wpDest;
	private WorldPoint wpPos;

	private Map<Actor, ActorData> actorMap =Map.<Actor, ActorData>of();


	@Data
	private class ActorData {
		final Actor actor;
		WorldPoint lastPosition;
	}

//	@Inject private Client client = null;
//	@Inject private ClientThread clientThread = null;
//	@Inject private EventBus eventBus = null;//plugin.getInjector.getInstance(classOf[ModelOutlineRenderer])

	Client client = RuneLite.getInjector().getInstance(Client.class);
	EventBus eventBus = RuneLite.getInjector().getInstance(EventBus.class);

	@Inject
	public LocalPlayerService() {}

	public void reset() {
		actorMap =Map.<Actor, ActorData>of();

		this.pRegionId = -1;
		this.pAnimation = -1;
		this.pInteracting = null;
		this.wpDest = null;
		this.wpPos = null;
	}

	public int getCurrentRegionID() {
		return pRegionId;
	}

	Optional<ActorPositionChanged> updateData(ActorData data) {
		WorldPoint old = data.getLastPosition();
		WorldPoint n  = TWorldPoint.get(data.getActor().getWorldLocation());
		data.setLastPosition(n);
		if(isDifferent(old, n)) {
			return Optional.of(new ActorPositionChanged(data.getActor(), old, n));
		}
		return Optional.empty();
	}

	@Subscribe(priority = 200)
	public void onGameTick(GameTick gt) {
		List<ActorData> newMap = client.getPlayers().stream()
			.map(p -> {
				ActorData d = actorMap.get(p);
				if(d == null) {
					d = new ActorData(p);
					d.setLastPosition(
						TWorldPoint.get(p.getWorldLocation())
					);
				}
				return d;
			}).collect(Collectors.toList());//.collect(Collectors.toMap(d -> d.getActor(), d -> d);


		List<ActorData> newNpcMap = client.getNpcs().stream()
			.map(n -> {
				ActorData d = actorMap.get(n);
				if(d == null) {
					d = new ActorData(n);
					d.setLastPosition(
						TWorldPoint.get(n.getWorldLocation())
					);
				}
				return d;
			}).collect(Collectors.toList());//.collect(Collectors.toMap(d -> d.getActor(), d -> d);

		List<ActorData> finalNewMap = Stream.<ActorData>concat(newMap.stream(), newNpcMap.stream()).collect(Collectors.toList());

		List<ActorPositionChanged> eventtsList = finalNewMap.stream().flatMap(d -> updateData(d).stream()).collect(Collectors.toList());
		for (ActorPositionChanged e : eventtsList) {
			eventBus.post(e);
		}
		actorMap = finalNewMap.stream().collect(Collectors.toMap(d -> d.getActor(), d -> d));

		Player me = EthanApiPlugin.getClient().getLocalPlayer();
		WorldPoint pos = Optional.ofNullable(me).map(Player::getWorldLocation)
			.map(TWorldPoint::get)//toTemplate(u, me.getWorldView(), client))
			.orElse(null);
		WorldPoint dest = Optional.ofNullable(client.getLocalDestinationLocation())
			.map(ldest -> TWorldPoint.get(WorldPoint.fromLocalInstance(client, ldest)))//WorldPointUtils.toTemplate(WorldPoint.fromLocalInstance(client, ldest), client.getWorldView(ldest.getWorldView()), client))
			.orElse(null);

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