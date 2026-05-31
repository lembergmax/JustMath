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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import io.github.lembergmax.justmath.calculator.errors.CalculatorErrorCode;
import io.github.lembergmax.justmath.calculator.exceptions.SyntaxErrorException;
import io.github.lembergmax.justmath.calculator.expression.ExpressionElement;
import io.github.lembergmax.justmath.calculator.expression.ExpressionElements;
import io.github.lembergmax.justmath.calculator.expression.elements.function.UnlimitedArgumentFunction;
import io.github.lembergmax.justmath.calculator.internal.Token;
import lombok.NoArgsConstructor;

/**
 * Converts a list of tokens from infix notation to postfix (Reverse Polish Notation).
 * Uses Dijkstra's Shunting-Yard algorithm for handling operator precedence and associativity.
 */
@NoArgsConstructor
public class PostfixParser {

    /**
     * Checks if the given expression element is a right-associative operator. Only
     * the power operator ({@code ^}) is identified here by symbol; prefix unary
     * operators are tagged by token type ({@link Token.Type#UNARY_OPERATOR}) and
     * handled in {@link #isRightAssociative(Token)}.
     *
     * @param expressionElement the expression element to check
     * @return true if the operator is right-associative, false otherwise
     */
    private static boolean isRightAssociativeOperator(ExpressionElement expressionElement) {
        return expressionElement.getSymbol().equals(ExpressionElements.OP_POWER);
    }

    /**
     * Retrieves the precedence value of the given expression element.
     *
     * @param expressionElement the expression element to check
     * @return the precedence value of the operator
     */
    private static int getOperatorPrecedence(ExpressionElement expressionElement) {
        return expressionElement.getPrecedence();
    }

    /**
     * Converts a list of infix tokens into a postfix list.
     *
     * @param tokens the list of infix tokens
     * @return the list of tokens in postfix (RPN) order
     */
    public List<Token> toPostfix(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Deque<Token> operatorStack = new ArrayDeque<>();
        Deque<Integer> argumentCountStack = new ArrayDeque<>();
        Deque<Boolean> functionCallParenStack = new ArrayDeque<>();
        Token previousToken = null;

        for (Token token : tokens) {
            switch (token.getType()) {
                case NUMBER, STRING, CONSTANT, VARIABLE -> output.add(token);

                case FUNCTION -> operatorStack.push(token);

                case LEFT_PAREN -> {
                    operatorStack.push(token);

                    boolean isFunctionCall = previousToken != null
                            && previousToken.getType() == Token.Type.FUNCTION;
                    boolean isUnlimitedCall = isFunctionCall && isUnlimitedArgumentFunction(previousToken);

                    argumentCountStack.push(isUnlimitedCall ? 1 : 0);
                    functionCallParenStack.push(isFunctionCall);
                }

                case OPERATOR, UNARY_OPERATOR -> {
                    // Factorial is postfix: its operand is already in the output, so emit it directly
                    // instead of running the shunting-yard precedence loop.
                    final boolean isFactorial = token.getType() == Token.Type.OPERATOR
                            && token.getValue().equals(ExpressionElements.OP_FACTORIAL);
                    if (isFactorial) {
                        output.add(token);
                    } else {
                        shuntInfixOperator(token, operatorStack, output);
                    }
                }
                case RIGHT_PAREN -> {
                    while (!operatorStack.isEmpty() && operatorStack.peek().getType() != Token.Type.LEFT_PAREN) {
                        output.add(operatorStack.pop());
                    }
                    if (operatorStack.isEmpty()) {
                        throw new SyntaxErrorException(CalculatorErrorCode.SYNTAX_UNMATCHED_PAREN, "Mismatched parentheses");
                    }

                    operatorStack.pop();

                    int argumentCount = argumentCountStack.isEmpty() ? 0 : argumentCountStack.pop();
                    if (!functionCallParenStack.isEmpty()) {
                        functionCallParenStack.pop();
                    }
                    if (!operatorStack.isEmpty() && operatorStack.peek().getType() == Token.Type.FUNCTION) {
                        Token functionToken = operatorStack.pop();

                        if (argumentCount > 0 && isUnlimitedArgumentFunction(functionToken)) {
                            output.add(new Token(Token.Type.NUMBER, Integer.toString(argumentCount)));
                        }

                        output.add(functionToken);
                    }
                }
                case SEMICOLON -> {
                    while (!operatorStack.isEmpty()
                            && operatorStack.peek().getType() != Token.Type.LEFT_PAREN) {
                        output.add(operatorStack.pop());
                    }
                    if (operatorStack.isEmpty()) {
                        throw new SyntaxErrorException(CalculatorErrorCode.SYNTAX_MISPLACED_SEPARATOR, "Misplaced semicolon or mismatched parentheses");
                    }

                    if (functionCallParenStack.isEmpty() || !functionCallParenStack.peek()) {
                        throw new SyntaxErrorException(CalculatorErrorCode.SYNTAX_MISPLACED_SEPARATOR,
                                "Misplaced semicolon: ';' is only valid between function arguments");
                    }

                    if (!argumentCountStack.isEmpty()) {
                        int current = argumentCountStack.pop();
                        if (current > 0) {
                            argumentCountStack.push(current + 1);
                        } else {
                            argumentCountStack.push(current);
                        }
                    }
                }
            }

            previousToken = token;
        }

        while (!operatorStack.isEmpty()) {
            Token top = operatorStack.pop();
            if (top.getType() == Token.Type.LEFT_PAREN || top.getType() == Token.Type.RIGHT_PAREN) {
                throw new SyntaxErrorException(CalculatorErrorCode.SYNTAX_UNMATCHED_PAREN, "Mismatched parentheses");
            }
            output.add(top);
        }

        return output;
    }

    /**
     * Checks whether the provided function token corresponds to a function that accepts
     * an unlimited number of arguments (variadic function).
     * <p>
     * The method resolves the associated ExpressionElement by its symbol and checks
     * whether it is an instance of UnlimitedArgumentFunction. If the element cannot be
     * found, the method returns false.
     *
     * @param functionToken the token representing the function (expected Token.Type.FUNCTION)
     * @return true if the function allows a variable number of arguments, false otherwise
     */
    private boolean isUnlimitedArgumentFunction(Token functionToken) {
        return ExpressionElements.findBySymbol(functionToken.getValue())
                .map(element -> element instanceof UnlimitedArgumentFunction)
                .orElse(false);
    }

    /**
     * Performs the shunting-yard precedence dance for an infix or prefix operator.
     *
     * <p>While operators of higher precedence — or of equal precedence when the incoming operator
     * is <em>left</em> associative — sit on top of {@code operatorStack}, they are popped onto the
     * {@code output} list. Functions on the operator stack are always popped first (they bind
     * tighter than any infix operator). Finally the incoming {@code operator} is pushed onto the
     * stack.</p>
     *
     * <p>Extracting this loop into a named helper replaces the previous inline {@code break;}
     * inside an arrow-form switch arm. The {@code break;} was syntactically valid (it exited the
     * switch arm) but read as if it were exiting the surrounding {@code for} loop.</p>
     *
     * @param operator      the incoming infix or prefix operator token
     * @param operatorStack the shunting-yard operator stack (mutated in place)
     * @param output        the postfix output list (mutated in place)
     */
    private void shuntInfixOperator(final Token operator, final Deque<Token> operatorStack, final List<Token> output) {
        while (!operatorStack.isEmpty()) {
            final Token top = operatorStack.peek();
            final boolean topIsOperator = top.getType() == Token.Type.OPERATOR
                    || top.getType() == Token.Type.UNARY_OPERATOR;
            final boolean shouldPop = top.getType() == Token.Type.FUNCTION
                    || (topIsOperator
                    && (hasHigherPrecedence(top, operator)
                    || (hasEqualPrecedence(top, operator) && !isRightAssociative(operator))));
            if (!shouldPop) {
                break;
            }
            output.add(operatorStack.pop());
        }
        operatorStack.push(operator);
    }

    /**
     * Checks if the precedence of the first operator token is higher than the second.
     *
     * @param operator1 the first operator token
     * @param operator2 the second operator token
     * @return true if operator1 has higher precedence than operator2, false otherwise
     */
    private boolean hasHigherPrecedence(Token operator1, Token operator2) {
        return getPrecedence(operator1) > getPrecedence(operator2);
    }

    /**
     * Checks if two operator tokens have equal precedence.
     *
     * @param operator1 the first operator token
     * @param operator2 the second operator token
     * @return true if both operators have equal precedence, false otherwise
     */
    private boolean hasEqualPrecedence(Token operator1, Token operator2) {
        return getPrecedence(operator1) == getPrecedence(operator2);
    }

    /**
     * Retrieves the precedence value of the given token if it is an arithmetic operator.
     *
     * @param token the token to check
     * @return the precedence value, or 0 if not an operator
     */
    private int getPrecedence(Token token) {
        return token.asArithmeticOperator()
                .map(PostfixParser::getOperatorPrecedence)
                .orElse(0);
    }

    /**
     * Determines if the given token represents a right-associative operator.
     *
     * @param token the token to check
     * @return true if the operator is right-associative, false otherwise
     */
    private boolean isRightAssociative(Token token) {
        if (token.getType() == Token.Type.UNARY_OPERATOR) {
            return true;
        }
        return token.asArithmeticOperator()
                .map(PostfixParser::isRightAssociativeOperator)
                .orElse(false);
    }

}
