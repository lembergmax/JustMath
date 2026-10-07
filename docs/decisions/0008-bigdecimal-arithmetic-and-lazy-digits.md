# 0008: Arithmetic on BigDecimal, digits written down on demand

Status: accepted for 1.7.0, replaces [0007](0007-string-based-arithmetic.md)

## Situation

Decision 0007 kept the arithmetic of `BasicMath` on decimal digit strings for 1.7.0 and planned to replace the hot paths later. The benchmarks showed that `BigNumber` was 90 to 2400 times slower than `BigDecimal` for add, multiply, divide and power. A profile with JDK Flight Recorder named four causes ([performance.md](../performance.md#where-the-time-went)):

1. Every operand took a detour: `BigNumber` to `BigDecimal`, to plain text, to parsed digits. Converting between decimal text and a `BigInteger` is quadratic in the number of digits. It was 85 % of an addition of 1000 digits.
2. The multiplication had one decimal digit per step, with a division by ten in the inner loop.
3. The division found each quotient digit by trial.
4. A value existed only as two strings, so every result was built as text and parsed again.

The differential property tests that compare `BasicMath` with `BigDecimal` and `BigInteger` for every rounding mode were already the safety net that decision 0007 asked for.

## Decision

- Addition, subtraction, multiplication, division, remainder, modulo, integer powers and the factorial run on `BigDecimal` and `BigInteger`.
- Division multiplies the unscaled dividend by a power of ten so that the integer quotient has at least two digits more than the precision, keeps a remainder that is not zero as one more non-zero digit, and lets `BigDecimal.round` apply the rounding mode of the caller once. `BigDecimal.divide(BigDecimal, MathContext)` gives the same values, but it removes the zeros of an exact quotient one division at a time, which is slow for two small numbers at a high precision.
- A `BigNumber` has two views of one value: the digits (`getValueBeforeDecimalPoint()`, `getValueAfterDecimalPoint()`, `isNegative()`) and the `BigDecimal` (`toBigDecimal()`). A number that is built from a `BigDecimal`, which is every result of a calculation, holds only the `BigDecimal` and writes the digits down when somebody asks for them. `toBigDecimal()` keeps its value, and every method that changes the digits discards it.
- `exp`, `ln` and the fractional power keep their algorithm on digit strings with guard digits. Their multiplication and division use the new primitives.

## Consequences

- The public API is unchanged. The digits, the scale of `toBigDecimal()`, the sign flag, the string representations, the exceptions and their messages are the same as before for every input that was compared: 300000 random operations against the previous implementation, and `BigNumberDigitsRepresentationTest` pins the cases at the edges.
- Reading a `BigNumber` from several threads is safe, also while its digits are written down for the first time: `valueBeforeDecimalPoint` is volatile and written last. Changing a shared number (`trim()`, `negateThis()`, the setters) is still not.
- A number can hold the digits and the `BigDecimal`, which costs memory for large values.
- `toBigDecimal()` returns the same instance on repeated calls. `BigDecimal` is immutable, so this is not visible except by identity.
- Speed: see [performance.md](../performance.md). The remaining cost is the conversion between text and numbers at the edges, which `BigDecimal` pays as well.
- The rejected alternative was to read the stored digits in the operand intake and to multiply with `BigInteger`, and to keep the value as text. That made an addition of 1000 digits six times faster and left it about 200 times slower than `BigDecimal`, because every result is still built as text.
