package com.mlprograms.justmath.calculator;

import java.util.*;

/**
 * Converts a list of tokens from infix notation to postfix (Reverse Polish Notation).
 * Uses Dijkstra's Shunting-Yard algorithm for handling operator precedence and associativity.
 */
public class Parser {

	private static final Map<String, Integer> PRECEDENCE = Map.of(
		"+", 2,
		"-", 2,
		"*", 3,
		"/", 3,
		"^", 4,
		"!", 5,
		"√", 4
	);

	private static final Set<String> RIGHT_ASSOCIATIVE = Set.of("^");

	/**
	 * Converts a list of infix tokens into a postfix list.
	 *
	 * @param tokens
	 * 	the list of infix tokens
	 *
	 * @return the list of tokens in postfix (RPN) order
	 */
	public static List<Token> toPostfix(List<Token> tokens) {
		List<Token> output = new ArrayList<>();
		Deque<Token> operatorStack = new ArrayDeque<>();

		for (Token token : tokens) {
			switch (token.getType()) {
				case NUMBER -> output.add(token);

				case FUNCTION -> operatorStack.push(token);

				case OPERATOR -> {
					while (!operatorStack.isEmpty()) {
						Token top = operatorStack.peek();
						if ((top.getType() == Token.Type.FUNCTION) ||
							    (top.getType() == Token.Type.OPERATOR &&
								     (hasHigherPrecedence(top, token) ||
									      (hasEqualPrecedence(top, token) && !isRightAssociative(token))))) {
							output.add(operatorStack.pop());
						} else {
							break;
						}
					}
					operatorStack.push(token);
				}

				case LEFT_PAREN -> operatorStack.push(token);

				case RIGHT_PAREN -> {
					while (!operatorStack.isEmpty() && operatorStack.peek().getType() != Token.Type.LEFT_PAREN) {
						output.add(operatorStack.pop());
					}
					if (operatorStack.isEmpty()) {
						throw new IllegalArgumentException("Mismatched parentheses");
					}
					operatorStack.pop(); // Remove '('
					if (!operatorStack.isEmpty() && operatorStack.peek().getType() == Token.Type.FUNCTION) {
						output.add(operatorStack.pop()); // function call after parentheses
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
	 * Checks if the precedence of the first operator is higher than the second.
	 *
	 * @param op1
	 * 	the first operator token
	 * @param op2
	 * 	the second operator token
	 *
	 * @return true if op1 has higher precedence than op2
	 */
	private static boolean hasHigherPrecedence(Token op1, Token op2) {
		return getPrecedence(op1) > getPrecedence(op2);
	}

	/**
	 * Checks if two operators have equal precedence.
	 *
	 * @param op1
	 * 	the first operator token
	 * @param op2
	 * 	the second operator token
	 *
	 * @return true if both operators have equal precedence
	 */
	private static boolean hasEqualPrecedence(Token op1, Token op2) {
		return getPrecedence(op1) == getPrecedence(op2);
	}

	/**
	 * Retrieves the precedence value for a given operator token.
	 *
	 * @param token
	 * 	the operator token
	 *
	 * @return the precedence value, or 0 if not found
	 */
	private static int getPrecedence(Token token) {
		return PRECEDENCE.getOrDefault(token.getValue(), 0);
	}

	/**
	 * Determines if the given operator token is right-associative.
	 *
	 * @param token
	 * 	the operator token
	 *
	 * @return true if the operator is right-associative
	 */
	private static boolean isRightAssociative(Token token) {
		return RIGHT_ASSOCIATIVE.contains(token.getValue());
	}
}

