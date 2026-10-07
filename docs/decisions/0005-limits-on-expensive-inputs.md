# 0005: Limits on expensive inputs

Status: accepted, since 1.7.0

## Situation

The engine evaluates strings from users. A short string can ask for a result that takes minutes or gigabytes: `1000000!`, `9^9999999999`, `nCr(1000000;500000)`, `summation(1;1000000000;k)`, `exp(1000000000)`, a multi-megabyte expression, a `MathContext` with a billion digits. One such input blocks a thread or exhausts the heap of the host application.

## Decision

An operation checks the size of its result before it computes, and rejects an input beyond a fixed limit with a typed error (`MATH_OVERFLOW` or a domain error). The limits are far above any realistic use, and a legitimate large result such as `2^2000000` or `1^n` is not affected.

| Limit | Value |
| --- | --- |
| Expression length | 100000 characters |
| Factorial argument | 100000 |
| Digits of an integer power | 1000000 |
| Size of `exp(x)` | `|x| * log10(e)` at most 1000000 |
| Combinatorial factors, result digits | 100000, 1000000 |
| Iterations of `summation` and `product` | 1000000 |
| Precision of a `MathContext` | 1000000 digits |

The structure of an expression is validated before anything is evaluated, so `50000!/` fails with `SYNTAX_TRAILING_OPERATOR` without computing `50000!`.

## Consequences

A result beyond a limit cannot be computed with the library. Raising a limit is a change that needs a reason and a test with the new size. The limits belong to the security policy: bypassing one is a vulnerability (see [SECURITY.md](../../SECURITY.md)).
