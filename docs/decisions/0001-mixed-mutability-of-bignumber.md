# 0001: `BigNumber` is not immutable

Status: accepted

## Situation

`BigNumber` has public setters for its locale, its `MathContext` and its `TrigonometricMode` (Lombok `@Setter`), and a few methods change the value in place: `negateThis()`, `trim()`, the `trimLeadingZeros...` family. These are part of the published API. A value type that offers them cannot be treated as immutable.

Earlier releases were not clear about it. Operations such as `abs()` or `floor()` sometimes returned the receiver or one of the shared constants in `BigNumbers`, so one caller that mutated its result changed the constant for everybody (audit findings K1, K2 and H10).

## Decision

- An operation never changes its receiver and never returns the receiver or a shared constant. It returns a new instance. This covers `add`, `multiply`, `abs`, `floor`, `round` and every math function.
- A method that changes the receiver says so in its name (`xxxThis`, `negateThis`), or is one of the setters, or is `trim`. The Javadoc names the mutation in its first sentence.
- `valueOf(long)` returns a copy of a cached template, never the cached instance.
- `clone()` returns a new instance and drops the lazily created calculator engine, so that two clones never share its mutable state.
- `equals`, `hashCode` and `compareTo` use the numeric value. `1.0` and `1.00` are equal, `+0` and `-0` have the same hash code.

## Consequences

Callers can treat a `BigNumber` as immutable as long as they do not call a mutator. The cost is an allocation per operation. Making the class immutable means removing public setters, which is a breaking change for a major release.

`BigNumberMutationAndRoundingTest`, `BigNumberConstantIntegrityTest` and `BigNumberEngineIsolationTest` guard the rules.
