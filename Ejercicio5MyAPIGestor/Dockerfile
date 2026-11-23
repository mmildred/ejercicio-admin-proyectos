# Dockerfile para Ejercicio5MyAPIGestor (Microservicio Backend API REST)
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Mildred Banda <mildred09b@gmail.com>"
LABEL description="Microservicio Gestor API (Backend REST Data JPA) - Java 21"

WORKDIR /app

# Copiar el artefacto generado por Maven en target/
COPY target/*.jar app.jar

# Exponer el puerto configurado del servicio
EXPOSE 8081

# Ejecutar el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]
