# 0002: Typed math exceptions

Status: accepted, since 1.7.0

## Situation

The math layer threw plain `ArithmeticException` and `IllegalArgumentException` with an English message. The engine classified a failure by looking for words such as "division by zero" or "overflow" in that message. Rewording a message changed the error code that a caller saw, and a foreign exception with a similar text was classified wrongly.

## Decision

- The math layer throws `MathArithmeticException` (extends `ArithmeticException`) and `MathArgumentException` (extends `IllegalArgumentException`). Both implement `ErrorCodeProvider` and carry a `CalculatorErrorCode`.
- `RuntimeExceptionClassifier` reads the code from an `ErrorCodeProvider`. Only an exception from outside the library is classified by its message, as a last resort.
- A new kind of failure gets a new code, a bundle key in every language file and a test that pins the code.

## Consequences

Code that caught `ArithmeticException` or `IllegalArgumentException` keeps working, because the new types extend them. Callers and tests branch on the code and not on the text. Changing a message no longer changes a classification.

Five expressions changed from `PROCESSING_INTERNAL` to `PROCESSING_DOMAIN_ERROR`, because their message matched no keyword and a non-integer argument is bad input and not an internal error: `comb(2.5;1)`, `perm(2.5;1)`, `gcd(1.5;2)`, `lcm(1.5;2)` and `summation(1;3;5)`. `UntypedMathExceptionGuardTest` fails when a new bare `ArithmeticException` or `IllegalArgumentException` appears in the math classes, and `MathExceptionTypingTest` pins the code of each failure.
