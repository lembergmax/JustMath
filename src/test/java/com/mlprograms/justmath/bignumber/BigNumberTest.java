package com.mlprograms.justmath.bignumber;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.MathContext;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BigNumberTest {

	@Nested
	public class BasicMath {

		@Test
		void additionTest() {
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
		void subtractionTest() {
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
		void multiplicationTest() {
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
		void divisionTest() {
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
		void powerTest() {
			BigNumber num1 = new BigNumber("2");
			BigNumber result = num1.power(new BigNumber("10"));
			assertEquals("1024", result.toString(), "2^10 should be 1024");

			num1 = new BigNumber("5");
			result = num1.power(new BigNumber("0"));
			assertEquals("1", result.toString(), "Any number to power 0 should be 1");

			num1 = new BigNumber("1.1");
			result = num1.power(new BigNumber("2"));
			assertEquals("1.21", result.toString(), "1.1^2 should be 1.21");

			num1 = new BigNumber("-1.2");
			result = num1.power(new BigNumber("-2.99"));
			assertEquals("-0.579759767", result.trim().toString().substring(0, 12), "1.1^2 should be -0.579759767");
		}

		@Test
		void factorialTest() {
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

	@Nested
	public class RadicalMath {

		@Test
		void squareRootTest() {
			BigNumber num = new BigNumber("9");
			BigNumber result = num.squareRoot();
			assertEquals("3", result.toString(), "Square root of 9 should be 3");

			num = new BigNumber("2");
			result = num.squareRoot();
			assertEquals("1.41421356237309504880168872420969807856967187537694", result.toString().substring(0, 52),
				"Square root of 2 should be accurate");

			num = new BigNumber("0");
			result = num.squareRoot();
			assertEquals("0", result.toString(), "Square root of 0 should be 0");
		}

		@Test
		void cubicRootTest() {
			BigNumber num = new BigNumber("27");
			BigNumber result = num.cubicRoot();
			assertEquals("3", result.toString(), "Cubic root of 27 should be 3");

			num = new BigNumber("0");
			result = num.cubicRoot();
			assertEquals("0", result.toString(), "Cubic root of 0 should be 0");

			num = new BigNumber("8");
			result = num.cubicRoot();
			assertEquals("2", result.toString(), "Cubic root of 8 should be 2");

			num = new BigNumber("-27");
			result = num.cubicRoot();
			assertEquals("-3", result.toString(), "Cubic root of -27 should be -3");
		}

		@Test
		void nthRootTest() {
			BigNumber num = new BigNumber("81");
			BigNumber root = new BigNumber("4");
			BigNumber result = num.nthRoot(root);
			assertEquals("3", result.toString(), "4th root of 81 should be 3");

			num = new BigNumber("32");
			root = new BigNumber("5");
			result = num.nthRoot(root);
			assertEquals("2", result.toString(), "5th root of 32 should be 2");

			num = new BigNumber("1");
			root = new BigNumber("100");
			result = num.nthRoot(root);
			assertEquals("1", result.toString(), "Any root of 1 should be 1");

			num = new BigNumber("-8");
			root = new BigNumber("3");
			result = num.nthRoot(root);
			assertEquals("-2", result.toString(), "3rd root of -8 should be -2");

			BigNumber finalNum = new BigNumber("-16");
			BigNumber finalRoot = new BigNumber("4");
			assertThrows(IllegalArgumentException.class, () -> finalNum.nthRoot(finalRoot),
				"Even root of negative number should throw exception");
		}

	}

	@Nested
	public class CombinatoricsMath {

		@Test
		void combinationTest() {
			BigNumber num1 = new BigNumber("0");
			BigNumber num2 = new BigNumber("0");
			BigNumber result = num1.combination(num2);

			assertEquals("1", result.toString(), "0C0 should be 1");

			num1 = new BigNumber("1");
			num2 = new BigNumber("1");
			result = num1.combination(num2);

			assertEquals("1", result.toString(), "1C1 should be 1");

			num1 = new BigNumber("12");
			num2 = new BigNumber("7");
			result = num1.combination(num2);

			assertEquals("792", result.toString(), "7C1 should be 792");

			num1 = new BigNumber("123");
			num2 = new BigNumber("345");

			BigNumber finalNum = num1;
			BigNumber finalNum1 = num2;
			assertThrows(IllegalArgumentException.class, () -> finalNum.combination(finalNum1));
		}

		@Test
		void permutationTest() {
			BigNumber num1 = new BigNumber("0");
			BigNumber num2 = new BigNumber("0");
			BigNumber result = num1.permutation(num2);

			assertEquals("1", result.toString(), "0P0 should be 1");

			num1 = new BigNumber("1");
			num2 = new BigNumber("1");
			result = num1.permutation(num2);

			assertEquals("1", result.toString(), "1P1 should be 1");

			num1 = new BigNumber("12");
			num2 = new BigNumber("7");
			result = num1.permutation(num2);

			assertEquals("3991680", result.toString(), "7P1 should be 3991680");

			num1 = new BigNumber("123");
			num2 = new BigNumber("345");

			BigNumber finalNum = num1;
			BigNumber finalNum1 = num2;
			assertThrows(IllegalArgumentException.class, () -> finalNum.permutation(finalNum1));
		}

	}

	@Nested
	public class CoordinateConversionMath {

		@Test
		void polarToCartesianCoordinateTest() {
			BigNumber num1 = new BigNumber("-1");
			BigNumber num2 = new BigNumber("0");
			BigNumberCoordinate result;

			BigNumber finalNum = num1;
			BigNumber finalNum1 = num2;
			assertThrows(IllegalArgumentException.class, () -> finalNum.polarToCartesianCoordinates(finalNum1));

			num1 = new BigNumber("0");
			num2 = new BigNumber("0");
			result = num1.polarToCartesianCoordinates(num2);

			assertEquals("0", result.getX().toString());
			assertEquals("0", result.getY().toString());

			num1 = new BigNumber("12.874");
			num2 = new BigNumber("7.000032");
			result = num1.polarToCartesianCoordinates(num2);

			assertEquals("12.7780382", result.getX().toString().substring(0, 10));
			assertEquals("1.56895306", result.getY().toString().substring(0, 10));
		}

		@Test
		void cartesianToPolarCoordinateTest() {
			BigNumber num1 = new BigNumber("-1");
			BigNumber num2 = new BigNumber("0");
			BigNumberCoordinate result;

			BigNumber finalNum = num1;
			BigNumber finalNum1 = num2;
			assertThrows(IllegalArgumentException.class, () -> finalNum.cartesianToPolarCoordinates(finalNum1));

			num1 = new BigNumber("1");
			num2 = new BigNumber("1");
			result = num1.cartesianToPolarCoordinates(num2);

			assertEquals("1.41421356", result.getX().toString().substring(0, 10));
			assertEquals("45", result.getY().toString());

			num1 = new BigNumber("12.874");
			num2 = new BigNumber("7.000032");
			result = num1.cartesianToPolarCoordinates(num2);

			assertEquals("14.6540207", result.getX().toString().substring(0, 10));
			assertEquals("28.5344307", result.getY().toString().substring(0, 10));
		}

	}

	@Nested
	public class HyperbolicTrigonometricMath {

		@Test
		void sinhTest() {

		}

		@Test
		void coshTest() {

		}

		@Test
		void tanhTest() {

		}

		@Test
		void cothTest() {

		}

	}

	@Nested
	public class InverseHyperbolicTrigonometricMath {

		@Test
		void asinhTest() {

		}

		@Test
		void acoshTest() {

		}

		@Test
		void atanhTest() {

		}

		@Test
		void acothTest() {

		}

	}

}
