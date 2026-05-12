/*
 * Copyright (c) 2025-2026 Max Lemberg
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

package com.mlprograms.justmath.calculator;

import static com.mlprograms.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;
import static com.mlprograms.justmath.calculator.CalculatorEngineUtils.*;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.errors.CalculatorError;
import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.calculator.errors.CalculatorResult;
import com.mlprograms.justmath.calculator.errors.ErrorMode;
import com.mlprograms.justmath.calculator.exceptions.CalculatorException;
import com.mlprograms.justmath.calculator.exceptions.SyntaxErrorException;
import com.mlprograms.justmath.calculator.internal.Token;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;

import java.math.MathContext;
import java.util.*;

import lombok.Getter;
import lombok.NonNull;

/**
 * Main entry point for evaluating arbitrary-precision mathematical expressions supplied as
 * strings.
 *
 * <p>
 * The engine converts the input into tokens, parses them into postfix notation (Reverse
 * Polish Notation) and evaluates the resulting sequence. It supports user-defined variables,
 * {@link BigNumber} arithmetic, localized error messages and an optional cache of tokenized
 * expressions for hot, repetitive workloads.
 * </p>
 *
 * <p>
 * <strong>Backwards compatibility:</strong> {@link #evaluateToString(String)} and
 * {@link #evaluateToPrettyString(String)} continue to return {@code e.getMessage()} for the
 * default {@link ErrorMode#RAW} (for example {@code "Syntax Error"}). Localized error output
 * is enabled by calling {@link #setLocale(Locale)} together with
 * {@link #setErrorMode(ErrorMode)}, or by using one of the {@code evaluateSafe(...)}
 * overloads that return a {@link CalculatorResult} for explicit error handling.
 * </p>
 */
@Getter
public class CalculatorEngine {

    /**
     * Thread-local snapshot of the currently active variables for nested evaluations. The
     * value is restored via try/finally in {@link #evaluate(String, Map)} so that no
     * variable state leaks between consecutive invocations on the same thread.
     */
    private static final ThreadLocal<Map<String, String>> currentVariables = ThreadLocal.withInitial(HashMap::new);

    /**
     * Tokenizer used for lexical analysis of the input expression.
     */
    private final Tokenizer tokenizer;

    /**
     * Evaluator that consumes the postfix token list and produces the {@link BigNumber} result.
     */
    private final Evaluator evaluator;

    /**
     * Parser that converts infix tokens to postfix notation.
     */
    private final PostfixParser postfixParser;

    /**
     * Locale used to render localized error messages. Defaults to {@link Locale#ENGLISH}.
     */
    @NonNull
    private Locale locale = Locale.ENGLISH;

    /**
     * Current error formatting mode. Defaults to {@link ErrorMode#RAW}, which preserves the
     * behaviour of older releases that pre-date the localization layer.
     */
    @NonNull
    private ErrorMode errorMode = ErrorMode.RAW;

    /**
     * Whether the token cache is enabled. Defaults to {@code false}.
     */
    private boolean expressionCacheEnabled = false;

    /**
     * Maximum size (LRU capacity) of the token cache. Defaults to {@code 128}.
     */
    private int expressionCacheSize = 128;

    /**
     * Lazily initialized LRU cache for tokenized expressions. Keys are the normalized
     * expression strings (after {@code replaceAbsSigns}); values are immutable token lists
     * captured <em>before</em> variable substitution.
     *
     * <p>
     * The postfix form is deliberately not cached: variable substitution mutates the token
     * list in place, and a postfix form built before substitution would still reference the
     * original {@code VARIABLE} tokens. Tokenization is the more expensive step, so caching
     * tokens already eliminates the bulk of the cost while keeping the cache content
     * variable-agnostic and therefore correct.
     * </p>
     */
    private Map<String, List<Token>> expressionCache;

    /**
     * Default constructor that uses {@link BigNumbers#DEFAULT_DIVISION_PRECISION} and
     * {@link TrigonometricMode#DEG}.
     */
    public CalculatorEngine() {
        this(getDefaultMathContext(DEFAULT_DIVISION_PRECISION), TrigonometricMode.DEG);
    }

    /**
     * Creates an engine with an explicit division precision and the default trigonometric mode.
     *
     * @param divisionPrecision precision used for division operations
     */
    public CalculatorEngine(int divisionPrecision) {
        this(getDefaultMathContext(divisionPrecision), TrigonometricMode.DEG);
    }

    /**
     * Creates an engine with an explicit division precision and trigonometric mode.
     *
     * @param divisionPrecision precision used for division operations
     * @param trigonometricMode trigonometric mode (DEG, RAD, GRAD); must not be {@code null}
     */
    public CalculatorEngine(int divisionPrecision, @NonNull TrigonometricMode trigonometricMode) {
        this(getDefaultMathContext(divisionPrecision), trigonometricMode);
    }

    /**
     * Creates an engine with the given {@link MathContext} and the default trigonometric mode.
     *
     * @param mathContext math context; must not be {@code null}
     */
    public CalculatorEngine(@NonNull MathContext mathContext) {
        this(mathContext, TrigonometricMode.DEG);
    }

    /**
     * Creates an engine with the given trigonometric mode and the default math context.
     *
     * @param trigonometricMode trigonometric mode; must not be {@code null}
     */
    public CalculatorEngine(@NonNull TrigonometricMode trigonometricMode) {
        this(getDefaultMathContext(DEFAULT_DIVISION_PRECISION), trigonometricMode);
    }

    /**
     * Canonical constructor.
     *
     * @param mathContext       math context controlling precision and rounding; must not be {@code null}
     * @param trigonometricMode trigonometric mode; must not be {@code null}
     */
    public CalculatorEngine(@NonNull MathContext mathContext, @NonNull TrigonometricMode trigonometricMode) {
        this.tokenizer = new Tokenizer();
        this.evaluator = new Evaluator(mathContext, trigonometricMode);
        this.postfixParser = new PostfixParser();
    }

    /**
     * Returns a defensive copy of the variables currently active on the calling thread.
     *
     * @return copy of the active variables; never {@code null}
     */
    public static Map<String, String> getCurrentVariables() {
        return new HashMap<>(currentVariables.get());
    }

    /**
     * Sets the locale used for localized error messages.
     *
     * @param locale target locale; must not be {@code null}
     * @return this engine for builder-style chaining
     */
    public CalculatorEngine setLocale(@NonNull final Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Sets the error formatting mode.
     *
     * @param errorMode {@link ErrorMode#RAW} or {@link ErrorMode#USER_FRIENDLY}; must not be {@code null}
     * @return this engine for builder-style chaining
     */
    public CalculatorEngine setErrorMode(@NonNull final ErrorMode errorMode) {
        this.errorMode = errorMode;
        return this;
    }

    /**
     * Enables or disables the expression cache. Disabling clears the cache.
     *
     * @param enabled {@code true} to enable caching of tokenized expressions
     * @return this engine for builder-style chaining
     */
    public CalculatorEngine setExpressionCacheEnabled(final boolean enabled) {
        this.expressionCacheEnabled = enabled;
        if (!enabled) {
            this.expressionCache = null;
        }
        return this;
    }

    /**
     * Sets the maximum size of the expression cache. The existing cache is discarded so that
     * the new capacity takes effect on the next access.
     *
     * @param size new maximum number of cached entries; must be greater than zero
     * @return this engine for builder-style chaining
     * @throws IllegalArgumentException if {@code size} is not strictly positive
     */
    public CalculatorEngine setExpressionCacheSize(final int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("expressionCacheSize must be positive");
        }
        this.expressionCacheSize = size;
        this.expressionCache = null;
        return this;
    }

    /**
     * Evaluates an expression with no user-defined variables.
     *
     * @param expression input expression; must not be {@code null}
     * @return the result as a {@link BigNumber}
     */
    public BigNumber evaluate(@NonNull String expression) {
        return evaluate(expression, Map.of());
    }

    /**
     * Evaluates an expression in the context of the given variable map.
     *
     * <p>
     * The thread-local variable context is captured before the call and restored via
     * try/finally afterwards, so that nested evaluations and subsequent calls on the same
     * thread never observe leaked state — even when this method throws.
     * </p>
     *
     * @param expression input expression; must not be {@code null}
     * @param variables  variable bindings (name → expression); must not be {@code null}
     * @return the result as a {@link BigNumber}
     */
    public BigNumber evaluate(@NonNull final String expression, @NonNull final Map<String, String> variables) {
        if (expression.isBlank()) {
            return BigNumbers.ZERO;
        }

        Map<String, String> previous = currentVariables.get();
        Map<String, String> combinedVariables = new HashMap<>(previous);
        combinedVariables.putAll(variables);
        currentVariables.set(combinedVariables);

        try {
            String normalized;
            try {
                normalized = replaceAbsSigns(expression);
            } catch (IllegalArgumentException iae) {
                throw new SyntaxErrorException(
                        CalculatorErrorCode.SYNTAX_INCOMPLETE_EXPRESSION,
                        Map.of(),
                        iae.getMessage() == null ? "Incomplete expression" : iae.getMessage(),
                        null);
            }

            List<Token> tokens;
            List<Token> cachedTokens = expressionCacheEnabled ? lookupCache(normalized) : null;
            if (cachedTokens != null) {
                tokens = new ArrayList<>(cachedTokens);
            } else {
                tokens = tokenizer.tokenize(normalized);
                if (expressionCacheEnabled) {
                    storeCache(normalized, List.copyOf(tokens));
                }
            }

            try {
                replaceVariables(this, tokens, combinedVariables);
            } catch (IllegalArgumentException iae) {
                String msg = Objects.requireNonNullElse(iae.getMessage(), "Variable is not defined");
                String variableName = extractVariableName(msg);
                throw new SyntaxErrorException(
                        CalculatorErrorCode.SYNTAX_UNKNOWN_VARIABLE,
                        variableName == null ? Map.of() : Map.of("variable", variableName),
                        msg,
                        null);
            }

            List<Token> postfix = postfixParser.toPostfix(tokens);
            return evaluator.evaluate(postfix).trim();
        } finally {
            if (previous.isEmpty()) {
                currentVariables.remove();
            } else {
                currentVariables.set(previous);
            }
        }
    }

    /**
     * Evaluates an expression and returns its result as a string. Errors are reported as
     * human-readable strings instead of being thrown.
     *
     * <p>
     * In the default {@link ErrorMode#RAW} the returned text matches the behaviour from
     * before the localization layer was introduced — typically the category default such as
     * {@code "Syntax Error"} or {@code "Processing Error"}. In {@link ErrorMode#USER_FRIENDLY}
     * the localized message from the configured bundle is returned instead.
     * </p>
     *
     * @param expression input expression; must not be {@code null}
     * @return result as a string, or an error message if evaluation failed
     */
    public String evaluateToString(@NonNull String expression) {
        return evaluateToString(expression, Map.of());
    }

    /**
     * Evaluates an expression with the given variable map and returns its result as a string.
     *
     * @param expression input expression; must not be {@code null}
     * @param variables  variable bindings; must not be {@code null}
     * @return result as a string, or an error message if evaluation failed
     */
    public String evaluateToString(@NonNull final String expression, @NonNull final Map<String, String> variables) {
        try {
            BigNumber result = evaluate(expression, variables);
            return result.toString();
        } catch (CalculatorException e) {
            return formatExceptionMessage(e);
        } catch (Exception e) {
            return Objects.requireNonNullElse(e.getMessage(), "Syntax Error");
        }
    }

    /**
     * Evaluates an expression and returns the result formatted for human consumption.
     *
     * @param expression input expression; must not be {@code null}
     * @return formatted result, or an error message if evaluation failed
     */
    public String evaluateToPrettyString(@NonNull String expression) {
        return evaluateToPrettyString(expression, Map.of());
    }

    /**
     * Evaluates an expression with the given variable map and returns the result formatted
     * for human consumption.
     *
     * @param expression input expression; must not be {@code null}
     * @param variables  variable bindings; must not be {@code null}
     * @return formatted result, or an error message if evaluation failed
     */
    public String evaluateToPrettyString(@NonNull final String expression, @NonNull final Map<String, String> variables) {
        try {
            BigNumber result = evaluate(expression, variables);
            return result.toPrettyString();
        } catch (CalculatorException e) {
            return formatExceptionMessage(e);
        } catch (Exception e) {
            return Objects.requireNonNullElse(e.getMessage(), "Syntax Error");
        }
    }

    /**
     * Evaluates an expression and returns a {@link CalculatorResult} so that callers can
     * branch on success and failure without using exceptions on the happy path.
     *
     * @param expression input expression; must not be {@code null}
     * @return success or failure result; never {@code null}
     */
    public CalculatorResult<BigNumber> evaluateSafe(@NonNull final String expression) {
        return evaluateSafe(expression, Map.of());
    }

    /**
     * Evaluates an expression with the given variable map and returns a {@link CalculatorResult}.
     *
     * @param expression input expression; must not be {@code null}
     * @param variables  variable bindings; must not be {@code null}
     * @return success or failure result; never {@code null}
     */
    public CalculatorResult<BigNumber> evaluateSafe(
            @NonNull final String expression,
            @NonNull final Map<String, String> variables
    ) {
        try {
            BigNumber result = evaluate(expression, variables);
            return CalculatorResult.success(result);
        } catch (CalculatorException ex) {
            CalculatorError err = ex.getCalculatorError();
            if (err == null) {
                err = new CalculatorError(
                        CalculatorErrorCode.PROCESSING_INTERNAL,
                        Objects.requireNonNullElse(ex.getDetailedMessage(), "Unknown processing error"));
            }
            return CalculatorResult.failure(err);
        } catch (Exception ex) {
            CalculatorError err = new CalculatorError(
                    CalculatorErrorCode.PROCESSING_INTERNAL,
                    Objects.requireNonNullElse(ex.getMessage(), "Unknown error"));
            return CalculatorResult.failure(err);
        }
    }

    /**
     * Formats a {@link CalculatorException} according to the current {@link ErrorMode}. In
     * {@link ErrorMode#RAW} the legacy {@link Throwable#getMessage()} value is returned (the
     * category default such as {@code "Syntax Error"}); in {@link ErrorMode#USER_FRIENDLY}
     * the localized template from the resource bundle is used.
     *
     * @param e exception to format; must not be {@code null}
     * @return formatted message; never {@code null}
     */
    private String formatExceptionMessage(@NonNull final CalculatorException e) {
        if (errorMode == ErrorMode.USER_FRIENDLY) {
            CalculatorError err = e.getCalculatorError();
            if (err == null) {
                err = new CalculatorError(
                        CalculatorErrorCode.PROCESSING_INTERNAL,
                        Objects.requireNonNullElse(e.getDetailedMessage(), e.getMessage()));
            }
            return err.format(locale, ErrorMode.USER_FRIENDLY);
        }
        return Objects.requireNonNullElse(e.getMessage(), "Syntax Error");
    }

    /**
     * Extracts the variable name from the legacy detail message
     * {@code "Variable 'x' is not defined."} by locating the apostrophe-delimited token.
     *
     * @param message source message; must not be {@code null}
     * @return the extracted variable name, or {@code null} if the message does not match the
     * expected pattern
     */
    private static String extractVariableName(@NonNull final String message) {
        int first = message.indexOf('\'');
        int last = message.lastIndexOf('\'');
        if (first >= 0 && last > first) {
            return message.substring(first + 1, last);
        }
        return null;
    }

    /**
     * Looks up a cached token list, initializing the cache lazily if necessary.
     *
     * @param key normalized expression string; must not be {@code null}
     * @return cached token list, or {@code null} if no entry exists
     */
    private synchronized List<Token> lookupCache(@NonNull final String key) {
        ensureCache();
        return expressionCache.get(key);
    }

    /**
     * Stores an immutable token list in the cache, initializing it lazily if necessary.
     *
     * @param key   normalized expression string; must not be {@code null}
     * @param value immutable token list captured before variable substitution; must not be {@code null}
     */
    private synchronized void storeCache(@NonNull final String key, @NonNull final List<Token> value) {
        ensureCache();
        expressionCache.put(key, value);
    }

    /**
     * Lazily creates the LRU cache. The capacity is captured once at creation time;
     * subsequent calls to {@link #setExpressionCacheSize(int)} discard the existing cache so
     * that this method can rebuild it with the new capacity.
     */
    private void ensureCache() {
        if (expressionCache == null) {
            final int cap = expressionCacheSize;
            expressionCache = Collections.synchronizedMap(new LinkedHashMap<>(cap, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, List<Token>> eldest) {
                    return size() > cap;
                }
            });
        }
    }
}
