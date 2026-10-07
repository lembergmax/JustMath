/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.lembergmax.justmath.benchmarks;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Compares the arithmetic of {@link BigNumber} with {@link BigDecimal} for operands of 10, 100 and 1000 digits.
 * Both sides compute the same value: the operands are random decimals with half of their digits after the decimal
 * point, division uses a {@link MathContext} with as many digits as the operands, and the factorial of {@code digits}
 * is compared with a product of {@link BigInteger} values.
 *
 * <p>The benchmarks that end in {@code FromTextToText} include what a caller pays around the operation: reading both
 * operands from text and writing the result as text.</p>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ArithmeticBenchmark {

    private static final long SEED = 20261006L;

    private static final int POWER_EXPONENT = 20;

    @Param({"10", "100", "1000"})
    public int digits;

    private BigNumber leftNumber;

    private BigNumber rightNumber;

    private BigNumber exponentNumber;

    private BigDecimal leftDecimal;

    private BigDecimal rightDecimal;

    private String leftText;

    private String rightText;

    private MathContext mathContext;

    @Setup
    public void setup() {
        final Random random = new Random(SEED + digits);
        leftDecimal = randomDecimal(random, digits);
        rightDecimal = randomDecimal(random, digits);
        leftNumber = new BigNumber(leftDecimal.toPlainString());
        leftText = leftDecimal.toPlainString();
        rightText = rightDecimal.toPlainString();
        rightNumber = new BigNumber(rightText);
        exponentNumber = BigNumber.valueOf(POWER_EXPONENT);
        mathContext = new MathContext(digits, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal randomDecimal(final Random random, final int digits) {
        final StringBuilder text = new StringBuilder(digits);
        text.append((char) ('1' + random.nextInt(9)));
        for (int index = 1; index < digits; index++) {
            text.append((char) ('0' + random.nextInt(10)));
        }
        return new BigDecimal(new BigInteger(text.toString()), digits / 2);
    }

    @Benchmark
    public BigNumber bigNumberAdd() {
        return leftNumber.add(rightNumber);
    }

    @Benchmark
    public BigDecimal bigDecimalAdd() {
        return leftDecimal.add(rightDecimal);
    }

    @Benchmark
    public BigNumber bigNumberMultiply() {
        return leftNumber.multiply(rightNumber);
    }

    @Benchmark
    public BigDecimal bigDecimalMultiply() {
        return leftDecimal.multiply(rightDecimal);
    }

    @Benchmark
    public BigNumber bigNumberDivide() {
        return leftNumber.divide(rightNumber, mathContext);
    }

    @Benchmark
    public BigDecimal bigDecimalDivide() {
        return leftDecimal.divide(rightDecimal, mathContext);
    }

    @Benchmark
    public String bigNumberAddFromTextToText() {
        return new BigNumber(leftText).add(new BigNumber(rightText)).toString();
    }

    @Benchmark
    public String bigDecimalAddFromTextToText() {
        return new BigDecimal(leftText).add(new BigDecimal(rightText)).toPlainString();
    }

    @Benchmark
    public String bigNumberMultiplyFromTextToText() {
        return new BigNumber(leftText).multiply(new BigNumber(rightText)).toString();
    }

    @Benchmark
    public String bigDecimalMultiplyFromTextToText() {
        return new BigDecimal(leftText).multiply(new BigDecimal(rightText)).toPlainString();
    }

    @Benchmark
    public String bigNumberDivideFromTextToText() {
        return new BigNumber(leftText).divide(new BigNumber(rightText), mathContext).toString();
    }

    @Benchmark
    public String bigDecimalDivideFromTextToText() {
        return new BigDecimal(leftText).divide(new BigDecimal(rightText), mathContext).toPlainString();
    }

    @Benchmark
    public BigNumber bigNumberPower() {
        return leftNumber.power(exponentNumber, mathContext);
    }

    @Benchmark
    public BigDecimal bigDecimalPower() {
        return leftDecimal.pow(POWER_EXPONENT);
    }

    @Benchmark
    public BigNumber bigNumberFactorial() {
        return BigNumber.valueOf(digits).factorial();
    }

    @Benchmark
    public BigInteger bigIntegerFactorial() {
        BigInteger product = BigInteger.ONE;
        for (int factor = 2; factor <= digits; factor++) {
            product = product.multiply(BigInteger.valueOf(factor));
        }
        return product;
    }
}
