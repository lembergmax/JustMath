# 0006: Closed registries

Status: accepted

## Situation

Two registries define what the library knows: `ExpressionElements` (functions, operators and constants of the engine) and `UnitRegistry` (the units). An application could be given a way to add its own entries at runtime. An earlier version of the registry accessor returned the live map, so any caller could poison the lookup of every engine in the JVM.

## Decision

- Both registries are filled in a static initializer and are read-only afterwards. `ExpressionElements.getRegistry()` returns an unmodifiable view. `UnitRegistry` validates at start that symbols are unique and that every unit has a definition and a base.
- There is no runtime extension point. An application that needs another function or unit adds it to the library with a pull request: [extending.md](../extending.md) is the guide.
- Binary and unary `+` and `-` use two registries on purpose, because the same symbol has different precedence in each role.

## Consequences

A registry cannot change while engines are running, so no lock is needed to read it, and two engines in one JVM always agree on the grammar. The price is that an application cannot add a domain-specific function without a release of the library.
