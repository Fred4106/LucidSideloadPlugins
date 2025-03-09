package com.fredplugins.kroovy.ui;

import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;

import java.awt.image.BufferedImage;

public interface IconSource
{

	BufferedImage toBufferedImage(ItemManager itemManager, SpriteManager spriteManager);

}