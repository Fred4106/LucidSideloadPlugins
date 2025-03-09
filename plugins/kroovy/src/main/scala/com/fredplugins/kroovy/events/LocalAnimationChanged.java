package com.fredplugins.kroovy.events;
import lombok.Data;
import net.runelite.api.Player;
import org.jetbrains.annotations.NotNull;

@Data
public class LocalAnimationChanged
{

	@NotNull private final Player localPlayer;

}
