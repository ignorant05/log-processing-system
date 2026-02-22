# log-processing-system

A CLI tool for producing, consuming, and inspecting Kafka log streams.

```sh
java -jar klog.jar <command> [options]
```

---

## Commands

| Command | Description |
|---|---|
| `generate` | Produce randomly generated log messages to a topic |
| `consume` | Consume and print messages from a topic |
| `topic` | Create, delete, describe, and list topics |
| `health` | Run a full connectivity and availability check |
| `metrics` | Print a snapshot of runtime metrics |

### Quick examples

```sh
# Check if everything is healthy
java -jar klog.jar health -b localhost:9092 -t logs

# Generate 100 test messages
java -jar klog.jar generate -b localhost:9092 -t logs -c 100 -i 500

# Consume messages
java -jar klog.jar consume -b localhost:9092 -t logs

# Topic operations
java -jar klog.jar topic create -n logs -p 4
java -jar klog.jar topic list
java -jar klog.jar topic describe -n logs
java -jar klog.jar topic delete -n logs

# View metrics
java -jar klog.jar metrics
java -jar klog.jar metrics --watch 2
```

---

## Installation

### Script (recommended)

```sh
curl -fsSL https://raw.githubusercontent.com/ignorant05/log-processing-system/main/install.sh | sh
```

### Docker

```sh
docker pull ghcr.io/ignorant05/log-processing-system:latest
docker run --rm ghcr.io/ignorant05/log-processing-system:latest health -b <broker>:9092 -t <topic>
```

### Build from source

```sh
git clone https://github.com/ignorant05/log-processing-system.git
cd log-processing-system
mvn package -DskipTests
java -jar target/klog.jar --help
```

---

## Run with Docker Compose

```sh
docker compose up -d
./test.sh
```

## Deploy to Kubernetes

```sh
# Start a minikube cluster with the amount of resources you want
minikube start --cpus 4 --memory 4096 
helm dependency update helm/log-processing-system
helm install klog helm/log-processing-system \
  --namespace klog \
  --create-namespace \
  --set kafka.enabled=false \
  --set externalKafka.bootstrapServers=confluent-kafka:9092

# Load the images you already have from Docker Compose into minikube
docker pull confluentinc/cp-zookeeper:7.4.0
minikube image load confluentinc/cp-zookeeper:7.4.0

docker pull confluentinc/cp-kafka:7.4.0
minikube image load confluentinc/cp-kafka:7.4.0

# Apply Confluent Kafka
kubectl apply -f k8s/confluent-kafka.yaml

# Wait for Kafka to be ready
kubectl rollout status deploy/zookeeper -n klog
kubectl rollout status deploy/confluent-kafka -n klog

# Install klog pointing at the external Confluent Kafka
helm install klog helm/log-processing-system \
  --namespace klog \
  --set kafka.enabled=false \
  --set externalKafka.bootstrapServers=confluent-kafka:9092

# Watch everything come up
kubectl get pods -n klog -w
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
