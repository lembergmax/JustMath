/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.lembergmax.justmath.bignumber;

import static io.github.lembergmax.justmath.bignumber.math.DecimalArbitraries.SEED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import io.github.lembergmax.justmath.bignumber.math.DecimalArbitraries;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

/**
 * Properties of the value semantics of {@link BigNumber}: rounding and integer parts against {@link BigDecimal},
 * the contract between {@code compareTo}, {@code equals} and {@code hashCode}, that an operation does not change its
 * receiver, and that a number survives formatting and parsing in another locale.
 */
class BigNumberValuePropertyTest {

    private static final int TRIES = 300;

    private static final int MAX_PRECISION = 60;

    private static final int MAX_SCALE = 40;

    private static final Locale[] LOCALES = {
            Locale.US, Locale.GERMANY, Locale.FRANCE, Locale.forLanguageTag("de-CH"), Locale.forLanguageTag("pt-BR"), Locale.ITALY
    };

    @Provide
    Arbitrary<BigDecimal> decimals() {
        return DecimalArbitraries.decimals();
    }

    @Provide
    Arbitrary<RoundingMode> roundingModes() {
        return DecimalArbitraries.roundingModes();
    }

    @Provide
    Arbitrary<Locale> locales() {
        return Arbitraries.of(LOCALES);
    }

    private static BigNumber number(final BigDecimal value) {
        return new BigNumber(value.toPlainString());
    }

    @Property(tries = TRIES, seed = SEED)
    void roundMatchesBigDecimal(
            @ForAll("decimals") final BigDecimal value,
            @ForAll @IntRange(min = 1, max = MAX_PRECISION) final int precision,
            @ForAll("roundingModes") final RoundingMode mode
    ) {
        final MathContext mathContext = new MathContext(precision, mode);

        final BigNumber actual = number(value).round(mathContext);

        assertEquals(0, value.round(mathContext).compareTo(actual.toBigDecimal()), value.toPlainString() + " rounded to " + mathContext);
    }

    @Property(tries = TRIES, seed = SEED)
    void roundAfterDecimalsMatchesSetScale(
            @ForAll("decimals") final BigDecimal value,
            @ForAll @IntRange(min = 0, max = MAX_SCALE) final int scale,
            @ForAll("roundingModes") final RoundingMode mode
    ) {
        final BigNumber actual = number(value).roundAfterDecimals(new MathContext(scale, mode));

        assertEquals(0, value.setScale(scale, mode).compareTo(actual.toBigDecimal()), value.toPlainString() + " to " + scale + " decimals, " + mode);
    }

    @Property(tries = TRIES, seed = SEED)
    void floorCeilAndTruncateMatchBigDecimal(@ForAll("decimals") final BigDecimal value) {
        final BigNumber number = number(value);

        assertEquals(0, value.setScale(0, RoundingMode.FLOOR).compareTo(number.floor().toBigDecimal()), "floor of " + value.toPlainString());
        assertEquals(0, value.setScale(0, RoundingMode.CEILING).compareTo(number.ceil().toBigDecimal()), "ceil of " + value.toPlainString());
        assertEquals(0, value.setScale(0, RoundingMode.DOWN).compareTo(number.truncate().toBigDecimal()), "truncate of " + value.toPlainString());
    }

    @Property(tries = TRIES, seed = SEED)
    void absAndNegateMatchBigDecimalAndLeaveTheReceiverAlone(@ForAll("decimals") final BigDecimal value) {
        final BigNumber number = number(value);
        final String before = number.toString();

        assertEquals(0, value.abs().compareTo(number.abs().toBigDecimal()), "abs of " + value.toPlainString());
        assertEquals(0, value.negate().compareTo(number.negate().toBigDecimal()), "negate of " + value.toPlainString());
        assertEquals(before, number.toString(), "the operations must not change the receiver");
    }

    @Property(tries = TRIES, seed = SEED)
    void compareToEqualsAndHashCodeAgree(@ForAll("decimals") final BigDecimal first, @ForAll("decimals") final BigDecimal second) {
        final BigNumber a = number(first);
        final BigNumber b = number(second);

        assertEquals(Integer.signum(first.compareTo(second)), Integer.signum(a.compareTo(b)), "compareTo");
        assertEquals(first.compareTo(second) == 0, a.equals(b), "equals");
        assertEquals(Integer.signum(b.compareTo(a)), -Integer.signum(a.compareTo(b)), "compareTo is antisymmetric");
        if (a.equals(b)) {
            assertEquals(a.hashCode(), b.hashCode(), "equal numbers must have equal hash codes");
        }
    }

    @Property(tries = TRIES, seed = SEED)
    void trailingZerosChangeNeitherEqualityNorHashCode(@ForAll("decimals") final BigDecimal value, @ForAll @IntRange(min = 1, max = 20) final int zeros) {
        final BigNumber plain = number(value);
        final BigNumber padded = number(value.setScale(Math.max(value.scale(), 0) + zeros));

        assertEquals(plain, padded);
        assertEquals(plain.hashCode(), padded.hashCode());
        assertEquals(0, plain.compareTo(padded));
    }

    @Property(tries = TRIES, seed = SEED)
    void aNumberSurvivesFormattingAndParsingInEveryLocale(@ForAll("decimals") final BigDecimal value, @ForAll("locales") final Locale locale) {
        final BigNumber original = number(value);

        final String formatted = original.toString(locale);
        final BigNumber parsed = new BigNumber(formatted, locale);

        assertEquals(0, value.compareTo(parsed.toBigDecimal()), value.toPlainString() + " formatted for " + locale + " as '" + formatted + "'");
    }

    @Property(tries = TRIES, seed = SEED)
    void aRetargetedLocaleKeepsTheValue(@ForAll("decimals") final BigDecimal value, @ForAll("locales") final Locale locale) {
        final BigNumber original = number(value);

        final BigNumber retargeted = new BigNumber(original, locale);

        assertEquals(original, retargeted);
        assertEquals(locale, retargeted.getLocale());
    }

}
