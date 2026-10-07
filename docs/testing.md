# Testing

About 2900 tests run in `./mvnw verify`, in about a minute. This page says what kinds of tests there are, what each kind is good for, and how to add one.

## Kinds of tests

| Kind | Where | What it protects |
| --- | --- | --- |
| Unit tests | `src/test/java`, mirrored packages | The documented behavior of a class: values, edge cases, error codes. |
| Audit tests | `*ConstantIntegrityTest`, `*MutationAndRoundingTest`, `*EngineIsolationTest`, `*NullByteTest` and others | Defects found in past audits (shared constants, mutation of the receiver, state shared between engines). They are not removed or weakened without understanding the finding behind them. |
| Property tests | `*PropertyTest` ([jqwik](https://jqwik.net)) | A rule that has to hold for every input, on generated input that favors the cases that break hand-written decimal arithmetic: long operands, runs of nines, powers of ten, zero, very different scales. |
| Differential tests | the same classes | The result must equal an independent reference: `BigDecimal` for arithmetic and rounding, `BigInteger` for the integer functions, `BigDecimalMath` at 200 more digits for the functions, integer matrix arithmetic for matrices. |
| Ill-conditioned cases | `BigNumberIllConditionedArgumentsTest`, `ArcTangentNearHalfPiTest` | Named examples where a computation with a fixed number of digits loses the result: `sin` near a multiple of π, `ln` near 1, `atan` of a huge number. |
| Unit definitions | `UnitDefinitionAuditTest` | Every unit has a definition with a source or a reason, and the value matches the definition. See [unit-audit.md](unit-audit.md). |
| Generated documents | `ReferenceDocumentsTest` | `docs/units.md` matches the registry. |
| Architecture | `PackageDependencyTest` | The packages depend on each other only in the allowed directions. See [architecture.md](architecture.md). |
| Contract tests | `BigNumberListJdkListContractTest`, `BigNumberOverloadConsistencyTest`, `BigNumberMathContextValidationTest` | A `BigNumberList` behaves like an `ArrayList`, every overload of a `BigNumber` method gives the same result, and every operation rejects an unsupported `MathContext`. |

## Accuracy tests

A function result is correct when it is within one unit in the last place (ulp) of the requested precision, measured **relative to the result itself**. `AccuracyAssertions.assertWithinUlps` does that. A result near zero has to be as accurate in its own digits as a result near one; an absolute tolerance would accept `4.6E-26` for `4.2E-36`. The reference is computed with 200 more digits than the test requests, which is enough to resolve a result that loses up to 45 digits to cancellation.

The generators of the hard arguments (near a multiple of π, near 1, tiny, huge) live in the test classes; add a generator when you find a new class of hard argument.

## Property tests

Each property runs with a fixed seed (`DecimalArbitraries.SEED`), so a build is deterministic. A failing property prints the shrunk sample and the seed. To search for new failures, change the seed temporarily, run the properties, and put the seed back. A failure that you find becomes a named regression test with at least two rounding modes, and an issue.

Keep a property fast: 150 to 300 tries, operands that are as long as the property needs and no longer. A property that calls a function with a wall-clock limit uses `assertTimeoutPreemptively` to turn a hang into a failure, never to measure speed.

## Mutation testing

Coverage shows that a line ran, mutation testing shows that a test would notice if the line were wrong. PIT runs once a week and fails below a threshold for the mutation score. See [mutation-testing.md](mutation-testing.md) for the baseline, what survives and how to kill a mutant.

## Rules for every test

- A bug fix starts with a test that fails without the fix.
- Test rounding with at least `HALF_UP` and `HALF_EVEN`, and formatting with `Locale.US`, `Locale.GERMANY` and `Locale.FRANCE`.
- Branch on the `CalculatorErrorCode`, not on the text of a message.
- Compare receiver and result before and after an operation to prove that it did not mutate. Check `BigNumbers.ZERO` and `ONE` after code that could return them.
- No `Thread.sleep`, no wall-clock assertions, no unseeded randomness.
- Test classes are package-private.
