# Security Gates: #28 kafka-streams-demo

- [x] No credential is required by default.
- [x] `.env`, keys, credentials, and secret directories are ignored.
- [x] Local regex fallback secret scan passed over 159 candidate files.
- [x] Gradle dependencies are locked.
- [x] Gradle wrapper distribution and JAR checksums are audited.
- [x] JDK, JRE, Kafka Compose, and Testcontainers images are digest-pinned.
- [x] Runtime is UID/GID 10001 with read-only root, dropped capabilities, and `no-new-privileges`.
- [x] Broker host port is loopback-only.
- [x] `/tmp` remains `noexec`; executable RocksDB JNI temp storage is narrowly mounted.
- [ ] Trivy filesystem dependency/secret/misconfiguration scan passes in CI/RC.
- [ ] Trivy application image scan has no unaccepted HIGH/CRITICAL finding.
- [ ] SPDX JSON SBOM is generated and uploaded as an artifact.
- [ ] GitHub dependency review passes on the PR.
- [ ] License inventory is reviewed from the SBOM.

No scan result may be marked passed before execution. Exceptions require CVE, affected surface, compensating control, owner, and expiry.
