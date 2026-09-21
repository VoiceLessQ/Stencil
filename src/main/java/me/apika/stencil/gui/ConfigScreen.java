package me.apika.stencil.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.config.ConfigStorage;
import me.apika.stencil.config.Configs;
import me.apika.stencil.config.Hotkeys;
import me.apika.stencil.input.Keybind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

/**
 * The settings screen: a row of category tabs, then one row per option with
 * its name on the left, an editor in the middle and a reset button on the
 * right. Values apply as they are changed and the file is written on close.
 * Options for features that do not exist yet are greyed out.
 */
public class ConfigScreen extends Screen
{
	private static final int COLOR_BACKGROUND = 0xE0101010;
	private static final int COLOR_TITLE = 0xFFFFFFFF;
	private static final int COLOR_LABEL = 0xFFE0E0E0;
	private static final int COLOR_LABEL_UNIMPLEMENTED = 0xFF707070;
	private static final int COLOR_LABEL_MODIFIED = 0xFFFFFF80;
	private static final int COLOR_TAB_SELECTED = 0xFFFFAA00;
	private static final int COLOR_SWATCH_BORDER = 0xFFFFFFFF;

	private static final int MARGIN = 10;
	private static final int TAB_Y = 22;
	private static final int TAB_HEIGHT = 20;
	private static final int ROW_HEIGHT = 22;
	private static final int WIDGET_HEIGHT = 20;
	private static final int RESET_WIDTH = 44;
	private static final int SWATCH_WIDTH = 16;

	private static final Keybind OPEN_KEY = new Keybind(Hotkeys.OPEN_GUI_SETTINGS);
	private static Tab lastTab = Tab.GENERIC;

	private enum Tab
	{
		GENERIC("Generic", Configs.Generic.OPTIONS),
		VISUALS("Visuals", Configs.Visuals.OPTIONS),
		INFO_OVERLAYS("Info Overlays", Configs.InfoOverlays.OPTIONS),
		COLORS("Colors", Configs.Colors.OPTIONS),
		HOTKEYS("Hotkeys", Hotkeys.HOTKEY_LIST);

		private final String displayName;
		private final List<ConfigOption<?>> options;

		Tab(String displayName, List<ConfigOption<?>> options)
		{
			this.displayName = displayName;
			this.options = options;
		}
	}

	/** One option's line: the editor, its reset button and where the label goes. */
	private record Row(ConfigOption<?> option, AbstractWidget editor, TextButton reset, int baseY) { }

	private final List<Row> rows = new ArrayList<>();
	private Tab tab = lastTab;
	private int listTop;
	private int listBottom;
	private int scroll;
	private int contentHeight;

	public ConfigScreen()
	{
		super(Component.literal("Stencil Configs"));
	}

	/** Polled every client tick from the mod initializer; opens the screen on its hotkey. */
	public static void tickHotkey(Minecraft mc)
	{
		OPEN_KEY.tick();

		if (OPEN_KEY.wasTriggered() && mc.gui.screen() == null && mc.player != null)
		{
			mc.gui.setScreen(new ConfigScreen());
		}
	}

	@Override
	protected void init()
	{
		int x = MARGIN;

		for (Tab candidate : Tab.values())
		{
			int width = this.font.width(candidate.displayName) + 12;
			TextButton button = new TextButton(x, TAB_Y, width, TAB_HEIGHT, candidate.displayName, () -> this.switchTab(candidate));

			if (candidate == this.tab)
			{
				button.setTextColor(COLOR_TAB_SELECTED);
				button.active = false;
			}

			this.addRenderableWidget(button);
			x += width + 2;
		}

		this.listTop = TAB_Y + TAB_HEIGHT + 6;
		this.listBottom = this.height - MARGIN;
		this.buildRows();
	}

	private void switchTab(Tab tab)
	{
		this.stopCapture();
		this.tab = tab;
		lastTab = tab;
		this.scroll = 0;
		this.rebuildWidgets();
	}

	private void buildRows()
	{
		this.rows.clear();

		int labelWidth = 0;

		for (ConfigOption<?> option : this.tab.options)
		{
			labelWidth = Math.max(labelWidth, this.font.width(option.getName()));
		}

		int editorX = MARGIN + labelWidth + 10;
		int editorWidth = this.tab == Tab.HOTKEYS ? 160 : 120;
		int y = this.listTop;

		for (ConfigOption<?> option : this.tab.options)
		{
			AbstractWidget editor = this.createEditor(option, editorX, y, editorWidth);
			TextButton reset = new TextButton(editorX + editorWidth + 4, y, RESET_WIDTH, WIDGET_HEIGHT, "Reset", () ->
			{
				option.resetToDefault();
				this.rebuildWidgets();
			});

			if (Configs.isImplemented(option) == false)
			{
				editor.active = false;
				reset.active = false;
				editor.setTooltip(Tooltip.create(Component.literal("Not implemented yet")));
			}
			else
			{
				String commentKey = option.getCommentKey();

				if (Language.getInstance().has(commentKey))
				{
					editor.setTooltip(Tooltip.create(Component.translatable(commentKey)));
				}
			}

			this.addWidget(editor);
			this.addWidget(reset);
			this.rows.add(new Row(option, editor, reset, y));
			y += ROW_HEIGHT;
		}

		this.contentHeight = y - this.listTop;
		this.scroll = Math.clamp(this.scroll, 0, this.getMaxScroll());
		this.layoutRows();
	}

	private AbstractWidget createEditor(ConfigOption<?> option, int x, int y, int width)
	{
		if (option instanceof ConfigOption.Bool bool)
		{
			return new BooleanWidget(x, y, width, WIDGET_HEIGHT, bool);
		}

		if (option instanceof ConfigOption.OptionList<?> list)
		{
			return new CycleWidget(x, y, width, WIDGET_HEIGHT, list);
		}

		if (option instanceof ConfigOption.Hotkey hotkey)
		{
			return new HotkeyWidget(x, y, width, WIDGET_HEIGHT, hotkey);
		}

		EditBox box = new EditBox(this.font, x, y, width, WIDGET_HEIGHT, Component.literal(option.getName()));
		box.setMaxLength(256);

		if (option instanceof ConfigOption.Int intOption)
		{
			box.setValue(String.valueOf(intOption.get()));
			box.setResponder(text -> parseInt(text).ifPresent(intOption::setValue));
		}
		else if (option instanceof ConfigOption.Dbl dbl)
		{
			box.setValue(String.valueOf(dbl.get()));
			box.setResponder(text -> parseDouble(text).ifPresent(dbl::setValue));
		}
		else if (option instanceof ConfigOption.Color color)
		{
			box.setValue(color.getAsString());
			box.setResponder(text -> color.setValue(ConfigOption.Color.parse(text)));
		}
		else if (option instanceof ConfigOption.Str str)
		{
			box.setValue(str.get());
			box.setResponder(str::setValue);
		}

		return box;
	}

	private static OptionalInt parseInt(String text)
	{
		try
		{
			return OptionalInt.of(Integer.parseInt(text.trim()));
		}
		catch (NumberFormatException e)
		{
			return OptionalInt.empty();
		}
	}

	private static OptionalDouble parseDouble(String text)
	{
		try
		{
			return OptionalDouble.of(Double.parseDouble(text.trim()));
		}
		catch (NumberFormatException e)
		{
			return OptionalDouble.empty();
		}
	}

	private int getMaxScroll()
	{
		return Math.max(0, this.contentHeight - (this.listBottom - this.listTop));
	}

	/** Moves every row to its scrolled position and hides the ones fully outside the list. */
	private void layoutRows()
	{
		for (Row row : this.rows)
		{
			int y = row.baseY() - this.scroll;
			boolean visible = y + WIDGET_HEIGHT > this.listTop && y < this.listBottom;

			row.editor().setY(y);
			row.reset().setY(y);
			row.editor().visible = visible;
			row.reset().visible = visible;
			row.reset().active = visible && row.option().isModified() && Configs.isImplemented(row.option());
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
	{
		graphics.fill(0, 0, this.width, this.height, COLOR_BACKGROUND);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
	{
		this.layoutRows();
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		graphics.text(this.font, this.title.getString(), MARGIN, MARGIN, COLOR_TITLE);

		graphics.enableScissor(0, this.listTop, this.width, this.listBottom);

		for (Row row : this.rows)
		{
			if (row.editor().visible == false)
			{
				continue;
			}

			int y = row.editor().getY();
			int color = Configs.isImplemented(row.option()) == false ? COLOR_LABEL_UNIMPLEMENTED
					: row.option().isModified() ? COLOR_LABEL_MODIFIED : COLOR_LABEL;
			graphics.text(this.font, row.option().getName(), MARGIN, y + (WIDGET_HEIGHT - this.font.lineHeight) / 2 + 1, color);

			row.editor().extractRenderState(graphics, mouseX, mouseY, partialTick);
			row.reset().extractRenderState(graphics, mouseX, mouseY, partialTick);

			if (row.option() instanceof ConfigOption.Color colorOption)
			{
				int x = row.reset().getRight() + 4;
				graphics.fill(x, y, x + SWATCH_WIDTH, y + WIDGET_HEIGHT, COLOR_SWATCH_BORDER);
				graphics.fill(x + 1, y + 1, x + SWATCH_WIDTH - 1, y + WIDGET_HEIGHT - 1, colorOption.get() | 0xFF000000);
			}
		}

		graphics.disableScissor();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
	{
		if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
		{
			return true;
		}

		if (mouseY >= this.listTop && mouseY < this.listBottom)
		{
			this.scroll = Math.clamp(this.scroll - (int) (scrollY * ROW_HEIGHT), 0, this.getMaxScroll());
			return true;
		}

		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event)
	{
		HotkeyWidget capturing = this.getCapturing();

		if (capturing != null)
		{
			return capturing.keyPressed(event);
		}

		return super.keyPressed(event);
	}

	@Override
	public boolean keyReleased(KeyEvent event)
	{
		HotkeyWidget capturing = this.getCapturing();

		if (capturing != null)
		{
			return capturing.keyReleased(event);
		}

		return super.keyReleased(event);
	}

	private HotkeyWidget getCapturing()
	{
		for (Row row : this.rows)
		{
			if (row.editor() instanceof HotkeyWidget widget && widget.isCapturing())
			{
				return widget;
			}
		}

		return null;
	}

	private void stopCapture()
	{
		HotkeyWidget capturing = this.getCapturing();

		if (capturing != null)
		{
			capturing.stopCapture();
		}
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	@Override
	public void removed()
	{
		this.stopCapture();
		ConfigStorage.save();
	}
}
