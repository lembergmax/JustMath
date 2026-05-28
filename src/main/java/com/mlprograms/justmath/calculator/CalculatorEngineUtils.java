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
import com.mlprograms.justmath.calculator.exceptions.CyclicVariableReferenceException;
import com.mlprograms.justmath.calculator.exceptions.SyntaxErrorException;
import com.mlprograms.justmath.calculator.expression.ExpressionElement;
import com.mlprograms.justmath.calculator.expression.ExpressionElements;
import com.mlprograms.justmath.calculator.expression.elements.Constant;
import com.mlprograms.justmath.calculator.expression.elements.function.CoordinateFunction;
import com.mlprograms.justmath.calculator.expression.elements.function.ThreeArgumentFunction;
import com.mlprograms.justmath.calculator.expression.elements.function.TwoArgumentFunction;
import com.mlprograms.justmath.calculator.expression.elements.function.UnlimitedArgumentFunction;
import com.mlprograms.justmath.calculator.expression.elements.operator.BinaryOperator;
import com.mlprograms.justmath.calculator.expression.elements.operator.UnaryOperator;
import com.mlprograms.justmath.calculator.expression.elements.operator.SimpleBinaryOperator;
import com.mlprograms.justmath.calculator.internal.Token;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;

public class CalculatorEngineUtils {

    /**
     * Replaces all occurrences of absolute value signs in a mathematical expression
     * with explicit function-style absolute value notation.
     * <p>
     * In many mathematical notations, absolute values are written using vertical bars, e.g. {@code |x+3|}.
     * Internally, however, this implementation represents absolute value as a function call
     * (e.g. {@code abs(x+3)}). This method ensures that each absolute value sign is consistently
     * converted to its functional form.
     * </p>
     *
     * <p>
     * The replacement follows this rule:
     * <ul>
     *   <li>The first occurrence of the absolute sign ({@link ExpressionElements#SURRFUNC_ABS_S})
     *       is replaced with {@code abs(} (opening the absolute value function).</li>
     *   <li>The second occurrence is replaced with a closing parenthesis {@code )}.</li>
     *   <li>The third again with {@code abs(}, the fourth with {@code )}, and so forth.</li>
     * </ul>
     * As a result, an even number of absolute signs is required to form valid pairs.
     * </p>
     *
     * <p>
     * For example:
     * <ul>
     *   <li>Input: {@code |x+2|} → Output: {@code abs(x+2)}</li>
     *   <li>Input: {@code |x-1| + |y|} → Output: {@code abs(x-1) + abs(y)}</li>
     * </ul>
     * </p>
     *
     * @param expression the mathematical expression containing absolute value signs
     * @return the expression with all absolute signs replaced by {@code abs(...)} notation
     * @throws IllegalArgumentException if the expression contains an odd number of
     *                                  absolute value signs, since this would result in unbalanced expressions
     *                                  (e.g., {@code |x+3}).
     */
    public static String replaceAbsSigns(String expression) {
        final char absSignCharacter = ExpressionElements.SURRFUNC_ABS_S.charAt(0);

        // Bail out early if the sign cardinality is already obviously wrong. A correct
        // expression must have an even number of {@code |} characters because every opening
        // bar needs a matching closer.
        final int occurrences = countOccurrences(expression, ExpressionElements.SURRFUNC_ABS_S);
        if (occurrences % 2 != 0) {
            throw new IllegalArgumentException(
                    "Expression must contain an even number of abs-sign characters ('|')");
        }

        // Context-aware open/close detection. The previous implementation alternated
        // open/close purely by position parity, so an input like {@code a|b|c|d|e} (four bars,
        // even count, all in operator-required positions) was rewritten to
        // {@code aabs(b)cabs(d)e} — syntactically nonsense that was only caught by downstream
        // validation with a diffuse error. By tracking whether we currently expect an operand
        // (so a bar opens an abs) or an operator (so a bar closes one), we both reject
        // misplaced bars early and produce correctly nested {@code abs(...)} output.
        final StringBuilder result = new StringBuilder(expression.length());
        boolean expectingOperand = true;
        int openAbsDepth = 0;

        for (int index = 0; index < expression.length(); index++) {
            final char currentChar = expression.charAt(index);

            if (currentChar == absSignCharacter) {
                if (expectingOperand) {
                    result.append(ExpressionElements.FUNC_ABS).append(ExpressionElements.PAR_LEFT);
                    openAbsDepth++;
                    // Inside an abs, the next character still starts an operand.
                    expectingOperand = true;
                } else {
                    if (openAbsDepth == 0) {
                        throw new IllegalArgumentException(
                                "Unmatched closing '|' at position " + index + " in expression: '" + expression + "'");
                    }
                    result.append(')');
                    openAbsDepth--;
                    expectingOperand = false;
                }
                continue;
            }

            result.append(currentChar);
            expectingOperand = expectsOperandAfter(currentChar, expectingOperand);
        }

        if (openAbsDepth != 0) {
            throw new IllegalArgumentException(
                    "Unmatched opening '|' in expression: '" + expression + "'");
        }
        return result.toString();
    }

    /**
     * Returns the {@code expectingOperand} state that applies after consuming {@code character},
     * given the previous state. Drives the open/close decision in {@link #replaceAbsSigns(String)}.
     *
     * <p>Whitespace and unknown characters are treated as transparent and pass the state through.
     * Operators, parentheses, and separators reset the state to "operand expected" because the
     * grammar requires an operand on the right-hand side of each of them. Digits, letters and
     * closing parens flip the state to "operator expected".</p>
     */
    private static boolean expectsOperandAfter(final char character, final boolean previousState) {
        if (Character.isWhitespace(character)) {
            return previousState;
        }
        switch (character) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '%':
            case '^':
            case '(':
            case ',':
            case ';':
                return true;
            case ')':
                return false;
            default:
                // Digits, decimal points, letters, and any registered function/constant text
                // produce values; after them we are inside an operator-expecting state.
                return !(Character.isLetterOrDigit(character) || character == '.');
        }
    }

    /**
     * Counts the number of occurrences of a given substring within a text.
     * <p>
     * This method performs a non-overlapping search for the {@code search} string
     * within the {@code text}, moving forward after each found occurrence.
     * </p>
     *
     * @param text   the input string to be scanned
     * @param search the substring to search for
     * @return the number of non-overlapping occurrences of {@code search} within {@code text};
     * {@code 0} if either {@code text} or {@code search} is empty (an empty text trivially
     * contains zero occurrences — the previous {@code -1} sentinel made the {@code replaceAbsSigns}
     * parity check report a spurious "odd number of bars" for empty input)
     */
    static int countOccurrences(@NonNull final String text, @NonNull final String search) {
        if (text.isEmpty() || search.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = text.indexOf(search, index)) != -1) {
            count++;
            index += search.length();
        }

        return count;
    }

    /**
     * Replaces variable tokens in the provided list with their corresponding values from the variable map.
     * If a variable is not defined in the map, throws an IllegalArgumentException.
     *
     * @param tokens    the list of tokens to process and replace variables in
     * @param variables a map of variable names with their BigNumber values
     * @throws IllegalArgumentException if a variable token does not have a corresponding value in the map
     */
    static void replaceVariables(@NonNull final CalculatorEngine calculatorEngine, @NonNull final List<Token> tokens, @NonNull final Map<String, String> variables) {
        checkVariablesForRecursion(calculatorEngine, variables);

        for (int i = 0; i < tokens.size(); i++) {
            final Token token = tokens.get(i);
            if (token.getType() == Token.Type.VARIABLE) {
                final String value = variables.get(token.getValue());

                if (value == null || value.isBlank()) {
                    throw new IllegalArgumentException("Variable '" + token.getValue() + "' is not defined.");
                }

                // Add zero to the evaluated variable value to coerce coordinate-style results into a single numeric value.
                // Example: evaluated value = "r=5; θ=53.13010235" -> "(r=5; θ=53.13010235) + 0 = 5"
                final String evaluatedVariableValue = calculatorEngine.evaluate(value, variables).add(BigNumbers.ZERO).toString();
                tokens.set(i, new Token(Token.Type.NUMBER, evaluatedVariableValue));
            }
        }
    }

    static void checkVariablesForRecursion(@NonNull final CalculatorEngine calculatorEngine, @NonNull final Map<String, String> variables) {
        Set<String> visitedVariables = new HashSet<>();
        Set<String> currentPath = new HashSet<>();

        for (final String variableName : variables.keySet()) {
            checkVariable(calculatorEngine, variableName, variables, visitedVariables, currentPath);
        }
    }

    private static void checkVariable(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String variableName, @NonNull final Map<String, String> variables, @NonNull final Set<String> visitedVariables, @NonNull final Set<String> currentPath) {
        if (currentPath.contains(variableName)) {
            throw new CyclicVariableReferenceException("Cyclic variable reference detected in: " + variableName);
        }

        if (visitedVariables.contains(variableName)) {
            return;
        }

        currentPath.add(variableName);

        final String expression = variables.get(variableName);
        if (expression != null && !expression.isEmpty()) {
            final List<Token> tokens = calculatorEngine.getTokenizer().tokenize(expression);

            for (final Token token : tokens) {
                if (token.getType() != Token.Type.VARIABLE) {
                    continue;
                }

                final String referencedVar = token.getValue();
                checkVariable(calculatorEngine, referencedVar, variables, visitedVariables, currentPath);
            }
        }

        currentPath.remove(variableName);
        visitedVariables.add(variableName);
    }

    /**
     * Structural validation of the <em>final</em> (post implicit-multiplication) infix
     * token list, run <em>before</em> variable substitution and the evaluator. It
     * rejects the structural defects that the postfix form can no longer see because
     * the shunting-yard parser discards parentheses:
     * <ul>
     *   <li>an empty parenthesis pair after a function — {@code sqrt()} — every
     *       function here needs at least one argument
     *       ({@link CalculatorErrorCode#SYNTAX_EMPTY_FUNCTION_ARGUMENT});</li>
     *   <li>a bare empty parenthesis pair {@code ()}
     *       ({@link CalculatorErrorCode#SYNTAX_EMPTY_PARENTHESES});</li>
     *   <li>a leading binary operator — {@code *5}, {@code /3} — (unary {@code +}/{@code -}
     *       are allowed) ({@link CalculatorErrorCode#SYNTAX_LEADING_OPERATOR});</li>
     *   <li>a trailing binary operator — {@code 5+}, {@code 50000!/}
     *       ({@link CalculatorErrorCode#SYNTAX_TRAILING_OPERATOR}).</li>
     * </ul>
     *
     * <p>
     * Catching these here means an expensive left-hand subexpression (such as a large
     * {@code !}) is never computed for an expression that cannot yield a result, and the
     * user gets a precise message instead of a wrong value or a generic error.
     * Everything that survives this check is handled by the arity dry-run
     * {@link #validatePostfixArity(List)}.
     * </p>
     *
     * @param tokens the tokenized (infix) expression; must not be {@code null}
     * @throws SyntaxErrorException on any of the structural defects listed above
     */
    static void validateInfixStructure(@NonNull final List<Token> tokens) {
        if (tokens.isEmpty()) {
            return;
        }

        for (int i = 0; i + 1 < tokens.size(); i++) {
            if (tokens.get(i).getType() == Token.Type.LEFT_PAREN
                    && tokens.get(i + 1).getType() == Token.Type.RIGHT_PAREN) {
                if (i > 0 && tokens.get(i - 1).getType() == Token.Type.FUNCTION) {
                    final String function = tokens.get(i - 1).getValue();
                    throw new SyntaxErrorException(
                            CalculatorErrorCode.SYNTAX_EMPTY_FUNCTION_ARGUMENT,
                            Map.of("function", function),
                            "Function '" + function + "' was called without an argument",
                            null);
                }
                throw new SyntaxErrorException(
                        CalculatorErrorCode.SYNTAX_EMPTY_PARENTHESES,
                        Map.of(),
                        "Empty parentheses '()' with no content",
                        null);
            }
        }

        validateFunctionArgumentCounts(tokens);

        for (int i = 0; i + 1 < tokens.size(); i++) {
            final Token a = tokens.get(i);
            final Token b = tokens.get(i + 1);
            // Two number literals with no operator and no implied multiplication
            // between them — e.g. the whitespace-separated "3 4". (Tokenizer implicit
            // multiplication already bridges every legitimate juxtaposition, so any
            // residual NUMBER->NUMBER adjacency is a genuine missing operator.)
            // Note: a three-argument function (summation/product) is pre-expanded by
            // the tokenizer to NUMBER NUMBER STRING FUNCTION — that legitimate
            // NUMBER->NUMBER pair is identified by a following STRING and excluded.
            final boolean threeArgExpansion =
                    i + 2 < tokens.size() && tokens.get(i + 2).getType() == Token.Type.STRING;
            final boolean numberNumber = !threeArgExpansion
                    && a.getType() == Token.Type.NUMBER && b.getType() == Token.Type.NUMBER;
            // A postfix factorial is value-producing; there is deliberately no implicit
            // multiplication after '!', so "2!3" / "5!sqrt(4)" are missing an operator.
            final boolean factorialThenOperand =
                    a.getType() == Token.Type.OPERATOR
                            && ExpressionElements.OP_FACTORIAL.equals(a.getValue())
                            && (b.getType() == Token.Type.NUMBER
                            || b.getType() == Token.Type.CONSTANT
                            || b.getType() == Token.Type.VARIABLE
                            || b.getType() == Token.Type.FUNCTION
                            || b.getType() == Token.Type.LEFT_PAREN);
            if (numberNumber || factorialThenOperand) {
                throw new SyntaxErrorException(
                        CalculatorErrorCode.SYNTAX_MISSING_OPERATOR,
                        Map.of(),
                        "Two operands are not connected by an operator",
                        null);
            }
        }

        final Token first = tokens.getFirst();
        if (first.getType() == Token.Type.OPERATOR
                && isBinaryOperatorSymbol(first.getValue())
                && !isUnarySignSymbol(first.getValue())) {
            throw new SyntaxErrorException(
                    CalculatorErrorCode.SYNTAX_LEADING_OPERATOR,
                    Map.of("operator", first.getValue()),
                    "Expression starts with operator '" + first.getValue()
                            + "' which is missing its left operand",
                    null);
        }

        final Token last = tokens.getLast();
        if (last.getType() == Token.Type.OPERATOR && isBinaryOperatorSymbol(last.getValue())) {
            throw new SyntaxErrorException(
                    CalculatorErrorCode.SYNTAX_TRAILING_OPERATOR,
                    Map.of("operator", last.getValue()),
                    "Incomplete expression: trailing operator '" + last.getValue()
                            + "' is missing its right operand",
                    null);
        }
    }

    /**
     * Validates the number of {@code ;}-separated arguments of every parenthesised
     * function call against the function's declared arity (1, 2 or 3; variadic
     * functions accept any positive count). This yields a precise
     * {@link CalculatorErrorCode#SYNTAX_WRONG_ARGUMENT_COUNT} for cases such as
     * {@code sqrt(1;2)} (too many) or {@code atan2(1)} / {@code logbase(8)} (too few)
     * instead of a generic "incomplete expression".
     *
     * <p>
     * Three-argument functions written as {@code summation(a;b;c)} are pre-expanded by
     * the tokenizer (and already validated there), so only the normal
     * {@code FUNCTION '(' … ')'} form is inspected here. Empty calls are reported
     * earlier by the empty-argument check.
     * </p>
     *
     * @param tokens the tokenized (infix) expression; must not be {@code null}
     * @throws SyntaxErrorException if a fixed-arity function receives the wrong count
     */
    private static void validateFunctionArgumentCounts(@NonNull final List<Token> tokens) {
        for (int i = 0; i + 1 < tokens.size(); i++) {
            if (tokens.get(i).getType() != Token.Type.FUNCTION
                    || tokens.get(i + 1).getType() != Token.Type.LEFT_PAREN) {
                continue;
            }
            final String function = tokens.get(i).getValue();
            final int expected = ExpressionElements.findBySymbol(function)
                    .map(CalculatorEngineUtils::expectedFunctionArity)
                    .orElse(0);
            if (expected <= 0) {
                continue; // unknown or variadic -> nothing to check here
            }

            int depth = 0;
            int arguments = 1;
            boolean sawContent = false;
            int j = i + 1;
            for (; j < tokens.size(); j++) {
                final Token.Type t = tokens.get(j).getType();
                if (t == Token.Type.LEFT_PAREN) {
                    depth++;
                } else if (t == Token.Type.RIGHT_PAREN) {
                    depth--;
                    if (depth == 0) {
                        break;
                    }
                } else if (t == Token.Type.SEMICOLON && depth == 1) {
                    arguments++;
                } else {
                    sawContent = true;
                }
            }
            if (!sawContent) {
                continue; // empty call -> handled by the empty-argument check
            }
            if (arguments != expected) {
                throw new SyntaxErrorException(
                        CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT,
                        Map.of("function", function,
                                "expected", String.valueOf(expected),
                                "actual", String.valueOf(arguments)),
                        "Function '" + function + "' expects " + expected
                                + " argument(s) but received " + arguments,
                        null);
            }
        }
    }

    /**
     * Declared argument count of a callable element: {@code 1} for ordinary one-arg
     * functions, {@code 2} for two-argument and coordinate functions, {@code 3} for
     * three-argument functions, and {@code 0} for variadic or non-checkable elements
     * (the caller then skips the count check).
     *
     * @param element the resolved expression element; must not be {@code null}
     * @return the declared arity, or {@code 0} to skip
     */
    private static int expectedFunctionArity(@NonNull final ExpressionElement element) {
        if (element instanceof UnlimitedArgumentFunction || element instanceof Constant) {
            return 0;
        }
        if (element instanceof ThreeArgumentFunction) {
            return 3;
        }
        if (element instanceof TwoArgumentFunction || element instanceof CoordinateFunction) {
            return 2;
        }
        return element.isFunction() ? 1 : 0;
    }

    /**
     * Arity-aware dry run over the postfix (RPN) token list. It mirrors the operand
     * stack of {@link Evaluator} using <em>counts only</em> — no arithmetic is
     * performed — so it detects an under-supplied operator/function or leftover
     * operands <em>before</em> the evaluator computes anything (no wasted
     * {@code 50000!}).
     *
     * <ul>
     *   <li>operator/function with too few operands →
     *       {@link CalculatorErrorCode#SYNTAX_MISSING_OPERAND} (parameter
     *       {@code operator});</li>
     *   <li>a variadic function whose declared argument count exceeds the available
     *       operands → {@link CalculatorErrorCode#SYNTAX_WRONG_ARGUMENT_COUNT};</li>
     *   <li>more than one value left at the end (missing operator between
     *       sub-expressions, too many function arguments) →
     *       {@link CalculatorErrorCode#SYNTAX_UNEXPECTED_END}.</li>
     * </ul>
     *
     * <p>
     * Unknown symbols are skipped so that {@link Evaluator} can raise the precise
     * {@code SYNTAX_UNKNOWN_FUNCTION}. The check is deliberately conservative: when an
     * injected variadic argument count cannot be read it consumes a single operand
     * instead of guessing, so a valid expression is never rejected.
     * </p>
     *
     * @param postfix the postfix token list produced by the shunting-yard parser;
     *                must not be {@code null}
     * @throws SyntaxErrorException if the token stream cannot reduce to a single value
     */
    static void validatePostfixArity(@NonNull final List<Token> postfix) {
        // Stack of operand "value hints": the literal int for NUMBER tokens (used to
        // read the variadic argument-count token the parser injects), {@code null}
        // otherwise. An ArrayList is used because it tolerates null entries
        // (ArrayDeque does not).
        final List<Integer> stack = new java.util.ArrayList<>();

        for (final Token token : postfix) {
            switch (token.getType()) {
                case NUMBER -> stack.add(parseIntOrNull(token.getValue()));
                case STRING, VARIABLE, CONSTANT -> stack.add(null);
                case OPERATOR, FUNCTION -> {
                    final ExpressionElement element =
                            ExpressionElements.findBySymbol(token.getValue()).orElse(null);
                    if (element == null) {
                        // Unknown symbol: let the evaluator raise SYNTAX_UNKNOWN_FUNCTION.
                        return;
                    }
                    if (element instanceof Constant) {
                        stack.add(null);
                        continue;
                    }
                    if (element instanceof UnlimitedArgumentFunction) {
                        if (stack.isEmpty()) {
                            throw missingOperand(token.getValue());
                        }
                        final Integer count = stack.remove(stack.size() - 1);
                        final int consume = (count == null) ? 1 : Math.max(count, 0);
                        if (count != null && stack.size() < consume) {
                            throw new SyntaxErrorException(
                                    CalculatorErrorCode.SYNTAX_WRONG_ARGUMENT_COUNT,
                                    Map.of("function", token.getValue(),
                                            "expected", String.valueOf(consume),
                                            "actual", String.valueOf(stack.size())),
                                    "Function '" + token.getValue() + "' expected " + consume
                                            + " arguments but only " + stack.size() + " are available",
                                    null);
                        }
                        final int toRemove = Math.min(consume, stack.size());
                        for (int k = 0; k < toRemove; k++) {
                            stack.remove(stack.size() - 1);
                        }
                        stack.add(null);
                        continue;
                    }
                    final int need = operandCountOf(element);
                    if (stack.size() < need) {
                        throw missingOperand(token.getValue());
                    }
                    for (int k = 0; k < need; k++) {
                        stack.remove(stack.size() - 1);
                    }
                    stack.add(null);
                }
                default -> {
                    // SEMICOLON / parentheses never appear in postfix output.
                }
            }
        }

        if (stack.size() != 1) {
            throw new SyntaxErrorException(
                    CalculatorErrorCode.SYNTAX_UNEXPECTED_END,
                    Map.of(),
                    "Incomplete expression: expected a single result but found " + stack.size(),
                    null);
        }
    }

    /**
     * Builds a {@link SyntaxErrorException} for an operator or function that is missing
     * an operand during the arity dry run.
     *
     * @param symbol the operator/function symbol; must not be {@code null}
     * @return the prepared exception
     */
    private static SyntaxErrorException missingOperand(final String symbol) {
        return new SyntaxErrorException(
                CalculatorErrorCode.SYNTAX_MISSING_OPERAND,
                Map.of("operator", symbol),
                "Incomplete expression: operator or function '" + symbol + "' is missing an operand",
                null);
    }

    /**
     * Returns the number of operands the given non-variadic, non-constant element
     * consumes from the stack.
     *
     * @param element the resolved expression element; must not be {@code null}
     * @return {@code 1}, {@code 2} or {@code 3}
     */
    private static int operandCountOf(@NonNull final ExpressionElement element) {
        if (element instanceof UnaryOperator) {
            return 1;
        }
        if (element instanceof BinaryOperator
                || element instanceof SimpleBinaryOperator
                || element instanceof TwoArgumentFunction
                || element instanceof CoordinateFunction) {
            return 2;
        }
        if (element instanceof ThreeArgumentFunction) {
            return 3;
        }
        // Remaining functions (sqrt, sin, ln, abs, gamma, …) take exactly one argument.
        return 1;
    }

    /**
     * Parses a token value as a non-negative {@code int}, returning {@code null} when
     * it is not a plain integer literal (used to read the variadic argument-count token
     * the shunting-yard parser injects before unlimited functions).
     *
     * @param value the token value; must not be {@code null}
     * @return the parsed integer, or {@code null} if not a plain integer
     */
    private static Integer parseIntOrNull(@NonNull final String value) {
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException notAnInt) {
            return null;
        }
    }

    /**
     * Determines whether the given operator symbol denotes a binary operator (two
     * operands), as opposed to the postfix unary {@code !} factorial operator.
     *
     * @param symbol the operator symbol to classify
     * @return {@code true} if the symbol resolves to a binary operator
     */
    private static boolean isBinaryOperatorSymbol(final String symbol) {
        return ExpressionElements.findBySymbol(symbol)
                .map(element -> element instanceof BinaryOperator || element instanceof SimpleBinaryOperator)
                .orElse(false);
    }

    /**
     * Whether the symbol is {@code +} or {@code -}, which are valid as a unary sign at
     * the start of an expression (e.g. {@code -5}).
     *
     * @param symbol the operator symbol
     * @return {@code true} for {@code "+"} or {@code "-"}
     */
    private static boolean isUnarySignSymbol(final String symbol) {
        return ExpressionElements.OP_PLUS.equals(symbol) || ExpressionElements.OP_MINUS.equals(symbol);
    }

    /**
     * Returns a default MathContext with the specified division precision and RoundingMode.HALF_UP.
     *
     * @param divisionPrecision the precision for division operations
     * @return a MathContext instance with the given precision and HALF_UP rounding mode
     */
    public static MathContext getDefaultMathContext(int divisionPrecision) {
        return new MathContext(divisionPrecision, RoundingMode.HALF_UP);
    }

}
