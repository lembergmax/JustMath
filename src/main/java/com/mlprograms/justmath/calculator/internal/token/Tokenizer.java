package com.mlprograms.justmath.calculator.internal.token;

import com.mlprograms.justmath.bignumber.internal.ArithmeticOperator;
import com.mlprograms.justmath.bignumber.math.utils.MathUtils;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import lombok.AllArgsConstructor;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@code Tokenizer} class provides functionality to parse a mathematical expression string
 * into a sequence of {@link Token} objects. Tokens represent numbers, operators, parentheses,
 * functions, constants, and other lexical units that the expression contains.
 * <p>
 * The tokenizer handles signed numbers, nested parentheses, constants like π and e,
 * functions, and operators according to the definitions in {@link ArithmeticOperator}.
 * <p>
 * Typical usage:
 * <pre>{@code
 *   Tokenizer tokenizer = new Tokenizer();
 *   List<Token> tokens = tokenizer.tokenize("3 + √(4)");
 * }</pre>
 * This results in tokens for NUMBER(3), OPERATOR(+), FUNCTION(√), LEFT_PAREN, NUMBER(4), RIGHT_PAREN.
 */
@AllArgsConstructor
public class Tokenizer {

	/**
	 * A set of all valid operator and function strings defined in {@link ArithmeticOperator}.
	 * This set is used to match substrings in the input expression.
	 */
	private final Set<String> validOperatorsAndFunctions = Arrays.stream(ArithmeticOperator.values())
		                                                       .map(ArithmeticOperator::getOperator)
		                                                       .collect(Collectors.toSet());

	/**
	 * Math context specifying the precision and rounding mode for calculations.
	 */
	private MathContext mathContext;

	/**
	 * Scans the given token list for occurrences where a signed number directly follows
	 * a closing parenthesis token (e.g. ") -5"). In such cases, the signed number token
	 * is split into an operator token ('+' or '-') and a separate unsigned number token.
	 * This is necessary because expressions like "(-3) + 5" or "(2) -4" are tokenized
	 * initially with signed number tokens which must be separated for correct parsing.
	 * <p>
	 * This method modifies the list in place.
	 *
	 * @param tokens
	 * 	the list of tokens to scan and fix
	 *
	 * @throws NullPointerException
	 * 	if {@code tokens} is null
	 */
	private void splitSignedNumbersAfterParentheses(List<Token> tokens) {
		for (int i = 0; i < tokens.size() - 1; i++) {
			Token current = tokens.get(i);
			Token next = tokens.get(i + 1);
			if (current.type() == Token.Type.RIGHT_PAREN && next.type() == Token.Type.NUMBER) {
				String value = next.value();
				if ((value.startsWith("+") || value.startsWith("-")) && value.length() > 1) {
					tokens.set(i + 1, new Token(Token.Type.OPERATOR, value.substring(0, 1)));
					tokens.add(i + 2, new Token(Token.Type.NUMBER, value.substring(1)));
					i++; // skip the inserted number token to avoid infinite loop
				}
			}
		}
	}

	/**
	 * Tokenizes the given mathematical expression string into a list of {@link Token} objects.
	 * <p>
	 * The tokenizer processes the input string in several stages:
	 * <ol>
	 *   <li>Removes all whitespace characters.</li>
	 *   <li>Scans the cleaned expression character by character to identify tokens:
	 *       <ul>
	 *         <li>Signed numbers (e.g. "+3.5", "-2")</li>
	 *         <li>Parentheses '(' and ')' </li>
	 *         <li>Semicolons ';'</li>
	 *         <li>Operators and functions defined in {@link ArithmeticOperator} (e.g. "+", "-", "√")</li>
	 *         <li>Constants such as "pi" and "e"</li>
	 *       </ul>
	 *   </li>
	 *   <li>Adjusts tokens where a signed number immediately follows a closing parenthesis, splitting
	 *       the signed number into an operator token and an unsigned number token. For example, "(2)-5"
	 *       becomes tokens: LEFT_PAREN, NUMBER("2"), RIGHT_PAREN, OPERATOR("-"), NUMBER("5").</li>
	 *   <li>Collapses consecutive '+' and '-' operator tokens into a single operator token based on parity.
	 *       For example, "--" becomes "+", "---" becomes "-".</li>
	 * </ol>
	 * <p>
	 * If the input contains invalid characters or sequences that cannot be tokenized, an
	 * {@link IllegalArgumentException} is thrown.
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 *   Tokenizer tokenizer = new Tokenizer();
	 *   List<Token> tokens = tokenizer.tokenize("3 + (-4) * pi");
	 * }</pre>
	 * Produces tokens for NUMBER(3), OPERATOR(+), LEFT_PAREN, OPERATOR(-), NUMBER(4), RIGHT_PAREN,
	 * OPERATOR(*), NUMBER(3.141592653589793...).
	 *
	 * @param input
	 * 	the mathematical expression string to tokenize, may contain whitespace
	 *
	 * @return a list of {@link Token} objects representing the tokenized expression, never null
	 *
	 * @throws IllegalArgumentException
	 * 	if the input contains invalid or unrecognized characters
	 * @throws NullPointerException
	 * 	if {@code input} is null
	 */
	public List<Token> tokenize(String input) {
		List<Token> tokens = new ArrayList<>();
		String expr = removeWhitespace(input);
		int index = 0;

		// Tokenize expression character by character
		while (index < expr.length()) {
			char c = expr.charAt(index);

			if (isSignedNumberStart(expr, index)) {
				index = tokenizeNumber(expr, index, tokens);
			} else if (isLeftParenthesis(c)) {
				tokens.add(new Token(Token.Type.LEFT_PAREN, String.valueOf(c)));
				index++;
			} else if (isRightParenthesis(c)) {
				tokens.add(new Token(Token.Type.RIGHT_PAREN, String.valueOf(c)));
				index++;
			} else if (isSemicolon(c)) {
				tokens.add(new Token(Token.Type.SEMICOLON, String.valueOf(c)));
				index++;
			} else {
				int lengthOfMatch = matchLongestOperatorOrFunction(expr, index, tokens);
				if (lengthOfMatch > 0) {
					index += lengthOfMatch;
				} else {
					throw new IllegalArgumentException("Invalid character at position " + index + ": " + c);
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
	 * by adjacent tokens like "(2)3", "2(3)", or "π(4)". Does not insert '*' if an explicit operator exists.
	 *
	 * @param tokens
	 * 	the list of tokens to scan and modify
	 */
	private void insertImplicitMultiplicationTokens(List<Token> tokens) {
		for (int i = 0; i < tokens.size() - 1; i++) {
			Token current = tokens.get(i);
			Token next = tokens.get(i + 1);

			// Skip if next token is an operator or semicolon – no implicit * needed
			if (next.type() == Token.Type.OPERATOR || next.type() == Token.Type.SEMICOLON) {
				continue;
			}

			// Insert * where implicit multiplication is likely
			boolean needsMultiplication =
				current.type() == Token.Type.NUMBER
					&& (next.type() == Token.Type.LEFT_PAREN || next.type() == Token.Type.FUNCTION)
					|| current.type() == Token.Type.RIGHT_PAREN
						   && (next.type() == Token.Type.NUMBER || next.type() == Token.Type.FUNCTION || next.type() == Token.Type.LEFT_PAREN);

			if (needsMultiplication) {
				tokens.add(i + 1, new Token(Token.Type.OPERATOR, "*"));
				i++; // Skip the inserted token
			}
		}
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
	private boolean isSignedNumberStart(String expression, int index) {
		char c = expression.charAt(index);

		if ((c == '-' || c == '+') && index + 1 < expression.length() && isDigitOrDecimal(expression.charAt(index + 1))) {
			if (index == 0) {
				return true; // Start of expression
			}

			char prev = expression.charAt(index - 1);

			// If prev is digit or right paren, then + or - is operator, not sign of number
			if (Character.isDigit(prev) || isRightParenthesis(prev)) {
				return false;
			}

			// Only if prev is '(' or an operator, then + or - is part of number sign
			return isLeftParenthesis(prev) || validOperatorsAndFunctions.contains(String.valueOf(prev));
		}

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

	private boolean isOperator(char c) {
		ArithmeticOperator operator = ArithmeticOperator.findByOperator(String.valueOf(c)).orElse(null);
		return operator != null && !operator.isFunction();
	}

	/**
	 * Parses a number token from the expression starting at {@code startIndex}. This number
	 * may include an optional leading '+' or '-' sign, followed by digits and at most one decimal point.
	 * <p>
	 * The parsed number token is added to the {@code tokens} list. Leading '+' signs are removed
	 * to normalize the number representation.
	 * <p>
	 * Example: for input "-3.14+2" starting at index 0, this method will extract "-3.14"
	 * as one number token and return the index of the character '+' after it.
	 *
	 * @param expression
	 * 	the full expression string to tokenize
	 * @param startIndex
	 * 	the position in {@code expression} to start parsing the number
	 * @param tokens
	 * 	the list of tokens to append the parsed number token to
	 *
	 * @return the index in {@code expression} immediately after the parsed number
	 *
	 * @throws IndexOutOfBoundsException
	 * 	if {@code startIndex} is outside the bounds of {@code expression}
	 * @throws NullPointerException
	 * 	if {@code expression} or {@code tokens} is null
	 */
	private int tokenizeNumber(String expression, int startIndex, List<Token> tokens) {
		int currentIndex = startIndex;

		char firstChar = expression.charAt(currentIndex);
		if (firstChar == '+' || firstChar == '-') {
			currentIndex++;
		}

		while (currentIndex < expression.length() && isDigitOrDecimal(expression.charAt(currentIndex))) {
			currentIndex++;
		}

		String rawNumber = expression.substring(startIndex, currentIndex);
		String normalizedNumber = rawNumber.startsWith("+") ? rawNumber.substring(1) : rawNumber;

		tokens.add(new Token(Token.Type.NUMBER, normalizedNumber));
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
	 * @param tokens
	 * 	the list of tokens to be processed and mutated
	 *
	 * @throws NullPointerException
	 * 	if {@code tokens} is null
	 */
	private void mergeConsecutiveSignOperators(List<Token> tokens) {
		List<Token> mergedTokens = new ArrayList<>();
		int i = 0;

		while (i < tokens.size()) {
			Token token = tokens.get(i);

			if (token.type() == Token.Type.OPERATOR && (token.value().equals("+") || token.value().equals("-"))) {
				int minusCount = 0;

				while (i < tokens.size()
					       && tokens.get(i).type() == Token.Type.OPERATOR
					       && (tokens.get(i).value().equals("+") || tokens.get(i).value().equals("-"))) {
					if (tokens.get(i).value().equals("-")) {
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
	 * Attempts to match the longest valid operator, function, or constant token starting at
	 * {@code startIndex} in the input expression. Matching strings include symbols defined
	 * in {@link ArithmeticOperator} and constants like "pi" and "e".
	 * <p>
	 * If a match is found, the corresponding token is added to {@code tokens}:
	 * <ul>
	 *   <li>Functions get {@link Token.Type#FUNCTION}</li>
	 *   <li>Operators get {@link Token.Type#OPERATOR}</li>
	 *   <li>Constants "pi" and "e" get {@link Token.Type#NUMBER} tokens with their numeric values</li>
	 * </ul>
	 * <p>
	 * Matching is case-insensitive for constants. The method always tries to match
	 * the longest possible valid token first.
	 *
	 * @param expression
	 * 	the full expression string to match against
	 * @param startIndex
	 * 	the index in the expression where matching should begin
	 * @param tokens
	 * 	the list to which the matched token should be added
	 *
	 * @return the length of the matched substring if a token is matched, otherwise 0
	 *
	 * @throws NullPointerException
	 * 	if {@code expression} or {@code tokens} is null
	 */
	private int matchLongestOperatorOrFunction(String expression, int startIndex, List<Token> tokens) {
		int maxTokenLength = validOperatorsAndFunctions.stream()
			                     .mapToInt(String::length)
			                     .max()
			                     .orElse(0);

		for (int length = maxTokenLength; length > 0; length--) {
			int endIndex = startIndex + length;
			if (endIndex > expression.length()) continue;

			String candidate = expression.substring(startIndex, endIndex);

			if (candidate.equalsIgnoreCase("pi")) {
				tokens.add(new Token(Token.Type.NUMBER, MathUtils.pi(mathContext).toString()));
				return length;
			}
			if (candidate.equalsIgnoreCase("e")) {
				tokens.add(new Token(Token.Type.NUMBER, MathUtils.e(mathContext).toString()));
				return length;
			}
			if (validOperatorsAndFunctions.contains(candidate)) {
				ArithmeticOperator operator = ArithmeticOperator.findByOperator(candidate).orElseThrow();
				Token.Type type = operator.isFunction() ? Token.Type.FUNCTION : Token.Type.OPERATOR;
				tokens.add(new Token(type, candidate));
				return length;
			}
		}
		return 0;
	}

}
