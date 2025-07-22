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

package com.mlprograms.justmath.calculator.internal.token;

import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.internal.expression.ExpressionElement;
import com.mlprograms.justmath.calculator.internal.expression.ExpressionElements;
import com.mlprograms.justmath.calculator.internal.expression.elements.Parenthesis;
import com.mlprograms.justmath.calculator.internal.expression.elements.Separator;
import com.mlprograms.justmath.calculator.internal.expression.elements.ThreeArgumentFunction;
import com.mlprograms.justmath.calculator.internal.expression.elements.ZeroArgumentConstant;

import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

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
	 * Set of all valid operator and function symbols recognized by the tokenizer.
	 * Populated from the registry of {@link ExpressionElement} instances.
	 * Used to identify operators and functions during tokenization.
	 */
	private final Set<String> validOperatorsAndFunctions = ExpressionElements.registry.values().stream().map(ExpressionElement::getSymbol).collect(Collectors.toSet());

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
	 * @param input
	 * 	the mathematical expression to tokenize, as a string
	 *
	 * @return a list of tokens representing the lexemes of the expression
	 *
	 * @throws IllegalArgumentException
	 * 	if the input contains invalid characters or malformed expressions
	 * @throws NullPointerException
	 * 	if the input string is null
	 */
	public List<Token> tokenize(String input) {
		List<Token> tokens = new ArrayList<>();
		String expression = removeWhitespace(input);
		int index = 0;

		while (index < expression.length()) {
			char c = expression.charAt(index);

			if (isSignedNumberStart(expression, index)) {
				index = tokenizeNumber(expression, index, tokens);
			} else if (isLeftParenthesis(c)) {
				tokens.add(new Token(Token.Type.LEFT_PAREN, String.valueOf(c)));
				index++;
			} else if (isRightParenthesis(c)) {
				tokens.add(new Token(Token.Type.RIGHT_PAREN, String.valueOf(c)));
				index++;
			} else if (isSeparator(c)) {
				tokens.add(new Token(Token.Type.SEMICOLON, String.valueOf(c)));
				index++;
			} else if (isAbsoluteValueSign(c)) {
				if (nextAbsoluteIsOpen) {
					tokens.add(new Token(Token.Type.FUNCTION, ExpressionElements.FUNC_ABS));
					tokens.add(new Token(Token.Type.LEFT_PAREN, ExpressionElements.PAR_LEFT));
				} else {
					tokens.add(new Token(Token.Type.RIGHT_PAREN, ExpressionElements.PAR_RIGHT));
				}
				nextAbsoluteIsOpen = !nextAbsoluteIsOpen;
				index++;
			} else if (matchThreeArgumentFunction(expression, index).isPresent()) {
				Optional<ExpressionElement> matchedFunction = matchThreeArgumentFunction(expression, index);

				if (matchedFunction.isEmpty()) {
					throw new IllegalArgumentException("Invalid function at position " + index);
				}

				ExpressionElement expressionElement = matchedFunction.get();
				String symbol = expressionElement.getSymbol();

				int functionStart = index + symbol.length();
				int closingParenthesis = findClosingParenthesis(expression, functionStart);
				if (closingParenthesis < 0) {
					throw new IllegalArgumentException("Unmatched '(' in function: " + symbol);
				}

				String inside = expression.substring(functionStart + 1, closingParenthesis);

				String[] parts = inside.split(ExpressionElements.SEP_SEMICOLON, 3);
				if (parts.length != 3) {
					throw new IllegalArgumentException("Three-argument function '" + symbol + "' must have three arguments");
				}

				tokens.add(new Token(Token.Type.NUMBER, parts[ 0 ]));
				tokens.add(new Token(Token.Type.NUMBER, parts[ 1 ]));
				tokens.add(new Token(Token.Type.STRING, parts[ 2 ]));
				tokens.add(new Token(Token.Type.FUNCTION, symbol));

				index = closingParenthesis + 1;
			} else {
				int lengthOfMatch = matchOtherOperatorOrFunction(expression, index, tokens);
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
	 * by adjacent tokens like "(2)3", "2(3)", "π(4)", or "ka".
	 * <p>
	 * <strong>Note:</strong> Implicit multiplication for constants only works for symbols
	 * already defined as {@link ExpressionElements} in the registry. New or unregistered symbols
	 * will not be recognized and may lead to an {@link IllegalArgumentException}.
	 *
	 * @param tokens
	 * 	the list of tokens to scan and modify
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
			if (needsMultiplication(current, next)) {
				tokens.add(i + 1, new Token(Token.Type.OPERATOR, "*"));
				i++; // Skip the inserted token
			}
		}
	}

	/**
	 * Determines whether implicit multiplication should be inserted between two tokens.
	 * <p>
	 * Returns true if the combination of `current` and `next` tokens represents a context
	 * where multiplication is implied, such as:
	 * <ul>
	 *   <li>Number followed by left parenthesis or function</li>
	 *   <li>Right parenthesis followed by number, function, or left parenthesis</li>
	 *   <li>Number, variable, or zero-argument constant followed by a zero-argument function</li>
	 *   <li>Zero-argument function followed by number or variable</li>
	 * </ul>
	 *
	 * @param current
	 * 	the current token in the sequence
	 * @param next
	 * 	the next token in the sequence
	 *
	 * @return true if implicit multiplication is needed, false otherwise
	 */
	private boolean needsMultiplication(Token current, Token next) {
		return
			(current.getType() == Token.Type.NUMBER
				 && (next.getType() == Token.Type.LEFT_PAREN || next.getType() == Token.Type.FUNCTION))
				|| (current.getType() == Token.Type.RIGHT_PAREN
					    && (next.getType() == Token.Type.NUMBER || next.getType() == Token.Type.FUNCTION || next.getType() == Token.Type.LEFT_PAREN))
				|| ((current.getType() == Token.Type.NUMBER || isZeroArgConstant(current)) && isZeroArgConstant(next))
				|| (isZeroArgConstant(current) && next.getType() == Token.Type.NUMBER)
				|| (current.getType() == Token.Type.CONSTANT && next.getType() == Token.Type.FUNCTION)
				|| (current.getType() == Token.Type.VARIABLE && next.getType() == Token.Type.VARIABLE)
				|| (current.getType() == Token.Type.VARIABLE && next.getType() == Token.Type.CONSTANT)
				|| (current.getType() == Token.Type.CONSTANT && next.getType() == Token.Type.VARIABLE)
			;
	}

	/**
	 * Checks if the given token represents a zero-argument constant.
	 * <p>
	 * This method looks up the token's symbol in the {@link ExpressionElements} registry
	 * and verifies if the associated {@link ExpressionElement} is an instance of {@link ZeroArgumentConstant}.
	 *
	 * @param token
	 * 	the token to check
	 *
	 * @return true if the token is a zero-argument constant, false otherwise
	 */
	private boolean isZeroArgConstant(Token token) {
		Optional<ExpressionElement> expressionElementOptional = ExpressionElements.findBySymbol(token.getValue());
		return expressionElementOptional.isPresent() && expressionElementOptional.get().getClass() == ZeroArgumentConstant.class;
	}

	/**
	 * Checks if the given character represents the absolute value sign.
	 *
	 * @param c
	 * 	the character to check
	 *
	 * @return true if the character is the absolute value sign, false otherwise
	 */
	private boolean isAbsoluteValueSign(char c) {
		return String.valueOf(c).equals(ExpressionElements.SURRFUNC_ABS_S);
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
		String cString = String.valueOf(c);

		if ((cString.equals(ExpressionElements.OP_MINUS) || cString.equals(ExpressionElements.OP_PLUS))
			    && index + 1 < expression.length() && isDigitOrDecimal(expression.charAt(index + 1))) {
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
	 * Tries to match a three-argument function starting at the given index.
	 *
	 * @param expression
	 * 	the full input expression
	 * @param index
	 * 	the index to start checking from
	 *
	 * @return an Optional containing the matched function symbol, or empty if not found
	 */
	private Optional<ExpressionElement> matchThreeArgumentFunction(String expression, int index) {
		for (Map.Entry<String, ExpressionElement> entry : ExpressionElements.registry.entrySet()) {
			String symbol = entry.getKey();
			ExpressionElement expressionElement = entry.getValue();

			if (expressionElement instanceof ThreeArgumentFunction &&
				    expression.startsWith(symbol + ExpressionElements.PAR_LEFT, index)) {
				return Optional.of(expressionElement);
			}
		}
		return Optional.empty();
	}

	/**
	 * Removes all whitespace characters from the input string.
	 *
	 * @param input
	 * 	the string to process
	 *
	 * @return the input string with all whitespace removed
	 */
	private String removeWhitespace(String input) {
		return input.replaceAll("\\s+", "");
	}

	/**
	 * Checks if the given character is a digit or a decimal point.
	 *
	 * @param c
	 * 	the character to check
	 *
	 * @return true if the character is a digit or '.', false otherwise
	 */
	private boolean isDigitOrDecimal(char c) {
		return Character.isDigit(c) || c == '.';
	}

	/**
	 * Determines if the given character is a left parenthesis, according to the expression elements registry.
	 *
	 * @param character
	 * 	the character to check
	 *
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
	 * @param character
	 * 	the character to check
	 *
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
	 * @param character
	 * 	the character to check
	 *
	 * @return true if the character is a separator, false otherwise
	 */
	private boolean isSeparator(char character) {
		return ExpressionElements.findBySymbol(String.valueOf(character))
			       .filter(element -> element instanceof Separator)
			       .isPresent();
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
	 * @param expression
	 * 	the input mathematical expression string to parse
	 * @param startIndex
	 * 	the position in the expression to start matching from
	 * @param tokens
	 * 	the list of tokens to append new tokens to if a match is found
	 *
	 * @return the length of the matched symbol (number of characters consumed),
	 * 	or 0 if no operator or function matched at the current position
	 *
	 * @throws IllegalArgumentException
	 * 	if the factorial operator '!' is found in an invalid position
	 * @throws NullPointerException
	 * 	if expression or tokens is null
	 */
	private int matchOtherOperatorOrFunction(String expression, int startIndex, List<Token> tokens) {
		int maxTokenLength = validOperatorsAndFunctions.stream()
			                     .mapToInt(String::length)
			                     .max()
			                     .orElse(0);

		for (int length = maxTokenLength; length > 0; length--) {
			int endIndex = startIndex + length;
			if (endIndex > expression.length()) {
				continue;
			}

			String candidate = expression.substring(startIndex, endIndex);

			Optional<ExpressionElement> zeroArg = ExpressionElements.findBySymbol(candidate).filter(element -> element instanceof ZeroArgumentConstant);
			if (zeroArg.isPresent()) {
				tokens.add(new Token(Token.Type.CONSTANT, zeroArg.get().getSymbol()));
				return length;
			}

			if (validOperatorsAndFunctions.contains(candidate)) {
				if (candidate.equalsIgnoreCase(ExpressionElements.OP_FACTORIAL)) {
					// must not be in the beginning or after another expressionElement
					Token previous = tokens.getLast();
					if (tokens.isEmpty() ||
						    !(previous.getType() == Token.Type.NUMBER
							      || previous.getType() == Token.Type.RIGHT_PAREN
							      || previous.getType() == Token.Type.VARIABLE
							      || previous.getType() == Token.Type.CONSTANT)) {
						throw new IllegalArgumentException("Factorial '!' must follow a number, constant, variable, or closing parenthesis");
					}

					// always tokenize as an expressionElement
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
	 * @param expression
	 * 	the expression string to search
	 * @param openIndex
	 * 	the index of the opening parenthesis to match
	 *
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
