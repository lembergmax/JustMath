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

import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.errors.CalculatorErrorCode;
import com.mlprograms.justmath.calculator.exceptions.SyntaxErrorException;
import com.mlprograms.justmath.calculator.expression.ExpressionElement;
import com.mlprograms.justmath.calculator.expression.ExpressionElements;
import com.mlprograms.justmath.calculator.expression.elements.Constant;
import com.mlprograms.justmath.calculator.expression.elements.Parenthesis;
import com.mlprograms.justmath.calculator.expression.elements.Separator;
import com.mlprograms.justmath.calculator.expression.elements.function.ThreeArgumentFunction;
import com.mlprograms.justmath.calculator.internal.Token;
import lombok.NonNull;

import java.math.MathContext;
import java.util.*;

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
        // Reject the internal whitespace-boundary sentinel if it appears in caller input.
        // The sentinel is a non-typeable control character injected by removeWhitespace to
        // mark deliberate token splits; if user input already contains it, the main scan
        // loop would silently skip it (collapsing e.g. "1<sentinel>2" into "12") and let an
        // attacker smuggle a boundary past a naive input filter. Treat it as an invalid
        // character with a precise position instead.
        final int sentinelIndex = input.indexOf(WHITESPACE_BOUNDARY);
        if (sentinelIndex >= 0) {
            throw new SyntaxErrorException(
                    CalculatorErrorCode.SYNTAX_INVALID_CHARACTER,
                    Map.of("character", String.valueOf(WHITESPACE_BOUNDARY)),
                    "Invalid control character at position " + sentinelIndex,
                    sentinelIndex);
        }

        // Tracks whether the next absolute-value bar opens or closes a context.
        boolean nextAbsoluteIsOpen = true;

        List<Token> tokens = new ArrayList<>();
        String expression = removeWhitespace(input);
        int index = 0;

        while (index < expression.length()) {
            char character = expression.charAt(index);

            // A whitespace boundary marker only forces a token split; consume and skip.
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

                // Split on the top-level separator only — nested parentheses must not be broken up,
                // otherwise e.g. iif(max(a;b);c;d) would be mis-parsed.
                List<String> parts = splitTopLevel(inside, ExpressionElements.SEP_SEMICOLON.charAt(0));
                if (parts.size() != 3) {
                    throw new SyntaxErrorException(
                            CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT,
                            Map.of("function", symbol, "expected", "3", "actual", String.valueOf(parts.size())),
                            "Function '" + symbol + "' must have three arguments",
                            null);
                }

                tokens.add(new Token(Token.Type.NUMBER, parts.get(0)));
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

        // Insert implicit multiplication tokens where necessary. Signs are never
        // folded into number literals and consecutive '+'/'-' runs are already
        // collapsed by tokenizeSignRun, so no post-pass normalisation is needed.
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
            // Unary '+' is a no-op: emit nothing.
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

            // Skip if the next token is an operator or semicolon – no implicit * needed
            if (next.getType() == Token.Type.OPERATOR || next.getType() == Token.Type.SEMICOLON) {
                continue;
            }

            // Insert * where implicit multiplication is likely
            if (needsMultiplicationSign(current, next)) {
                tokens.add(i + 1, new Token(Token.Type.OPERATOR, ExpressionElements.OP_MULTIPLY));
                i++; // Skip the inserted token
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
        final Token previous = tokens.get(tokens.size() - 1);
        switch (previous.getType()) {
            case NUMBER, RIGHT_PAREN, CONSTANT, VARIABLE -> {
                return false;
            }
            case FUNCTION -> {
                // Trailing token of a pre-expanded multi-argument function
                // (e.g. "summation(1;3;k)") — a completed operand, so the sign
                // is binary. Real prefix functions are followed by '(', never a sign.
                return false;
            }
            case OPERATOR -> {
                // Binary after the postfix factorial ("3!-2"); unary after any
                // other (binary) operator (e.g. "2*-3").
                return !ExpressionElements.OP_FACTORIAL.equals(previous.getValue());
            }
            default -> {
                // LEFT_PAREN, SEMICOLON
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
     * Removes all whitespace characters from the input string.
     *
     * @param input the string to process
     * @return the input string with all whitespace removed
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
            // Whitespace is a separator, not a no-op:
            //   1) Between two operand characters (letter/digit/'.' on BOTH sides)
            //      operands must not silently merge ("3 4" → not "34"); a boundary
            //      sentinel is inserted so they stay separate tokens.
            //   2) Between two sign characters ('+' or '-' on both sides) the
            //      aggressive sign-run merger in tokenizeSignRun must not collapse
            //      them across a deliberate whitespace gap. "5 - -3" must parse
            //      as 5, binary '-', unary '-', 3 — not as a single net sign.
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

        // Note: a space may be replaced 1:1 by the boundary sentinel, so the length
        // can stay equal while the content changed — always return the rebuilt string.
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

        // Sign characters are never folded into a number literal (see
        // startsNumericLiteral); the input here always begins with a digit
        // or decimal point. Walk forward over the operand chars.
        // A number literal may contain at most one '.': "1.2.3" is rejected
        // with a clear syntax error rather than silently producing one token.
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
                    // must not be in the beginning or after another expressionElement.
                    // Check emptiness BEFORE getLast(): a leading '!' (empty token list)
                    // would otherwise raise an uncaught NoSuchElementException that is
                    // misclassified as a generic processing error instead of a syntax error.
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
        // Variable names are restricted to ASCII letters. Registered non-ASCII symbols
        // (pi, the square-root sign, Greek letters, etc.) are matched earlier via the
        // registry; any remaining non-ASCII character is a genuine invalid character
        // and is reported as such by the caller instead of becoming an unknown variable.
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
