package me.apika.stencil.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.regex.Pattern;

import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.config.ConfigStorage;
import me.apika.stencil.config.Configs;
import me.apika.stencil.config.Hotkeys;
import me.apika.stencil.input.Keybind;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

/**
 * The settings screen: a row of category tabs, then one row per option with
 * its name on the left, an editor in the middle and a reset button on the
 * right. Values apply as they are changed and the file is written on close.
 * The search box filters every tab's options by name.
 */
public class ConfigScreen extends Screen
{
	private static final int COLOR_BACKGROUND = 0xE0101010;
	private static final int COLOR_TITLE = 0xFFFFFFFF;
	private static final int COLOR_LABEL = 0xFFE0E0E0;
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
	private static final int SEARCH_WIDTH = 120;
	private static final Pattern COLOR_PATTERN = Pattern.compile("#?[0-9a-fA-F]{6}");

	private static final Keybind OPEN_KEY = new Keybind(Hotkeys.OPEN_GUI_SETTINGS);
	private static Tab lastTab = Tab.GENERIC;
	private static String lastSearch = "";

	private enum Tab
	{
		GENERIC("Generic", Configs.Generic.OPTIONS),
		VISUALS("Visuals", Configs.Visuals.OPTIONS),
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
	private String search = lastSearch;
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

		EditBox searchBox = new EditBox(this.font, this.width - MARGIN - SEARCH_WIDTH, TAB_Y, SEARCH_WIDTH, TAB_HEIGHT, Component.literal("Search"));
		searchBox.setHint(Component.literal("Search"));
		searchBox.setValue(this.search);
		searchBox.setResponder(text ->
		{
			if (text.equals(this.search) == false)
			{
				this.search = text;
				lastSearch = text;
				this.scroll = 0;
				this.rebuildRows();
			}
		});
		this.addRenderableWidget(searchBox);

		this.listTop = TAB_Y + TAB_HEIGHT + 6;
		this.listBottom = this.height - MARGIN;
		this.buildRows();
	}

	/** The current tab's options, or every tab's options matching the search text. */
	private List<ConfigOption<?>> getVisibleOptions()
	{
		if (this.search.isBlank())
		{
			return this.tab.options;
		}

		String needle = this.search.trim().toLowerCase(Locale.ROOT);
		List<ConfigOption<?>> matches = new ArrayList<>();

		for (Tab candidate : Tab.values())
		{
			for (ConfigOption<?> option : candidate.options)
			{
				if (option.getName().toLowerCase(Locale.ROOT).contains(needle))
				{
					matches.add(option);
				}
			}
		}

		return matches;
	}

	/** Replaces the option rows without touching the tabs or the search box, so typing keeps its focus. */
	private void rebuildRows()
	{
		this.stopCapture();
		this.commitEditors();

		for (Row row : this.rows)
		{
			this.removeWidget(row.editor());
			this.removeWidget(row.reset());
		}

		this.buildRows();
	}

	private void switchTab(Tab tab)
	{
		this.stopCapture();
		this.clearFocus();
		this.commitEditors();
		this.tab = tab;
		lastTab = tab;
		this.scroll = 0;
		this.rebuildWidgets();
	}

	private void buildRows()
	{
		this.rows.clear();

		List<ConfigOption<?>> options = this.getVisibleOptions();
		int labelWidth = 0;
		boolean hasHotkey = false;

		for (ConfigOption<?> option : options)
		{
			labelWidth = Math.max(labelWidth, this.font.width(option.getName()));
			hasHotkey |= option instanceof ConfigOption.Hotkey;
		}

		int editorX = MARGIN + labelWidth + 10;
		int editorWidth = hasHotkey ? 160 : 120;
		int y = this.listTop;

		for (ConfigOption<?> option : options)
		{
			AbstractWidget editor = this.createEditor(option, editorX, y, editorWidth);
			TextButton reset = new TextButton(editorX + editorWidth + 4, y, RESET_WIDTH, WIDGET_HEIGHT, "Reset", () ->
			{
				option.resetToDefault();
				this.rebuildWidgets();
			});

			String commentKey = option.getCommentKey();

			if (Language.getInstance().has(commentKey))
			{
				editor.setTooltip(Tooltip.create(Component.translatable(commentKey)));
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

		box.setValue(valueText(option));

		// Numbers and colours apply on commit, so a half-typed value is not clamped or rejected mid-edit.
		if (option instanceof ConfigOption.Str str)
		{
			box.setResponder(str::setValue);
		}

		return box;
	}

	/** What a text editor shows for the option's stored value. */
	private static String valueText(ConfigOption<?> option)
	{
		if (option instanceof ConfigOption.Color color)
		{
			return color.getAsString();
		}

		return String.valueOf(option.getValue());
	}

	/** Applies every unfocused number or colour editor, then rewrites it to the stored (clamped, parsed) value. */
	private void commitEditors()
	{
		for (Row row : this.rows)
		{
			if (row.editor() instanceof EditBox box && box.isFocused() == false)
			{
				applyText(row.option(), box.getValue());
				String text = valueText(row.option());

				if (box.getValue().equals(text) == false)
				{
					box.setValue(text);
				}
			}
		}
	}

	private static void applyText(ConfigOption<?> option, String text)
	{
		if (option instanceof ConfigOption.Int intOption)
		{
			parseInt(text).ifPresent(intOption::setValue);
		}
		else if (option instanceof ConfigOption.Dbl dbl)
		{
			parseDouble(text).ifPresent(dbl::setValue);
		}
		else if (option instanceof ConfigOption.Color color && COLOR_PATTERN.matcher(text.trim()).matches())
		{
			color.setValue(ConfigOption.Color.parse(text.trim()));
		}
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
			row.reset().active = visible && row.option().isModified();
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
			int color = row.option().isModified() ? COLOR_LABEL_MODIFIED : COLOR_LABEL;
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
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
	{
		HotkeyWidget capturing = this.getCapturing();

		if (capturing != null)
		{
			return capturing.mouseClicked(event, doubleClick);
		}

		boolean handled = super.mouseClicked(event, doubleClick);

		if (handled == false)
		{
			this.clearFocus();
		}

		this.commitEditors();
		return handled;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event)
	{
		HotkeyWidget capturing = this.getCapturing();

		if (capturing != null)
		{
			return capturing.mouseReleased(event);
		}

		return super.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event)
	{
		HotkeyWidget capturing = this.getCapturing();

		if (capturing != null)
		{
			return capturing.keyPressed(event);
		}

		boolean enter = event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER;

		if (enter && this.getFocused() instanceof EditBox)
		{
			this.clearFocus();
			this.commitEditors();
			return true;
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
		this.clearFocus();
		this.commitEditors();
		ConfigStorage.save();
	}
}
