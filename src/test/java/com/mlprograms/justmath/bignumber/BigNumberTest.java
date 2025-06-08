package com.mlprograms.justmath.bignumber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigNumberTest {

	@Test
	void add() {
		BigNumber augend = new BigNumber("2.5");
		BigNumber addend = new BigNumber("3.5");
		assertEquals("6", augend.add(addend).toString());

		BigNumber verySmall = new BigNumber("0.0000000000000000000000000000000000000000000000000001");
		BigNumber veryLarge = new BigNumber("100000000000000000000000000000000000000000000000000");
		assertEquals("100000000000000000000000000000000000000000000000000.0000000000000000000000000000000000000000000000000001", verySmall.add(veryLarge).toString());

		BigNumber negative = new BigNumber("-10");
		BigNumber positive = new BigNumber("20");
		assertEquals("10", negative.add(positive).toString());
	}

	@Test
	void subtract() {
		BigNumber minuend = new BigNumber("10");
		BigNumber subtrahend = new BigNumber("3");
		assertEquals("7", minuend.subtract(subtrahend).toString());

		BigNumber verySmall = new BigNumber("0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001");
		BigNumber veryLarge = new BigNumber("1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
		assertEquals("-999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999", verySmall.subtract(veryLarge).toString());

		BigNumber small = new BigNumber("5");
		BigNumber large = new BigNumber("10");
		assertEquals("-5", small.subtract(large).toString());
	}

	@Test
	void multiply() {
		BigNumber factor1 = new BigNumber("6");
		BigNumber factor2 = new BigNumber("7");
		assertEquals("42", factor1.multiply(factor2).toString());

		BigNumber veryLarge = new BigNumber("10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
		BigNumber verySmall = new BigNumber("0.00000000000000000000000000000000000000000000000000001");
		assertEquals("1", veryLarge.multiply(verySmall).toString());

		BigNumber negative = new BigNumber("-5");
		BigNumber positive = new BigNumber("4");
		assertEquals("-20", negative.multiply(positive).toString());
	}

	@Test
	void divide() {
		BigNumber dividend = new BigNumber("10");
		BigNumber divisor = new BigNumber("2");
		assertEquals("5", dividend.divide(divisor).toString());

		BigNumber veryLarge = new BigNumber("100000000000000000000000000000000000000000000000");
		BigNumber verySmall = new BigNumber("0.0000000001");
		assertEquals("1000000000000000000000000000000000000000000000000000000000", veryLarge.divide(verySmall).toString());

		BigNumber numerator = new BigNumber("100");
		BigNumber negativeDivisor = new BigNumber("-10");
		assertEquals("-10", numerator.divide(negativeDivisor).toString());
	}

	@Test
	void power() {
		BigNumber base = new BigNumber("2");
		BigNumber exponent = new BigNumber("3");
		assertEquals("8", base.power(exponent).toString());

		BigNumber veryLarge = new BigNumber("100000000000000000000000000000000000000000000000000");
		BigNumber zero = new BigNumber("0");
		assertEquals("1", veryLarge.power(zero).toString());

		BigNumber two = new BigNumber("2");
		BigNumber negativeExponent = new BigNumber("-2");
		assertEquals("0.25", two.power(negativeExponent).toString());
	}

	@Test
	void squareRoot() {
		BigNumber input = new BigNumber("49");
		assertEquals("7", input.squareRoot().toString());

		BigNumber verySmall = new BigNumber("0.00000000000000000000000000000000000000000000000001");
		assertEquals("0.00000000000000000000000000000000000001", verySmall.squareRoot().toString());

		BigNumber zero = new BigNumber("0");
		assertEquals("0", zero.squareRoot().toString());
	}

	@Test
	void factorial() {
		BigNumber input = new BigNumber("5");
		assertEquals("120", input.factorial().toString());

		BigNumber zero = new BigNumber("0");
		assertEquals("1", zero.factorial().toString());

		BigNumber one = new BigNumber("1");
		assertEquals("1", one.factorial().toString());
	}

}
