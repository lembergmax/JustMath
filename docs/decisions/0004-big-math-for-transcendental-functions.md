# 0004: big-math for the transcendental functions

Status: accepted for 1.x. Revisit if a function cannot be made correct with the library, or if the dependency becomes a problem.

## Situation

`BasicMath` computes the arithmetic itself: addition, subtraction, multiplication, division, integer powers, `exp` and the fractional power. The roots, the logarithms and the trigonometric, hyperbolic and inverse functions call [big-math](https://github.com/eobermuhlner/big-math) (`BigDecimalMath`), which is the only runtime dependency. Fourteen old issues asked for in-house versions of them; they were closed in favor of one open question: keep the library or write the functions.

Writing them means owning the proof of correctness and a high-precision oracle for each function. `BigDecimalMath` guarantees an absolute error of one unit in the last place of the requested precision. That is not enough for a result near a zero of the function, near a pole, or near ±π/2, where the digits that matter are far behind the decimal point.

## Decision

- Keep big-math as the engine for these functions.
- Close the gap with the contract instead of with a rewrite: `MathUtils.computeWithGuardDigits` evaluates with guard digits, reads the size of the result and repeats with more digits until every requested digit is right, then rounds once with the caller's `MathContext`. Where the library returns a wrong value for a large argument (`atan` of a huge number), the function uses an exact identity so that the library only sees an argument it handles correctly.
- Test the result against the library at 200 more digits on generated hard arguments, with a tolerance of one unit in the last place measured relative to the result (`AccuracyAssertions`, `BigNumberFunctionPropertyTest`).

## Consequences

The accuracy contract is the same for every function and is tested. The cost is a second evaluation for results that lose digits. A defect of big-math has to be found by the property tests, and the first rounds found three: the loss of digits near a zero of a function (#207), a slow `acosh` (#208) and the loss of the distance from π/2 in `atan`, `acot` and `atan2` (#215).

Replacing a function later is possible one function at a time, because the oracle and the property tests exist.
