package me.apika.stencil.config;

public enum KeybindSettings
{
	DEFAULT(false),
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
