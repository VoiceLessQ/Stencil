package me.apika.stencil.spawnproof;

/**
 * The spawn proof tools. They only work with the tool item (a stick by
 * default) in the main hand: the tool-select key plus the wheel picks one,
 * and right click runs it.
 */
public enum ToolAction
{
	/** Right click on a ghost places the current block there. */
	PLACE        ("Place blocks"),
	/** Right click places the current block from the inventory on every ghost within reach. */
	PLACE_ALL    ("Place all"),
	/** Right click on a ghost with a block in the off hand makes every ghost that block. */
	CHANGE_BLOCK ("Change block"),
	/** Wheel grows or shrinks only the side of the area the player is facing. */
	EXTEND_SIDE  ("Extend facing side");

	private final String displayName;

	ToolAction(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName()
	{
		return this.displayName;
	}

	public ToolAction next()
	{
		ToolAction[] values = values();
		return values[(this.ordinal() + 1) % values.length];
	}

	public ToolAction previous()
	{
		ToolAction[] values = values();
		return values[(this.ordinal() + values.length - 1) % values.length];
	}
}
