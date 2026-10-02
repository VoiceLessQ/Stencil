package me.apika.stencil.config;

/**
 * How a hotkey is matched. Only the two cases Stencil uses are modelled,
 * rather than the full malilib matrix of context/activation/exclusivity flags.
 */
public enum KeybindSettings
{
	/** Fires on press, extra held modifiers or mouse buttons block it. */
	DEFAULT(false),
	/** Fires on press even when other keys are held (mouse buttons, tools). */
	PRESS_ALLOWEXTRA(true);

	private final boolean allowExtraKeys;

	KeybindSettings(boolean allowExtraKeys)
	{
		this.allowExtraKeys = allowExtraKeys;
	}

	public boolean getAllowExtraKeys()
	{
		return this.allowExtraKeys;
	}
}
