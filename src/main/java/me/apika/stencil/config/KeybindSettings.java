package me.apika.stencil.config;

/**
 * How a hotkey is matched. Only the four combinations Litematica actually uses
 * are modelled, rather than the full malilib matrix of context/activation/
 * exclusivity flags.
 */
public enum KeybindSettings
{
	/** Fires on press, extra held keys block it. */
	DEFAULT(Activation.PRESS, false, false),
	/** Fires on press even when other keys are held (mouse buttons, tools). */
	PRESS_ALLOWEXTRA(Activation.PRESS, true, false),
	/** Fires on release, and swallows the key so nothing else sees it. */
	RELEASE_EXCLUSIVE(Activation.RELEASE, false, true),
	/** Never fires on its own; only read as a held modifier while in game. */
	MODIFIER_INGAME(Activation.BOTH, true, false);

	public enum Activation
	{
		PRESS,
		RELEASE,
		BOTH
	}

	private final Activation activation;
	private final boolean allowExtraKeys;
	private final boolean exclusive;

	KeybindSettings(Activation activation, boolean allowExtraKeys, boolean exclusive)
	{
		this.activation = activation;
		this.allowExtraKeys = allowExtraKeys;
		this.exclusive = exclusive;
	}

	public Activation getActivation()
	{
		return this.activation;
	}

	public boolean getAllowExtraKeys()
	{
		return this.allowExtraKeys;
	}

	public boolean isExclusive()
	{
		return this.exclusive;
	}
}
