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

import java.math.MathContext;
import java.util.*;

import io.github.lembergmax.justmath.bignumber.BigNumbers;
import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.exceptions.SyntaxErrorException;
import io.github.lembergmax.justmath.calculator.expression.ExpressionElement;
import io.github.lembergmax.justmath.calculator.expression.ExpressionElements;
import io.github.lembergmax.justmath.calculator.expression.elements.Constant;
import io.github.lembergmax.justmath.calculator.expression.elements.Parenthesis;
import io.github.lembergmax.justmath.calculator.expression.elements.Separator;
import io.github.lembergmax.justmath.calculator.expression.elements.function.ThreeArgumentFunction;
import io.github.lembergmax.justmath.calculator.internal.Token;
import lombok.NonNull;

/**
 * Tokenizer for mathematical expressions.
 * <p>
 * This class performs lexical analysis of a mathematical expression string,
 * converting it into a sequence of tokens suitable for parsing and evaluation.
 * It recognizes numeric literals (including signed and decimal numbers), operators,
 * functions, constants (e.g., π and e), parentheses, and separators.
 * <p>
 * The tokenizer also handles special cases such as:
 * <ul>
 *   <li>Emitting prefix signs as {@link Token.Type#UNARY_OPERATOR} tokens (never folded
 *       into a number literal), so that {@code -3^2} parses as {@code -(3^2)}.</li>
 *   <li>Collapsing a run of consecutive '+'/'-' characters into a single net sign token
 *       (see {@code tokenizeSignRun}), respecting arithmetic sign rules.</li>
 *   <li>Inserting implicit multiplication tokens where multiplication is implied by juxtaposition,
 *       such as between a number and a parenthesis ("2(3)"), or between parentheses and functions.</li>
 * </ul>
 * <p>
 * The set of valid operators and functions is dynamically populated from the
 * {@link ExpressionElements} registry, allowing extensibility and consistency
 * with the overall expression language.
 * <p>
 * The tokenizer is locale-agnostic but uses a {@link MathContext} to obtain precise representations
 * of mathematical constants like π and e.
 * <p>
 * This class is not thread-safe; each instance should be used by a single thread or
 * externally synchronized if shared.
 */
public class Tokenizer {

    /**
     * Candidate descriptor for registered three-argument functions used by the tokenizer.
     *
     * <p>This record stores:
     * <ul>
     *   <li>{@code symbol} — the function symbol as registered in {@link ExpressionElements},</li>
     *   <li>{@code symbolLength} — the precomputed length of the symbol (used to optimize matching),</li>
     *   <li>{@code element} — the corresponding {@link ExpressionElement} instance.</li>
     * </ul>
     *
     * <p>Instances of this record are sorted by {@code symbolLength} (longest first) to
     * implement a maximal-munch lexical matching strategy.</p>
     *
     * @param symbol       the function symbol string
     * @param symbolLength the length of the function symbol (precomputed)
     * @param element      the associated ExpressionElement instance
     */
    private record ThreeArgCandidate(String symbol, int symbolLength, ExpressionElement element) {

        /**
         * Convenience constructor that computes {@code symbolLength} from the provided {@code symbol}
         * and delegates to the canonical record constructor.
         *
         * @param symbol  the function symbol
         * @param element the associated ExpressionElement instance
         */
        private ThreeArgCandidate(final String symbol, final ExpressionElement element) {
            this(symbol, symbol.length(), element);
        }

    }

    /**
     * Cached view of the registered operator and function symbols.
     * <p>
     * This set references the key set of the {@link ExpressionElements} registry and is
     * used during tokenization to quickly determine whether a substring corresponds to
     * a known operator or function. It is a live view and will reflect changes in the
     * registry.
     */
    private static final Set<String> VALID_OPERATORS_AND_FUNCTIONS = ExpressionElements.getRegistry().keySet();

    /**
     * Precomputed candidates for registered three-argument functions.
     *
     * <p>This array is constructed once at class initialization by scanning
     * {@link ExpressionElements#getRegistry()} for instances of {@link ThreeArgumentFunction}.
     * Candidates are sorted by decreasing symbol length to ensure a maximal-munch
     * matching strategy during tokenization.</p>
     */
    private static final ThreeArgCandidate[] THREE_ARGUMENT_FUNCTION_CANDIDATES =
            buildThreeArgumentFunctionCandidates();

    /**
     * Tokenizes the input mathematical expression string into a list of {@link Token} objects.
     * <p>
     * This method performs lexical analysis by scanning the input expression character by character.
     * It recognizes numbers, parentheses, separators, operators, functions, constants (such as pi
     * and e), emits prefix signs as unary-operator tokens, collapses consecutive '+'/'-' runs into
     * a single net sign, and inserts implicit multiplication tokens where applicable.
     * <p>
     * The token list returned by this method is suitable for further syntactic parsing and evaluation.
     *
     * @param input the mathematical expression to tokenize, as a string
     * @return a list of tokens representing the lexemes of the expression
     * @throws IllegalArgumentException if the input contains invalid characters or malformed expressions
     * @throws NullPointerException     if the input string is null
     */
    public List<Token> tokenize(@NonNull final String input) {
        return tokenize(input, '.');
    }

    /**
     * Tokenizes the input using the given decimal separator for numeric literals.
     *
     * <p>
     * Strict locale-aware parsing: when {@code decimalSeparator} is not {@code '.'} (for example
     * {@code ','} for German, French or Spanish input), only that character is accepted as a
     * decimal point. The canonical {@code '.'} then becomes an invalid character, and every
     * occurrence of {@code decimalSeparator} is normalised to {@code '.'} so that the parser,
     * evaluator and {@link io.github.lembergmax.justmath.bignumber.BigNumber} keep operating on
     * locale-agnostic, US-canonical number literals. The argument separator stays {@code ';'}
     * regardless of locale, so {@code "summation(1,5;2,5;k)"} parses as two comma-decimals
     * separated by {@code ';'}.
     * </p>
     *
     * <p>
     * Input containing the internal {@link #WHITESPACE_BOUNDARY} sentinel — a non-typeable control
     * character that {@link #removeWhitespace(String)} injects to mark deliberate token splits — is
     * rejected as an invalid character; otherwise a caller could smuggle a boundary past a naive input
     * filter and the scan loop would silently collapse the surrounding characters (audit fix K5).
     * </p>
     *
     * @param input            the mathematical expression to tokenize; must not be {@code null}
     * @param decimalSeparator the decimal separator of the active input locale (typically {@code '.'} or {@code ','})
     * @return a list of tokens representing the lexemes of the expression
     * @throws SyntaxErrorException if the input contains invalid characters or malformed expressions
     */
    public List<Token> tokenize(@NonNull final String input, final char decimalSeparator) {
        final int sentinelIndex = input.indexOf(WHITESPACE_BOUNDARY);
        if (sentinelIndex >= 0) {
            throw new SyntaxErrorException(
                    CalculatorErrorCode.SYNTAX_INVALID_CHARACTER,
                    Map.of("character", String.valueOf(WHITESPACE_BOUNDARY)),
                    "Invalid control character at position " + sentinelIndex,
                    sentinelIndex);
        }

        final String source = normalizeDecimalSeparator(input, decimalSeparator);

        boolean nextAbsoluteIsOpen = true;

        List<Token> tokens = new ArrayList<>();
        String expression = removeWhitespace(source);
        int index = 0;

        while (index < expression.length()) {
            char character = expression.charAt(index);

            if (character == WHITESPACE_BOUNDARY) {
                index++;
                continue;
            }

            Optional<ExpressionElement> matchedFunction = matchThreeArgumentFunction(expression, index);

            if (startsNumericLiteral(character)) {
                index = tokenizeNumber(expression, index, tokens);
            } else if (isSignChar(character)) {
                index = tokenizeSignRun(expression, index, tokens);
            } else if (isLeftParenthesis(character)) {
                tokens.add(new Token(Token.Type.LEFT_PAREN, String.valueOf(character)));
                index++;
            } else if (isRightParenthesis(character)) {
                tokens.add(new Token(Token.Type.RIGHT_PAREN, String.valueOf(character)));
                index++;
            } else if (isSeparator(character)) {
                tokens.add(new Token(Token.Type.SEMICOLON, String.valueOf(character)));
                index++;
            } else if (isAbsoluteValueSign(character)) {
                if (nextAbsoluteIsOpen) {
                    tokens.add(new Token(Token.Type.FUNCTION, ExpressionElements.FUNC_ABS));
                    tokens.add(new Token(Token.Type.LEFT_PAREN, ExpressionElements.PAR_LEFT));
                } else {
                    tokens.add(new Token(Token.Type.RIGHT_PAREN, ExpressionElements.PAR_RIGHT));
                }

                nextAbsoluteIsOpen = !nextAbsoluteIsOpen;
                index++;
            } else if (matchedFunction.isPresent()) {
                ExpressionElement expressionElement = matchedFunction.get();
                String symbol = expressionElement.getSymbol();

                int functionStart = index + symbol.length();
                if (functionStart >= expression.length() || !isLeftParenthesis(expression.charAt(functionStart))) {
                    throw new SyntaxErrorException(
                            CalculatorErrorCode.SYNTAX_MISSING_RIGHT_PAREN,
                            Map.of("function", symbol),
                            "Expected '(' after function: " + symbol,
                            null);
                }
                int closingParenthesis = findClosingParenthesis(expression, functionStart);
                if (closingParenthesis < 0) {
                    throw new SyntaxErrorException(
                            CalculatorErrorCode.SYNTAX_MISSING_RIGHT_PAREN,
                            Map.of("function", symbol),
                            "Unmatched '(' in function: " + symbol,
                            null);
                }

                String inside = expression.substring(functionStart + 1, closingParenthesis);

                List<String> parts = splitTopLevel(inside, ExpressionElements.SEP_SEMICOLON.charAt(0));
                if (parts.size() != 3) {
                    throw new SyntaxErrorException(
                            CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT,
                            Map.of("function", symbol, "expected", "3", "actual", String.valueOf(parts.size())),
                            "Function '" + symbol + "' must have three arguments",
                            null);
                }

                tokens.add(new Token(Token.Type.NUMBER, parts.getFirst()));
                tokens.add(new Token(Token.Type.NUMBER, parts.get(1)));
                tokens.add(new Token(Token.Type.STRING, parts.get(2)));
                tokens.add(new Token(Token.Type.FUNCTION, symbol));

                index = closingParenthesis + 1;
            } else {
                int lengthOfMatch = getLengthOfMatchingOperatorOrFunction(expression, index, tokens);
                if (lengthOfMatch > 0) {
                    index += lengthOfMatch;
                } else {
                    throw new SyntaxErrorException(
                            CalculatorErrorCode.SYNTAX_INVALID_CHARACTER,
                            Map.of("character", String.valueOf(character)),
                            "Invalid character at position " + index + ": " + character,
                            index);
                }
            }
        }

        insertImplicitMultiplicationTokens(tokens);

        return tokens;
    }

    /**
     * Consumes a run of consecutive {@code '+'} / {@code '-'} characters starting
     * at {@code startIndex} and emits at most one token reflecting the net sign.
     * <p>
     * Behaviour:
     * <ul>
     *   <li>The run is classified once at {@code startIndex}: in unary context
     *       (start of expression, after operator, after {@code (}, after
     *       {@code ;}) the emitted token is {@link Token.Type#UNARY_OPERATOR};
     *       otherwise {@link Token.Type#OPERATOR}.</li>
     *   <li>The net sign is computed by parity of {@code '-'} count. An even
     *       count yields {@code '+'}, an odd count yields {@code '-'}.</li>
     *   <li>Unary {@code '+'} is a no-op and is dropped entirely (no token).
     *       Binary {@code '+'} or {@code '-'} is always emitted.</li>
     * </ul>
     * Whitespace between two signs is preserved as a
     * {@link #WHITESPACE_BOUNDARY} by {@link #removeWhitespace(String)}, which
     * stops the run scan and forces two separate classifications.
     *
     * @param expression the input expression (whitespace already normalised)
     * @param startIndex the index of the first {@code '+'} or {@code '-'}
     * @param tokens     the token list to append to
     * @return the index immediately after the consumed run
     */
    private int tokenizeSignRun(final String expression, final int startIndex, final List<Token> tokens) {
        final boolean unaryContext = isUnarySignStart(expression, startIndex, tokens);
        int minusCount = 0;
        int i = startIndex;
        while (i < expression.length()) {
            char c = expression.charAt(i);
            if (c == '-') {
                minusCount++;
                i++;
            } else if (c == '+') {
                i++;
            } else {
                break;
            }
        }
        final boolean netMinus = (minusCount & 1) == 1;
        if (unaryContext) {
            if (netMinus) {
                tokens.add(new Token(Token.Type.UNARY_OPERATOR, ExpressionElements.OP_MINUS));
            }
        } else {
            tokens.add(new Token(Token.Type.OPERATOR,
                    netMinus ? ExpressionElements.OP_MINUS : ExpressionElements.OP_PLUS));
        }
        return i;
    }

    /**
     * Inserts implicit multiplication tokens into the token list where a multiplication is implied
     * by adjacent tokens like "(2)3", "2(3)", "π(4)", or "ka".
     * <p>
     * <strong>Note:</strong> Implicit multiplication for constants only works for symbols
     * already defined as {@link ExpressionElements} in the registry. New or unregistered symbols
     * will not be recognized and may lead to an {@link IllegalArgumentException}.
     *
     * @param tokens the list of tokens to scan and modify
     */
    private void insertImplicitMultiplicationTokens(List<Token> tokens) {
        for (int i = 0; i < tokens.size() - 1; i++) {
            Token current = tokens.get(i);
            Token next = tokens.get(i + 1);

            if (next.getType() == Token.Type.OPERATOR || next.getType() == Token.Type.SEMICOLON) {
                continue;
            }

            if (needsMultiplicationSign(current, next)) {
                tokens.add(i + 1, new Token(Token.Type.OPERATOR, ExpressionElements.OP_MULTIPLY));
                i++;
            }
        }
    }

    /**
     * Determines whether implicit multiplication should be inserted between two tokens.
     * <p>
     * Returns true if the combination of {@code current} and {@code next} tokens
     * represents a context where multiplication is implied, such as:
     * <ul>
     *   <li>Number followed by left parenthesis or function</li>
     *   <li>Right parenthesis followed by number, function, or left parenthesis</li>
     *   <li>Number, variable, or zero-argument constant followed by a zero-argument function</li>
     *   <li>Zero-argument function followed by number or variable</li>
     * </ul>
     *
     * @param current the current token in the sequence
     * @param next    the next token in the sequence
     * @return true if implicit multiplication is needed, false otherwise
     */
    private boolean needsMultiplicationSign(final Token current, final Token next) {
        return isNumberFollowedByParenOrFunction(current, next)
                || isRightParenFollowedByValidToken(current, next)
                || isNumberOrConstantFollowedByZeroArgFunction(current, next)
                || isZeroArgFunctionFollowedByNumberOrVariable(current, next)
                || isConstantFollowedByFunction(current, next)
                || isVariableFollowedByVariableOrConstant(current, next)
                || isVariableOrConstantFollowedByNumber(current, next)
                || isNumberFollowedByVariableOrConstant(current, next)
                || isConstantFollowedByVariable(current, next)
                || isVariableOrConstantFollowedByLeftParenOrFunction(current, next);
    }

    /**
     * Checks whether a numeric token is immediately followed by either a left parenthesis
     * or a function token. Used to detect contexts where implicit multiplication should
     * be inserted (e.g. "2(" or "2sin").
     *
     * @param current the current token (expected to be a number)
     * @param next    the next token in sequence
     * @return true if current is NUMBER and next is LEFT_PAREN or FUNCTION; false otherwise
     */
    private boolean isNumberFollowedByParenOrFunction(final Token current, final Token next) {
        return current.getType() == Token.Type.NUMBER
                && (next.getType() == Token.Type.LEFT_PAREN || next.getType() == Token.Type.FUNCTION);
    }

    /**
     * Determines whether a closing parenthesis is followed by a token that can form
     * an implicit multiplication with it. Typical cases are ")2", ")sin", or ")(".
     *
     * @param current the current token (expected to be a right parenthesis)
     * @param next    the following token to inspect
     * @return true if current is RIGHT_PAREN and next is NUMBER, FUNCTION, or LEFT_PAREN
     */
    private boolean isRightParenFollowedByValidToken(final Token current, final Token next) {
        return current.getType() == Token.Type.RIGHT_PAREN
                && (next.getType() == Token.Type.NUMBER
                || next.getType() == Token.Type.FUNCTION
                || next.getType() == Token.Type.LEFT_PAREN
                || next.getType() == Token.Type.VARIABLE
                || next.getType() == Token.Type.CONSTANT);
    }

    /**
     * Checks whether a NUMBER or a zero-argument constant is followed by another
     * zero-argument constant. This indicates a juxtaposition like "2pi" or "pi e"
     * which may require implicit multiplication.
     *
     * @param current the current token (NUMBER or potential zero-arg constant)
     * @param next    the next token to check for being a zero-argument constant
     * @return true if (current is NUMBER or zero-arg constant) and next is a zero-arg constant
     */
    private boolean isNumberOrConstantFollowedByZeroArgFunction(final Token current, final Token next) {
        return (current.getType() == Token.Type.NUMBER || isZeroArgConstant(current))
                && isZeroArgConstant(next);
    }

    /**
     * Determines if a zero-argument constant (e.g. "pi") is followed by a NUMBER or VARIABLE,
     * which often implies multiplication (e.g. "pi2" or "pi x").
     *
     * @param current the current token to test (expected zero-arg constant)
     * @param next    the token following current
     * @return true if current is a zero-arg constant and next is a NUMBER
     */
    private boolean isZeroArgFunctionFollowedByNumberOrVariable(final Token current, final Token next) {
        return isZeroArgConstant(current)
                && next.getType() == Token.Type.NUMBER;
    }

    /**
     * Checks whether a constant token is immediately followed by a function token.
     * Example: "pi sin" could be treated as implicit multiplication "pi * sin".
     *
     * @param current the current token (expected to be CONSTANT)
     * @param next    the token that follows
     * @return true if current is CONSTANT and next is FUNCTION
     */
    private boolean isConstantFollowedByFunction(final Token current, final Token next) {
        return current.getType() == Token.Type.CONSTANT
                && next.getType() == Token.Type.FUNCTION;
    }

    /**
     * Determines whether a VARIABLE or CONSTANT token is immediately followed by a left parenthesis
     * or a function token. This is used to detect contexts where implicit multiplication is implied,
     * e.g. `x(` or `pi sin` should behave like `x * (` or `pi * sin`.
     *
     * @param current the current token (expected to be VARIABLE or CONSTANT)
     * @param next    the following token to inspect
     * @return true if {@code current} is VARIABLE or CONSTANT and {@code next} is LEFT_PAREN or FUNCTION
     */
    private boolean isVariableOrConstantFollowedByLeftParenOrFunction(final Token current, final Token next) {
        return (current.getType() == Token.Type.VARIABLE || current.getType() == Token.Type.CONSTANT)
                && (next.getType() == Token.Type.LEFT_PAREN || next.getType() == Token.Type.FUNCTION);
    }

    /**
     * Determines whether a variable token is followed by another variable or a constant.
     * Useful for detecting implicit multiplication in sequences like "xy" or "xpi".
     *
     * @param current the current token (VARIABLE)
     * @param next    the following token
     * @return true if current is VARIABLE and next is VARIABLE or CONSTANT
     */
    private boolean isVariableFollowedByVariableOrConstant(final Token current, final Token next) {
        return current.getType() == Token.Type.VARIABLE
                && (next.getType() == Token.Type.VARIABLE || next.getType() == Token.Type.CONSTANT);
    }

    /**
     * Returns true when a variable or a constant token is immediately followed by a numeric token.
     * This scenario commonly implies implicit multiplication (e.g. `x2` or `pi2`).
     *
     * @param current the current token (expected VARIABLE or CONSTANT)
     * @param next    the following token to inspect
     * @return true if {@code current} is VARIABLE or CONSTANT and {@code next} is NUMBER
     */
    private boolean isVariableOrConstantFollowedByNumber(final Token current, final Token next) {
        return (current.getType() == Token.Type.VARIABLE || current.getType() == Token.Type.CONSTANT)
                && next.getType() == Token.Type.NUMBER;
    }

    /**
     * Returns true when a numeric token is immediately followed by a variable or constant token.
     * This also commonly implies implicit multiplication (e.g. `2x` or `2pi`).
     *
     * @param current the current token (expected NUMBER)
     * @param next    the following token to inspect
     * @return true if {@code current} is NUMBER and {@code next} is VARIABLE or CONSTANT
     */
    private boolean isNumberFollowedByVariableOrConstant(final Token current, final Token next) {
        return current.getType() == Token.Type.NUMBER
                && (next.getType() == Token.Type.VARIABLE || next.getType() == Token.Type.CONSTANT);
    }

    /**
     * Checks whether a constant is followed by a variable (e.g. "pi x"),
     * which commonly indicates implicit multiplication.
     *
     * @param current the current token (CONSTANT)
     * @param next    the following token (VARIABLE)
     * @return true if current is CONSTANT and next is VARIABLE
     */
    private boolean isConstantFollowedByVariable(final Token current, final Token next) {
        return current.getType() == Token.Type.CONSTANT
                && next.getType() == Token.Type.VARIABLE;
    }

    /**
     * Checks if the given token represents a zero-argument constant.
     * <p>
     * This method looks up the token's symbol in the {@link ExpressionElements} registry
     * and verifies if the associated {@link ExpressionElement} is an instance of {@link Constant}.
     *
     * @param token the token to check
     * @return true if the token is a zero-argument constant, false otherwise
     */
    private boolean isZeroArgConstant(Token token) {
        Optional<ExpressionElement> expressionElementOptional = ExpressionElements.findBySymbol(token.getValue());
        return expressionElementOptional.isPresent() && expressionElementOptional.get().getClass() == Constant.class;
    }

    /**
     * Checks if the given character represents the absolute value sign.
     *
     * @param c the character to check
     * @return true if the character is the absolute value sign, false otherwise
     */
    private boolean isAbsoluteValueSign(char c) {
        return String.valueOf(c).equals(ExpressionElements.SURRFUNC_ABS_S);
    }

    /**
     * Decide whether the character {@code c} starts a numeric literal.
     *
     * <p>
     * Sign characters ({@code '+'} / {@code '-'}) are <em>never</em> folded into a
     * number literal; they are emitted as separate {@link Token.Type#UNARY_OPERATOR}
     * (or binary {@link Token.Type#OPERATOR}) tokens by {@link #tokenizeSignRun}. This
     * preserves the precedence rule that {@code -3^2 == -(3^2) == -9} (unary minus
     * binds looser than {@code '^'}); sign-absorption would wrongly collapse it into
     * {@code (-3)^2 == 9}. A numeric literal therefore begins only with a digit or a
     * decimal point.
     * </p>
     *
     * @param c the character to test
     * @return {@code true} if {@code c} is a digit or {@code '.'}, {@code false} otherwise
     */
    private boolean startsNumericLiteral(final char c) {
        return isDigitOrDecimal(c);
    }

    /**
     * Boundary marker injected by {@link #removeWhitespace(String)} where whitespace
     * separates two operand characters ({@code "3 4"}) or two sign characters
     * ({@code "5 - -3"}). It is a non-typeable control character that acts purely as a
     * hard token boundary and is skipped by the main scan loop.
     */
    private static final char WHITESPACE_BOUNDARY = '';

    /**
     * Decides whether a {@code '+'} or {@code '-'} at {@code index} is a <em>prefix
     * unary</em> sign applied to a following non-numeric operand (a parenthesised
     * group, a function call, a constant or a variable) — e.g. the leading {@code -}
     * in {@code -(3+4)}, {@code -sin(0)}, {@code -x} or {@code 2*-(1+1)}.
     *
     * <p>
     * Classifies a single sign by the preceding token. After a {@code NUMBER},
     * {@code RIGHT_PAREN}, {@code CONSTANT}, {@code VARIABLE} or a pre-expanded
     * multi-argument {@code FUNCTION} the sign is binary; after the postfix
     * factorial {@code !} it is binary too ({@code 3!-2}); otherwise (start of
     * expression, after {@code (}, after a binary operator, or after {@code ;})
     * it is unary.
     * </p>
     *
     * @param expression the full (whitespace-normalised) expression; must not be {@code null}
     * @param index      position of the candidate sign character
     * @param tokens     tokens produced so far; must not be {@code null}
     * @return {@code true} if the sign should be emitted as a prefix unary operator
     */
    private boolean isUnarySignStart(final String expression, final int index, final List<Token> tokens) {
        final char c = expression.charAt(index);
        if (!isSignChar(c)) {
            return false;
        }
        if (tokens.isEmpty()) {
            return true;
        }
        final Token previous = tokens.getLast();
        switch (previous.getType()) {
            case NUMBER, RIGHT_PAREN, CONSTANT, VARIABLE -> {
                return false;
            }
            case FUNCTION -> {
                return false;
            }
            case OPERATOR -> {
                return !ExpressionElements.OP_FACTORIAL.equals(previous.getValue());
            }
            default -> {
                return true;
            }
        }
    }

    /**
     * Whether {@code c} can be part of an operand literal (digit, ASCII letter or the
     * decimal point) — used to decide whether whitespace between two such characters
     * is a forbidden silent merge such as {@code "3 4"}.
     *
     * @param c the character to test
     * @return {@code true} for {@code 0-9}, {@code A-Z}, {@code a-z} or {@code '.'}
     */
    private static boolean isOperandChar(final char c) {
        return (c >= '0' && c <= '9') || isAsciiLetter(c) || c == '.';
    }

    /**
     * Whether {@code c} is a prefix/binary sign character ({@code '+'} or {@code '-'}).
     * Used by {@link #removeWhitespace(String)} to preserve a whitespace boundary
     * between two signs (so that {@code "5 - -3"} is not collapsed by the
     * aggressive sign-run merger into a single net sign).
     *
     * @param c the character to test
     * @return {@code true} for {@code '+'} or {@code '-'}
     */
    private static boolean isSignChar(final char c) {
        return c == '+' || c == '-';
    }

    /**
     * Whether {@code c} is an ASCII letter ({@code A-Z} or {@code a-z}); the permitted
     * character set for variable names.
     *
     * @param c the character to test
     * @return {@code true} for {@code A-Z} or {@code a-z}
     */
    private static boolean isAsciiLetter(final char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    /**
     * Build an array of candidates representing registered three-argument functions.
     *
     * <p>This method performs two passes over the {@link ExpressionElements#getRegistry()}:
     * <ol>
     *   <li>First pass: counts how many registry entries are instances of {@link ThreeArgumentFunction}
     *       to allocate an array of the exact required size.</li>
     *   <li>Second pass: creates a {@link ThreeArgCandidate} for each matching entry and fills the array.</li>
     * </ol>
     *
     * <p>Finally, the resulting array is sorted in descending order by symbol length. Sorting
     * longest-first is important for lexical scanning: when attempting to match a function
     * symbol in the input expression, longer symbols must be tested before shorter ones so
     * that the tokenizer implements a maximal-munch strategy and avoids premature shorter matches.
     *
     * @return a non-null array of {@link ThreeArgCandidate} instances sorted by decreasing symbol length;
     * the array may be empty if no three-argument functions are registered
     */
    private static ThreeArgCandidate[] buildThreeArgumentFunctionCandidates() {
        int count = 0;
        for (final var element : ExpressionElements.getRegistry().values()) {
            if (element instanceof ThreeArgumentFunction) {
                count++;
            }
        }

        final ThreeArgCandidate[] candidates = new ThreeArgCandidate[count];
        int writeIndex = 0;

        for (final var entry : ExpressionElements.getRegistry().entrySet()) {
            final ExpressionElement element = entry.getValue();
            if (element instanceof ThreeArgumentFunction) {
                final String symbol = entry.getKey();
                candidates[writeIndex++] = new ThreeArgCandidate(symbol, element);
            }
        }

        Arrays.sort(candidates, (a, b) -> Integer.compare(b.symbolLength, a.symbolLength));
        return candidates;
    }

    /**
     * Tries to match a three-argument function starting at the given index.
     *
     * @param expression the full input expression
     * @param index      the index to start checking from
     * @return an Optional containing the matched function symbol, or empty if not found
     */
    private Optional<ExpressionElement> matchThreeArgumentFunction(String expression, int index) {
        if (expression == null) {
            return Optional.empty();
        }

        final int expressionLength = expression.length();
        if (index < 0 || index >= expressionLength) {
            return Optional.empty();
        }

        final char firstChar = expression.charAt(index);

        for (final ThreeArgCandidate candidate : THREE_ARGUMENT_FUNCTION_CANDIDATES) {
            if (candidate.symbol.charAt(0) != firstChar) {
                continue;
            }

            final int afterSymbolIndex = index + candidate.symbolLength;
            if (afterSymbolIndex >= expressionLength) {
                continue;
            }

            if (expression.charAt(afterSymbolIndex) != ExpressionElements.PAR_LEFT.charAt(0)) {
                continue;
            }

            if (expression.regionMatches(index, candidate.symbol, 0, candidate.symbolLength)) {
                return Optional.of(candidate.element);
            }
        }

        return Optional.empty();
    }

    /**
     * Normalises the active input locale's decimal separator to the canonical {@code '.'}.
     *
     * <p>
     * For the default separator {@code '.'} this is a no-op and the input is returned unchanged,
     * preserving the legacy US-canonical grammar exactly. Otherwise (strict locale parsing) a
     * literal {@code '.'} is rejected as an invalid character — it is not the decimal separator of
     * the active locale — and every {@code decimalSeparator} is rewritten to {@code '.'}. The
     * argument separator {@code ';'} is never touched.
     * </p>
     *
     * @param input            the raw (abs-normalized) expression
     * @param decimalSeparator the active input locale's decimal separator
     * @return the expression with the locale decimal separator rewritten to {@code '.'}
     * @throws SyntaxErrorException if a literal {@code '.'} appears under a non-{@code '.'} locale
     */
    private String normalizeDecimalSeparator(final String input, final char decimalSeparator) {
        if (decimalSeparator == '.') {
            return input;
        }
        final int dotIndex = input.indexOf('.');
        if (dotIndex >= 0) {
            throw new SyntaxErrorException(
                    CalculatorErrorCode.SYNTAX_INVALID_CHARACTER,
                    Map.of("character", "."),
                    "Invalid character at position " + dotIndex
                            + ": '.' is not the decimal separator for this locale (expected '"
                            + decimalSeparator + "')",
                    dotIndex);
        }
        return input.replace(decimalSeparator, '.');
    }

    /**
     * Removes whitespace from the input, inserting a {@link #WHITESPACE_BOUNDARY} sentinel where the
     * gap is semantically significant so that whitespace acts as a separator rather than a no-op.
     *
     * <p>A boundary sentinel is inserted when whitespace sits between two operand characters
     * (letter/digit/{@code '.'} on both sides), so {@code "3 4"} stays two tokens instead of merging
     * into {@code "34"}; and between two sign characters ({@code '+'}/{@code '-'} on both sides), so the
     * sign-run merge in {@link #tokenizeSignRun} cannot collapse a deliberate gap — {@code "5 - -3"}
     * parses as {@code 5}, binary {@code '-'}, unary {@code '-'}, {@code 3} rather than a single net
     * sign.</p>
     *
     * @param input the string to process
     * @return the input with whitespace removed and boundary sentinels inserted where required
     */
    private String removeWhitespace(final String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        final int length = input.length();
        final StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            final char charAt = input.charAt(i);
            if (!Character.isWhitespace(charAt)) {
                stringBuilder.append(charAt);
                continue;
            }
            int j = i;
            while (j < length && Character.isWhitespace(input.charAt(j))) {
                j++;
            }
            if (j < length && stringBuilder.length() > 0) {
                char prev = stringBuilder.charAt(stringBuilder.length() - 1);
                char next = input.charAt(j);
                boolean operandPair = isOperandChar(prev) && isOperandChar(next);
                boolean signPair = isSignChar(prev) && isSignChar(next);
                if (operandPair || signPair) {
                    stringBuilder.append(WHITESPACE_BOUNDARY);
                }
            }
            i = j - 1;
        }

        return stringBuilder.toString();
    }

    /**
     * Checks if the given character is a digit or a decimal point.
     *
     * @param c the character to check
     * @return true if the character is a digit or '.', false otherwise
     */
    private boolean isDigitOrDecimal(char c) {
        return Character.isDigit(c) || c == '.';
    }

    /**
     * Determines if the given character is a left parenthesis, according to the expression elements registry.
     *
     * @param character the character to check
     * @return true if the character is a left parenthesis, false otherwise
     */
    private boolean isLeftParenthesis(char character) {
        return ExpressionElements.findBySymbol(String.valueOf(character))
                .filter(element -> element instanceof Parenthesis && ((Parenthesis) element).isLeft())
                .isPresent();
    }

    /**
     * Determines if the given character is a right parenthesis, according to the expression elements registry.
     *
     * @param character the character to check
     * @return true if the character is a right parenthesis, false otherwise
     */
    private boolean isRightParenthesis(char character) {
        return ExpressionElements.findBySymbol(String.valueOf(character))
                .filter(element -> element instanceof Parenthesis && ((Parenthesis) element).isRight())
                .isPresent();
    }

    /**
     * Determines if the given character is a separator, according to the expression elements registry.
     *
     * @param character the character to check
     * @return true if the character is a separator, false otherwise
     */
    private boolean isSeparator(char character) {
        return ExpressionElements.findBySymbol(String.valueOf(character))
                .filter(element -> element instanceof Separator)
                .isPresent();
    }

    /**
     * Parse a numeric literal from {@code expression} starting at {@code startIndex} and append a
     * {@link Token} of type {@code NUMBER} to the provided {@code tokens} list.
     *
     * <p>
     * This method recognizes an optional leading sign character ('+' or '-') followed by one or more
     * digit or decimal characters ('.'). It returns the index immediately after the last character
     * that belongs to the parsed number (i.e. the next position to be processed by the tokenizer).
     * </p>
     *
     * <p><strong>Normalization rules</strong> applied to the parsed number string before adding the
     * token:</p>
     * <ul>
     *   <li>If the parsed number starts with a leading plus sign ('+'), that leading '+' is removed
     *       (e.g. {@code "+2"} → {@code "2"}).</li>
     *   <li>If the parsed number starts with a minus sign ('-'), the minus is preserved (e.g.
     *       {@code "-2"} → {@code "-2"}).</li>
     * </ul>
     *
     * <p><strong>Side-effect</strong>: a new {@code Token} of type {@code NUMBER} containing the
     * normalized numeric string is appended to {@code tokens}.</p>
     *
     * <h3>Examples</h3>
     * <ul>
     *   <li>Given {@code expression = "+3+4"} and {@code startIndex = 0}, this method appends
     *       {@code Token(Type.NUMBER, "3")} and returns {@code 2} (position of the next '+').</li>
     *   <li>Given {@code expression = "-5*2"} and {@code startIndex = 0}, this method appends
     *       {@code Token(Type.NUMBER, "-5")} and returns {@code 2} (position of '*').</li>
     * </ul>
     *
     * @param expression the full input expression string (must not be {@code null})
     * @param startIndex index in {@code expression} where the number starts (0-based)
     * @param tokens     the list to which the created NUMBER token will be appended (must not be {@code null})
     * @return the index immediately after the parsed number (the next character to read)
     * @throws IndexOutOfBoundsException if {@code startIndex} is outside {@code expression}'s bounds
     * @throws NullPointerException      if {@code expression} or {@code tokens} is {@code null}
     */
    private int tokenizeNumber(String expression, int startIndex, List<Token> tokens) {
        int currentIndex = startIndex;
        boolean decimalPointSeen = false;

        while (currentIndex < expression.length() && isDigitOrDecimal(expression.charAt(currentIndex))) {
            if (expression.charAt(currentIndex) == '.') {
                if (decimalPointSeen) {
                    throw new SyntaxErrorException(
                            CalculatorErrorCode.SYNTAX_INVALID_CHARACTER,
                            Map.of("character", "."),
                            "Invalid number literal at position " + startIndex + ": multiple decimal points",
                            currentIndex);
                }
                decimalPointSeen = true;
            }
            currentIndex++;
        }

        tokens.add(new Token(Token.Type.NUMBER, expression.substring(startIndex, currentIndex)));
        return currentIndex;
    }

    /**
     * Attempts to match and consume an operator, function, or constant symbol from the
     * expression starting at the given index. It tries to match the longest possible symbol
     * first, based on the known set of valid operators and functions.
     * <p>
     * Special cases are handled for the constants "pi" and "e" which are converted to number tokens
     * with their corresponding {@link BigNumbers} values.
     * <p>
     * The method also verifies the validity of the factorial operator '!' to ensure it follows
     * a number or closing parenthesis.
     * <p>
     * If a valid operator or function is matched, a corresponding token is added to the token list.
     *
     * @param expression the input mathematical expression string to parse
     * @param startIndex the position in the expression to start matching from
     * @param tokens     the list of tokens to append new tokens to if a match is found
     * @return the length of the matched symbol (number of characters consumed),
     * or 0 if no operator or function matched at the current position
     * @throws IllegalArgumentException if the factorial operator '!' is found in an invalid position
     * @throws NullPointerException     if expression or tokens is null
     */
    private int getLengthOfMatchingOperatorOrFunction(String expression, int startIndex, List<Token> tokens) {
        int maxTokenLength = ExpressionElements.getMaxTokenLength();

        for (int length = maxTokenLength; length > 0; length--) {
            int endIndex = startIndex + length;
            if (endIndex > expression.length()) {
                continue;
            }

            String candidate = expression.substring(startIndex, endIndex);

            Optional<ExpressionElement> zeroArg = ExpressionElements.findBySymbol(candidate).filter(element -> element instanceof Constant);
            if (zeroArg.isPresent()) {
                tokens.add(new Token(Token.Type.CONSTANT, zeroArg.get().getSymbol()));
                return length;
            }

            if (VALID_OPERATORS_AND_FUNCTIONS.contains(candidate)) {
                if (candidate.equalsIgnoreCase(ExpressionElements.OP_FACTORIAL)) {
                    if (tokens.isEmpty()) {
                        throw new SyntaxErrorException(
                                CalculatorErrorCode.SYNTAX_INVALID_FACTORIAL,
                                Map.of(),
                                "Factorial '!' must follow a number, constant, variable, or closing parenthesis",
                                null);
                    }

                    Token previous = tokens.getLast();
                    if (!(previous.getType() == Token.Type.NUMBER
                            || previous.getType() == Token.Type.RIGHT_PAREN
                            || previous.getType() == Token.Type.VARIABLE
                            || previous.getType() == Token.Type.CONSTANT)) {
                        throw new SyntaxErrorException(
                                CalculatorErrorCode.SYNTAX_INVALID_FACTORIAL,
                                Map.of(),
                                "Factorial '!' must follow a number, constant, variable, or closing parenthesis",
                                null);
                    }

                    tokens.add(new Token(Token.Type.OPERATOR, ExpressionElements.OP_FACTORIAL));
                    return length;
                }

                ExpressionElement expressionElement = ExpressionElements.findBySymbol(candidate).orElseThrow();
                Token.Type type = expressionElement.isFunction() ? Token.Type.FUNCTION : Token.Type.OPERATOR;
                tokens.add(new Token(type, candidate));
                return length;
            }
        }

        StringBuilder variable = new StringBuilder();
        while (startIndex < expression.length() && isAsciiLetter(expression.charAt(startIndex))) {
            variable.append(expression.charAt(startIndex));
            startIndex++;
        }
        if (!variable.isEmpty()) {
            tokens.add(new Token(Token.Type.VARIABLE, variable.toString()));
            return variable.length();
        }

        return 0;
    }

    /**
     * Finds the index of the closing parenthesis that matches the opening parenthesis
     * at the specified position in the expression string.
     * <p>
     * This method tracks the nesting depth of parentheses starting from {@code openIndex}.
     * It increments the depth for each left parenthesis and decrements for each right parenthesis.
     * When the depth returns to zero, the matching closing parenthesis is found.
     *
     * @param expression the expression string to search
     * @param openIndex  the index of the opening parenthesis to match
     * @return the index of the matching closing parenthesis, or -1 if not found
     */
    private int findClosingParenthesis(String expression, int openIndex) {
        if (openIndex >= expression.length() || !isLeftParenthesis(expression.charAt(openIndex))) {
            return -1;
        }

        int depth = 0;
        boolean opened = false;
        for (int i = openIndex; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (isLeftParenthesis(c)) {
                depth++;
                opened = true;
            } else if (isRightParenthesis(c)) {
                depth--;
            }

            if (opened && depth == 0) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Splits a string on the given separator character, but only at the top nesting level (depth 0).
     * Separators inside parentheses (or any registered left/right parenthesis pair) are preserved
     * in the produced parts. Used by the three-argument-function tokenizer path so that nested
     * expressions like {@code iif(max(a;b);c;d)} are not mis-split.
     *
     * @param input    the inner expression text (must not be {@code null})
     * @param separator the top-level separator character
     * @return list of substrings; never {@code null}, may be empty if {@code input} is empty
     */
    private List<String> splitTopLevel(final String input, final char separator) {
        final List<String> parts = new ArrayList<>();
        int depth = 0;
        int partStart = 0;
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (isLeftParenthesis(c)) {
                depth++;
            } else if (isRightParenthesis(c)) {
                depth--;
            } else if (c == separator && depth == 0) {
                parts.add(input.substring(partStart, i));
                partStart = i + 1;
            }
        }
        parts.add(input.substring(partStart));
        return parts;
    }

}
