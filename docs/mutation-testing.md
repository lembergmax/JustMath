# Mutation testing

Line coverage says that a test ran a line. It does not say that the test would notice if the line were wrong. [PIT](https://pitest.org) answers the second question: it changes the compiled code in small ways (a `<` becomes `<=`, a `+` becomes `-`, a return value becomes `null`, a method call is removed) and runs the tests against every change. A change that makes a test fail is killed. A change that no test notices has survived, and it points at behavior that is not tested.

The mutation score is the share of mutants that were detected. The weekly workflow [mutation.yml](../.github/workflows/mutation.yml) runs PIT and fails if the score is below the threshold in `pom.xml`.

## Run it

```bash
./mvnw -B -Djacoco.skip=true test-compile org.pitest:pitest-maven:mutationCoverage
```

A full run takes about 50 minutes on a machine with 22 cores and 8 PIT threads, and longer on a CI runner. The report is `target/pit-reports/index.html`, and `target/pit-reports/mutations.xml` has the same data for scripts.

To look at one class, narrow the mutated classes:

```bash
./mvnw -B -Djacoco.skip=true -DtargetClasses=io.github.lembergmax.justmath.bignumber.math.utils.MathUtils test-compile org.pitest:pitest-maven:mutationCoverage
```

PIT needs a green test suite. If it stops with "tests did not pass without mutation", fix the failing test first. A test that fails only under PIT usually depends on the order of tests or on files that the build creates.

## What is mutated and which tests run

| Setting | Value | Why |
| --- | --- | --- |
| `targetClasses` | `bignumber.*` and `calculator.*` | These packages hold the arithmetic, the functions, the lists, the matrices and the expression pipeline. The `converter` package is mostly the table of unit definitions, which [UnitDefinitionAuditTest](../src/test/java/io/github/lembergmax/justmath/converter/UnitDefinitionAuditTest.java) checks value by value. |
| `excludedTestClasses` | `*PropertyTest`, `*IllConditionedArgumentsTest`, `*RaceTest`, `*ConcurrencyTest` | PIT runs every test that covers a mutated line once per mutant. A property test runs hundreds of samples and the concurrency tests wait for threads, so one run would take many hours. The unit, contract and differential tests that stay are enough to kill most mutants. |
| `threads` | 8 (`pit.threads`) | |
| `timeoutConstant` | 10 s (`pit.timeout.constant.ms`) | A mutant that turns a loop into an endless loop is stopped after this time and counts as detected. |

The properties are set in `pom.xml`, so a run can change them on the command line (`-Dpit.threads=4`, `-DexcludedTestClasses=...`).

## Baseline

Measured on 2026-10-07 on `developer` after the arithmetic moved to `BigDecimal` ([decision 0008](decisions/0008-bigdecimal-arithmetic-and-lazy-digits.md)), with 2870 mutants:

| Result | Mutants |
| --- | ---: |
| Killed by a failing test | 2336 |
| Timed out or ran out of memory (detected) | 59 |
| Survived | 322 |
| Not covered by any test | 153 |

The mutation score is **83 %**, the test strength (detected mutants among the covered ones) is 88 %, and the line coverage of the mutated classes is 92 %. The run took 44 minutes.

| Package (below `io.github.lembergmax.justmath`) | Mutants | Detected | Score |
| --- | ---: | ---: | ---: |
| `bignumber` | 789 | 712 | 90.2 % |
| `bignumber.algorithms` | 269 | 222 | 82.5 % |
| `bignumber.internal` | 85 | 61 | 71.8 % |
| `bignumber.math` | 1027 | 769 | 74.9 % |
| `bignumber.math.utils` | 34 | 34 | 100.0 % |
| `calculator` | 591 | 543 | 91.9 % |
| `calculator.errors` | 32 | 23 | 71.9 % |
| the other packages | 43 | 31 | 72.1 % |

`bignumber.math` holds `BasicMath`, which has 598 of the 2870 mutants (66 % detected). Most of them are in `exp`, `ln` and the fractional power, which still compute on digit strings, and in the rounding of the double fast paths. The addition, multiplication, division, remainder, power and factorial run on `BigDecimal` and `BigInteger` and are protected mostly by the property tests, which are excluded from the run, so the score understates how well `BasicMath` is protected. The first run, before the arithmetic moved, found 83 % as well, with 3044 mutants.

## What survives and why

Most of the 475 survivors are of three kinds:

- **Mutants that change speed and not the result.** The pivot choice of `QuickSort` and the capacity of the LRU cache in `CalculatorEngine` do not change what a caller sees. No test can fail, so these mutants stay.
- **Boundary mutants in code that the tests reach only with typical values.** A `<` that becomes `<=` matters only for the value exactly on the boundary. These are worth killing when the boundary is part of a contract (the maximum length of an expression, the largest `MathContext` precision), and they are not worth it otherwise.
- **Lines without a test.** The 153 mutants without coverage point at methods and branches that no test executes. These are the best place to start.

A mutant is never "wrong" on its own. Read the changed line, decide what a caller could observe, and write the test that observes it. If nothing could be observed, leave the mutant and move on.

## Threshold

`pit.mutation.threshold` in `pom.xml` is 81, two points below the baseline. The margin keeps a run on a slower machine, where a different number of mutants times out, from failing the weekly job for no reason.

- Raise the threshold when a run is three points or more above it, in the same commit that adds the tests.
- Do not lower it. If a change removes tests or adds code without tests, the weekly job should say so. When the drop is deliberate, the commit message gives the reason.

## Writing a test that kills a mutant

- Test what a caller can see: the return value, the exception and its `CalculatorErrorCode`, the state of the receiver and of the arguments afterwards. Do not test that a private method was called.
- Test the value on the boundary and the values on both sides of it. [MathUtilsTest](../src/test/java/io/github/lembergmax/justmath/bignumber/math/utils/MathUtilsTest.java) pins the precision limit, the number of attempts of `computeWithGuardDigits` and the guard digits of each attempt.
- A method that returns `this` or a new object needs an assertion on the returned value, not only on the changed list. [BigNumberListFluentOperationsTest](../src/test/java/io/github/lembergmax/justmath/bignumber/BigNumberListFluentOperationsTest.java) does that with `assertSame`.
- For a class that implements a JDK interface, compare it with the JDK implementation. [BigNumberListJdkListContractTest](../src/test/java/io/github/lembergmax/justmath/bignumber/BigNumberListJdkListContractTest.java) runs 66 `List` operations on a `BigNumberList` and on an `ArrayList`.
- The general rules are in [testing.md](testing.md).
