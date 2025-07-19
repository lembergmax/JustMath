package com.mlprograms.justmath.bignumber.internal;

import java.util.Locale;

/**
 * Provides a centralized list of Locales used for automatic number parsing.
 */
public final class LocalesConfig {

	/**
	 * A wide range of Locales for detecting number formats.
	 *
	 * @return an array of Locales
	 */
	public static Locale[] getSupportedLocales() {
		return Locale.getAvailableLocales();
	}
}

