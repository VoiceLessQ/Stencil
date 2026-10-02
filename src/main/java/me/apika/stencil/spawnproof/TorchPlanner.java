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

public class TorchPlanner
{
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
