package me.apika.stencil.spawnproof;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.config.ConfigStorage;
import me.apika.stencil.config.Configs;
import me.apika.stencil.config.Hotkeys;
import me.apika.stencil.input.Keybind;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Keeps the set of ghost blocks for spawn proofing: one per mob-spawnable spot
 * within the configured radius around the player, drawn as a translucent box
 * the shape of the ghost block. The scan reruns when the player moves a block,
 * the radius changes, or once a second so light changes are picked up.
 */
public class SpawnProofManager
{
	private static final SpawnProofManager INSTANCE = new SpawnProofManager();
	private static final int RESCAN_INTERVAL_TICKS = 20;

	private final Keybind toggle = new Keybind(Hotkeys.SPAWN_PROOF_TOGGLE);
	private final Keybind modeCycle = new Keybind(Hotkeys.SPAWN_PROOF_MODE);
	private final Keybind shapeCycle = new Keybind(Hotkeys.SPAWN_PROOF_SHAPE);
	private final Keybind adjustSide = new Keybind(Hotkeys.SPAWN_PROOF_ADJUST_SIDE);
	private final Keybind radiusIncrease = new Keybind(Hotkeys.SPAWN_PROOF_RADIUS_INCREASE);
	private final Keybind radiusDecrease = new Keybind(Hotkeys.SPAWN_PROOF_RADIUS_DECREASE);

	private final Set<BlockPos> generated = new HashSet<>();
	private final Set<BlockPos> excluded = new HashSet<>();
	private BlockState blockState = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
	private final Map<SpawnProofMode, SpawnProofArea> areas = new EnumMap<>(SpawnProofMode.class);
	private SpawnProofMode mode = SpawnProofMode.SPAWN_PROOF;
	private boolean enabled;
	private boolean dirty;
	private BlockPos lastCenter;
	private int ticksSinceScan;

	private SpawnProofManager()
	{
		for (SpawnProofMode mode : SpawnProofMode.values())
		{
			this.areas.put(mode, new SpawnProofArea(mode.getRadius().get()));
		}
	}

	public static SpawnProofManager getInstance()
	{
		return INSTANCE;
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}

	public int getGhostCount()
	{
		return this.generated.size();
	}

	public BlockState getBlockState()
	{
		return this.blockState;
	}

	public SpawnProofMode getMode()
	{
		return this.mode;
	}

	public SpawnProofArea getArea()
	{
		return this.areas.get(this.mode);
	}

	/** Forget everything, for a world change. */
	public void clear()
	{
		this.generated.clear();
		this.excluded.clear();
		this.lastCenter = null;
	}

	/**
	 * Mouse wheel while the adjust-side key is held grows or shrinks the side
	 * of the area the player is facing. Returns true when the scroll was used,
	 * so the hotbar does not also change.
	 */
	public boolean onScroll(double yOffset)
	{
		Minecraft mc = Minecraft.getInstance();

		if (this.enabled == false || mc.player == null || mc.gui.screen() != null || this.adjustSide.isHeld() == false)
		{
			return false;
		}

		if (yOffset == 0.0)
		{
			return true;
		}

		Direction side = mc.player.getDirection();
		SpawnProofArea area = this.getArea();
		area.setExtent(side, area.getExtent(side) + (yOffset > 0.0 ? 1 : -1));
		this.dirty = true;
		mc.player.sendOverlayMessage(Component.literal(this.mode.getDisplayName() + " " + side.getName() + ": " + area.getExtent(side)));
		return true;
	}

	public void onClientTick(Minecraft mc)
	{
		if (mc.level == null || mc.player == null)
		{
			return;
		}

		if (mc.gui.screen() == null)
		{
			this.handleHotkeys(mc);
		}

		if (this.enabled == false)
		{
			return;
		}

		BlockPos center = mc.player.blockPosition();
		++this.ticksSinceScan;

		if (this.dirty ||
			center.equals(this.lastCenter) == false ||
			this.ticksSinceScan >= RESCAN_INTERVAL_TICKS)
		{
			this.rescan(mc.level, center);
		}

		this.emitGizmos(mc);
	}

	private void handleHotkeys(Minecraft mc)
	{
		this.toggle.tick();
		this.modeCycle.tick();
		this.shapeCycle.tick();
		this.adjustSide.tick();
		this.radiusIncrease.tick();
		this.radiusDecrease.tick();

		if (this.toggle.wasTriggered())
		{
			this.enabled = !this.enabled;
			this.generated.clear();
			this.lastCenter = null;
			mc.player.sendOverlayMessage(Component.literal("Spawn proof " + (this.enabled ? "ON" : "OFF")));
		}

		if (this.enabled == false)
		{
			return;
		}

		if (this.modeCycle.wasTriggered())
		{
			this.mode = this.mode.next();
			this.generated.clear();
			this.dirty = true;
			mc.player.sendOverlayMessage(Component.literal("Spawn proof mode: " + this.mode.getDisplayName()));
		}

		if (this.shapeCycle.wasTriggered())
		{
			this.getArea().cycleShape();
			this.dirty = true;
			mc.player.sendOverlayMessage(Component.literal(this.mode.getDisplayName() + " area: " + this.getArea()));
		}

		int delta = 0;

		if (this.radiusIncrease.wasTriggered())
		{
			delta = 1;
		}
		else if (this.radiusDecrease.wasTriggered())
		{
			delta = -1;
		}

		if (delta != 0)
		{
			ConfigOption.Int radius = this.mode.getRadius();
			radius.setValue(radius.get() + delta);
			ConfigStorage.save();
			this.getArea().setAll(radius.get());
			this.dirty = true;
			mc.player.sendOverlayMessage(Component.literal(this.mode.getDisplayName() + " radius: " + radius.get()));
		}
	}

	private void rescan(Level world, BlockPos center)
	{
		SpawnProofArea area = this.getArea();
		Set<BlockPos> found = this.mode == SpawnProofMode.LAYER
				? SpawnProofScanner.scanLayer(world, center, area, this.excluded)
				: SpawnProofScanner.scan(world, center, area, this.mode.getRadius().get(), this.excluded);

		this.lastCenter = center;
		this.dirty = false;
		this.ticksSinceScan = 0;
		this.generated.clear();
		this.generated.addAll(found);
	}

	/**
	 * Gizmos added during the client tick are drawn by the level renderer until
	 * the next tick replaces them, so every tick submits the full set again.
	 */
	private void emitGizmos(Minecraft mc)
	{
		if (this.generated.isEmpty())
		{
			return;
		}

		int fill = Configs.Colors.SPAWN_PROOF_GHOST_COLOR.get();
		int stroke = fill | 0xFF000000;
		GizmoStyle style = GizmoStyle.strokeAndFill(stroke, 1.0f, fill);
		VoxelShape shape = this.blockState.getShape(mc.level, BlockPos.ZERO);
		AABB bounds = shape.isEmpty() ? new AABB(BlockPos.ZERO) : shape.bounds();

		try (Gizmos.TemporaryCollection ignored = mc.collectPerTickGizmos())
		{
			for (BlockPos pos : this.generated)
			{
				Gizmos.cuboid(bounds.move(pos), style);
			}
		}
	}
}
