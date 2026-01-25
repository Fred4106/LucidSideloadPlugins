package com.fredplugins.dynamicHighlights;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.runelite.client.config.ConfigManager;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Supplier;
import static net.runelite.http.api.RuneLiteAPI.GSON;

public class ConfigAdapter<E> {
//	private final String group;
	private final String key;
	private final TypeToken<E> typeToken;
	private final Supplier<E> defaultE;
	private final FredsDynamicHighlightsPlugin plugin;
//	private final ConfigManager cm;

	private ConfigManager cm() {
		return plugin.getConfigManager();
	}

	private String group() {
		return plugin.CONFIG_GROUP;
	}

	public ConfigAdapter(FredsDynamicHighlightsPlugin parent, String key, TypeToken<E> typeToken, Supplier<E> defaultE) {
		this.plugin = parent;
//		this.group = group;
		this.key = key;
		this.typeToken = typeToken;
		this.defaultE = defaultE;
	}

	public boolean save(E e) {
		Type tpe = typeToken.getType();
		String jsonStr = null;
		if(e != null) {
			try {
				jsonStr = GSON.toJson(e, tpe);
			} catch (RuntimeException ex) {
				jsonStr="";
			} finally {
				if(jsonStr.isEmpty()) jsonStr = null;
			}
		}
		E def = defaultE.get();
		if(jsonStr == null && def != null) {
			try {
				jsonStr = GSON.toJson(def, tpe);
			} catch (RuntimeException ex) {
				jsonStr="";
			}finally {
				if(jsonStr.isEmpty()) jsonStr = null;
			}
		}

		if(jsonStr != null) {
			cm().setConfiguration(group(),key, jsonStr);
		} else {
			cm().unsetConfiguration(group(), key);
		}
		return cm().getConfigurationKeys(group()).stream().map(s -> {System.out.println("  "+s); return s;}).anyMatch(k -> k.equals(group()+"."+key));
	}

	public E load() {
		String rawE = cm().getConfiguration(group(), key);
		E toReturn = null;
		if(rawE != null && !rawE.isEmpty()) {
			try {
				toReturn = GSON.fromJson(rawE, typeToken.getType());
			} catch (JsonSyntaxException e) {
				cm().unsetConfiguration(group(), key);
			}
		}
		return Optional.ofNullable(toReturn).orElseGet(defaultE);
	}

	@Override
	public String toString() {
		return "ConfigAdapter<"+typeToken.toString()+">("+group()+", "+key+")";
	}
}
