package me.apika.stencil.input;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;

import me.apika.stencil.StencilClient;
import me.apika.stencil.config.ConfigOption.Hotkey;
import me.apika.stencil.config.Configs;
import net.minecraft.client.Minecraft;

/**
 * Polls the keys of one {@link Hotkey} once per client tick. The key string is
 * Litematica's comma separated GLFW style names ("LEFT_SHIFT,UP"), translated
 * here to the vanilla key names so the game does the key code lookup.
 *
 * A keybind is held while every key in it is down, and triggers on the tick
 * where the last key goes down while the others are already held.
 */
public class Keybind
{
	private static final Set<Integer> pressedMouseButtons = new HashSet<>();

	private final Hotkey hotkey;
	private String parsedKeys = null;
	private final List<InputConstants.Key> keys = new ArrayList<>();
	private boolean lastKeyWasDown;
	private boolean triggered;

	public Keybind(Hotkey hotkey)
	{
		this.hotkey = hotkey;
	}

	public boolean isHeld()
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

		return true;
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

		if (this.keys.isEmpty())
		{
			return;
		}

		boolean lastDown = isDown(this.keys.getLast());

		if (lastDown && this.lastKeyWasDown == false)
		{
			this.triggered = this.isHeld();
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
