package me.apika.stencil.spawnproof;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.apika.stencil.config.Configs;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Draws every ghost as the real block model, translucent with the alpha of
 * the ghost colour, so the player sees which block goes where.
 */
public final class SpawnProofRenderer
{
	private static final Direction[] SIDES = { null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };
	private static final RandomSource RANDOM = RandomSource.create();

	private SpawnProofRenderer()
	{
	}

	public static void collectSubmits(LevelRenderContext context)
	{
		SpawnProofManager manager = SpawnProofManager.getInstance();

		if (manager.isEnabled() == false || manager.getGhostCount() == 0)
		{
			return;
		}

		BlockState state = manager.getBlockState();

		if (state == null)
		{
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(state);
		List<BlockStateModelPart> parts = new ArrayList<>();
		RANDOM.setSeed(42L);
		model.collectParts(RANDOM, parts);
		List<BakedQuad> quads = new ArrayList<>();

		for (BlockStateModelPart part : parts)
		{
			for (Direction side : SIDES)
			{
				quads.addAll(part.getQuads(side));
			}
		}

		if (quads.isEmpty())
		{
			return;
		}

		int alpha = Configs.Colors.SPAWN_PROOF_GHOST_COLOR.get() >>> 24;
		QuadInstance tint = new QuadInstance();
		tint.setColor(alpha << 24 | 0xFFFFFF);
		Vec3 camera = context.levelState().cameraRenderState.pos;
		PoseStack poseStack = context.poseStack();
		SubmitNodeCollector collector = context.submitNodeCollector();

		for (BlockPos pos : manager.getGhosts())
		{
			poseStack.pushPose();
			poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
			collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, consumer) -> putQuads(pose, consumer, quads, tint));
			poseStack.popPose();
		}
	}

	private static void putQuads(PoseStack.Pose pose, VertexConsumer consumer, List<BakedQuad> quads, QuadInstance tint)
	{
		for (BakedQuad quad : quads)
		{
			consumer.putBakedQuad(pose, quad, tint);
		}
	}
}
