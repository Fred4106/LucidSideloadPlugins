package com.fredplugins.teleportMenu;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.Keybind;

@Slf4j
@EqualsAndHashCode
public class Multikeybind
{
	@Getter
	private final List<Keybind> keybinds;

	public Multikeybind(Keybind... binds)
	{
		keybinds = ImmutableList.copyOf(binds);
	}

	private Multikeybind(ImmutableList<Keybind> keybinds)
	{
		this.keybinds = keybinds;
	}

	@VisibleForTesting
	static int swapNumpadKey(int code)
	{
		if (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9)
		{
			code += KeyEvent.VK_NUMPAD0 - KeyEvent.VK_0;
		}
		else if (code >= KeyEvent.VK_NUMPAD0 && code <= KeyEvent.VK_NUMPAD9)
		{
			code += KeyEvent.VK_0 - KeyEvent.VK_NUMPAD0;
		}
		return code;
	}

	private static boolean matches(Keybind bind, KeyEvent ev, boolean aliasNumpad)
	{
		if (bind.matches(ev))
		{
			return true;
		}

		if (aliasNumpad)
		{
			int key2 = swapNumpadKey(bind.getKeyCode());
			if (key2 != bind.getKeyCode())
			{
				return new Keybind(key2, bind.getModifiers()).matches(ev);
			}
		}

		return false;
	}

	public MatchState matches(List<KeyEvent> evs)
	{
		return matches(evs, false);
	}

	public MatchState matches(List<KeyEvent> evs, boolean aliasNumpad)
	{
		outer:
		for (int ii = 0; ii < evs.size(); ii++)
		{
			for (int ei = ii, ki = 0; ; ki++, ei++)
			{
				if (ei >= evs.size())
				{
					// no more events
					// if we are also out of keybinds this is a perfect match
					if (ki >= keybinds.size())
					{
						return MatchState.YES;
					}

					// otherwise only a prefix
					return MatchState.PARTIAL;
				}

				if (ki >= keybinds.size())
				{
					// more keypresses, will not match unless they are just modifiers
					for (; ei < evs.size(); ei++)
					{
						if (!isModifierOnly(evs.get(ei).getKeyCode()))
						{
							continue outer;
						}
					}
					break;
				}

				KeyEvent ev = evs.get(ei);
				if (!matches(keybinds.get(ki), ev, aliasNumpad))
				{
					if (isModifierOnly(ev.getKeyCode()) && ei != ii)
					{
						ki--;
						continue;
					}
					break;
				}
			}
		}

		return MatchState.NO;
	}

	public static boolean isModifierOnly(int keyCode)
	{
		return keyCode == KeyEvent.VK_UNDEFINED || Keybind.getModifierForKeyCode(keyCode) != null;
	}

	public String toConfig()
	{
		return keybinds.stream()
			.map(k -> k.getKeyCode() + ":" + k.getModifiers())
			.collect(Collectors.joining(":"));
	}

	public static Multikeybind fromConfig(String config)
	{
		ImmutableList.Builder<Keybind> v = ImmutableList.builder();
		if (config == null)
		{
			return null;
		}
		if (!config.isEmpty())
		{
			try
			{
				Iterator<String> bits = Splitter.on(':').split(config).iterator();
				for (; bits.hasNext(); )
				{
					int code = Integer.parseInt(bits.next());
					int mods = Integer.parseInt(bits.next());
					if (code == KeyEvent.VK_UNDEFINED && mods == 0)
					{
						continue;
					}
					v.add(new Keybind(code, mods));
				}
			}
			catch (Exception e)
			{
				log.warn("Malformed Multikeybind", e);
			}
		}

		return new Multikeybind(v.build());
	}

	public boolean isUnset()
	{
		return keybinds.size() == 0;
	}

	public String toString()
	{
		if (keybinds.size() == 0)
		{
			return "Not set";
		}

		return keybinds.stream()
			.map(Keybind::toString)
			.collect(Collectors.joining(" "));
	}

	public Multikeybind with(Keybind... more)
	{
		return new Multikeybind(ImmutableList.<Keybind>builder()
			.addAll(keybinds)
			.add(more)
			.build());
	}

	enum MatchState
	{
		NO,
		PARTIAL,
		YES,
	}
}
