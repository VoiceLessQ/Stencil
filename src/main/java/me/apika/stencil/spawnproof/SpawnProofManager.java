package me.apika.stencil.spawnproof;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.apika.stencil.StencilClient;
import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.config.ConfigStorage;
import me.apika.stencil.config.Configs;
import me.apika.stencil.config.Hotkeys;
import me.apika.stencil.input.Keybind;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
	private final Keybind toolSelect = new Keybind(Hotkeys.TOOL_SELECT);
	private final Map<ToolAction, Keybind> toolKeys = new EnumMap<>(ToolAction.class);
	private final Keybind radiusIncrease = new Keybind(Hotkeys.SPAWN_PROOF_RADIUS_INCREASE);
	private final Keybind radiusDecrease = new Keybind(Hotkeys.SPAWN_PROOF_RADIUS_DECREASE);

	private final Set<BlockPos> generated = new HashSet<>();
	private final Set<BlockPos> excluded = new HashSet<>();
	private final Set<BlockPos> placed = new HashSet<>();
	private BlockState blockState;
	private ToolAction action = ToolAction.PLACE;
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
			this.areas.put(mode, new SpawnProofArea(mode.getRadius()));
		}

		for (ToolAction tool : ToolAction.values())
		{
			this.toolKeys.put(tool, new Keybind(tool.getHotkey()));
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

	public Set<BlockPos> getGhosts()
	{
		return this.generated;
	}

	/** The block ghosts are drawn as and placed with, or null until one is chosen. */
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
		this.placed.clear();
		this.lastCenter = null;
	}

	/**
	 * Mouse wheel with the tool-select key held cycles the tool; with the
	 * extend-side key held it resizes the facing side. Works while spawn proof
	 * is off too, so a choice is never swallowed. Returns true when the scroll
	 * was used, so the hotbar does not also change.
	 */
	public boolean onScroll(double yOffset)
	{
		Minecraft mc = Minecraft.getInstance();

		if (mc.player == null || mc.gui.screen() != null || yOffset == 0.0)
		{
			return false;
		}

		boolean up = yOffset > 0.0;

		if (this.toolSelect.isHeld())
		{
			this.action = up ? this.action.next() : this.action.previous();
			mc.player.sendOverlayMessage(Component.literal("Tool: " + this.action.getDisplayName()));
			return true;
		}

		if (this.toolKeys.get(ToolAction.EXTEND_SIDE).isHeld())
		{
			this.extendFacingSide(mc, up);
			return true;
		}

		return false;
	}

	/** The tool whose key is held, else the selected one. */
	private ToolAction activeTool()
	{
		for (ToolAction tool : ToolAction.values())
		{
			if (this.toolKeys.get(tool).isHeld())
			{
				return tool;
			}
		}

		return this.action;
	}

	private void extendFacingSide(Minecraft mc, boolean up)
	{
		Direction side = mc.player.getDirection();
		SpawnProofArea area = this.getArea();
		area.setExtent(side, area.getExtent(side) + (up ? 1 : -1));
		this.dirty = true;
		mc.player.sendOverlayMessage(Component.literal(this.mode.getDisplayName() + " " + side.getName() + ": " + area.getExtent(side)));
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
		this.syncRadius();

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
		this.toolSelect.tick();
		this.toolKeys.values().forEach(Keybind::tick);
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

		// Held place keys keep going, so a fill grows outward as ghosts come into reach.
		if (this.toolKeys.get(ToolAction.PLACE_ALL).isHeld())
		{
			this.placeAll(mc, this.toolKeys.get(ToolAction.PLACE_ALL).wasTriggered());
		}
		else if (this.toolKeys.get(ToolAction.PLACE).isHeld())
		{
			this.placeAimed(mc);
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

	/**
	 * Use (right click, pressed or held) while spawn proofing runs the active
	 * tool: place the aimed ghost, place every ghost within reach, or take the
	 * block in hand as the ghost block. Returns true when the click was used,
	 * so vanilla does not also act on it.
	 */
	public boolean onUse()
	{
		Minecraft mc = Minecraft.getInstance();

		if (this.enabled == false || mc.player == null || mc.level == null || mc.gui.screen() != null)
		{
			return false;
		}

		return switch (this.activeTool())
		{
			case PLACE -> this.placeAimed(mc);
			case PLACE_ALL -> this.placeAll(mc, true);
			case CHANGE_BLOCK -> this.chooseHeldBlock(mc);
			case EXTEND_SIDE -> false;
		};
	}

	/** Aiming at a ghost with a block item in hand makes every ghost that block, if it stops spawns. */
	private boolean chooseHeldBlock(Minecraft mc)
	{
		BlockPos target = this.findAimedGhost(mc);

		if (target == null)
		{
			return false;
		}

		if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem item))
		{
			mc.player.sendOverlayMessage(Component.literal("Hold a block item to choose it"));
			return true;
		}

		BlockState state = floorState(item.getBlock());
		String name = item.getBlock().getName().getString();

		if (state.isValidSpawn(mc.level, target, EntityTypes.ZOMBIE))
		{
			mc.player.sendOverlayMessage(Component.literal(name + " does not stop spawns"));
			return true;
		}

		this.blockState = state;
		mc.player.sendOverlayMessage(Component.literal("Spawn proof block: " + name));
		return true;
	}

	/** The state a block takes when placed on the floor, so buttons and levers do not preview on a wall. */
	private static BlockState floorState(Block block)
	{
		BlockState state = block.defaultBlockState();

		if (state.hasProperty(BlockStateProperties.ATTACH_FACE))
		{
			state = state.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR);
		}

		return state;
	}

	private boolean placeAimed(Minecraft mc)
	{
		BlockPos target = this.findAimedGhost(mc);

		if (target == null)
		{
			return false;
		}

		this.withChosenBlockInHand(mc, true, () -> this.placeAt(mc, target));
		return true;
	}

	/**
	 * Runs the action with the chosen block in the main hand, pulled from
	 * wherever it sits in the inventory, and puts things back afterwards.
	 */
	private void withChosenBlockInHand(Minecraft mc, boolean report, Runnable action)
	{
		if (this.blockState == null)
		{
			if (report)
			{
				mc.player.sendOverlayMessage(Component.literal("No block chosen: use the Change block tool on a ghost"));
			}

			return;
		}

		Inventory inventory = mc.player.getInventory();
		Item item = this.blockState.getBlock().asItem();

		if (inventory.getSelectedItem().getItem() == item)
		{
			action.run();
			return;
		}

		int slot = this.findInventorySlot(inventory, item);

		if (slot < 0 && mc.player.hasInfiniteMaterials())
		{
			this.withCreativeStack(mc, new ItemStack(item), action);
			return;
		}

		if (slot < 0)
		{
			if (report)
			{
				mc.player.sendOverlayMessage(Component.literal("No " + this.blockState.getBlock().getName().getString() + " in inventory"));
			}

			return;
		}

		int selected = inventory.getSelectedSlot();
		boolean swapped = Inventory.isHotbarSlot(slot) == false;

		if (swapped)
		{
			mc.gameMode.handleContainerInput(InventoryMenu.CONTAINER_ID, slot, selected, ContainerInput.SWAP, mc.player);
		}
		else
		{
			this.selectHotbarSlot(mc, slot);
		}

		action.run();

		if (swapped)
		{
			mc.gameMode.handleContainerInput(InventoryMenu.CONTAINER_ID, slot, selected, ContainerInput.SWAP, mc.player);
		}
		else
		{
			this.selectHotbarSlot(mc, selected);
		}
	}

	/** Places on every ghost the player could reach, nearest first. */
	private boolean placeAll(Minecraft mc, boolean report)
	{
		if (this.blockState == null)
		{
			if (report)
			{
				mc.player.sendOverlayMessage(Component.literal("No block chosen: use the Change block tool on a ghost"));
			}

			return true;
		}

		Vec3 eye = mc.player.getEyePosition();
		double reach = mc.player.blockInteractionRange();
		List<BlockPos> targets = new ArrayList<>();

		for (BlockPos pos : this.generated)
		{
			if (pos.closerToCenterThan(eye, reach))
			{
				targets.add(pos);
			}
		}

		if (targets.isEmpty())
		{
			if (report)
			{
				mc.player.sendOverlayMessage(Component.literal("No ghosts in reach"));
			}

			return true;
		}

		targets.sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(eye)));
		int[] placed = {0};

		this.withChosenBlockInHand(mc, report, () ->
		{
			Inventory inventory = mc.player.getInventory();

			for (BlockPos pos : targets)
			{
				if (inventory.getSelectedItem().isEmpty())
				{
					break;
				}

				if (this.placeAt(mc, pos))
				{
					++placed[0];
				}
			}

			if (report || placed[0] > 0)
			{
				mc.player.sendOverlayMessage(Component.literal("Placed " + placed[0] + " of " + targets.size() + " in reach"));
			}
		});

		return true;
	}

	/** Creative mode: drop the stack into the selected slot for the action, then put the old stack back. */
	private void withCreativeStack(Minecraft mc, ItemStack stack, Runnable action)
	{
		Inventory inventory = mc.player.getInventory();
		int selected = inventory.getSelectedSlot();
		int containerSlot = InventoryMenu.USE_ROW_SLOT_START + selected;
		ItemStack previous = inventory.getSelectedItem().copy();

		inventory.setItem(selected, stack);
		mc.gameMode.handleCreativeModeItemAdd(stack, containerSlot);
		action.run();
		inventory.setItem(selected, previous);
		mc.gameMode.handleCreativeModeItemAdd(previous, containerSlot);
	}

	/** Index into the 36 non-equipment slots holding the item, hotbar first, or -1. */
	private int findInventorySlot(Inventory inventory, Item item)
	{
		NonNullList<ItemStack> items = inventory.getNonEquipmentItems();

		for (int i = 0; i < items.size(); ++i)
		{
			if (items.get(i).getItem() == item)
			{
				return i;
			}
		}

		return -1;
	}

	private void selectHotbarSlot(Minecraft mc, int slot)
	{
		mc.player.getInventory().setSelectedSlot(slot);
		mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
	}

	/** True when the main hand holds the ghost block's item; otherwise says what is missing. */
	/**
	 * Places the block into the ghost's own position, like Litematica's easy
	 * place: click the top of the block below, or a side neighbour when the
	 * block below is the same single slab, since clicking that would merge
	 * the two into a double slab instead of filling the ghost.
	 */
	private boolean placeAt(Minecraft mc, BlockPos target)
	{
		if (mc.level.getBlockState(target).canBeReplaced() == false)
		{
			this.generated.remove(target);
			return false;
		}

		// The game refuses a block inside the player; do not keep asking.
		if (mc.player.getBoundingBox().intersects(new AABB(target)))
		{
			return false;
		}

		BlockHitResult result = this.findPlacementClick(mc, target);

		if (result == null)
		{
			return false;
		}

		boolean ok = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, result).consumesAction();

		if (Configs.Generic.DEBUG_LOGGING.getValue())
		{
			StencilClient.LOGGER.info("place target={} click={} face={} hit={} ok={} now={}", target, result.getBlockPos(), result.getDirection(), result.getLocation(), ok, mc.level.getBlockState(target));
		}

		// A refused ghost comes back on the next scan, not on the next tick.
		if (ok == false)
		{
			this.generated.remove(target);
			return false;
		}

		mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
		this.generated.remove(target);
		this.placed.add(target);
		this.dirty = true;
		return true;
	}

	/** A click on a neighbour's face that makes vanilla place into the target, or null. */
	private BlockHitResult findPlacementClick(Minecraft mc, BlockPos target)
	{
		BlockPos below = target.below();
		BlockState belowState = mc.level.getBlockState(below);

		if (belowState.canBeReplaced() == false && this.isSingleSlabOfOurs(belowState) == false)
		{
			Vec3 hit = new Vec3(target.getX() + 0.5, target.getY() + 0.25, target.getZ() + 0.5);
			return new BlockHitResult(hit, Direction.UP, below, false);
		}

		for (Direction side : Direction.Plane.HORIZONTAL)
		{
			BlockPos neighbour = target.relative(side);

			BlockState neighbourState = mc.level.getBlockState(neighbour);

			if (neighbourState.canBeReplaced() || this.isSingleSlabOfOurs(neighbourState))
			{
				continue;
			}

			Direction face = side.getOpposite();
			Vec3 hit = new Vec3(
					neighbour.getX() + 0.5 + face.getStepX() * 0.5,
					neighbour.getY() + 0.25,
					neighbour.getZ() + 0.5 + face.getStepZ() * 0.5);
			return new BlockHitResult(hit, face, neighbour, false);
		}

		return null;
	}

	/** True for a top or bottom slab of the chosen block, which a click on top would complete. */
	private boolean isSingleSlabOfOurs(BlockState state)
	{
		return state.getBlock() == this.blockState.getBlock()
				&& state.hasProperty(SlabBlock.TYPE)
				&& state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
	}

	/** The first ghost along the crosshair within reach, stepping a tenth of a block at a time. */
	private BlockPos findAimedGhost(Minecraft mc)
	{
		Vec3 eye = mc.player.getEyePosition();
		Vec3 view = mc.player.getViewVector(1.0f);
		double reach = mc.player.blockInteractionRange();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (double d = 0.0; d <= reach; d += 0.1)
		{
			pos.set(eye.x + view.x * d, eye.y + view.y * d, eye.z + view.z * d);

			if (this.generated.contains(pos))
			{
				return pos.immutable();
			}

			if (mc.level.getBlockState(pos).isAir() == false)
			{
				return null;
			}
		}

		return null;
	}

	/** Picks up a radius edited in the settings screen, which bypasses the hotkeys. */
	private void syncRadius()
	{
		SpawnProofArea area = this.getArea();
		int radius = this.mode.getRadius().get();

		if (area.getBaseRadius() != radius)
		{
			area.setAll(radius);
			this.dirty = true;
		}
	}

	private void rescan(Level world, BlockPos center)
	{
		SpawnProofArea area = this.getArea();
		Set<BlockPos> found = this.mode == SpawnProofMode.LAYER
				? SpawnProofScanner.scanLayer(world, center, area, this.excluded)
				: SpawnProofScanner.scan(world, center, area, this.mode.getRadius().get(), this.excluded);

		if (Configs.Generic.SPAWN_PROOF_SINGLE_LAYER.getValue())
		{
			found.removeIf(pos -> this.isOnOurBlock(world, pos));
		}

		this.lastCenter = center;
		this.dirty = false;
		this.ticksSinceScan = 0;
		this.generated.clear();
		this.generated.addAll(found);
	}

	/** True when the spot sits on a block we placed, or on the chosen block, so nothing stacks. */
	private boolean isOnOurBlock(Level world, BlockPos pos)
	{
		BlockPos below = pos.below();

		if (this.placed.contains(below))
		{
			return true;
		}

		return this.blockState != null && world.getBlockState(below).getBlock() == this.blockState.getBlock();
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

		int stroke = Configs.Colors.SPAWN_PROOF_GHOST_COLOR.get() | 0xFF000000;
		GizmoStyle style = GizmoStyle.stroke(stroke, 1.0f);
		AABB bounds = new AABB(BlockPos.ZERO);

		if (this.blockState != null)
		{
			VoxelShape shape = this.blockState.getShape(mc.level, BlockPos.ZERO);
			bounds = shape.isEmpty() ? bounds : shape.bounds();
		}

		try (Gizmos.TemporaryCollection ignored = mc.collectPerTickGizmos())
		{
			for (BlockPos pos : this.generated)
			{
				Gizmos.cuboid(bounds.move(pos), style);
			}
		}
	}
}
