package me.apika.stencil.spawnproof;

public enum ToolAction
{
	PLACE        ("Place blocks"),
	PLACE_ALL    ("Place all"),
	REPLACE      ("Replace blocks"),
	CHANGE_BLOCK ("Change block"),
	EXTEND_SIDE  ("Extend facing side"),
	MARK_AREA    ("Mark area");

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
