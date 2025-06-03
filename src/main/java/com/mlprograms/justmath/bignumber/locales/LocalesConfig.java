package com.mlprograms.justmath.bignumber.locales;

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
		return new Locale[] {
			Locale.ENGLISH,
			Locale.FRENCH,
			Locale.GERMAN,
			Locale.ITALIAN,
			Locale.JAPANESE,
			Locale.KOREAN,
			Locale.CHINESE,
			Locale.SIMPLIFIED_CHINESE,
			Locale.TRADITIONAL_CHINESE,
			Locale.FRANCE,
			Locale.GERMANY,
			Locale.ITALY,
			Locale.JAPAN,
			Locale.KOREA,
			Locale.UK,
			Locale.US,
			Locale.CANADA,
			Locale.CANADA_FRENCH,
			Locale.forLanguageTag("ru-RU"),
			Locale.forLanguageTag("pl-PL"),
			Locale.forLanguageTag("pt-BR"),
			Locale.forLanguageTag("es-ES"),
			Locale.forLanguageTag("nl-NL"),
			Locale.forLanguageTag("sv-SE"),
			Locale.forLanguageTag("da-DK"),
			Locale.forLanguageTag("fi-FI"),
			Locale.forLanguageTag("cs-CZ"),
			Locale.forLanguageTag("hu-HU"),
			Locale.forLanguageTag("zh-CN"),
			Locale.forLanguageTag("tr-TR"),
			Locale.forLanguageTag("ar-AE"),
			Locale.forLanguageTag("el-GR"),
			Locale.forLanguageTag("he-IL"),
			Locale.forLanguageTag("th-TH"),
			Locale.forLanguageTag("id-ID"),
			Locale.forLanguageTag("vi-VN"),
			Locale.forLanguageTag("no-NO"),
			Locale.forLanguageTag("uk-UA"),
			Locale.forLanguageTag("ro-RO"),
			Locale.forLanguageTag("sk-SK"),
			Locale.forLanguageTag("bg-BG"),
			Locale.forLanguageTag("hr-HR"),
			Locale.forLanguageTag("lt-LT"),
			Locale.forLanguageTag("lv-LV"),
			Locale.forLanguageTag("sl-SI"),
			Locale.forLanguageTag("et-EE")
		};
	}
}

