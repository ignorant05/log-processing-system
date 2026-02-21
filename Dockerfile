# ==========================
# ====== Build Stage =======
# ==========================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml . 

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# ==========================
# ===== Runtime Stage ======
# ==========================
FROM eclipse-temurin:17-jre AS runner

RUN apt-get update && apt-get install -y --no-install-recommends \
	dumb-init \
	&& rm -rf /var/lib/apt/lists/*
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

COPY --from=builder --chown=appuser:appgroup /app/target/klog.jar /klog.jar 

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod 755 /docker-entrypoint.sh && \
	chown appuser:appgroup /docker-entrypoint.sh

USER appuser

ENTRYPOINT ["/docker-entrypoint.sh"]
