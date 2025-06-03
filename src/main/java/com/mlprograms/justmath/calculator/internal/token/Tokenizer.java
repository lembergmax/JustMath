package com.mlprograms.justmath.calculator.internal.token;

import com.mlprograms.justmath.util.Values;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Tokenizer class for breaking down a mathematical expression string into tokens.
 * Handles numbers, operators, parentheses, functions, and constants.
 */
@NoArgsConstructor
public class Tokenizer {

	// Supported operators and functions
	private final Set<String> FUNCTIONS = Set.of(
		"sin", "cos", "tan", "asin", "acos", "atan",
		"sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
		"log", "ln", "sqrt", "cbrt", "!", "^"
	);

	private final Set<Character> OPERATORS = Set.of('+', '-', '*', '/', '^', '!', '√');

	/**
	 * Tokenizes the input expression into a list of tokens.
	 *
	 * @param input
	 * 	the raw mathematical expression string
	 *
	 * @return a list of tokens
	 *
	 * @throws IllegalArgumentException
	 * 	if the expression contains invalid characters
	 */
	public List<Token> tokenize(String input) {
		List<Token> tokens = new ArrayList<>();
		String normalized = input.replaceAll("\\s+", ""); // remove whitespace
		int i = 0;

		while (i < normalized.length()) {
			char c = normalized.charAt(i);

			if (Character.isDigit(c) || c == '.') {
				// Parse number
				int start = i;
				while (i < normalized.length() &&
					       (Character.isDigit(normalized.charAt(i)) || normalized.charAt(i) == '.')) {
					i++;
				}
				String number = normalized.substring(start, i);
				tokens.add(new Token(Token.Type.NUMBER, number));
			} else if (Character.isLetter(c)) {
				// Parse function or constant
				int start = i;
				while (i < normalized.length() && Character.isLetter(normalized.charAt(i))) {
					i++;
				}
				String name = normalized.substring(start, i);

				if (name.equalsIgnoreCase("pi")) {
					tokens.add(new Token(Token.Type.NUMBER, Values.PI));
				} else if (name.equalsIgnoreCase("e")) {
					tokens.add(new Token(Token.Type.NUMBER, Values.E));
				} else if (FUNCTIONS.contains(name)) {
					tokens.add(new Token(Token.Type.FUNCTION, name));
				} else {
					throw new IllegalArgumentException("Unknown identifier: " + name);
				}
			} else if (OPERATORS.contains(c)) {
				tokens.add(new Token(Token.Type.OPERATOR, String.valueOf(c)));
				i++;
			} else if (c == '(') {
				tokens.add(new Token(Token.Type.LEFT_PAREN, "("));
				i++;
			} else if (c == ')') {
				tokens.add(new Token(Token.Type.RIGHT_PAREN, ")"));
				i++;
			} else {
				throw new IllegalArgumentException("Invalid character: " + c);
			}
		}

		return tokens;
	}
}

