package me.apika.stencil.gui;

import me.apika.stencil.config.ConfigOption;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Shows "true" in green or "false" in red, and flips on click. */
public class BooleanWidget extends StencilWidget
{
	private static final int COLOR_TRUE = 0xFF55FF55;
	private static final int COLOR_FALSE = 0xFFFF5555;

	private final ConfigOption.Bool option;

	public BooleanWidget(int x, int y, int width, int height, ConfigOption.Bool option)
	{
		super(x, y, width, height, Component.literal(option.getName()));
		this.option = option;
	}

	@Override
	protected String getLabel()
	{
		return String.valueOf(this.option.get());
	}

	@Override
	protected int getLabelColor()
	{
		if (this.active == false)
		{
			return COLOR_TEXT_INACTIVE;
		}

		return this.option.get() ? COLOR_TRUE : COLOR_FALSE;
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick)
	{
		this.option.toggle();
	}
}
