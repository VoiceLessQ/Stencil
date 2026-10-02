package me.apika.stencil.command;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.config.ConfigOptionValue;
import me.apika.stencil.config.ConfigStorage;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 * Client-side /stencil command for the settings: list, get, set and reset any
 * option by its config name, the same names as in config/stencil.json. A set
 * value is checked like the settings screen checks it and saved at once.
 */
public class StencilCommand
{
	private static final Pattern COLOR_PATTERN = Pattern.compile("#?[0-9a-fA-F]{6}");

	private static final SuggestionProvider<FabricClientCommandSource> OPTION_NAMES = (context, builder) ->
			SharedSuggestionProvider.suggest(ConfigStorage.getAllOptions().stream().map(ConfigOption::getName), builder);

	private static final SuggestionProvider<FabricClientCommandSource> OPTION_VALUES = (context, builder) ->
	{
		ConfigOption<?> option = find(StringArgumentType.getString(context, "option"));

		if (option instanceof ConfigOption.Bool)
		{
			return SharedSuggestionProvider.suggest(new String[] {"true", "false"}, builder);
		}

		if (option instanceof ConfigOption.OptionList<?> list)
		{
			return SharedSuggestionProvider.suggest(Arrays.stream(list.getValues()).map(ConfigOptionValue::getConfigString), builder);
		}

		if (option != null)
		{
			return SharedSuggestionProvider.suggest(new String[] {valueText(option)}, builder);
		}

		return builder.buildFuture();
	};

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher)
	{
		dispatcher.register(literal("stencil")
				.executes(StencilCommand::usage)
				.then(literal("list").executes(StencilCommand::list))
				.then(literal("get")
						.then(argument("option", StringArgumentType.word()).suggests(OPTION_NAMES)
								.executes(StencilCommand::get)))
				.then(literal("set")
						.then(argument("option", StringArgumentType.word()).suggests(OPTION_NAMES)
								.then(argument("value", StringArgumentType.greedyString()).suggests(OPTION_VALUES)
										.executes(StencilCommand::set))))
				.then(literal("reset")
						.then(argument("option", StringArgumentType.word()).suggests(OPTION_NAMES)
								.executes(StencilCommand::reset))));
	}

	private static int usage(CommandContext<FabricClientCommandSource> context)
	{
		context.getSource().sendFeedback(Component.literal("/stencil list | get <option> | set <option> <value> | reset <option>"));
		return 1;
	}

	private static int list(CommandContext<FabricClientCommandSource> context)
	{
		for (ConfigOption<?> option : ConfigStorage.getAllOptions())
		{
			context.getSource().sendFeedback(Component.literal(describe(option)));
		}

		return 1;
	}

	private static int get(CommandContext<FabricClientCommandSource> context)
	{
		ConfigOption<?> option = findOrComplain(context);

		if (option == null)
		{
			return 0;
		}

		context.getSource().sendFeedback(Component.literal(describe(option)));
		return 1;
	}

	private static int set(CommandContext<FabricClientCommandSource> context)
	{
		ConfigOption<?> option = findOrComplain(context);

		if (option == null)
		{
			return 0;
		}

		String text = StringArgumentType.getString(context, "value").trim();

		if (apply(option, text) == false)
		{
			context.getSource().sendError(Component.literal("Not a valid value for " + option.getName() + ": " + text + expected(option)));
			return 0;
		}

		ConfigStorage.save();
		context.getSource().sendFeedback(Component.literal(describe(option)));
		return 1;
	}

	private static int reset(CommandContext<FabricClientCommandSource> context)
	{
		ConfigOption<?> option = findOrComplain(context);

		if (option == null)
		{
			return 0;
		}

		option.resetToDefault();
		ConfigStorage.save();
		context.getSource().sendFeedback(Component.literal(describe(option)));
		return 1;
	}

	private static ConfigOption<?> findOrComplain(CommandContext<FabricClientCommandSource> context)
	{
		String name = StringArgumentType.getString(context, "option");
		ConfigOption<?> option = find(name);

		if (option == null)
		{
			context.getSource().sendError(Component.literal("No option named " + name + "; /stencil list shows them all"));
		}

		return option;
	}

	/** Case-insensitive, so "toolitem" finds toolItem. */
	private static ConfigOption<?> find(String name)
	{
		for (ConfigOption<?> option : ConfigStorage.getAllOptions())
		{
			if (option.getName().equalsIgnoreCase(name))
			{
				return option;
			}
		}

		return null;
	}

	/** Parses and stores the value; false leaves the option unchanged. Numbers are clamped to their range. */
	private static boolean apply(ConfigOption<?> option, String text)
	{
		try
		{
			if (option instanceof ConfigOption.Bool bool)
			{
				switch (text.toLowerCase(Locale.ROOT))
				{
					case "true", "on" -> bool.setValue(true);
					case "false", "off" -> bool.setValue(false);
					default -> { return false; }
				}
			}
			else if (option instanceof ConfigOption.Int intOption)
			{
				intOption.setValue(Integer.parseInt(text));
			}
			else if (option instanceof ConfigOption.Dbl dbl)
			{
				dbl.setValue(Double.parseDouble(text));
			}
			else if (option instanceof ConfigOption.Color color)
			{
				if (COLOR_PATTERN.matcher(text).matches() == false)
				{
					return false;
				}

				color.setValue(ConfigOption.Color.parse(text));
			}
			else if (option instanceof ConfigOption.OptionList<?> list)
			{
				return setListValue(list, text);
			}
			else if (option instanceof ConfigOption.Hotkey hotkey)
			{
				// "none" clears the binding, since an empty value cannot be typed.
				hotkey.setValue(text.equalsIgnoreCase("none") ? "" : text.toUpperCase(Locale.ROOT).replace(" ", ""));
			}
			else if (option instanceof ConfigOption.Str str)
			{
				str.setValue(text);
			}
			else
			{
				return false;
			}
		}
		catch (NumberFormatException e)
		{
			return false;
		}

		return true;
	}

	private static <E extends Enum<E> & ConfigOptionValue> boolean setListValue(ConfigOption.OptionList<E> list, String text)
	{
		for (E value : list.getValues())
		{
			if (value.getConfigString().equalsIgnoreCase(text))
			{
				list.setValue(value);
				return true;
			}
		}

		return false;
	}

	private static String expected(ConfigOption<?> option)
	{
		if (option instanceof ConfigOption.Int intOption)
		{
			return " (a whole number, " + intOption.getMinValue() + " to " + intOption.getMaxValue() + ")";
		}

		if (option instanceof ConfigOption.Dbl dbl)
		{
			return " (a number, " + dbl.getMinValue() + " to " + dbl.getMaxValue() + ")";
		}

		if (option instanceof ConfigOption.Bool)
		{
			return " (true or false)";
		}

		if (option instanceof ConfigOption.Color)
		{
			return " (#RRGGBB)";
		}

		return "";
	}

	/** "toolItem = minecraft:stick", with the default when changed. */
	private static String describe(ConfigOption<?> option)
	{
		String text = option.getName() + " = " + valueText(option);

		if (option.isModified())
		{
			text += " (default " + defaultText(option) + ")";
		}

		return text;
	}

	private static String valueText(ConfigOption<?> option)
	{
		if (option instanceof ConfigOption.Color color)
		{
			return color.getAsString();
		}

		if (option instanceof ConfigOption.OptionList<?> list)
		{
			return list.get().getConfigString();
		}

		if (option instanceof ConfigOption.Hotkey hotkey && hotkey.getKeysAsString().isEmpty())
		{
			return "none";
		}

		return String.valueOf(option.getValue());
	}

	private static String defaultText(ConfigOption<?> option)
	{
		Object value = option.getDefaultValue();

		if (option instanceof ConfigOption.Color)
		{
			return ConfigOption.Color.format((Integer) value);
		}

		if (value instanceof ConfigOptionValue optionValue)
		{
			return optionValue.getConfigString();
		}

		if (option instanceof ConfigOption.Hotkey && "".equals(value))
		{
			return "none";
		}

		return String.valueOf(value);
	}
}
