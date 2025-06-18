package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.MathContext;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BigNumberTest {

	@Nested
	public class BasicMath {

		@ParameterizedTest
		@CsvSource({
			"25.5,30.2,55.7",
			"0.000000000000000000000000000000000000000000000001,0.00000000000000000003,0.000000000000000000030000000000000000000000000001",
			"8736519650165165946166562572365809265462671456,143153651451954101155145145169254155145,8736519793318817398120663727510954434716826601",
			"123,456,579"
		})
		void additionTest(String inputNum1, String inputNum2, String inputExpectedResult) {
			BigNumber num1 = new BigNumber(inputNum1);
			BigNumber num2 = new BigNumber(inputNum2);

			BigNumber actualResult = num1.add(num2);

			assertEquals(inputExpectedResult, actualResult.toString());
		}

		@ParameterizedTest
		@CsvSource({
			"100,30,70",
			"0.0000000001,0.00000000009,0.00000000001",
			"500,1000,-500"
		})
		void subtractionTest(String inputNum1, String inputNum2, String inputExpectedResult) {
			BigNumber num1 = new BigNumber(inputNum1);
			BigNumber num2 = new BigNumber(inputNum2);
			BigNumber result = num1.subtract(num2);
			assertEquals(inputExpectedResult, result.toString());
		}

		@ParameterizedTest
		@CsvSource({
			"12,3,36",
			"0.00001,100000,1",
			"123456789,987654321,121932631112635269"
		})
		void multiplicationTest(String inputNum1, String inputNum2, String inputExpectedResult) {
			BigNumber num1 = new BigNumber(inputNum1);
			BigNumber num2 = new BigNumber(inputNum2);
			BigNumber result = num1.multiply(num2);
			assertEquals(inputExpectedResult, result.toString());
		}

		@ParameterizedTest
		@CsvSource({
			"100,4,25",
			"1,3,0.33333333333333333333",
			"123456789123456789,1,123456789123456789"
		})
		void divisionTest(String inputNum1, String inputNum2, String inputExpectedResult) {
			BigNumber num1 = new BigNumber(inputNum1);
			BigNumber num2 = new BigNumber(inputNum2);
			BigNumber result = num1.divide(num2, new MathContext(20, RoundingMode.HALF_UP));
			assertEquals(inputExpectedResult, result.trim().toString());
		}

		@ParameterizedTest
		@CsvSource({
			"2,10,1024",
			"5,0,1",
			"1.1,2,1.21",
			"-1.2,-2.99,-0.579759767"
		})
		void powerTest(String inputNum1, String inputNum2, String inputExpectedResultPrefix) {
			BigNumber num1 = new BigNumber(inputNum1);
			BigNumber exponent = new BigNumber(inputNum2);
			BigNumber result = num1.power(exponent);
			assertEquals(inputExpectedResultPrefix, result.trim().toString().substring(0, inputExpectedResultPrefix.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"0,1",
			"1,1",
			"5,120",
			"20,2432902008176640000"
		})
		void factorialTest(String inputNum, String inputExpectedResult) {
			BigNumber num = new BigNumber(inputNum);
			BigNumber result = num.factorial();
			assertEquals(inputExpectedResult, result.toString());
		}

	}

	@Nested
	public class CombinatoricsMath {

		@ParameterizedTest
		@CsvSource({
			"0,0,1",
			"1,1,1",
			"12,7,792"
		})
		void combinationTest(String inputNum1, String inputNum2, String inputExpectedResult) {
			BigNumber num1 = new BigNumber(inputNum1);
			BigNumber num2 = new BigNumber(inputNum2);
			BigNumber result = num1.combination(num2);

			assertEquals(inputExpectedResult, result.toString());
		}

		@Test
		void combinationInvalidInputTest() {
			BigNumber num1 = new BigNumber("123");
			BigNumber num2 = new BigNumber("345");

			assertThrows(IllegalArgumentException.class, () -> num1.combination(num2));
		}

		@ParameterizedTest
		@CsvSource({
			"0,0,1",
			"1,1,1",
			"12,7,3991680"
		})
		void permutationTest(String inputNum1, String inputNum2, String inputExpectedResult) {
			BigNumber num1 = new BigNumber(inputNum1);
			BigNumber num2 = new BigNumber(inputNum2);
			BigNumber result = num1.permutation(num2);

			assertEquals(inputExpectedResult, result.toString());
		}

		@Test
		void permutationInvalidInputTest() {
			BigNumber num1 = new BigNumber("123");
			BigNumber num2 = new BigNumber("345");

			assertThrows(IllegalArgumentException.class, () -> num1.permutation(num2));
		}

	}

	@Nested
	public class CoordinateConversionMath {

		@ParameterizedTest
		@CsvSource({
			"0,0,0,0",
			"12.874,7.000032,12.7780382,1.56895306"
		})
		void polarToCartesianCoordinateTest(String inputNum1, String inputNum2, String expectedX, String expectedY) {
			BigNumber num1 = new BigNumber(inputNum1);
			BigNumber num2 = new BigNumber(inputNum2);
			BigNumberCoordinate result = num1.polarToCartesianCoordinates(num2);

			assertEquals(expectedX, result.getX().toString().substring(0, expectedX.length()));
			assertEquals(expectedY, result.getY().toString().substring(0, expectedY.length()));
		}

		@Test
		void polarToCartesianCoordinateInvalidTest() {
			BigNumber num1 = new BigNumber("-1");
			BigNumber num2 = new BigNumber("0");

			assertThrows(IllegalArgumentException.class, () -> num1.polarToCartesianCoordinates(num2));
		}

		@ParameterizedTest
		@CsvSource({
			"1,1,1.41421356,45",
			"12.874,7.000032,14.6540207,28.5344307"
		})
		void cartesianToPolarCoordinateTest(String inputNum1, String inputNum2, String expectedX, String expectedY) {
			BigNumber num1 = new BigNumber(inputNum1);
			BigNumber num2 = new BigNumber(inputNum2);
			BigNumberCoordinate result = num1.cartesianToPolarCoordinates(num2);

			assertEquals(expectedX, result.getX().toString().substring(0, expectedX.length()));
			assertEquals(expectedY, result.getY().toString().substring(0, expectedY.length()));
		}

		@Test
		void cartesianToPolarCoordinateInvalidTest() {
			BigNumber num1 = new BigNumber("-1");
			BigNumber num2 = new BigNumber("0");

			assertThrows(IllegalArgumentException.class, () -> num1.cartesianToPolarCoordinates(num2));
		}

	}

	@Nested
	public class HyperbolicTrigonometricMath {

		@ParameterizedTest
		@CsvSource({
			"0,0",
			"1,1.17520119",
			"-1,-1.1752011",
			"1.2541,1.60967510"
		})
		void sinhTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input);
			BigNumber result = num.sinh().round(new MathContext(expectedResult.length(), RoundingMode.HALF_UP)).trim();

			assertEquals(expectedResult, result.toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"0,1",
			"1,1.54308063",
			"-1,1.54308063",
			"1.2541,1.89500763"
		})
		void coshTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input);
			BigNumber result = num.cosh().round(new MathContext(expectedResult.length(), RoundingMode.HALF_UP)).trim();

			assertEquals(expectedResult, result.toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"0,0",
			"1,0.76159415",
			"-1,-0.7615941",
			"1.2541,0.84942934"
		})
		void tanhTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input);
			BigNumber result = num.tanh();

			assertEquals(expectedResult, result.toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"1,1.313035285",
			"-1,-1.313035285"
		})
		void cothTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input);
			BigNumber result = num.coth();

			assertEquals(expectedResult, result.round(new MathContext(10 /* hardcoding = happy :) */, RoundingMode.HALF_UP)).toString());
		}

		@Test
		void cothInvalidTest() {
			BigNumber zero = new BigNumber("0");
			assertThrows(IllegalArgumentException.class, zero::coth, "coth(0) should throw ArithmeticException (division by zero)");
		}

	}

	@Nested
	public class InverseHyperbolicTrigonometricMath {

		@ParameterizedTest
		@CsvSource({
			"0,0",
			"1,0.88137358",
			"-1,-0.8813735",
			"2.5,1.64723114"
		})
		void asinhTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input);
			BigNumber result = num.asinh();

			assertEquals(expectedResult, result.toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"1,0",
			"2,1.31695789",
			"10,2.99322284"
		})
		void acoshTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input);
			BigNumber result = num.acosh();

			assertEquals(expectedResult, result.toString().substring(0, expectedResult.length()));
		}

		@Test
		void acoshInvalidInputTest() {
			BigNumber invalid = new BigNumber("0.5");
			assertThrows(IllegalArgumentException.class, invalid::acosh, "acosh(x < 1) should throw exception");
		}

		@ParameterizedTest
		@CsvSource({
			"0.5,0.54930614"
		})
		void atanhTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input);
			BigNumber result = num.atanh();

			assertEquals(expectedResult, result.toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"-1.01",
			"1"
		})
		void atanhInvalidInputTest(String input) {
			BigNumber num = new BigNumber(input);
			assertThrows(IllegalArgumentException.class, num::atanh, "atanh(|x| >= 1) should throw exception");
		}

		@ParameterizedTest
		@CsvSource({
			"2,0.54930614",
			"-2,-0.5493061"
		})
		void acothTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input);
			BigNumber result = num.acoth();

			assertEquals(expectedResult, result.toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"0.5",
			"1",
			"-1" // throws ArithmeticException
		})
		void acothInvalidInputTest(String input) {
			BigNumber num = new BigNumber(input);
			assertThrows(Exception.class, num::acoth, "acoth(|x| <= 1) should throw exception");
		}

	}

	@Nested
	public class InverseTrigonometricMath {

		@ParameterizedTest
		@CsvSource({
			"0,RAD,0",
			"0,DEG,0",
			"1,RAD,1.570796",
			"1,DEG,90",
			"-1,RAD,-1.570796",
			"-1,DEG,-90"
		})
		void asinTest(String input, TrigonometricMode trigonometricMode, String expectedResult) {
			BigNumber num = new BigNumber(input, trigonometricMode);
			assertEquals(expectedResult, num.asin().toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"1,RAD,0",
			"1,DEG,0",
			"0,RAD,1.570796",
			"0,DEG,90",
			"-1,RAD,3.141592",
			"-1,DEG,180"
		})
		void acosTest(String input, TrigonometricMode trigonometricMode, String expectedResult) {
			BigNumber num = new BigNumber(input, trigonometricMode);
			assertEquals(expectedResult, num.acos().toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"0,RAD,0",
			"0,DEG,0",
			"1,RAD,0.785398",
			"1,DEG,45",
			"-1,RAD,-0.785398",
			"-1,DEG,-45",
			"1000,RAD,1.569796",
			"1000,DEG,89.942"
		})
		void atanTest(String input, TrigonometricMode trigonometricMode, String expectedResult) {
			BigNumber num = new BigNumber(input, trigonometricMode);
			assertEquals(expectedResult, num.atan().toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"1,RAD,0.785398",
			"1,DEG,45",
			"2,RAD,0.463647",
			"2,DEG,26,565052",
			// "-2,RAD,-0.463647", // TODO: es kommt ein falsches Ergebnis raus
			// "-2,DEG,-26,565052" // TODO: es kommt ein falsches Ergebnis raus
		})
		void acotTest(String input, TrigonometricMode trigonometricMode, String expectedResult) {
			BigNumber num = new BigNumber(input);
			assertEquals(expectedResult, num.acot(trigonometricMode).toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"2,RAD",
			"2,DEG"
		})
		void asinInvalidTest(String input, TrigonometricMode trigonometricMode) {
			BigNumber num = new BigNumber(input, trigonometricMode);
			assertThrows(ArithmeticException.class, num::asin);
		}

		@ParameterizedTest
		@CsvSource({
			"1.5,RAD",
			"1.5,DEG"
		})
		void acosInvalidTest(String input, TrigonometricMode trigonometricMode) {
			BigNumber num = new BigNumber(input, trigonometricMode);
			assertThrows(ArithmeticException.class, num::acos);
		}

		@ParameterizedTest
		@CsvSource({
			"0,RAD",
			"0,DEG"
		})
		void acotInvalidTest(String input, TrigonometricMode trigonometricMode) {
			BigNumber num = new BigNumber(input, trigonometricMode);
			assertThrows(ArithmeticException.class, num::acot);
		}

	}

	@Nested
	public class LogarithmicMath {

	}

	@Nested
	public class NumberTheoryMath {

	}

	@Nested
	public class PercentageMath {

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

			num = new BigNumber("8");
			root = new BigNumber("-12");
			result = num.nthRoot(root);
			assertEquals("0.840896415", result.trim().toString().substring(0, 11), "-12th root of 8 should be (rounded) 0.840896415");

			BigNumber finalNum = new BigNumber("-16");
			BigNumber finalRoot = new BigNumber("4");
			assertThrows(IllegalArgumentException.class, () -> finalNum.nthRoot(finalRoot),
				"Even root of negative number should throw exception");
		}

	}

	@Nested
	public class TrigonometricMath {

	}

	@Nested
	public class TwoDimensionalMath {

	}

}
