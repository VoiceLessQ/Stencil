package me.apika.stencil.config;

/** Implemented by the enums used as {@link ConfigOption.OptionList} values. */
public interface ConfigOptionValue
{
	/** The stable string written to the config file. */
	String getConfigString();

	/** Translation key for the value as shown on the cycle button. */
	String getTranslationKey();
}
