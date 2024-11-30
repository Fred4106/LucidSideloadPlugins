package com.fredplugins.teleportMaps.definition;

import lombok.Data;
import lombok.Getter;
import lombok.Value;

@Data
public class HotKeyDefinition
{
	private int x = -1;
	private int y = -1;

	public HotKeyDefinition(int x, int y){
		this.x = x;
		this.y = y;
	}
}
