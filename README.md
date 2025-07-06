# 📐 JustMath

**JustMath** is a high-precision, extensible **Java math library** featuring its own `BigNumber` class and a modern
**string-based calculation engine**. It is designed to evaluate **complex mathematical expressions with virtually
unlimited precision**, avoiding the limitations of primitive types like `double` or `float`.

## 🧮 Features

✅ **Virtually unlimited precision** via `BigNumber`
✅ **String-based expression evaluation**
✅ **Supports trigonometry, logarithms, combinatorics, coordinates, factorials, and more**

## 🔢 BigNumber – Precision Without Limits

The `BigNumber` class supports a wide range of mathematical operations:

| Category                         | Methods                                                                |
|----------------------------------|------------------------------------------------------------------------|
| **Basic Arithmetic**             | `add`, `subtract`, `multiply`, `divide`, `modulo`, `power`             |
| **Roots & Powers**               | `squareRoot`, `cubicRoot`, `nthRoot`, `exp`, `factorial`               |
| **Logarithms**                   | `log2`, `log10`, `ln`, `logBase`                                       |
| **Trigonometry**                 | `sin`, `cos`, `tan`, `cot`, `atan`, `acot`, `atan2`                    |
| **Hyperbolic Functions**         | `sinh`, `cosh`, `tanh`, `coth`                                         |
| **Inverse Hyperbolic Functions** | `asinh`, `acosh`, `atanh`, `acoth`                                     |
| **Combinatorics**                | `combination`, `permutation`                                           |
| **Coordinate Transformations**   | `polarToCartesianCoordinates`, `cartesianToPolarCoordinates`           |
| **Miscellaneous**                | `randomIntegerForRange`, `percentFromM`, `isXPercentOfN`, `gcd`, `lcm` |

All methods support customizable **`MathContext`** and **`Locale` settings** to meet international precision and
formatting requirements.

## 🔧 CalculatorEngine – Evaluate Math Strings

The built-in **CalculatorEngine** directly evaluates mathematical strings with support for:

✅ Arithmetic and power operators (`+`, `-`, `*`, `/`, `%`, `^`, `!`)
✅ Math functions (trigonometry, logarithms, roots, combinatorics, coordinates)
✅ Parentheses for nested expressions
✅ Flexible trigonometric modes (DEG/RAD)

### ✅ Supported Operators & Functions

| Category                         | Operator / Function     | Description                       |
|----------------------------------|-------------------------|-----------------------------------|
| **Arithmetic**                   | `+`, `-`, `*`, `/`      | Basic operations                  |
|                                  | `%`, `^`, `!`           | Modulo, exponentiation, factorial |
| **Roots**                        | `√`, `sqrt`             | Square root                       |
|                                  | `³√`, `cbrt`            | Cube root                         |
|                                  | `rootn(a, n)`           | n-th root                         |
| **Logarithms**                   | `log2(x)`               | Base-2 logarithm                  |
|                                  | `log(x)`                | Base-10 logarithm                 |
|                                  | `ln(x)`                 | Natural logarithm                 |
|                                  | `logbase(x, b)`         | Logarithm with arbitrary base     |
| **Trigonometry**                 | `sin(x)`, `cos(x)`      | Sine, cosine                      |
|                                  | `tan(x)`, `cot(x)`      | Tangent, cotangent                |
|                                  | `atan(x)`, `tan⁻¹(x)`   | Arctangent                        |
|                                  | `acot(x)`, `cot⁻¹(x)`   | Arccotangent                      |
|                                  | `atan2(y, x)`           | Two-argument arctangent           |
| **Hyperbolic Functions**         | `sinh(x)`, `cosh(x)`    | Hyperbolic sine, cosine           |
|                                  | `tanh(x)`, `coth(x)`    | Hyperbolic tangent, cotangent     |
| **Inverse Hyperbolic Functions** | `asinh(x)`, `sinh⁻¹(x)` | Inverse hyperbolic sine           |
|                                  | `acosh(x)`, `cosh⁻¹(x)` | Inverse hyperbolic cosine         |
|                                  | `atanh(x)`, `tanh⁻¹(x)` | Inverse hyperbolic tangent        |
|                                  | `acoth(x)`, `coth⁻¹(x)` | Inverse hyperbolic cotangent      |
| **Combinatorics**                | `nCr`, `comb(n, r)`     | Combinations                      |
|                                  | `nPr`, `perm(n, r)`     | Permutations                      |
| **Number Theory**                | `GCD(a, b)`             | Greatest common divisor           |
|                                  | `LCM(a, b)`             | Least common multiple             |
| **Random Generator**             | `RandInt(min, max)`     | Random integer in a given range   |
| **Coordinates**                  | `Pol(x, y)`             | Cartesian → Polar                 |
|                                  | `Rec(r, θ)`             | Polar → Cartesian                 |

## 📚 Static Utility Methods

JustMath provides a suite of **static utility methods** grouped in dedicated classes. These can be used independently of
`BigNumber` or `CalculatorEngine` for direct access to high-precision calculations.

| Class                                | Method(s)                                                                 | Description                                              |
|--------------------------------------|---------------------------------------------------------------------------|----------------------------------------------------------|
| `BasicMath`                          | `add`, `subtract`, `multiply`, `divide`, `modulo`, `power`                | Basic arithmetic operations                              |
|                                      | `factorial`, `exp`                                                        | Factorial and exponential function                       |
| `CombinatoricsMath`                  | `combination`, `permutation`                                              | Calculate combinations (nCr) and permutations (nPr)      |
| `CoordinateConversionMath`           | `polarToCartesianCoordinates`, `cartesianToPolarCoordinates`              | Convert between polar and cartesian coordinates          |
| `HyperbolicTrigonometricMath`        | `sinh`, `cosh`, `tanh`, `coth`                                            | Hyperbolic sine, cosine, tangent, and cotangent          |
| `InverseHyperbolicTrigonometricMath` | `asinh`, `acosh`, `atanh`, `acoth`                                        | Inverse hyperbolic functions                             |
| `InverseTrigonometricMath`           | `asin`, `acos`, `atan`, `acot`                                            | Inverse trigonometric functions                          |
| `LogarithmicMath`                    | `log2`, `log10`, `ln`, `logBase`                                          | Binary, decimal, natural, and arbitrary base logarithms  |
| `NumberTheoryMath`                   | `gcd`, `lcm`                                                              | Greatest common divisor and least common multiple        |
| `PercentageMath`                     | `nPercentFromM`, `xIsNPercentOfN`                                         | Percent calculations                                     |
| `RadicalMath`                        | `squareRoot`, `cubicRoot`, `nthRoot`                                      | Compute square, cube, and n-th roots                     |
| `TrigonometricMath`                  | `sin`, `cos`, `tan`, `coth`                                               | Trigonometric functions (coth also here for convenience) |
| `TwoDimensionalMath`                 | `atan2`                                                                   | Two-argument arctangent                                  |
| `MathUtils`                          | `convertAngle`, `bigDecimalRadiansToDegrees`, `bigDecimalNumberToRadians` | Angle conversions                                        |
|                                      | `randomIntegerBigNumberInRange`                                           | Random integer generation using `BigNumber`              |
|                                      | `e`, `pi`                                                                 | Mathematical constants as `BigNumber`                    |

## 📐 Constants

The `BigNumberValues` class provides reusable **high-precision constants** and default configuration values that are
used throughout JustMath. These can be accessed statically and are ideal for custom calculations or configurations.

| Constant                     | Description                                     |
|------------------------------|-------------------------------------------------|
| `CALCULATION_LOCALE`         | Default `Locale` used for parsing/formatting    |
| `DEFAULT_DIVISION_PRECISION` | Default precision for division operations       |
| `DEFAULT_MATH_CONTEXT`       | Default `MathContext` with precision & rounding |
| `NEGATIVE_ONE`               | BigNumber value of -1                           |
| `ZERO`                       | BigNumber value of 0                            |
| `ONE`                        | BigNumber value of 1                            |
| `TWO`                        | BigNumber value of 2                            |
| `THREE`                      | BigNumber value of 3                            |
| `ONE_HUNDRED`                | BigNumber value of 100                          |
| `ONE_HUNDRED_EIGHTY`         | BigNumber value of 180                          |

## 🧑‍💻 Practical Examples

### ➕ Add Very Large Numbers

```java
BigNumber num1 = new BigNumber("8736519650165165946166562572365809265462671456");
BigNumber num2 = new BigNumber("143153651451954101155145145169254155145");
BigNumber sum = num1.add(num2);

System.out.println(sum);
// 8736519793318817398120663727510954434716826601
```

### ➖ Subtract Small Decimals

```java
BigNumber a = new BigNumber("0.0000000001");
BigNumber b = new BigNumber("0.00000000009");
BigNumber diff = a.subtract(b);

System.out.println(diff);
// 0.00000000001
```

### ➗ High-Precision Fractions

```java
BigNumber e = new BigNumber("1");
BigNumber f = new BigNumber("3");
BigNumber quotient = e.divide(f, new MathContext(50, RoundingMode.HALF_UP));

System.out.println(quotient);
// 0.33333333333333333333333333333333333333333333333333
```

### ⚡ Power with Negative Exponents

```java
BigNumber base = new BigNumber("-1.2");
BigNumber exponent = new BigNumber("-2.99");
BigNumber result = base.power(exponent);

System.out.println(result);
// -0.579759767...
```

## 🧩 CalculatorEngine – Evaluate Math Strings

### 🚀 Create an Instance

```java
CalculatorEngine calc = new CalculatorEngine(50, TrigonometricMode.DEG);
```

### ➕ Large Number Addition

```java
BigNumber result = calc.evaluate(
	"8736519650165165946166562572365809265462671456 + 143153651451954101155145145169254155145"
);

System.out.println(result);
// 8736519793318817398120663727510954434716826601
```

### ➗ Fractions

```java
BigNumber result = calc.evaluate("1 / 3");

System.out.println(result);
// 0.33333333333333333333333333333333333333333333333333
```

### 📐 Trigonometry in DEG Mode

```java
BigNumber result = calc.evaluate("sin(90)");

System.out.println(result);
// 1
```

### 🎲 Combine Factorials

```java
BigNumber result = calc.evaluate("5! + 2^3");

System.out.println(result);
// 128
```

### 🧮 N-th Root

```java
BigNumber result = calc.evaluate("rootn(27; 3)");

System.out.println(result);
// 3
```

### 🎰 Calculate Combinations

```java
BigNumber result = calc.evaluate("comb(6; 3)");

System.out.println(result);
// 20
```

## ⚙️ Maven (Coming soon)

```xml

<dependency>
    <groupId>io.github.lembergmax</groupId>
    <artifactId>justmath</artifactId>
    <version></version>
</dependency>
```

## 📜 License

**MIT License**

You are free to:

* use
* copy
* modify
* merge
* publish
* distribute
* sublicense
* and/or sell copies of JustMath

for both private and commercial purposes, **as long as the original license and copyright
notice are included** in all copies or substantial portions of the software.

👉 [MIT License – Full Text](https://opensource.org/licenses/MIT)

## 👤 Author

**Max Lemberg**
🔗 [GitHub Profile](https://github.com/lembergmax)

© 2025 Max Lemberg. All rights reserved.
Licensed under the MIT License.
