package me.apika.stencil.config.values;

import me.apika.stencil.config.ConfigOptionValue;

/**
 * The enum types behind the cycle-button settings. Config strings match
 * Litematica's so the values survive a copied config file.
 *
 * HudAlignment and MessageOutputType live in malilib rather than in Litematica
 * itself, so their constants here are the set Litematica actually uses.
 */
public final class ConfigValues
{
	private ConfigValues() { }

	public enum EasyPlaceProtocol implements ConfigOptionValue
	{
		AUTO("auto"),
		V3("v3"),
		V2("v2"),
		SLAB_ONLY("slabs_only"),
		NONE("none");

		private final String configString;

		EasyPlaceProtocol(String configString)
		{
			this.configString = configString;
		}

		@Override
		public String getConfigString()
		{
			return this.configString;
		}

		@Override
		public String getTranslationKey()
		{
			return "stencil.label.easy_place_protocol." + this.configString;
		}
	}

	public enum PasteNbtBehavior implements ConfigOptionValue
	{
		NONE("none"),
		PLACE_MODIFY("place_data_modify"),
		PLACE_CLONE("place_clone");

		private final String configString;

		PasteNbtBehavior(String configString)
		{
			this.configString = configString;
		}

		@Override
		public String getConfigString()
		{
			return this.configString;
		}

		@Override
		public String getTranslationKey()
		{
			return "stencil.label.paste_nbt_behavior." + this.configString;
		}
	}

	public enum ReplaceBehavior implements ConfigOptionValue
	{
		NONE("none"),
		ALL("all"),
		WITH_NON_AIR("with_non_air");

		private final String configString;

		ReplaceBehavior(String configString)
		{
			this.configString = configString;
		}

		@Override
		public String getConfigString()
		{
			return this.configString;
		}

		@Override
		public String getTranslationKey()
		{
			return "stencil.label.replace_behavior." + this.configString;
		}
	}

	public enum CornerSelectionMode implements ConfigOptionValue
	{
		CORNERS("corners"),
		EXPAND("expand");

		private final String configString;

		CornerSelectionMode(String configString)
		{
			this.configString = configString;
		}

		@Override
		public String getConfigString()
		{
			return this.configString;
		}

		@Override
		public String getTranslationKey()
		{
			return "stencil.label.corner_selection_mode." + this.configString;
		}
	}

	public enum DataFixerMode implements ConfigOptionValue
	{
		ALWAYS("always"),
		BELOW_1205("below_1205"),
		BELOW_120X("below_120X"),
		BELOW_119X("below_119X"),
		BELOW_117X("below_117X"),
		BELOW_116X("below_116X"),
		BELOW_113X("below_113X"),
		BELOW_112X("below_112X"),
		NEVER("never");

		private final String configString;

		DataFixerMode(String configString)
		{
			this.configString = configString;
		}

		@Override
		public String getConfigString()
		{
			return this.configString;
		}

		@Override
		public String getTranslationKey()
		{
			return "stencil.label.data_fixer_mode." + this.configString;
		}
	}

	public enum BlockInfoAlignment implements ConfigOptionValue
	{
		CENTER("center"),
		TOP_CENTER("top_center");

		private final String configString;

		BlockInfoAlignment(String configString)
		{
			this.configString = configString;
		}

		@Override
		public String getConfigString()
		{
			return this.configString;
		}

		@Override
		public String getTranslationKey()
		{
			return "stencil.label.alignment." + this.configString;
		}
	}

	public enum HudAlignment implements ConfigOptionValue
	{
		TOP_LEFT("top_left"),
		TOP_RIGHT("top_right"),
		BOTTOM_LEFT("bottom_left"),
		BOTTOM_RIGHT("bottom_right"),
		CENTER("center");

		private final String configString;

		HudAlignment(String configString)
		{
			this.configString = configString;
		}

		@Override
		public String getConfigString()
		{
			return this.configString;
		}

		@Override
		public String getTranslationKey()
		{
			return "stencil.label.hud_alignment." + this.configString;
		}
	}

	public enum MessageOutputType implements ConfigOptionValue
	{
		NONE("none"),
		MESSAGE("message"),
		ACTIONBAR("actionbar");

		private final String configString;

		MessageOutputType(String configString)
		{
			this.configString = configString;
		}

		@Override
		public String getConfigString()
		{
			return this.configString;
		}

		@Override
		public String getTranslationKey()
		{
			return "stencil.label.message_output." + this.configString;
		}
	}
}
