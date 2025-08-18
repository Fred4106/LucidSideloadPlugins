package com.fredplugins.common.magic.events;

import com.fredplugins.common.magic.RuneChanges;
import com.fredplugins.common.magic.SpellIds;
import lombok.Getter;

@Getter
public class RunesChanged {
	@Override
	public String toString() {
		return "RunesChanged{" +
			"changes=" + changes +
			'}';
	}

	private RuneChanges changes;

    public RunesChanged(RuneChanges changes)
    {
        this.changes = changes;
    }
}