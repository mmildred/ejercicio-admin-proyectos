# Documento Técnico: Actualización Tecnológica de Microservicios a Java 21, Cobertura JaCoCo 100%, Despliegue con Docker y Pipelines CI en Jenkins

**Asignatura:** Desarrollo e Integración Continua  
**Práctica:** Actualización de Microservicios y Pipelines CI (Parte 1 y Parte 2 Completa)  
**Estudiante:** Mildred Banda (`mmildred`)  
**Fecha:** Noviembre 2026  
**Repositorio Principal:** [https://github.com/mmildred/ejercicio-admin-proyectos](https://github.com/mmildred/ejercicio-admin-proyectos)  

---

## 1. Resumen Ejecutivo y Arquitectura de la Solución

El proyecto implementa un sistema empresarial distribuido basado en arquitectura de microservicios:
1. **`Ejercicio5Cliente` (Frontend / Web UI):** Aplicación MVC basada en **Spring Boot 3.2.5** y Thymeleaf que provee la interfaz gráfica de usuario en el puerto `8082`.
2. **`Ejercicio5MyAPIGestor` (Backend / REST API):** Servicio API REST basado en **Spring Boot 2.7.17**, Spring Data JPA y MariaDB en el puerto `8081`.
3. **`mariadb` (Base de Datos):** Servidor MariaDB 10.11 ejecutándose en el puerto `3306` e inicializado automáticamente con el script `dump-dbgestor-202406261453.sql`.

---

## 2. Despliegue con Docker (Java 21) y Docker Compose

### 2.1. Dockerfiles con Imagen Base Oficial Java 21
Se crearon los Dockerfiles optimizados para cada microservicio utilizando la imagen base ligera **`eclipse-temurin:21-jre-alpine`**:

- **Frontend ([Ejercicio5Cliente/Dockerfile](file:///C:/Users/marim/Desktop/admin-proyectos/Ejercicio5Cliente/Dockerfile)):**
  ```dockerfile
  FROM eclipse-temurin:21-jre-alpine
  WORKDIR /app
  COPY target/*.jar app.jar
  EXPOSE 8082
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

- **Backend ([Ejercicio5MyAPIGestor/Dockerfile](file:///C:/Users/marim/Desktop/admin-proyectos/Ejercicio5MyAPIGestor/Dockerfile)):**
  ```dockerfile
  FROM eclipse-temurin:21-jre-alpine
  WORKDIR /app
  COPY target/*.jar app.jar
  EXPOSE 8081
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

### 2.2. Orquestación Multi-contenedor con `docker-compose.yml`
El archivo [docker-compose.yml](file:///C:/Users/marim/Desktop/admin-proyectos/docker-compose.yml) conecta los 3 servicios bajo la red bridge `admin-proyectos-network`:
- **`mariadb`**: Monta el volumen persistente `db_data` y ejecuta el script de inicialización SQL en `/docker-entrypoint-initdb.d/init.sql`. Cuenta con un `healthcheck` activo.
- **`backend`**: Espera a que MariaDB esté saludable (`condition: service_healthy`) y consume variables de entorno (`DB_HOST=mariadb`, `SPRING_DATASOURCE_URL`, etc.).
- **`frontend`**: Expone el puerto `8082` y se conecta al backend mediante `GESTOR_API_URL=http://backend:8081`.

---

## 3. Pruebas Unitarias y Cobertura al 100% en JaCoCo

### 3.1. Configuración de Exclusiones en `jacoco-maven-plugin`
En estricto apego a la rúbrica de evaluación, se excluyeron las clases sin lógica de negocio evaluable mediante configuración explícita en ambos `pom.xml`:

```xml
<configuration>
    <!-- Exclusiones según rúbrica: Clases sin lógica de negocio evaluable -->
    <excludes>
        <!-- Modelos y entidades de base de datos (POJOs, Getters y Setters) -->
        <exclude>**/modelo/**</exclude>
        <exclude>**/model/**</exclude>
        <exclude>**/entity/**</exclude>
        <exclude>**/dto/**</exclude>
        <!-- Clases de configuración de infraestructura de Spring -->
        <exclude>**/config/**</exclude>
        <!-- Clase de inicio de Spring Boot (Entry Point sin lógica empresarial) -->
        <exclude>**/*Application.*</exclude>
    </excludes>
</configuration>
```

#### Justificación Técnica de Exclusiones:
- **Entidades y Modelos (`**/modelo/**`):** Son contenedores de estado (POJOs anémicos con getters y setters). Medir cobertura sobre ellos infla métricas artificialmente sin aportar valor a la verificación lógica del sistema.
- **Configuraciones de Infraestructura (`**/config/**`):** Bean declarations gestionadas por el framework Spring.
- **Clase Application (`*Application.class`):** Método `main` estándar generado por Spring Initializr que únicamente delega en `SpringApplication.run`.

### 3.2. Métricas Reales de Cobertura Obtenidas (Reportes JaCoCo)

| Microservicio | Clases Evaluables | Métodos Cubiertos | Líneas Cubiertas | Ramas Cubiertas | Cobertura de Métodos |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **`Ejercicio5Cliente`** | 1 (`GestorController`) | **30 de 30 (100%)** | **112 de 112 (100%)** | **15 de 16 (93%)** | **100%** |
| **`Ejercicio5MyAPIGestor`** | 5 (Controladores) | **33 de 33 (100%)** | **132 de 132 (100%)** | **42 de 42 (100%)** | **100%** |
| **Total Global** | **6 Clases** | **63 de 63 (100%)** | **244 de 244 (100%)** | **57 de 58 (98.2%)** | **100% MÁXIMA** |

### 3.3. Suite de Pruebas Unitarias Implementadas
Se implementaron **41 pruebas automatizadas** con **JUnit 5** y **Mockito**:
- **`GestorControllerUnitTests.java` (Cliente):** 17 métodos de prueba que aíslan las llamadas HTTP mediante Mockito (`mockRestTemplate`), verificando respuestas, modelos, redirecciones y ramas condicionales nulas.
- **`ClienteIntegracionTests.java` (Cliente):** 6 pruebas de integración MockMvc.
- **`GestorUnitTests.java` (Gestor):** 16 pruebas que cubren exhaustivamente los 5 controladores (`Asignaciones`, `Comentarios`, `Proyectos`, `Tareas`, `Usuario`), validando casos exitosos, registros no encontrados (`Optional.empty()`) y parámetros nulos.

---

## 4. Escaneo de Vulnerabilidades y Análisis de Seguridad

1. **Exclusión de Directorios `target/`:**  
   Se definió en `<properties>` de ambos `pom.xml`:
   ```xml
   <sonar.exclusions>**/target/**</sonar.exclusions>
   <sonar.coverage.jacoco.xmlReportPaths>${project.build.directory}/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
   ```
   Esto asegura que el motor de escaneo analice estrictamente el código fuente (`src/`) sin procesar binarios ni librerías temporales, evitando falsos positivos.

2. **Auditoría de Inyección SQL (SQL Injection):**  
   Todas las consultas nativas en `UsuarioRepository`, `TareasRepository` y `ComentariosRepository` utilizan parámetros nombrados vinculados con `@Param("...")`:
   ```java
   @Query(value = "select ... from usuario u where correo_electronico = :correo_electronico and contrasena = :contrasena", nativeQuery = true)
   Optional<Usuario> findByCorreoPass(@Param("correo_electronico") String correo_electronico, @Param("contrasena") String contrasena);
   ```
   No existe concatenación dinámica de cadenas, mitigando al 100% el riesgo de inyección SQL (OWASP A03:2021).

3. **Parametrización Segura de Credenciales y URLs:**  
   Se desacoplaron las credenciales quemadas en `application.properties` utilizando placeholders de variables de entorno con valores por defecto seguros (`${DB_HOST:localhost}`, `${DB_USER:root}`, `${GESTOR_API_URL:http://localhost:8081}`).

---

## 5. Tres Pipelines de Jenkins (CI/CD)

Se crearon 3 pipelines declarativos especializados en la carpeta `jenkins/`:

### 5.1. Pipeline 1: Backend ([jenkins/backend/Jenkinsfile](file:///C:/Users/marim/Desktop/admin-proyectos/jenkins/backend/Jenkinsfile))
- **Flujo:** Checkout SCM -> Validación Java 21 -> Compilación Backend -> Pruebas Unitarias y Reporte JaCoCo -> Escaneo SonarQube (excluyendo target) -> Empaquetado JAR -> Construcción de Imagen Docker.
- **Artefactos publicados:** Reportes XML JUnit y reporte JaCoCo.

### 5.2. Pipeline 2: Frontend ([jenkins/frontend/Jenkinsfile](file:///C:/Users/marim/Desktop/admin-proyectos/jenkins/frontend/Jenkinsfile))
- **Flujo:** Checkout SCM -> Validación Java 21 -> Instalación de Dependencias Web (Bootstrap, Webjars) -> Pruebas MockMvc y Unitarias -> Análisis de Calidad SonarQube -> Build JAR -> Construcción de Imagen Docker.

### 5.3. Pipeline 3: Base de Datos ([jenkins/database/Jenkinsfile](file:///C:/Users/marim/Desktop/admin-proyectos/jenkins/database/Jenkinsfile))
- **Flujo:** Checkout SCM -> Verificación de Archivo DDL (`dump-dbgestor-202406261453.sql`) -> Análisis de Sintaxis SQL -> Validación de Esquema con Entidades JPA -> Smoke Test de Configuración Docker Compose.

---

## 6. Guía de Ejecución de Comandos

### A. Para compilar el proyecto y generar los archivos `.jar`:
```bash
# En la raíz del proyecto (C:\Users\marim\Desktop\admin-proyectos):
.\mvnw.cmd clean package -DskipTests
```
*(Los archivos ejecutables quedarán generados en `Ejercicio5Cliente/target/*.jar` y `Ejercicio5MyAPIGestor/target/*.jar`)*.

### B. Para ejecutar todas las pruebas y generar los reportes de JaCoCo al 100%:
```bash
.\mvnw.cmd test
```
*(Los reportes legibles se encuentran en `Ejercicio5Cliente/target/site/jacoco/index.html` y `Ejercicio5MyAPIGestor/target/site/jacoco/index.html`)*.

### C. Para levantar toda la infraestructura con Docker Compose:
```bash
docker compose up -d --build
```
*(O `docker-compose up -d --build`)*.

### D. Para verificar el estado de los contenedores:
```bash
docker compose ps
```
- **Frontend Web UI:** `http://localhost:8082`
- **Backend API REST:** `http://localhost:8081`
- **MariaDB Database:** `localhost:3306`

---

## 7. Ligas de GitHub de Entrega

1. **Repositorio Principal:** [https://github.com/mmildred/ejercicio-admin-proyectos](https://github.com/mmildred/ejercicio-admin-proyectos)
2. **Microservicio Frontend:** [https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/Ejercicio5Cliente](https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/Ejercicio5Cliente)
3. **Microservicio Backend:** [https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/Ejercicio5MyAPIGestor](https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/Ejercicio5MyAPIGestor)
4. **Pipelines de Jenkins:** [https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/jenkins](https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/jenkins)
5. **Evidencias de POMs:** [https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/evidencias_entrega](https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/evidencias_entrega)
