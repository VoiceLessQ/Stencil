package me.apika.stencil.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * One persisted setting. Subclasses cover the value types the settings screen
 * knows how to draw: boolean, integer, double, string, colour, enum cycle and
 * hotkey.
 *
 * The JSON key of every option matches the corresponding Litematica option, so
 * a hand-copied block of an existing litematica.json loads here unchanged.
 */
public abstract class ConfigOption<T>
{
	private final String name;
	private final T defaultValue;
	protected T value;

	protected ConfigOption(String name, T defaultValue)
	{
		this.name = name;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public String getName()
	{
		return this.name;
	}

	/** Translation key for the label shown in the settings screen. */
	public String getNameKey()
	{
		return "stencil.config." + this.categoryKey() + ".name." + this.name;
	}

	/** Translation key for the hover tooltip. */
	public String getCommentKey()
	{
		return "stencil.config." + this.categoryKey() + ".comment." + this.name;
	}

	protected abstract String categoryKey();

	public T getValue()
	{
		return this.value;
	}

	public void setValue(T value)
	{
		this.value = value;
	}

	public T getDefaultValue()
	{
		return this.defaultValue;
	}

	public boolean isModified()
	{
		return !this.value.equals(this.defaultValue);
	}

	public void resetToDefault()
	{
		this.value = this.defaultValue;
	}

	public abstract JsonElement toJson();

	public abstract void fromJson(JsonElement element);

	// ------------------------------------------------------------------

	public static class Bool extends ConfigOption<Boolean>
	{
		private final String category;

		public Bool(String category, String name, boolean defaultValue)
		{
			super(name, defaultValue);
			this.category = category;
		}

		@Override
		protected String categoryKey()
		{
			return this.category;
		}

		public boolean get()
		{
			return this.value;
		}

		public void toggle()
		{
			this.value = !this.value;
		}

		@Override
		public JsonElement toJson()
		{
			return new JsonPrimitive(this.value);
		}

		@Override
		public void fromJson(JsonElement element)
		{
			if (element.isJsonPrimitive())
			{
				this.value = element.getAsBoolean();
			}
		}
	}

	public static class Int extends ConfigOption<Integer>
	{
		private final String category;
		private final int minValue;
		private final int maxValue;

		public Int(String category, String name, int defaultValue, int minValue, int maxValue)
		{
			super(name, defaultValue);
			this.category = category;
			this.minValue = minValue;
			this.maxValue = maxValue;
		}

		@Override
		protected String categoryKey()
		{
			return this.category;
		}

		public int get()
		{
			return this.value;
		}

		public int getMinValue()
		{
			return this.minValue;
		}

		public int getMaxValue()
		{
			return this.maxValue;
		}

		@Override
		public void setValue(Integer value)
		{
			super.setValue(Math.clamp(value, this.minValue, this.maxValue));
		}

		@Override
		public JsonElement toJson()
		{
			return new JsonPrimitive(this.value);
		}

		@Override
		public void fromJson(JsonElement element)
		{
			if (element.isJsonPrimitive())
			{
				this.setValue(element.getAsInt());
			}
		}
	}

	public static class Dbl extends ConfigOption<Double>
	{
		private final String category;
		private final double minValue;
		private final double maxValue;

		public Dbl(String category, String name, double defaultValue, double minValue, double maxValue)
		{
			super(name, defaultValue);
			this.category = category;
			this.minValue = minValue;
			this.maxValue = maxValue;
		}

		@Override
		protected String categoryKey()
		{
			return this.category;
		}

		public double get()
		{
			return this.value;
		}

		public double getMinValue()
		{
			return this.minValue;
		}

		public double getMaxValue()
		{
			return this.maxValue;
		}

		@Override
		public void setValue(Double value)
		{
			super.setValue(Math.clamp(value, this.minValue, this.maxValue));
		}

		@Override
		public JsonElement toJson()
		{
			return new JsonPrimitive(this.value);
		}

		@Override
		public void fromJson(JsonElement element)
		{
			if (element.isJsonPrimitive())
			{
				this.setValue(element.getAsDouble());
			}
		}
	}

	public static class Str extends ConfigOption<String>
	{
		private final String category;

		public Str(String category, String name, String defaultValue)
		{
			super(name, defaultValue);
			this.category = category;
		}

		@Override
		protected String categoryKey()
		{
			return this.category;
		}

		public String get()
		{
			return this.value;
		}

		@Override
		public JsonElement toJson()
		{
			return new JsonPrimitive(this.value);
		}

		@Override
		public void fromJson(JsonElement element)
		{
			if (element.isJsonPrimitive())
			{
				this.value = element.getAsString();
			}
		}
	}

	/**
	 * An ARGB colour. Stored in the file as the "#AARRGGBB" string Litematica
	 * uses, kept in memory as a packed int for the renderer.
	 */
	public static class Color extends ConfigOption<Integer>
	{
		private final String category;

		public Color(String category, String name, String defaultValue)
		{
			super(name, parse(defaultValue));
			this.category = category;
		}

		@Override
		protected String categoryKey()
		{
			return this.category;
		}

		public int get()
		{
			return this.value;
		}

		public String getAsString()
		{
			return format(this.value);
		}

		/** Reads #RRGGBB; a leading alpha byte from an older config is dropped. */
		public static int parse(String str)
		{
			String hex = str.startsWith("#") ? str.substring(1) : str;

			try
			{
				return (int) Long.parseLong(hex, 16) & 0xFFFFFF;
			}
			catch (NumberFormatException e)
			{
				return 0xFFFFFF;
			}
		}

		public static String format(int color)
		{
			return String.format("#%06X", color & 0xFFFFFF);
		}

		@Override
		public JsonElement toJson()
		{
			return new JsonPrimitive(format(this.value));
		}

		@Override
		public void fromJson(JsonElement element)
		{
			if (element.isJsonPrimitive())
			{
				this.value = parse(element.getAsString());
			}
		}
	}

	/** A cycle through the constants of an enum, stored by its config string. */
	public static class OptionList<E extends Enum<E> & ConfigOptionValue> extends ConfigOption<E>
	{
		private final String category;
		private final E[] values;

		public OptionList(String category, String name, E defaultValue)
		{
			super(name, defaultValue);
			this.category = category;
			this.values = defaultValue.getDeclaringClass().getEnumConstants();
		}

		@Override
		protected String categoryKey()
		{
			return this.category;
		}

		public E get()
		{
			return this.value;
		}

		public E[] getValues()
		{
			return this.values;
		}

		public void cycle(boolean forward)
		{
			int index = this.value.ordinal() + (forward ? 1 : -1);

			if (index < 0)
			{
				index = this.values.length - 1;
			}
			else if (index >= this.values.length)
			{
				index = 0;
			}

			this.value = this.values[index];
		}

		@Override
		public JsonElement toJson()
		{
			return new JsonPrimitive(this.value.getConfigString());
		}

		@Override
		public void fromJson(JsonElement element)
		{
			if (element.isJsonPrimitive() == false)
			{
				return;
			}

			String str = element.getAsString();

			for (E val : this.values)
			{
				if (val.getConfigString().equals(str))
				{
					this.value = val;
					return;
				}
			}
		}
	}

	/**
	 * A key combination, stored as the comma separated GLFW key names
	 * Litematica uses ("M,A", "LEFT_CONTROL,LEFT_ALT,S"), so existing muscle
	 * memory and existing config files carry over.
	 */
	public static class Hotkey extends ConfigOption<String>
	{
		private final KeybindSettings settings;

		public Hotkey(String name, String defaultKeys)
		{
			this(name, defaultKeys, KeybindSettings.DEFAULT);
		}

		public Hotkey(String name, String defaultKeys, KeybindSettings settings)
		{
			super(name, defaultKeys);
			this.settings = settings;
		}

		@Override
		protected String categoryKey()
		{
			return "hotkeys";
		}

		public String getKeysAsString()
		{
			return this.value;
		}

		public KeybindSettings getSettings()
		{
			return this.settings;
		}

		@Override
		public JsonElement toJson()
		{
			JsonObject obj = new JsonObject();
			obj.add("keys", new JsonPrimitive(this.value));
			return obj;
		}

		@Override
		public void fromJson(JsonElement element)
		{
			if (element.isJsonObject())
			{
				JsonObject obj = element.getAsJsonObject();

				if (obj.has("keys") && obj.get("keys").isJsonPrimitive())
				{
					this.value = obj.get("keys").getAsString();
				}
			}
			else if (element.isJsonPrimitive())
			{
				this.value = element.getAsString();
			}
		}
	}
}
