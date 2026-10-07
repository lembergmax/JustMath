# Releasing JustMath

A version on Maven Central cannot be changed or removed. The release therefore has two manual steps: the maintainer merges the release pull request into `main`, and the maintainer approves the deployment in GitHub. Everything else runs in `.github/workflows/publish.yml`.

## Before the release

1. Every issue of the milestone is closed or moved, and `CI result` is green on `developer`.
2. `pom.xml` has the release version (no `-SNAPSHOT`, the workflow rejects it).
3. `project.build.outputTimestamp` in `pom.xml` is the commit date of the release, so that two builds of the commit give identical jars.
4. `CHANGELOG.md` has a section `## [x.y.z]` for the version. The workflow copies that section into the GitHub release and fails if it is missing.
5. `./mvnw verify` passes on a clean checkout. This runs the tests, the coverage gate, SpotBugs, Javadoc, the formatting check and japicmp against the previous release.

## Rehearsal

The workflow can run without publishing. Start *Publish to Maven Central* by hand from the branch that you want to release and tick `dry_run`. The job `rehearsal` builds and tests the project, collects the jars and the SBOM, writes `SHA256SUMS` and prints the release notes from `CHANGELOG.md`. It does not sign, deploy or create a release, and it needs no approval. Do this once before the pull request to `main`, and again after you change the workflow.

```bash
gh workflow run publish.yml --ref developer -f dry_run=true
```

## Release

1. Open a pull request from `developer` into `main`. Branch protection requires `CI result`.
2. Merge it with a merge commit.
3. The push to `main` starts the workflow *Publish to Maven Central*. The job `plan` reads the version and stops if Central already has it. The job `publish` then waits for approval.
4. Open the run in the Actions tab, choose *Review deployments*, tick `maven-central` and approve. The environment lists the maintainer as the only reviewer and accepts only the branch `main`.
5. The job `publish` runs `mvn -P release clean deploy`: it runs the tests again, signs the jars with the release key, uploads the bundle to the Central Portal and returns when Central has validated it. Central releases the validated bundle on its own (`autoPublish`).
6. The same job writes `SHA256SUMS` for the jars, the signatures and the SBOM, and attests the build provenance of the jars.
7. The job `release` waits until `repo1.maven.org` serves the jar (usually 15 to 30 minutes), then creates the tag `x.y.z` on the merge commit and the GitHub release with the notes from `CHANGELOG.md` and these files:
   - `justmath-x.y.z.jar`, `-sources.jar`, `-javadoc.jar` and their `.asc` signatures,
   - `justmath-x.y.z-cyclonedx.json`, the CycloneDX software bill of materials,
   - `SHA256SUMS`.

Tags have no `v` prefix (`1.6.0`, `1.7.0`).

## After the release

1. Check `https://repo1.maven.org/maven2/io/github/lembergmax/justmath/` for the new version and the GitHub release for its files.
2. Set `japicmp.baseline.version` in `pom.xml` to the released version and remove the exclusions that were allowed for it.
3. Move `developer` to the next version, for example `x.y.(z+1)-SNAPSHOT`.
4. Close the milestone and the tracking issue.

## When something fails

| Failure | What to do |
| --- | --- |
| `plan` reports "already on Maven Central" | The version exists. Bump the version in `pom.xml`. |
| The tests fail in `publish` | Nothing was uploaded. Fix on `developer`, merge again. |
| Central rejects the bundle | Nothing was published. Read the message in the log, fix the cause (usually a missing signature, a missing pom field or a wrong credential), merge again or use *Re-run jobs*. |
| `release` times out | Central was slow. Use *Re-run failed jobs*. The job creates the release or adds the files to an existing one. |
| The wrong version was released | It stays on Central. Release a fixed version and say in the changelog that the old one should not be used. |

## Secrets

The workflow reads `CENTRAL_USERNAME`, `CENTRAL_PASSWORD`, `GPG_PRIVATE_KEY` and `GPG_PASSPHRASE`. They are repository secrets. A stricter setup stores them as secrets of the `maven-central` environment, so that only the approved job can read them.

The public key is on `keyserver.ubuntu.com`. [SECURITY.md](../SECURITY.md#verifying-a-release) shows how users verify a release.
