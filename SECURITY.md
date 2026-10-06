# Security policy

## Supported versions

Security fixes go into the latest release of the current minor version. Older versions are not patched; upgrade to the latest release.

| Version | Supported |
|---------|-----------|
| 1.7.x   | yes       |
| 1.6.x and older | no |

## Reporting a vulnerability

Please do not open a public issue for a security problem.

Report it privately through GitHub: open the [Security tab](https://github.com/lembergmax/JustMath/security/advisories/new) of the repository and choose "Report a vulnerability". Include:

- the version of JustMath and of Java,
- what you did (the expression, the call or the input),
- what happened and what you expected,
- why you think it is a security problem.

What to expect:

- an acknowledgement within 7 days,
- an assessment and a plan within 14 days,
- a fix in a patch release, announced in a GitHub security advisory. The reporter is credited unless they prefer not to be.

Please give the project time to release a fix before you publish details. Ninety days is a reasonable upper limit; if a fix needs longer, the maintainer will say so.

## What counts as a vulnerability

JustMath is a library that evaluates expressions from strings, so input is the attack surface. These are in scope:

- an input that makes the engine hang, exhaust memory or overflow the stack although it is small (the engine has documented limits, for example on expression length, factorial size, power size and series length),
- a way to bypass those limits,
- an input that makes the engine run code, read files or reach the network,
- state that leaks between engines, threads or callers (for example a shared constant that can be modified),
- a flaw in how releases are built, signed or published.

Wrong results for valid input are bugs, not vulnerabilities. Please report them as normal [issues](https://github.com/lembergmax/JustMath/issues/new/choose).

## Dependencies

The only runtime dependency is [big-math](https://github.com/eobermuhlner/big-math). Dependabot watches it and the build tooling, and CodeQL scans the code.
