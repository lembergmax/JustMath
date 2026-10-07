# JustMath

[![Maven Central](https://img.shields.io/maven-central/v/io.github.lembergmax/justmath)](https://central.sonatype.com/artifact/io.github.lembergmax/justmath)
[![CI](https://github.com/lembergmax/JustMath/actions/workflows/ci.yml/badge.svg?branch=developer)](https://github.com/lembergmax/JustMath/actions/workflows/ci.yml)
[![CodeQL](https://github.com/lembergmax/JustMath/actions/workflows/codeql.yml/badge.svg?branch=developer)](https://github.com/lembergmax/JustMath/actions/workflows/codeql.yml)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/lembergmax/JustMath/badge)](https://scorecard.dev/viewer/?uri=github.com/lembergmax/JustMath)
[![Javadoc](https://javadoc.io/badge2/io.github.lembergmax/justmath/javadoc.svg)](https://javadoc.io/doc/io.github.lembergmax/justmath)
[![License: MIT](https://img.shields.io/github/license/lembergmax/JustMath)](LICENSE)
![Java 21+](https://img.shields.io/badge/Java-21%2B-blue)

JustMath is a Java library for calculations with as many digits as you ask for. It has a number type (`BigNumber`), an engine that evaluates text such as `2*sin(30)+sqrt(2)`, lists and matrices of such numbers, and a converter for 601 units. The only runtime dependency is [big-math](https://github.com/eobermuhlner/big-math).

- Addition, subtraction, multiplication and integer powers are exact. Every other result is rounded once, with the precision and the rounding mode that you pass, and is correct to within one unit in the last place.
- The expression engine reports a failure with a typed error code that you can branch on, and with a message in 13 languages (20 locales).
- A small input that asks for a huge result, such as `1000000!` or `9^9999999999`, is rejected with an error instead of being computed.
- The results are tested against `BigDecimal`, `BigInteger` and `BigDecimalMath` on generated hard arguments in every build. See [docs/testing.md](docs/testing.md).

## Install

Java 21 or newer.

```xml
<dependency>
    <groupId>io.github.lembergmax</groupId>
    <artifactId>justmath</artifactId>
    <version>1.7.0</version>
</dependency>
```

```groovy
implementation 'io.github.lembergmax:justmath:1.7.0'
```

The jars are on [Maven Central](https://central.sonatype.com/artifact/io.github.lembergmax/justmath) and, with checksums, a software bill of materials and a build provenance attestation, on the [GitHub releases](https://github.com/lembergmax/JustMath/releases). [SECURITY.md](SECURITY.md#verifying-a-release) shows how to verify a release. The newest, possibly unstable code is on the [developer](https://github.com/lembergmax/JustMath/tree/developer) branch.

## Quick start

```java
BigNumber a = new BigNumber("0.1");
BigNumber b = new BigNumber("0.2");
System.out.println(a.add(b));                       // 0.3, not 0.30000000000000004

BigNumber third = new BigNumber("1").divide(new BigNumber("3"), new MathContext(50, RoundingMode.HALF_UP));
System.out.println(third);                          // 0.33333333333333333333333333333333333333333333333333

CalculatorEngine engine = new CalculatorEngine();   // angles in degrees
System.out.println(engine.evaluate("sqrt(16)*2^10 + sin(30)")); // 4096.5

UnitConverter converter = new UnitConverter();
System.out.println(converter.convert("212", Unit.Temperature.FAHRENHEIT, Unit.Temperature.CELSIUS).toDisplayString()); // 100 °C
```

## BigNumber

`BigNumber` holds a decimal value of any size, a `Locale` for formatting, a `MathContext` and a `TrigonometricMode` (degrees or radians, degrees by default). Every operation has an overload with an explicit `MathContext` and one with a `Locale`.

| Area | Methods |
| --- | --- |
| Arithmetic | `add`, `subtract`, `multiply`, `divide`, `modulo`, `remainder`, `power`, `abs`, `negate` |
| Rounding | `round`, `roundAfterDecimals`, `floor`, `ceil`, `truncate` |
| Roots, exponential | `squareRoot`, `cubicRoot`, `nthRoot`, `exp`, `factorial` |
| Logarithms | `log2`, `log10`, `ln`, `logBase` |
| Trigonometry | `sin`, `cos`, `tan`, `cot`, `asin`, `acos`, `atan`, `acot`, `atan2` |
| Hyperbolic | `sinh`, `cosh`, `tanh`, `coth`, `asinh`, `acosh`, `atanh`, `acoth` |
| Number theory and counting | `gcd`, `lcm`, `combination`, `permutation` |
| Series, special functions | `summation`, `product`, `gamma`, `beta` |
| Coordinates | `polarToCartesianCoordinates`, `cartesianToPolarCoordinates` |
| Percentages | `isXPercentOfN` |

How precise a result is:

- A division, a negative power, `exp`, a fractional power, a root, a logarithm and a trigonometric or hyperbolic function return a result that is correct to within one unit in the last place of your `MathContext`, rounded once with its rounding mode. A function that loses digits near one of its zeros (`sin` near a multiple of π, `ln` near 1, `sinh` near 0) is evaluated with as many guard digits as it needs. Where the exact result is zero, such as `sin(180°)`, it is zero.
- `BigNumberList` and `BigNumberMatrix` use the default context of 100 digits and `HALF_UP` for their divisions and their means.
- Speed is not the strength of `BigNumber`: its arithmetic is 90 to 2400 times slower than `BigDecimal`. If you only need exact decimal arithmetic in a hot loop, use `BigDecimal`. See [docs/performance.md](docs/performance.md).

Mutation:

- An operation returns a new instance and leaves the receiver alone. A method that changes the receiver says so: `negateThis()`, `trim()` and the setters. A shared constant such as `BigNumbers.ZERO` is never returned to you, so mutating a result is safe. See [decision 0001](docs/decisions/0001-mixed-mutability-of-bignumber.md).
- `equals` and `hashCode` compare the numeric value: `1.0` equals `1.00`.

The locale only changes how a number is read and written (`toString(Locale)`, `toPrettyString(Locale)`). It never changes a value.

## Expressions

`CalculatorEngine.evaluate(String)` evaluates an expression with the precision and the angle mode of the engine. The argument separator is `;` and the decimal separator is `.`.

| Group | Syntax |
| --- | --- |
| Operators | `+` `-` `*` `×` `/` `÷` `%` `^` `!`, `nPr` and `nCr` (`10 nCr 3`) |
| Constants | `pi` `π` `e` |
| Roots | `sqrt(x)` `√(x)`, `cbrt(x)` `³√(x)`, `rootn(x;n)` |
| Logarithms | `log2(x)`, `log10(x)`, `ln(x)`, `logbase(x;b)` |
| Trigonometry | `sin(x)` `cos(x)` `tan(x)` `cot(x)`, `asin(x)` `acos(x)` `atan(x)` `acot(x)`, `atan2(y;x)`; the inverse functions also as `sin⁻¹(x)` and so on |
| Hyperbolic | `sinh(x)` `cosh(x)` `tanh(x)` `coth(x)`, `asinh(x)` `acosh(x)` `atanh(x)` `acoth(x)`; also `sinh⁻¹(x)` and so on |
| Series | `summation(start;end;expression)` `∑(...)`, `product(start;end;expression)` `∏(...)`; the index is `k` |
| Statistics | `sum(a;b;...)`, `avg(a;b;...)` `average(a;b;...)`, `median(a;b;...)` |
| Counting | `comb(n;r)`, `perm(n;r)` |
| Number theory | `gcd(a;b)` `GCD(a;b)`, `lcm(a;b)` `LCM(a;b)`, `RandInt(min;max)` |
| Special functions | `gamma(x)` `Γ(x)`, `beta(x;y)` `B(x;y)`, `abs(x)` `\|x\|` |
| Coordinates | `Pol(x;y)` returns `r` and `θ`, `Rec(r;θ)` returns `x` and `y` |

Variables are passed as a map. A variable may refer to another one. A reference cycle is an error.

```java
Map<String, String> variables = new HashMap<>();
variables.put("a", "sqrt(b)");
variables.put("b", "3");

System.out.println(engine.evaluate("2*a + b^2", variables)); // 12.4641016151377545870548926830117447...
```

Syntax rules that surprise people:

- `1,5+1,5` is an error: `,` is not a decimal separator. `setInputLocale(Locale.GERMANY)` opts into comma-decimal input. See [decision 0003](docs/decisions/0003-input-locale-is-opt-in.md).
- Whitespace separates tokens: `3 4` is an error, not `34`.
- There is no implicit multiplication after `!`: write `5!*sqrt(4)`.
- A sign before a group, function, constant or variable works: `-(3+4)`, `-sin(0)`, `2*-(1+1)`.
- A variable name has ASCII letters only.
- The structure of the expression is checked before anything is computed, so `50000!/` fails at once.

## Errors and results

`CalculatorEngine` offers four families of methods. Choose by what you want to happen on a failure.

| Family | Methods | On failure |
| --- | --- | --- |
| Core | `evaluate(...)` returns `BigNumber` | throws a `CalculatorException` |
| Text | `evaluateToString(...)`, `evaluateToPrettyString(...)` | the error text is the return value |
| Safe text | `evaluateSafeToString(...)`, `evaluateSafeToPrettyString(...)` | never throws, never returns `null`, `"Error: ..."` prefix |
| Typed result | `evaluateSafe(...)`, `evaluateToStringResult(...)`, `evaluateToPrettyStringResult(...)` | a `CalculatorResult` with a `CalculatorError` |

```java
CalculatorEngine german = new CalculatorEngine()
        .setLocale(Locale.GERMAN)
        .setErrorMode(ErrorMode.USER_FRIENDLY);

CalculatorResult<BigNumber> result = german.evaluateSafe("5/0");
if (result.isFailure()) {
    CalculatorError error = result.error().orElseThrow();
    System.out.println(error.code());                                           // PROCESSING_DIVISION_BY_ZERO
    System.out.println(error.format(Locale.GERMAN, ErrorMode.USER_FRIENDLY));  // Division durch Null ist nicht erlaubt.
}
```

Branch on the `CalculatorErrorCode`, not on the text. There are 25 codes:

| Category | Codes |
| --- | --- |
| Syntax | `SYNTAX_INVALID_CHARACTER`, `SYNTAX_MISSING_RIGHT_PAREN`, `SYNTAX_UNMATCHED_PAREN`, `SYNTAX_UNKNOWN_FUNCTION`, `SYNTAX_UNKNOWN_VARIABLE`, `SYNTAX_INCOMPLETE_EXPRESSION`, `SYNTAX_TRAILING_OPERATOR`, `SYNTAX_LEADING_OPERATOR`, `SYNTAX_MISSING_OPERAND`, `SYNTAX_MISSING_OPERATOR`, `SYNTAX_EMPTY_PARENTHESES`, `SYNTAX_EMPTY_FUNCTION_ARGUMENT`, `SYNTAX_UNEXPECTED_END`, `SYNTAX_MISPLACED_SEPARATOR`, `SYNTAX_INVALID_FACTORIAL` |
| Argument | `SYNTAX_WRONG_ARGUMENT_COUNT`, `ARGUMENT_COUNT_MISMATCH` |
| Math | `PROCESSING_DIVISION_BY_ZERO`, `PROCESSING_DOMAIN_ERROR`, `MATH_FACTORIAL_NEGATIVE`, `MATH_FACTORIAL_NON_INTEGER`, `MATH_LOG_NON_POSITIVE`, `MATH_ROOT_OF_NEGATIVE` |
| Range | `MATH_OVERFLOW` |
| Internal | `PROCESSING_INTERNAL` |

`ErrorMode.RAW` (the default) returns a technical English message. `ErrorMode.USER_FRIENDLY` returns a localized one with three tiers: the text of the code, then the text of its category (`Syntax Error`, `Math Error`, ...), then a generic text. `CalculatorEngine.getSupportedLanguages()` lists the 20 locales of 13 languages.

`setLocale(Locale)` sets the language of the errors and the separators of the text output: `evaluateToString("1/2")` gives `0,5` for `Locale.GERMANY`, and `evaluateToPrettyString("1234567890.123456")` gives `1.234.567.890,123456`. It does not change how input is read.

## Lists and matrices

`BigNumberList` implements `List<BigNumber>`. It adds statistics (`sum`, `average`, `median`, `modes`, `min`, `max`, `range`, `variance`, `standardDeviation`, `geometricMean`, `harmonicMean`), transformations (`absAll`, `negateAll`, `scale`, `translate`, `powEach`, `clampAll`, `normalizeToSum`, `map`), sorting with eight algorithms and queries. The `...All` methods and `sortAscending`, `sortDescending` and `reverse` change the list in place; `copy()` and `clone()` return independent copies.

```java
BigNumberList numbers = BigNumberList.of(new BigNumber("3"), new BigNumber("1"), new BigNumber("2"), new BigNumber("2"));
numbers.sortAscending();                      // [1, 2, 2, 3]
numbers.sum();                                // 8
numbers.median();                             // 2
numbers.sort(QuickSort.class);                // a specific algorithm
```

`BigNumberMatrix` is created from dimensions, a string or nested lists. It supports `add`, `subtract`, `multiply`, `scalarMultiply`, `transpose`, `determinant` (exact for integers), `inverse`, `power`, `trace` and property checks such as `isSquare` and `isIdentityMatrix`.

```java
BigNumberMatrix a = new BigNumberMatrix("1,2;3,4", Locale.US);
a.determinant();                              // -2
a.multiply(new BigNumberMatrix("5,6;7,8", Locale.US)).toPlainDataString();   // [[19, 22], [43, 50]]
```

## Units

`UnitConverter` converts between 601 units in 13 groups: length, area, volume, mass, temperature, pressure, energy, power, time, force, speed, fuel consumption and data storage. A conversion goes through the base unit of the group. Units of different groups are rejected with a `UnitConversionException`.

```java
UnitConverter converter = new UnitConverter(50);                // 50 digits
converter.convert(new BigNumber("1"), Unit.Length.KILOMETER, Unit.Length.METER).toDisplayString();   // 1000 m
converter.convert("2.5", Unit.Mass.POUND, Unit.Mass.KILOGRAM).toDisplayString();                     // 1.133980925 kg
new UnitValue("12.5 km");                                       // number and unit from text
UnitElements.parseUnit("km");                                   // Unit.Length.KILOMETER
```

Every value was checked against its definition (SI, the international yard and pound, CODATA 2022, IAU, WGS 84). The list of the units with their constants, symbols, values and the basis of each value is in [docs/units.md](docs/units.md), and the method and the sources in [docs/unit-audit.md](docs/unit-audit.md). Symbols are case-sensitive.

## Limits and security

An input that is small but asks for an enormous result is rejected with a typed error: expressions longer than 100000 characters, a factorial above 100000, an integer power with more than a million digits, `exp` of a huge argument, a series of more than a million terms. The limits are listed in [decision 0005](docs/decisions/0005-limits-on-expensive-inputs.md). A way around a limit is a vulnerability: report it as described in [SECURITY.md](SECURITY.md).

## Thread safety

`BigNumber`, the lists and the matrices are not synchronized. `CalculatorEngine` keeps mutable settings (`setLocale`, `setInputLocale`, `setErrorMode`, the expression cache). The settings are visible to other threads, but changing them while another thread evaluates gives that evaluation a mix of old and new settings. Use one engine per thread, or configure the engine before you share it. The static math classes and `UnitConverter` keep no state.

## Documentation

| Page | Content |
| --- | --- |
| [Changelog](CHANGELOG.md) | What changed in each release, with the breaking changes marked |
| [Architecture](docs/architecture.md) | Packages, the expression pipeline, the error model, precision, limits |
| [Decisions](docs/decisions/README.md) | Why the library is built the way it is |
| [Units](docs/units.md) and [unit audit](docs/unit-audit.md) | All 601 units and where their values come from |
| [Testing](docs/testing.md) | The kinds of tests and the accuracy contract |
| [Mutation testing](docs/mutation-testing.md) | What the mutation score says, how to run PIT and what the threshold is |
| [Performance](docs/performance.md) | Benchmarks of `BigNumber` against `BigDecimal`, and what they mean |
| [Extending](docs/extending.md) | How to add a function, an operator, a unit or a sorting algorithm |
| [Releasing](docs/releasing.md) | How a release is built, approved and published |
| [Javadoc](https://javadoc.io/doc/io.github.lembergmax/justmath) | The API |

## Build from source

```bash
./mvnw verify
```

`verify` runs the tests, the coverage gate, SpotBugs with FindSecBugs, the Javadoc build, the formatting check and an API compatibility check against the previous release. [CONTRIBUTING.md](CONTRIBUTING.md) describes the workflow and the rules for a pull request. Questions go to [SUPPORT.md](SUPPORT.md).

## License

JustMath is released under the [MIT License](LICENSE). Copyright (c) 2025-2026 Max Lemberg.
