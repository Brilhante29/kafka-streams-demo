# Reuse Delta

Concrete generic gaps found:

1. Streaming evidence must encode whether it came from a topology driver or real broker.
2. Hardened JVM containers that use RocksDB need a dedicated executable JNI temp mount without making `/tmp` executable.
3. Quota-aware work needs milestone checkpoints because exact weekly balance is not programmatically observable.

The project consumes kit 1.2.0 now. Generalization of the broker harness waits for a second streaming consumer or explicit kit acceptance.
