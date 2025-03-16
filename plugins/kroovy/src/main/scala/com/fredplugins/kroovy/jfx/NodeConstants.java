package com.fredplugins.kroovy.jfx;

import com.google.common.collect.ImmutableList;
import static com.fredplugins.kroovy.jfx.NodeConstants.NodeScript.ON_DISABLED;
import static com.fredplugins.kroovy.jfx.NodeConstants.NodeScript.ON_PRESS;

public class NodeConstants {
	public enum NodeScript
	{
		ON_ENABLED("onEnabled"),
		ON_DISABLED("onDisabled"),
		ON_PRESS("onPress"),
		ON_RELEASE("onRelease"),
		ON_EVENT("onTrigger");

		final String ident;

		NodeScript(String ident)
		{
			this.ident = ident;
		}
	}

	public enum NodeType {
		HOT_KEY(ImmutableList.of(NodeScript.ON_ENABLED, ON_DISABLED, ON_PRESS, NodeScript.ON_RELEASE)),
		GAME_EVENT(ImmutableList.of(NodeScript.ON_ENABLED, ON_DISABLED, NodeScript.ON_EVENT));

		final ImmutableList<NodeScript> legalScripts;
		NodeType(ImmutableList<NodeScript> scripts) {
			this.legalScripts = scripts;
		}
		public boolean isLegal(NodeScript s) {
			return legalScripts.contains(s);
		}
	}
}
