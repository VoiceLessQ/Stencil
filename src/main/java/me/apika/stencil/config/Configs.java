package me.apika.stencil.config;

import java.util.List;

import me.apika.stencil.config.ConfigOption.Bool;
import me.apika.stencil.config.ConfigOption.Color;
import me.apika.stencil.config.ConfigOption.Dbl;
import me.apika.stencil.config.ConfigOption.Int;
import me.apika.stencil.config.ConfigOption.OptionList;
import me.apika.stencil.config.ConfigOption.Str;
import me.apika.stencil.config.values.ConfigValues.BlockInfoAlignment;
import me.apika.stencil.config.values.ConfigValues.CornerSelectionMode;
import me.apika.stencil.config.values.ConfigValues.DataFixerMode;
import me.apika.stencil.config.values.ConfigValues.EasyPlaceProtocol;
import me.apika.stencil.config.values.ConfigValues.HudAlignment;
import me.apika.stencil.config.values.ConfigValues.MessageOutputType;
import me.apika.stencil.config.values.ConfigValues.PasteNbtBehavior;
import me.apika.stencil.config.values.ConfigValues.ReplaceBehavior;

/**
 * The settings, grouped into the same four categories Litematica uses, with the
 * same JSON keys and the same defaults. Hotkeys are the fifth category and live
 * in {@link Hotkeys}.
 *
 * Most of these describe features Stencil has not built yet. They are declared
 * up front so the config file and the settings screen are stable from the
 * start; {@link #isImplemented} is what the screen greys out.
 */
public class Configs
{
	private static final String GENERIC = "generic";
	private static final String VISUALS = "visuals";
	private static final String INFO_OVERLAYS = "info_overlays";
	private static final String COLORS = "colors";

	public static class Generic
	{
		public static final OptionList<EasyPlaceProtocol> EASY_PLACE_PROTOCOL = new OptionList<>(GENERIC, "easyPlaceProtocolVersion", EasyPlaceProtocol.AUTO);
		public static final OptionList<PasteNbtBehavior> PASTE_NBT_BEHAVIOR = new OptionList<>(GENERIC, "pasteNbtRestoreBehavior", PasteNbtBehavior.NONE);
		public static final OptionList<ReplaceBehavior> PASTE_REPLACE_BEHAVIOR = new OptionList<>(GENERIC, "pasteReplaceBehavior", ReplaceBehavior.NONE);
		public static final OptionList<ReplaceBehavior> PLACEMENT_REPLACE_BEHAVIOR = new OptionList<>(GENERIC, "placementReplaceBehavior", ReplaceBehavior.ALL);
		public static final OptionList<MessageOutputType> PLACEMENT_RESTRICTION_WARN = new OptionList<>(GENERIC, "placementRestrictionWarn", MessageOutputType.ACTIONBAR);
		public static final OptionList<CornerSelectionMode> SELECTION_CORNERS_MODE = new OptionList<>(GENERIC, "selectionCornersMode", CornerSelectionMode.CORNERS);
		public static final OptionList<DataFixerMode> DATAFIXER_MODE = new OptionList<>(GENERIC, "datafixerMode", DataFixerMode.ALWAYS);

		public static final Bool CUSTOM_SCHEMATIC_BASE_DIRECTORY_ENABLED = new Bool(GENERIC, "customSchematicBaseDirectoryEnabled", false);
		public static final Str CUSTOM_SCHEMATIC_BASE_DIRECTORY = new Str(GENERIC, "customSchematicBaseDirectory", "");

		public static final Bool AREAS_PER_WORLD = new Bool(GENERIC, "areaSelectionsPerWorld", true);
		public static final Bool CHANGE_SELECTED_CORNER = new Bool(GENERIC, "changeSelectedCornerOnMove", true);
		public static final Bool CLONE_AT_ORIGINAL_POS = new Bool(GENERIC, "cloneAtOriginalPosition", false);
		public static final Bool COMMAND_DISABLE_FEEDBACK = new Bool(GENERIC, "commandDisableFeedback", true);
		public static final Int COMMAND_FILL_MAX_VOLUME = new Int(GENERIC, "commandFillMaxVolume", 32768, 256, 10000000);
		public static final Bool COMMAND_FILL_NO_CHUNK_CLAMP = new Bool(GENERIC, "commandFillNoChunkClamp", false);
		public static final Int COMMAND_LIMIT = new Int(GENERIC, "commandLimitPerTick", 24, 1, 256);
		public static final Str COMMAND_NAME_CLONE = new Str(GENERIC, "commandNameClone", "clone");
		public static final Str COMMAND_NAME_FILL = new Str(GENERIC, "commandNameFill", "fill");
		public static final Str COMMAND_NAME_SETBLOCK = new Str(GENERIC, "commandNameSetblock", "setblock");
		public static final Str COMMAND_NAME_SUMMON = new Str(GENERIC, "commandNameSummon", "summon");
		public static final Int COMMAND_TASK_INTERVAL = new Int(GENERIC, "commandTaskInterval", 1, 1, 1000);
		public static final Bool COMMAND_USE_WORLDEDIT = new Bool(GENERIC, "commandUseWorldEdit", false);
		public static final Bool DEBUG_LOGGING = new Bool(GENERIC, "debugLogging", false);
		public static final Int DATAFIXER_DEFAULT_SCHEMA = new Int(GENERIC, "datafixerDefaultSchema", 1139, 99, 2724);
		public static final Bool EASY_PLACE_FIRST = new Bool(GENERIC, "easyPlaceFirst", true);
		public static final Bool EASY_PLACE_HOLD_ENABLED = new Bool(GENERIC, "easyPlaceHoldEnabled", true);
		public static final Bool EASY_PLACE_MODE = new Bool(GENERIC, "easyPlaceMode", false);
		public static final Bool EASY_PLACE_SP_HANDLING = new Bool(GENERIC, "easyPlaceSinglePlayerHandling", true);
		public static final Int EASY_PLACE_SWAP_INTERVAL = new Int(GENERIC, "easyPlaceSwapInterval", 0, 0, 10000);
		public static final Bool EASY_PLACE_SWING_HAND = new Bool(GENERIC, "easyPlaceSwingHand", true);
		public static final Bool EASY_PLACE_VANILLA_REACH = new Bool(GENERIC, "easyPlaceVanillaReach", false);
		public static final Bool ENTITY_DATA_SYNC = new Bool(GENERIC, "entityDataSync", true);
		public static final Bool ENTITY_DATA_SYNC_BACKUP = new Bool(GENERIC, "entityDataSyncBackup", true);
		public static final Bool EXECUTE_REQUIRE_TOOL = new Bool(GENERIC, "executeRequireHoldingTool", true);
		public static final Bool FIX_CHEST_MIRROR = new Bool(GENERIC, "fixChestMirror", true);
		public static final Bool FIX_RAIL_ROTATION = new Bool(GENERIC, "fixRailRotation", true);
		public static final Bool GENERATE_LOWERCASE_NAMES = new Bool(GENERIC, "generateLowercaseNames", false);
		public static final Bool HIGHLIGHT_BLOCK_IN_INV = new Bool(GENERIC, "highlightBlockInInventory", false);
		public static final Bool ITEM_USE_PACKET_CHECK_BYPASS = new Bool(GENERIC, "itemUsePacketCheckBypass", true);
		public static final Bool LAYER_MODE_DYNAMIC = new Bool(GENERIC, "layerModeFollowsPlayer", false);
		public static final Bool MATERIAL_LIST_IGNORE_STATE = new Bool(GENERIC, "materialListIgnoreState", false);
		public static final Bool PASTE_ALWAYS_USE_FILL = new Bool(GENERIC, "pasteAlwaysUseFill", false);
		public static final Bool PASTE_IGNORE_BE_ENTIRELY = new Bool(GENERIC, "pasteIgnoreBlockEntitiesEntirely", false);
		public static final Bool PASTE_IGNORE_BE_IN_FILL = new Bool(GENERIC, "pasteIgnoreBlockEntitiesFromFill", true);
		public static final Bool PASTE_IGNORE_CMD_LIMIT = new Bool(GENERIC, "pasteIgnoreCommandLimitWithNbtRestore", true);
		public static final Bool PASTE_IGNORE_ENTITIES = new Bool(GENERIC, "pasteIgnoreEntities", false);
		public static final Bool PASTE_IGNORE_INVENTORY = new Bool(GENERIC, "pasteIgnoreInventories", false);
		public static final Bool PASTE_TO_MCFUNCTION = new Bool(GENERIC, "pasteToMcFunctionFiles", false);
		public static final Bool PASTE_USE_FILL_COMMAND = new Bool(GENERIC, "pasteUseFillCommand", true);
		public static final Bool PASTE_USING_COMMANDS_IN_SP = new Bool(GENERIC, "pasteUsingCommandsInSp", false);
		public static final Bool PASTE_USING_SERVUX = new Bool(GENERIC, "pasteUsingServux", true);
		public static final Bool PICK_BLOCK_AVOID_DAMAGEABLE = new Bool(GENERIC, "pickBlockAvoidDamageable", true);
		public static final Bool PICK_BLOCK_AVOID_TOOLS = new Bool(GENERIC, "pickBlockAvoidTools", false);
		public static final Bool PICK_BLOCK_ENABLED = new Bool(GENERIC, "pickBlockEnabled", true);
		public static final Bool PICK_BLOCK_SHULKERS = new Bool(GENERIC, "pickBlockShulkers", false);
		public static final Str PICK_BLOCKABLE_SLOTS = new Str(GENERIC, "pickBlockableSlots", "1,2,3,4,5");
		public static final Bool PLACEMENT_RESTRICTION = new Bool(GENERIC, "placementRestriction", false);
		public static final Bool RENDER_MATERIALS_IN_GUI = new Bool(GENERIC, "renderMaterialListInGuis", true);
		public static final Bool RENDER_THREAD_NO_TIMEOUT = new Bool(GENERIC, "renderThreadNoTimeout", true);
		public static final Bool SIGN_TEXT_PASTE = new Bool(GENERIC, "signTextPaste", true);
		public static final Int SPAWN_PROOF_LAYER_RADIUS = new Int(GENERIC, "spawnProofLayerRadius", 4, 1, 32);
		public static final Int SPAWN_PROOF_RADIUS = new Int(GENERIC, "spawnProofRadius", 4, 1, 16);
		public static final Str TOOL_ITEM = new Str(GENERIC, "toolItem", "minecraft:stick");
		public static final Bool TOOL_ITEM_ENABLED = new Bool(GENERIC, "toolItemEnabled", true);
		public static final Str TOOL_ITEM_COMPONENTS = new Str(GENERIC, "toolItemComponents", "empty");
		public static final Bool UNHIDE_SCHEMATIC_PROJECTS = new Bool(GENERIC, "unhideSchematicVCS", false);

		public static final List<ConfigOption<?>> OPTIONS = List.of(
				AREAS_PER_WORLD,
				CHANGE_SELECTED_CORNER,
				CLONE_AT_ORIGINAL_POS,
				COMMAND_DISABLE_FEEDBACK,
				COMMAND_FILL_NO_CHUNK_CLAMP,
				COMMAND_USE_WORLDEDIT,
				CUSTOM_SCHEMATIC_BASE_DIRECTORY_ENABLED,
				DEBUG_LOGGING,
				DATAFIXER_MODE,
				DATAFIXER_DEFAULT_SCHEMA,
				EASY_PLACE_FIRST,
				EASY_PLACE_HOLD_ENABLED,
				EASY_PLACE_MODE,
				EASY_PLACE_SP_HANDLING,
				EASY_PLACE_PROTOCOL,
				EASY_PLACE_SWING_HAND,
				EASY_PLACE_VANILLA_REACH,
				ENTITY_DATA_SYNC,
				ENTITY_DATA_SYNC_BACKUP,
				EXECUTE_REQUIRE_TOOL,
				FIX_CHEST_MIRROR,
				FIX_RAIL_ROTATION,
				GENERATE_LOWERCASE_NAMES,
				HIGHLIGHT_BLOCK_IN_INV,
				ITEM_USE_PACKET_CHECK_BYPASS,
				LAYER_MODE_DYNAMIC,
				MATERIAL_LIST_IGNORE_STATE,
				PASTE_ALWAYS_USE_FILL,
				PASTE_IGNORE_BE_ENTIRELY,
				PASTE_IGNORE_BE_IN_FILL,
				PASTE_IGNORE_CMD_LIMIT,
				PASTE_IGNORE_ENTITIES,
				PASTE_IGNORE_INVENTORY,
				PASTE_NBT_BEHAVIOR,
				PASTE_TO_MCFUNCTION,
				PASTE_USE_FILL_COMMAND,
				PASTE_USING_COMMANDS_IN_SP,
				PASTE_USING_SERVUX,
				PICK_BLOCK_AVOID_DAMAGEABLE,
				PICK_BLOCK_AVOID_TOOLS,
				PICK_BLOCK_ENABLED,
				PICK_BLOCK_SHULKERS,
				PLACEMENT_REPLACE_BEHAVIOR,
				PLACEMENT_RESTRICTION,
				PLACEMENT_RESTRICTION_WARN,
				RENDER_MATERIALS_IN_GUI,
				RENDER_THREAD_NO_TIMEOUT,
				SIGN_TEXT_PASTE,
				TOOL_ITEM_ENABLED,
				UNHIDE_SCHEMATIC_PROJECTS,

				PASTE_REPLACE_BEHAVIOR,
				SELECTION_CORNERS_MODE,

				COMMAND_FILL_MAX_VOLUME,
				COMMAND_LIMIT,
				COMMAND_NAME_CLONE,
				COMMAND_NAME_FILL,
				COMMAND_NAME_SETBLOCK,
				COMMAND_NAME_SUMMON,
				COMMAND_TASK_INTERVAL,
				CUSTOM_SCHEMATIC_BASE_DIRECTORY,
				EASY_PLACE_SWAP_INTERVAL,
				PICK_BLOCKABLE_SLOTS,
				SPAWN_PROOF_LAYER_RADIUS,
				SPAWN_PROOF_RADIUS,
				TOOL_ITEM,
				TOOL_ITEM_COMPONENTS
		);
	}

	public static class Visuals
	{
		public static final Bool ENABLE_AREA_SELECTION_RENDERING = new Bool(VISUALS, "enableAreaSelectionBoxesRendering", true);
		public static final Bool ENABLE_PLACEMENT_BOXES_RENDERING = new Bool(VISUALS, "enablePlacementBoxesRendering", true);
		public static final Bool ENABLE_RENDERING = new Bool(VISUALS, "enableRendering", true);
		public static final Bool ENABLE_SCHEMATIC_BLOCKS = new Bool(VISUALS, "enableSchematicBlocksRendering", true);
		public static final Bool ENABLE_SCHEMATIC_OVERLAY = new Bool(VISUALS, "enableSchematicOverlay", true);
		public static final Bool ENABLE_SCHEMATIC_RENDERING = new Bool(VISUALS, "enableSchematicRendering", true);
		public static final Dbl GHOST_BLOCK_ALPHA = new Dbl(VISUALS, "ghostBlockAlpha", 0.5, 0, 1);
		public static final Bool IGNORE_EXISTING_FLUIDS = new Bool(VISUALS, "ignoreExistingFluids", false);
		public static final Bool OVERLAY_REDUCED_INNER_SIDES = new Bool(VISUALS, "overlayReducedInnerSides", false);
		public static final Dbl PLACEMENT_BOX_SIDE_ALPHA = new Dbl(VISUALS, "placementBoxSideAlpha", 0.2, 0, 1);
		public static final Bool RENDER_AREA_SELECTION_BOX_SIDES = new Bool(VISUALS, "renderAreaSelectionBoxSides", true);
		public static final Bool RENDER_BLOCKS_AS_TRANSLUCENT = new Bool(VISUALS, "renderBlocksAsTranslucent", false);
		public static final Bool RENDER_COLLIDING_SCHEMATIC_BLOCKS = new Bool(VISUALS, "renderCollidingSchematicBlocks", false);
		public static final Bool RENDER_ERROR_MARKER_CONNECTIONS = new Bool(VISUALS, "renderErrorMarkerConnections", false);
		public static final Bool RENDER_ERROR_MARKER_SIDES = new Bool(VISUALS, "renderErrorMarkerSides", true);
		public static final Bool RENDER_PLACEMENT_BOX_SIDES = new Bool(VISUALS, "renderPlacementBoxSides", false);
		public static final Bool RENDER_PLACEMENT_ENCLOSING_BOX = new Bool(VISUALS, "renderPlacementEnclosingBox", true);
		public static final Bool RENDER_PLACEMENT_ENCLOSING_BOX_SIDES = new Bool(VISUALS, "renderPlacementEnclosingBoxSides", false);
		public static final Bool RENDER_TRANSLUCENT_INNER_SIDES = new Bool(VISUALS, "renderTranslucentBlockInnerSides", false);
		public static final Bool SCHEMATIC_OVERLAY_ENABLE_OUTLINES = new Bool(VISUALS, "schematicOverlayEnableOutlines", true);
		public static final Bool SCHEMATIC_OVERLAY_ENABLE_RESORTING = new Bool(VISUALS, "schematicOverlayEnableResorting", false);
		public static final Bool SCHEMATIC_OVERLAY_ENABLE_SIDES = new Bool(VISUALS, "schematicOverlayEnableSides", true);
		public static final Bool SCHEMATIC_OVERLAY_MODEL_OUTLINE = new Bool(VISUALS, "schematicOverlayModelOutline", true);
		public static final Bool SCHEMATIC_OVERLAY_MODEL_SIDES = new Bool(VISUALS, "schematicOverlayModelSides", false);
		public static final Dbl SCHEMATIC_OVERLAY_OUTLINE_WIDTH = new Dbl(VISUALS, "schematicOverlayOutlineWidth", 1.0, 0, 64);
		public static final Dbl SCHEMATIC_OVERLAY_OUTLINE_WIDTH_THROUGH = new Dbl(VISUALS, "schematicOverlayOutlineWidthThrough", 3.0, 0, 64);
		public static final Bool SCHEMATIC_OVERLAY_RENDER_THROUGH = new Bool(VISUALS, "schematicOverlayRenderThroughBlocks", false);
		public static final Bool SCHEMATIC_OVERLAY_TYPE_EXTRA = new Bool(VISUALS, "schematicOverlayTypeExtra", true);
		public static final Bool SCHEMATIC_OVERLAY_TYPE_MISSING = new Bool(VISUALS, "schematicOverlayTypeMissing", true);
		public static final Bool SCHEMATIC_OVERLAY_TYPE_WRONG_BLOCK = new Bool(VISUALS, "schematicOverlayTypeWrongBlock", true);
		public static final Bool SCHEMATIC_OVERLAY_TYPE_WRONG_STATE = new Bool(VISUALS, "schematicOverlayTypeWrongState", true);
		public static final Bool SCHEMATIC_VERIFIER_BLOCK_MODELS = new Bool(VISUALS, "schematicVerifierUseBlockModels", false);

		public static final List<ConfigOption<?>> OPTIONS = List.of(
				ENABLE_RENDERING,
				ENABLE_SCHEMATIC_RENDERING,

				ENABLE_AREA_SELECTION_RENDERING,
				ENABLE_PLACEMENT_BOXES_RENDERING,
				ENABLE_SCHEMATIC_BLOCKS,
				ENABLE_SCHEMATIC_OVERLAY,
				IGNORE_EXISTING_FLUIDS,
				OVERLAY_REDUCED_INNER_SIDES,
				RENDER_AREA_SELECTION_BOX_SIDES,
				RENDER_BLOCKS_AS_TRANSLUCENT,
				RENDER_COLLIDING_SCHEMATIC_BLOCKS,
				RENDER_ERROR_MARKER_CONNECTIONS,
				RENDER_ERROR_MARKER_SIDES,
				RENDER_PLACEMENT_BOX_SIDES,
				RENDER_PLACEMENT_ENCLOSING_BOX,
				RENDER_PLACEMENT_ENCLOSING_BOX_SIDES,
				RENDER_TRANSLUCENT_INNER_SIDES,
				SCHEMATIC_OVERLAY_ENABLE_OUTLINES,
				SCHEMATIC_OVERLAY_ENABLE_RESORTING,
				SCHEMATIC_OVERLAY_ENABLE_SIDES,
				SCHEMATIC_OVERLAY_MODEL_OUTLINE,
				SCHEMATIC_OVERLAY_MODEL_SIDES,
				SCHEMATIC_OVERLAY_RENDER_THROUGH,
				SCHEMATIC_OVERLAY_TYPE_EXTRA,
				SCHEMATIC_OVERLAY_TYPE_MISSING,
				SCHEMATIC_OVERLAY_TYPE_WRONG_BLOCK,
				SCHEMATIC_OVERLAY_TYPE_WRONG_STATE,
				SCHEMATIC_VERIFIER_BLOCK_MODELS,

				GHOST_BLOCK_ALPHA,
				PLACEMENT_BOX_SIDE_ALPHA,
				SCHEMATIC_OVERLAY_OUTLINE_WIDTH,
				SCHEMATIC_OVERLAY_OUTLINE_WIDTH_THROUGH
		);
	}

	public static class InfoOverlays
	{
		public static final OptionList<HudAlignment> BLOCK_INFO_LINES_ALIGNMENT = new OptionList<>(INFO_OVERLAYS, "blockInfoLinesAlignment", HudAlignment.TOP_RIGHT);
		public static final OptionList<BlockInfoAlignment> BLOCK_INFO_OVERLAY_ALIGNMENT = new OptionList<>(INFO_OVERLAYS, "blockInfoOverlayAlignment", BlockInfoAlignment.TOP_CENTER);
		public static final OptionList<HudAlignment> INFO_HUD_ALIGNMENT = new OptionList<>(INFO_OVERLAYS, "infoHudAlignment", HudAlignment.BOTTOM_RIGHT);
		public static final OptionList<HudAlignment> TOOL_HUD_ALIGNMENT = new OptionList<>(INFO_OVERLAYS, "toolHudAlignment", HudAlignment.BOTTOM_LEFT);

		public static final Bool BLOCK_INFO_LINES_ENABLED = new Bool(INFO_OVERLAYS, "blockInfoLinesEnabled", true);
		public static final Dbl BLOCK_INFO_LINES_FONT_SCALE = new Dbl(INFO_OVERLAYS, "blockInfoLinesFontScale", 0.5, 0, 10);
		public static final Int BLOCK_INFO_LINES_OFFSET_X = new Int(INFO_OVERLAYS, "blockInfoLinesOffsetX", 4, 0, 2000);
		public static final Int BLOCK_INFO_LINES_OFFSET_Y = new Int(INFO_OVERLAYS, "blockInfoLinesOffsetY", 4, 0, 2000);
		public static final Int BLOCK_INFO_OVERLAY_OFFSET_Y = new Int(INFO_OVERLAYS, "blockInfoOverlayOffsetY", 6, -2000, 2000);
		public static final Bool BLOCK_INFO_OVERLAY_ENABLED = new Bool(INFO_OVERLAYS, "blockInfoOverlayEnabled", true);
		public static final Int INFO_HUD_MAX_LINES = new Int(INFO_OVERLAYS, "infoHudMaxLines", 10, 1, 128);
		public static final Int INFO_HUD_OFFSET_X = new Int(INFO_OVERLAYS, "infoHudOffsetX", 1, 0, 32000);
		public static final Int INFO_HUD_OFFSET_Y = new Int(INFO_OVERLAYS, "infoHudOffsetY", 1, 0, 32000);
		public static final Dbl INFO_HUD_SCALE = new Dbl(INFO_OVERLAYS, "infoHudScale", 1, 0.1, 4);
		public static final Bool INFO_OVERLAYS_TARGET_FLUIDS = new Bool(INFO_OVERLAYS, "infoOverlaysTargetFluids", false);
		public static final Int MATERIAL_LIST_HUD_MAX_LINES = new Int(INFO_OVERLAYS, "materialListHudMaxLines", 10, 1, 128);
		public static final Dbl MATERIAL_LIST_HUD_SCALE = new Dbl(INFO_OVERLAYS, "materialListHudScale", 1, 0.1, 4);
		public static final Bool STATUS_INFO_HUD = new Bool(INFO_OVERLAYS, "statusInfoHud", false);
		public static final Bool STATUS_INFO_HUD_AUTO = new Bool(INFO_OVERLAYS, "statusInfoHudAuto", true);
		public static final Int TOOL_HUD_OFFSET_X = new Int(INFO_OVERLAYS, "toolHudOffsetX", 1, 0, 32000);
		public static final Int TOOL_HUD_OFFSET_Y = new Int(INFO_OVERLAYS, "toolHudOffsetY", 1, 0, 32000);
		public static final Dbl TOOL_HUD_SCALE = new Dbl(INFO_OVERLAYS, "toolHudScale", 1, 0.1, 4);
		public static final Dbl VERIFIER_ERROR_HILIGHT_ALPHA = new Dbl(INFO_OVERLAYS, "verifierErrorHilightAlpha", 0.2, 0, 1);
		public static final Int VERIFIER_ERROR_HILIGHT_MAX_POSITIONS = new Int(INFO_OVERLAYS, "verifierErrorHilightMaxPositions", 1000, 1, 1000000);
		public static final Bool VERIFIER_OVERLAY_ENABLED = new Bool(INFO_OVERLAYS, "verifierOverlayEnabled", true);
		public static final Bool WARN_DISABLED_RENDERING = new Bool(INFO_OVERLAYS, "warnDisabledRendering", true);

		public static final List<ConfigOption<?>> OPTIONS = List.of(
				BLOCK_INFO_LINES_ENABLED,
				BLOCK_INFO_OVERLAY_ENABLED,
				INFO_OVERLAYS_TARGET_FLUIDS,
				STATUS_INFO_HUD,
				STATUS_INFO_HUD_AUTO,
				VERIFIER_OVERLAY_ENABLED,
				WARN_DISABLED_RENDERING,

				BLOCK_INFO_LINES_ALIGNMENT,
				BLOCK_INFO_OVERLAY_ALIGNMENT,
				INFO_HUD_ALIGNMENT,
				TOOL_HUD_ALIGNMENT,

				BLOCK_INFO_LINES_OFFSET_X,
				BLOCK_INFO_LINES_OFFSET_Y,
				BLOCK_INFO_LINES_FONT_SCALE,
				BLOCK_INFO_OVERLAY_OFFSET_Y,
				INFO_HUD_MAX_LINES,
				INFO_HUD_OFFSET_X,
				INFO_HUD_OFFSET_Y,
				INFO_HUD_SCALE,
				MATERIAL_LIST_HUD_MAX_LINES,
				MATERIAL_LIST_HUD_SCALE,
				TOOL_HUD_OFFSET_X,
				TOOL_HUD_OFFSET_Y,
				TOOL_HUD_SCALE,
				VERIFIER_ERROR_HILIGHT_ALPHA,
				VERIFIER_ERROR_HILIGHT_MAX_POSITIONS
		);
	}

	public static class Colors
	{
		public static final Color AREA_SELECTION_BOX_SIDE_COLOR = new Color(COLORS, "areaSelectionBoxSideColor", "#30FFFFFF");
		public static final Color HIGHTLIGHT_BLOCK_IN_INV_COLOR = new Color(COLORS, "hightlightBlockInInventoryColor", "#30FF30FF");
		public static final Color MATERIAL_LIST_HUD_ITEM_COUNTS = new Color(COLORS, "materialListHudItemCountsColor", "#FFFFAA00");
		public static final Color REBUILD_BREAK_OVERLAY_COLOR = new Color(COLORS, "schematicRebuildBreakPlaceOverlayColor", "#4C33CC33");
		public static final Color REBUILD_BREAK_EXCEPT_OVERLAY_COLOR = new Color(COLORS, "schematicRebuildBreakExceptPlaceOverlayColor", "#4CF03030");
		public static final Color REBUILD_REPLACE_OVERLAY_COLOR = new Color(COLORS, "schematicRebuildReplaceOverlayColor", "#4CF0A010");
		public static final Color SCHEMATIC_OVERLAY_COLOR_EXTRA = new Color(COLORS, "schematicOverlayColorExtra", "#4CFF4CE6");
		public static final Color SCHEMATIC_OVERLAY_COLOR_MISSING = new Color(COLORS, "schematicOverlayColorMissing", "#2C33B3E6");
		public static final Color SCHEMATIC_OVERLAY_COLOR_WRONG_BLOCK = new Color(COLORS, "schematicOverlayColorWrongBlock", "#4CFF3333");
		public static final Color SCHEMATIC_OVERLAY_COLOR_WRONG_STATE = new Color(COLORS, "schematicOverlayColorWrongState", "#4CFF9010");
		public static final Color SPAWN_PROOF_GHOST_COLOR = new Color(COLORS, "spawnProofGhostColor", "#60FF8800");

		public static final List<ConfigOption<?>> OPTIONS = List.of(
				AREA_SELECTION_BOX_SIDE_COLOR,
				HIGHTLIGHT_BLOCK_IN_INV_COLOR,
				MATERIAL_LIST_HUD_ITEM_COUNTS,
				REBUILD_BREAK_OVERLAY_COLOR,
				REBUILD_BREAK_EXCEPT_OVERLAY_COLOR,
				REBUILD_REPLACE_OVERLAY_COLOR,
				SCHEMATIC_OVERLAY_COLOR_EXTRA,
				SCHEMATIC_OVERLAY_COLOR_MISSING,
				SCHEMATIC_OVERLAY_COLOR_WRONG_BLOCK,
				SCHEMATIC_OVERLAY_COLOR_WRONG_STATE,
				SPAWN_PROOF_GHOST_COLOR
		);
	}

	/**
	 * Whether the feature behind an option actually exists yet. Everything is
	 * declared, almost nothing is wired, and the settings screen says so rather
	 * than offering a toggle that does nothing.
	 */
	public static boolean isImplemented(ConfigOption<?> option)
	{
		return IMPLEMENTED.contains(option);
	}

	private static final List<ConfigOption<?>> IMPLEMENTED = List.of(
			Generic.DEBUG_LOGGING,
			Generic.CUSTOM_SCHEMATIC_BASE_DIRECTORY_ENABLED,
			Generic.CUSTOM_SCHEMATIC_BASE_DIRECTORY,
			Generic.SPAWN_PROOF_LAYER_RADIUS,
			Generic.SPAWN_PROOF_RADIUS,
			Colors.SPAWN_PROOF_GHOST_COLOR,
			Hotkeys.OPEN_GUI_SETTINGS,
			Hotkeys.SPAWN_PROOF_TOGGLE,
			Hotkeys.SPAWN_PROOF_MODE,
			Hotkeys.SPAWN_PROOF_SHAPE,
			Hotkeys.SPAWN_PROOF_ADJUST_SIDE,
			Hotkeys.SPAWN_PROOF_RADIUS_INCREASE,
			Hotkeys.SPAWN_PROOF_RADIUS_DECREASE
	);
}
