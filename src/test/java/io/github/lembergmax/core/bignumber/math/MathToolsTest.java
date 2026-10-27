package io.github.lembergmax.core.bignumber.math;

import io.github.lembergmax.bignumber.BigNumber;
import io.github.lembergmax.bignumber.math.MathTools;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathToolsTest {


    @Test
    public void languageGermanTest() {
        assertEquals(
                "",
                MathTools.makeNumbersSameLength(
                        new BigNumber("", Locale.GERMAN),
                        new BigNumber("", Locale.GERMAN)
                )
        );
    }

}
