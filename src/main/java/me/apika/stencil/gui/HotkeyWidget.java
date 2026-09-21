package me.apika.stencil.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.input.Keybind;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Shows the key combination of a hotkey. Click it, press the keys you want
 * (they are collected in order as they go down), and releasing any key ends
 * the capture. Escape while capturing clears the binding.
 */
public class HotkeyWidget extends StencilWidget
{
	private static final int COLOR_CAPTURING = 0xFFFFAA00;

	private final ConfigOption.Hotkey option;
	private final List<String> captured = new ArrayList<>();
	private boolean capturing;

	public HotkeyWidget(int x, int y, int width, int height, ConfigOption.Hotkey option)
	{
		super(x, y, width, height, Component.literal(option.getName()));
		this.option = option;
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
			keys = "NONE";
		}

		return this.capturing ? "> " + keys + " <" : keys;
	}

	@Override
	protected int getLabelColor()
	{
		return this.capturing ? COLOR_CAPTURING : super.getLabelColor();
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick)
	{
		if (this.capturing)
		{
			this.stopCapture();
		}
		else
		{
			this.captured.clear();
			this.capturing = true;
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event)
	{
		if (this.capturing == false)
		{
			return false;
		}

		InputConstants.Key key = InputConstants.getKey(event);

		if (key.getValue() == InputConstants.KEY_ESCAPE)
		{
			this.captured.clear();
			this.stopCapture();
			return true;
		}

		String name = Keybind.fromVanillaName(key.getName());

		if (this.captured.contains(name) == false)
		{
			this.captured.add(name);
		}

		return true;
	}

	@Override
	public boolean keyReleased(KeyEvent event)
	{
		if (this.capturing == false)
		{
			return false;
		}

		if (this.captured.isEmpty() == false)
		{
			this.stopCapture();
		}

		return true;
	}

	/** Ends the capture, keeping whatever was pressed. Also used when the screen closes. */
	public void stopCapture()
	{
		if (this.capturing)
		{
			this.capturing = false;
			this.option.setValue(String.join(",", this.captured));
		}
	}
}
