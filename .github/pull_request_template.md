## What and why

<!-- What does this change, and why is it needed? Link the issue: "Closes #123". -->

## Checklist

- [ ] One topic only; formatting changes are in their own commit
- [ ] A test covers the change; for a bug fix it fails without the fix
- [ ] `./mvnw verify` passes locally
- [ ] Public methods have Javadoc that states mutation, rounding and exceptions
- [ ] Rounding mode and `MathContext` of the caller are passed on (if math is touched)
- [ ] README and `CHANGELOG.md` are updated if behaviour or the public API changes

## Compatibility

<!-- Does this change what existing callers see (results, exceptions, signatures)? If yes, say what and mark it breaking in the changelog. -->

None / describe here
