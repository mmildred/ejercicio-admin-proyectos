# Etapa de construccion (Multi-stage build) con Java 21 JDK
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar Maven desde imagen oficial para compilar en Alpine con Java 21
COPY --from=maven:3.9-eclipse-temurin-21-alpine /usr/share/maven /usr/share/maven
ENV MAVEN_HOME=/usr/share/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

# Copiar archivos del proyecto
COPY pom.xml .
COPY src ./src

# Compilar y empaquetar el microservicio Frontend
RUN mvn clean package -DskipTests

# Etapa final de ejecucion con Java 21 JRE ligero
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar artefacto .jar generado
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8082} -jar app.jar"]
