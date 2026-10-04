# SBOM Review

Artifact: `.portfolio-control/evidence/sbom.spdx.json`
Image: `sha256:913190bf4f387cca93a09d66a3c7ca8969f9636223aad993462e39f21a6af61c`
Artifact SHA-256: `5653a530a575798d907aa42d9bdc2fc889d9b8f00167014271aa89f68665496f`
Format: SPDX 2.3
Packages: 158

## Security result

The final digest-pinned Trivy 0.69.3 filesystem and application-image scans exited zero for HIGH/CRITICAL findings. Scans included vulnerability, secret, and misconfiguration scanners and used no `ignore-unfixed`, ignore file, or finding suppression.

## License review

- Project-owned source is MIT under `LICENSE`.
- The project license does not replace dependency, base-image, or operating-system package licenses.
- Twenty-five packages have `licenseDeclared` and `licenseConcluded` equal to `NOASSERTION` in Trivy output.
- The `NOASSERTION` set contains 16 JVM/Maven packages and 9 operating-system packages.
- `NOASSERTION` means the generated SBOM did not establish a license; it is not an approval or a vulnerability finding.
- Upstream license texts and distribution obligations must be checked before using this image in a distribution policy that requires complete automated attribution.

Representative JVM entries include Kafka Streams, Kotlin, Jackson, RocksDB JNI, compression libraries, and SLF4J. Representative OS entries include OpenSSL, fontconfig, compiler runtime libraries, ncurses, and p11-kit.

## Decision

Retain the complete machine-readable SPDX artifact and this human review. No package was removed or waived solely because of an unresolved automated license field. A future CI policy can fail on `NOASSERTION` only after the reuse kit supplies an audited license-override registry with provenance and expiry.
