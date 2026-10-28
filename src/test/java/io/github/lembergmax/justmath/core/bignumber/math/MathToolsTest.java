package io.github.lembergmax.justmath.core.core.bignumber.math;

import io.github.lembergmax.bignumber.BigNumber;
import io.github.lembergmax.bignumber.math.MathTools;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathToolsTest {

    @Test
    public void makeNumbersSameLengthTest() {
        assertEquals(
                "123,12300",
                MathTools.makeNumbersSameLength(
                        new BigNumber("123,123", Locale.GERMAN),
                        new BigNumber("123,12345", Locale.GERMAN)
                )
        );
    }

}
