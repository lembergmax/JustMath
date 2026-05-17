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

package com.mlprograms.justmath.calculator.errors;

import com.mlprograms.justmath.exceptions.CustomExceptionMessages;
import lombok.Getter;
import lombok.NonNull;

/**
 * Strongly typed catalogue of every failure cause that can be reported by the
 * {@code CalculatorEngine}.
 *
 * <p>
 * Each code carries:
 * </p>
 * <ul>
 *   <li>a <em>bundle key</em> resolved against
 *       {@code i18n/calculator_errors*.properties} (see {@link #getBundleKey()}), and</li>
 *   <li>a <em>category</em> from {@link CustomExceptionMessages} that maps the new code back
 *       to the legacy {@code SyntaxErrorException}/{@code ProcessingErrorException} message
 *       contract so that {@link Throwable#getMessage()} remains backwards compatible.</li>
 * </ul>
 *
 * <p>
 * These codes replace the previous "stringly typed" approach of throwing free-form English
 * detail strings: callers can now branch on a structural value instead of parsing English
 * text fragments.
 * </p>
 */
@Getter
public enum CalculatorErrorCode {

    /**
     * The expression contains a character that the tokenizer does not recognize
     * (for example {@code "2#3"}). Parameters: {@code character}, {@code position}.
     */
    SYNTAX_INVALID_CHARACTER("error.syntax.invalidCharacter", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * A function call or sub-expression is missing its closing parenthesis.
     * Parameters: {@code function} (optional).
     */
    SYNTAX_MISSING_RIGHT_PAREN("error.syntax.missingRightParen", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * Mismatched parentheses (a stray opening or closing parenthesis that has no partner).
     */
    SYNTAX_UNMATCHED_PAREN("error.syntax.unmatchedParen", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * The expression references an unknown function or operator. Parameter: {@code function}.
     */
    SYNTAX_UNKNOWN_FUNCTION("error.syntax.unknownFunction", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * The expression uses a variable that was not declared in the variable map.
     * Parameter: {@code variable}.
     */
    SYNTAX_UNKNOWN_VARIABLE("error.syntax.unknownVariable", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * The expression is incomplete — for example it has an odd number of absolute value
     * bars {@code |...} or ends unexpectedly.
     */
    SYNTAX_INCOMPLETE_EXPRESSION("error.syntax.incompleteExpression", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * The expression ends with a binary operator that has no right-hand operand
     * (for example {@code "50000!/"}). Parameter: {@code operator}.
     */
    SYNTAX_TRAILING_OPERATOR("error.syntax.trailingOperator", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * An operator or function is missing one of its operands during evaluation (stack
     * underflow). Parameter: {@code operator}.
     */
    SYNTAX_MISSING_OPERAND("error.syntax.missingOperand", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * The expression did not reduce to a single result (dangling operands / missing
     * operator between sub-expressions).
     */
    SYNTAX_UNEXPECTED_END("error.syntax.unexpectedEnd", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * A function was called with the wrong number of arguments.
     * Parameters: {@code function}, {@code expected}, {@code actual} (when available).
     */
    SYNTAX_WRONG_ARGUMENT_COUNT("error.syntax.wrongArgumentCount", "error.category.argument", CustomExceptionMessages.ARGUMENT_ERROR),

    /**
     * A variadic function received an argument count that it cannot accept.
     * Parameters: {@code function}, {@code expected}, {@code actual}.
     */
    ARGUMENT_COUNT_MISMATCH("error.argument.countMismatch", "error.category.argument", CustomExceptionMessages.ARGUMENT_ERROR),

    /**
     * The argument separator ({@code ;}) appears outside of a function call or in a
     * position that is otherwise invalid.
     */
    SYNTAX_MISPLACED_SEPARATOR("error.syntax.misplacedSeparator", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * The factorial operator {@code !} appears in a position where it cannot be applied.
     */
    SYNTAX_INVALID_FACTORIAL("error.syntax.invalidFactorial", "error.category.syntax", CustomExceptionMessages.SYNTAX_ERROR),

    /**
     * The evaluator attempted to divide by zero.
     */
    PROCESSING_DIVISION_BY_ZERO("error.processing.divisionByZero", "error.category.math", CustomExceptionMessages.MATH_ERROR),

    /**
     * The value passed to a function is outside the function's mathematical domain
     * (for example {@code sqrt(-4)} over the reals).
     */
    PROCESSING_DOMAIN_ERROR("error.processing.domainError", "error.category.math", CustomExceptionMessages.MATH_ERROR),

    /**
     * Factorial applied to a negative value. {@code !} is undefined for negatives over
     * the reals.
     */
    MATH_FACTORIAL_NEGATIVE("error.math.factorialNegative", "error.category.math", CustomExceptionMessages.MATH_ERROR),

    /**
     * Factorial applied to a non-integer value.
     */
    MATH_FACTORIAL_NON_INTEGER("error.math.factorialNonInteger", "error.category.math", CustomExceptionMessages.MATH_ERROR),

    /**
     * A logarithm received a non-positive argument or an invalid base.
     */
    MATH_LOG_NON_POSITIVE("error.math.logNonPositive", "error.category.math", CustomExceptionMessages.MATH_ERROR),

    /**
     * An even root (e.g. {@code sqrt}) was applied to a negative number over the reals.
     */
    MATH_ROOT_OF_NEGATIVE("error.math.rootOfNegative", "error.category.math", CustomExceptionMessages.MATH_ERROR),

    /**
     * The computation exceeded the supported numeric range (overflow / value too large).
     */
    MATH_OVERFLOW("error.math.overflow", "error.category.range", CustomExceptionMessages.RANGE_ERROR),

    /**
     * Fallback code used for processing failures that cannot be classified more precisely.
     */
    PROCESSING_INTERNAL("error.processing.internal", "error.category.internal", CustomExceptionMessages.PROCESSING_ERROR);

    /**
     * Top-level generic bundle key — the last localized fallback before the technical
     * detail string is used.
     */
    public static final String GENERIC_BUNDLE_KEY = "error.generic";

    /**
     * Key used to look up the localized template inside
     * {@code i18n/calculator_errors*.properties}.
     */
    @NonNull
    private final String bundleKey;

    /**
     * Casio-style category bundle key resolved when the specific {@link #bundleKey} is
     * missing (mid-tier fallback). See {@link com.mlprograms.justmath.calculator.errors.CalculatorError#format}.
     */
    @NonNull
    private final String categoryBundleKey;

    /**
     * Legacy category that this code maps onto. Determines the stable
     * {@link Throwable#getMessage()} text exposed by
     * {@link com.mlprograms.justmath.calculator.exceptions.SyntaxErrorException}
     * and {@link com.mlprograms.justmath.calculator.exceptions.ProcessingErrorException}.
     */
    @NonNull
    private final CustomExceptionMessages category;

    CalculatorErrorCode(
            @NonNull final String bundleKey,
            @NonNull final String categoryBundleKey,
            @NonNull final CustomExceptionMessages category
    ) {
        this.bundleKey = bundleKey;
        this.categoryBundleKey = categoryBundleKey;
        this.category = category;
    }

}
