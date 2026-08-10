# Reuse Delta

Concrete generic gaps found and proven:

1. Evidence digests must bind the canonical payload; validators must reject tampered metrics.
2. Secret-ignore profiles need executable tests, including the `.env.example` exception.
3. Scanner policy must reject implicit suppression such as `ignore-unfixed` unless an explicit expiring exception exists.
4. Domain records should remain framework/serialization-free when infrastructure-owned wire DTO mapping is sufficient.
5. Streaming evidence must distinguish topology-driver and real-broker benchmark IDs and comparability keys.
6. Hardened JVM containers using RocksDB need a dedicated executable JNI temp mount without making `/tmp` executable.
7. Quota-aware work needs milestone checkpoints because exact weekly balance is not programmatically observable.

The project consumes kit 1.2.0. Items 1-4 and 6 are ready for compatibility-tested kit patches. Broker harness generalization still waits for a second streaming consumer.
