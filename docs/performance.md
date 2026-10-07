# Performance

`BigNumber` computes with `BigDecimal` and `BigInteger`. For numbers of 100 digits and more it is within a factor of 1.0 to 4.3 of `BigDecimal` for one operation, and within 1.2 to 1.6 when reading the operands from text and writing the result as text is counted. For numbers of 10 digits the fixed cost of the object shows, and it is 3 to 9 times slower. Before this version the arithmetic ran on digit strings and was 90 to 2400 times slower; the table shows both. This page has the numbers, the method, what was slow and what remains.

## What was measured

The JMH benchmarks in `benchmarks/` run the same operation on the same operands with `BigNumber` and with `BigDecimal` (and `BigInteger` for the factorial):

- `ArithmeticBenchmark`: add, multiply, divide, an integer power with exponent 20, and the factorial of `n` equal to the number of digits. The operands are random decimals of 10, 100 and 1000 digits with half of the digits after the decimal point. Division uses a `MathContext` with as many digits as the operands. The benchmarks ending in `FromTextToText` read both operands from text and write the result as text, for both types.
- `EngineBenchmark`: `CalculatorEngine.evaluate` for a short expression (`2+3*4-5/2`) and a longer one (`sqrt(16)*2^10+sin(30)*cos(60)+10!/8!-summation(1;20;k^2)`), with the expression cache on and off.

Environment of the numbers below: Intel Core Ultra 7 155H, 15.6 GB, Windows 11, Oracle JDK 21.0.9, JMH 1.37, one fork, 3 warm-up and 5 measured iterations of one second each, nothing else running. The expression benchmarks were measured again with 5 warm-up and 8 measured iterations, because the first run had a large error. Times are the average per operation in microseconds with the error of the 99.9 % confidence interval. Absolute numbers depend on the machine; the ratios are what matters. The column "Before" is the digit-string implementation, measured with the same benchmarks on the same machine.

## Results

### One operation

The operands are numbers that exist already. The result is not written as text: it holds its value as a `BigDecimal` and writes its digits down when somebody asks for them (see [decision 0008](decisions/0008-bigdecimal-arithmetic-and-lazy-digits.md)).

| Operation | Digits | Before (µs) | Now (µs) | BigDecimal (µs) | Faster than before | Slower than BigDecimal |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| add | 10 | 0.979 | 0.011 ± 0.002 | 0.003 ± 0.000 | 92x | 3.2x |
| add | 100 | 5.955 | 0.024 ± 0.006 | 0.019 ± 0.004 | 246x | 1.3x |
| add | 1000 | 99.926 | 0.103 ± 0.015 | 0.083 ± 0.014 | 966x | 1.3x |
| multiply | 10 | 1.185 | 0.049 ± 0.002 | 0.013 ± 0.002 | 24x | 3.7x |
| multiply | 100 | 27.442 | 0.299 ± 0.049 | 0.069 ± 0.013 | 92x | 4.3x |
| multiply | 1000 | 2081.246 | 7.413 ± 1.316 | 2.773 ± 0.836 | 281x | 2.7x |
| divide | 10 | 3.922 | 0.103 ± 0.019 | 0.012 ± 0.002 | 38x | 8.9x |
| divide | 100 | 184.613 | 0.661 ± 0.100 | 0.523 ± 0.133 | 279x | 1.3x |
| divide | 1000 | 18242.597 | 22.042 ± 2.407 | 19.119 ± 4.947 | 828x | 1.2x |
| power (exponent 20) | 10 | 79.911 | 0.484 ± 0.031 | 0.143 ± 0.032 | 165x | 3.4x |
| power (exponent 20) | 100 | 8121.323 | 6.002 ± 0.281 | 3.633 ± 0.384 | 1353x | 1.7x |
| power (exponent 20) | 1000 | 748674.730 | 329.357 ± 73.681 | 314.707 ± 59.595 | 2273x | 1.0x |
| factorial of n = digits | 10 | 0.873 | 0.097 ± 0.010 | 0.127 ± 0.008 | 9.0x | 0.8x |
| factorial of n = digits | 100 | 31.218 | 1.458 ± 0.134 | 2.215 ± 0.131 | 21x | 0.7x |
| factorial of n = digits | 1000 | 5820.609 | 20.180 ± 0.960 | 117.282 ± 10.427 | 288x | 0.2x |

The factorial is compared with a plain product of `BigInteger` values, not with a product tree. `BigNumber` uses a product tree, so it is faster.

### From text to text

A caller usually reads numbers from text and writes the result as text. These benchmarks include both for `BigNumber` and for `BigDecimal`.

| Operation | Digits | BigNumber (µs) | BigDecimal (µs) | Ratio |
| --- | ---: | ---: | ---: | ---: |
| add | 10 | 0.518 ± 0.046 | 0.076 ± 0.016 | 6.8x |
| add | 100 | 3.401 ± 0.239 | 2.121 ± 0.272 | 1.6x |
| add | 1000 | 63.114 ± 15.430 | 52.061 ± 2.422 | 1.2x |
| multiply | 10 | 0.856 ± 0.097 | 0.255 ± 0.021 | 3.4x |
| multiply | 100 | 5.266 ± 0.764 | 3.756 ± 0.270 | 1.4x |
| multiply | 1000 | 110.394 ± 11.747 | 92.755 ± 6.070 | 1.2x |
| divide | 10 | 0.657 ± 0.048 | 0.082 ± 0.005 | 8.0x |
| divide | 100 | 3.979 ± 0.130 | 2.694 ± 0.179 | 1.5x |
| divide | 1000 | 90.222 ± 16.900 | 76.768 ± 8.414 | 1.2x |

From 100 digits on, the conversion between text and numbers is most of the time, and both types pay it. `BigNumber` reads a number with locale detection and validation, which is the difference at 10 digits.

### Expression engine

| Expression | Expression cache | Before (µs) | Now (µs) |
| --- | --- | ---: | ---: |
| short | on | 3.48 | 2.59 ± 0.07 |
| short | off | 4.32 | 3.45 ± 0.09 |
| long | on | 248.90 | 184.78 ± 6.53 |
| long | off | 255.31 | 191.27 ± 13.24 |

The expression cache saves the tokenizer: 25 % for the short expression and nothing visible for the long one. In the long expression the time goes into `sin`, `cos`, `sqrt` and the summation, which call big-math with guard digits, and not into the arithmetic.

## Where the time went

The profile of the digit-string implementation, taken with JDK Flight Recorder around a plain loop over one operation (6 seconds per operation after a 2 second warm-up), named four causes:

| Operation | Digits | Where the samples were |
| --- | ---: | --- |
| add | 1000 | 85 % in `BasicMath.parseFromBigNumber`: 58 % in `BigDecimal.toPlainString` (the conversion of a `BigInteger` to text) and 24 % in `new BigDecimal(String)`. |
| multiply | 100 | 76 % in `BasicMath.multiplyUnsigned`, 12 % in `parseFromBigNumber`, 8 % in `new BigNumber(String, Locale)` for the result. |
| divide | 100 | 98 % in the long division: 65 % in `estimateQuotientDigit` and 28 % in `subtractProductFromRemainder`. |
| power | 100 | 99.6 % in `multiplyUnsigned`. |

1. **The operands took a detour through `BigDecimal`.** `parseFromBigNumber` converted a `BigNumber` to `toBigDecimal().toPlainString()` and parsed that text again. Converting between decimal text and a `BigInteger` is quadratic in the number of digits: at 1000 digits `new BigDecimal(String)` takes 13.7 µs and `BigDecimal.toPlainString()` 25.5 µs, while `BigDecimal.add` takes 0.2 µs.
2. **The multiplication had one decimal digit per step**, with a division and a remainder by 10 in the inner loop.
3. **The division found one quotient digit per step by trial.**
4. **A value was two strings.** Every result was built as text and parsed and validated again by the constructor.

An experiment that fixed only the first two causes and kept the value as text made an addition of 1000 digits six times faster and a multiplication of 1000 digits 17 times faster, and left both far above `BigDecimal` (about 200 and 50 times), because every result was still built as text. That is why the change went further.

## What changed

- Add, subtract, multiply, divide, remainder, modulo, integer powers and the factorial run on `BigDecimal` and `BigInteger`. The factorial is a product tree.
- A `BigNumber` holds its value as a `BigDecimal` and writes its digits down only when they are asked for. A chain of calculations never converts to text between the steps, and `toBigDecimal()` keeps its value instead of parsing the digits on every call.
- Division multiplies the unscaled dividend by a power of ten, divides once, and lets `BigDecimal.round` apply the rounding mode. For two small numbers at a high precision that is faster than `BigDecimal.divide(BigDecimal, MathContext)`, which removes the zeros of an exact quotient one division at a time.
- The trailing zeros of a result are removed one division at a time when there are few, and in one step when there are many.
- The results are the same as before, digit for digit, scale for scale and exception for exception. [Decision 0008](decisions/0008-bigdecimal-arithmetic-and-lazy-digits.md) has the reasons and the evidence.

## What is still slower

- **Small numbers.** At 10 digits an operation costs 3 to 9 times what `BigDecimal` costs: 0.01 to 0.1 µs against 0.003 to 0.012 µs. A `BigNumber` is an object with a locale, a `MathContext` and two views of its value, and a result removes its trailing zeros.
- **Multiplication.** 2.7 to 4.3 times `BigDecimal`. The product is reduced to the smallest scale, which `BigDecimal` leaves to the caller.
- **Text.** Reading a number checks and normalizes it for every locale that `BigNumber` understands.
- **The functions.** `sin`, `cos`, `sqrt` and the other functions run on big-math with the guard digits that the accuracy contract needs. They take 25 to 110 µs at 100 digits.

If you need exact decimal arithmetic in a tight loop of small numbers, `BigDecimal` is still faster. If you need the functions, the engine, the units or the rounding contract, the cost per call is in the range of tens of nanoseconds to microseconds for numbers of up to 100 digits.

## Reproduce

```bash
./mvnw -DskipTests install
./mvnw -f benchmarks/pom.xml package
java -jar benchmarks/target/benchmarks.jar -rf json -rff results.json
```

A single benchmark: `java -jar benchmarks/target/benchmarks.jar ArithmeticBenchmark.bigNumberDivide -p digits=100`. Close other programs first: a build or an IDE that runs in the background changes the numbers.
