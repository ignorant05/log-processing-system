# ==========================
# ====== Build Stage =======
# ==========================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

COPY pom.xml . 

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# ==========================
# ===== Runtime Stage ======
# ==========================
FROM eclipse-temurin:17-jre-alpine AS runner  

RUN  apk add --no-cache dumb-init

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder --chown=appuser:appgroup /app/target/klog.jar app.jar 

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
