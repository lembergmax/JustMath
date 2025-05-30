package com.mlprograms.number;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class BigNumberArithmeticTest {

	private static final Locale LOCALE = Locale.US;

	private BigNumber bn(String beforeDec, String afterDec, boolean negative) {
		boolean hasDec = afterDec != null && !afterDec.isEmpty();
		return BigNumber.builder()
			       .locale(LOCALE)
			       .valueBeforeDecimal(beforeDec)
			       .valueAfterDecimal(hasDec ? afterDec : "0")
			       .hasDecimal(hasDec)
			       .isNegative(negative)
			       .build();
	}

	private BigNumber zero() {
		return bn("0", "0", false);
	}

	/* ====== ADDITION ====== */

	@Test
	void add_SameSign_Simple() {
		BigNumber a = bn("123", "45", false);
		BigNumber b = bn("10", "550", false);
		BigNumber result = BigNumberArithmetic.add(a, b);

		assertEquals("134", result.getValueBeforeDecimal());
		assertEquals("0", result.getValueAfterDecimal());
		assertFalse(result.isNegative());
	}

	@Test
	void add_SameSign_WithCarryFraction() {
		BigNumber a = bn("0", "999", false);
		BigNumber b = bn("0", "002", false);
		BigNumber result = BigNumberArithmetic.add(a, b);

		assertEquals("1", result.getValueBeforeDecimal());
		assertEquals("001", result.getValueAfterDecimal());
		assertFalse(result.isNegative());
	}

	@Test
	void add_NegativeNumbers() {
		BigNumber a = bn("10", "5", true);
		BigNumber b = bn("20", "5", true);
		BigNumber result = BigNumberArithmetic.add(a, b);

		assertEquals("31", result.getValueBeforeDecimal());
		assertEquals("0", result.getValueAfterDecimal());
		assertEquals("-31", result.toString());
		assertTrue(result.isNegative());
	}

	@Test
	void add_DifferentSigns_Throws() {
		BigNumber a = bn("10", "0", false);
		BigNumber b = bn("10", "0", true);

		assertThrows(UnsupportedOperationException.class, () -> BigNumberArithmetic.add(a, b));
	}

	/* ====== SUBTRACTION ====== */

	@Test
	void subtract_SimplePositiveResult() {
		BigNumber a = bn("20", "500", false);
		BigNumber b = bn("10", "250", false);
		BigNumber result = BigNumberArithmetic.subtract(a, b);

		assertEquals("10", result.getValueBeforeDecimal());
		assertEquals("25", result.getValueAfterDecimal());
		assertFalse(result.isNegative());
	}

	@Test
	void subtract_ResultNegative() {
		BigNumber a = bn("10", "0", false);
		BigNumber b = bn("20", "0", false);
		BigNumber result = BigNumberArithmetic.subtract(a, b);

		assertEquals("10", result.getValueBeforeDecimal());
		assertEquals("0", result.getValueAfterDecimal());
		assertTrue(result.isNegative());
	}

	@Test
	void subtract_WithBorrowing() {
		BigNumber a = bn("100", "0", false);
		BigNumber b = bn("99", "999", false);
		BigNumber result = BigNumberArithmetic.subtract(a, b);

		assertEquals("0", result.getValueBeforeDecimal());
		assertEquals("001", result.getValueAfterDecimal());
		assertFalse(result.isNegative());
	}

	@Test
	void subtract_SameNumbers_ZeroResult() {
		BigNumber a = bn("1234", "5678", false);
		BigNumber result = BigNumberArithmetic.subtract(a, a);

		assertEquals("0", result.getValueBeforeDecimal());
		assertEquals("0", result.getValueAfterDecimal());
		assertFalse(result.isNegative());
	}

	@Test
	void subtract_DifferentSigns_DelegatesToAdd() {
		BigNumber a = bn("10", "0", false);
		BigNumber b = bn("10", "0", true);
		BigNumber result = BigNumberArithmetic.subtract(a, b);

		// a - (-b) = a + b = 20
		assertEquals("20", result.getValueBeforeDecimal());
		assertFalse(result.isNegative());
	}

	/* ====== MULTIPLICATION ====== */

	@Test
	void multiply_SmallNumbers() {
		BigNumber a = bn("10", "5", false);  // 10.5
		BigNumber b = bn("2", "0", false);   // 2.0
		BigNumber result = BigNumberArithmetic.multiply(a, b);

		// 10.5 * 2 = 21.0
		assertEquals("21", result.getValueBeforeDecimal());
		assertEquals("0", result.getValueAfterDecimal());
		assertFalse(result.isNegative());
	}

	@Test
	void multiply_LargeNumbers() {
		String largeNum1 = "12345678901234567890";
		String largeNum2 = "98765432109876543210";

		BigNumber a = bn(largeNum1, "", false);
		BigNumber b = bn(largeNum2, "", false);

		BigNumber result = BigNumberArithmetic.multiply(a, b);

		// Just check sign and length since exact number is huge
		assertFalse(result.isNegative());
		assertTrue(result.getValueBeforeDecimal().length() > largeNum1.length());
	}

	@Test
	void multiply_NegativeResult() {
		BigNumber a = bn("5", "5", true);
		BigNumber b = bn("2", "0", false);
		BigNumber result = BigNumberArithmetic.multiply(a, b);

		assertTrue(result.isNegative());
		assertEquals("11", result.getValueBeforeDecimal());
		assertEquals("0", result.getValueAfterDecimal());
	}

	@Test
	void multiply_ZeroResult() {
		BigNumber a = bn("0", "0", false);
		BigNumber b = bn("1234", "5678", true);
		BigNumber result = BigNumberArithmetic.multiply(a, b);

		assertEquals("0", result.getValueBeforeDecimal());
		assertEquals("0", result.getValueAfterDecimal());
		assertFalse(result.isNegative());
	}

	/* ====== DIVISION ====== */

	@Test
	void divide_Simple() {
		BigNumber a = bn("10", "0", false);
		BigNumber b = bn("2", "0", false);
		BigNumber result = BigNumberArithmetic.divide(a, b);

		assertEquals("5", result.getValueBeforeDecimal());
		assertFalse(result.isNegative());
		assertTrue(result.hasDecimal());
	}

	@Test
	void divide_DecimalResult() {
		BigNumber a = bn("1", "0", false);
		BigNumber b = bn("4", "0", false);
		BigNumber result = BigNumberArithmetic.divide(a, b);

		// 1 / 4 = 0.25 with precision 10 -> fractionalPart length = 10
		assertEquals("0", result.getValueBeforeDecimal());
		assertTrue(result.getValueAfterDecimal().startsWith("25"));
		assertFalse(result.isNegative());
	}

	@Test
	void divide_NegativeResult() {
		BigNumber a = bn("10", "0", true);
		BigNumber b = bn("2", "0", false);
		BigNumber result = BigNumberArithmetic.divide(a, b);

		assertTrue(result.isNegative());
		assertEquals("5", result.getValueBeforeDecimal());
	}

	@Test
	void divide_DivisionByZero_Throws() {
		BigNumber a = bn("10", "0", false);
		BigNumber b = bn("0", "0", false);

		assertThrows(ArithmeticException.class, () -> BigNumberArithmetic.divide(a, b));
	}

	/* ====== EDGE CASES ====== */

	@Test
	void add_Zero() {
		BigNumber a = zero();
		BigNumber b = bn("123", "456", false);

		BigNumber result = BigNumberArithmetic.add(a, b);

		assertEquals("123", result.getValueBeforeDecimal());
		assertEquals("456", result.getValueAfterDecimal());
	}

	@Test
	void subtract_Zero() {
		BigNumber a = bn("123", "456", false);
		BigNumber b = zero();

		BigNumber result = BigNumberArithmetic.subtract(a, b);

		assertEquals("123", result.getValueBeforeDecimal());
		assertEquals("456", result.getValueAfterDecimal());
	}

	@Test
	void multiply_Zero() {
		BigNumber a = bn("123", "456", false);
		BigNumber b = zero();

		BigNumber result = BigNumberArithmetic.multiply(a, b);

		assertEquals("0", result.getValueBeforeDecimal());
		assertEquals("0", result.getValueAfterDecimal());
	}

	@Test
	void divide_ZeroDividend() {
		BigNumber a = zero();
		BigNumber b = bn("123", "456", false);

		BigNumber result = BigNumberArithmetic.divide(a, b);

		assertEquals("0", result.getValueBeforeDecimal());
		assertEquals("0", result.getValueAfterDecimal());
	}

}
