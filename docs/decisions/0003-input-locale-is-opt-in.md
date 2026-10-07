# 0003: The input locale is opt-in

Status: accepted, since 1.6.0

## Situation

The engine evaluates strings such as `1,5+2,5`. In a comma-decimal locale `,` is the decimal separator, but the engine also uses `;` as the argument separator of functions, and `,` has no other job. Letting the output locale decide how input is read would make the same expression mean different things on different machines.

## Decision

- Input is read with `Locale.US` by default: `.` is the decimal separator and `;` the argument separator. `1,5+1,5` is a syntax error.
- `CalculatorEngine.setInputLocale(Locale)` is the opt-in. It is strict: only the decimal separator of that locale is valid, and the tokenizer normalizes it to `.`. `;` stays the argument separator.
- `setLocale(Locale)` only controls the output format and the language of the errors. It does not change how input is read.
- Changing the input locale discards the expression cache, because cached tokens were produced for the old separator.

## Consequences

An expression has one meaning on every machine unless the caller asks for another one. Arithmetic, comparison and hashing never depend on a locale, because the value is normalized before it reaches `BigDecimal`. A number that is formatted for a locale can be parsed again with that locale (`BigNumberValuePropertyTest` checks six locales).
