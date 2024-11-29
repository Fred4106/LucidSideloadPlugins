/*
 * Copyright (c) 2018 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.fredplugins.teleportMaps;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import lombok.Getter;
import net.runelite.client.config.Keybind;
import net.runelite.client.ui.FontManager;

class MultikeybindButton extends JButton
{
	@Getter
	private Multikeybind value;
	private boolean fresh = true;

	public MultikeybindButton(Multikeybind value)
	{
		this.value = value;

		setFont(FontManager.getDefaultFont().deriveFont(12.f));
		update();
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				// We have to use a mouse adapter instead of an action listener so the press action key (space) can be bound
				MultikeybindButton.this.value = new Multikeybind();
				update();
			}
		});

		addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				Multikeybind v = MultikeybindButton.this.value;
				if (fresh)
				{
					v = new Multikeybind();
					fresh = false;
				}
				Keybind newBind = new Keybind(e);

				// prevent modifier only multi key binds
				if (v.getKeybinds().size() < 1 || newBind.getKeyCode() != KeyEvent.VK_UNDEFINED)
				{
					v = v.with(newBind);
				}
				if (v.getKeybinds().size() == 2 && v.getKeybinds().get(0).getKeyCode() == KeyEvent.VK_UNDEFINED)
				{
					v = new Multikeybind(v.getKeybinds().get(1));
				}

				MultikeybindButton.this.value = v;
				update();
			}
		});
	}

	private void update()
	{
		setText(value.toString());
	}

	public void setValue(Multikeybind value)
	{
		this.value = value;
		update();
	}
}