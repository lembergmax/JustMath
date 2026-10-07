# 0007: Arithmetic on digit strings

Status: accepted for 1.7, to be revisited in [#220](https://github.com/lembergmax/JustMath/issues/220)

## Situation

`BasicMath` implements addition, subtraction, multiplication, division, integer powers, `exp` and the fractional power on decimal digit strings, and does not use `BigDecimal` for them. Nothing in the repository said how fast or how correct this is compared with the JDK.

The tests of 1.7.0 answer the second question. `BasicMathArithmeticPropertyTest`, `BasicMathDivisionPropertyTest` and the rounding tests compare `BasicMath` with `BigDecimal` and `BigInteger` for every `RoundingMode` on generated operands, and they found four defects in the division, the power and `exp` (#202 to #205). The defects are fixed, and the same tests now keep the results equal.

The benchmarks answer the first question: `BigNumber` is 90 to 2400 times slower than `BigDecimal` for add, multiply, divide and power ([performance.md](../performance.md)).

## Decision

Keep the digit-string implementation in 1.7.0. It is correct as far as the differential tests can tell, it keeps the rounding contract of the library, and replacing it is a change of the core that does not belong into a release that is about correctness and process. The release says in the open how slow it is.

Replace the hot paths with `BigDecimal` and `BigInteger` in a later minor release ([#220](https://github.com/lembergmax/JustMath/issues/220)). The tests above are the safety net: the replacement must keep every one of them green without changing an expected value. A profile shows where to start: the operand intake of `BasicMath` converts every operand through `BigDecimal` and back to text, which costs most of an addition, and the multiplication and the division work on one decimal digit per step ([performance.md](../performance.md#where-the-time-goes)).

## Consequences

Callers who need speed for plain decimal arithmetic should use `BigDecimal` today. The README and the Javadoc do not claim that the arithmetic is fast. A later replacement changes no result and no signature.
