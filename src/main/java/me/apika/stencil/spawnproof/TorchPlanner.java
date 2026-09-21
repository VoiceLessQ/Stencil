package me.apika.stencil.spawnproof;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Chooses where light sources go so no dark spot in the area is left. Open
 * ground gets a diamond lattice, which tiles the plane with the source's reach
 * and no overlap; whatever the lattice leaves dark, behind walls or on other
 * levels, gets one more source at a time where it lights the most spots.
 * Light is simulated as a flood through non-occluding blocks, one level per
 * step; the real light engine takes over once a source is placed.
 */
public class TorchPlanner
{
	/**
	 * Positions for the light block that leave no dark spot lit at or below
	 * the spawn light level. Existing sources count too, since the light
	 * engine lags a tick behind a placement.
	 */
	public static Set<BlockPos> plan(Level world, BlockPos center, SpawnProofArea area, int verticalRadius, BlockState light, int maxSpawnLight, Set<BlockPos> dark, Set<BlockPos> existing)
	{
		int reach = light.getLightEmission() - maxSpawnLight - 1;

		if (reach < 1 || dark.isEmpty())
		{
			return Set.of();
		}

		Set<BlockPos> uncovered = new HashSet<>(dark);
		Set<BlockPos> planned = new HashSet<>();

		for (BlockPos pos : existing)
		{
			int emission = world.getBlockState(pos).getLightEmission();

			if (emission > 0)
			{
				flood(world, pos, emission, maxSpawnLight, uncovered);
			}
		}

		seedLattice(world, center, area, verticalRadius, light, maxSpawnLight, reach, uncovered, planned);
		patch(world, center, light, maxSpawnLight, reach, uncovered, planned);
		return planned;
	}

	/**
	 * Diamonds of radius r tile the grid exactly on the lattice spanned by
	 * (r, r+1) and (r+1, -r). It is anchored on the world origin, so the
	 * points stay put as the player walks.
	 */
	private static void seedLattice(Level world, BlockPos center, SpawnProofArea area, int verticalRadius, BlockState light, int maxSpawnLight, int reach, Set<BlockPos> uncovered, Set<BlockPos> planned)
	{
		int r = reach;
		int det = r * r + (r + 1) * (r + 1);
		int i0 = Math.floorDiv(r * center.getX() + (r + 1) * center.getZ(), det);
		int j0 = Math.floorDiv((r + 1) * center.getX() - r * center.getZ(), det);
		int span = Math.max(area.getExtent(Direction.EAST) + area.getExtent(Direction.WEST), area.getExtent(Direction.NORTH) + area.getExtent(Direction.SOUTH)) / r + 2;
		int minY = Math.max(center.getY() - verticalRadius, world.getMinY());
		int maxY = Math.min(center.getY() + verticalRadius, world.getMaxY());
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int i = i0 - span; i <= i0 + span; ++i)
		{
			for (int j = j0 - span; j <= j0 + span; ++j)
			{
				int x = r * i + (r + 1) * j;
				int z = (r + 1) * i - r * j;

				if (area.contains(x - center.getX(), z - center.getZ()) == false)
				{
					continue;
				}

				// Nearest dark floor in the column takes the lattice point.
				for (int dy = 0; dy <= verticalRadius; ++dy)
				{
					if (tryPlace(world, light, maxSpawnLight, pos.set(x, center.getY() + dy, z), minY, maxY, uncovered, planned) ||
						tryPlace(world, light, maxSpawnLight, pos.set(x, center.getY() - dy, z), minY, maxY, uncovered, planned))
					{
						break;
					}
				}
			}
		}
	}

	private static boolean tryPlace(Level world, BlockState light, int maxSpawnLight, BlockPos.MutableBlockPos pos, int minY, int maxY, Set<BlockPos> uncovered, Set<BlockPos> planned)
	{
		if (pos.getY() < minY || pos.getY() > maxY || uncovered.contains(pos) == false || canPlace(world, light, pos) == false)
		{
			return false;
		}

		BlockPos at = pos.immutable();
		planned.add(at);
		flood(world, at, light.getLightEmission(), maxSpawnLight, uncovered);
		return true;
	}

	/**
	 * Takes the dark spots nearest the player first, so the sources it adds are
	 * the ones in reach; for each still dark, puts a source on whichever nearby
	 * dark spot would light the most of the rest.
	 */
	private static void patch(Level world, BlockPos center, BlockState light, int maxSpawnLight, int reach, Set<BlockPos> uncovered, Set<BlockPos> planned)
	{
		List<BlockPos> ordered = new ArrayList<>(uncovered);
		ordered.sort(Comparator.comparingInt(center::distManhattan));
		int shift = reach / 2;

		for (BlockPos spot : ordered)
		{
			if (uncovered.contains(spot) == false)
			{
				continue;
			}

			BlockPos best = null;
			int bestCount = -1;

			for (BlockPos candidate : uncovered)
			{
				if (spot.distManhattan(candidate) > shift || canPlace(world, light, candidate) == false)
				{
					continue;
				}

				int count = 0;

				for (BlockPos other : uncovered)
				{
					if (candidate.distManhattan(other) <= reach)
					{
						++count;
					}
				}

				if (count > bestCount)
				{
					best = candidate;
					bestCount = count;
				}
			}

			// No spot nearby can hold the block, so give up on this one.
			if (best == null)
			{
				uncovered.remove(spot);
				continue;
			}

			planned.add(best);
			flood(world, best, light.getLightEmission(), maxSpawnLight, uncovered);
		}
	}

	private static boolean canPlace(Level world, BlockState light, BlockPos pos)
	{
		return world.getBlockState(pos).canBeReplaced() && light.canSurvive(world, pos);
	}

	/** Floods light out from a source, dropping every spot it lifts above the spawn level. */
	private static void flood(Level world, BlockPos origin, int emission, int maxSpawnLight, Set<BlockPos> uncovered)
	{
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		ArrayDeque<Integer> values = new ArrayDeque<>();
		seen.add(origin);
		queue.add(origin);
		values.add(emission);

		while (queue.isEmpty() == false)
		{
			BlockPos pos = queue.poll();
			int value = values.poll();
			uncovered.remove(pos);
			int next = value - 1;

			if (next <= maxSpawnLight)
			{
				continue;
			}

			for (Direction side : Direction.values())
			{
				BlockPos neighbour = pos.relative(side);

				if (world.isOutsideBuildHeight(neighbour) || seen.contains(neighbour) || world.getBlockState(neighbour).canOcclude())
				{
					continue;
				}

				seen.add(neighbour);
				queue.add(neighbour);
				values.add(next);
			}
		}
	}
}
