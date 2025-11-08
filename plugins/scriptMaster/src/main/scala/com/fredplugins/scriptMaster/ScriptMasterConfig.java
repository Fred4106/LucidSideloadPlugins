package com.fredplugins.scriptMaster;

import com.fredplugins.scriptMaster.ScriptMasterConfig.KEYS;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(KEYS.GROUP)
public interface ScriptMasterConfig extends Config
{
	final class KEYS {
		private KEYS() {}
		final static String GROUP = "scriptmaster";
		final static String SOURCE_CODE = "srcCode";
		final static String COMPILE_SIGNEL = "compile";
		final static String DEBUG_STRING = "debug";
	}

	@ConfigItem(
		keyName = KEYS.SOURCE_CODE,
		name = "Source Code",
		description = "Source to compile and run"
	)
	default String sourceCode()
	{
		return "println(\"Hello World!\");";
	}

	@ConfigItem(
		keyName = KEYS.COMPILE_SIGNEL,
		name = "Compile",
		description = "Triggers the plugin to attempt to compile/run the source code"
	)
	default boolean compileSignal()
	{
		return true;
	}

	@ConfigItem(
		keyName = KEYS.DEBUG_STRING,
		name = "Debug data",
		description = "debug data from plugin"
	)
	default String debugString()
	{
		return "";
	}
}
