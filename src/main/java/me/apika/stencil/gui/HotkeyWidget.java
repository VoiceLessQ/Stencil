package me.apika.stencil.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.config.Hotkeys;
import me.apika.stencil.input.Keybind;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Shows the key combination of a hotkey. Click it, then press the keys or
 * mouse buttons you want (they are collected in order as they go down), and
 * releasing any of them ends the capture. While capturing, Escape cancels and
 * keeps the old binding, Backspace or Delete clears it to NONE. A binding
 * shared with another hotkey is shown in red, since only one of them would
 * ever fire.
 */
public class HotkeyWidget extends StencilWidget
{
	private static final int COLOR_CAPTURING = 0xFFFFAA00;
	private static final int COLOR_CONFLICT = 0xFFFF5555;

	private final ConfigOption.Hotkey option;
	private final List<String> captured = new ArrayList<>();
	private boolean capturing;

	public HotkeyWidget(int x, int y, int width, int height, ConfigOption.Hotkey option)
	{
		super(x, y, width, height, Component.literal(option.getName()));
		this.option = option;
		this.updateConflict();
	}

	private void updateConflict()
	{
		ConfigOption.Hotkey conflict = Hotkeys.findConflict(this.option);
		this.setTooltip(conflict != null ? Tooltip.create(Component.literal("Conflicts with " + conflict.getName())) : null);
	}

	public boolean isCapturing()
	{
		return this.capturing;
	}

	@Override
	protected String getLabel()
	{
		String keys = this.capturing ? String.join(",", this.captured) : this.option.getKeysAsString();

		if (keys.isEmpty())
		{
			keys = this.capturing ? "..." : "NONE";
		}

		return this.capturing ? "> " + keys + " <" : keys;
	}

	@Override
	protected int getLabelColor()
	{
		if (this.capturing)
		{
			return COLOR_CAPTURING;
		}

		return Hotkeys.findConflict(this.option) != null ? COLOR_CONFLICT : super.getLabelColor();
	}

	/** While capturing, every button anywhere on the screen is part of the combination. */
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
	{
		if (this.capturing)
		{
			this.add(Keybind.fromVanillaName(InputConstants.Type.MOUSE.getOrCreate(event.button()).getName()));
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event)
	{
		if (this.capturing)
		{
			this.finishIfCaptured();
			return true;
		}

		return super.mouseReleased(event);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick)
	{
		this.captured.clear();
		this.capturing = true;
	}

	@Override
	public boolean keyPressed(KeyEvent event)
	{
		if (this.capturing == false)
		{
			return false;
		}

		int key = event.key();

		if (key == InputConstants.KEY_ESCAPE)
		{
			this.cancel();
			return true;
		}

		if (this.captured.isEmpty() && (key == InputConstants.KEY_BACKSPACE || key == InputConstants.KEY_DELETE))
		{
			this.capturing = false;
			this.option.setValue("");
			this.updateConflict();
			return true;
		}

		this.add(Keybind.fromVanillaName(InputConstants.getKey(event).getName()));
		return true;
	}

	@Override
	public boolean keyReleased(KeyEvent event)
	{
		if (this.capturing == false)
		{
			return false;
		}

		this.finishIfCaptured();
		return true;
	}

	private void add(String name)
	{
		if (this.captured.contains(name) == false)
		{
			this.captured.add(name);
		}
	}

	private void finishIfCaptured()
	{
		if (this.captured.isEmpty() == false)
		{
			this.stopCapture();
		}
	}

	private void cancel()
	{
		this.captured.clear();
		this.capturing = false;
	}

	/** Ends the capture: keeps what was pressed, or the old binding if nothing was. Also used when the screen closes. */
	public void stopCapture()
	{
		if (this.capturing == false)
		{
			return;
		}

		this.capturing = false;

		if (this.captured.isEmpty() == false)
		{
			this.option.setValue(String.join(",", this.captured));
			this.updateConflict();
		}
	}
}
