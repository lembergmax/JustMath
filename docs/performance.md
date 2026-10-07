# Performance

`BigNumber` is built for correct results, not for speed. For raw arithmetic it is much slower than `BigDecimal`: 90 to 2400 times for add, multiply, divide and power. This page has the numbers, the method and what follows from them. The follow-up work is tracked in [#220](https://github.com/lembergmax/JustMath/issues/220).

## What was measured

The JMH benchmarks in `benchmarks/` run the same operation on the same operands with `BigNumber` and with `BigDecimal` (and `BigInteger` for the factorial):

- `ArithmeticBenchmark`: add, multiply, divide, an integer power with exponent 20, and the factorial of `n` equal to the number of digits. The operands are random decimals of 10, 100 and 1000 digits with half of the digits after the decimal point. Division uses a `MathContext` with as many digits as the operands.
- `EngineBenchmark`: `CalculatorEngine.evaluate` for a short expression (`2+3*4-5/2`) and a longer one (`sqrt(16)*2^10+sin(30)*cos(60)+10!/8!-summation(1;20;k^2)`), with the expression cache on and off.

Environment of the numbers below: Intel Core Ultra 7 155H, 15.6 GB, Windows 11, Oracle JDK 21.0.9, JMH 1.37, one fork, 3 warm-up and 5 measured iterations of one second each, nothing else running. Times are the average per operation in microseconds with the error of the 99.9 % confidence interval. Absolute numbers depend on the machine; the ratios are what matters.

## Results

Arithmetic:

| Operation | Digits | BigNumber (µs) | BigDecimal (µs) | Ratio |
| --- | ---: | ---: | ---: | ---: |
| add | 10 | 0.979 ± 0.018 | 0.003 ± 0.000 | 287x |
| add | 100 | 5.955 ± 0.243 | 0.020 ± 0.002 | 301x |
| add | 1000 | 99.926 ± 2.197 | 0.094 ± 0.009 | 1068x |
| multiply | 10 | 1.185 ± 0.047 | 0.014 ± 0.002 | 87x |
| multiply | 100 | 27.442 ± 0.383 | 0.071 ± 0.005 | 387x |
| multiply | 1000 | 2081.246 ± 26.301 | 2.718 ± 0.167 | 766x |
| divide | 10 | 3.922 ± 0.219 | 0.012 ± 0.001 | 323x |
| divide | 100 | 184.613 ± 16.270 | 0.521 ± 0.033 | 354x |
| divide | 1000 | 18242.597 ± 341.869 | 21.312 ± 10.433 | 856x |
| power (exponent 20) | 10 | 79.911 ± 5.065 | 0.182 ± 0.112 | 440x |
| power (exponent 20) | 100 | 8121.323 ± 447.852 | 3.615 ± 0.162 | 2246x |
| power (exponent 20) | 1000 | 748674.730 ± 20865.372 | 313.875 ± 22.516 | 2385x |
| factorial of n = digits | 10 | 0.873 ± 0.019 | 0.138 ± 0.016 | 6x |
| factorial of n = digits | 100 | 31.218 ± 2.257 | 2.323 ± 0.218 | 13x |
| factorial of n = digits | 1000 | 5820.609 ± 755.589 | 115.442 ± 2.480 | 50x |

The factorial is compared with a plain product of `BigInteger` values, not with a product tree.

Expression engine:

| Expression | Expression cache | Time per evaluation (µs) |
| --- | --- | ---: |
| short | on | 3.48 ± 0.08 |
| short | off | 4.32 ± 0.11 |
| long | on | 248.90 ± 14.00 |
| long | off | 255.31 ± 14.57 |

## Where the time goes

A profile with JDK Flight Recorder (`-XX:StartFlightRecording=settings=profile`) around a plain loop over one operation, same machine and JVM as above, 6 seconds per operation after a 2 second warm-up:

| Operation | Digits | Where the samples are |
| --- | ---: | --- |
| add | 1000 | 85 % in `BasicMath.parseFromBigNumber`: 58 % in `BigDecimal.toPlainString` (the conversion of a `BigInteger` to text) and 24 % in `new BigDecimal(String)`. The addition of the digits is a small rest. |
| multiply | 100 | 76 % in `BasicMath.multiplyUnsigned`, 12 % in `parseFromBigNumber`, 8 % in `new BigNumber(String, Locale)` for the result. |
| divide | 100 | 98 % in the long division: 65 % in `estimateQuotientDigit` (47 % of it in `compareDivisorTimesDigitToRemainder`) and 28 % in `subtractProductFromRemainder`. |
| power | 100 | 99.6 % in `multiplyUnsigned`, called again and again with longer operands. |

Four causes follow from this, in the order of how much they cost:

1. **The operands take a detour through `BigDecimal`.** `parseFromBigNumber` turns a `BigNumber` into `toBigDecimal().toPlainString()` and parses that text again, although the number already holds its digits as two strings. Converting between decimal text and a `BigInteger` is quadratic in the number of digits: at 1000 digits `new BigDecimal(String)` takes 13.7 µs and `BigDecimal.toPlainString()` 25.5 µs, while `BigDecimal.add` takes 0.22 µs. Every operation pays this for each operand, so it dominates add, subtract and compare, and it is a tenth of a multiplication of 100 digits.
2. **The multiplication has one decimal digit per step.** `multiplyUnsigned` is the schoolbook algorithm on single decimal digits, with a division and a remainder by 10 in the inner loop. `BigDecimal` works on 32-bit limbs (about ten digits each, so about a hundred times fewer steps for the same numbers) and switches to Karatsuba and Toom-Cook for long operands. A power is a chain of such multiplications.
3. **The division finds one quotient digit per step by trial.** For each digit of the quotient it estimates a digit, multiplies the divisor by it and compares the product with the remainder, and then subtracts. That is quadratic with a large constant. `BigDecimal` uses Burnikel-Ziegler for long operands.
4. **A value is two strings.** `BigNumber` stores the digits before and after the decimal point as text. Each result is built as text, parsed and validated again by `new BigNumber(String, Locale)` (number check, locale handling, a second `BigNumber` inside the parser) and trimmed. This is the floor that stays when the first three are fixed: `add` of two numbers of 10 digits still takes about 0.8 µs against 0.003 µs.

An experiment shows how much the first two are worth. In a scratch copy, `parseFromBigNumber` read the two stored digit strings, and `multiplyUnsigned` multiplied with `BigInteger`. Nothing else changed. Time per operation in µs, measured with a plain timing loop (not JMH, so read the ratios, not the digits); the test suite was not run against the experiment:

| Operation | Digits | Today | Direct operands | Direct operands and `BigInteger` multiply | `BigDecimal` |
| --- | ---: | ---: | ---: | ---: | ---: |
| add | 10 | 1.25 | 0.79 | | 0.003 |
| add | 100 | 7.7 | 2.6 | | 0.020 |
| add | 1000 | 117 | 21 | 18 | 0.094 |
| multiply | 100 | 29.9 | 27.2 | 7.0 | 0.071 |
| multiply | 1000 | 2464 | 2217 | 141 | 2.7 |
| power (exponent 20) | 100 | 8041 | | 555 | 3.6 |
| power (exponent 20) | 1000 | 869986 | | 22645 | 314 |
| divide | 100 | 209 | 224 | | 0.52 |
| divide | 1000 | 21423 | 20495 | | 21.3 |

Reading it: the direct operands make add 1.6 times faster at 10 digits and three to six times faster at 100 to 1000 digits, and leave the other operations as they are. The `BigInteger` multiplication makes a multiplication four times faster at 100 digits and 17 times at 1000, and a power 15 to 38 times. Division does not change, because its time is in the digit loop. What stays is the conversion between text and `BigInteger` (141 µs for a multiplication of 1000 digits against 2.7 µs for `BigDecimal`). To get close to `BigDecimal`, `BigNumber` would have to hold a `BigDecimal` and make the text lazy. That touches `trim()` and the other mutators and the public getters `getValueBeforeDecimalPoint()` and `getValueAfterDecimalPoint()`, so it is a design decision and not a speed-up of a method.

## Reading the numbers

- The arithmetic of `BigNumber` runs on digit strings. Every operation parses its operands, runs a schoolbook algorithm and formats the result, while `BigDecimal` runs on `BigInteger` with Karatsuba, Toom-Cook and Burnikel-Ziegler algorithms. The gap grows with the number of digits.
- `BigNumber` also carries a locale, a `MathContext` and a trimmed representation, and `BigDecimal` is a JDK class that the JIT compiler knows. The comparison is what a caller sees when the same step is written with each type, not a comparison of algorithms alone.
- The expression cache saves the tokenizer: 20 % for a short expression and nothing visible for a longer one, where the time goes into the functions (`sin`, `sqrt`, the factorial and the summation).
- What `BigNumber` buys for this price: the rounding contract (one rounding step with your `MathContext` and rounding mode for every function), the functions with a tested accuracy, the exact comparison and hashing by value, the locale handling and the engine around it.

If you only need exact decimal arithmetic in a hot loop, use `BigDecimal`. If you need the functions, the engine, the units or the rounding contract, the cost per call is in the range of microseconds to milliseconds for numbers of up to 100 digits.

## Reproduce

```bash
./mvnw -DskipTests install
./mvnw -f benchmarks/pom.xml package
java -jar benchmarks/target/benchmarks.jar -rf json -rff results.json
```

A single benchmark: `java -jar benchmarks/target/benchmarks.jar ArithmeticBenchmark.bigNumberDivide -p digits=100`. Close other programs first: a build or an IDE that runs in the background changes the numbers.

## What follows

[Decision 0007](decisions/0007-string-based-arithmetic.md) records why the arithmetic is as it is. The plan in [#220](https://github.com/lembergmax/JustMath/issues/220) follows the causes above, cheapest first: read the stored digits directly, multiply and raise to a power with `BigInteger`, divide with `BigInteger` or `BigDecimal`, and last decide whether `BigNumber` should hold a `BigDecimal`. Every step keeps the rounding contract. The differential property tests that compare `BasicMath` with `BigDecimal` for every rounding mode make each step safe, and these benchmarks show whether it helped.
