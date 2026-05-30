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

package io.github.lembergmax.justmath.calculator.errors;

import java.util.Optional;
import java.util.function.Function;

import io.github.lembergmax.justmath.calculator.exceptions.CalculatorException;
import io.github.lembergmax.justmath.calculator.exceptions.ProcessingErrorException;
import io.github.lembergmax.justmath.calculator.exceptions.SyntaxErrorException;
import io.github.lembergmax.justmath.exceptions.CustomExceptionMessages;
import lombok.NonNull;

/**
 * Outcome of a {@code evaluateSafe} invocation — either a {@link Success} that carries the
 * computed value or a {@link Failure} that carries a {@link CalculatorError}.
 *
 * <p>
 * This type lets callers branch on success and failure without using exceptions on the
 * happy path. Example:
 * </p>
 * <pre>{@code
 * CalculatorResult<BigNumber> result = engine.evaluateSafe("5/0");
 * if (result.isFailure()) {
 *     CalculatorError err = result.error().orElseThrow();
 *     String localized = err.format(Locale.GERMAN, ErrorMode.USER_FRIENDLY);
 * }
 * }</pre>
 *
 * @param <T> the type of the successful value
 */
public sealed interface CalculatorResult<T> permits CalculatorResult.Success, CalculatorResult.Failure {

    /**
     * Creates a successful result.
     *
     * @param value the computed value; must not be {@code null}
     * @param <T>   the type of the value
     * @return a {@link Success} wrapping {@code value}; never {@code null}
     */
    static <T> CalculatorResult<T> success(@NonNull final T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failure result.
     *
     * @param error the error describing the failure; must not be {@code null}
     * @param <T>   the type of the expected value
     * @return a {@link Failure} wrapping {@code error}; never {@code null}
     */
    static <T> CalculatorResult<T> failure(@NonNull final CalculatorError error) {
        return new Failure<>(error);
    }

    /**
     * @return {@code true} if this result is a {@link Success}; {@code false} otherwise
     */
    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    /**
     * @return {@code true} if this result is a {@link Failure}; {@code false} otherwise
     */
    default boolean isFailure() {
        return this instanceof Failure<T>;
    }

    /**
     * Returns the successful value, if present.
     *
     * @return an {@link Optional} containing the value for {@link Success}, otherwise empty
     */
    default Optional<T> value() {
        return this instanceof Success<T>(T successValue) ? Optional.of(successValue) : Optional.empty();
    }

    /**
     * Returns the failure descriptor, if present.
     *
     * @return an {@link Optional} containing the error for {@link Failure}, otherwise empty
     */
    default Optional<CalculatorError> error() {
        return this instanceof Failure<T>(CalculatorError failureError) ? Optional.of(failureError) : Optional.empty();
    }

    /**
     * Returns the successful value, or throws a {@link CalculatorException} that matches the
     * error code's category for the failure case.
     *
     * <p>
     * Failures whose code maps to {@link CustomExceptionMessages#SYNTAX_ERROR} are surfaced
     * as a {@link SyntaxErrorException}; everything else is surfaced as a
     * {@link ProcessingErrorException}. This preserves the exception class that callers
     * expect from the equivalent {@code evaluate} entry points.
     * </p>
     *
     * @return the successful value
     * @throws CalculatorException if this result is a {@link Failure}
     */
    default T valueOrThrow() {
        if (this instanceof Success<T>(T successValue)) {
            return successValue;
        }
        final CalculatorError err = ((Failure<T>) this).failureError();
        if (err.code().getCategory() == CustomExceptionMessages.SYNTAX_ERROR) {
            throw new SyntaxErrorException(err.code(), err.params(), err.technicalDetail(), err.position());
        }
        throw new ProcessingErrorException(err.code(), err.technicalDetail());
    }

    /**
     * Applies the given mapper to the successful value, leaving a {@link Failure} unchanged.
     *
     * @param mapper mapping function applied to the successful value; must not be {@code null}
     * @param <R>    the target type of the mapping
     * @return a new {@link CalculatorResult} with the mapped value, or the original failure
     */
    default <R> CalculatorResult<R> map(@NonNull final Function<? super T, ? extends R> mapper) {
        if (this instanceof Success<T>(T successValue)) {
            return new Success<>(mapper.apply(successValue));
        }
        return new Failure<>(((Failure<T>) this).failureError());
    }

    /**
     * Returns the {@link CustomExceptionMessages} category for the failure case. Useful for
     * consumers that still branch on the legacy category model rather than the new
     * {@link CalculatorErrorCode}.
     *
     * @return an {@link Optional} containing the category, or empty for {@link Success}
     */
    default Optional<CustomExceptionMessages> category() {
        return error().map(e -> e.code().getCategory());
    }

    /**
     * Successful result.
     *
     * @param successValue the computed value
     * @param <T>          the type of the value
     */
    record Success<T>(@NonNull T successValue) implements CalculatorResult<T> {
    }

    /**
     * Failure result.
     *
     * @param failureError the structured error descriptor
     * @param <T>          the type of the expected value
     */
    record Failure<T>(@NonNull CalculatorError failureError) implements CalculatorResult<T> {
    }

}
