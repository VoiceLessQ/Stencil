package me.apika.stencil.config;

import java.util.List;

import me.apika.stencil.config.ConfigOption.Hotkey;

/**
 * The keybinds, with Litematica's defaults kept as-is so the muscle memory
 * transfers: M opens the main menu, M,A adds a selection box, and so on.
 */
public class Hotkeys
{
	public static final Hotkey ADD_SELECTION_BOX = new Hotkey("addSelectionBox", "M,A");
	public static final Hotkey CLONE_SELECTION = new Hotkey("cloneSelection", "");
	public static final Hotkey DELETE_SELECTION_BOX = new Hotkey("deleteSelectionBox", "");
	public static final Hotkey EASY_PLACE_ACTIVATION = new Hotkey("easyPlaceUseKey", "BUTTON_2", KeybindSettings.PRESS_ALLOWEXTRA);
	public static final Hotkey EASY_PLACE_TOGGLE = new Hotkey("easyPlaceToggle", "");
	public static final Hotkey EXECUTE_OPERATION = new Hotkey("executeOperation", "");
	public static final Hotkey INVERT_GHOST_BLOCK_RENDER_STATE = new Hotkey("invertGhostBlockRenderState", "");
	public static final Hotkey INVERT_OVERLAY_RENDER_STATE = new Hotkey("invertOverlayRenderState", "");
	public static final Hotkey LAYER_MODE_NEXT = new Hotkey("layerModeNext", "M,PAGE_UP");
	public static final Hotkey LAYER_MODE_PREVIOUS = new Hotkey("layerModePrevious", "M,PAGE_DOWN");
	public static final Hotkey LAYER_NEXT = new Hotkey("layerNext", "PAGE_UP");
	public static final Hotkey LAYER_PREVIOUS = new Hotkey("layerPrevious", "PAGE_DOWN");
	public static final Hotkey LAYER_SET_HERE = new Hotkey("layerSetHere", "");
	public static final Hotkey NUDGE_SELECTION_NEGATIVE = new Hotkey("nudgeSelectionNegative", "");
	public static final Hotkey NUDGE_SELECTION_POSITIVE = new Hotkey("nudgeSelectionPositive", "");
	public static final Hotkey MOVE_ENTIRE_SELECTION = new Hotkey("moveEntireSelection", "");
	public static final Hotkey OPEN_GUI_AREA_SETTINGS = new Hotkey("openGuiAreaSettings", "KP_MULTIPLY");
	public static final Hotkey OPEN_GUI_LOADED_SCHEMATICS = new Hotkey("openGuiLoadedSchematics", "");
	public static final Hotkey OPEN_GUI_MAIN_MENU = new Hotkey("openGuiMainMenu", "M", KeybindSettings.RELEASE_EXCLUSIVE);
	public static final Hotkey OPEN_GUI_MATERIAL_LIST = new Hotkey("openGuiMaterialList", "M,L");
	public static final Hotkey OPEN_GUI_PLACEMENT_SETTINGS = new Hotkey("openGuiPlacementSettings", "KP_SUBTRACT");
	public static final Hotkey OPEN_GUI_SCHEMATIC_PLACEMENTS = new Hotkey("openGuiSchematicPlacements", "M,P");
	public static final Hotkey OPEN_GUI_SCHEMATIC_PROJECTS = new Hotkey("openGuiSchematicProjects", "");
	public static final Hotkey OPEN_GUI_SCHEMATIC_VERIFIER = new Hotkey("openGuiSchematicVerifier", "M,V");
	public static final Hotkey OPEN_GUI_SELECTION_MANAGER = new Hotkey("openGuiSelectionManager", "M,S");
	public static final Hotkey OPEN_GUI_SETTINGS = new Hotkey("openGuiSettings", "M,C");
	public static final Hotkey OPERATION_MODE_CHANGE_MODIFIER = new Hotkey("operationModeChangeModifier", "LEFT_CONTROL", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey PICK_BLOCK_FIRST = new Hotkey("pickBlockFirst", "BUTTON_3", KeybindSettings.PRESS_ALLOWEXTRA);
	public static final Hotkey PICK_BLOCK_LAST = new Hotkey("pickBlockLast", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey PICK_BLOCK_TOGGLE = new Hotkey("pickBlockToggle", "M,BUTTON_3");
	public static final Hotkey RENDER_INFO_OVERLAY = new Hotkey("renderInfoOverlay", "I", KeybindSettings.PRESS_ALLOWEXTRA);
	public static final Hotkey RENDER_OVERLAY_THROUGH_BLOCKS = new Hotkey("renderOverlayThroughBlocks", "RIGHT_CONTROL", KeybindSettings.PRESS_ALLOWEXTRA);
	public static final Hotkey RERENDER_SCHEMATIC = new Hotkey("rerenderSchematic", "F3,M");
	public static final Hotkey SAVE_AREA_AS_IN_MEMORY_SCHEMATIC = new Hotkey("saveAreaAsInMemorySchematic", "");
	public static final Hotkey SAVE_AREA_AS_SCHEMATIC_TO_FILE = new Hotkey("saveAreaAsSchematicToFile", "LEFT_CONTROL,LEFT_ALT,S");
	public static final Hotkey SCHEMATIC_EDIT_BREAK_ALL = new Hotkey("schematicEditBreakPlaceAll", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SCHEMATIC_EDIT_BREAK_ALL_EXCEPT = new Hotkey("schematicEditBreakAllExcept", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SCHEMATIC_EDIT_BREAK_DIRECTION = new Hotkey("schematicEditBreakPlaceDirection", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SCHEMATIC_EDIT_REPLACE_ALL = new Hotkey("schematicEditReplaceAll", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SCHEMATIC_EDIT_REPLACE_BLOCK = new Hotkey("schematicEditReplaceBlock", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SCHEMATIC_EDIT_REPLACE_DIRECTION = new Hotkey("schematicEditReplaceDirection", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SCHEMATIC_PLACEMENT_ROTATION = new Hotkey("schematicPlacementRotation", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SCHEMATIC_PLACEMENT_MIRROR = new Hotkey("schematicPlacementMirror", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SCHEMATIC_VERSION_CYCLE_MODIFIER = new Hotkey("schematicVersionCycleModifier", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SCHEMATIC_VERSION_CYCLE_NEXT = new Hotkey("schematicVersionCycleNext", "");
	public static final Hotkey SCHEMATIC_VERSION_CYCLE_PREVIOUS = new Hotkey("schematicVersionCyclePrevious", "");
	public static final Hotkey SELECTION_GRAB_MODIFIER = new Hotkey("selectionGrabModifier", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SELECTION_GROW_HOTKEY = new Hotkey("selectionGrow", "");
	public static final Hotkey SELECTION_GROW_MODIFIER = new Hotkey("selectionGrowModifier", "", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SELECTION_NUDGE_MODIFIER = new Hotkey("selectionNudgeModifier", "LEFT_ALT", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey SELECTION_MODE_CYCLE = new Hotkey("selectionModeCycle", "LEFT_CONTROL,M");
	public static final Hotkey SELECTION_SHRINK_HOTKEY = new Hotkey("selectionShrink", "");
	public static final Hotkey SET_AREA_ORIGIN = new Hotkey("setAreaOrigin", "");
	public static final Hotkey TOOL_SELECT = new Hotkey("toolSelect", "LEFT_CONTROL");
	public static final Hotkey TOOL_PLACE_BLOCKS = new Hotkey("toolPlaceBlocks", "");
	public static final Hotkey TOOL_PLACE_ALL = new Hotkey("toolPlaceAll", "V");
	public static final Hotkey TOOL_CHANGE_BLOCK = new Hotkey("toolChangeBlock", "C");
	public static final Hotkey TOOL_EXTEND_SIDE = new Hotkey("toolExtendSide", "LEFT_ALT");
	public static final Hotkey SPAWN_PROOF_MODE = new Hotkey("spawnProofMode", "M,N");
	public static final Hotkey SPAWN_PROOF_SHAPE = new Hotkey("spawnProofShape", "M,B");
	public static final Hotkey SPAWN_PROOF_RADIUS_DECREASE = new Hotkey("spawnProofRadiusDecrease", "LEFT_SHIFT,DOWN");
	public static final Hotkey SPAWN_PROOF_RADIUS_INCREASE = new Hotkey("spawnProofRadiusIncrease", "LEFT_SHIFT,UP");
	public static final Hotkey SPAWN_PROOF_TOGGLE = new Hotkey("spawnProofToggle", "M,X");
	public static final Hotkey SET_SELECTION_BOX_POSITION_1 = new Hotkey("setSelectionBoxPosition1", "");
	public static final Hotkey SET_SELECTION_BOX_POSITION_2 = new Hotkey("setSelectionBoxPosition2", "");
	public static final Hotkey TOGGLE_ALL_RENDERING = new Hotkey("toggleAllRendering", "M,R");
	public static final Hotkey TOGGLE_AREA_SELECTION_RENDERING = new Hotkey("toggleAreaSelectionBoxesRendering", "");
	public static final Hotkey TOGGLE_INFO_OVERLAY_RENDERING = new Hotkey("toggleInfoOverlayRendering", "");
	public static final Hotkey TOGGLE_OVERLAY_RENDERING = new Hotkey("toggleOverlayRendering", "");
	public static final Hotkey TOGGLE_OVERLAY_OUTLINE_RENDERING = new Hotkey("toggleOverlayOutlineRendering", "");
	public static final Hotkey TOGGLE_OVERLAY_SIDE_RENDERING = new Hotkey("toggleOverlaySideRendering", "");
	public static final Hotkey TOGGLE_PLACEMENT_BOXES_RENDERING = new Hotkey("togglePlacementBoxesRendering", "");
	public static final Hotkey TOGGLE_PLACEMENT_RESTRICTION = new Hotkey("togglePlacementRestriction", "");
	public static final Hotkey TOGGLE_SCHEMATIC_BLOCK_RENDERING = new Hotkey("toggleSchematicBlockRendering", "");
	public static final Hotkey TOGGLE_SCHEMATIC_RENDERING = new Hotkey("toggleSchematicRendering", "M,G");
	public static final Hotkey TOGGLE_SIGN_TEXT_PASTE = new Hotkey("toggleSignTextPaste", "");
	public static final Hotkey TOGGLE_TRANSLUCENT_RENDERING = new Hotkey("toggleTranslucentRendering", "");
	public static final Hotkey TOGGLE_VERIFIER_OVERLAY_RENDERING = new Hotkey("toggleVerifierOverlayRendering", "");
	public static final Hotkey TOOL_ENABLED_TOGGLE = new Hotkey("toolEnabledToggle", "M,T");
	public static final Hotkey TOOL_PLACE_CORNER_1 = new Hotkey("toolPlaceCorner1", "BUTTON_1", KeybindSettings.PRESS_ALLOWEXTRA);
	public static final Hotkey TOOL_PLACE_CORNER_2 = new Hotkey("toolPlaceCorner2", "BUTTON_2", KeybindSettings.PRESS_ALLOWEXTRA);
	public static final Hotkey TOOL_SELECT_ELEMENTS = new Hotkey("toolSelectElements", "BUTTON_3", KeybindSettings.PRESS_ALLOWEXTRA);
	public static final Hotkey TOOL_SELECT_MODIFIER_BLOCK_1 = new Hotkey("toolSelectModifierBlock1", "LEFT_ALT", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey TOOL_SELECT_MODIFIER_BLOCK_2 = new Hotkey("toolSelectModifierBlock2", "LEFT_SHIFT", KeybindSettings.MODIFIER_INGAME);
	public static final Hotkey UNLOAD_CURRENT_SCHEMATIC = new Hotkey("unloadCurrentSchematic", "");

	public static final List<ConfigOption<?>> HOTKEY_LIST = List.of(
			ADD_SELECTION_BOX,
			CLONE_SELECTION,
			DELETE_SELECTION_BOX,
			EASY_PLACE_ACTIVATION,
			EASY_PLACE_TOGGLE,
			EXECUTE_OPERATION,
			INVERT_GHOST_BLOCK_RENDER_STATE,
			INVERT_OVERLAY_RENDER_STATE,
			LAYER_MODE_NEXT,
			LAYER_MODE_PREVIOUS,
			LAYER_NEXT,
			LAYER_PREVIOUS,
			LAYER_SET_HERE,
			NUDGE_SELECTION_NEGATIVE,
			NUDGE_SELECTION_POSITIVE,
			MOVE_ENTIRE_SELECTION,
			OPEN_GUI_AREA_SETTINGS,
			OPEN_GUI_LOADED_SCHEMATICS,
			OPEN_GUI_MAIN_MENU,
			OPEN_GUI_MATERIAL_LIST,
			OPEN_GUI_PLACEMENT_SETTINGS,
			OPEN_GUI_SCHEMATIC_PLACEMENTS,
			OPEN_GUI_SCHEMATIC_PROJECTS,
			OPEN_GUI_SCHEMATIC_VERIFIER,
			OPEN_GUI_SELECTION_MANAGER,
			OPEN_GUI_SETTINGS,
			OPERATION_MODE_CHANGE_MODIFIER,
			PICK_BLOCK_FIRST,
			PICK_BLOCK_LAST,
			PICK_BLOCK_TOGGLE,
			RENDER_INFO_OVERLAY,
			RENDER_OVERLAY_THROUGH_BLOCKS,
			RERENDER_SCHEMATIC,
			SAVE_AREA_AS_IN_MEMORY_SCHEMATIC,
			SAVE_AREA_AS_SCHEMATIC_TO_FILE,
			SCHEMATIC_EDIT_BREAK_ALL,
			SCHEMATIC_EDIT_BREAK_ALL_EXCEPT,
			SCHEMATIC_EDIT_BREAK_DIRECTION,
			SCHEMATIC_EDIT_REPLACE_ALL,
			SCHEMATIC_EDIT_REPLACE_BLOCK,
			SCHEMATIC_EDIT_REPLACE_DIRECTION,
			SCHEMATIC_PLACEMENT_ROTATION,
			SCHEMATIC_PLACEMENT_MIRROR,
			SCHEMATIC_VERSION_CYCLE_MODIFIER,
			SCHEMATIC_VERSION_CYCLE_NEXT,
			SCHEMATIC_VERSION_CYCLE_PREVIOUS,
			SELECTION_GRAB_MODIFIER,
			SELECTION_GROW_HOTKEY,
			SELECTION_GROW_MODIFIER,
			SELECTION_NUDGE_MODIFIER,
			SELECTION_MODE_CYCLE,
			SELECTION_SHRINK_HOTKEY,
			SET_AREA_ORIGIN,
			TOOL_SELECT,
			TOOL_PLACE_BLOCKS,
			TOOL_PLACE_ALL,
			TOOL_CHANGE_BLOCK,
			TOOL_EXTEND_SIDE,
			SPAWN_PROOF_MODE,
			SPAWN_PROOF_RADIUS_DECREASE,
			SPAWN_PROOF_RADIUS_INCREASE,
			SPAWN_PROOF_SHAPE,
			SPAWN_PROOF_TOGGLE,
			SET_SELECTION_BOX_POSITION_1,
			SET_SELECTION_BOX_POSITION_2,
			TOGGLE_ALL_RENDERING,
			TOGGLE_AREA_SELECTION_RENDERING,
			TOGGLE_INFO_OVERLAY_RENDERING,
			TOGGLE_OVERLAY_RENDERING,
			TOGGLE_OVERLAY_OUTLINE_RENDERING,
			TOGGLE_OVERLAY_SIDE_RENDERING,
			TOGGLE_PLACEMENT_BOXES_RENDERING,
			TOGGLE_PLACEMENT_RESTRICTION,
			TOGGLE_SCHEMATIC_BLOCK_RENDERING,
			TOGGLE_SCHEMATIC_RENDERING,
			TOGGLE_SIGN_TEXT_PASTE,
			TOGGLE_TRANSLUCENT_RENDERING,
			TOGGLE_VERIFIER_OVERLAY_RENDERING,
			TOOL_ENABLED_TOGGLE,
			TOOL_PLACE_CORNER_1,
			TOOL_PLACE_CORNER_2,
			TOOL_SELECT_ELEMENTS,
			TOOL_SELECT_MODIFIER_BLOCK_1,
			TOOL_SELECT_MODIFIER_BLOCK_2,
			UNLOAD_CURRENT_SCHEMATIC
	);

	/** Another hotkey bound to exactly the same keys, or null if the keys are free. */
	public static Hotkey findConflict(Hotkey hotkey)
	{
		String keys = hotkey.getKeysAsString();

		if (keys.isEmpty())
		{
			return null;
		}

		for (ConfigOption<?> other : HOTKEY_LIST)
		{
			if (other != hotkey && other instanceof Hotkey otherHotkey && keys.equals(otherHotkey.getKeysAsString()))
			{
				return otherHotkey;
			}
		}

		return null;
	}
}
