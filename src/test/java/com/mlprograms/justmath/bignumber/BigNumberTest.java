package com.mlprograms.justmath.bignumber;

import org.junit.jupiter.api.Test;

import java.math.MathContext;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigNumberTest {

	@Test
	void addition() {
		BigNumber num1 = new BigNumber("25.5");
		BigNumber num2 = new BigNumber("30.2");

		BigNumber result = num1.add(num2);
		assertEquals("55.7", result.toString(), "Addition result should be 55.7");

		num1 = new BigNumber("0.000000000000000000000000000000000000000000000001");
		num2 = new BigNumber("0.00000000000000000003");
		result = num1.add(num2);

		assertEquals("0.000000000000000000030000000000000000000000000001", result.toString(),
			"Addition of very small numbers should be accurate");

		num1 = new BigNumber("8736519650165165946166562572365809265462671456");
		num2 = new BigNumber("143153651451954101155145145169254155145");
		result = num1.add(num2);

		assertEquals("8736519793318817398120663727510954434716826601", result.toString(),
			"Addition of large numbers should be accurate");

		num1 = new BigNumber("123");
		num2 = new BigNumber("456");
		BigNumber num3 = new BigNumber("789");

		result = num1.add(num2).add(num3);

		assertEquals("1368", result.toString(), "Chained addition should yield 1368");
	}

	@Test
	void subtraction() {
		BigNumber num1 = new BigNumber("100");
		BigNumber num2 = new BigNumber("30");
		BigNumber result = num1.subtract(num2);
		assertEquals("70", result.toString(), "Subtraction result should be 70");

		num1 = new BigNumber("0.0000000001");
		num2 = new BigNumber("0.00000000009");
		result = num1.subtract(num2);
		assertEquals("0.00000000001", result.toString(), "Subtraction of small decimals should be precise");

		num1 = new BigNumber("500");
		num2 = new BigNumber("1000");
		result = num1.subtract(num2);
		assertEquals("-500", result.toString(), "Subtraction should support negative results");
	}

	@Test
	void multiplication() {
		BigNumber num1 = new BigNumber("12");
		BigNumber num2 = new BigNumber("3");
		BigNumber result = num1.multiply(num2);
		assertEquals("36", result.toString(), "Multiplication result should be 36");

		num1 = new BigNumber("0.00001");
		num2 = new BigNumber("100000");
		result = num1.multiply(num2);
		assertEquals("1", result.toString(), "Multiplication with inverse values should yield 1");

		num1 = new BigNumber("123456789");
		num2 = new BigNumber("987654321");
		result = num1.multiply(num2);
		assertEquals("121932631112635269", result.toString(), "Multiplication of large numbers should be accurate");
	}

	@Test
	void division() {
		BigNumber num1 = new BigNumber("100");
		BigNumber num2 = new BigNumber("4");
		BigNumber result = num1.divide(num2);
		assertEquals("25", result.toString(), "Division result should be 25");

		num1 = new BigNumber("1", new MathContext(10, RoundingMode.HALF_UP));
		num2 = new BigNumber("3");
		result = num1.divide(num2);
		assertEquals("0.3333333333", result.toString(),
			"Division should be precise with recurring decimals");

		num1 = new BigNumber("123456789123456789");
		num2 = new BigNumber("1");
		result = num1.divide(num2);
		assertEquals("123456789123456789", result.toString(), "Division by one should return the same number");
	}

	@Test
	void power() {
		BigNumber num1 = new BigNumber("2");
		BigNumber result = num1.power(new BigNumber("10"));
		assertEquals("1024", result.toString(), "2^10 should be 1024");

		num1 = new BigNumber("5");
		result = num1.power(new BigNumber("0"));
		assertEquals("1", result.toString(), "Any number to power 0 should be 1");

		num1 = new BigNumber("1.1");
		result = num1.power(new BigNumber("2"));
		assertEquals("1.21", result.toString(), "1.1^2 should be 1.21");
	}

	@Test
	void squareRoot() {
		BigNumber num = new BigNumber("9");
		BigNumber result = num.squareRoot();
		assertEquals("3", result.toString(), "Square root of 9 should be 3");

		num = new BigNumber("2");
		result = num.squareRoot();
		assertEquals("1.41421356237309504880168872420969807856967187537694", result.toString(),
			"Square root of 2 should be accurate");

		num = new BigNumber("0");
		result = num.squareRoot();
		assertEquals("0", result.toString(), "Square root of 0 should be 0");
	}

	@Test
	void factorial() {
		BigNumber num = new BigNumber("0");
		BigNumber result = num.factorial();
		assertEquals("1", result.toString(), "0! should be 1");

		num = new BigNumber("1");
		result = num.factorial();
		assertEquals("1", result.toString(), "1! should be 1");

		num = new BigNumber("5");
		result = num.factorial();
		assertEquals("120", result.toString(), "5! should be 120");

		num = new BigNumber("20");
		result = num.factorial();
		assertEquals("2432902008176640000", result.toString(), "20! should be 2432902008176640000");
	}

}
