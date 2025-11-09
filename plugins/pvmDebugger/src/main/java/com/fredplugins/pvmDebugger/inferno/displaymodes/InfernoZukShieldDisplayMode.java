package com.fredplugins.pvmDebugger.inferno.displaymodes;

import lombok.AccessLevel;
import lombok.Getter;

@Getter
public enum InfernoZukShieldDisplayMode
{
	OFF("Off"),
	LIVE("Live (follow shield)"),
	PREDICT("Predict"),
	LIVEPLUSPREDICT("Live and Predict");

	final private String name;

	InfernoZukShieldDisplayMode(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
