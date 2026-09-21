package me.apika.stencil.spawnproof;

import java.util.HashSet;
import java.util.Set;

import me.apika.stencil.config.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEventListener;

/**
 * Finds the positions a hostile mob could spawn at: a spawnable surface below,
 * a 1x2 column of empty space, and no block light. Pure world queries, no
 * state of its own.
 */
public class SpawnProofScanner
{
	/**
	 * Every spawnable position within the area's footprint around the center,
	 * spanning the given number of blocks up and down, clamped to the world height.
	 */
	public static Set<BlockPos> scan(Level world, BlockPos center, SpawnProofArea area, int verticalRadius, Set<BlockPos> excluded)
	{
		int minY = Math.max(center.getY() - verticalRadius, world.getMinY());
		int maxY = Math.min(center.getY() + verticalRadius, world.getMaxY());
		LayerLightEventListener blockLight = world.getLightEngine().getLayerListener(LightLayer.BLOCK);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Set<BlockPos> found = new HashSet<>();

		for (int y = minY; y <= maxY; ++y)
		{
			for (int z = area.getMinZ(center.getZ()); z <= area.getMaxZ(center.getZ()); ++z)
			{
				for (int x = area.getMinX(center.getX()); x <= area.getMaxX(center.getX()); ++x)
				{
					if (area.contains(x - center.getX(), z - center.getZ()) == false)
					{
						continue;
					}

					pos.set(x, y, z);

					if (excluded.contains(pos) == false && isSpawnable(world, blockLight, pos))
					{
						found.add(pos.immutable());
					}
				}
			}
		}

		return found;
	}

	/**
	 * Every replaceable position on the center's own Y level within the area's
	 * footprint, so a flat layer can be filled with whatever block is selected.
	 */
	public static Set<BlockPos> scanLayer(Level world, BlockPos center, SpawnProofArea area, Set<BlockPos> excluded)
	{
		int y = center.getY();

		if (y < world.getMinY() || y > world.getMaxY())
		{
			return Set.of();
		}

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Set<BlockPos> found = new HashSet<>();

		for (int z = area.getMinZ(center.getZ()); z <= area.getMaxZ(center.getZ()); ++z)
		{
			for (int x = area.getMinX(center.getX()); x <= area.getMaxX(center.getX()); ++x)
			{
				if (area.contains(x - center.getX(), z - center.getZ()) == false)
				{
					continue;
				}

				pos.set(x, y, z);

				if (excluded.contains(pos) == false && world.getBlockState(pos).canBeReplaced())
				{
					found.add(pos.immutable());
				}
			}
		}

		return found;
	}

	public static boolean isSpawnable(Level world, LayerLightEventListener blockLight, BlockPos pos)
	{
		BlockPos below = pos.below();
		BlockState stateBelow = world.getBlockState(below);

		if (stateBelow.isValidSpawn(world, below, EntityTypes.ZOMBIE) == false)
		{
			return false;
		}

		BlockState state = world.getBlockState(pos);

		if (NaturalSpawner.isValidEmptySpawnBlock(world, pos, state, state.getFluidState(), EntityTypes.ZOMBIE) == false)
		{
			return false;
		}

		// Tall mobs need a second free block; spiders and other short mobs do not.
		if (Configs.Generic.SPAWN_PROOF_LOW_GAPS.getValue() == false)
		{
			BlockPos above = pos.above();
			BlockState stateAbove = world.getBlockState(above);

			if (NaturalSpawner.isValidEmptySpawnBlock(world, above, stateAbove, stateAbove.getFluidState(), EntityTypes.ZOMBIE) == false)
			{
				return false;
			}
		}

		return blockLight.getLightValue(pos) == 0;
	}
}
