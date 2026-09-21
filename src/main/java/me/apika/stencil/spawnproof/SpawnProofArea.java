package me.apika.stencil.spawnproof;

import me.apika.stencil.config.ConfigOption;
import net.minecraft.core.Direction;

/**
 * The footprint of a scan around the player: how far it reaches to each of
 * the four sides, and whether the corners are kept (square) or cut (circle).
 * The extents are session state, not config; the radius hotkeys reset all
 * four to the mode's configured radius.
 */
public class SpawnProofArea
{
	public enum Shape
	{
		SQUARE ("Square"),
		CIRCLE ("Circle");

		private final String displayName;

		Shape(String displayName)
		{
			this.displayName = displayName;
		}

		public String getDisplayName()
		{
			return this.displayName;
		}

		public Shape next()
		{
			Shape[] values = values();
			return values[(this.ordinal() + 1) % values.length];
		}
	}

	private final int maxExtent;
	private int north;
	private int south;
	private int east;
	private int west;
	private int baseRadius;
	private Shape shape = Shape.SQUARE;

	/** The sides can grow no further than the radius option itself allows. */
	public SpawnProofArea(ConfigOption.Int radius)
	{
		this.maxExtent = radius.getMaxValue();
		this.setAll(radius.get());
	}

	public Shape getShape()
	{
		return this.shape;
	}

	public void cycleShape()
	{
		this.shape = this.shape.next();
	}

	public int getExtent(Direction side)
	{
		return switch (side)
		{
			case NORTH -> this.north;
			case SOUTH -> this.south;
			case EAST -> this.east;
			case WEST -> this.west;
			default -> 0;
		};
	}

	public void setExtent(Direction side, int value)
	{
		value = Math.clamp(value, 0, this.maxExtent);

		switch (side)
		{
			case NORTH -> this.north = value;
			case SOUTH -> this.south = value;
			case EAST -> this.east = value;
			case WEST -> this.west = value;
			default -> {}
		}
	}

	/** The radius the sides were last reset to, before any per-side adjustment. */
	public int getBaseRadius()
	{
		return this.baseRadius;
	}

	public void setAll(int radius)
	{
		this.baseRadius = radius;
		this.north = this.south = this.east = this.west = Math.clamp(radius, 0, this.maxExtent);
	}

	public int getMinX(int centerX) { return centerX - this.west; }
	public int getMaxX(int centerX) { return centerX + this.east; }
	public int getMinZ(int centerZ) { return centerZ - this.north; }
	public int getMaxZ(int centerZ) { return centerZ + this.south; }

	/** Whether an offset from the center lies inside the footprint. */
	public boolean contains(int dx, int dz)
	{
		int ex = dx >= 0 ? this.east : this.west;
		int ez = dz >= 0 ? this.south : this.north;

		if (Math.abs(dx) > ex || Math.abs(dz) > ez)
		{
			return false;
		}

		if (this.shape == Shape.SQUARE)
		{
			return true;
		}

		// Half-block padding so a radius of N still covers N blocks along the axes.
		double rx = ex + 0.5;
		double rz = ez + 0.5;
		return (dx * dx) / (rx * rx) + (dz * dz) / (rz * rz) <= 1.0;
	}

	@Override
	public String toString()
	{
		return this.shape.getDisplayName() + " N" + this.north + " S" + this.south + " E" + this.east + " W" + this.west;
	}
}
