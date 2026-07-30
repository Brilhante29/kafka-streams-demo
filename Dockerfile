ARG JVM_VERSION=21
ARG JDK_IMAGE_DIGEST=sha256:da9d3a4f7650db39b918fc5a2c3da76556fb8cc8e5f3767cdea0bb409286951a
ARG JRE_IMAGE_DIGEST=sha256:273396ed5998598ed1091e8d72711c2d36980a0e65103859c55a4e977a41ffd3

FROM eclipse-temurin:${JVM_VERSION}-jdk@${JDK_IMAGE_DIGEST} AS build

WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts gradle.properties gradle.lockfile ./
COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew && \
    ./gradlew --no-daemon clean check integrationTestClasses installDist

FROM eclipse-temurin:${JVM_VERSION}-jre@${JRE_IMAGE_DIGEST} AS runtime

ARG SOURCE_COMMIT=0000000000000000000000000000000000000000
ARG DEPENDENCY_LOCK_DIGEST=sha256:0000000000000000000000000000000000000000000000000000000000000000

RUN groupadd --system --gid 10001 app && \
    useradd --uid 10001 --gid app --home-dir /app --no-create-home --shell /usr/sbin/nologin app

WORKDIR /app

COPY --from=build --chown=10001:10001 /workspace/build/install/kafka-streams-demo/ ./

RUN mkdir -p /app/build/tmp /app/build/streams-state && chown -R 10001:10001 /app

ENV SOURCE_COMMIT=${SOURCE_COMMIT} \
    DEPENDENCY_LOCK_DIGEST=${DEPENDENCY_LOCK_DIGEST} \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8 -Djava.io.tmpdir=/app/build/tmp"

USER 10001:10001

ENTRYPOINT ["/app/bin/kafka-streams-demo"]
CMD ["demo"]
