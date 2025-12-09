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

import com.mlprograms.justmath.calculator.expression.ExpressionElement;
import com.mlprograms.justmath.calculator.expression.ExpressionElements;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

/**
 * Represents a lexical token extracted from a mathematical expression.
 * Tokens can be numbers, operators, functions, parentheses, or special symbols.
 */
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Token {

	private Type type;
	private String value;

	/**
	 * Returns the matching ArithmeticOperator if available.
	 *
	 * @return Optional of ArithmeticOperator
	 */
	public Optional<ExpressionElement> asArithmeticOperator() {
		return ExpressionElements.findBySymbol(value);
	}

	/**
	 * Enumeration of token types.
	 */
	public enum Type {
		NUMBER,
		OPERATOR,
		FUNCTION,
		LEFT_PAREN,
		RIGHT_PAREN,
		SEMICOLON,
		STRING,
		CONSTANT,
		VARIABLE
	}
}
