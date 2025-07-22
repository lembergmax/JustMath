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

package com.mlprograms.justmath.calculator.internal;

import com.mlprograms.justmath.calculator.internal.expression.ExpressionElement;
import com.mlprograms.justmath.calculator.internal.expression.ExpressionElements;
import com.mlprograms.justmath.calculator.internal.token.Token;
import lombok.NoArgsConstructor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Converts a list of tokens from infix notation to postfix (Reverse Polish Notation).
 * Uses Dijkstra's Shunting-Yard algorithm for handling operator precedence and associativity.
 */
@NoArgsConstructor
public class Parser {

	/**
	 * Checks if the given expression element is a right-associative operator.
	 * Currently, only the power operator (^) is considered right-associative.
	 *
	 * @param expressionElement
	 * 	the expression element to check
	 *
	 * @return true if the operator is right-associative, false otherwise
	 */
	private static boolean isRightAssociativeOperator(ExpressionElement expressionElement) {
		return expressionElement.getSymbol().equals(ExpressionElements.OP_POWER);
	}

	/**
	 * Retrieves the precedence value of the given expression element.
	 *
	 * @param expressionElement
	 * 	the expression element to check
	 *
	 * @return the precedence value of the operator
	 */
	private static int getOperatorPrecedence(ExpressionElement expressionElement) {
		return expressionElement.getPrecedence();
	}

	/**
	 * Converts a list of infix tokens into a postfix list.
	 *
	 * @param tokens
	 * 	the list of infix tokens
	 *
	 * @return the list of tokens in postfix (RPN) order
	 */
	public List<Token> toPostfix(List<Token> tokens) {
		List<Token> output = new ArrayList<>();
		Deque<Token> operatorStack = new ArrayDeque<>();

		for (Token token : tokens) {
			switch (token.getType()) {
				case NUMBER, STRING, CONSTANT -> output.add(token);
				case FUNCTION, LEFT_PAREN -> operatorStack.push(token);
				case OPERATOR -> {
					// Factorial is a postfix operator → add directly to the output
					if (token.getValue().equals("!")) {
						output.add(token);
						continue;
					}

					while (!operatorStack.isEmpty()) {
						Token top = operatorStack.peek();

						if ((top.getType() == Token.Type.FUNCTION) || (top.getType() == Token.Type.OPERATOR && (hasHigherPrecedence(top, token)
							                                                                                        || (hasEqualPrecedence(top, token) && !isRightAssociative(token))))) {
							output.add(operatorStack.pop());
						} else {
							break;
						}
					}
					operatorStack.push(token);
				}
				case RIGHT_PAREN -> {
					while (!operatorStack.isEmpty() && operatorStack.peek().getType() != Token.Type.LEFT_PAREN) {
						output.add(operatorStack.pop());
					}
					if (operatorStack.isEmpty()) {
						throw new IllegalArgumentException("Mismatched parentheses");
					}
					operatorStack.pop(); // Remove '('

					// If there's a function before the '(', pop it
					if (!operatorStack.isEmpty() && operatorStack.peek().getType() == Token.Type.FUNCTION) {
						output.add(operatorStack.pop());
					}
				}
				case SEMICOLON -> {
					while (!operatorStack.isEmpty() && operatorStack.peek().getType() != Token.Type.LEFT_PAREN) {
						output.add(operatorStack.pop());
					}
					if (operatorStack.isEmpty()) {
						throw new IllegalArgumentException("Misplaced semicolon or mismatched parentheses");
					}
				}
			}
		}

		while (!operatorStack.isEmpty()) {
			Token top = operatorStack.pop();
			if (top.getType() == Token.Type.LEFT_PAREN || top.getType() == Token.Type.RIGHT_PAREN) {
				throw new IllegalArgumentException("Mismatched parentheses");
			}
			output.add(top);
		}

		return output;
	}

	/**
	 * Checks if the precedence of the first operator token is higher than the second.
	 *
	 * @param token1
	 * 	the first operator token
	 * @param token2
	 * 	the second operator token
	 *
	 * @return true if token1 has higher precedence than token2, false otherwise
	 */
	private boolean hasHigherPrecedence(Token token1, Token token2) {
		return getPrecedence(token1) > getPrecedence(token2);
	}

	/**
	 * Checks if two operator tokens have equal precedence.
	 *
	 * @param token1
	 * 	the first operator token1
	 * @param token2
	 * 	the second operator token1
	 *
	 * @return true if both operators have equal precedence, false otherwise
	 */
	private boolean hasEqualPrecedence(Token token1, Token token2) {
		return getPrecedence(token1) == getPrecedence(token2);
	}

	/**
	 * Retrieves the precedence value of the given token if it is an arithmetic operator.
	 *
	 * @param token
	 * 	the token to check
	 *
	 * @return the precedence value, or 0 if not an operator
	 */
	private int getPrecedence(Token token) {
		return token.asArithmeticOperator()
			       .map(Parser::getOperatorPrecedence)
			       .orElse(0);
	}

	/**
	 * Determines if the given token represents a right-associative operator.
	 *
	 * @param token
	 * 	the token to check
	 *
	 * @return true if the operator is right-associative, false otherwise
	 */
	private boolean isRightAssociative(Token token) {
		return token.asArithmeticOperator()
			       .map(Parser::isRightAssociativeOperator)
			       .orElse(false);
	}

}
