package me.apika.stencil.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.apika.stencil.StencilClient;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Reads and writes config/stencil.json. The category names and the per-option
 * keys are Litematica's, so lifting a block out of a litematica.json into this
 * file works.
 */
public class ConfigStorage
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private record Category(String jsonName, List<ConfigOption<?>> options) { }

	private static final List<Category> CATEGORIES = List.of(
			new Category("Colors", Configs.Colors.OPTIONS),
			new Category("Generic", Configs.Generic.OPTIONS),
			new Category("Hotkeys", Hotkeys.HOTKEY_LIST),
			new Category("Visuals", Configs.Visuals.OPTIONS)
	);

	public static Path getConfigFile()
	{
		return FabricLoader.getInstance().getConfigDir().resolve(StencilClient.MOD_ID + ".json");
	}

	public static void load()
	{
		Path file = getConfigFile();

		if (Files.isRegularFile(file) == false)
		{
			return;
		}

		try (Reader reader = Files.newBufferedReader(file))
		{
			JsonElement root = JsonParser.parseReader(reader);

			if (root.isJsonObject() == false)
			{
				return;
			}

			JsonObject obj = root.getAsJsonObject();

			for (Category category : CATEGORIES)
			{
				JsonElement element = obj.get(category.jsonName());

				if (element != null && element.isJsonObject())
				{
					readCategory(element.getAsJsonObject(), category.options());
				}
			}
		}
		catch (IOException | RuntimeException e)
		{
			StencilClient.LOGGER.warn("Failed to read {}, keeping defaults", file, e);
		}
	}

	public static void save()
	{
		Path file = getConfigFile();
		JsonObject root = new JsonObject();

		for (Category category : CATEGORIES)
		{
			root.add(category.jsonName(), writeCategory(category.options()));
		}

		try
		{
			Files.createDirectories(file.getParent());

			try (Writer writer = Files.newBufferedWriter(file))
			{
				GSON.toJson(root, writer);
			}
		}
		catch (IOException e)
		{
			StencilClient.LOGGER.warn("Failed to write {}", file, e);
		}
	}

	private static void readCategory(JsonObject obj, List<ConfigOption<?>> options)
	{
		for (ConfigOption<?> option : options)
		{
			JsonElement element = obj.get(option.getName());

			if (element != null)
			{
				option.fromJson(element);
			}
		}
	}

	private static JsonObject writeCategory(List<ConfigOption<?>> options)
	{
		JsonObject obj = new JsonObject();

		for (ConfigOption<?> option : options)
		{
			obj.add(option.getName(), option.toJson());
		}

		return obj;
	}
}
