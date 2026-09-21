package me.apika.stencil.gui;

import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.config.ConfigOptionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Cycles an enum option: left click forward, right click backward. */
public class CycleWidget extends StencilWidget
{
	private final ConfigOption.OptionList<?> option;

	public CycleWidget(int x, int y, int width, int height, ConfigOption.OptionList<?> option)
	{
		super(x, y, width, height, Component.literal(option.getName()));
		this.option = option;
	}

	@Override
	protected String getLabel()
	{
		ConfigOptionValue value = this.option.get();
		return Component.translatableWithFallback(value.getTranslationKey(), value.getConfigString()).getString();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
	{
		if (this.active == false || this.visible == false || this.isMouseOver(event.x(), event.y()) == false)
		{
			return false;
		}

		if (event.button() == 0)
		{
			this.option.cycle(true);
		}
		else if (event.button() == 1)
		{
			this.option.cycle(false);
		}
		else
		{
			return false;
		}

		this.playDownSound(Minecraft.getInstance().getSoundManager());
		return true;
	}
}
