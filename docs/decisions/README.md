# Decision records

These pages record the decisions that surprise a reader of the code. Each one says what the situation was, what was decided and what follows from it. A decision stays valid until a later record replaces it.

| Record | Decision |
| --- | --- |
| [0001](0001-mixed-mutability-of-bignumber.md) | `BigNumber` is not immutable: operations return new instances, a few named methods mutate. |
| [0002](0002-typed-math-exceptions.md) | The math layer throws typed exceptions that extend the JDK types and carry an error code. |
| [0003](0003-input-locale-is-opt-in.md) | The expression engine reads input with `Locale.US`; another input locale is opt-in. |
| [0004](0004-big-math-for-transcendental-functions.md) | Transcendental functions use big-math with adaptive guard digits, they are not reimplemented. |
| [0005](0005-limits-on-expensive-inputs.md) | Inputs that ask for an enormous result are rejected with a typed error. |
| [0006](0006-closed-registries.md) | The function registry and the unit registry are closed after static initialization. |
| [0007](0007-string-based-arithmetic.md) | The arithmetic ran on digit strings and was slower than `BigDecimal`. Superseded by 0008. |
| [0008](0008-bigdecimal-arithmetic-and-lazy-digits.md) | The arithmetic runs on `BigDecimal` and `BigInteger`, and a `BigNumber` writes its digits down only when they are asked for. |

To add a record, copy the layout of an existing one, number it and add it to this table.
