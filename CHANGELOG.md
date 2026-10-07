# Changelog

All notable changes to JustMath are written down here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows [Semantic Versioning](https://semver.org/). A change that can break a caller is marked **Breaking**. The release workflow takes the GitHub release notes from the section of the version, so each release needs one.

Versions before 1.5.0 were not documented when they were released. Their entries were rebuilt from the Git history and are short. Versions 1.0.0 to 1.4.4 were direct downloads, 1.5.0 and 1.6.0 are on Maven Central.

## [1.7.0] - 2026-10-07

1.7.0 corrects results that were wrong in the last digits, makes the arithmetic and the functions agree with `BigDecimal` and `BigDecimalMath` on generated hard arguments, reduces the public API, and adds the build, test and release checks of a library that other projects depend on. Some results change: read the lists of the corrected constants and functions before you upgrade.

### Breaking

- The expression pipeline is no longer public. `Tokenizer`, `PostfixParser`, `CalculatorEngineUtils` and `calculator.internal.Token` are package-private, and `CalculatorEngine` no longer has `getEvaluator()`, `getPostfixParser()`, `getTokenizer()`, `getExpressionCache()` and `getInputDecimalSeparator()`. Only `CalculatorEngine` and `SupportedLanguages` are public in `calculator`. `CalculatorEngineUtils.getDefaultMathContext(int)` moved to `BigNumbers.getDefaultMathContext(int)`. (#168)
- `LocalesConfig.SUPPORTED_LOCALES` is an unmodifiable `List<Locale>` and not an array. (#165)
- `BigNumber.round(BigNumber, MathContext)` is static and formats the result with the locale of the rounded number.
- The utility classes `BasicMath`, `CoordinateConversionMath`, `HyperbolicTrigonometricMath`, `InverseHyperbolicTrigonometricMath` and `NumberTheoryMath` are final and have a private constructor.
- `BigNumberList.clone()` returns an independent copy, like `copy()`. It was a view on the same storage. Build `new BigNumberList(list.getValues())` if you want a shared view.
- `BigNumberList.sum`, `average`, `median`, `min` and `max` return new instances and no longer the stored element, so changing a result does not change the list. (#164)
- `BigNumber.toDegrees(MathContext)` and `toRadians(MathContext)` throw an `IllegalArgumentException` for a precision of zero, such as `MathContext.UNLIMITED`. They silently cut the result before.
- 66 unit constants have other values, among them the `Btu (th)`, the dalton, the ken, the masses of the Earth and the Sun, the radius of the Sun and of the Earth and the Planck units. Every old and new value is listed in [docs/unit-audit.md](docs/unit-audit.md). (#161, #162)
- Five expressions report `PROCESSING_DOMAIN_ERROR` and not `PROCESSING_INTERNAL`: `comb(2.5;1)`, `perm(2.5;1)`, `gcd(1.5;2)`, `lcm(1.5;2)` and `summation(1;3;5)`. A non-integer argument is bad input. (#167)
- The functions below give other digits than before because the old digits were wrong. In particular `sin`, `cos`, `tan` and `cot` in degrees return exactly `0` at the multiples of 90° where they were noise of the order 1E-50, and `lcm` is no longer rounded to 100 digits.

### Added

- Typed math exceptions: `MathArithmeticException` (extends `ArithmeticException`), `MathArgumentException` (extends `IllegalArgumentException`) and `ErrorCodeProvider`. The engine classifies by code and no longer by message. (#167)
- Correctly named unit constants `CARAT` and `NAIL_CLOTH`. The old constants `CARRAT` and `NAIL_COTH` and the binary data units that carried a decimal name (`KILOBIT`, `KILOBYTE`, `MEGABIT`, `MEGABYTE`, `GIGABIT`, `GIGABYTE`, `TERABIT`, `TERABYTE`, `PETABIT`, `PETABYTE`, `EXABIT`, `EXABYTE`) are deprecated aliases of `CARAT`, `NAIL_CLOTH` and `KIBIBIT` to `EXBIBYTE`. They convert to the same result and do not appear in `UnitElements.all()`. The decimal units are `KILOBYTE_DECIMAL` and its relatives. (#163)
- `BoundedCache`, and the cached values of pi and e in `BigNumbers` are bounded to 32 precisions. (#169)
- `MathUtils.computeWithGuardDigits`, which evaluates a function with as many guard digits as its result needs and rounds once.
- `docs/units.md`, generated from the registry and guarded by a test, `docs/unit-audit.md` and the audit files under `src/test/resources/unit-audit`. (#162, #187)
- JMH benchmarks under `benchmarks/` and `docs/performance.md`. They showed that the arithmetic of `BigNumber` was 90 to 2400 times slower than `BigDecimal`, which is fixed, see Changed. (#179, #220)
- `docs/architecture.md`, decision records, a guide for adding a function, an operator, a unit or a sorting algorithm, and `docs/testing.md`. (#188)
- `CHANGELOG.md`, `SECURITY.md`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SUPPORT.md`, `CODEOWNERS`, issue forms and a pull request template. (#185, #186)
- Releases carry an SBOM (CycloneDX), SHA-256 checksums and a build provenance attestation, and are published after the maintainer approves them. (#182, #183)

### Changed

- The arithmetic is 9 to 2300 times faster, depending on the operation and the number of digits, and for numbers of 100 digits and more it takes 1.0 to 4.3 times as long as `BigDecimal`. Add, subtract, multiply, divide, remainder, modulo, integer powers and the factorial run on `BigDecimal` and `BigInteger`, and a `BigNumber` holds its value as a `BigDecimal` and writes its digits down only when they are asked for. The results are the same digit for digit, scale for scale and exception for exception. `toBigDecimal()` keeps its value and returns it again, and `getValueBeforeDecimalPoint()`, `getValueAfterDecimalPoint()` and `isNegative()` write the digits down on their first use. See [decision 0008](docs/decisions/0008-bigdecimal-arithmetic-and-lazy-digits.md) and [docs/performance.md](docs/performance.md). (#220)
- `BigNumberList.isMonotonicIncreasing()` and `isMonotonicDecreasing()` return what `isSortedAscending()` and `isSortedDescending()` return. (#223)
- Division, negative integer powers, `exp` and fractional powers round once, with the rounding mode of the caller, and keep every digit of a result that has more integer digits than the precision, and every digit of a quotient smaller than `10^-(precision+2)`. `100!/3` was wrong by `10^58`, `2^-1000` and `1/10^200` were `0`. (#202, #203, #204, #205)
- `sin`, `cos`, `tan`, `cot`, `asin`, `acos`, `atan2`, `ln`, `log2`, `log10`, `sinh` and `tanh` keep every requested digit when the result is close to zero. `sin(3.14159265358979323846264338327950288)` with 20 digits returned `4.6E-26` and not `4.2E-36`. (#207)
- `acosh` and the other inverse hyperbolic functions are computed with `BigDecimalMath` behind the same guard digits. `acosh(1.00068864)` with 60 digits took longer than ten seconds. (#208)
- `atan`, `acot` and `atan2` keep the distance from π/2. `acot(-1E-18)` with 20 digits was off by ten units in the last place. (#215)
- `lcm` is the exact integer for any number of digits. It was rounded to the 100 digits of the default precision. (#214)
- `exp` and `power` refuse results with more than a million digits with `MATH_OVERFLOW`, and combinations and permutations are computed with `BigInteger`.
- `atan` and `acot` use `BigDecimalMath` with guard digits and round once. `acot(x)` follows the `atan(1/x)` branch, with the range `(-π/2, π/2)` without `0`.
- `°F` converts with the exact ratios 5/9 and -160/9, so `32 °F` is exactly `0 °C`. A conversion from unit to unit rounds once.
- `asin` rounds to significant digits like `acos` and `atan`, not to decimal places.
- `BigNumberMatrix` has `equals` and `hashCode` by value, exact integer determinants (Bareiss) and `MathContext` overloads.
- `BigNumberCoordinate` has `equals`, `hashCode` and ordering by value and is immutable.
- `BigNumber.roundAfterDecimals` formats the result in the locale of the receiver.
- `CalculatorError` copies its parameter map, replaces placeholders in a single pass and a precise error is reported for `;` in a bare parenthesis.
- `CalculatorEngine`: the configuration fields are `volatile`, the expression cache has its own lock, and `setInputLocale` is synchronized. (#166)
- The safe and text evaluation methods catch `StackOverflowError`, so deeply nested input never escapes.
- `UnitElements.all()` and `getRegistry()` return the units in the order of the registry in every JVM. (#217)
- The documentation, the README and the Javadoc were corrected where they did not match the code: `factorial`, `nthRoot`, `lcm`, `acot`, `atan2`, the unit groups, the temperature base, the unit symbols and the input locale. The README is rewritten. (#187)

### Fixed

- Defects found by the new property tests: the magnitude of a rounded result (#202), tiny quotients (#203), rounding of ties and directed modes in `divide` (#204), the accuracy of `exp` and fractional powers (#205), digits of results near zero (#207), a slow `acosh` (#208), `lcm` (#214), `atan` near π/2 (#215) and the order of `UnitElements.all()` (#217).
- `gamma` and `beta` accepted `MathContext.UNLIMITED` and a precision above the supported limit. Every operation now rejects them with a typed error. (#221)
- The unit constants of the nautical league (it was wrong by a factor of 10000), the light year, the astronomical unit, the parsec family and the Thomson cross section, and the conversion constants listed in [docs/unit-audit.md](docs/unit-audit.md). (#161, #162)
- `RadixSort` keeps equal negative values stable.
- Numeric input with malformed grouping separators is rejected, and a `BigNumber` can be serialized after its calculator engine was created.
- `BigNumberList` implements `Cloneable` and calls `super.clone()`. `BigNumberMatrix` keeps its dimensions independent of caller-held numbers. `cartesianToPolarCoordinates` accepts points on an axis and rejects only the origin.
- Input is checked before it is evaluated: expressions longer than 100000 characters, factorials above 100000, powers with more than a million digits, series with more than a million terms and absurd precisions fail with a typed error. `1000000!` and `9^9999999999` no longer hang.

### Removed

- The JavaFX interface GraphFx and the placeholder `CalculusMath`. The sources are kept under the Git tag `archive/graphfx`. The screenshots in `images/` are deleted. (#189)
- The unused dependencies `apfloat` and `mockito`, the checked-in jars under `out/` and the changelog in the README.

### Security

- Dependabot, CodeQL, dependency review and the OpenSSF Scorecard run on every change. (#181)
- Releases need the maintainer's approval, are signed, come with checksums, an SBOM and a build provenance attestation, and the jars are byte-identical for the same commit. (#171, #182, #183)
- The inputs that ask for an enormous result are rejected, see [decision 0005](docs/decisions/0005-limits-on-expensive-inputs.md).
- Private vulnerability reporting is on, and `SECURITY.md` describes the process.

### Build and quality

- Maven Wrapper (Maven 3.9.16 with a pinned checksum), Maven Enforcer, reproducible jars, a pinned compiler release 21 and Lombok through `annotationProcessorPaths`. (#171)
- Updated JUnit (6.1.3), Lombok (1.18.48), the Maven plugins and the Central publishing plugin. (#172)
- JaCoCo with a coverage gate, SpotBugs with FindSecBugs, Javadoc as an error, Spotless with `.editorconfig` and `.gitattributes`, and japicmp against the previous release. (#170, #173, #174, #175, #178)
- jqwik property tests and differential tests against `BigDecimal`, `BigInteger` and `BigDecimalMath`, tests for the unit definitions, the Markdown links and the README, and an architecture test for the package dependencies. About 3300 tests, up from about 1600. (#176)
- CI on JDK 21, 23 and 25, on Windows, under three default locales, with a reproducibility check. (#180)
- PIT mutation tests once a week, with a threshold for the mutation score. See [docs/mutation-testing.md](docs/mutation-testing.md). (#177)

## [1.6.0] - 2026-05-30

### Added

- 13 languages and 20 locales for error messages and locale-aware number formatting: English, German (also `de-AT`, `de-CH`), Spanish, French (also `fr-BE`), Italian, Portuguese (`pt`, `pt-PT`, `pt-BR`), Dutch (also `nl-BE`), Czech, Danish, Swedish, Norwegian (`no`, `nb`), Finnish and Polish.
- The `SupportedLanguages` registry, `CalculatorEngine.getSupportedLanguages()` and `isLanguageSupported(Locale)`.
- `CalculatorEngine.setInputLocale(Locale)`: opt-in, strict parsing of comma-decimal input. The default stays `Locale.US`. `;` stays the argument separator.
- Publishing to Maven Central under the group ID `io.github.lembergmax`.

### Changed

- **Breaking:** the group ID and the packages moved to `io.github.lembergmax`.
- `CalculatorEngine`: the race between disabling the cache and an ongoing evaluation is closed, and an internal failure of an expression element reports `PROCESSING_INTERNAL`.

### Removed

- The placeholder `CalculusMath` and the JavaFX scaffolding.

## [1.5.0] - 2026-05-29

### Added

- Structural validation before evaluation: malformed input fails fast with a specific error code instead of computing an expensive sub-expression (`50000!/`, `5000!sqrt()`, `!5`, `*5`, `()`, `3 4`, `sqrt(1;2)`).
- Prefix unary `+` and `-` before groups, functions, constants and variables (`-(3+4)`, `-sin(0)`, `-x`, `2*-(1+1)`).
- Casio-style error categories (`Syntax`, `Math`, `Argument`, `Stack`, `Range`, `Dimension`) with a three-tier localized fallback.

### Changed

- Whitespace is a separator: `3 4` is an error and not `34`. Variable names have ASCII letters only. There is no implicit multiplication after `!`.
- **Breaking:** division by zero and domain errors are in the category `Math Error`. In `ErrorMode.RAW`, `getMessage()` is `Math Error` and no longer `Processing Error`. The typed codes did not change.

## [1.4.4] - 2026-05-14

- The code of 1.4.3. The tag only changes the version number in `pom.xml`.

## [1.4.3] - 2026-05-14

### Added

- Exact rational conversion formulas (`RationalConversionFormula`, `RationalReciprocalConversionFormula`), so a conversion such as km/h to m/s has no rounding error, and a base unit per unit group.
- Locale-aware result formatting with `evaluateToString`, `evaluateToPrettyString` and the safe and typed variants.
- Localized matrix errors with a fallback.

## [1.4.2] - 2026-05-13

### Added

- The `MultiValueResult` interface, so a coordinate returned by `Pol` or `Rec` can take part in a larger expression as its first value.

## [1.4.1] - 2026-05-13

### Changed

- All errors of the `CalculatorEngine` are localized.

## [1.4.0] - 2026-05-13

### Added

- The safe evaluation API (`evaluateSafe` and relatives) with typed results, localized error messages and the expression cache.

## [1.3.0] - 2026-02-28

### Added

- New unit groups: volume, time, force, speed, fuel consumption, data storage, pressure, energy and power, and locale detection for `UnitValue` input.

## [1.2.2.6] - 2025-12-09

### Added

- `median`, the sorting algorithms (`QuickSort` and the `Algorithm` base class) and `InsufficientElementsException`.

## [1.2.0] - 2025-12-04

### Added

- A demo `Main` class and the symbol for the inverse tangent of two arguments.

## [1.1.5] - 2025-12-03

### Added

- The symbol variant `FUNC_ATAN_2_S`.

## [1.1.4] - 2025-08-23

### Added

- Custom exceptions, `evaluateToString` and `evaluateToPrettyString`.

## [1.0.4] - 2025-08-23

### Added

- `BigNumberMatrix`.

## [1.0.3] - 2025-08-22

### Fixed

- Small fixes in the tests and the version information.

## [1.0.2] - 2025-08-22

### Changed

- `BigNumberCoordinate` extends `BigNumber`, and a blank expression evaluates to zero.

## [1.0.1] - 2025-08-22

### Changed

- Release packaging and the first publishing workflow.

## [1.0.0] - 2025-08-21

- First release: `BigNumber`, the `CalculatorEngine` with tokenizer, parser and evaluator, the math classes and the first tests.

[1.7.0]: https://github.com/lembergmax/JustMath/compare/1.6.0...1.7.0
[1.6.0]: https://github.com/lembergmax/JustMath/compare/1.4.4...1.6.0
[1.5.0]: https://github.com/lembergmax/JustMath/compare/1.4.4...1.6.0
[1.4.4]: https://github.com/lembergmax/JustMath/compare/1.4.3...1.4.4
[1.4.3]: https://github.com/lembergmax/JustMath/compare/1.4.2...1.4.3
[1.4.2]: https://github.com/lembergmax/JustMath/compare/1.4.1...1.4.2
[1.4.1]: https://github.com/lembergmax/JustMath/compare/1.4.0...1.4.1
[1.4.0]: https://github.com/lembergmax/JustMath/compare/1.3.0...1.4.0
[1.3.0]: https://github.com/lembergmax/JustMath/compare/1.2.2.6...1.3.0
[1.2.2.6]: https://github.com/lembergmax/JustMath/compare/1.2.0...1.2.2.6
[1.2.0]: https://github.com/lembergmax/JustMath/compare/1.1.5...1.2.0
[1.1.5]: https://github.com/lembergmax/JustMath/compare/1.1.4...1.1.5
[1.1.4]: https://github.com/lembergmax/JustMath/compare/1.0.4...1.1.4
[1.0.4]: https://github.com/lembergmax/JustMath/compare/1.0.3...1.0.4
[1.0.3]: https://github.com/lembergmax/JustMath/compare/1.0.2...1.0.3
[1.0.2]: https://github.com/lembergmax/JustMath/compare/1.0.1...1.0.2
[1.0.1]: https://github.com/lembergmax/JustMath/compare/1.0.0...1.0.1
[1.0.0]: https://github.com/lembergmax/JustMath/releases/tag/1.0.0
