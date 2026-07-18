FROM gradle:8.10.2-jdk21 AS build

WORKDIR /workspace
COPY . .
RUN gradle --no-daemon clean check installDist

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/build/install/kafka-streams-demo/ ./

ENTRYPOINT ["/app/bin/kafka-streams-demo"]
CMD ["benchmark", "1000", "/app/benchmarks/results/latest.json"]
