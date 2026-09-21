package me.apika.stencil.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * A flat button in the style of the classic config screens: black box, grey
 * border that turns white on hover, text centered. Subclasses decide what the
 * text says and what a click does.
 */
public abstract class StencilWidget extends AbstractWidget
{
	protected static final int COLOR_BACKGROUND = 0xFF000000;
	protected static final int COLOR_BORDER = 0xFFA0A0A0;
	protected static final int COLOR_BORDER_HOVER = 0xFFFFFFFF;
	protected static final int COLOR_TEXT = 0xFFE0E0E0;
	protected static final int COLOR_TEXT_INACTIVE = 0xFF808080;

	protected StencilWidget(int x, int y, int width, int height, Component message)
	{
		super(x, y, width, height, message);
	}

	/** The text drawn in the box; called every frame so it follows the value. */
	protected abstract String getLabel();

	protected int getLabelColor()
	{
		return this.active ? COLOR_TEXT : COLOR_TEXT_INACTIVE;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
	{
		int x1 = this.getX();
		int y1 = this.getY();
		int x2 = x1 + this.width;
		int y2 = y1 + this.height;
		int border = this.active && this.isHoveredOrFocused() ? COLOR_BORDER_HOVER : COLOR_BORDER;

		graphics.fill(x1, y1, x2, y2, border);
		graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, COLOR_BACKGROUND);

		var font = Minecraft.getInstance().font;
		String label = font.plainSubstrByWidth(this.getLabel(), this.width - 6);
		int textX = x1 + (this.width - font.width(label)) / 2;
		int textY = y1 + (this.height - font.lineHeight) / 2 + 1;
		graphics.text(font, label, textX, textY, this.getLabelColor());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output)
	{
		this.defaultButtonNarrationText(output);
	}
}
