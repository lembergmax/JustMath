package com.mlprograms.justmath.calculator.internal.token;

import com.mlprograms.justmath.bignumber.internal.ArithmeticOperator;
import com.mlprograms.justmath.util.Values;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@code Tokenizer} is responsible for parsing mathematical expressions
 * into a list of {@link Token} objects. It supports numbers, operators,
 * parentheses, functions, and constants such as π and e.
 * <p>
 * Example: "3 + √(4)" will be tokenized into
 * [NUMBER(3), OPERATOR(+), FUNCTION(√), LEFT_PAREN, NUMBER(4), RIGHT_PAREN].
 */
@NoArgsConstructor
public class Tokenizer {

	/**
	 * A set of all valid operator and function strings defined in {@link ArithmeticOperator}.
	 * This set is used to match substrings in the input expression.
	 */
	private final Set<String> validOperatorsAndFunctions = Arrays.stream(ArithmeticOperator.values())
		                                                       .map(ArithmeticOperator::getOperator)
		                                                       .collect(Collectors.toSet());

	/**
	 * Tokenizes the given mathematical input string.
	 *
	 * @param input
	 * 	the raw mathematical expression (e.g. "3 + sqrt(9)")
	 *
	 * @return a list of tokens representing the expression
	 *
	 * @throws IllegalArgumentException
	 * 	if the input contains unknown symbols or invalid characters
	 */
	public List<Token> tokenize(String input) {
		List<Token> tokens = new ArrayList<>();
		String expression = removeWhitespace(input);
		int index = 0;

		while (index < expression.length()) {
			char currentChar = expression.charAt(index);

			if (isNegativeNumberStart(expression, index)) {
				index = tokenizeNumber(expression, index, tokens);
				continue;
			}

			if (isSemicolon(currentChar)) {
				tokens.add(new Token(Token.Type.SEMICOLON, String.valueOf(currentChar)));
				index++;
				continue;
			}

			if (isLeftParenthesis(currentChar)) {
				tokens.add(new Token(Token.Type.LEFT_PAREN, String.valueOf(currentChar)));
				index++;
				continue;
			}

			if (isRightParenthesis(currentChar)) {
				tokens.add(new Token(Token.Type.RIGHT_PAREN, String.valueOf(currentChar)));
				index++;
				continue;
			}

			int matchedLength = matchLongestOperatorOrFunction(expression, index, tokens);
			if (matchedLength > 0) {
				index += matchedLength;
				continue;
			}

			if (Character.isLetter(currentChar)) {
				throw new IllegalArgumentException("Unknown identifier at position " + index + ": " + currentChar);
			}

			throw new IllegalArgumentException("Invalid character at position " + index + ": " + currentChar);
		}

		return tokens;
	}

	/**
	 * Determines whether the character at the specified index in the given expression
	 * marks the beginning of a number, including the case of a negative number.
	 * <p>
	 * This method handles both explicitly negative numbers (e.g., {@code -5}, {@code -3.2})
	 * and regular numeric values (e.g., {@code 5}, {@code 3.2}). A minus sign is only
	 * interpreted as indicating a negative number if it is followed by a digit or a
	 * decimal separator. It is either at the very start of the expression or
	 * preceded by a valid context (such as a left parenthesis or an operator).
	 * <p>
	 * Examples of valid negative number starts:
	 * <ul>
	 *   <li>{@code "-5"} (at the beginning)</li>
	 *   <li>{@code "(-3"} (after an opening parenthesis)</li>
	 *   <li>{@code "*-2"} (after an operator)</li>
	 * </ul>
	 * Examples that are not valid negative number starts:
	 * <ul>
	 *   <li>{@code "5-3"} (the minus is treated as a subtraction operator)</li>
	 *   <li>{@code "x-4"} (minus after a variable, not a number start)</li>
	 * </ul>
	 *
	 * @param expression
	 * 	the full mathematical expression as a string
	 * @param index
	 * 	the position of the character to check within the expression
	 *
	 * @return {@code true} if the character at the specified index starts a (possibly negative) number;
	 *   {@code false} otherwise
	 *
	 * @throws IndexOutOfBoundsException
	 * 	if the {@code index} is not a valid position in the expression
	 */
	private boolean isNegativeNumberStart(String expression, int index) {
		char c = expression.charAt(index);

		// directly a negative number: -5 or -3.2
		if (c == '-' && index + 1 < expression.length() && isDigitOrDecimal(expression.charAt(index + 1))) {
			// check whether the minus sign is at a valid position
			if (index == 0) {
				return true; // at the very beginning
			}

			char prev = expression.charAt(index - 1);
			// valid cases: minus is preceded by '(' or an operator
			return isLeftParenthesis(prev) || validOperatorsAndFunctions.contains(String.valueOf(prev));
		}

		// regular number without minus
		return isDigitOrDecimal(c);
	}

	/**
	 * Removes all whitespace characters from the input string.
	 */
	private String removeWhitespace(String input) {
		return input.replaceAll("\\s+", "");
	}

	/**
	 * Returns true if the character is a digit or a decimal separator.
	 */
	private boolean isDigitOrDecimal(char c) {
		return Character.isDigit(c) || c == '.';
	}

	/**
	 * Returns true if the character is an opening parenthesis.
	 */
	private boolean isLeftParenthesis(char c) {
		return String.valueOf(c).equals(ArithmeticOperator.LEFT_PARENTHESIS.getOperator());
	}

	/**
	 * Returns true if the character is a closing parenthesis.
	 */
	private boolean isRightParenthesis(char c) {
		return String.valueOf(c).equals(ArithmeticOperator.RIGHT_PARENTHESIS.getOperator());
	}

	/**
	 * Returns true if the character is a semicolon.
	 */
	private boolean isSemicolon(char c) {
		return String.valueOf(c).equals(ArithmeticOperator.SEMICOLON.getOperator());
	}

	/**
	 * Parses a number token starting from the given index and adds it to the token list.
	 *
	 * @return the index after the parsed number
	 */
	private int tokenizeNumber(String expression, int startIndex, List<Token> tokens) {
		int index = startIndex;

		// optional negative sign
		if (expression.charAt(index) == '-') {
			index++;
		}

		while (index < expression.length() && isDigitOrDecimal(expression.charAt(index))) {
			index++;
		}

		String number = expression.substring(startIndex, index);
		tokens.add(new Token(Token.Type.NUMBER, number));
		return index;
	}

	/**
	 * Tries to match the longest valid operator or function (including constants) starting
	 * from the given index. If a match is found, the corresponding token is added.
	 *
	 * @return the length of the matched substring, or 0 if no match was found
	 */
	private int matchLongestOperatorOrFunction(String expression, int startIndex, List<Token> tokens) {
		final int maxTokenLength = validOperatorsAndFunctions.stream()
			                           .mapToInt(String::length)
			                           .max()
			                           .orElse(0);

		for (int length = maxTokenLength; length > 0; length--) {
			int endIndex = startIndex + length;
			if (endIndex > expression.length()) {
				continue;
			}

			String substring = expression.substring(startIndex, endIndex);

			// Special handling for constants
			if (substring.equalsIgnoreCase("pi")) {
				tokens.add(new Token(Token.Type.NUMBER, Values.PI.toString()));
				return length;
			}

			if (substring.equalsIgnoreCase("e")) {
				tokens.add(new Token(Token.Type.NUMBER, Values.E.toString()));
				return length;
			}

			if (validOperatorsAndFunctions.contains(substring)) {
				ArithmeticOperator operator = ArithmeticOperator.findByOperator(substring).orElseThrow();
				Token.Type tokenType = operator.isFunction() ? Token.Type.FUNCTION : Token.Type.OPERATOR;
				tokens.add(new Token(tokenType, substring));
				return length;
			}
		}

		return 0;
	}

}
