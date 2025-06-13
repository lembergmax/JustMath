
# JustMath

JustMath is a high-precision Java mathematics library designed for developers and scientists who require extreme numerical accuracy. It includes a robust calculation engine capable of evaluating complex mathematical expressions provided as strings. Built with `BigDecimal` at its core, JustMath aims to eliminate the limitations of floating-point arithmetic, ensuring correctness and precision even in sensitive domains.

## ✨ Features

- 🧮 **Expression Engine**: Evaluate full mathematical expressions like `"3 * sin(45) + ln(2.5)"` with automatic parsing.
- 🔍 **Extreme Precision**: Internally uses `BigDecimal` for all calculations to avoid floating-point inaccuracies.
- 🧠 **Rich Function Set**:
    - Trigonometric (sin, cos, tan, asin, atan, etc.)
    - Logarithmic (log, ln)
    - Exponential (exp, pow)
    - Hyperbolic (sinh, cosh, tanh, etc.)
    - Combinatorics (factorials, combinations, permutations)
    - Coordinate transformations
- 🌐 **Modular Design**: Easily integrate or extend the engine for scientific computing, finance, or education.
- 🧪 **JUnit-Tested**: Thoroughly tested for correctness and edge cases.

## 📦 Installation

Once available on Maven Central, you can include JustMath by adding the following to your `pom.xml`:

```xml
<dependency>
    <groupId>com.yourdomain</groupId>
    <artifactId>justmath</artifactId>
    <version>1.0.0</version>
</dependency>
```

> **Note:** Java 21 or higher is required.

## 🚀 Quick Start

```java
import justmath.engine.CalculatorEngine;

public class Main {
    public static void main(String[] args) {
        CalculatorEngine engine = new CalculatorEngine();
        String expression = "3 * sin(45) + ln(2.5)";
        String result = engine.evaluate(expression);
        System.out.println("Result: " + result);
    }
}
```

## 📚 Core Components

### `BigNumber`

A high-precision wrapper over `BigDecimal` that adds:

- Trigonometric functions (supporting DEG and RAD modes)
- Factorials, combinations, permutations
- Coordinate transformations
- Inverse and hyperbolic math functions
- Internal caching and value simplification

### `CalculatorEngine`

Evaluates string-based mathematical expressions and supports:

- Nested expressions with parentheses
- Operator precedence
- Customizable angle units (degrees/radians)
- Floating point precision control

### `TrigonometricMode`

A simple enum to toggle between **DEG** and **RAD**:

```java
BigNumber.setTrigonometricMode(TrigonometricMode.DEG);
```

## 🔧 Configuration

You can globally change the trigonometric mode for the engine:

```java
BigNumber.setTrigonometricMode(TrigonometricMode.RAD); // or DEG
```

## 📈 Example Use Cases

### High-Precision Calculation

```java
BigNumber pi = BigNumber.pi(50); // π with 50-digit precision
BigNumber sqrt2 = BigNumber.sqrt("2", 100); // √2 with 100-digit precision
```

### Coordinate Conversion

```java
BigNumber[] cartesian = BigNumber.polarToCartesianCoordinates("5", "60");
System.out.println("x = " + cartesian[0] + ", y = " + cartesian[1]);
```

### Combinatorics

```java
BigNumber combinations = BigNumber.combination("10", "3"); // 10 choose 3
```

## 🧪 Testing

JUnit 5 test cases are included to validate core mathematical operations and engine behavior. You can run them with:

```bash
mvn test
```

## 📁 Project Structure

```
justmath/
├── engine/                # Expression parsing and evaluation
├── math/                  # BigNumber and supporting math functions
├── mode/                  # Enum for TrigonometricMode
├── tests/                 # JUnit tests
└── util/                  # Utility classes
```

## 🛠️ Build

JustMath uses **Maven**. To build the project, run:

```bash
mvn clean install
```

## 🧩 Planned Enhancements

- Expression tree visualization
- User-defined functions
- Variable support (e.g., `x = 5`)
- Integration with symbolic algebra systems

## 💬 Feedback and Contributions

Feedback, bug reports, and contributions are welcome! Feel free to open an [issue](https://github.com/yourname/justmath/issues) or submit a [pull request](https://github.com/yourname/justmath/pulls).

## 📄 License

This project is licensed under the MIT License. See the `LICENSE` file for details.

---

## ❤️ Acknowledgments

JustMath is inspired by the limitations of floating-point arithmetic in Java and the need for a more robust, developer-friendly alternative.

---

## 🔍 See Also

- [BigDecimal (JavaDoc)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html)
- [JUnit 5](https://junit.org/junit5/)
