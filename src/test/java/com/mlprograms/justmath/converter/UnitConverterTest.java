package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.converter.units.UnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitConverterTest {

    private final List<String> temporaryRegistryKeys = new ArrayList<>();

    @AfterEach
    void cleanupRegistry() {
        temporaryRegistryKeys.forEach(UnitElements.getRegistry()::remove);
        temporaryRegistryKeys.clear();
    }

    @Test
    void shouldConvertCentimeterToFeetUsingSymbols() {
        BigNumber result = UnitConverter.convert(new BigNumber("100"), "cm", "ft");

        assertEquals("3.28083989501312335958005249343832", result.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "250, CENTIMETER, Feet, 8.202099737532808398950131233595801",
            "100, centimetre, foot, 3.28083989501312335958005249343832",
            "1, zoll, cm, 2.54",
            "1, seemeile, m, 1852"
    })
    void shouldResolveAliasesAndCaseInsensitiveTokens(final String amount, final String from, final String to, final String expected) {
        BigNumber result = UnitConverter.convert(new BigNumber(amount), from, to);

        assertEquals(expected, result.toString());
    }

    @Test
    void shouldReturnSameNumberWhenConvertingToSameUnit() {
        BigNumber amount = new BigNumber("1234.56789");

        BigNumber result = UnitConverter.convert(amount, "cm", "centimeter");

        assertEquals("1234.56789", result.toString());
    }

    @Test
    void shouldKeepSignForNegativeValues() {
        BigNumber result = UnitConverter.convert(new BigNumber("-10"), "m", "cm");

        assertEquals("-1000", result.toString());
    }

    @Test
    void shouldConvertZeroToZero() {
        BigNumber result = UnitConverter.convert(new BigNumber("0"), "cm", "ft");

        assertEquals("0", result.toString());
    }

    @Test
    void shouldPreserveLocaleAndMathContextFromAmount() {
        BigNumber amount = new BigNumber("1", Locale.GERMANY, new MathContext(10));

        BigNumber result = UnitConverter.convert(amount, "m", "cm");

        assertEquals(Locale.GERMANY, result.getLocale());
        assertEquals(new MathContext(10), result.getMathContext());
        assertEquals("100", result.toString());
    }

    @Test
    void unitFacadeShouldDelegateToConverter() {
        BigNumber result = Unit.convert(new BigNumber("100"), "cm", "ft");

        assertEquals("3.28083989501312335958005249343832", result.toString());
    }

    @Test
    void shouldFailForUnknownSourceUnit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> UnitConverter.convert(new BigNumber("1"), "banana", "cm"));

        assertTrue(exception.getMessage().contains("Unknown unit"));
    }

    @Test
    void shouldFailForUnknownTargetUnit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> UnitConverter.convert(new BigNumber("1"), "cm", "banana"));

        assertTrue(exception.getMessage().contains("Unknown unit"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void shouldFailForInvalidSourceToken(final String sourceToken) {
        assertThrows(IllegalArgumentException.class, () -> UnitConverter.convert(new BigNumber("1"), sourceToken, "cm"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void shouldFailForInvalidTargetToken(final String targetToken) {
        assertThrows(IllegalArgumentException.class, () -> UnitConverter.convert(new BigNumber("1"), "cm", targetToken));
    }

    @Test
    void shouldFailForNullAmount() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> UnitConverter.convert(null, "cm", "ft"));

        assertEquals("Amount must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailWhenUnitCategoriesDiffer() {
        UnitType syntheticType = new UnitType() {
            @Override
            public String key() {
                return "synthetic";
            }

            @Override
            public UnitCategory category() {
                return null;
            }
        };

        UnitDefinition syntheticDefinition = UnitDefinition.builder()
                .type(syntheticType)
                .displayName("Synthetic")
                .symbol("syn")
                .factorToBase("1")
                .alias("synthetic")
                .build();

        UnitElements.getRegistry().put("synthetic", syntheticDefinition);
        temporaryRegistryKeys.add("synthetic");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> UnitConverter.convert(new BigNumber("1"), "cm", "synthetic"));

        assertTrue(exception.getMessage().contains("Cannot convert between different categories"));
    }

    @Test
    void lengthDefinitionsShouldContainCentimeterAndFeet() {
        assertNotNull(Unit.lengthDefinitions().stream().filter(definition -> "cm".equals(definition.getSymbol())).findFirst().orElse(null));
        assertNotNull(Unit.lengthDefinitions().stream().filter(definition -> "ft".equals(definition.getSymbol())).findFirst().orElse(null));
    }

}
