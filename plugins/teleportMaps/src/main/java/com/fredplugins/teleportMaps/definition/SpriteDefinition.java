package com.fredplugins.teleportMaps.definition;

import lombok.Getter;
import net.runelite.client.game.SpriteOverride;

@Getter
public class SpriteDefinition implements SpriteOverride
{
	private int spriteId;
	private String fileName;
}