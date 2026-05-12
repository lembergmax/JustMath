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

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.errors.CalculatorError;
import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.calculator.errors.CalculatorResult;
import com.mlprograms.justmath.calculator.errors.ErrorMode;
import com.mlprograms.justmath.calculator.exceptions.CalculatorException;
import com.mlprograms.justmath.calculator.exceptions.ProcessingErrorException;
import com.mlprograms.justmath.calculator.exceptions.SyntaxErrorException;
import com.mlprograms.justmath.calculator.internal.Token;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.Getter;
import lombok.NonNull;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.mlprograms.justmath.bignumber.BigNumbers.DEFAULT_DIVISION_PRECISION;
import static com.mlprograms.justmath.calculator.CalculatorEngineUtils.*;

/**
 * Hauptklasse zur Auswertung mathematischer Ausdrücke als Strings mit exakter Genauigkeit.
 *
 * <p>
 * Wandelt die Eingabe in Tokens, parst diese nach Postfix (RPN) und wertet das Ergebnis aus.
 * Unterstützt Variablen, BigNumber-Genauigkeit, lokalisierte Fehlermeldungen sowie ein
 * optionales Caching von Tokens und Postfix-Notation für wiederholte Ausdrücke.
 * </p>
 *
 * <p>
 * <strong>Rückwärtskompatibilität:</strong> {@link #evaluateToString} und
 * {@link #evaluateToPrettyString} liefern weiterhin {@code e.getMessage()} im Fehlerfall
 * (z. B. {@code "Syntax Error"}) sofern der Standard-{@link ErrorMode#RAW} aktiv bleibt.
 * Lokalisierung wird über {@link #setLocale(Locale)} + {@link #setErrorMode(ErrorMode)}
 * aktiviert oder pro Aufruf über {@link #evaluateSafe(String, Map, Locale)}.
 * </p>
 */
@Getter
public class CalculatorEngine {

    /**
     * Thread-lokaler Speicher für die aktuell aktiven Variablen während verschachtelter
     * Auswertungen. Wird in {@link #evaluate(String, Map)} per try/finally korrekt
     * wiederhergestellt, damit Variablen-State nicht zwischen Aufrufen leakt.
     */
    private static final ThreadLocal<Map<String, String>> currentVariables = ThreadLocal.withInitial(HashMap::new);

    /**
     * Tokenizer für die lexikalische Analyse.
     */
    private final Tokenizer tokenizer;
    /**
     * Evaluator für RPN-basierte Ausführung.
     */
    private final Evaluator evaluator;
    /**
     * Parser zur Konvertierung von Infix nach Postfix.
     */
    private final PostfixParser postfixParser;

    /**
     * Aktuelle Locale für lokalisierte Fehlermeldungen. Default: {@link Locale#ENGLISH}.
     */
    @NonNull
    private Locale locale = Locale.ENGLISH;

    /**
     * Aktueller Fehler-Modus. Default: {@link ErrorMode#RAW} — gleiches Verhalten wie vor
     * Einführung der Lokalisierung.
     */
    @NonNull
    private ErrorMode errorMode = ErrorMode.RAW;

    /**
     * Aktivierung des Expression-Cache. Default: aus.
     */
    private boolean expressionCacheEnabled = false;

    /**
     * Maximale Größe des Expression-Cache (LRU). Default: 128.
     */
    private int expressionCacheSize = 128;

    /**
     * Lazy initialisierter LRU-Cache. Schlüssel: normalisierter Ausdrucksstring (nach
     * {@code replaceAbsSigns}). Wert: unveränderliche Token-Liste (vor Variablen-Substitution).
     *
     * <p>
     * Postfix wird absichtlich nicht gecacht, da die Variablen-Substitution Tokens in der
     * Infix-Liste ersetzt; eine vor der Substitution gebaute Postfix-Liste würde noch auf die
     * ursprünglichen VARIABLE-Tokens verweisen. Tokenizing ist der teurere Schritt; das
     * erneute Postfix-Parsen nach Substitution ist günstig.
     * </p>
     */
    private Map<String, List<Token>> expressionCache;

    /**
     * Default-Konstruktor mit Standardpräzision und {@link TrigonometricMode#DEG}.
     */
    public CalculatorEngine() {
        this(getDefaultMathContext(DEFAULT_DIVISION_PRECISION), TrigonometricMode.DEG);
    }

    /**
     * Konstruktor mit angegebener Division-Präzision, sonst Defaults.
     *
     * @param divisionPrecision Präzision für Divisionen
     */
    public CalculatorEngine(int divisionPrecision) {
        this(getDefaultMathContext(divisionPrecision), TrigonometricMode.DEG);
    }

    /**
     * Konstruktor mit Division-Präzision und Trigonometriemodus.
     *
     * @param divisionPrecision Präzision für Divisionen
     * @param trigonometricMode Trigonometriemodus (DEG/RAD/...)
     */
    public CalculatorEngine(int divisionPrecision, @NonNull TrigonometricMode trigonometricMode) {
        this(getDefaultMathContext(divisionPrecision), trigonometricMode);
    }

    /**
     * Konstruktor mit gegebenem {@link MathContext}, Default-Trigonometriemodus.
     *
     * @param mathContext Mathematischer Kontext
     */
    public CalculatorEngine(@NonNull MathContext mathContext) {
        this(mathContext, TrigonometricMode.DEG);
    }

    /**
     * Konstruktor mit angegebenem Trigonometriemodus, Default-MathContext.
     *
     * @param trigonometricMode Trigonometriemodus
     */
    public CalculatorEngine(@NonNull TrigonometricMode trigonometricMode) {
        this(getDefaultMathContext(DEFAULT_DIVISION_PRECISION), trigonometricMode);
    }

    /**
     * Vollständiger Konstruktor mit MathContext und Trigonometriemodus.
     *
     * @param mathContext       Mathematischer Kontext (Präzision und Rundung)
     * @param trigonometricMode Trigonometriemodus
     */
    public CalculatorEngine(@NonNull MathContext mathContext, @NonNull TrigonometricMode trigonometricMode) {
        this.tokenizer = new Tokenizer();
        this.evaluator = new Evaluator(mathContext, trigonometricMode);
        this.postfixParser = new PostfixParser();
    }

    /**
     * Liefert die aktuell aktiven Variablen aus dem Thread-lokalen Kontext (Kopie).
     *
     * @return Kopie der aktiven Variablen
     */
    public static Map<String, String> getCurrentVariables() {
        return new HashMap<>(currentVariables.get());
    }

    /**
     * Setzt die Locale für nutzerfreundliche Fehlermeldungen.
     *
     * @param locale Ziel-Locale
     * @return diese Engine (Builder-Stil)
     */
    public CalculatorEngine setLocale(@NonNull final Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Setzt den Fehler-Modus.
     *
     * @param errorMode Modus: {@link ErrorMode#RAW} oder {@link ErrorMode#USER_FRIENDLY}
     * @return diese Engine
     */
    public CalculatorEngine setErrorMode(@NonNull final ErrorMode errorMode) {
        this.errorMode = errorMode;
        return this;
    }

    /**
     * Aktiviert oder deaktiviert den Token-/Postfix-Cache.
     *
     * @param enabled true zum Aktivieren
     * @return diese Engine
     */
    public CalculatorEngine setExpressionCacheEnabled(final boolean enabled) {
        this.expressionCacheEnabled = enabled;
        if (!enabled) {
            this.expressionCache = null;
        }
        return this;
    }

    /**
     * Setzt die maximale Cache-Größe. Muss > 0 sein.
     *
     * @param size neue maximale Anzahl Einträge
     * @return diese Engine
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
     * Wertet einen Ausdruck ohne Variablen aus.
     *
     * @param expression Eingabe-Ausdruck
     * @return Ergebnis als {@link BigNumber}
     */
    public BigNumber evaluate(@NonNull String expression) {
        return evaluate(expression, Map.of());
    }

    /**
     * Wertet einen Ausdruck mit optionalen Variablen aus.
     *
     * <p>
     * Der Thread-lokale Variablen-Kontext wird per try/finally korrekt wiederhergestellt,
     * sodass verschachtelte Auswertungen und Folgeaufrufe innerhalb desselben Threads
     * keinen State leaken — auch nicht im Fehlerfall.
     * </p>
     *
     * @param expression Eingabe-Ausdruck
     * @param variables  Variablen-Map (Name → Ausdruck)
     * @return Ergebnis als {@link BigNumber}
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
     * Wertet einen Ausdruck aus und gibt das Ergebnis als String zurück.
     *
     * <p>
     * Bei Fehlern wird im {@link ErrorMode#RAW} (Default) das gleiche Verhalten wie vor
     * der Lokalisierung beibehalten: {@code e.getMessage()} oder Fallback {@code "Syntax Error"}.
     * Bei {@link ErrorMode#USER_FRIENDLY} wird die lokalisierte Meldung verwendet.
     * </p>
     *
     * @param expression Eingabe-Ausdruck
     * @return Ergebnis als String oder Fehlermeldung
     */
    public String evaluateToString(@NonNull String expression) {
        return evaluateToString(expression, Map.of());
    }

    /**
     * Wertet einen Ausdruck mit Variablen aus und gibt das Ergebnis als String zurück.
     *
     * @param expression Eingabe-Ausdruck
     * @param variables  Variablen-Map
     * @return Ergebnis als String oder Fehlermeldung
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
     * Wertet einen Ausdruck aus und gibt das Ergebnis als formatierten String zurück.
     *
     * @param expression Eingabe-Ausdruck
     * @return formatiertes Ergebnis oder Fehlermeldung
     */
    public String evaluateToPrettyString(@NonNull String expression) {
        return evaluateToPrettyString(expression, Map.of());
    }

    /**
     * Wertet einen Ausdruck mit Variablen aus und gibt das Ergebnis als formatierten String zurück.
     *
     * @param expression Eingabe-Ausdruck
     * @param variables  Variablen-Map
     * @return formatiertes Ergebnis oder Fehlermeldung
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
     * Sichere Auswertung ohne Exceptions im Erfolgs- und Fehlerpfad.
     *
     * @param expression Eingabe-Ausdruck
     * @return Erfolgs- oder Fehlerergebnis
     */
    public CalculatorResult<BigNumber> evaluateSafe(@NonNull final String expression) {
        return evaluateSafe(expression, Map.of(), this.locale);
    }

    /**
     * Sichere Auswertung mit Variablen.
     *
     * @param expression Eingabe-Ausdruck
     * @param variables  Variablen-Map
     * @return Erfolgs- oder Fehlerergebnis
     */
    public CalculatorResult<BigNumber> evaluateSafe(
            @NonNull final String expression,
            @NonNull final Map<String, String> variables
    ) {
        return evaluateSafe(expression, variables, this.locale);
    }

    /**
     * Sichere Auswertung mit Variablen und explizit übergebener Locale (überschreibt die
     * Engine-Locale nur für diesen Aufruf).
     *
     * @param expression Eingabe-Ausdruck
     * @param variables  Variablen-Map
     * @param locale     Locale für diesen Aufruf
     * @return Erfolgs- oder Fehlerergebnis
     */
    public CalculatorResult<BigNumber> evaluateSafe(
            @NonNull final String expression,
            @NonNull final Map<String, String> variables,
            @NonNull final Locale locale
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
     * Liefert die Meldung einer {@link CalculatorException} entsprechend dem aktuellen
     * {@link ErrorMode}. Im RAW-Modus wird der bisherige {@code getMessage()}-Wert
     * zurückgegeben (Kategorie, z. B. {@code "Syntax Error"}); im USER_FRIENDLY-Modus
     * der lokalisierte Text aus dem Bundle.
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
     * Extrahiert den Variablennamen aus der legacy Fehlermeldung {@code "Variable 'x' is not defined."}.
     *
     * @param message Quelltext
     * @return Variablenname oder {@code null}
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
     * Lazy-Initialisierung des LRU-Cache und Lookup.
     */
    private synchronized List<Token> lookupCache(@NonNull final String key) {
        ensureCache();
        return expressionCache.get(key);
    }

    private synchronized void storeCache(@NonNull final String key, @NonNull final List<Token> value) {
        ensureCache();
        expressionCache.put(key, value);
    }

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
