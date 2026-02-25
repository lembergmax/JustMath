package com.mlprograms.justmath.converter;

import com.mlprograms.justmath.converter.unit.Unit;
import com.mlprograms.justmath.converter.unit.UnitElements;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitElementsTest {

    @Test
    void shouldFindBuiltInUnitBySymbol() {
        assertEquals(Unit.METER, UnitElements.findBySymbol("m").orElseThrow());
    }

    @Test
    void shouldExposeUnmodifiableRegistryView() {
        assertTrue(UnitElements.getRegistry().containsKey("m"));
    }

}
