package com.mlprograms.justmath.bignumber;

import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

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
			BigNumber num1 = new BigNumber(inputNum1, Locale.US);
			BigNumber num2 = new BigNumber(inputNum2, Locale.US);

			BigNumber actualResult = num1.add(num2, Locale.US);

			assertEquals(inputExpectedResult, actualResult.toString());
		}

		@ParameterizedTest
		@CsvSource({
			"100,30,70",
			"0.0000000001,0.00000000009,0.00000000001",
			"500,1000,-500"
		})
		void subtractionTest(String inputNum1, String inputNum2, String inputExpectedResult) {
			BigNumber num1 = new BigNumber(inputNum1, Locale.US);
			BigNumber num2 = new BigNumber(inputNum2, Locale.US);
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
			BigNumber num1 = new BigNumber(inputNum1, Locale.US);
			BigNumber num2 = new BigNumber(inputNum2, Locale.US);
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
			BigNumber num1 = new BigNumber(inputNum1, Locale.US);
			BigNumber num2 = new BigNumber(inputNum2, Locale.US);
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
			BigNumber num1 = new BigNumber(inputNum1, Locale.US);
			BigNumber exponent = new BigNumber(inputNum2, Locale.US);
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
			BigNumber num = new BigNumber(inputNum, Locale.US);
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
			BigNumber num1 = new BigNumber(inputNum1, Locale.US);
			BigNumber num2 = new BigNumber(inputNum2, Locale.US);
			BigNumber result = num1.combination(num2);

			assertEquals(inputExpectedResult, result.toString());
		}

		@Test
		void combinationInvalidInputTest() {
			BigNumber num1 = new BigNumber("123", Locale.US);
			BigNumber num2 = new BigNumber("345", Locale.US);

			assertThrows(IllegalArgumentException.class, () -> num1.combination(num2));
		}

		@ParameterizedTest
		@CsvSource({
			"0,0,1",
			"1,1,1",
			"12,7,3991680"
		})
		void permutationTest(String inputNum1, String inputNum2, String inputExpectedResult) {
			BigNumber num1 = new BigNumber(inputNum1, Locale.US);
			BigNumber num2 = new BigNumber(inputNum2, Locale.US);
			BigNumber result = num1.permutation(num2);

			assertEquals(inputExpectedResult, result.toString());
		}

		@Test
		void permutationInvalidInputTest() {
			BigNumber num1 = new BigNumber("123", Locale.US);
			BigNumber num2 = new BigNumber("345", Locale.US);

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
			BigNumber num1 = new BigNumber(inputNum1, Locale.US);
			BigNumber num2 = new BigNumber(inputNum2, Locale.US);
			BigNumberCoordinate result = num1.polarToCartesianCoordinates(num2);

			assertEquals(expectedX, result.getX().toString().substring(0, expectedX.length()));
			assertEquals(expectedY, result.getY().toString().substring(0, expectedY.length()));
		}

		@Test
		void polarToCartesianCoordinateInvalidTest() {
			BigNumber num1 = new BigNumber("-1", Locale.US);
			BigNumber num2 = new BigNumber("0", Locale.US);

			assertThrows(IllegalArgumentException.class, () -> num1.polarToCartesianCoordinates(num2));
		}

		@ParameterizedTest
		@CsvSource({
			"1,1,1.41421356,45",
			"12.874,7.000032,14.6540207,28.5344307"
		})
		void cartesianToPolarCoordinateTest(String inputNum1, String inputNum2, String expectedX, String expectedY) {
			BigNumber num1 = new BigNumber(inputNum1, Locale.US);
			BigNumber num2 = new BigNumber(inputNum2, Locale.US);
			BigNumberCoordinate result = num1.cartesianToPolarCoordinates(num2);

			assertEquals(expectedX, result.getX().toString().substring(0, expectedX.length()));
			assertEquals(expectedY, result.getY().toString().substring(0, expectedY.length()));
		}

		@Test
		void cartesianToPolarCoordinateInvalidTest() {
			BigNumber num1 = new BigNumber("-1", Locale.US);
			BigNumber num2 = new BigNumber("0", Locale.US);

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
			BigNumber num = new BigNumber(input, Locale.US);
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
			BigNumber num = new BigNumber(input, Locale.US);
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
			BigNumber num = new BigNumber(input, Locale.US);
			BigNumber result = num.tanh();

			assertEquals(expectedResult, result.toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"1,1.313035285",
			"-1,-1.313035285"
		})
		void cothTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input, Locale.US);
			BigNumber result = num.coth();

			assertEquals(expectedResult, result.round(new MathContext(10 /* hardcoding = happy :) */, RoundingMode.HALF_UP)).toString());
		}

		@Test
		void cothInvalidTest() {
			BigNumber zero = new BigNumber("0", Locale.US);
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
			BigNumber num = new BigNumber(input, Locale.US);
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
			BigNumber num = new BigNumber(input, Locale.US);
			BigNumber result = num.acosh();

			assertEquals(expectedResult, result.toString().substring(0, expectedResult.length()));
		}

		@Test
		void acoshInvalidInputTest() {
			BigNumber invalid = new BigNumber("0.5", Locale.US);
			assertThrows(IllegalArgumentException.class, invalid::acosh, "acosh(x < 1) should throw exception");
		}

		@ParameterizedTest
		@CsvSource({
			"0.5,0.54930614"
		})
		void atanhTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input, Locale.US);
			BigNumber result = num.atanh();

			assertEquals(expectedResult, result.toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"-1.01",
			"1"
		})
		void atanhInvalidInputTest(String input) {
			BigNumber num = new BigNumber(input, Locale.US);
			assertThrows(IllegalArgumentException.class, num::atanh, "atanh(|x| >= 1) should throw exception");
		}

		@ParameterizedTest
		@CsvSource({
			"2,0.54930614",
			"-2,-0.5493061"
		})
		void acothTest(String input, String expectedResult) {
			BigNumber num = new BigNumber(input, Locale.US);
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
			BigNumber num = new BigNumber(input, Locale.US);
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
			BigNumber num = new BigNumber(input, trigonometricMode, Locale.US);
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
			BigNumber num = new BigNumber(input, trigonometricMode, Locale.US);
			assertEquals(expectedResult, num.atan().toString().substring(0, expectedResult.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"1,RAD,0.785398",
			"1,DEG,45",
			"2,RAD,0.463647",
			"2,DEG,26,565052",
			"-2,RAD,-0.463647",
			"-2,DEG,-26,565052"
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

		@ParameterizedTest
		@CsvSource({
			"1,0",
			"2,1",
			"8,3",
			"10,3.321928094"
		})
		void log2Test(String input, String expectedPrefix) {
			BigNumber arg = new BigNumber(input);
			BigNumber result = arg.log2(new MathContext(expectedPrefix.length(), RoundingMode.HALF_UP), Locale.US);
			assertEquals(expectedPrefix, result.toString().substring(0, expectedPrefix.length()));
		}

		@Test
		void log2InvalidTest() {
			BigNumber zero = new BigNumber("0");
			assertThrows(IllegalArgumentException.class, () -> zero.log2(new MathContext(10, RoundingMode.HALF_UP), Locale.US));
		}

		@ParameterizedTest
		@CsvSource({
			"1,0",
			"10,1",
			"100,2",
			"2,0.3010299"
		})
		void log10Test(String input, String expectedPrefix) {
			BigNumber arg = new BigNumber(input);
			BigNumber result = arg.log10(new MathContext(expectedPrefix.length(), RoundingMode.HALF_UP), Locale.US);
			assertEquals(expectedPrefix, result.toString().substring(0, expectedPrefix.length()));
		}

		@Test
		void log10InvalidTest() {
			BigNumber neg = new BigNumber("-5");
			assertThrows(IllegalArgumentException.class, () -> neg.log10(new MathContext(10, RoundingMode.HALF_UP), Locale.GERMAN));
		}

		@ParameterizedTest
		@CsvSource({
			"1,0",
			"2.71828182845904523536,1",
			"10,2.302585093"
		})
		void lnTest(String input, String expectedPrefix) {
			BigNumber arg = new BigNumber(input);
			BigNumber result = arg.ln(new MathContext(expectedPrefix.length(), RoundingMode.HALF_UP), Locale.US);
			assertEquals(expectedPrefix, result.toString().substring(0, expectedPrefix.length()));
		}

		@Test
		void lnInvalidTest() {
			BigNumber neg = new BigNumber("0");
			assertThrows(ArithmeticException.class, () -> neg.ln(new MathContext(10, RoundingMode.HALF_UP), Locale.US));
		}

		@ParameterizedTest
		@CsvSource({
			"8,2,3",
			"27,3,3",
			"16,2,4",
			"10,10,1"
		})
		void logBaseTest(String number, String base, String expectedPrefix) {
			BigNumber arg = new BigNumber(number);
			BigNumber b = new BigNumber(base);
			BigNumber result = arg.logBase(b, new MathContext(expectedPrefix.length(), RoundingMode.HALF_UP), Locale.US);
			assertEquals(expectedPrefix, result.toString().substring(0, expectedPrefix.length()));
		}

		@ParameterizedTest
		@CsvSource({
			"0,2",
			"5,1",
			"1,0",
			"-3,2"
		})
		void logBaseInvalidTest(String number, String base) {
			BigNumber arg = new BigNumber(number);
			BigNumber b = new BigNumber(base);
			assertThrows(IllegalArgumentException.class,
				() -> arg.logBase(b, new MathContext(10, RoundingMode.HALF_UP), Locale.US));
		}

	}

	@Nested
	public class NumberTheoryMath {

		@ParameterizedTest
		@CsvSource({
			"12, 8, 4",
			"100, 25, 25",
			"7, 3, 1",
			"-18, 24, 6",
			"0, 5, 5",
			"5, 0, 5",
			"0, 0, 0"
		})
		void gcdTest(String input1, String input2, String expected) {
			BigNumber a = new BigNumber(input1);
			BigNumber b = new BigNumber(input2);
			BigNumber result = a.gcd(b);
			assertEquals(expected, result.toString());
		}

		@ParameterizedTest
		@CsvSource({
			"4, 6, 12",
			"5, 3, 15",
			"0, 7, 0",
			"7, 0, 0",
			"-3, 5, 15",
			"-2, -4, 4"
		})
		void lcmTest(String input1, String input2, String expected) {
			BigNumber a = new BigNumber(input1);
			BigNumber b = new BigNumber(input2);
			BigNumber result = a.lcm(b);
			assertEquals(expected, result.toString());
		}

	}

	@Nested
	public class PercentageMath {

		@ParameterizedTest
		@CsvSource({
			"20, 50, 10",
			"50, 200, 100",
			"0, 1000, 0",
			"100, 42, 42",
			"12.5, 80, 10",
			"-25, 200, -50",
			"150, 10, 15"
		})
		void nPercentFromMTest(String n, String m, String expectedResult) {
			BigNumber percent = new BigNumber(n, Locale.US);
			BigNumber base = new BigNumber(m, Locale.US);

			BigNumber result = base.percentFromM(percent);
			assertEquals(expectedResult, result.toString());
		}

		@ParameterizedTest
		@CsvSource({
			"25, 50, 50",
			"100, 200, 50",
			"42, 42, 100",
			"10, 80, 12.5",
			"0, 1000, 0",
			"-50, 200, -25",
			"15, 10, 150"
		})
		void xIsNPercentOfMTest(String part, String total, String expectedResult) {
			BigNumber partVal = new BigNumber(part, Locale.US);
			BigNumber totalVal = new BigNumber(total, Locale.US);

			BigNumber result = partVal.isXPercentOfN(totalVal);
			assertEquals(expectedResult, result.toString());
		}

	}

	@Nested
	public class RadicalMath {

		@Test
		void squareRootTest() {
			BigNumber num = new BigNumber("9", Locale.US);
			BigNumber result = num.squareRoot();
			assertEquals("3", result.toString(), "Square root of 9 should be 3");

			num = new BigNumber("2", Locale.US);
			result = num.squareRoot();
			assertEquals("1.41421356237309504880168872420969807856967187537694", result.toString().substring(0, 52),
				"Square root of 2 should be accurate");

			num = new BigNumber("0", Locale.US);
			result = num.squareRoot();
			assertEquals("0", result.toString(), "Square root of 0 should be 0");
		}

		@Test
		void cubicRootTest() {
			BigNumber num = new BigNumber("27", Locale.US);
			BigNumber result = num.cubicRoot();
			assertEquals("3", result.toString(), "Cubic root of 27 should be 3");

			num = new BigNumber("0", Locale.US);
			result = num.cubicRoot();
			assertEquals("0", result.toString(), "Cubic root of 0 should be 0");

			num = new BigNumber("8", Locale.US);
			result = num.cubicRoot();
			assertEquals("2", result.toString(), "Cubic root of 8 should be 2");

			num = new BigNumber("-27", Locale.US);
			result = num.cubicRoot();
			assertEquals("-3", result.toString(), "Cubic root of -27 should be -3");
		}

		@Test
		void nthRootTest() {
			BigNumber num = new BigNumber("81", Locale.US);
			BigNumber root = new BigNumber("4", Locale.US);
			BigNumber result = num.nthRoot(root);
			assertEquals("3", result.toString(), "4th root of 81 should be 3");

			num = new BigNumber("32", Locale.US);
			root = new BigNumber("5", Locale.US);
			result = num.nthRoot(root);
			assertEquals("2", result.toString(), "5th root of 32 should be 2");

			num = new BigNumber("1", Locale.US);
			root = new BigNumber("100", Locale.US);
			result = num.nthRoot(root);
			assertEquals("1", result.toString(), "Any root of 1 should be 1");

			num = new BigNumber("8", Locale.US);
			root = new BigNumber("-12", Locale.US);
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

		@ParameterizedTest
		@CsvSource({
			"0, RAD, 0",
			"0, DEG, 0",
			"1, RAD, 0.8414709848",
			"1, DEG, 0.01745240643",
			"1.57079632679, RAD, 1",
			"90, DEG, 1",
			"3.14159265359, RAD, 0",
			"180, DEG, 0",
			"-1.57079632679, RAD, -1",
			"-90, DEG, -1"
		})
		void sinTest(String input, TrigonometricMode trigonometricMode, String expected) {
			BigNumber angle = new BigNumber(input, Locale.US);
			BigNumber result = angle.sin(trigonometricMode);
			assertEquals(expected, result.toString());
		}

		@ParameterizedTest
		@CsvSource({
		})
		void cosTest(String input, TrigonometricMode trigonometricMode, String expected) {
		}

		@ParameterizedTest
		@CsvSource({
		})
		void tanTest(String input, TrigonometricMode trigonometricMode, String expected) {
		}

		@ParameterizedTest
		@CsvSource({
		})
		void cotTest(String input, TrigonometricMode trigonometricMode, String expected) {
		}

	}

	@Nested
	public class TwoDimensionalMath {

		@ParameterizedTest
		@CsvSource({
			"1,1,0.78539",
			"1,-1,2.35619",
			"-1,-1,-2.3561",
			"-1,1,-0.7853",
		})
		void atan2Test(String inputY, String inputX, String expected) {
			BigNumber y = new BigNumber(inputY, Locale.US);
			BigNumber x = new BigNumber(inputX, Locale.US);

			BigNumber result = y.atan2(x);
			assertEquals(expected, result.toString().substring(0, 7));
		}

		@ParameterizedTest
		@CsvSource({
			"0,0",
			"0,1",
			"1,0"
		})
		void atan2InvalidTest(String inputY, String inputX) {
			BigNumber y = new BigNumber(inputY, Locale.US);
			BigNumber x = new BigNumber(inputX, Locale.US);

			assertThrows(IllegalArgumentException.class, () -> y.atan2(x));
		}

	}

}
