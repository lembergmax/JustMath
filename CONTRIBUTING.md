# Contributing to JustMath

Thanks for taking the time. This page explains how to build the project, which checks a change has to pass, and the rules the maintainer applies in review. Please read it before you open a pull request. By contributing you agree that your work is released under the [MIT License](LICENSE).

## Build and test

You need JDK 21 or newer. Use the Maven Wrapper, so everybody builds with the same Maven:

```bash
./mvnw verify
```

On Windows use `mvnw.cmd verify`. `verify` runs the whole quality gate:

| Check | What fails the build |
|-------|----------------------|
| Tests (JUnit 5, about 1650) | any failing test |
| Enforcer | JDK older than 21, Maven older than 3.9, an unpinned plugin, diverging dependency versions |
| JaCoCo | line coverage below 88 % or branch coverage below 62 % |
| Javadoc | any error or warning (it runs on the delomboked sources) |
| SpotBugs with FindSecBugs | any finding that is not listed, with a reason, in `config/spotbugs-exclude.xml` |
| Spotless | tabs, trailing whitespace, a missing final newline or an unused import in a Java file |

Useful commands:

```bash
./mvnw spotless:apply                         # fix the formatting
./mvnw -Dtest=BigNumberListContractTest test   # run one test class
./mvnw clean verify                           # also re-runs Javadoc, which Maven skips when it thinks nothing changed
```

The jars are reproducible: two clean builds of the same commit are byte-identical. When the version is bumped for a release, set `project.build.outputTimestamp` in `pom.xml` to the commit date.

## Working on a change

1. Open or pick an issue first, unless it is a small fix.
2. Branch from `developer` and open the pull request against `developer`. `main` only receives release merges.
3. Keep a pull request to one topic. Do not mix a bug fix and a refactoring, and do not mix formatting with logic.
4. Write the test first. A bug fix needs a regression test that fails before the fix and passes after it.
5. Make sure `./mvnw verify` passes. The `CI result` check has to be green before a pull request is merged.

Commit messages start with an area and a short summary, for example `bignumber: return copies from BigNumberList.min()`. The areas in use are `bignumber`, `calculator`, `converter`, `build`, `ci`, `docs`, `test`, `style` and `chore`. Use the body to say why the change is needed, and finish with `Closes #123` when it fixes an issue. Formatting-only commits are listed in `.git-blame-ignore-revs`; run `git config blame.ignoreRevsFile .git-blame-ignore-revs` to skip them in `git blame`.

## Code rules

**Mutation is visible in the name.** A method called `xxx()` returns a new instance and leaves the receiver alone (`add`, `sin`, `round`, `negate`, `abs`, `floor`). A method called `xxxThis()` changes the receiver in place and returns it (`negateThis()`). Collection methods that change every element end in `All` (`absAll`, `negateAll`). Copies are called `copy()` or `clone()`. `BigNumber` has a few in-place mutators (`negateThis()`, `trim()`, the setters), so never hand out an instance you also keep: the constants in `BigNumbers` (`ZERO`, `ONE`, ...) are shared and are never returned as a result, and values that come out of a collection are copies.

**Rounding and precision.** Pass the caller's `MathContext` to every sub-operation and keep its rounding mode. If you need extra digits, build a new `MathContext` with the caller's rounding mode, compute with the guard digits and round once at the end. Test with at least `HALF_UP` and `HALF_EVEN`.

**Locale is about presentation.** Arithmetic, comparison and hashing work on `toBigDecimal()`. A locale only changes how a value is parsed from or formatted to a string. The expression parser reads `.` as the decimal separator and `;` as the argument separator unless an input locale is set with `setInputLocale`. Test locale-dependent code with `Locale.US`, `Locale.GERMANY` and `Locale.FRANCE`.

**Errors.** A domain violation (division by zero, logarithm of zero or less, a negative factorial, a pole of `tan`) is reported with a typed exception and a `CalculatorErrorCode`, not with `null`, `NaN` or a bare `RuntimeException`. Tests branch on the code, never on the message text. No empty `catch`, no `System.out`, no `printStackTrace()`.

**Null.** Mark parameters that must not be `null` with Lombok's `@NonNull` and do not add a manual check on top.

**Style.** Four spaces, `final` on parameters and local variables that do not change, no wildcard imports in new files, constants in `UPPER_SNAKE_CASE`, no cryptic abbreviations (`mathContext`, not `mc`). New Java files start with the MIT license header: copy it from any existing file. Spotless checks the whitespace rules.

**Documentation.** Every public type, method and field has Javadoc. It states what the method does, whether it changes the receiver, how it rounds, which exceptions it throws and what it returns. Javadoc must not describe behaviour the code does not have, so change it together with the code. Avoid `//` comments inside method bodies; if a block needs explaining, give it a name and extract it. Update the README when you change behaviour it describes.

**Pipeline.** Expression evaluation has three stages: the tokenizer splits the text, the postfix parser orders the tokens, the evaluator runs them. Keep each stage to its job. Math belongs in the matching `*Math` class (all static and stateless), and `BigNumber` delegates to it. Do not add new math directly to `BigNumber`, which is already very large.

**Tests.** Use JUnit 5, `@DisplayName` in plain English and a method name that says what is checked. Math tests cover zero, plus and minus one, very small and very large values and the edges of the domain. Parser changes need a valid example and an invalid example with the expected `CalculatorErrorCode` for every new form. Tests with names such as `*ConstantIntegrityTest`, `*MutationAndRoundingTest` or `*EngineIsolationTest` protect against defects found in past audits. Extend them, do not remove or weaken them. Do not use `Thread.sleep` or the wall clock.

## Public API and versioning

JustMath follows [Semantic Versioning](https://semver.org). The public API is everything that is `public` in a package that is not named `internal`, and the types of `calculator.internal` that appear in public signatures (`TrigonometricMode`, `CoordinateType`).

- A new method or class is a minor change.
- A change that can break a caller (a removed or renamed public member, a changed signature, changed behaviour that is documented) needs a new major version. If one is unavoidable in a minor release, it must be announced in the pull request, listed in the changelog and marked there as breaking.
- To retire something, deprecate it first: `@Deprecated(since = "x.y.z")` plus a Javadoc `@deprecated` tag that names the replacement. Remove it no earlier than the next major version.

## Extending the library

**A new function or operator.** Add or reuse an element class in `calculator.expression.elements`, register it in the static block of `ExpressionElements` (the registry is closed after class initialization on purpose), and add an operation interface if it has a new argument shape. Add tests for valid and invalid use, an error code and its translations in `i18n/calculator_errors_*.properties` if needed, and a row in the README table.

**A new unit.** Add the constant to the right enum in `Unit` and exactly one `define(...)` line in `UnitRegistry.BUILT_IN`. Use an exact value or a rational formula, name the source of the constant in the pull request, and add a test with an exact expected value. Symbols must be unique and are case-sensitive.

**A new sorting algorithm.** Extend `SortingAlgorithm`, call `abortIfInterrupted()` inside the passes, and add a test class that extends `AbstractSortAlgorithmTest`.

## Reporting problems

Open an [issue](https://github.com/lembergmax/JustMath/issues/new/choose) for bugs and ideas, start a [discussion](https://github.com/lembergmax/JustMath/discussions) for questions, and follow [SECURITY.md](SECURITY.md) for vulnerabilities. This project follows the [Code of Conduct](CODE_OF_CONDUCT.md).
