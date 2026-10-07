# Extending JustMath

The function registry and the unit registry are closed after static initialization on purpose ([decision 0006](decisions/0006-closed-registries.md)). You cannot add a function or a unit at runtime. This guide is for a pull request that adds one to the library. Read [CONTRIBUTING.md](../CONTRIBUTING.md) first for the workflow and the code rules, and [architecture.md](architecture.md) for the structure.

The short version of the rules: a new method says in its name whether it mutates, passes the caller's `MathContext` and rounding mode through, rounds once, throws a typed exception with an error code, and has a test that fails without it.

## Add a function

Example: a function `foo(x)` of one argument.

1. **Math.** Write the computation as a static method in the class of `bignumber.math` that fits (`LogarithmicMath`, `RadicalMath`, ...), or in a new final class with a private constructor. Mark parameters that must not be `null` with `@NonNull`. Document the domain, the rounding, the singularities and the thrown exceptions in the Javadoc. Throw `MathArgumentException` or `MathArithmeticException` with a `CalculatorErrorCode`, never a bare JDK exception: `UntypedMathExceptionGuardTest` fails otherwise. If the library function loses digits near a zero or a pole, evaluate it through `MathUtils.computeWithGuardDigits`.
2. **`BigNumber`.** Add the public method with its overloads (no argument with the default context, with a `MathContext`, with a `MathContext` and a `Locale`) and delegate to the math class. Do not put the computation into `BigNumber`.
3. **Registry.** In `ExpressionElements` add a constant `FUNC_FOO = "foo"` (and `FUNC_FOO_S` for a symbol variant) and one line in the static block:

   ```java
   new OneArgumentFunction(FUNC_FOO, 6, BigNumber::foo),
   ```

   Pick the element class by shape: `OneArgumentFunction`, `OneArgumentTrigonometricFunction` (takes the angle mode), `TwoArgumentFunction`, `ThreeArgumentFunction`, `UnlimitedArgumentFunction` (a list of arguments, such as `avg`), `CoordinateFunction` (returns a coordinate). The second argument is the precedence; use the value of a function of the same kind. The tokenizer picks the longest registered symbol, so a longer name wins over its prefix.
4. **Errors.** Reuse an existing `CalculatorErrorCode` if one fits. A new code needs a bundle key in `i18n/calculator_errors.properties` and in the other language files; `CalculatorErrorLocalizationTest` checks that each code has an entry in English and German and a fallback.
5. **Tests.** At least:
   - a test of `BigNumber.foo` for zero, ±1, a tiny and a huge value, the edges of the domain, and two rounding modes (`HALF_UP` and `HALF_EVEN`);
   - a property or differential test against a reference if one exists (see [testing.md](testing.md)); a function from `BigDecimalMath` belongs into `BigNumberFunctionPropertyTest`;
   - an engine test for the expression `foo(...)`, for the wrong number of arguments and for the error code of a domain error.
6. **Docs.** Add the function to the table in the README and to the changelog. `ReadmeConsistencyTest` fails when a registered function is missing from the README.

## Add an operator

Operators are registered like functions, with `BinaryOperator`, `SimpleBinaryOperator` (an operation that needs no `MathContext`) or `UnaryOperator` (prefix or postfix). Precedence: `+` and `-` 2, `*`, `/` and `%` 3, `^` 4, the postfix `!` 5, `nPr` and `nCr` 6. Binary and unary `+` and `-` live in two registries (`register` and `registerUnary`), because the same symbol has different precedence in each role. A change of the grammar needs tests for every new valid form and for every new invalid form with its `CalculatorErrorCode`, and a check in `TokenizerStatefulnessTest` that the tokenizer keeps no state.

## Add a unit

1. Add the constant to the enum of its group in `Unit` (`Unit.Length`, `Unit.Mass`, ...) with a one-line Javadoc.
2. Add **one** `define` line to `UnitRegistry.BUILT_IN`: constant, display name, symbol, and the value of one unit in the base unit of the group. Use `defineFraction` when the definition is a ratio without a finite decimal form (a third, 5/9), `defineAffineFraction` for an affine unit such as a temperature scale, and the reciprocal forms for a unit that is a quantity per distance. Symbols are case-sensitive and unique.
3. Add the unit to `src/test/resources/unit-audit/unit-definitions.tsv` with the value that its definition gives and the source, or to `unit-exceptions.tsv` with the reason why no definition exists. `UnitDefinitionAuditTest` fails when a unit is in neither file and when a value differs from its definition. Derive the value from the defining constants, do not copy it from the registry or from a conversion website.
4. Regenerate the reference: `./mvnw test -Dtest=ReferenceDocumentsTest -Dupdate.docs=true`.
5. Add the unit to the changelog. A change of an existing value is a change of results and is written there with the old and the new value.

## Add a sorting algorithm

1. Add a final class that extends `SortingAlgorithm` in `bignumber.algorithms`. It sorts a copy, never the argument. Call `abortIfInterrupted()` in the outer loop so that a long sort can be cancelled.
2. Add a test class that extends `AbstractSortAlgorithmTest`, like `MergeSortTest`.
3. Add the class to the list in `BigNumberListPropertyTest`, which compares every algorithm with `List.sort`.
4. Change `BigNumberList.sort()` only if the new algorithm should be chosen automatically for a size or an input.
