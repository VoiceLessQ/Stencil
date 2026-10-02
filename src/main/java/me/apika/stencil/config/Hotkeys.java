package me.apika.stencil.config;

import java.util.List;

import me.apika.stencil.config.ConfigOption.Hotkey;

public class Hotkeys
{
	public static final Hotkey OPEN_GUI_SETTINGS = new Hotkey("openGuiSettings", "M,C");
	public static final Hotkey SPAWN_PROOF_TOGGLE = new Hotkey("spawnProofToggle", "M,X");
	public static final Hotkey SPAWN_PROOF_MODE = new Hotkey("spawnProofMode", "M,N");
	public static final Hotkey SPAWN_PROOF_SHAPE = new Hotkey("spawnProofShape", "M,B");
	public static final Hotkey SPAWN_PROOF_RADIUS_DECREASE = new Hotkey("spawnProofRadiusDecrease", "LEFT_SHIFT,DOWN");
	public static final Hotkey SPAWN_PROOF_RADIUS_INCREASE = new Hotkey("spawnProofRadiusIncrease", "LEFT_SHIFT,UP");
	public static final Hotkey TOOL_SELECT = new Hotkey("toolSelect", "LEFT_CONTROL", KeybindSettings.PRESS_ALLOWEXTRA);

	public static final List<ConfigOption<?>> HOTKEY_LIST = List.of(
			OPEN_GUI_SETTINGS,
			SPAWN_PROOF_TOGGLE,
			SPAWN_PROOF_MODE,
			SPAWN_PROOF_SHAPE,
			SPAWN_PROOF_RADIUS_DECREASE,
			SPAWN_PROOF_RADIUS_INCREASE,
			TOOL_SELECT
	);

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
