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

package io.github.lembergmax.justmath.calculator;

import static io.github.lembergmax.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;
import static io.github.lembergmax.justmath.calculator.CalculatorEngineUtils.*;

import java.math.MathContext;
import java.text.DecimalFormatSymbols;
import java.util.*;

import io.github.lembergmax.justmath.bignumber.BigNumber;
import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.calculator.errors.CalculatorError;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.errors.CalculatorResult;
import io.github.lembergmax.justmath.calculator.errors.ErrorMode;
import io.github.lembergmax.justmath.calculator.exceptions.CalculatorException;
import io.github.lembergmax.justmath.calculator.exceptions.ProcessingErrorException;
import io.github.lembergmax.justmath.calculator.exceptions.SyntaxErrorException;
import io.github.lembergmax.justmath.calculator.internal.Token;
import io.github.lembergmax.justmath.calculator.internal.TrigonometricMode;
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
 * <h2>API families</h2>
 *
 * <p>
 * The engine exposes four families of {@code evaluate} methods. Pick one based on whether you
 * want a raw {@link BigNumber} or a {@link String}, and whether failures should be signalled
 * via an exception, an embedded error message or a typed {@link CalculatorResult}.
 * </p>
 *
 * <ol>
 *   <li><b>Core Evaluation API</b> — {@link #evaluate(String)}, {@link #evaluate(String, Map)}.<br>
 *       Returns a {@link BigNumber}. <em>Throws</em> {@link CalculatorException} on invalid
 *       input. Intended for internal pipelines and callers that already handle exceptions.</li>
 *
 *   <li><b>Text Output API</b> — {@link #evaluateToString(String)},
 *       {@link #evaluateToPrettyString(String)} (and their {@code Map}-variants).<br>
 *       Returns a locale-formatted {@link String}. <em>Does not throw</em>: failures are
 *       caught and folded into the return value as a message string (the exact text depends
 *       on the configured {@link ErrorMode}; see {@link #setErrorMode(ErrorMode)}). Inputs are
 *       {@code @NonNull} — passing {@code null} raises {@link NullPointerException}. Intended
 *       for simple output where the caller does not need to distinguish success from failure
 *       programmatically.</li>
 *
 *   <li><b>Safe UI String API</b> — {@link #evaluateSafeToString(String)},
 *       {@link #evaluateSafeToPrettyString(String)} (and their {@code Map}-variants).<br>
 *       Like the Text Output API but additionally null-tolerant: the expression and the
 *       variable map may be {@code null}. Never throws, never returns {@code null}. On failure
 *       the return value carries a locale-aware error prefix ({@code "Error: ..."} or
 *       {@code "Fehler: ..."}). Intended for UI labels, log lines and other fire-and-forget
 *       rendering. <em>Not</em> suitable when the caller has to branch on success/failure —
 *       use the Typed Result API for that.</li>
 *
 *   <li><b>Typed Result API</b> — {@link #evaluateSafe(String)},
 *       {@link #evaluateToStringResult(String)}, {@link #evaluateToPrettyStringResult(String)}
 *       (and their {@code Map}-variants).<br>
 *       Returns a {@link CalculatorResult}: {@link CalculatorResult#isSuccess()} carries the
 *       computed value, {@link CalculatorResult#isFailure()} carries a structured
 *       {@link CalculatorError}. No string-prefix parsing needed. {@link #evaluateSafe(String)}
 *       wraps a {@link BigNumber}; the {@code ...Result} variants wrap the locale-formatted
 *       {@link String}. Intended for application logic, tests and robust error handling.</li>
 * </ol>
 *
 * <h2>Locale handling</h2>
 *
 * <p>
 * {@link #setLocale(Locale)} controls two things only:
 * </p>
 * <ul>
 *   <li>the decimal/grouping separators used when formatting the result, and</li>
 *   <li>the language of error messages produced by {@link ErrorMode#USER_FRIENDLY} and the
 *       {@code "Error: " / "Fehler: "} prefix of the Safe UI String API.</li>
 * </ul>
 * <p>
 * The locale <em>does not</em> change the input grammar. Expressions are always parsed with
 * {@code .} as the decimal separator: {@code "1.5+1.5"} is valid under any locale, while
 * {@code "1,5+1,5"} is a syntax error because {@code ,} is reserved as an argument separator
 * (e.g. {@code summation(1;5;k)}). The {@code ToString} / {@code ToPrettyString} naming
 * mirrors the locale-aware output:
 * </p>
 * <ul>
 *   <li>{@code evaluateToString(...)} → locale-aware decimal separator, <em>no</em>
 *       thousands grouping. Example under {@link Locale#GERMANY}:
 *       {@code "1234.56"} → {@code "1234,56"}.</li>
 *   <li>{@code evaluateToPrettyString(...)} → locale-aware decimal separator <em>and</em>
 *       thousands grouping. Example under {@link Locale#GERMANY}:
 *       {@code "1234.56"} → {@code "1.234,56"}.</li>
 * </ul>
 * <p>
 * The default locale is {@link Locale#ENGLISH}, which preserves the legacy
 * {@code .}-as-decimal output of earlier releases.
 * </p>
 *
 * <h2>Backwards compatibility</h2>
 *
 * <p>
 * {@link #evaluateToString(String)} and {@link #evaluateToPrettyString(String)} continue to
 * return {@code e.getMessage()} for the default {@link ErrorMode#RAW} (for example
 * {@code "Syntax Error"}). Localized error output is enabled by calling
 * {@link #setLocale(Locale)} together with {@link #setErrorMode(ErrorMode)}, or by using the
 * Typed Result API.
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
     * Locale used for output formatting and error localization. It controls:
     * <ul>
     *   <li>the decimal separator used by {@code evaluateToString} / {@code evaluateSafeToString}
     *       / {@code evaluateToStringResult} (no grouping),</li>
     *   <li>the decimal <em>and</em> grouping separators used by
     *       {@code evaluateToPrettyString} / {@code evaluateSafeToPrettyString} /
     *       {@code evaluateToPrettyStringResult},</li>
     *   <li>the language of {@link ErrorMode#USER_FRIENDLY} messages and the
     *       {@code "Error: " / "Fehler: "} prefix used by the Safe UI String API.</li>
     * </ul>
     * <p>
     * Input parsing is <em>not</em> affected — expressions always use {@code .} as the decimal
     * separator regardless of this field. Defaults to {@link Locale#ENGLISH}, which preserves
     * the legacy {@code .}-as-decimal output of earlier releases.
     * </p>
     */
    @NonNull
    private Locale locale = Locale.ENGLISH;

    /**
     * Locale that governs how numeric literals in the <em>input</em> expression are parsed,
     * controlled via {@link #setInputLocale(Locale)} and independent of the output
     * {@link #locale}. Defaults to {@link Locale#US} ({@code '.'} decimal separator), which
     * preserves the legacy grammar in which {@code "1.5+1.5"} is valid and {@code "1,5+1,5"}
     * is a syntax error. Selecting a comma-decimal locale (e.g. {@link Locale#GERMANY}) makes
     * {@code ','} the decimal separator instead; the argument separator is always {@code ';'}.
     */
    @NonNull
    private Locale inputLocale = Locale.US;

    /**
     * Decimal separator derived from {@link #inputLocale}, cached so the hot evaluation path
     * does not resolve {@link DecimalFormatSymbols} on every call. Kept in sync by
     * {@link #setInputLocale(Locale)}. A getter is generated by the class-level {@code @Getter}.
     */
    private char inputDecimalSeparator = '.';

    /**
     * Current error formatting mode. Defaults to {@link ErrorMode#RAW}, which preserves the
     * behaviour of older releases that pre-date the localization layer.
     */
    @NonNull
    private ErrorMode errorMode = ErrorMode.RAW;

    /**
     * Whether the token cache is enabled. Defaults to {@code false}.
     *
     * <p>Declared {@code volatile} so that the unsynchronized read in
     * {@link #evaluate(String, Map)} observes the latest value written by
     * {@link #setExpressionCacheEnabled(boolean)} or {@link #setExpressionCacheSize(int)} on
     * another thread. The actual cache mutation (lookup / store / discard) is still serialized
     * through this engine's monitor — the {@code volatile} only fixes the visibility gap
     * between the setter and the fast-path enabled-check in callers.</p>
     */
    private volatile boolean expressionCacheEnabled = false;

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
     *
     * <p>Declared {@code volatile} together with {@link #expressionCacheEnabled} so that the
     * {@code null}-write performed by the cache-discarding setters becomes visible promptly
     * to other threads — even outside the synchronized lookup/store helpers.</p>
     */
    private volatile Map<String, List<Token>> expressionCache;

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
     * @param trigonometricMode trigonometric mode (DEG or RAD); must not be {@code null}
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
     * Sets the locale used for output formatting and error localization. The configured
     * locale is applied to:
     * <ul>
     *   <li>{@link #evaluateToString(String)}, {@link #evaluateSafeToString(String)} and
     *       {@link #evaluateToStringResult(String)} — locale-aware decimal separator,
     *       <em>no</em> thousands grouping
     *       (e.g. {@code "1234,56"} for {@link Locale#GERMANY},
     *       {@code "1234.56"} for {@link Locale#US}).</li>
     *   <li>{@link #evaluateToPrettyString(String)},
     *       {@link #evaluateSafeToPrettyString(String)} and
     *       {@link #evaluateToPrettyStringResult(String)} — locale-aware decimal separator
     *       <em>and</em> thousands grouping
     *       (e.g. {@code "1.234,56"} for {@link Locale#GERMANY},
     *       {@code "1,234.56"} for {@link Locale#US}).</li>
     *   <li>error messages emitted by {@link ErrorMode#USER_FRIENDLY} and the
     *       {@code "Error: " / "Fehler: "} prefix produced by the Safe UI String API.</li>
     * </ul>
     *
     * <p>
     * Input parsing is <strong>not</strong> affected — expressions are always parsed with
     * {@code .} as the decimal separator regardless of this setting. {@code "1.5+1.5"} is
     * valid under any locale; {@code "1,5+1,5"} is a syntax error because {@code ,} is
     * reserved as an argument separator (e.g. {@code summation(1;5;k)}).
     * </p>
     *
     * @param locale target locale; must not be {@code null}
     * @return this engine for builder-style chaining
     */
    public CalculatorEngine setLocale(@NonNull final Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Returns the languages JustMath ships translated messages and locale-aware formatting for.
     *
     * <p>
     * Consuming applications should use this list to decide which languages to offer their users,
     * rather than hard-coding their own: an application can only meaningfully offer a language that
     * JustMath itself supports. The returned list is the single source of truth maintained by
     * {@link SupportedLanguages}.
     * </p>
     *
     * @return an unmodifiable, non-empty list of supported locales; never {@code null}
     * @see SupportedLanguages#all()
     */
    public static List<Locale> getSupportedLanguages() {
        return SupportedLanguages.all();
    }

    /**
     * Reports whether JustMath advertises first-class support for the given locale.
     *
     * <p>
     * The check matches either the exact locale (language and country) or its language, so for
     * example {@code fr-CA} is reported as supported because French is supported. Locales that are
     * not supported still work — they fall back to English messages and JDK number formatting.
     * </p>
     *
     * @param locale the locale to test; must not be {@code null}
     * @return {@code true} if the locale or its language is supported, {@code false} otherwise
     * @see SupportedLanguages#isSupported(Locale)
     */
    public static boolean isLanguageSupported(@NonNull final Locale locale) {
        return SupportedLanguages.isSupported(locale);
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
     * Sets the locale used to parse numeric literals in the <em>input</em> expression. This is
     * independent of {@link #setLocale(Locale)}, which only affects output formatting and error
     * language.
     *
     * <p>
     * Parsing is <em>strict</em>: the decimal separator becomes the one defined by {@code inputLocale}
     * and only that character is accepted as a decimal point. For a comma-decimal locale such as
     * {@link Locale#GERMANY} or {@link Locale#FRANCE}, {@code "3,14"} is parsed as {@code 3.14} and a
     * literal {@code '.'} becomes an invalid character; for {@link Locale#US} (the default) {@code '.'}
     * is the decimal separator and {@code ','} is invalid. The argument separator is always {@code ';'}
     * regardless of locale, so {@code "summation(1,5;2,5;k)"} is two comma-decimals separated by
     * {@code ';'}. Internally the value is normalized to the canonical {@code '.'} form, so arithmetic,
     * comparison and hashing remain locale-agnostic.
     * </p>
     *
     * <p>
     * Changing the input locale discards the (optional) tokenized-expression cache, because cached
     * tokens were produced for the previous decimal separator.
     * </p>
     *
     * @param inputLocale locale whose decimal separator is used when parsing input; must not be {@code null}
     * @return this engine for builder-style chaining
     */
    public CalculatorEngine setInputLocale(@NonNull final Locale inputLocale) {
        this.inputLocale = inputLocale;
        this.inputDecimalSeparator = getDecimalSeparator(inputLocale);
        // Cached tokens were produced for the previous input separator; discard them so a
        // subsequent evaluation re-tokenizes with the new locale's decimal separator.
        this.expressionCache = null;
        return this;
    }

    /**
     * Resolves the decimal separator of the given locale via {@link DecimalFormatSymbols}.
     *
     * @param locale the locale to resolve; must not be {@code null}
     * @return the locale's decimal separator character
     */
    private static char getDecimalSeparator(@NonNull final Locale locale) {
        return DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
    }

    /**
     * Enables or disables the expression cache. Disabling clears the cache.
     *
     * @param enabled {@code true} to enable caching of tokenized expressions
     * @return this engine for builder-style chaining
     */
    public synchronized CalculatorEngine setExpressionCacheEnabled(final boolean enabled) {
        // Synchronized on {@code this} so it shares the same monitor as {@link #lookupCache(String)}
        // and {@link #storeCache(String, List)}: without the lock, this setter could write
        // {@code expressionCache = null} while another thread sat inside a synchronized lookup
        // and was about to dereference the field.
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
    public synchronized CalculatorEngine setExpressionCacheSize(final int size) {
        // See {@link #setExpressionCacheEnabled(boolean)} — same lock-sharing rationale: this
        // setter discards the cache reference, and that write must not race with concurrent
        // lookups/stores that already hold this engine's monitor.
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
            // Fresh instance, never the shared constant: this value is returned to callers who
            // may mutate it (e.g. negateThis), which must not corrupt the global BigNumbers.ZERO.
            return new BigNumber("0");
        }

        Map<String, String> previous = currentVariables.get();
        Map<String, String> combinedVariables = new HashMap<>(previous);
        combinedVariables.putAll(variables);
        currentVariables.set(combinedVariables);

        try {
            String normalized;
            try {
                normalized = replaceAbsSigns(expression);
            } catch (IllegalArgumentException illegalArgumentException) {
                throw new SyntaxErrorException(
                        CalculatorErrorCode.SYNTAX_INCOMPLETE_EXPRESSION,
                        Map.of(),
                        illegalArgumentException.getMessage() == null ? "Incomplete expression" : illegalArgumentException.getMessage(),
                        null,
                        illegalArgumentException);
            }

            List<Token> tokens;
            // Both reads of {@code expressionCacheEnabled} are delegated to the synchronized
            // {@link #lookupCache(String)} / {@link #storeCache(String, List)} helpers, which
            // re-check the flag inside the monitor. This eliminates the race window where the
            // flag flipped to {@code false} (and the cache was discarded) between an
            // unsynchronized check at this site and the actual cache mutation. See
            // {@link #setExpressionCacheEnabled(boolean)} for the writer side.
            List<Token> cachedTokens = lookupCache(normalized);
            if (cachedTokens != null) {
                tokens = new ArrayList<>(cachedTokens);
            } else {
                try {
                    tokens = tokenizer.tokenize(normalized, inputDecimalSeparator);
                } catch (final CalculatorException calculatorException) {
                    throw calculatorException;
                } catch (final RuntimeException tokenizerFailure) {
                    // Any unchecked failure from the tokenizer is a malformed-input
                    // problem, not an internal processing error: classify it as a
                    // syntax error so it never surfaces as "Processing Error". The
                    // original throwable is chained as the cause so callers and tests
                    // can still inspect the underlying root reason.
                    throw new SyntaxErrorException(
                            CalculatorErrorCode.SYNTAX_INCOMPLETE_EXPRESSION,
                            Map.of(),
                            Objects.requireNonNullElse(tokenizerFailure.getMessage(),
                                    "Malformed expression"),
                            null,
                            tokenizerFailure);
                }
                storeCache(normalized, List.copyOf(tokens));
            }

            // Structural pre-checks run BEFORE variable substitution and the evaluator,
            // so an expensive subexpression (e.g. a large factorial) is never computed
            // for an expression that cannot yield a result.
            validateInfixStructure(tokens);

            try {
                replaceVariables(this, tokens, combinedVariables);
            } catch (IllegalArgumentException variableSubstitutionFailure) {
                final String message = Objects.requireNonNullElse(variableSubstitutionFailure.getMessage(), "Variable is not defined");
                final String variableName = extractVariableName(message);
                throw new SyntaxErrorException(
                        CalculatorErrorCode.SYNTAX_UNKNOWN_VARIABLE,
                        variableName == null ? Map.of() : Map.of("variable", variableName),
                        message,
                        null,
                        variableSubstitutionFailure);
            }

            final List<Token> postfix;
            try {
                postfix = postfixParser.toPostfix(tokens);
            } catch (final CalculatorException calculatorException) {
                throw calculatorException;
            } catch (final RuntimeException parserFailure) {
                throw new SyntaxErrorException(
                        CalculatorErrorCode.SYNTAX_INCOMPLETE_EXPRESSION,
                        Map.of(),
                        Objects.requireNonNullElse(parserFailure.getMessage(), "Malformed expression"),
                        null,
                        parserFailure);
            }

            // Arity dry run: reject under-supplied operators / leftover operands before
            // the evaluator performs any (potentially expensive) computation.
            validatePostfixArity(postfix);

            try {
                return evaluator.evaluate(postfix).trim();
            } catch (final CalculatorException calculatorException) {
                throw calculatorException;
            } catch (final RuntimeException runtimeException) {
                throw classifyRuntimeException(runtimeException);
            }
        } finally {
            if (previous.isEmpty()) {
                currentVariables.remove();
            } else {
                currentVariables.set(previous);
            }
        }
    }

    /**
     * Evaluates an expression and returns its result as a {@link String}. Belongs to the
     * <em>Text Output API</em>: errors are caught and folded into the return value as a
     * message string rather than being thrown.
     *
     * <p>
     * The result uses the configured {@link #setLocale(Locale) locale}'s decimal separator
     * but <em>no</em> thousands grouping. For example with {@link Locale#GERMANY}
     * {@code "1234.56"} yields {@code "1234,56"} (not {@code "1.234,56"} — that grouped form
     * is the contract of {@link #evaluateToPrettyString(String)}); with {@link Locale#US} or
     * {@link Locale#ENGLISH} it yields {@code "1234.56"}.
     * </p>
     *
     * <p>
     * In the default {@link ErrorMode#RAW} the error text matches the legacy behaviour
     * (category default such as {@code "Syntax Error"} or {@code "Processing Error"}). In
     * {@link ErrorMode#USER_FRIENDLY} the localized message from the configured bundle is
     * returned instead. The input is {@code @NonNull}: passing {@code null} raises
     * {@link NullPointerException}. Callers that need null-tolerance should use the Safe UI
     * String API; callers that need to branch on success/failure should use the Typed Result
     * API.
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
            return result.toString(locale);
        } catch (final CalculatorException calculatorException) {
            return formatExceptionMessage(calculatorException);
        } catch (final Exception exception) {
            return formatExceptionMessage(classifyRuntimeException(exception));
        } catch (final StackOverflowError stackOverflowError) {
            // StackOverflowError is an Error, not an Exception: catch it explicitly so deep
            // recursion (e.g. a long variable-reference chain) is reported, not propagated (H6).
            return formatExceptionMessage(deeplyNestedException());
        }
    }

    /**
     * Evaluates an expression and returns the result formatted for human consumption
     * <em>with</em> thousands grouping. Belongs to the <em>Text Output API</em>: errors are
     * caught and folded into the return value as a message string rather than being thrown.
     *
     * <p>
     * Decimal and grouping separators follow the configured {@link #setLocale(Locale) locale}:
     * {@code "1234.56"} renders as {@code "1.234,56"} under {@link Locale#GERMANY},
     * {@code "1,234.56"} under {@link Locale#ENGLISH} / {@link Locale#US}, and the
     * corresponding separators for any other JDK-supported locale. The input is
     * {@code @NonNull}: passing {@code null} raises {@link NullPointerException}.
     * </p>
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
            return result.toPrettyString(locale);
        } catch (CalculatorException e) {
            return formatExceptionMessage(e);
        } catch (Exception e) {
            return formatExceptionMessage(classifyRuntimeException(e));
        } catch (final StackOverflowError stackOverflowError) {
            // See evaluateToString: keep the Text Output API's "never throws" contract on deep recursion.
            return formatExceptionMessage(deeplyNestedException());
        }
    }

    /**
     * Null- and exception-tolerant variant of {@link #evaluateToString(String)}. Belongs to
     * the <em>Safe UI String API</em>: accepts {@code null} as expression or variable map,
     * never throws, never returns {@code null}. Failures are returned as a prefixed error
     * string ({@code "Error: ..."} or, when the configured
     * {@link #setLocale(Locale) locale} is German, {@code "Fehler: ..."}).
     *
     * <p>
     * On success the result uses the configured locale's decimal separator but <em>no</em>
     * thousands grouping (e.g. {@code "1234,56"} under {@link Locale#GERMANY},
     * {@code "1234.56"} under {@link Locale#US}), consistent with
     * {@link #evaluateToString(String)}. Callers that need to branch on success/failure
     * without parsing the prefix should use {@link #evaluateToStringResult(String)} from the
     * Typed Result API.
     * </p>
     *
     * @param expression input expression; may be {@code null}
     * @return result as a string, or a prefixed error message
     */
    public String evaluateSafeToString(final String expression) {
        return evaluateSafeToString(expression, Map.of());
    }

    /**
     * Null- and exception-tolerant variant of {@link #evaluateToString(String, Map)}.
     *
     * @param expression input expression; may be {@code null}
     * @param variables  variable bindings; may be {@code null}
     * @return result as a string, or a prefixed error message
     */
    public String evaluateSafeToString(final String expression, final Map<String, String> variables) {
        if (expression == null) {
            return formatSafeError(new IllegalArgumentException("expression must not be null"));
        }
        try {
            BigNumber result = evaluate(expression, variables == null ? Map.of() : variables);
            return result.toString(locale);
        } catch (final CalculatorException calculatorException) {
            return formatSafeError(calculatorException);
        } catch (final Exception exception) {
            return formatSafeError(exception);
        } catch (final StackOverflowError stackOverflowError) {
            // StackOverflowError is an Error: catch it so the Safe UI String API never throws (H6).
            return formatSafeError(deeplyNestedException());
        }
    }

    /**
     * Null- and exception-tolerant variant of {@link #evaluateToPrettyString(String)}.
     * Belongs to the <em>Safe UI String API</em>: accepts {@code null}, never throws, never
     * returns {@code null}. Failures are returned as a prefixed error string
     * ({@code "Error: ..."} or, under a German {@link #setLocale(Locale) locale},
     * {@code "Fehler: ..."}).
     *
     * <p>
     * On success the result uses the configured locale's decimal separator <em>and</em>
     * thousands grouping (e.g. {@code "1.234,56"} under {@link Locale#GERMANY},
     * {@code "1,234.56"} under {@link Locale#US}), consistent with
     * {@link #evaluateToPrettyString(String)}. Callers that need to branch on success/failure
     * without parsing the prefix should use {@link #evaluateToPrettyStringResult(String)}
     * from the Typed Result API.
     * </p>
     *
     * @param expression input expression; may be {@code null}
     * @return pretty-formatted result, or a prefixed error message
     */
    public String evaluateSafeToPrettyString(final String expression) {
        return evaluateSafeToPrettyString(expression, Map.of());
    }

    /**
     * Null- and exception-tolerant variant of {@link #evaluateToPrettyString(String, Map)}.
     *
     * @param expression input expression; may be {@code null}
     * @param variables  variable bindings; may be {@code null}
     * @return pretty-formatted result, or a prefixed error message
     */
    public String evaluateSafeToPrettyString(final String expression, final Map<String, String> variables) {
        if (expression == null) {
            return formatSafeError(new IllegalArgumentException("expression must not be null"));
        }
        try {
            BigNumber result = evaluate(expression, variables == null ? Map.of() : variables);
            return result.toPrettyString(locale);
        } catch (final CalculatorException calculatorException) {
            return formatSafeError(calculatorException);
        } catch (final Exception exception) {
            return formatSafeError(exception);
        } catch (final StackOverflowError stackOverflowError) {
            // See evaluateSafeToString: keep the Safe UI String API's "never throws" contract.
            return formatSafeError(deeplyNestedException());
        }
    }

    /**
     * Formats any exception caught by the {@code evaluateSafe...} string methods into a short,
     * prefixed error string. The prefix is locale-aware: {@code "Fehler"} for German,
     * {@code "Error"} otherwise. {@link CalculatorException}s and classifiable
     * {@link RuntimeException}s are routed through {@link #formatExceptionMessage} so that the
     * configured {@link ErrorMode} (RAW vs. USER_FRIENDLY) is respected.
     *
     * @param exception exception to format; must not be {@code null}
     * @return short, single-line error string
     */
    private String formatSafeError(final Exception exception) {
        final String prefix = "de".equalsIgnoreCase(locale.getLanguage()) ? "Fehler" : "Error";
        String message;
        if (exception instanceof CalculatorException calculatorException) {
            message = formatExceptionMessage(calculatorException);
        } else if (exception instanceof RuntimeException) {
            message = formatExceptionMessage(classifyRuntimeException(exception));
        } else {
            message = exception.getMessage();
        }
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }
        return prefix + ": " + message;
    }

    /**
     * Builds the error reported when evaluation recurses so deeply that the JVM raises a
     * {@link StackOverflowError} — for example a pathologically long or deeply nested
     * variable-reference chain such as {@code x1=x2+1, x2=x3+1, ...}. A {@code StackOverflowError}
     * is an {@link Error}, not an {@link Exception}, so it would otherwise slip past the
     * {@code catch (Exception)} clauses of the Safe / Text Output APIs and break their documented
     * "never throws" contract. The Safe / Text boundaries catch it explicitly and route it through
     * this typed error instead.
     *
     * @return a typed processing error describing the over-deep nesting; never {@code null}
     */
    private static ProcessingErrorException deeplyNestedException() {
        return new ProcessingErrorException(
                CalculatorErrorCode.PROCESSING_INTERNAL,
                "Expression is nested too deeply to evaluate");
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
        } catch (final Exception exception) {
            CalculatorException classified = classifyRuntimeException(exception);
            CalculatorError err = classified.getCalculatorError();
            if (err == null) {
                err = new CalculatorError(
                        CalculatorErrorCode.PROCESSING_INTERNAL,
                        Objects.requireNonNullElse(exception.getMessage(), "Unknown error"));
            }
            return CalculatorResult.failure(err);
        } catch (final StackOverflowError stackOverflowError) {
            // StackOverflowError is an Error, not an Exception: catch it so the Typed Result API
            // honors its "never throws" contract on pathologically deep recursion (H6).
            return CalculatorResult.failure(new CalculatorError(
                    CalculatorErrorCode.PROCESSING_INTERNAL,
                    "Expression is nested too deeply to evaluate"));
        }
    }

    /**
     * Evaluates an expression and returns a {@link CalculatorResult} that wraps the
     * <em>locale-formatted</em> result string. Belongs to the <em>Typed Result API</em>:
     * the typed counterpart to {@link #evaluateSafeToString(String)}, so callers can branch
     * on {@link CalculatorResult#isSuccess()} / {@link CalculatorResult#isFailure()} without
     * parsing an {@code "Error: "} / {@code "Fehler: "} prefix.
     *
     * <p>
     * On success the wrapped string uses the configured {@link #setLocale(Locale) locale}'s
     * decimal separator but <em>no</em> thousands grouping (e.g. {@code "1234,56"} under
     * {@link Locale#GERMANY}, {@code "1234.56"} under {@link Locale#US}), matching
     * {@link #evaluateToString(String)}. On failure the structured {@link CalculatorError}
     * produced by {@link #evaluateSafe(String)} is propagated unchanged.
     * </p>
     *
     * @param expression input expression; must not be {@code null}
     * @return success carrying the formatted result, or failure carrying the structured error;
     * never {@code null}
     */
    public CalculatorResult<String> evaluateToStringResult(@NonNull final String expression) {
        return evaluateToStringResult(expression, Map.of());
    }

    /**
     * Variant of {@link #evaluateToStringResult(String)} accepting user-defined variables.
     *
     * @param expression input expression; must not be {@code null}
     * @param variables  variable bindings; must not be {@code null}
     * @return success carrying the formatted result, or failure carrying the structured error;
     * never {@code null}
     */
    public CalculatorResult<String> evaluateToStringResult(
            @NonNull final String expression,
            @NonNull final Map<String, String> variables
    ) {
        return evaluateSafe(expression, variables).map(value -> value.toString(locale));
    }

    /**
     * Evaluates an expression and returns a {@link CalculatorResult} that wraps the
     * <em>pretty-formatted</em> result string — locale-aware decimal separator <em>and</em>
     * thousands grouping per the configured {@link #setLocale(Locale) locale}
     * (e.g. {@code "1.234,56"} under {@link Locale#GERMANY}, {@code "1,234.56"} under
     * {@link Locale#US}). Belongs to the <em>Typed Result API</em>: the typed counterpart to
     * {@link #evaluateSafeToPrettyString(String)}.
     *
     * <p>
     * On failure the structured {@link CalculatorError} produced by
     * {@link #evaluateSafe(String)} is propagated unchanged, so callers can branch on
     * success/failure without parsing a prefixed error string.
     * </p>
     *
     * @param expression input expression; must not be {@code null}
     * @return success carrying the pretty-formatted result, or failure carrying the structured
     * error; never {@code null}
     */
    public CalculatorResult<String> evaluateToPrettyStringResult(@NonNull final String expression) {
        return evaluateToPrettyStringResult(expression, Map.of());
    }

    /**
     * Variant of {@link #evaluateToPrettyStringResult(String)} accepting user-defined variables.
     *
     * @param expression input expression; must not be {@code null}
     * @param variables  variable bindings; must not be {@code null}
     * @return success carrying the pretty-formatted result, or failure carrying the structured
     * error; never {@code null}
     */
    public CalculatorResult<String> evaluateToPrettyStringResult(
            @NonNull final String expression,
            @NonNull final Map<String, String> variables
    ) {
        return evaluateSafe(expression, variables).map(value -> value.toPrettyString(locale));
    }

    /**
     * Formats a {@link CalculatorException} according to the current {@link ErrorMode}. In
     * {@link ErrorMode#RAW} the legacy {@link Throwable#getMessage()} value is returned (the
     * category default such as {@code "Syntax Error"}); in {@link ErrorMode#USER_FRIENDLY}
     * the localized template from the resource bundle is used.
     *
     * @param calculatorException exception to format; must not be {@code null}
     * @return formatted message; never {@code null}
     */
    private String formatExceptionMessage(@NonNull final CalculatorException calculatorException) {
        if (errorMode == ErrorMode.USER_FRIENDLY) {
            CalculatorError calculatorError = calculatorException.getCalculatorError();
            if (calculatorError == null) {
                calculatorError = new CalculatorError(
                        CalculatorErrorCode.PROCESSING_INTERNAL,
                        Objects.requireNonNullElse(calculatorException.getDetailedMessage(), calculatorException.getMessage()));
            }
            return calculatorError.format(locale, ErrorMode.USER_FRIENDLY);
        }
        return Objects.requireNonNullElse(calculatorException.getMessage(), "Syntax Error");
    }

    /**
     * Maps an unchecked runtime exception that bubbled out of the lower-level math layer
     * (typically {@link ArithmeticException} or {@link IllegalArgumentException} from
     * {@code BigNumber} operations) to a typed {@link ProcessingErrorException} with a
     * matching {@link CalculatorErrorCode} so that downstream formatting can resolve a
     * localized template. Without this mapping such exceptions would surface verbatim in
     * English and bypass the {@code i18n/calculator_errors_*.properties} bundles entirely.
     *
     * @param throwable the unchecked exception thrown during evaluation; must not be {@code null}
     * @return a localized-friendly {@link ProcessingErrorException}; never {@code null}
     */
    private static ProcessingErrorException classifyRuntimeException(@NonNull final Throwable throwable) {
        final String message = Objects.requireNonNullElse(throwable.getMessage(), "");
        final String lower = message.toLowerCase(Locale.ROOT);
        final CalculatorErrorCode code;
        if (lower.contains("division by zero")
                || lower.contains("divisor zero")
                || lower.contains("undefined for value 0")
                || lower.contains("undefined for x = 0")
                || lower.contains("normalize list with sum 0")) {
            code = CalculatorErrorCode.PROCESSING_DIVISION_BY_ZERO;
        } else if (lower.contains("factorial") && lower.contains("non-negative")) {
            // "Factorial is only defined for non-negative integers."
            code = CalculatorErrorCode.MATH_FACTORIAL_NEGATIVE;
        } else if (lower.contains("factorial") && lower.contains("integer")) {
            // "Factorial is only defined for integers."
            code = CalculatorErrorCode.MATH_FACTORIAL_NON_INTEGER;
        } else if (lower.contains("ln(x) undefined")
                || lower.contains("number must be positive")
                || lower.contains("base must be positive")
                || ((lower.contains("log") || lower.contains("logarith")) && lower.contains("positive"))) {
            code = CalculatorErrorCode.MATH_LOG_NON_POSITIVE;
        } else if (lower.contains("root of a negative")
                || lower.contains("sqrt is only defined for non-negative")) {
            code = CalculatorErrorCode.MATH_ROOT_OF_NEGATIVE;
        } else if (lower.contains("overflow") || lower.contains("too large")) {
            code = CalculatorErrorCode.MATH_OVERFLOW;
        } else if (throwable instanceof ArithmeticException
                || lower.contains("undefined")
                || lower.contains("only defined")
                || lower.contains("must be")
                || lower.contains("must satisfy")
                || lower.contains("cannot be")
                || lower.contains("non-negative")
                || lower.contains("not a real number")
                || lower.contains("must not be")
                || lower.contains("only positive")
                || lower.contains("greater than")
                || lower.contains("less than")) {
            code = CalculatorErrorCode.PROCESSING_DOMAIN_ERROR;
        } else {
            code = CalculatorErrorCode.PROCESSING_INTERNAL;
        }
        return new ProcessingErrorException(code, message.isEmpty() ? "Processing error" : message);
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
     * Looks up a cached token list, initializing the cache lazily if necessary. When the cache
     * is disabled this method returns {@code null} without allocating, so callers do not need
     * to pre-check {@link #expressionCacheEnabled} on the fast path — the check inside the
     * monitor is the single source of truth and rules out the disable-vs-lookup race that
     * could previously leak a write into a cache that was supposed to be off.
     *
     * @param key normalized expression string; must not be {@code null}
     * @return cached token list, or {@code null} if no entry exists or the cache is disabled
     */
    private synchronized List<Token> lookupCache(@NonNull final String key) {
        if (!expressionCacheEnabled) {
            return null;
        }
        ensureCache();
        return expressionCache.get(key);
    }

    /**
     * Stores an immutable token list in the cache, initializing it lazily if necessary. When
     * the cache is disabled this method silently no-ops, see {@link #lookupCache(String)} for
     * the rationale behind centralising the enabled-check inside the monitor.
     *
     * @param key   normalized expression string; must not be {@code null}
     * @param value immutable token list captured before variable substitution; must not be {@code null}
     */
    private synchronized void storeCache(@NonNull final String key, @NonNull final List<Token> value) {
        if (!expressionCacheEnabled) {
            return;
        }
        ensureCache();
        expressionCache.put(key, value);
    }

    /**
     * Lazily creates the LRU cache. The capacity is captured once at creation time;
     * subsequent calls to {@link #setExpressionCacheSize(int)} discard the existing cache so
     * that this method can rebuild it with the new capacity.
     *
     * <p><strong>Threading contract:</strong> the method is intentionally not declared
     * {@code synchronized} because every existing caller ({@link #lookupCache(String)},
     * {@link #storeCache(String, List)}) already holds this engine's monitor. New callers must
     * hold the same monitor before invoking this method; otherwise the {@code null}-check and
     * the assignment race with the cache-discarding setters.</p>
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
