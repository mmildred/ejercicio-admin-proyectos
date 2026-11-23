# Dockerfile para Ejercicio5Cliente (Microservicio Frontend Web)
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Mildred Banda <mildred09b@gmail.com>"
LABEL description="Microservicio Cliente (Frontend Thymeleaf) - Java 21"

WORKDIR /app

# Copiar el artefacto generado por Maven en target/
COPY target/*.jar app.jar

# Exponer el puerto configurado del servicio
EXPOSE 8082

# Ejecutar el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]
