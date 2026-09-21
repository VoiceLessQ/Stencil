package me.apika.stencil.config;

import java.util.List;

import me.apika.stencil.config.ConfigOption.Bool;
import me.apika.stencil.config.ConfigOption.Color;
import me.apika.stencil.config.ConfigOption.Dbl;
import me.apika.stencil.config.ConfigOption.Int;

/**
 * The settings, grouped into the same categories Litematica uses, with the
 * same JSON keys where an option has a Litematica counterpart. Hotkeys are
 * their own category and live in {@link Hotkeys}.
 */
public class Configs
{
	private static final String GENERIC = "generic";
	private static final String VISUALS = "visuals";
	private static final String COLORS = "colors";

	public static class Generic
	{
		public static final Bool DEBUG_LOGGING = new Bool(GENERIC, "debugLogging", false);
		public static final Int SPAWN_PROOF_LAYER_RADIUS = new Int(GENERIC, "spawnProofLayerRadius", 4, 1, 32);
		public static final Int SPAWN_PROOF_RADIUS = new Int(GENERIC, "spawnProofRadius", 4, 1, 16);
		public static final Bool SPAWN_PROOF_LOW_GAPS = new Bool(GENERIC, "spawnProofLowGaps", true);
		public static final Bool SPAWN_PROOF_SINGLE_LAYER = new Bool(GENERIC, "spawnProofSingleLayer", true);
		public static final Int LIGHT_PLAN_RADIUS = new Int(GENERIC, "lightPlanRadius", 32, 1, 64);
		public static final Int COMMAND_REACH = new Int(GENERIC, "commandReach", 0, 0, 64);
		public static final Int TORCH_MAX_SPAWN_LIGHT = new Int(GENERIC, "torchMaxSpawnLight", 0, 0, 14);

		public static final List<ConfigOption<?>> OPTIONS = List.of(
				DEBUG_LOGGING,
				SPAWN_PROOF_LAYER_RADIUS,
				SPAWN_PROOF_RADIUS,
				SPAWN_PROOF_LOW_GAPS,
				SPAWN_PROOF_SINGLE_LAYER,
				LIGHT_PLAN_RADIUS,
				COMMAND_REACH,
				TORCH_MAX_SPAWN_LIGHT
		);
	}

	public static class Visuals
	{
		public static final Dbl GHOST_BLOCK_ALPHA = new Dbl(VISUALS, "ghostBlockAlpha", 0.4, 0.05, 1);

		public static final List<ConfigOption<?>> OPTIONS = List.of(
				GHOST_BLOCK_ALPHA
		);
	}

	public static class Colors
	{
		public static final Color SPAWN_PROOF_GHOST_COLOR = new Color(COLORS, "spawnProofGhostColor", "#FFFFFF");

		public static final List<ConfigOption<?>> OPTIONS = List.of(
				SPAWN_PROOF_GHOST_COLOR
		);
	}
}
