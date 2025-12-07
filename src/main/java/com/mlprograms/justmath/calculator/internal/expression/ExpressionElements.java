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

package com.mlprograms.justmath.calculator.internal.expression;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.internal.expression.elements.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for managing all supported mathematical expression elements.
 * <p>
 * This class defines constants for operator, function, parenthesis, and separator symbols,
 * and provides a registry for mapping symbols to their corresponding {@link ExpressionElement} implementations.
 * <p>
 * The static initializer block registers all supported elements, establishing precedence and availability.
 */
public class ExpressionElements {

    public static final Map<String, ExpressionElement> registry = new HashMap<>();

    public static final String PI = "pi";
    public static final String PI_S = "π";
    public static final String EULERS_NUMBER = "e";
    //
    public static final String SEP_SEMICOLON = ";";
    public static final String PAR_LEFT = "(";
    public static final String PAR_RIGHT = ")";
    //
    public static final String OP_PLUS = "+";
    public static final String OP_MINUS = "-";
    public static final String OP_MULTIPLY = "*";
    public static final String OP_MULTIPLY_S = "×";
    public static final String OP_DIVIDE = "/";
    public static final String OP_DIVIDE_S = "÷";
    public static final String OP_MODULO = "%";
    public static final String OP_POWER = "^";
    public static final String OP_PERMUTATION = "nPr";
    public static final String OP_COMBINATION = "nCr";
    //
    public static final String OP_FACTORIAL = "!";
    public static final String FUNC_SQRT = "sqrt";
    public static final String FUNC_SQRT_S = "√";
    public static final String FUNC_CBRT = "cbrt";
    public static final String FUNC_CBRT_S = "³√";
    public static final String FUNC_ROOTN = "rootn";
    //
    public static final String FUNC_COT = "cot";
    public static final String FUNC_TAN = "tan";
    public static final String FUNC_COS = "cos";
    public static final String FUNC_SIN = "sin";
    //
    public static final String FUNC_SINH = "sinh";
    public static final String FUNC_COSH = "cosh";
    public static final String FUNC_TANH = "tanh";
    public static final String FUNC_COTH = "coth";
    //
    public static final String FUNC_ASIN = "asin";
    public static final String FUNC_ACOS = "acos";
    public static final String FUNC_ATAN = "atan";
    public static final String FUNC_ACOT = "acot";
    public static final String FUNC_SIN_S = "sin⁻¹";
    public static final String FUNC_COS_S = "cos⁻¹";
    public static final String FUNC_TAN_S = "tan⁻¹";
    public static final String FUNC_COT_S = "cot⁻¹";
    //
    public static final String FUNC_ASINH = "asinh";
    public static final String FUNC_ACOSH = "acosh";
    public static final String FUNC_ATANH = "atanh";
    public static final String FUNC_ACOTH = "acoth";
    public static final String FUNC_ASINH_S = "sinh⁻¹";
    public static final String FUNC_ACOS_S = "cosh⁻¹";
    public static final String FUNC_ATANH_S = "tanh⁻¹";
    public static final String FUNC_ACOTH_S = "coth⁻¹";
    //
    public static final String FUNC_LOG_2 = "log2";
    public static final String FUNC_LOG_10 = "log10";
    public static final String FUNC_LN = "ln";
    public static final String FUNC_LOGBASE = "logbase";
    //
    public static final String FUNC_ATAN_2 = "atan2";
    public static final String FUNC_ATAN_2_S = "tan2⁻¹";
    public static final String FUNC_PERM = "perm";
    public static final String FUNC_COMB = "comb";
    //
    public static final String FUNC_LCM = "LCM";
    public static final String FUNC_GCD = "GCD";
    //
    public static final String FUNC_REC = "Rec";
    public static final String FUNC_POL = "Pol";
    //
    public static final String FUNC_RANDINT = "RandInt";
    //
    public static final String FUNC_SUMM = "sum";
    public static final String FUNC_SUMM_S = "∑";
    public static final String FUNC_PROD = "prod";
    public static final String FUNC_PROD_S = "∏";
    //
    public static final String FUNC_GAMMA = "Γ";
    public static final String FUNC_GAMMA_S = "gamma";
    public static final String FUNC_BETA = "B";
    public static final String FUNC_BETA_S = "beta";
    //
    public static final String FUNC_ABS = "abs";
    public static final String SURRFUNC_ABS_S = "|"; // that is special :0
    //
    public static final String K_SERIES_MATH_VARIABLE = "k";

    static {
        List<ExpressionElement> expressionElementList = List.of(
                new ZeroArgumentConstant(PI, BigNumbers::pi),
                new ZeroArgumentConstant(PI_S, BigNumbers::pi),
                new ZeroArgumentConstant(EULERS_NUMBER, BigNumbers::e),
                //
                new Parenthesis(Parenthesis.Type.LEFT),
                new Parenthesis(Parenthesis.Type.RIGHT),
                new Separator(SEP_SEMICOLON),
                //
                new SimpleBinaryOperator(OP_PLUS, 2, BigNumber::add),
                new SimpleBinaryOperator(OP_MINUS, 2, BigNumber::subtract),
                new SimpleBinaryOperator(OP_MULTIPLY, 3, BigNumber::multiply),
                new SimpleBinaryOperator(OP_MULTIPLY_S, 3, BigNumber::multiply),
                new BinaryOperator(OP_DIVIDE, 3, BigNumber::divide),
                new BinaryOperator(OP_DIVIDE_S, 3, BigNumber::divide),
                new SimpleBinaryOperator(OP_MODULO, 3, BigNumber::modulo),
                new BinaryOperator(OP_POWER, 4, BigNumber::power),
                new BinaryOperator(OP_PERMUTATION, 6, BigNumber::permutation),
                new BinaryOperator(OP_COMBINATION, 6, BigNumber::combination),
                //
                new PostfixUnaryOperator(OP_FACTORIAL, 5, BigNumber::factorial),
                new OneArgumentFunction(FUNC_SQRT, 4, BigNumber::squareRoot),
                new OneArgumentFunction(FUNC_SQRT_S, 4, BigNumber::squareRoot),
                new OneArgumentFunction(FUNC_CBRT, 4, BigNumber::cubicRoot),
                new OneArgumentFunction(FUNC_CBRT_S, 4, BigNumber::cubicRoot),
                new TwoArgumentFunction(FUNC_ROOTN, 4, BigNumber::nthRoot),
                //
                new OneArgumentTrigonometricFunction(FUNC_SIN, 6, BigNumber::sin),
                new OneArgumentTrigonometricFunction(FUNC_COS, 6, BigNumber::cos),
                new OneArgumentTrigonometricFunction(FUNC_TAN, 6, BigNumber::tan),
                new OneArgumentTrigonometricFunction(FUNC_COT, 6, BigNumber::cot),
                //
                new OneArgumentFunction(FUNC_SINH, 6, BigNumber::sinh),
                new OneArgumentFunction(FUNC_COSH, 6, BigNumber::cosh),
                new OneArgumentFunction(FUNC_TANH, 6, BigNumber::tanh),
                new OneArgumentFunction(FUNC_COTH, 6, BigNumber::coth),
                //
                new OneArgumentTrigonometricFunction(FUNC_ASIN, 6, BigNumber::asin),
                new OneArgumentTrigonometricFunction(FUNC_ACOS, 6, BigNumber::acos),
                new OneArgumentTrigonometricFunction(FUNC_ATAN, 6, BigNumber::atan),
                new OneArgumentTrigonometricFunction(FUNC_ACOT, 6, BigNumber::acot),
                new OneArgumentTrigonometricFunction(FUNC_SIN_S, 6, BigNumber::asin),
                new OneArgumentTrigonometricFunction(FUNC_COS_S, 6, BigNumber::acos),
                new OneArgumentTrigonometricFunction(FUNC_TAN_S, 6, BigNumber::atan),
                new OneArgumentTrigonometricFunction(FUNC_COT_S, 6, BigNumber::acot),
                //
                new OneArgumentFunction(FUNC_ASINH, 6, BigNumber::asinh),
                new OneArgumentFunction(FUNC_ACOSH, 6, BigNumber::acosh),
                new OneArgumentFunction(FUNC_ATANH, 6, BigNumber::atanh),
                new OneArgumentFunction(FUNC_ACOTH, 6, BigNumber::acoth),
                new OneArgumentFunction(FUNC_ASINH_S, 6, BigNumber::asinh),
                new OneArgumentFunction(FUNC_ACOS_S, 6, BigNumber::acosh),
                new OneArgumentFunction(FUNC_ATANH_S, 6, BigNumber::atanh),
                new OneArgumentFunction(FUNC_ACOTH_S, 6, BigNumber::acoth),
                //
                new OneArgumentFunction(FUNC_LOG_2, 6, BigNumber::log2),
                new OneArgumentFunction(FUNC_LOG_10, 6, BigNumber::log10),
                new OneArgumentFunction(FUNC_LN, 6, BigNumber::ln),
                new TwoArgumentFunction(FUNC_LOGBASE, 6, BigNumber::logBase),
                //
                new TwoArgumentFunction(FUNC_ATAN_2, 6, BigNumber::atan2),
                new TwoArgumentFunction(FUNC_ATAN_2_S, 6, BigNumber::atan2),
                new TwoArgumentFunction(FUNC_PERM, 6, BigNumber::permutation),
                new TwoArgumentFunction(FUNC_COMB, 6, BigNumber::combination),
                //
                new TwoArgumentFunction(FUNC_LCM, 6, BigNumber::lcm),
                new TwoArgumentFunction(FUNC_LCM.toLowerCase(), 6, BigNumber::lcm),
                new SimpleTwoArgumentFunction(FUNC_GCD, 6, BigNumber::gcd),
                new SimpleTwoArgumentFunction(FUNC_GCD.toLowerCase(), 6, BigNumber::gcd),
                //
                new CoordinateFunction(FUNC_REC, 6, BigNumber::polarToCartesianCoordinates),
                new SimpleCoordinateFunction(FUNC_POL, 6, BigNumber::cartesianToPolarCoordinates),
                //
                new SimpleTwoArgumentFunction(FUNC_RANDINT, 6, BigNumber::randomIntegerForRange),
                //
                new ThreeArgumentFunction(FUNC_SUMM, 6, BigNumber::summation),
                new ThreeArgumentFunction(FUNC_SUMM_S, 6, BigNumber::summation),
                new ThreeArgumentFunction(FUNC_PROD, 6, BigNumber::product),
                new ThreeArgumentFunction(FUNC_PROD_S, 6, BigNumber::product),
                //
                new OneArgumentFunction(FUNC_GAMMA, 6, BigNumber::gamma),
                new OneArgumentFunction(FUNC_GAMMA_S, 6, BigNumber::gamma),
                new TwoArgumentFunction(FUNC_BETA, 6, BigNumber::beta),
                new TwoArgumentFunction(FUNC_BETA_S, 6, BigNumber::beta),
                //
                new OneArgumentZeroParamFunction(FUNC_ABS, 6, BigNumber::abs),
                new OneArgumentZeroParamFunction(SURRFUNC_ABS_S, 6, BigNumber::abs)
        );

        for (ExpressionElement expressionElement : expressionElementList) {
            register(expressionElement);
        }
    }

    /**
     * Finds an {@link ExpressionElement} by its symbol.
     *
     * @param symbol the symbol to look up
     * @return an {@link Optional} containing the found {@code ExpressionElement}, or empty if not found
     */
    public static Optional<ExpressionElement> findBySymbol(String symbol) {
        return Optional.ofNullable(registry.get(symbol));
    }

    /**
     * Registers an {@link ExpressionElement} in the registry.
     * The element is mapped by its symbol for a later lookup.
     *
     * @param element the {@code ExpressionElement} to register
     */
    public static void register(ExpressionElement element) {
        registry.put(element.getSymbol(), element);
    }

}
