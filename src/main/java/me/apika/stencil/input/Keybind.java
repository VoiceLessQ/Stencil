package me.apika.stencil.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;

import me.apika.stencil.StencilClient;
import me.apika.stencil.config.ConfigOption;
import me.apika.stencil.config.ConfigOption.Hotkey;
import me.apika.stencil.config.Configs;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * Polls the keys of one {@link Hotkey} once per client tick. The key string is
 * Litematica's comma separated GLFW style names ("LEFT_SHIFT,UP"), translated
 * here to the vanilla key names so the game does the key code lookup.
 *
 * A keybind is held while every key in it is down, and triggers on the tick
 * where the last key goes down while the others are already held. Each hotkey
 * also has an unbound vanilla key mapping in the game's Controls screen; a key
 * bound there works alongside the chord.
 */
public class Keybind
{
	private static final Set<Integer> pressedMouseButtons = new HashSet<>();
	private static final Map<Hotkey, KeyMapping> keyMappings = new HashMap<>();
	private static final List<String> MODIFIER_KEYS = List.of(
			"key.keyboard.left.shift", "key.keyboard.right.shift",
			"key.keyboard.left.control", "key.keyboard.right.control",
			"key.keyboard.left.alt", "key.keyboard.right.alt",
			"key.keyboard.left.win", "key.keyboard.right.win");

	private final Hotkey hotkey;
	private String parsedKeys = null;
	private final List<InputConstants.Key> keys = new ArrayList<>();
	private boolean lastKeyWasDown;
	private boolean triggered;

	public Keybind(Hotkey hotkey)
	{
		this.hotkey = hotkey;
	}

	/** Adds a "Stencil" category to the game's Controls screen; call once during client init. */
	public static void registerKeyMappings(List<ConfigOption<?>> hotkeys)
	{
		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(StencilClient.MOD_ID, StencilClient.MOD_ID));

		for (ConfigOption<?> option : hotkeys)
		{
			if (option instanceof Hotkey hotkey)
			{
				KeyMapping mapping = new KeyMapping("key." + StencilClient.MOD_ID + "." + hotkey.getName(), InputConstants.UNKNOWN.getValue(), category);
				keyMappings.put(hotkey, KeyMappingHelper.registerKeyMapping(mapping));
			}
		}
	}

	public boolean isHeld()
	{
		KeyMapping mapping = keyMappings.get(this.hotkey);
		return (mapping != null && mapping.isDown()) || this.isChordHeld();
	}

	private boolean isChordHeld()
	{
		this.reparseIfChanged();

		if (this.keys.isEmpty())
		{
			return false;
		}

		for (InputConstants.Key key : this.keys)
		{
			if (isDown(key) == false)
			{
				return false;
			}
		}

		return this.hotkey.getSettings().getAllowExtraKeys() || this.hasExtraKeys() == false;
	}

	/**
	 * True while a modifier or mouse button outside this chord is down, so M,X
	 * does not fire as Ctrl+M,X and a mouse bound hotkey does not fire on a
	 * Shift click. Movement keys are left alone, so hotkeys work while walking.
	 */
	private boolean hasExtraKeys()
	{
		for (String name : MODIFIER_KEYS)
		{
			InputConstants.Key key = InputConstants.getKey(name);

			if (this.keys.contains(key) == false && isDown(key))
			{
				return true;
			}
		}

		for (int button : pressedMouseButtons)
		{
			if (this.keys.contains(InputConstants.Type.MOUSE.getOrCreate(button)) == false)
			{
				return true;
			}
		}

		return false;
	}

	/** True on the one tick the combination was completed. */
	public boolean wasTriggered()
	{
		return this.triggered;
	}

	public void tick()
	{
		this.reparseIfChanged();
		this.triggered = false;
		KeyMapping mapping = keyMappings.get(this.hotkey);

		// Drain every queued press, so one tap does not fire again next tick.
		while (mapping != null && mapping.consumeClick())
		{
			this.triggered = true;
		}

		if (this.keys.isEmpty())
		{
			return;
		}

		boolean lastDown = isDown(this.keys.getLast());

		if (lastDown && this.lastKeyWasDown == false)
		{
			this.triggered |= this.isChordHeld();
		}

		this.lastKeyWasDown = lastDown;
	}

	private void reparseIfChanged()
	{
		String str = this.hotkey.getKeysAsString();

		if (str.equals(this.parsedKeys))
		{
			return;
		}

		this.parsedKeys = str;
		this.keys.clear();
		this.lastKeyWasDown = false;

		for (String name : str.split(","))
		{
			name = name.trim();

			if (name.isEmpty())
			{
				continue;
			}

			try
			{
				this.keys.add(InputConstants.getKey(toVanillaName(name)));
			}
			catch (IllegalArgumentException e)
			{
				this.keys.clear();
				return;
			}
		}
	}

	/** True if every name in a key string like "LEFT_SHIFT,UP" is a key the game knows. */
	public static boolean isValidKeys(String str)
	{
		for (String name : str.split(","))
		{
			name = name.trim();

			if (name.isEmpty())
			{
				return false;
			}

			try
			{
				InputConstants.getKey(toVanillaName(name));
			}
			catch (IllegalArgumentException e)
			{
				return false;
			}
		}

		return true;
	}

	/** "LEFT_SHIFT" to "key.keyboard.left.shift", "BUTTON_2" to "key.mouse.right". */
	private static String toVanillaName(String name)
	{
		if (name.startsWith("BUTTON_"))
		{
			return switch (name)
			{
				case "BUTTON_1" -> "key.mouse.left";
				case "BUTTON_2" -> "key.mouse.right";
				case "BUTTON_3" -> "key.mouse.middle";
				default -> "key.mouse." + name.substring(7);
			};
		}

		if (name.startsWith("KP_"))
		{
			name = "KEYPAD_" + name.substring(3);
		}

		return "key.keyboard." + name.toLowerCase().replace('_', '.');
	}

	/** The reverse: "key.keyboard.left.shift" to "LEFT_SHIFT", "key.mouse.right" to "BUTTON_2". */
	public static String fromVanillaName(String name)
	{
		if (name.startsWith("key.mouse."))
		{
			return switch (name)
			{
				case "key.mouse.left" -> "BUTTON_1";
				case "key.mouse.right" -> "BUTTON_2";
				case "key.mouse.middle" -> "BUTTON_3";
				default -> "BUTTON_" + name.substring(10);
			};
		}

		if (name.startsWith("key.keyboard."))
		{
			name = name.substring(13);
		}

		name = name.toUpperCase().replace('.', '_');

		return name.startsWith("KEYPAD_") ? "KP_" + name.substring(7) : name;
	}

	/** Fed by the MouseHandler mixin on every button press and release. */
	public static void onMouseButton(int button, boolean pressed)
	{
		if (Configs.Generic.DEBUG_LOGGING.getValue())
		{
			StencilClient.LOGGER.info("mouse button={} pressed={}", button, pressed);
		}

		if (pressed)
		{
			pressedMouseButtons.add(button);
		}
		else
		{
			pressedMouseButtons.remove(button);
		}
	}

	private static boolean isDown(InputConstants.Key key)
	{
		if (key.getType() == InputConstants.Type.MOUSE)
		{
			return pressedMouseButtons.contains(key.getValue());
		}

		return InputConstants.isKeyDown(key.getValue());
	}
}
