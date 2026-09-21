package me.apika.stencil.spawnproof;

import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.config.Configs;

/** Which positions get a ghost block, and which radius setting drives it. */
public enum SpawnProofMode
{
	/** Every mob-spawnable spot in a cube around the player. */
	SPAWN_PROOF ("Spawn proof", Configs.Generic.SPAWN_PROOF_RADIUS),
	/** Every empty spot on the player's own Y level, for filling a flat layer. */
	LAYER       ("Layer", Configs.Generic.SPAWN_PROOF_LAYER_RADIUS);

	private final String displayName;
	private final ConfigOption.Int radius;

	SpawnProofMode(String displayName, ConfigOption.Int radius)
	{
		this.displayName = displayName;
		this.radius = radius;
	}

	public String getDisplayName()
	{
		return this.displayName;
	}

	public ConfigOption.Int getRadius()
	{
		return this.radius;
	}

	public SpawnProofMode next()
	{
		SpawnProofMode[] values = values();
		return values[(this.ordinal() + 1) % values.length];
	}
}
