# Security Gates: #28 kafka-streams-demo

- [x] No credential is required by default.
- [x] `.env`, keys, credentials, and secret directories are ignored; `.env.example` remains trackable.
- [x] Local fallback secret scan passes.
- [x] Gradle dependencies are locked.
- [x] Gradle wrapper distribution and JAR checksums are audited.
- [x] JDK, JRE, Kafka Compose, and Testcontainers images are digest-pinned.
- [x] Runtime is UID/GID 10001 with read-only root, dropped capabilities, and `no-new-privileges`.
- [x] Broker host port is loopback-only.
- [x] `/tmp` remains `noexec`; executable RocksDB JNI temp storage is narrowly mounted.
- [x] Trivy filesystem dependency/secret/misconfiguration scan reports zero HIGH/CRITICAL findings.
- [x] Trivy application-image scan exits zero for HIGH/CRITICAL.
- [x] Both local Trivy scans run without `ignore-unfixed`, ignore files, or finding suppressions.
- [x] SPDX 2.3 JSON SBOM is retained locally: 158 packages, SHA-256 `5653a530a575798d907aa42d9bdc2fc889d9b8f00167014271aa89f68665496f`.
- [x] License inventory review records 25 upstream `NOASSERTION` entries without treating the project MIT license as a dependency license.
- [ ] GitHub dependency review passes on the PR.
- [x] Manual CI run 30557441814 uploaded SBOM artifact 8765516776 from published commit 7e906b0.

No vulnerability exception is active. A future exception requires CVE, affected surface, compensating control, owner, and expiry.
