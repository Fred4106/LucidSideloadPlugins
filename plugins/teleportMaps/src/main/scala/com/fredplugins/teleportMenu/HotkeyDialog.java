package com.fredplugins.teleportMenu;

import lombok.Getter;
import net.runelite.client.config.Keybind;
import net.runelite.client.ui.FontManager;

import java.awt.BorderLayout;
import java.awt.MouseInfo;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class HotkeyDialog extends JDialog
{
	static class MultikeybindButton extends JButton
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
	public HotkeyDialog(Window owner, String titleText, Multikeybind defaul, Multikeybind current, Consumer<Multikeybind> done)
	{
		super(owner, ModalityType.APPLICATION_MODAL);
		setTitle("Set hotkey");

		JPanel pane = new JPanel();
		GroupLayout gl = new GroupLayout(pane);
		pane.setLayout(gl);

		JLabel title = new JLabel(titleText);
		MultikeybindButton hotkeyBtn = new MultikeybindButton(current);

		JButton defaultBtn = new JButton("Default");
		defaultBtn.addActionListener(ev ->
		{
			hotkeyBtn.setValue(defaul);
		});

		JButton ok = new JButton("Ok");
		ok.addActionListener(ev ->
		{
			done.accept(hotkeyBtn.getValue());
			this.setVisible(false);
			this.dispose();
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(ev ->
		{
			this.setVisible(false);
			this.dispose();
		});

		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		gl.setVerticalGroup(gl.createSequentialGroup()
			.addComponent(true, title)
				.addGroup(gl.createParallelGroup()
					.addComponent(hotkeyBtn)
					.addComponent(defaultBtn))
			.addGroup(gl.createParallelGroup()
				.addComponent(ok)
				.addComponent(cancel)));
		gl.setHorizontalGroup(gl.createParallelGroup()
			.addComponent(title)
			.addGroup(gl.createSequentialGroup()
				.addComponent(hotkeyBtn)
				.addGap(12)
				.addComponent(defaultBtn))
			.addGroup(gl.createSequentialGroup()
				.addGap(0)
				.addComponent(ok)
				.addComponent(cancel)));

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(pane, BorderLayout.CENTER);

		pack();
		setLocation(MouseInfo.getPointerInfo().getLocation());
		setVisible(true);
	}
}
