/*
 * Copyright (c) 2025 Max Lemberg
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
import com.mlprograms.justmath.calculator.exceptions.SyntaxErrorException;
import com.mlprograms.justmath.calculator.expression.ExpressionElement;
import com.mlprograms.justmath.calculator.expression.ExpressionElements;
import com.mlprograms.justmath.calculator.expression.elements.Constant;
import com.mlprograms.justmath.calculator.expression.elements.Parenthesis;
import com.mlprograms.justmath.calculator.expression.elements.Separator;
import com.mlprograms.justmath.calculator.expression.elements.function.ThreeArgumentFunction;
import com.mlprograms.justmath.calculator.expression.elements.operator.PostfixUnaryOperator;
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
 *   <li>Splitting signed numbers that appear immediately after closing parentheses into
 *       separate operator and number tokens (e.g., ") -5" becomes [")", "-", "5"]).</li>
 *   <li>Inserting implicit multiplication tokens where multiplication is implied by juxtaposition,
 *       such as between a number and a parenthesis ("2(3)"), or between parentheses and functions.</li>
 *   <li>Merging consecutive '+' and '-' operators into a single normalized operator token,
 *       respecting arithmetic sign rules.</li>
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
    private static final Set<String> VALID_OPERATORS_AND_FUNCTIONS = ExpressionElements.registry.keySet();

    /**
     * Precomputed candidates for registered three-argument functions.
     *
     * <p>This array is constructed once at class initialization by scanning
     * {@link ExpressionElements#registry} for instances of {@link ThreeArgumentFunction}.
     * Candidates are sorted by decreasing symbol length to ensure a maximal-munch
     * matching strategy during tokenization.</p>
     */
    private static final ThreeArgCandidate[] THREE_ARGUMENT_FUNCTION_CANDIDATES =
            buildThreeArgumentFunctionCandidates();

    /**
     * Tracks whether the next encountered absolute value sign (|) should be treated as an opening or closing.
     * Used to alternate between opening and closing absolute value contexts during tokenization.
     */
    private boolean nextAbsoluteIsOpen = true;

    /**
     * Scans the given token list for occurrences where a signed number directly follows
     * a closing parenthesis token (e.g. ") -5"). In such cases, the signed number token
     * is split into an operator token ('+' or '-') and a separate unsigned number token.
     * This is necessary because expressions like "(-3) + 5" or "(2) -4" are tokenized
     * initially with signed number tokens which must be separated for correct parsing.
     * <p>
     * This method modifies the list in place.
     *
     * @param tokens the list of tokens to scan and fix
     * @throws NullPointerException if {@code tokens} is null
     */
    private void splitSignedNumbersAfterParentheses(List<Token> tokens) {
        for (int i = 0; i < tokens.size() - 1; i++) {
            Token current = tokens.get(i);
            Token next = tokens.get(i + 1);
            if (current.getType() == Token.Type.RIGHT_PAREN && next.getType() == Token.Type.NUMBER) {
                String value = next.getValue();
                if ((value.startsWith("+") || value.startsWith("-")) && value.length() > 1) {
                    tokens.set(i + 1, new Token(Token.Type.OPERATOR, value.substring(0, 1)));
                    tokens.add(i + 2, new Token(Token.Type.NUMBER, value.substring(1)));
                    i++; // skip the inserted number token to avoid an infinite loop
                }
            }
        }
    }

    /**
     * Tokenizes the input mathematical expression string into a list of {@link Token} objects.
     * <p>
     * This method performs lexical analysis by scanning the input expression character by character.
     * It recognizes numbers (including signed numbers), parentheses, separators, operators,
     * functions, constants (such as pi and e), and inserts implicit multiplication tokens where
     * applicable. It also merges consecutive '+' and '-' operators into a single operator token
     * for normalization.
     * <p>
     * The token list returned by this method is suitable for further syntactic parsing and evaluation.
     *
     * @param input the mathematical expression to tokenize, as a string
     * @return a list of tokens representing the lexemes of the expression
     * @throws IllegalArgumentException if the input contains invalid characters or malformed expressions
     * @throws NullPointerException     if the input string is null
     */
    public List<Token> tokenize(@NonNull final String input) {
        List<Token> tokens = new ArrayList<>();
        String expression = removeWhitespace(input);
        int index = 0;

        while (index < expression.length()) {
            char character = expression.charAt(index);
            Optional<ExpressionElement> matchedFunction = matchThreeArgumentFunction(expression, index);

            if (isSignedNumberStart(expression, index, tokens)) {
                index = tokenizeNumber(expression, index, tokens);
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
                int closingParenthesis = findClosingParenthesis(expression, functionStart);
                if (closingParenthesis < 0) {
                    throw new SyntaxErrorException("Unmatched '(' in function: " + symbol);
                }

                String inside = expression.substring(functionStart + 1, closingParenthesis);

                String[] parts = inside.split(ExpressionElements.SEP_SEMICOLON, 3);
                if (parts.length != 3) {
                    throw new SyntaxErrorException("Function '" + symbol + "' must have three arguments");
                }

                tokens.add(new Token(Token.Type.NUMBER, parts[0]));
                tokens.add(new Token(Token.Type.NUMBER, parts[1]));
                tokens.add(new Token(Token.Type.STRING, parts[2]));
                tokens.add(new Token(Token.Type.FUNCTION, symbol));

                index = closingParenthesis + 1;
            } else {
                int lengthOfMatch = getLengthOfMatchingOperatorOrFunction(expression, index, tokens);
                if (lengthOfMatch > 0) {
                    index += lengthOfMatch;
                } else {
                    throw new SyntaxErrorException("Invalid character at position " + index + ": " + character);
                }
            }
        }

        // Fix cases like ") -5" → OPERATOR(-), NUMBER(5)
        splitSignedNumbersAfterParentheses(tokens);

        // Insert implicit multiplication tokens where necessary
        insertImplicitMultiplicationTokens(tokens);

        // Merge consecutive + and - operators
        mergeConsecutiveSignOperators(tokens);

        return tokens;
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
     * Decide whether the character at {@code index} in {@code expression} starts a (possibly signed) numeric literal.
     *
     * <p>
     * The decision uses both the raw character context in {@code expression} and the already-produced {@code tokens}
     * for lexical context (this is important because previously emitted tokens may represent multi-character
     * constructs such as function names). The method therefore determines whether a leading '+' or '-' should be
     * treated as part of a number (unary sign) or as a binary operator.
     * </p>
     *
     * <p><strong>Rules implemented</strong>:
     * <ul>
     *   <li>If the character at {@code index} is neither '+' nor '-', this method returns whether it is a digit
     *       or a decimal point.</li>
     *   <li>If the character is '+' or '-' it must be followed by a digit or decimal point to be considered a number
     *       start; otherwise it is not a number start.</li>
     *   <li>If no tokens have been produced yet, a leading '+' or '-' starts a number (e.g. {@code "+2"} or {@code "-2"}).</li>
     *   <li>If the last produced token is a {@code NUMBER}, {@code RIGHT_PAREN}, {@code CONSTANT} or {@code VARIABLE},
     *       then '+' / '-' is treated as a binary operator (not part of a number).</li>
     *   <li>If the last produced token is a {@code LEFT_PAREN}, only a '-' is treated as a unary sign (so {@code "(+2)"}
     *       is tokenized as {@code '(', '+', '2', ')'}, whereas {@code "(-2)"} produces {@code '(', NUMBER("-2"), ')'}).</li>
     *   <li>If the last produced token is an {@code OPERATOR}, {@code FUNCTION} or {@code SEMICOLON}, the '+' / '-'
     *       is treated as a unary sign for the following number (e.g. {@code "2*-3"}).</li>
     * </ul>
     * </p>
     *
     * <h4>Examples</h4>
     * <ul>
     *   <li>{@code isSignedNumberStart("+2", 0, emptyTokens)} → {@code true}</li>
     *   <li>{@code isSignedNumberStart("-5", 0, emptyTokens)} → {@code true}</li>
     *   <li>Given tokens ending with {@code RIGHT_PAREN}: {@code isSignedNumberStart("+3", idx, tokens)} → {@code false}</li>
     *   <li>Given tokens ending with {@code OPERATOR}: {@code isSignedNumberStart("-3", idx, tokens)} → {@code true}</li>
     *   <li>Inside parentheses: {@code isSignedNumberStart("+2", idx, tokensWithLastLeftParen)} → {@code false}</li>
     * </ul>
     *
     * @param expression full input expression (must not be {@code null})
     * @param index      index in {@code expression} to test (0-based). The caller must ensure {@code index} is valid.
     * @param tokens     the list of tokens that have already been produced while tokenizing the expression;
     *                   this method inspects the last produced token to decide context (must not be {@code null}).
     * @return {@code true} if the character at {@code index} should be interpreted as starting a numeric token
     * (including an optional unary '+' or '-' sign), {@code false} otherwise
     * @throws IndexOutOfBoundsException if {@code index} is outside the bounds of {@code expression}
     * @throws NullPointerException      if {@code expression} or {@code tokens} is {@code null}
     */
    private boolean isSignedNumberStart(String expression, int index, List<Token> tokens) {
        char c = expression.charAt(index);

        // If not + or -, it's a normal digit start decision
        if (!(c == '+' || c == '-')) {
            return isDigitOrDecimal(c);
        }

        // must be followed by digit or decimal
        if (index + 1 >= expression.length() || !isDigitOrDecimal(expression.charAt(index + 1))) {
            return false;
        }

        // If nothing parsed yet => sign at start of expression is part of number
        if (tokens.isEmpty()) {
            return true;
        }

        // Look at last produced token for context
        Token previous = tokens.getLast(); // safe for any List implementation
        Token.Type prevType = previous.getType();

        // If previous is a number, right paren, constant or variable => + / - is BINARY operator
        if (prevType == Token.Type.NUMBER
                || prevType == Token.Type.RIGHT_PAREN
                || prevType == Token.Type.CONSTANT
                || prevType == Token.Type.VARIABLE) {
            return false;
        }

        // If previous is a left paren: only '-' is unary sign (so "(+2)" -> "(, +, 2, )")
        if (prevType == Token.Type.LEFT_PAREN) {
            return c == '-';
        }

        // If previous is an operator, we must treat postfix-unary operators (like '!') specially:
        if (prevType == Token.Type.OPERATOR) {
            // If the previous operator is a postfix-unary operator (e.g. '!'), then + / - is NOT a unary sign.
            // Otherwise (previous is a binary operator like '*' or a prefix operator), + / - is a unary sign.
            return ExpressionElements.findBySymbol(previous.getValue())
                    .map(element -> !(element instanceof PostfixUnaryOperator))
                    .orElse(true);
        }

        // If previous is FUNCTION or SEMICOLON -> + / - is a sign of number (e.g. 2*-3 or func(-2))
        return prevType == Token.Type.FUNCTION || prevType == Token.Type.SEMICOLON;
    }

    /**
     * Build an array of candidates representing registered three-argument functions.
     *
     * <p>This method performs two passes over the {@link ExpressionElements#registry}:
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
        for (final var element : ExpressionElements.registry.values()) {
            if (element instanceof ThreeArgumentFunction) {
                count++;
            }
        }

        final ThreeArgCandidate[] candidates = new ThreeArgCandidate[count];
        int writeIndex = 0;

        for (final var entry : ExpressionElements.registry.entrySet()) {
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
            }
        }

        return stringBuilder.length() == length ? input : stringBuilder.toString();
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

        if (currentIndex < expression.length()) {
            char fc = expression.charAt(currentIndex);
            if (fc == '+' || fc == '-') {
                currentIndex++;
            }
        }

        while (currentIndex < expression.length() && isDigitOrDecimal(expression.charAt(currentIndex))) {
            currentIndex++;
        }

        String rawNumber = expression.substring(startIndex, currentIndex);

        if (rawNumber.startsWith("+")) {
            rawNumber = rawNumber.substring(1);
        }

        tokens.add(new Token(Token.Type.NUMBER, rawNumber));
        return currentIndex;
    }

    /**
     * Collapses sequences of consecutive '+' and '-' operator tokens in the token list
     * into a single operator token. The result is determined by the parity of the number
     * of '-' operators in the sequence: an even number of '-' results in '+', an odd number in '-'.
     * <p>
     * For example, the sequence "--" becomes "+", "---" becomes "-".
     * <p>
     * This method modifies the list of tokens in place.
     *
     * @param tokens the list of tokens to be processed and mutated
     * @throws NullPointerException if {@code tokens} is null
     */
    private void mergeConsecutiveSignOperators(List<Token> tokens) {
        List<Token> mergedTokens = new ArrayList<>();
        int i = 0;

        while (i < tokens.size()) {
            Token token = tokens.get(i);

            if (token.getType() == Token.Type.OPERATOR && (token.getValue().equals("+") || token.getValue().equals("-"))) {
                int minusCount = 0;

                while (i < tokens.size()
                        && tokens.get(i).getType() == Token.Type.OPERATOR
                        && (tokens.get(i).getValue().equals("+") || tokens.get(i).getValue().equals("-"))) {
                    if (tokens.get(i).getValue().equals("-")) {
                        minusCount++;
                    }
                    i++;
                }

                String resolvedOperator = (minusCount % 2 == 0) ? "+" : "-";
                mergedTokens.add(new Token(Token.Type.OPERATOR, resolvedOperator));
            } else {
                mergedTokens.add(token);
                i++;
            }
        }

        tokens.clear();
        tokens.addAll(mergedTokens);
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
                    // must not be in the beginning or after another expressionElement
                    Token previous = tokens.getLast();
                    if (tokens.isEmpty() ||
                            !(previous.getType() == Token.Type.NUMBER
                                    || previous.getType() == Token.Type.RIGHT_PAREN
                                    || previous.getType() == Token.Type.VARIABLE
                                    || previous.getType() == Token.Type.CONSTANT)) {
                        throw new SyntaxErrorException("Factorial '!' must follow a number, constant, variable, or closing parenthesis");
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
        while (startIndex < expression.length() && Character.isLetter(expression.charAt(startIndex))) {
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
        int depth = 0;
        for (int i = openIndex; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (isLeftParenthesis(c)) {
                depth++;
            } else if (isRightParenthesis(c)) {
                depth--;
            }

            if (depth == 0) {
                return i;
            }
        }

        return -1;
    }

}
