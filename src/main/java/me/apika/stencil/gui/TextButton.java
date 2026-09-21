package me.apika.stencil.gui;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** A plain button with fixed text that runs something when clicked. */
public class TextButton extends StencilWidget
{
	private final Runnable action;
	private int textColor = COLOR_TEXT;

	public TextButton(int x, int y, int width, int height, String text, Runnable action)
	{
		super(x, y, width, height, Component.literal(text));
		this.action = action;
	}

	public TextButton setTextColor(int color)
	{
		this.textColor = color;
		return this;
	}

	@Override
	protected String getLabel()
	{
		return this.getMessage().getString();
	}

	@Override
	protected int getLabelColor()
	{
		return this.active ? this.textColor : COLOR_TEXT_INACTIVE;
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick)
	{
		this.action.run();
	}
}
