# 📐 JustMath

**JustMath** is a high-precision, extensible **Java math library** featuring its own `BigNumber` class and a modern
**string-based calculation engine**. It is designed to evaluate **complex mathematical expressions with virtually
unlimited precision**, avoiding the limitations of primitive types like `double` or `float`.

## 🧮 Features

- ✅ **Virtually unlimited precision** via `BigNumber`
- ✅ **String-based expression evaluation**
- ✅ **Supports trigonometry, logarithms, combinatorics, summations, coordinates, factorials, and many more**
- ✅ **Locale-aware result formatting** — `setLocale(Locale)` drives both error messages and the decimal/grouping separators of evaluation results (incl. `MultiValueResult` components such as `Pol`/`Rec`)

## 🔢 BigNumber

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
| **Series**                       | `summation`, `product`                                                 |
| **Coordinate Transformations**   | `polarToCartesianCoordinates`, `cartesianToPolarCoordinates`           |
| **Miscellaneous**                | `randomIntegerForRange`, `percentFromM`, `isXPercentOfN`, `gcd`, `lcm` |
| **Special Functions**            | `gamma`, `beta`, `abs`                                                 |
| **Statistics**                   | `sum`, `average`, `median`                                             |

All methods support customizable **`MathContext`** and **`Locale` settings** to meet international precision and
formatting requirements.

## 📃 BigNumberList – High-Precision Collections

BigNumberList is a domain-specific, list-like container for BigNumber instances.
It implements java.util.List<BigNumber> and adds high-level statistical, transformation, and sorting utilities on top.

### ✅ Core Capabilities
| Category                 | Methods                                                                                                                                    | Description                                                 |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| **Construction**         | `BigNumberList()`, `BigNumberList(List<BigNumber>)`, `of(...)`, `fromStrings(...)`, `copy`, `clone`                                        | Create lists from existing values or string representations |
| **Conversion**           | `toUnmodifiableList`, `toBigNumberArray`, `toStringList`, `toDoubleArray`                                                                  | Convert to arrays, immutable views, or string/double lists  |
| **Sorting**              | `sort`, `sort(Class<? extends SortingAlgorithm>)`, `sortAscending`, `sortDescending`                                                       | Sort using custom algorithms or natural order               |
| **Statistics**           | `sum`, `average`, `median`, `modes`, `min`, `max`, `range`, `variance`, `standardDeviation`, `geometricMean`, `harmonicMean`               | High-precision statistical operations                       |
| **Transformations**      | `absAll`, `negateAll`, `scale`, `translate`, `powEach`, `clampAll`, `normalizeToSum`, `reverse`, `shuffle`, `rotate`, `map`                | In-place or copy-based transformations on all elements      |
| **Structure & Sets**     | `distinct`, `append`, `subListCopy`                                                                                                        | Remove duplicates, concatenate lists, copy subranges        |
| **Predicates & Queries** | `anyMatch`, `allMatch`, `findFirst`, `filter`, `isSortedAscending`, `isSortedDescending`, `isMonotonicIncreasing`, `isMonotonicDecreasing` | Query list properties in a numerically robust way           |

All higher-level operations are implemented in terms of BigNumber’s arbitrary precision arithmetic and comparison,
avoiding issues with primitive types.

### 🧮 Example: Working with BigNumberList
```java
// Create a high-precision list
BigNumberList numbers = BigNumberList.of(
new BigNumber("3"),
new BigNumber("1"),
new BigNumber("2"),
new BigNumber("2")
);

// Sort ascending using the built-in natural order
numbers.sortAscending();
System.out.println(numbers);
// [1, 2, 2, 3]

// Compute statistics
BigNumber sum     = numbers.sum();       // 8
BigNumber avg     = numbers.average();   // 2
BigNumber median  = numbers.median();    // 2
Set<BigNumber> modes = numbers.modes();  // {2}

// Transform values in-place
numbers.negateAll();                     // [-1, -2, -2, -3]
numbers.absAll();                        // [1, 2, 2, 3]

// Use a custom sorting algorithm
numbers.sort(QuickSort.class);           // Uses your SortingAlgorithm implementation
```

## 🧩 BigNumberMatrix – High-Precision Matrices

The `BigNumberMatrix` class extends the power of `BigNumber` into **linear algebra**.
It supports creation from **dimensions, strings, or nested lists** and provides a wide range of matrix operations with arbitrary precision.

### ✅ Supported Matrix Operations

| Category             | Methods                                                                 |
| -------------------- | ----------------------------------------------------------------------- |
| **Basic Arithmetic** | `add`, `subtract`, `multiply`, `divide` (element-wise)                  |
| **Matrix Algebra**   | `multiply` (matrix product), `power`, `inverse`, `determinant`, `trace` |
| **Transformations**  | `transpose`, `scalarMultiply`, `negate`                                 |
| **Properties**       | `isSquare`, `isSymmetric`, `isZeroMatrix`, `isIdentityMatrix`           |
| **Aggregates**       | `sumElements`, `max`, `flatten`                                         |
| **Utilities**        | `equalsMatrix`, `clone`, `forEachElement`, `forEachIndex`               |

All operations are **locale-aware** and preserve the formatting/parsing rules of `BigNumber`.

### 🧮 Example: Creating and Using Matrices

```java
// Create a 2x2 matrix from a string
BigNumberMatrix a = new BigNumberMatrix("1,2;3,4", Locale.US);

// Compute the determinant
BigNumber det = a.determinant();
System.out.println(det);
// -2

// Compute the inverse
BigNumberMatrix inv = a.inverse();
System.out.println(inv.toPlainDataString());
// [[-2.0, 1.0], [1.5, -0.5]]

// Multiply matrices
BigNumberMatrix b = new BigNumberMatrix("5,6;7,8", Locale.US);
BigNumberMatrix c = a.multiply(b);
System.out.println(c.toPlainDataString());
// [[19, 22], [43, 50]]

// Check identity matrix
BigNumberMatrix i = new BigNumberMatrix("1,0;0,1", Locale.US);
System.out.println(i.isIdentityMatrix());
// true
```


## 🔧 CalculatorEngine – Evaluate Math Strings

The built-in **CalculatorEngine** directly evaluates mathematical strings and supports all operators and functions listed in the following table:

### ✅ Supported Operators & Functions

| Category                         | Operator / Function                              | Description                        |
|----------------------------------|--------------------------------------------------|------------------------------------|
| **Arithmetic**                   | `+`, `-`, `*`, `/`                               | Basic operations                   |
|                                  | `%`, `^`, `!`                                    | Modulo, exponentiation, factorial  |
| **Roots**                        | `√(x)`, `sqrt(x)`                                | Square root                        |
|                                  | `³√(x)`, `cbrt(x)`                               | Cube root                          |
|                                  | `rootn(a, n)`                                    | n-th root                          |
| **Logarithms**                   | `log2(x)`                                        | Base-2 logarithm                   |
|                                  | `log(x)`                                         | Base-10 logarithm                  |
|                                  | `ln(x)`                                          | Natural logarithm                  |
|                                  | `logbase(x, b)`                                  | Logarithm with arbitrary base      |
| **Trigonometry**                 | `sin(x)`, `cos(x)`                               | Sine, cosine                       |
|                                  | `tan(x)`, `cot(x)`                               | Tangent, cotangent                 |
|                                  | `atan(x)`, `tan⁻¹(x)`                            | Arctangent                         |
|                                  | `acot(x)`, `cot⁻¹(x)`                            | Arccotangent                       |
|                                  | `atan2(y, x)`                                    | Two-argument arctangent            |
| **Hyperbolic Functions**         | `sinh(x)`, `cosh(x)`                             | Hyperbolic sine, cosine            |
|                                  | `tanh(x)`, `coth(x)`                             | Hyperbolic tangent, cotangent      |
| **Inverse Hyperbolic Functions** | `asinh(x)`, `sinh⁻¹(x)`                          | Inverse hyperbolic sine            |
|                                  | `acosh(x)`, `cosh⁻¹(x)`                          | Inverse hyperbolic cosine          |
|                                  | `atanh(x)`, `tanh⁻¹(x)`                          | Inverse hyperbolic tangent         |
|                                  | `acoth(x)`, `coth⁻¹(x)`                          | Inverse hyperbolic cotangent       |
| **Combinatorics**                | `nCr(n, r)`, `comb(n, r)`                        | Combinations                       |
|                                  | `nPr(n, r)`, `perm(n, r)`                        | Permutations                       |
| **Series**                       | `∑(start; end; expr)`                            | Sigma notation (e.g., ∑(0;10;2^k)) |
|                                  | `sum(start; end; expr)`                          | Named summation function           |
|                                  | `∏(start; end; expr)`                            | Product notation (e.g., ∏(1;4;k))  |
|                                  | `prod(start; end; expr)`                         | Named product function             |
| **Number Theory**                | `GCD(a, b)`                                      | Greatest common divisor            |
|                                  | `LCM(a, b)`                                      | Least common multiple              |
| **Random Generator**             | `RandInt(min, max)`                              | Random integer in a given range    |
| **Coordinates**                  | `Pol(x, y)`                                      | Cartesian → Polar                  |
|                                  | `Rec(r, θ)`                                      | Polar → Cartesian                  |
| **Special Functions**            | `Γ(x, y)`, `gamma(x)`                            | Gamma                              |
|                                  | `B(x, y)`, `beta(x, y)`                          | Beta                               |
|                                  | `\|x\|`, `abs(x)`                                | Absolute value                     |
| **Statistics**                   | `avg(n1, n2, n3, ...)`, `average(n1, n2, n3...)` | Average of n elements              |
|                                  | `sum(n1, n2, n3, ...)`                           | Sum of n elements                  |
|                                  | `median(n1, n2, n3, ...)`                        | Median of n elements               |

## 🔤 Variables

JustMath allows you to **define and substitute variables** directly in expressions.  
Variables are passed as a `Map<String, String>` when calling `evaluate`.

- Variables can be reused across nested evaluations.
- An **exception** is thrown if an undefined variable is encountered.

### ✅ Example: Using Variables

```java
CalculatorEngine calculator = new CalculatorEngine();

// Define variables
Map<String, String> variables = new HashMap<>();
variables.put("a", "5+3");
variables.put("b", "3");

// Evaluate expression with variables
BigNumber result = calculator.evaluate("2*a + b^2", variables);

System.out.println(result);
// 25

// Call other variables in a variable
variables = new HashMap<>();
variables.put("a", "root(b)");
variables.put("b", "3");

result = calculator.evaluate("2*a + b^2", variables);

System.out.println(result);
// 12.464101615...
```

## 🛡️ Safe Evaluation & Localized Errors

In addition to the throwing `evaluate(...)` entry points, `CalculatorEngine` exposes
**`evaluateSafe(...)`**, which never throws on calculator-level failures. Instead it returns
a sealed **`CalculatorResult<BigNumber>`** that is either `Success` (carries the value) or
`Failure` (carries a `CalculatorError`).

Each failure is described by a **`CalculatorErrorCode`** (for example
`SYNTAX_INVALID_CHARACTER`, `SYNTAX_UNKNOWN_VARIABLE`, `PROCESSING_DIVISION_BY_ZERO`,
`PROCESSING_DOMAIN_ERROR`), so callers can branch on a structural value instead of parsing
English text fragments.

### 🌍 Localized Messages

Error messages can be rendered in two modes via **`ErrorMode`**:

| Mode            | Description                                                                                              |
|-----------------|----------------------------------------------------------------------------------------------------------|
| `RAW`           | Technical English detail including internal context (tokens, positions, stack sizes). This is the default. |
| `USER_FRIENDLY` | Localized, end-user oriented message taken from `i18n/calculator_errors_*.properties` (currently `en`, `de`). |

The active locale is configured on the engine via `setLocale(Locale)`, the error mode via
`setErrorMode(ErrorMode)`. Both setters are fluent and return the engine instance.

### 🔣 Localized Result Formatting

`setLocale(Locale)` not only controls error messages but also drives **locale-aware result
formatting** for all string-returning evaluation methods:

| Method                          | Format                              |
|---------------------------------|-------------------------------------|
| `evaluateToString(...)`         | Locale decimal separator, no grouping |
| `evaluateToPrettyString(...)`   | Locale decimal **and** grouping separators |
| `evaluateSafeToString(...)`     | Same as `evaluateToString`; error path keeps the `Error:` / `Fehler:` prefix |
| `evaluateSafeToPrettyString(...)` | Same as `evaluateToPrettyString`; error path keeps the `Error:` / `Fehler:` prefix |

Input parsing is **not** affected — expressions are always parsed with `.` as the decimal
separator regardless of the engine locale. Default locale is `Locale.ENGLISH`, which preserves
the legacy `.` / `,` formatting of earlier releases.

```java
CalculatorEngine engine = new CalculatorEngine();

engine.setLocale(Locale.US);
engine.evaluateToString("1/2");           // "0.5"
engine.evaluateToPrettyString("1234.56"); // "1,234.56"

engine.setLocale(Locale.GERMANY);
engine.evaluateToString("1/2");           // "0,5"
engine.evaluateToPrettyString("1234.56"); // "1.234,56"
engine.evaluateToString("-1234.56");      // "-1234,56"
engine.evaluateToPrettyString("1234567890.123456"); // "1.234.567.890,123456"

engine.setLocale(Locale.FRANCE);
engine.evaluateToString("1/2");           // "0,5"
```

The same formatting is honored by `BigNumber.toString(Locale)` and
`BigNumber.toPrettyString(Locale)`, so library callers can render values in any locale without
going through the engine.

### 🎯 MultiValueResult Formatting

Functions that return more than one scalar component (e.g. `Pol(...)` → `(r, θ)`,
`Rec(...)` → `(x, y)`) are exposed as `MultiValueResult` implementations such as
`BigNumberCoordinate`. When such a result is rendered via the engine, **both components** are
formatted with the configured locale and both are returned in the output string:

```java
CalculatorEngine engine = new CalculatorEngine(TrigonometricMode.DEG)
        .setLocale(Locale.GERMANY);

engine.evaluateToString("Pol(1;2)");
// r=2,2360...; θ=63,4349...

engine.evaluateToPrettyString("Rec(2;1)");
// x=1,9996...; y=0,0349...
```

When the same coordinate participates in a larger scalar expression, it transparently
collapses to its `firstValue()` (e.g. `r` for polar, `x` for cartesian) before formatting.

### ✅ Example: Result-based Evaluation

```java
CalculatorEngine engine = new CalculatorEngine()
        .setLocale(Locale.GERMAN)
        .setErrorMode(ErrorMode.USER_FRIENDLY);

CalculatorResult<BigNumber> result = engine.evaluateSafe("5/0");

if (result.isFailure()) {
    CalculatorError err = result.error().orElseThrow();
    System.out.println(err.code());
    // PROCESSING_DIVISION_BY_ZERO

    System.out.println(err.format(Locale.GERMAN, ErrorMode.USER_FRIENDLY));
    // Division durch Null ist nicht erlaubt.
} else {
    BigNumber value = result.value().orElseThrow();
    System.out.println(value);
}
```

`CalculatorResult` also supports `map(...)` for chaining and `valueOrThrow()` if you prefer
to fall back to the classical `SyntaxErrorException` / `ProcessingErrorException` contract.

### ⚡ Caching

`BigNumber.valueOf(...)`, the constants exposed on `BigNumbers`, and the `CalculatorEngine`
expression pipeline use internal caches to avoid redundant work for repeated values and
re-evaluations of the same expression — `engine.evaluate("∑(0;100;k)")` invoked twice in a
row reuses the parsed/postfix representation on the second call.

### 🧬 Cloneable BigNumber

`BigNumber` implements `Cloneable` and exposes a safe `clone()` method that returns a value-
equal copy without mutating any shared internal state — convenient when caching or passing
`BigNumber` instances into APIs that mutate their inputs.

```java
BigNumber a = new BigNumber("3.14159");
BigNumber b = a.clone();
// a.equals(b) == true, but a != b
```

## 📚 Static Utility Methods

JustMath provides a suite of **static utility methods** grouped in dedicated classes. These can be used independently of
`BigNumber` or `CalculatorEngine` for direct access to high-precision calculations.

| Class                                | Method(s)                                                                                                                                  | Description                                              |
|--------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------|
| `BasicMath`                          | `add`, `subtract`, `multiply`, `divide`, `modulo`, `power`                                                                                 | Basic arithmetic operations                              |
|                                      | `factorial`, `exp`                                                                                                                         | Factorial and exponential function                       |
| `CombinatoricsMath`                  | `combination`, `permutation`                                                                                                               | Calculate combinations (nCr) and permutations (nPr)      |
| `CoordinateConversionMath`           | `polarToCartesianCoordinates`, `cartesianToPolarCoordinates`                                                                               | Convert between polar and cartesian coordinates          |
| `HyperbolicTrigonometricMath`        | `sinh`, `cosh`, `tanh`, `coth`                                                                                                             | Hyperbolic sine, cosine, tangent, and cotangent          |
| `InverseHyperbolicTrigonometricMath` | `asinh`, `acosh`, `atanh`, `acoth`                                                                                                         | Inverse hyperbolic functions                             |
| `InverseTrigonometricMath`           | `asin`, `acos`, `atan`, `acot`                                                                                                             | Inverse trigonometric functions                          |
| `LogarithmicMath`                    | `log2`, `log10`, `ln`, `logBase`                                                                                                           | Binary, decimal, natural, and arbitrary base logarithms  |
| `MatrixMath`                         | `add`, `subtract`, `multiply`, `divide`, `scalarMultiply`, `transpose`, `determinant`, `inverse`, `power`, `minor`, `identity`, `adjugate` | Matrix operations                                        |
| `NumberTheoryMath`                   | `gcd`, `lcm`                                                                                                                               | Greatest common divisor and least common multiple        |
| `PercentageMath`                     | `nPercentFromM`, `xIsNPercentOfN`                                                                                                          | Percent calculations                                     |
| `RadicalMath`                        | `squareRoot`, `cubicRoot`, `nthRoot`                                                                                                       | Compute square, cube, and n-th roots                     |
| `TrigonometricMath`                  | `sin`, `cos`, `tan`, `coth`                                                                                                                | Trigonometric functions (coth also here for convenience) |
| `TwoDimensionalMath`                 | `atan2`                                                                                                                                    | Two-argument arctangent                                  |
| `MathUtils`                          | `convertAngle`, `bigDecimalRadiansToDegrees`, `bigDecimalNumberToRadians`                                                                  | Angle conversions                                        |
|                                      | `randomIntegerBigNumberInRange`                                                                                                            | Random integer generation using `BigNumber`              |
|                                      | `e`, `pi`                                                                                                                                  | Mathematical constants as `BigNumber`                    |
| `SeriesMath`                         | `summation`                                                                                                                                | Summation logic                                          |
|                                      | `product`                                                                                                                                  | Product logic                                            |
|                                      | `product`                                                                                                                                  | Product logic                                            |
| `SpecialFunctionMath`                | `gamma`, `beta`                                                                                                                            | Gamma and Beta special functions                         | 
| `StatisticsMath`                     | `sum`, `average`, `median`                                                                                                                 | Sum and average of provided elements                     | 
 
## 📐 Constants

The `BigNumbers` class provides reusable **high-precision constants** and default configuration values that are
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
| `FOUR`                       | BigNumber value of 4                            |
| `FIVE`                       | BigNumber value of 5                            |
| `SIX`                        | BigNumber value of 6                            |
| `SEVEN`                      | BigNumber value of 7                            |
| `EIGHT`                      | BigNumber value of 8                            |
| `NINE`                       | BigNumber value of 9                            |
| `TEN`                        | BigNumber value of 10                           |
| `ONE_HUNDRED`                | BigNumber value of 100                          |
| `ONE_HUNDRED_EIGHTY`         | BigNumber value of 180                          |

## 🧭 Algorithms

You can also sort a `List<BigNumber>` using any of the following algorithms.

| Algorithm       |
|-----------------|
| `BubbleSort`    |
| `GnomeSort`     |
| `InsertionSort` |
| `MergeSort`     |
| `QuickSort`     |
| `RadixSort`     |
| `SelectionSort` |
| `TimSort`       |

### ✅ Example: Using Algorithms

```java
List<BigNumber> numbers = Arrays.asList(
        new BigNumber("3.14"),
        new BigNumber("2.71"),
        new BigNumber("1.41"),
        new BigNumber("1.73")
);

numbers = new QuickSort().sort(numbers); // [1.41, 1.73, 2.71, 3.14]
```

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
// -0.5797597677291667131944984780245747754620911770325891258918945726243986428499938555808865049096166498
```

### ∑ Sigma Summation (Custom Expression)

```java
CalculatorEngine calculator = new CalculatorEngine(50, TrigonometricMode.DEG);
BigNumber result = calculator.evaluate("∑(0;5;k^2+1)");
// Equivalent: sum(0;5;k^2+1)

System.out.println(result);
// 61
```

## 📏 Unit Converter (High-Precision)

JustMath includes a **high-precision unit converter** built on top of `BigNumber`.
It is designed to be:

- **Type-safe**: units are represented by enums (identifiers only), not by mutable data objects.
- **Extensible**: adding a new unit is a single, deterministic change in the internal registry.
- **Precise & deterministic**: all conversions use `BigNumber` arithmetic and an explicit `MathContext`.

### ✅ Supported Unit Groups (so far)

- **Length** (base: meter) → `Unit.Length`
- **Mass** (base: kilogram) → `Unit.Mass`
- **Temperature** (base: kelvin) → `Unit.Temperature`
- **Area** (base: square meter) → `Unit.Area`

> Cross-group conversions are **rejected** by design (e.g. length → mass).

### 🧠 Design Overview

The converter module separates **unit identifiers** from **unit metadata**:

- `Unit` (and nested enums like `Unit.Length`, `Unit.Mass`) are **pure identifiers**.
- The internal `UnitRegistry` is the **single source of truth** for:
  - `displayName` (human-readable label)
  - `symbol` (parse/format token, e.g. `"km"`)
  - conversion formula (scale/offset mapping to the base unit)

This keeps the public API stable and makes the unit catalog deterministic and easy to maintain.

### 🔁 Converting Values

Use `UnitConverter` to convert between units **within the same group**.

```java
UnitConverter converter = new UnitConverter();

// 1 km -> m
UnitValue meters = converter.convert(new BigNumber("1"), Unit.Length.KILOMETER, Unit.Length.METER);
System.out.println(meters.toDisplayString()); // 1000 m

// 2.5 lb -> kg
UnitValue kg = converter.convert("2.5", Unit.Mass.POUND, Unit.Mass.KILOGRAM);
System.out.println(kg.toDisplayString()); // 1.13398... kg
```

#### Precision / Rounding

Conversions use a `MathContext` internally (especially for divisions).
You can control this via:

```java
// uses a MathContext derived from the given division precision
UnitConverter converter = new UnitConverter(50);

// or provide your own MathContext
UnitConverter converter2 = new UnitConverter(new MathContext(80, RoundingMode.HALF_UP));
```

### 🧾 Parsing Inputs like `"12.5 km"`

Use `UnitValue` to parse a combined text input into a `(BigNumber + Unit)` pair.

Supported formats:

* Preferred: `"<number> <symbol>"` → `"12.5 km"`
* Also supported: suffix without whitespace → `"12.5km"`

Locale handling:

* `new UnitValue(String)` detects decimal separators using lightweight heuristics
* You can also pass an explicit `Locale`

```java
import com.mlprograms.justmath.converter.UnitConverter;
import com.mlprograms.justmath.converter.UnitValue;

UnitValue value = new UnitValue("12,5 km"); // auto-detects comma decimal (e.g. de_DE)
UnitConverter converter = new UnitConverter();

UnitValue result = converter.convert(value.getValue(), value.getUnit(), Unit.Length.METER);
System.out.println(result.toDisplayString()); // 12500 m
```

### 🔎 Looking up Units by Symbol / Getting Metadata

The public facade `UnitElements` provides:

* strict symbol parsing
* metadata access (display name, symbol)
* listing all built-in units

```java
import com.mlprograms.justmath.converter.Unit;
import com.mlprograms.justmath.converter.UnitElements;

// parse symbol -> unit
Unit km = UnitElements.parseUnit("km");

// metadata
System.out.println(UnitElements.getDisplayName(km)); // "Kilometer"
System.out.println(UnitElements.getSymbol(km));      // "km"

// list all units (deterministic registry order)
for (Unit unit : UnitElements.all()) {
    System.out.println(UnitElements.getSymbol(unit) + " -> " + UnitElements.getDisplayName(unit));
}
```

### 📚 Supported Units (most common)

> The table lists **Symbol → Name → Enum constant**.
> All symbols are **case-sensitive**.

#### 📏 Length (Unit.Length) — base: meter

**Metric / SI & small units**

| Name       | Symbol | Enum                     |
| ---------- | ------ | ------------------------ |
| Exameter   | Em     | `Unit.Length.EXAMETER`   |
| Petameter  | Pm     | `Unit.Length.PETAMETER`  |
| Terameter  | Tm     | `Unit.Length.TERAMETER`  |
| Gigameter  | Gm     | `Unit.Length.GIGAMETER`  |
| Megameter  | Mm     | `Unit.Length.MEGAMETER`  |
| Kilometer  | km     | `Unit.Length.KILOMETER`  |
| Hectometer | hm     | `Unit.Length.HECTOMETER` |
| Dekameter  | dam    | `Unit.Length.DEKAMETER`  |
| Meter      | m      | `Unit.Length.METER`      |
| Decimeter  | dm     | `Unit.Length.DECIMETER`  |
| Centimeter | cm     | `Unit.Length.CENTIMETER` |
| Millimeter | mm     | `Unit.Length.MILLIMETER` |
| Micrometer | um     | `Unit.Length.MICROMETER` |
| Micron     | µm     | `Unit.Length.MICRON`     |
| Nanometer  | nm     | `Unit.Length.NANOMETER`  |
| Angstrom   | Å      | `Unit.Length.ANGSTROM`   |
| Picometer  | pm     | `Unit.Length.PICOMETER`  |
| Femtometer | fm     | `Unit.Length.FEMTOMETER` |
| Attometer  | am     | `Unit.Length.ATTOMETER`  |

**Physics / constants**

| Name            | Symbol | Enum                          |
| --------------- | ------ | ----------------------------- |
| Planck Length   | lP     | `Unit.Length.PLANCK_LENGTH`   |
| Electron Radius | re     | `Unit.Length.ELECTRON_RADIUS` |
| Bohr Radius     | a0     | `Unit.Length.BOHR_RADIUS`     |
| X Unit          | xu     | `Unit.Length.X_UNIT`          |
| Fermi           | frm    | `Unit.Length.FERMI`           |

**Astronomy**

| Name                    | Symbol     | Enum                                  |
| ----------------------- | ---------- | ------------------------------------- |
| Sun Radius              | Rsun       | `Unit.Length.SUN_RADIUS`              |
| Earth Equatorial Radius | R_earth_eq | `Unit.Length.EARTH_EQUATORIAL_RADIUS` |
| Earth Polar Radius      | R_earth_p  | `Unit.Length.EARTH_POLAR_RADIUS`      |
| Astronomical Unit       | au         | `Unit.Length.ASTRONOMICAL_UNIT`       |
| Earth Distance from Sun | AU         | `Unit.Length.EARTH_DISTANCE_FROM_SUN` |
| Parsec                  | pc         | `Unit.Length.PARSEC`                  |
| Kiloparsec              | kpc        | `Unit.Length.KILOPARSEC`              |
| Megaparsec              | Mpc        | `Unit.Length.MEGAPARSEC`              |
| Light Year              | ly         | `Unit.Length.LIGHT_YEAR`              |

**Nautical / maritime**

| Name                 | Symbol   | Enum                                        |
| -------------------- | -------- | ------------------------------------------- |
| League               | lea      | `Unit.Length.LEAGUE`                        |
| Nautical League      | NL       | `Unit.Length.NAUTICAL_LEAGUE_INTERNATIONAL` |
| Nautical League (UK) | NL (UK)  | `Unit.Length.NAUTICAL_LEAGUE_UK`            |
| Nautical Mile        | nmi      | `Unit.Length.NAUTICAL_MILE`                 |
| Nautical Mile (UK)   | nmi (UK) | `Unit.Length.NAUTICAL_MILE_UK`              |

**Imperial / historical / misc.**

| Name           | Symbol     | Enum                        |
| -------------- | ---------- | --------------------------- |
| Mile           | mi         | `Unit.Length.MILE`          |
| Roman Mile     | m.p.       | `Unit.Length.MILE_ROMAN`    |
| Kiloyard       | kyd        | `Unit.Length.KILOYARD`      |
| Furlong        | fur        | `Unit.Length.FURLONG`       |
| Chain          | ch         | `Unit.Length.CHAIN`         |
| Rope           | rope       | `Unit.Length.ROPE`          |
| Rod            | rod        | `Unit.Length.ROD`           |
| Fathom         | ftm        | `Unit.Length.FATHOM`        |
| Famn           | famn       | `Unit.Length.FAMN`          |
| Ell            | ell        | `Unit.Length.ELL`           |
| Aln            | aln        | `Unit.Length.ALN`           |
| Cubit (UK)     | cubit      | `Unit.Length.CUBIT_UK`      |
| Span (cloth)   | span       | `Unit.Length.SPAN_CLOTH`    |
| Link           | li         | `Unit.Length.LINK`          |
| Finger (cloth) | finger     | `Unit.Length.FINGER_CLOTH`  |
| Hand           | hand       | `Unit.Length.HAND`          |
| Handbreadth    | hb         | `Unit.Length.HANDBREADTH`   |
| Nail (cloth)   | nail       | `Unit.Length.NAIL_COTH`     |
| Fingerbreadth  | fb         | `Unit.Length.FINGERBREADTH` |
| Barleycorn     | barleycorn | `Unit.Length.BARLEYCORN`    |
| Yard           | yd         | `Unit.Length.YARD`          |
| Foot           | ft         | `Unit.Length.FEET`          |
| Inch           | in         | `Unit.Length.INCH`          |
| Centiinch      | cin        | `Unit.Length.CENTIINCH`     |
| Caliber        | cl         | `Unit.Length.CALIBER`       |
| Mil            | mil        | `Unit.Length.MIL`           |
| Microinch      | µin        | `Unit.Length.MICROINCH`     |
| Arpent         | arp        | `Unit.Length.ARPENT`        |
| Ken            | ken        | `Unit.Length.KEN`           |

**Typography / CSS**

| Name  | Symbol | Enum                |
| ----- | ------ | ------------------- |
| Pixel | px     | `Unit.Length.PIXEL` |
| Point | pt     | `Unit.Length.POINT` |
| Pica  | pica   | `Unit.Length.PICA`  |
| Em    | em     | `Unit.Length.EM`    |
| Twip  | twip   | `Unit.Length.TWIP`  |

#### ⚖️ Mass (Unit.Mass) — base: kilogram

| Name             | Symbol | Enum                         |
| ---------------- | ------ | ---------------------------- |
| Tonne            | t      | `Unit.Mass.TON`              |
| Kilogram         | kg     | `Unit.Mass.KILOGRAM`         |
| Gram             | g      | `Unit.Mass.GRAM`             |
| Milligram        | mg     | `Unit.Mass.MILLIGRAM`        |
| Long Ton         | lt     | `Unit.Mass.LONG_TON`         |
| Short Ton        | st     | `Unit.Mass.SHORT_TON`        |
| Pound            | lb     | `Unit.Mass.POUND`            |
| Ounce            | oz     | `Unit.Mass.OUNCE`            |
| Carat            | ct     | `Unit.Mass.CARRAT`           |
| Atomic Mass Unit | u      | `Unit.Mass.ATOMIC_MASS_UNIT` |

#### 🌡️ Temperature (Unit.Temperature) — base: kelvin

| Name       | Symbol | Enum                          |
| ---------- | ------ | ----------------------------- |
| Kelvin     | K      | `Unit.Temperature.KELVIN`     |
| Celsius    | °C     | `Unit.Temperature.CELSIUS`    |
| Fahrenheit | °F     | `Unit.Temperature.FAHRENHEIT` |

#### 🧱 Area (Unit.Area) — base: square meter

| Name                  | Symbol   | Enum                               |
| --------------------- | -------- | ---------------------------------- |
| Square Kilometer      | km^2     | `Unit.Area.SQUARE_KILOMETER`       |
| Square Hectometer     | hm^2     | `Unit.Area.SQUARE_HECTOMETER`      |
| Square Dekameter      | dam^2    | `Unit.Area.SQUARE_DEKAMETER`       |
| Square Meter          | m^2      | `Unit.Area.SQUARE_METER`           |
| Square Decimeter      | dm^2     | `Unit.Area.SQUARE_DECIMETER`       |
| Square Centimeter     | cm^2     | `Unit.Area.SQUARE_CENTIMETER`      |
| Square Millimeter     | mm^2     | `Unit.Area.SQUARE_MILLIMETER`      |
| Square Micrometer     | µm^2     | `Unit.Area.SQUARE_MICROMETER`      |
| Square Nanometer      | nm^2     | `Unit.Area.SQUARE_NANOMETER`       |
| Hectare               | ha       | `Unit.Area.HECTARE`                |
| Are                   | a        | `Unit.Area.ARE`                    |
| Barn                  | b        | `Unit.Area.BARN`                   |
| Thomson Cross Section | σT       | `Unit.Area.ELECTRON_CROSS_SECTION` |
| Township              | twp      | `Unit.Area.TOWNSHIP`               |
| Section               | sec      | `Unit.Area.SECTION`                |
| Homestead             | hstd     | `Unit.Area.HOMESTEAD`              |
| Square Mile           | mi^2     | `Unit.Area.SQUARE_MILE`            |
| Acre                  | ac       | `Unit.Area.ACRE`                   |
| Rood                  | rood     | `Unit.Area.ROOD`                   |
| Square Chain          | ch^2     | `Unit.Area.SQUARE_CHAIN`           |
| Square Rod            | rd^2     | `Unit.Area.SQUARE_ROD`             |
| Square Pole           | pole^2   | `Unit.Area.SQUARE_POLE`            |
| Square Rope           | rope^2   | `Unit.Area.SQUARE_ROPE`            |
| Square Yard           | yd^2     | `Unit.Area.SQUARE_YARD`            |
| Square Foot           | ft^2     | `Unit.Area.SQUARE_FOOT`            |
| Square Inch           | in^2     | `Unit.Area.SQUARE_INCH`            |
| Arpent                | arp_area | `Unit.Area.ARPENT`                 |
| Cuerda                | cda      | `Unit.Area.CUERDA`                 |
| Plaza                 | plz      | `Unit.Area.PLAZA`                  |

### 🚫 Error Handling

The converter module uses conversion-specific runtime exceptions:

* `ConversionException`
  Thrown for invalid numeric input / parse issues.

* `UnitConversionException`
  Thrown when:

  * symbols are unknown or missing
  * units are incompatible (cross-group conversion)
  * the input format is malformed

Example:

```java
try {
    UnitValue v = new UnitValue("abc km");
} catch (ConversionException ex) {
    // invalid number
}
```

### ➕ Adding a New Unit (Internal Registry)

To add a new built-in unit, you typically do **only two things**:

1. Add the enum constant to the correct group (e.g. `Unit.Length`)
2. Add exactly one `define(...)` entry to the internal `UnitRegistry.BUILT_IN`

Conversion mapping is defined using an affine formula into the base unit:

```text
base = value * scaleToBase + offsetToBase
```

For purely linear conversions (most units), `offsetToBase = "0"`.

Example (linear):

```java
define(Unit.Length.MEGAMETER, "Megameter", "Mm", "1000000")
```

The registry validates at startup:

* every unit is defined exactly once
* every symbol is unique
* groups are consistent and deterministic

## ⚙️ Maven (Coming Soon)

Cannot wait? Just download the latest jar:

<table style="width:100%">
  <tr>
    <th>Version</th>
    <th>Download</th>
    <th>Release Type</th>
  </tr>
  <tr>
      <td>v1.4.3</td>
      <td><a href="out/artifacts/justmath_jar/justmath-1.4.3.jar">JustMath v1.4.3</a></td>
      <td>Release</td>
  </tr>
  <tr>
      <td>v1.4.2</td>
      <td><a href="out/artifacts/justmath_jar/justmath-1.4.2.jar">JustMath v1.4.2</a></td>
      <td>Release</td>
  </tr>
  <tr>
      <td>v1.4.1</td>
      <td><a href="out/artifacts/justmath_jar/justmath-1.4.1.jar">JustMath v1.4.1</a></td>
      <td>Release</td>
  </tr>
  <tr>
    <td>v1.4.0</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.4.0.jar">JustMath v1.4.0</a></td>
    <td>Release</td>
  </tr>
  <tr>
    <td>v1.3.0</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.3.0.jar">JustMath v1.3.0</a></td>
    <td>Release</td>
  </tr>
  <tr>
    <td>v1.2.5</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.2.5.jar">JustMath v1.2.5</a></td>
    <td>Preview</td>
  </tr>
  <tr>
    <td>v1.2.2</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.2.2.jar">JustMath v1.2.2</a></td>
    <td>Release</td>
  </tr>
  <tr>
    <td>v1.2.1</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.2.1.jar">JustMath v1.2.1</a></td>
    <td>Preview</td>
  </tr>
 <tr>
    <td>v1.2.0</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.2.0.jar">JustMath v1.2.0</a></td>
    <td>Release</td>
  </tr>
 <tr>
    <td>v1.1.5</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.1.5.jar">JustMath v1.1.5</a></td>
    <td>Release</td>
  </tr>
 <tr>
    <td>v1.1.4</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.1.4.jar">JustMath v1.1.4</a></td>
    <td>Release</td>
  </tr>
  <tr>
    <td>v1.0.3</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.0.3.jar">JustMath v1.0.3</a></td>
    <td>Release</td>
  </tr>
  <tr>
    <td>v1.0.2</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.0.2.jar">JustMath v1.0.2</a></td>
    <td>Release</td>
  </tr>
  <tr>
    <td>v1.0.1</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.0.1.jar">JustMath v1.0.1</a></td>
    <td>Release</td>
  </tr>
  <tr>
    <td>v1.0.0</td>
    <td><a href="out/artifacts/justmath_jar/justmath-1.0.0.jar">JustMath v1.0.0</a></td>
    <td>Release</td>
  </tr>
</table>

Need something newer than the latest release? You can find the newest (possibly unstable) builds on the <a href="https://github.com/lembergmax/JustMath/tree/developer">developer</a> branch.

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

© 2024-2026 Max Lemberg. All rights reserved.
Licensed under the MIT License.
