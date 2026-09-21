package me.apika.stencil.spawnproof;

import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.config.Hotkeys;

/**
 * The spawn proof tools. One is selected with the tool-select key plus the
 * wheel and runs on right click; each also has its own key that uses it
 * directly while held.
 */
public enum ToolAction
{
	/** Right click on a ghost places the current block there. */
	PLACE        ("Place blocks", Hotkeys.TOOL_PLACE_BLOCKS),
	/** Its key, or right click, places the current block from the inventory on every ghost within reach. */
	PLACE_ALL    ("Place all", Hotkeys.TOOL_PLACE_ALL),
	/** Right click on a ghost with a block in hand makes every ghost that block. */
	CHANGE_BLOCK ("Change block", Hotkeys.TOOL_CHANGE_BLOCK),
	/** Wheel grows or shrinks only the side of the area the player is facing. */
	EXTEND_SIDE  ("Extend facing side", Hotkeys.TOOL_EXTEND_SIDE);

	private final String displayName;
	private final ConfigOption.Hotkey hotkey;

	ToolAction(String displayName, ConfigOption.Hotkey hotkey)
	{
		this.displayName = displayName;
		this.hotkey = hotkey;
	}

	public String getDisplayName()
	{
		return this.displayName;
	}

	public ConfigOption.Hotkey getHotkey()
	{
		return this.hotkey;
	}

	public ToolAction next()
	{
		ToolAction[] values = values();
		return values[(this.ordinal() + 1) % values.length];
	}

	public ToolAction previous()
	{
		ToolAction[] values = values();
		return values[(this.ordinal() + values.length - 1) % values.length];
	}
}
