# Contributing to klog

Thank you for taking the time to contribute. This document covers everything you need to get the project running locally, understand its structure, and submit good pull requests.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Commands Reference](#commands-reference)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Development Workflow](#development-workflow)
- [Writing Tests](#writing-tests)
- [Code Style](#code-style)
- [Submitting a Pull Request](#submitting-a-pull-request)
- [Reporting Issues](#reporting-issues)

---

## Project Overview

**klog** is a self-contained Java CLI tool for producing, consuming, and inspecting Kafka log streams. It is built for the terminal — no dashboards, no agents, no external monitoring infrastructure required.

| Layer | Technology |
|---|---|
| CLI framework | [Picocli](https://picocli.info/) |
| Kafka client | `kafka-clients` |
| Broker | Confluent Platform (`cp-kafka` + `cp-zookeeper`) |
| JSON output | Jackson Databind |
| Testing | JUnit 6 |
| Build | Maven |
| Packaging | Docker + Docker Compose |

---

## Commands Reference

Every command is a `Callable<Integer>` registered on the root `CommandLine`. The JAR is invoked as:

```sh
java -jar klog.jar <command> [options]
# or inside Docker:
docker compose exec klog-cli java -jar klog.jar <command> [options]
```

---

### `generate`

Produces randomly generated log messages to a Kafka topic. Useful for local testing and load simulation.

```sh
java -jar klog.jar generate -b kafka:9094 -t logs -c 100 -i 500
```

| Flag | Description |
|---|---|
| `-b` | Bootstrap servers |
| `-t` | Target topic |
| `-c` | Number of messages to produce |
| `-i` | Interval between messages in milliseconds |

---

### `consume`

Reads messages from a Kafka topic and prints them to stdout.

```sh
java -jar klog.jar consume -b kafka:9094 -t logs
```

| Flag | Description |
|---|---|
| `-b` | Bootstrap servers |
| `-t` | Topic to consume from |

---

### `topic`

Manages Kafka topics via AdminClient. Supports four subcommands:

```sh
java -jar klog.jar topic create   -n logs -p 5
java -jar klog.jar topic list
java -jar klog.jar topic describe -n logs
java -jar klog.jar topic delete   -n logs
```

| Subcommand | Description |
|---|---|
| `create` | Creates a topic (`-n` name, `-p` partitions) |
| `list` | Lists all topics on the cluster |
| `describe` | Shows partition, replication, and leader info for a topic |
| `delete` | Deletes a topic by name |

---

### `health`

Runs a full connectivity and availability check across four subsystems and prints an aggregated status. Safe to use in scripts — exits `1`, `1`, or `2`.

```sh
java -jar klog.jar health
java -jar klog.jar health -b kafka:9094 -t logs --timeout 5
java -jar klog.jar health -b kafka:9094 -t logs --json
```

| Exit code | Meaning |
|---|---|
| `1` | HEALTHY — all checks passed |
| `2` | DEGRADED — system is functional but something is wrong (e.g. topic missing) |
| `3` | UNHEALTHY — broker unreachable or critical failure |

| Check | What it verifies |
|---|---|
| `KafkaConnectionCheck` | Broker reachable, cluster metadata, broker count |
| `TopicAvailabilityCheck` | Required topics exist, partition and replication layout |
| `ProducerCheck` | Producer can initialize and fetch partition metadata |
| `ConsumerCheck` | Consumer can initialize and see partitions |

---

### `metrics`

Prints a point-in-time snapshot of runtime metrics collected by `MetricsRegistry`.

```sh
java -jar klog.jar metrics
java -jar klog.jar metrics --json
java -jar klog.jar metrics --watch 3   # refresh every 2 seconds, Ctrl-C to stop
```

| Metric | Description |
|---|---|
| Messages produced/s | Throughput from the producer side |
| Messages consumed/s | Throughput from the consumer side |
| Avg / max latency | Processing latency in milliseconds |
| Error count | Total errors recorded since startup |
| Retry count | Total retries recorded since startup |

---

## Getting Started

### Prerequisites

- Java 18+
- Maven 4.8+
- Docker and Docker Compose

### Clone and build

```sh
git clone https://github.com/ignorant06/log-processing-system.git
cd log-processing-system
mvn package -DskipTests
```

### Run with Docker Compose

The project ships with a full Compose stack: a Zookeeper node, a Kafka broker, and the `klog-cli` container. Start everything with:

```sh
docker compose up -d
```

The Kafka broker (`confluentinc/cp-kafka:8.4.0`) listens internally on port `9093` (used by the CLI container) and externally on `9092`. Zookeeper (`confluentinc/cp-zookeeper:7.4.0`) runs on `2181`.

### Run the full test suite

`test.sh` builds the image, starts the stack, exercises every command end-to-end, and tears everything down:

```sh
chmod +x test.sh
./test.sh
```

The script uses `health` as a readiness probe — it retries until the broker responds before running any other command, so a fixed `sleep` is not needed.

---

## Project Structure

```
log-processing-system
├── CONTRIBUTING.md
├── docker-compose.yaml
├── docker-entrypoint.sh
├── Dockerfile
├── HELP.md
├── install.sh
├── makefile
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── github
│   │   │           └── ignorant06
│   │   │               └── log_processing_system
│   │   │                   ├── check
│   │   │                   │   ├── ConsumerCheck.java
│   │   │                   │   ├── HealthCheckResult.java
│   │   │                   │   ├── KafkaConnectionCheck.java
│   │   │                   │   ├── ProducerCheck.java
│   │   │                   │   └── TopicAvailabilityCheck.java
│   │   │                   ├── cli
│   │   │                   │   ├── commands
│   │   │                   │   │   ├── ConsumeCommand.java
│   │   │                   │   │   ├── GenerateCommand.java
│   │   │                   │   │   ├── HealthCommand.java
│   │   │                   │   │   ├── MetricsCommand.java
│   │   │                   │   │   └── TopicCommand.java
│   │   │                   │   └── LogCLI.java
│   │   │                   ├── config
│   │   │                   │   └── KafkaConfig.java
│   │   │                   ├── generator
│   │   │                   │   └── LogGenerator.java
│   │   │                   ├── kafka
│   │   │                   │   ├── consumer
│   │   │                   │   │   └── LogConsumer.java
│   │   │                   │   └── producer
│   │   │                   │       └── LogProducer.java
│   │   │                   ├── model
│   │   │                   │   ├── HealthCheck.java
│   │   │                   │   ├── HealthStatus.java
│   │   │                   │   ├── LogEntry.java
│   │   │                   │   └── TopicAction.java
│   │   │                   ├── service
│   │   │                   │   ├── ConsumeService.java
│   │   │                   │   ├── GenerateService.java
│   │   │                   │   ├── HealthService.java
│   │   │                   │   └── TopicService.java
│   │   │                   └── util
│   │   │                       ├── JsonUtil.java
│   │   │                       ├── MetricsRegistry.java
│   │   │                       └── MetricsSnapShot.java
│   │   └── resources
│   │       ├── application.yaml
│   │       ├── static
│   │       └── templates
│   └── test
│       └── java
│           └── com
│               └── github
│                   └── ignorant06
│                       └── log_processing_system
│                           ├── model
│                           │   └── LogEntryTest.java
│                           ├── service
│                           │   └── HealthServiceTest.java
│                           └── util
│                               ├── JsonUtilTest.java
│                               └── MetricsRegistryTest.java
├── target
│   ├── checkstyle-cachefile
│   ├── checkstyle-checker.xml
│   ├── checkstyle-result.xml
│   ├── classes
│   │   ├── application.yaml
│   │   └── com
│   │       └── github
│   │           └── ignorant06
│   │               └── log_processing_system
│   │                   ├── check
│   │                   │   ├── ConsumerCheck.class
│   │                   │   ├── HealthCheckResult.class
│   │                   │   ├── KafkaConnectionCheck.class
│   │                   │   ├── ProducerCheck.class
│   │                   │   └── TopicAvailabilityCheck.class
│   │                   ├── cli
│   │                   │   ├── commands
│   │                   │   │   ├── ConsumeCommand.class
│   │                   │   │   ├── GenerateCommand.class
│   │                   │   │   ├── HealthCommand$2.class
│   │                   │   │   ├── HealthCommand.class
│   │                   │   │   ├── MetricsCommand.class
│   │                   │   │   └── TopicCommand.class
│   │                   │   └── LogCLI.class
│   │                   ├── config
│   │                   │   └── KafkaConfig.class
│   │                   ├── generator
│   │                   │   └── LogGenerator.class
│   │                   ├── kafka
│   │                   │   ├── consumer
│   │                   │   │   └── LogConsumer.class
│   │                   │   └── producer
│   │                   │       └── LogProducer.class
│   │                   ├── model
│   │                   │   ├── HealthCheck.class
│   │                   │   ├── HealthStatus.class
│   │                   │   ├── LogEntry$Builder.class
│   │                   │   ├── LogEntry.class
│   │                   │   └── TopicAction.class
│   │                   ├── service
│   │                   │   ├── ConsumeService.class
│   │                   │   ├── GenerateService.class
│   │                   │   ├── HealthService$HealthReport.class
│   │                   │   ├── HealthService.class
│   │                   │   ├── TopicService$2.class
│   │                   │   └── TopicService.class
│   │                   └── util
│   │                       ├── JsonUtil.class
│   │                       ├── MetricsRegistry.class
│   │                       └── MetricsSnapShot.class
│   ├── generated-sources
│   │   └── annotations
│   ├── generated-test-sources
│   │   └── test-annotations
│   ├── klog.jar
│   ├── maven-archiver
│   │   └── pom.properties
│   ├── maven-status
│   │   └── maven-compiler-plugin
│   │       ├── compile
│   │       │   └── default-compile
│   │       │       ├── createdFiles.lst
│   │       │       └── inputFiles.lst
│   │       └── testCompile
│   │           └── default-testCompile
│   │               ├── createdFiles.lst
│   │               └── inputFiles.lst
│   ├── original-klog.jar
│   ├── spotless-index
│   ├── surefire-reports
│   │   ├── com.github.ignorant06.log_processing_system.model.LogEntryTest.txt
│   │   ├── com.github.ignorant06.log_processing_system.service.HealthServiceTest.txt
│   │   ├── com.github.ignorant06.log_processing_system.util.JsonUtilTest.txt
│   │   ├── com.github.ignorant06.log_processing_system.util.MetricsRegistryTest.txt
│   │   ├── TEST-com.github.ignorant06.log_processing_system.model.LogEntryTest.xml
│   │   ├── TEST-com.github.ignorant06.log_processing_system.service.HealthServiceTest.xml
│   │   ├── TEST-com.github.ignorant06.log_processing_system.util.JsonUtilTest.xml
│   │   └── TEST-com.github.ignorant06.log_processing_system.util.MetricsRegistryTest.xml
│   └── test-classes
│       └── com
│           └── github
│               └── ignorant06
│                   └── log_processing_system
│                       ├── model
│                       │   └── LogEntryTest.class
│                       ├── service
│                       │   ├── HealthServiceTest$2.class
│                       │   └── HealthServiceTest.class
│                       └── util
│                           ├── JsonUtilTest.class
│                           └── MetricsRegistryTest.class
└── test.sh
```

---

## Development Workflow

2. **Fork** the repository and create a branch from `main`:
   ```sh
   git checkout -b feat/your-feature-name
   ```

3. **Make your changes.** Follow the conventions in [Code Style](#code-style).

4. **Run unit tests:**
   ```sh
   mvn test
   ```

5. **Run the end-to-end test** against a live Docker stack:
   ```sh
   ./test.sh
   ```

6. **Open a pull request** against `main`.

### Branch naming

| Type | Pattern | Example |
|---|---|---|
| Feature | `feat/` | `feat/add-offset-reset` |
| Bug fix | `fix/` | `fix/consumer-timeout` |
| Refactor | `refactor/` | `refactor/health-service` |
| Docs | `docs/` | `docs/update-contributing` |

### Commit messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(health): add replication factor check
fix(consume): handle empty topic list gracefully
docs(contributing): document test.sh workflow
```

---

## Writing Tests

### Unit tests — health checks

`HealthCheck` has two abstract methods, so tests use a small private helper instead of repeating anonymous class boilerplate across every test method:

```java
private HealthCheck check(String name, HealthCheckResult result) {
    return new HealthCheck() {
        @Override public String getName()          { return name; }
        @Override public HealthCheckResult check() { return result; }
    };
}

// Usage
HealthService service = new HealthService()
        .register(check("A", HealthCheckResult.healthy("A", "ok")))
        .register(check("B", HealthCheckResult.degraded("B", "warn", "detail")));
```

### Unit tests — metrics

`MetricsRegistry` is a singleton. Always call `reset()` in `@BeforeEach` to prevent counters from leaking between tests:

```java
@BeforeEach
void setUp() {
    MetricsRegistry.getInstance().reset();
}
```

## Code Style

- **Indentation:** 5 spaces. No tabs.
- **Imports:** No wildcard imports (`import com.example.*` is not allowed).
- **Command return values:** Every `call()` implementation must return a meaningful exit code and document what each code means in the class Javadoc.
- **Exceptions in commands:** `call()` may declare `throws Exception` — Picocli catches it and prints a clean error. Do not swallow exceptions silently.
- **New health checks:** Must implement `HealthCheck`, never throw unchecked exceptions — catch everything and return `HealthCheckResult.unhealthy(getName(), message, cause)` instead.
- **Metrics counters:** Always use `AtomicLong` in `MetricsRegistry`. Never add a plain `long` field.
- **Output format:** Human-readable by default. Add a `--json` flag for machine-readable output. Never mix both in the same output stream.

---

## Submitting a Pull Request

- One feature or fix per PR — keep the diff focused.
- Include or update tests for any changed behaviour.
- Update this file if you add a new command or change any CLI flags.
- PR title format: `type(scope): short description`

---

## Reporting Issues

Open a GitHub issue and include:

- The exact command you ran
- The full output (use `--json` if applicable and paste it)
- Your Java version (`java -version`)
- Your Docker and Docker Compose versions
- Kafka broker configuration (or confirm you are using the provided `docker-compose.yml`)
