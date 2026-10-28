package io.github.lembergmax.core.bignumber.math;

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
    }

}
