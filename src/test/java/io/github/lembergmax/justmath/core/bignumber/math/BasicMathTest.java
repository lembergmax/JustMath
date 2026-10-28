package io.github.lembergmax.justmath.core.bignumber.math;

import io.github.lembergmax.bignumber.math.BasicMath;
import org.junit.jupiter.api.Test;

import io.github.lembergmax.bignumber.BigNumber;
import io.github.lembergmax.bignumber.math.MathTools;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicMathTest {

    @Test
    public void addTest() {
        assertEquals(
                "777,777",
                BasicMath.add(
                        new BigNumber("123,456", Locale.GERMAN),
                        new BigNumber("654,321", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "1999,998",
                BasicMath.add(
                        new BigNumber("999,999", Locale.GERMAN),
                        new BigNumber("999,999", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0",
                BasicMath.add(
                        new BigNumber("0", Locale.GERMAN),
                        new BigNumber("0", Locale.GERMAN)
                ).toString()
        );
        assertEquals(
                "0,00000002",
                BasicMath.add(
                        new BigNumber("0,00000001", Locale.GERMAN),
                        new BigNumber("0,00000001", Locale.GERMAN)
                ).toString()
        );

        assertEquals(
                "100000000000",
                BasicMath.add(
                        new BigNumber("50000000000", Locale.GERMAN),
                        new BigNumber("50000000000", Locale.GERMAN)
                ).toString()
        );
    }

}
