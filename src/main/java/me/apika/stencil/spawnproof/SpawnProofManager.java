package me.apika.stencil.spawnproof;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.Util;
import net.minecraft.server.permissions.Permissions;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpawnProofManager
{
	private static final SpawnProofManager INSTANCE = new SpawnProofManager();
	private static final int RESCAN_INTERVAL_TICKS = 20;
	private static final int LIGHT_PLAN_VERTICAL_RADIUS = 16;
	private static final String REACH_ATTRIBUTE = "minecraft:block_interaction_range";

	private final Keybind toggle = new Keybind(Hotkeys.SPAWN_PROOF_TOGGLE);
	private final Keybind modeCycle = new Keybind(Hotkeys.SPAWN_PROOF_MODE);
	private final Keybind shapeCycle = new Keybind(Hotkeys.SPAWN_PROOF_SHAPE);
	private final Keybind toolSelect = new Keybind(Hotkeys.TOOL_SELECT);
	private final Keybind radiusIncrease = new Keybind(Hotkeys.SPAWN_PROOF_RADIUS_INCREASE);
	private final Keybind radiusDecrease = new Keybind(Hotkeys.SPAWN_PROOF_RADIUS_DECREASE);

	private final Set<BlockPos> generated = new HashSet<>();
	private final Set<BlockPos> excluded = new HashSet<>();
	private final Set<BlockPos> placed = new HashSet<>();
	private BlockState blockState;
	private ToolAction action = ToolAction.PLACE;
	private final Map<SpawnProofMode, SpawnProofArea> areas = new EnumMap<>(SpawnProofMode.class);
	private final SpawnProofArea lightArea = new SpawnProofArea(Configs.Generic.LIGHT_PLAN_RADIUS);
	private SpawnProofMode mode = SpawnProofMode.SPAWN_PROOF;
	private boolean enabled;
	private boolean dirty;
	private BlockPos lastCenter;
	private int ticksSinceScan;
	private CompletableFuture<Set<BlockPos>> pendingScan;
	private int appliedReach;
	private boolean reachWarned;
	private BlockPos markCorner;
	private BoundingBox marked;
	private int reportedNeeded = -1;
	private int reportedCarried = -1;
	private BlockPos replaceTarget;
	private Direction replaceFace;
	private boolean useArmed = true;

	private SpawnProofManager()
	{
		for (SpawnProofMode mode : SpawnProofMode.values())
		{
			this.areas.put(mode, new SpawnProofArea(mode.getRadius()));
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

	public BlockState getBlockState()
	{
		return this.blockState;
	}

	private boolean isLightBlock()
	{
		return this.blockState != null && this.blockState.getLightEmission() > Configs.Generic.TORCH_MAX_SPAWN_LIGHT.get();
	}

	public SpawnProofMode getMode()
	{
		return this.mode;
	}

	public SpawnProofArea getArea()
	{
		return this.isLightPlanning() ? this.lightArea : this.areas.get(this.mode);
	}

	private ConfigOption.Int getRadius()
	{
		return this.isLightPlanning() ? Configs.Generic.LIGHT_PLAN_RADIUS : this.mode.getRadius();
	}

	private boolean isLightPlanning()
	{
		return this.mode == SpawnProofMode.SPAWN_PROOF && this.isLightBlock();
	}

	public void clear()
	{
		this.generated.clear();
		this.excluded.clear();
		this.placed.clear();
		this.pendingScan = null;
		this.lastCenter = null;
		this.appliedReach = 0;
		this.reachWarned = false;
		this.markCorner = null;
		this.marked = null;
		this.replaceTarget = null;
	}

	public boolean onScroll(double yOffset)
	{
		Minecraft mc = Minecraft.getInstance();

		if (mc.player == null || mc.gui.screen() != null || yOffset == 0.0 || this.isHoldingTool(mc) == false)
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

		if (this.action == ToolAction.EXTEND_SIDE && this.enabled)
		{
			this.extendFacingSide(mc, up);
			return true;
		}

		return false;
	}

	private boolean isHoldingTool(Minecraft mc)
	{
		String wanted = Configs.Generic.TOOL_ITEM.get().trim();

		if (wanted.indexOf(':') < 0)
		{
			wanted = "minecraft:" + wanted;
		}

		return BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).toString().equals(wanted);
	}

	private void extendFacingSide(Minecraft mc, boolean up)
	{
		if (this.marked != null)
		{
			Direction side = mc.player.getNearestViewDirection();
			this.setMarked(grow(this.marked, side, up ? 1 : -1));
			mc.player.sendOverlayMessage(Component.literal("Marked " + side.getName() + ": " + describe(this.marked)));
			return;
		}

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

		this.syncReach(mc);
		this.useArmed = mc.options.keyUse.isDown() == false;

		if (this.enabled == false)
		{
			return;
		}

		BlockPos center = this.scanCenter(mc.player.blockPosition());
		++this.ticksSinceScan;
		this.syncRadius();
		this.finishScan();

		if (this.pendingScan == null &&
			(this.dirty ||
			center.equals(this.lastCenter) == false ||
			this.ticksSinceScan >= RESCAN_INTERVAL_TICKS))
		{
			this.startScan(mc.level, center);
		}

		if (this.marked != null && this.pendingScan == null && this.dirty == false && this.isHoldingTool(mc))
		{
			this.reportNeeded(mc);
		}
		else if (this.isHoldingTool(mc) == false)
		{
			this.reportedNeeded = -1;
		}

		this.emitGizmos(mc);
	}

	private BlockPos scanCenter(BlockPos player)
	{
		if (this.marked == null)
		{
			return player;
		}

		BlockPos middle = this.marked.getCenter();
		return this.mode == SpawnProofMode.LAYER ? new BlockPos(middle.getX(), player.getY(), middle.getZ()) : middle;
	}

	private void setMarked(BoundingBox box)
	{
		this.marked = box;
		this.pendingScan = null;
		this.dirty = true;
		this.reportedNeeded = -1;
	}

	private static BoundingBox grow(BoundingBox box, Direction side, int amount)
	{
		int minX = box.minX();
		int minY = box.minY();
		int minZ = box.minZ();
		int maxX = box.maxX();
		int maxY = box.maxY();
		int maxZ = box.maxZ();

		switch (side)
		{
			case WEST -> minX = Math.min(minX - amount, maxX);
			case EAST -> maxX = Math.max(maxX + amount, minX);
			case NORTH -> minZ = Math.min(minZ - amount, maxZ);
			case SOUTH -> maxZ = Math.max(maxZ + amount, minZ);
			case DOWN -> minY = Math.min(minY - amount, maxY);
			case UP -> maxY = Math.max(maxY + amount, minY);
		}

		return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private static String describe(BoundingBox box)
	{
		return box.getXSpan() + "x" + box.getZSpan() + ", " + box.getYSpan() + " high";
	}

	private void markAreaCorner(Minecraft mc)
	{
		if (mc.player.isShiftKeyDown())
		{
			this.markCorner = null;

			if (this.marked != null)
			{
				this.setMarked(null);
			}

			mc.player.sendOverlayMessage(Component.literal("Marked area cleared"));
			return;
		}

		if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK)
		{
			mc.player.sendOverlayMessage(Component.literal("Aim at a block to mark a corner"));
			return;
		}

		BlockPos pos = hit.getBlockPos();

		if (this.markCorner == null)
		{
			this.markCorner = pos;
			mc.player.sendOverlayMessage(Component.literal("Corner set, now mark the opposite corner"));
			return;
		}

		BoundingBox box = BoundingBox.fromCorners(this.markCorner, pos);
		this.markCorner = null;
		this.setMarked(new BoundingBox(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY() + 1, box.maxZ()));
		mc.player.sendOverlayMessage(Component.literal("Marked area " + describe(this.marked)));
	}

	private void reportNeeded(Minecraft mc)
	{
		int needed = this.generated.size();
		int carried = this.blockState == null || mc.player.hasInfiniteMaterials() ? -1 : countCarried(mc.player.getInventory(), this.blockState.getBlock().asItem());

		if (needed == this.reportedNeeded && carried == this.reportedCarried)
		{
			return;
		}

		this.reportedNeeded = needed;
		this.reportedCarried = carried;
		mc.player.sendOverlayMessage(Component.literal(this.describeNeeded(needed, carried)));
	}

	private String describeNeeded(int needed, int carried)
	{
		if (needed == 0)
		{
			return "Marked area: nothing left to place";
		}

		if (this.blockState == null)
		{
			return "Marked area: " + needed + " to place, choose a block with Change block";
		}

		String text = "Marked area: " + needed + " " + this.blockState.getBlock().getName().getString() + " to place";

		if (carried < 0)
		{
			return text;
		}

		return carried >= needed ? text + ", " + carried + " carried" : text + ", " + carried + " carried, " + (needed - carried) + " short";
	}

	private static int countCarried(Inventory inventory, Item item)
	{
		int count = 0;

		for (ItemStack stack : inventory.getNonEquipmentItems())
		{
			if (stack.getItem() == item)
			{
				count += stack.getCount();
			}
		}

		return count;
	}

	private void handleHotkeys(Minecraft mc)
	{
		this.toggle.tick();
		this.modeCycle.tick();
		this.shapeCycle.tick();
		this.toolSelect.tick();
		this.radiusIncrease.tick();
		this.radiusDecrease.tick();

		if (this.toggle.wasTriggered())
		{
			this.enabled = !this.enabled;
			this.generated.clear();
			this.pendingScan = null;
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
			this.pendingScan = null;
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
			ConfigOption.Int radius = this.getRadius();
			radius.setValue(radius.get() + delta);
			ConfigStorage.save();
			this.getArea().setAll(radius.get());
			this.dirty = true;
			mc.player.sendOverlayMessage(Component.literal(this.mode.getDisplayName() + " radius: " + radius.get()));
		}
	}

	public boolean onUse()
	{
		Minecraft mc = Minecraft.getInstance();

		if (this.enabled == false || mc.player == null || mc.level == null || mc.gui.screen() != null || this.isHoldingTool(mc) == false)
		{
			return false;
		}

		boolean fresh = this.useArmed;
		this.useArmed = false;

		switch (this.action)
		{
			case PLACE -> this.placeAimed(mc);
			case PLACE_ALL -> this.placeAll(mc, true);
			case REPLACE -> this.startReplace(mc, fresh);
			case CHANGE_BLOCK -> this.chooseHeldBlock(mc);
			case EXTEND_SIDE -> {}
			case MARK_AREA ->
			{
				if (fresh)
				{
					this.markAreaCorner(mc);
				}
			}
		}

		return true;
	}

	private void startReplace(Minecraft mc, boolean fresh)
	{
		if (this.replaceTarget != null)
		{
			return;
		}

		if (this.blockState == null)
		{
			if (fresh)
			{
				mc.player.sendOverlayMessage(Component.literal("No block chosen: use the Change block tool on a ghost"));
			}

			return;
		}

		if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK)
		{
			return;
		}

		BlockPos pos = hit.getBlockPos();
		String refusal = this.replaceRefusal(mc.level, pos, mc.level.getBlockState(pos));

		if (refusal != null)
		{
			if (fresh)
			{
				mc.player.sendOverlayMessage(Component.literal(refusal));
			}

			return;
		}

		this.replaceTarget = pos;
		this.replaceFace = hit.getDirection();
		mc.gameMode.startDestroyBlock(pos, this.replaceFace);
		mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
	}

	private String replaceRefusal(Level level, BlockPos pos, BlockState state)
	{
		String name = state.getBlock().getName().getString();

		if (this.marked != null && this.marked.isInside(pos) == false)
		{
			return "Outside the marked area";
		}

		if (state.getBlock() == this.blockState.getBlock())
		{
			return "Already " + name;
		}

		if (state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0.0f)
		{
			return name + " cannot be replaced";
		}

		if (this.placed.contains(pos) == false && state.isValidSpawn(level, pos, EntityTypes.ZOMBIE))
		{
			return "Only spawn proofing blocks can be replaced";
		}

		return null;
	}

	public boolean continueReplace()
	{
		Minecraft mc = Minecraft.getInstance();
		BlockPos target = this.replaceTarget;

		if (target == null || mc.player == null || mc.level == null)
		{
			return false;
		}

		if (this.enabled == false || this.blockState == null || mc.options.keyUse.isDown() == false || this.isHoldingTool(mc) == false)
		{
			this.stopReplace(mc);
			return false;
		}

		if (mc.level.getBlockState(target).canBeReplaced())
		{
			this.replaceTarget = null;
			this.withChosenBlockInHand(mc, true, () -> this.placeAt(mc, target));
			return true;
		}

		if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK || hit.getBlockPos().equals(target) == false)
		{
			this.stopReplace(mc);
			return false;
		}

		if (mc.gameMode.continueDestroyBlock(target, this.replaceFace))
		{
			mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
		}

		return true;
	}

	private void stopReplace(Minecraft mc)
	{
		this.replaceTarget = null;
		mc.gameMode.stopDestroyBlock();
	}

	private boolean chooseHeldBlock(Minecraft mc)
	{
		BlockPos target = this.findAimedGhost(mc);

		if (target == null && this.generated.isEmpty() == false)
		{
			return false;
		}

		if (!(mc.player.getOffhandItem().getItem() instanceof BlockItem item))
		{
			mc.player.sendOverlayMessage(Component.literal("Hold a block item in the off hand to choose it"));
			return true;
		}

		BlockState state = floorState(item.getBlock());
		String name = item.getBlock().getName().getString();

		if (state.getLightEmission() > Configs.Generic.TORCH_MAX_SPAWN_LIGHT.get())
		{
			this.blockState = state;
			this.dirty = true;
			mc.player.sendOverlayMessage(Component.literal("Light block: " + name + ", ghosts show where to place it"));
			return true;
		}

		if (state.isValidSpawn(mc.level, target == null ? mc.player.blockPosition() : target, EntityTypes.ZOMBIE))
		{
			mc.player.sendOverlayMessage(Component.literal(name + " does not stop spawns"));
			return true;
		}

		this.blockState = state;
		this.dirty = true;
		mc.player.sendOverlayMessage(Component.literal("Spawn proof block: " + name));
		return true;
	}

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

	private void withChosenBlockInHand(Minecraft mc, boolean report, Runnable action)
	{
		BlockState chosen = this.blockState;

		if (chosen == null)
		{
			if (report)
			{
				mc.player.sendOverlayMessage(Component.literal("No block chosen: use the Change block tool on a ghost"));
			}

			return;
		}

		Inventory inventory = mc.player.getInventory();
		Item item = chosen.getBlock().asItem();

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
				mc.player.sendOverlayMessage(Component.literal("No " + chosen.getBlock().getName().getString() + " in inventory"));
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
				mc.player.sendOverlayMessage(Component.literal("No ghosts in reach" + this.describeNearest(mc)));
			}

			if (Configs.Generic.DEBUG_LOGGING.getValue())
			{
				StencilClient.LOGGER.info("place all: none of {} ghosts within reach {}", this.generated.size(), reach);
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

	private String describeNearest(Minecraft mc)
	{
		BlockPos here = mc.player.blockPosition();
		BlockPos nearest = null;
		int best = Integer.MAX_VALUE;

		for (BlockPos pos : this.generated)
		{
			int distance = here.distManhattan(pos);

			if (distance < best)
			{
				best = distance;
				nearest = pos;
			}
		}

		if (nearest == null)
		{
			return "";
		}

		int dx = nearest.getX() - here.getX();
		int dz = nearest.getZ() - here.getZ();
		String ns = dz < 0 ? "north" : dz > 0 ? "south" : "";
		String ew = dx > 0 ? "east" : dx < 0 ? "west" : "";
		String heading = ns.isEmpty() || ew.isEmpty() ? ns + ew : ns + "-" + ew;
		return ", nearest " + best + " blocks " + heading;
	}

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

	private boolean placeAt(Minecraft mc, BlockPos target)
	{
		if (mc.level.getBlockState(target).canBeReplaced() == false)
		{
			this.generated.remove(target);
			return false;
		}

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

	private boolean isSingleSlabOfOurs(BlockState state)
	{
		return state.getBlock() == this.blockState.getBlock()
				&& state.hasProperty(SlabBlock.TYPE)
				&& state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
	}

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

	private void syncReach(Minecraft mc)
	{
		int desired = this.enabled ? Configs.Generic.COMMAND_REACH.get() : 0;

		if (desired == this.appliedReach)
		{
			return;
		}

		if (mc.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) == false)
		{
			if (this.reachWarned == false && desired > 0)
			{
				this.reachWarned = true;
				mc.player.sendOverlayMessage(Component.literal("commandReach needs cheats or op; placing stays at normal reach"));

				if (Configs.Generic.DEBUG_LOGGING.getValue())
				{
					StencilClient.LOGGER.info("reach command skipped, no gamemaster permission");
				}
			}

			return;
		}

		String command = desired == 0
				? "attribute @s " + REACH_ATTRIBUTE + " base reset"
				: "attribute @s " + REACH_ATTRIBUTE + " base set " + desired;
		mc.player.connection.sendCommand(command);
		this.appliedReach = desired;
		this.reachWarned = false;

		if (Configs.Generic.DEBUG_LOGGING.getValue())
		{
			StencilClient.LOGGER.info("sent /{}", command);
		}
	}

	private void syncRadius()
	{
		SpawnProofArea area = this.getArea();
		int radius = this.getRadius().get();

		if (area.getBaseRadius() != radius)
		{
			area.setAll(radius);
			this.dirty = true;
		}
	}

	private void startScan(Level world, BlockPos center)
	{
		SpawnProofMode mode = this.mode;
		BoundingBox bounds = this.marked;
		SpawnProofArea area = bounds == null ? this.getArea() : boxArea(bounds, center);
		BlockState state = this.blockState;
		boolean lights = this.isLightBlock();
		int radius = this.getRadius().get();
		int vertical = bounds != null ? Math.max(center.getY() - bounds.minY(), bounds.maxY() - center.getY())
				: lights ? Math.min(radius, LIGHT_PLAN_VERTICAL_RADIUS) : radius;
		boolean singleLayer = Configs.Generic.SPAWN_PROOF_SINGLE_LAYER.getValue();
		Set<BlockPos> excluded = Set.copyOf(this.excluded);
		Set<BlockPos> placed = Set.copyOf(this.placed);

		this.lastCenter = center;
		this.dirty = false;
		this.ticksSinceScan = 0;
		this.pendingScan = CompletableFuture.supplyAsync(() ->
			scan(world, center, mode, area, bounds, state, lights, vertical, singleLayer, excluded, placed), Util.backgroundExecutor());
	}

	private static SpawnProofArea boxArea(BoundingBox box, BlockPos center)
	{
		return new SpawnProofArea(center.getX() - box.minX(), box.maxX() - center.getX(), center.getZ() - box.minZ(), box.maxZ() - center.getZ());
	}

	private static Set<BlockPos> inside(Set<BlockPos> found, BoundingBox bounds)
	{
		if (bounds == null)
		{
			return found;
		}

		Set<BlockPos> kept = new HashSet<>(found);
		kept.removeIf(pos -> bounds.isInside(pos) == false);
		return kept;
	}

	private void finishScan()
	{
		if (this.pendingScan == null || this.pendingScan.isDone() == false)
		{
			return;
		}

		Set<BlockPos> found;

		try
		{
			found = new HashSet<>(this.pendingScan.join());
		}
		catch (CompletionException e)
		{
			StencilClient.LOGGER.error("Spawn proof scan failed", e.getCause());
			found = new HashSet<>();
		}

		this.pendingScan = null;
		found.removeAll(this.placed);
		found.removeAll(this.excluded);
		this.generated.clear();
		this.generated.addAll(found);
	}

	private static Set<BlockPos> scan(Level world, BlockPos center, SpawnProofMode mode, SpawnProofArea area, BoundingBox bounds, BlockState state, boolean lights, int vertical, boolean singleLayer, Set<BlockPos> excluded, Set<BlockPos> placed)
	{
		if (mode == SpawnProofMode.LAYER)
		{
			return inside(SpawnProofScanner.scanLayer(world, center, area, excluded), bounds);
		}

		if (lights)
		{
			return planLights(world, center, area, bounds, state, vertical, excluded, placed);
		}

		Set<BlockPos> found = inside(SpawnProofScanner.scan(world, center, area, vertical, excluded), bounds);

		if (singleLayer)
		{
			found.removeIf(pos -> isOnOurBlock(world, pos, state, placed));
		}

		return found;
	}

	private static Set<BlockPos> planLights(Level world, BlockPos center, SpawnProofArea area, BoundingBox bounds, BlockState state, int vertical, Set<BlockPos> excluded, Set<BlockPos> placed)
	{
		int maxLight = Configs.Generic.TORCH_MAX_SPAWN_LIGHT.get();
		Set<BlockPos> dark = inside(SpawnProofScanner.scan(world, center, area, vertical, maxLight, excluded), bounds);
		Set<BlockPos> planned = new HashSet<>(TorchPlanner.plan(world, center, area, vertical, state, maxLight, dark, placed));

		if (Configs.Generic.DEBUG_LOGGING.getValue())
		{
			StencilClient.LOGGER.info("light plan block={} emission={} dark={} planned={}", state.getBlock().getName().getString(), state.getLightEmission(), dark.size(), planned.size());
		}

		return planned;
	}

	private static boolean isOnOurBlock(Level world, BlockPos pos, BlockState state, Set<BlockPos> placed)
	{
		BlockPos below = pos.below();

		if (placed.contains(below))
		{
			return true;
		}

		return state != null && world.getBlockState(below).getBlock() == state.getBlock();
	}

	private void emitGizmos(Minecraft mc)
	{
		if (this.generated.isEmpty() && this.marked == null && this.markCorner == null)
		{
			return;
		}

		int stroke = Configs.Colors.SPAWN_PROOF_GHOST_COLOR.get() | 0xFF000000;
		GizmoStyle style = GizmoStyle.stroke(stroke, 1.0f);
		GizmoStyle markStyle = GizmoStyle.stroke(stroke, 2.0f);
		AABB bounds = new AABB(BlockPos.ZERO);

		if (this.blockState != null)
		{
			VoxelShape shape = this.blockState.getShape(mc.level, BlockPos.ZERO);
			bounds = shape.isEmpty() ? bounds : shape.bounds();
		}

		try (Gizmos.TemporaryCollection ignored = mc.collectPerTickGizmos())
		{
			if (this.marked != null)
			{
				Gizmos.cuboid(AABB.of(this.marked), markStyle);
			}

			if (this.markCorner != null)
			{
				Gizmos.cuboid(new AABB(this.markCorner), markStyle);
			}

			for (BlockPos pos : this.generated)
			{
				Gizmos.cuboid(bounds.move(pos), style);
			}
		}
	}
}
