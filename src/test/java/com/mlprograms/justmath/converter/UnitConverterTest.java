package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.converter.units.Length;
import com.mlprograms.justmath.converter.units.UnitType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.MathContext;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitConverterTest {

    @Test
    void shouldConvertCentimeterToFeet() {
        BigNumber result = UnitConverter.convert(new BigNumber("100"), Length.CENTIMETER, Length.FEET);

        assertEquals("3.28083989501312335958005249343832", result.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "250, CENTIMETER, FEET, 8.202099737532808398950131233595801",
            "1, INCH, CENTIMETER, 2.54",
            "1, NAUTICAL_MILE, METER, 1852",
            "12, FEET, INCH, 144",
            "1000, MILLIMETER, METER, 1"
    })
    void shouldConvertDifferentLengthEnums(final String amount, final Length from, final Length to, final String expected) {
        BigNumber result = UnitConverter.convert(new BigNumber(amount), from, to);

        assertEquals(expected, result.toString());
    }

    @Test
    void shouldReturnSameNumberWhenConvertingToSameUnit() {
        BigNumber amount = new BigNumber("1234.56789");

        BigNumber result = UnitConverter.convert(amount, Length.CENTIMETER, Length.CENTIMETER);

        assertEquals("1234.56789", result.toString());
    }

    @Test
    void shouldKeepSignForNegativeValues() {
        BigNumber result = UnitConverter.convert(new BigNumber("-10"), Length.METER, Length.CENTIMETER);

        assertEquals("-1000", result.toString());
    }

    @Test
    void shouldConvertZeroToZero() {
        BigNumber result = UnitConverter.convert(new BigNumber("0"), Length.CENTIMETER, Length.FEET);

        assertEquals("0", result.toString());
    }

    @Test
    void shouldPreserveLocaleAndMathContextFromAmount() {
        BigNumber amount = new BigNumber("1", Locale.GERMANY, new MathContext(10));

        BigNumber result = UnitConverter.convert(amount, Length.METER, Length.CENTIMETER);

        assertEquals(Locale.GERMANY, result.getLocale());
        assertEquals(new MathContext(10), result.getMathContext());
        assertEquals("100", result.toString());
    }

    @Test
    void unitCalculatorShouldConvertUsingCoreLogic() {
        UnitCalculator calculator = new UnitCalculator();

        BigNumber result = calculator.convert(new BigNumber("100"), Length.CENTIMETER, Length.FEET);

        assertEquals("3.28083989501312335958005249343832", result.toString());
    }

    @Test
    void unitFacadeShouldDelegateToConverter() {
        BigNumber result = Unit.convert(new BigNumber("100"), Length.CENTIMETER, Length.FEET);

        assertEquals("3.28083989501312335958005249343832", result.toString());
    }

    @Test
    void shouldFailForNullAmount() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> UnitConverter.convert(null, Length.CENTIMETER, Length.FEET));

        assertEquals("Amount must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailForNullFromUnit() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> UnitConverter.convert(new BigNumber("1"), null, Length.CENTIMETER));

        assertEquals("From unit must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailForNullToUnit() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> UnitConverter.convert(new BigNumber("1"), Length.CENTIMETER, null));

        assertEquals("To unit must not be null.", exception.getMessage());
    }

    @Test
    void shouldFailForUnknownEnumImplementation() {
        UnitType unknownLengthType = new UnitType() {
            @Override
            public String key() {
                return "UNKNOWN_LENGTH";
            }

            @Override
            public UnitCategory category() {
                return UnitCategory.LENGTH;
            }
        };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> UnitConverter.convert(new BigNumber("1"), Length.CENTIMETER, unknownLengthType));

        assertTrue(exception.getMessage().contains("Unknown unit enum"));
    }


    @Test
    void unitElementsShouldResolveByTypeAndToken() {
        UnitDefinition byType = UnitElements.getByType(Length.CENTIMETER).orElseThrow();
        UnitDefinition byToken = UnitElements.getByToken("cm").orElseThrow();

        assertEquals(Length.CENTIMETER, byType.getType());
        assertEquals(Length.CENTIMETER, byToken.getType());
    }

    @Test
    void unitElementsShouldExposeMaxTokenLength() {
        assertTrue(UnitElements.getMaxTokenLength() > 0);
    }

    @Test
    void definitionsShouldBeGeneratedFromLengthEnum() {
        assertEquals(Unit.lengthTypes().size(), Unit.definitions().size());
        assertTrue(Unit.lengthTypes().stream().allMatch(type ->
                Unit.definitions().stream().anyMatch(definition -> definition.getType() == type)));
    }

    @Test
    void definitionsShouldContainCentimeterAndFeet() {
        assertNotNull(Unit.definitions().stream().filter(definition -> definition.getType() == Length.CENTIMETER).findFirst().orElse(null));
        assertNotNull(Unit.definitions().stream().filter(definition -> definition.getType() == Length.FEET).findFirst().orElse(null));
    }

}
