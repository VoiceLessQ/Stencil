package me.apika.stencil.config;

import java.util.List;

import me.apika.stencil.config.ConfigOption.Hotkey;

/**
 * The keybinds. Litematica's M-prefixed style is kept so the muscle memory
 * transfers: M,C opens the settings, M,X toggles the overlay, and so on.
 */
public class Hotkeys
{
	public static final Hotkey OPEN_GUI_SETTINGS = new Hotkey("openGuiSettings", "M,C");
	public static final Hotkey SPAWN_PROOF_TOGGLE = new Hotkey("spawnProofToggle", "M,X");
	public static final Hotkey SPAWN_PROOF_MODE = new Hotkey("spawnProofMode", "M,N");
	public static final Hotkey SPAWN_PROOF_SHAPE = new Hotkey("spawnProofShape", "M,B");
	public static final Hotkey SPAWN_PROOF_RADIUS_DECREASE = new Hotkey("spawnProofRadiusDecrease", "LEFT_SHIFT,DOWN");
	public static final Hotkey SPAWN_PROOF_RADIUS_INCREASE = new Hotkey("spawnProofRadiusIncrease", "LEFT_SHIFT,UP");
	public static final Hotkey TOOL_SELECT = new Hotkey("toolSelect", "LEFT_CONTROL");
	public static final Hotkey TOOL_PLACE_BLOCKS = new Hotkey("toolPlaceBlocks", "");
	public static final Hotkey TOOL_PLACE_ALL = new Hotkey("toolPlaceAll", "V");
	public static final Hotkey TOOL_CHANGE_BLOCK = new Hotkey("toolChangeBlock", "C");
	public static final Hotkey TOOL_EXTEND_SIDE = new Hotkey("toolExtendSide", "LEFT_ALT");

	public static final List<ConfigOption<?>> HOTKEY_LIST = List.of(
			OPEN_GUI_SETTINGS,
			SPAWN_PROOF_TOGGLE,
			SPAWN_PROOF_MODE,
			SPAWN_PROOF_SHAPE,
			SPAWN_PROOF_RADIUS_DECREASE,
			SPAWN_PROOF_RADIUS_INCREASE,
			TOOL_SELECT,
			TOOL_PLACE_BLOCKS,
			TOOL_PLACE_ALL,
			TOOL_CHANGE_BLOCK,
			TOOL_EXTEND_SIDE
	);

	/** Another hotkey bound to exactly the same keys, or null if the keys are free. */
	public static Hotkey findConflict(Hotkey hotkey)
	{
		String keys = hotkey.getKeysAsString();

		if (keys.isEmpty())
		{
			return null;
		}

		for (ConfigOption<?> other : HOTKEY_LIST)
		{
			if (other != hotkey && other instanceof Hotkey otherHotkey && keys.equals(otherHotkey.getKeysAsString()))
			{
				return otherHotkey;
			}
		}

		return null;
	}
}
