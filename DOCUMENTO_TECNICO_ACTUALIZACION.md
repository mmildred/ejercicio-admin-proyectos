# Documento Técnico: Actualización Tecnológica de Microservicios a Java 21 y Pipelines CI en Jenkins

**Asignatura:** Desarrollo e Integración Continua  
**Práctica:** Actualización de Microservicios y Pipelines CI (Parte 1: Actualización Tecnológica)  
**Estudiante:** Mildred Banda (`mmildred`)  
**Fecha:** Noviembre 2026  
**Repositorio Principal:** [https://github.com/mmildred/ejercicio-admin-proyectos](https://github.com/mmildred/ejercicio-admin-proyectos)  

---

## 1. Resumen Ejecutivo y Propósito de la Práctica

El presente proyecto contiene una solución empresarial modularizada basada en Spring Boot compuesta por dos microservicios interconectados:
1. **`Ejercicio5Cliente` (Frontend / Web UI):** Aplicación MVC basada en **Spring Boot 3.2.5** y Thymeleaf que provee la interfaz gráfica de usuario para la administración de proyectos, usuarios, tareas y asignaciones.
2. **`Ejercicio5MyAPIGestor` (Backend / REST API):** Servicio API REST basado en **Spring Boot 2.7.17**, Spring Data JPA y base de datos MariaDB para la gestión y persistencia de las entidades del negocio.

### Objetivos Alcanzados en esta Entrega:
- **Migración Integral a Java 21 LTS:** Actualización tecnológica de ambos microservicios a la versión LTS más moderna del ecosistema Java.
- **Configuración de la Tríada de Calidad en Maven:** Incorporación estandarizada de `maven-surefire-plugin` (v3.2.5), `jacoco-maven-plugin` (v0.8.11) y `sonar-maven-plugin` (v5.5.0.6356) con compatibilidad estricta con bytecode de Java 21.
- **Exclusión de Directorios `target/`:** Configuración de directivas de escaneo de seguridad y análisis estático para excluir binarios generados sin comprometer la inspección del código fuente.
- **Alineación con Jenkins CI/CD:** Definición de herramientas globales (JDK 21 y Maven 3.9+) y diseño de un `Jenkinsfile` declarativo para la automatización de checkout, compilación, pruebas, cobertura y análisis de código.
- **Validación Local y en CI:** Ejecución exitosa de 23 pruebas automatizadas (7 en Cliente y 16 en Gestor) con resultado `BUILD SUCCESS` y 0 errores.

---

## 2. Justificación Técnica del Salto Tecnológico a Java 21 (LTS)

La decisión de migrar los microservicios desde versiones anteriores (**Java 17** en Cliente y **Java 11** en Gestor) hacia **Java 21 LTS** se fundamenta en los siguientes pilares de ingeniería de software:

| Aspecto Clave | Situación Anterior (Java 11 / 17) | Beneficio y Justificación en Java 21 LTS |
| :--- | :--- | :--- |
| **Soporte y Ciclo de Vida (LTS)** | Java 11 ya superó su periodo de soporte extendido estándar; Java 17 es LTS intermedio. | Java 21 es la versión LTS actual con soporte extendido hasta 2029+, garantizando parches de seguridad críticos. |
| **Virtual Threads (Project Loom - JEP 444)** | Hilos atados 1:1 al sistema operativo (costosos en memoria y context switching). | Permite millones de hilos ligeros concurrentes, ideal para microservicios I/O-bound (llamadas HTTP entre Cliente y Gestor API). |
| **Generational ZGC (JEP 439)** | Pausas de Garbage Collector perceptibles bajo alta carga en G1 o ParallelGC. | Recolección de basura con latencias sub-milisegundo (< 1 ms) sin impacto en el rendimiento de transacciones web. |
| **Sintaxis y Patrones Modernos** | Estructuras `instanceof` clásicas y casting repetitivo. | *Pattern Matching for switch* (JEP 441) y *Record Patterns* (JEP 440) para un código más expresivo, tipado y seguro. |
| **Compatibilidad con Ecosistema Moderno** | Versiones antiguas causan advertencias de Reflection y módulos restringidos. | Integración nativa con Maven 3.9+, Spring Boot 3+, JaCoCo 0.8.11+ y SonarScanner moderno. |

---

## 3. Análisis Comparativo de los Archivos `pom.xml`

Para dar cumplimiento formal al requerimiento de evidencia de entrega, se identifican y comparan los 4 archivos POM (2 originales y 2 actualizados).

### 3.1. Microservicio 1: `Ejercicio5Cliente`

#### Comparativa de Cambios:
- **Versión de Java:** Se actualizó de `<java.version>17</java.version>` a `<java.version>21</java.version>`.
- **Exclusión de artefactos en Sonar:** Se añadieron las propiedades `<sonar.exclusions>**/target/**</sonar.exclusions>` y `<sonar.coverage.jacoco.xmlReportPaths>` para vincular métricas de cobertura.
- **Maven Surefire Plugin:** Se agregó explícitamente la versión `3.2.5` para garantizar la ejecución de pruebas JUnit Jupiter en Java 21.
- **JaCoCo Maven Plugin:** Se integró la versión `0.8.11` con los objetivos `prepare-agent` y `report` (fase test).
- **Sonar Maven Plugin:** Se añadió el plugin oficial de SonarSource en su versión requerida `5.5.0.6356`.

```diff
--- pom-original-Ejercicio5Cliente.xml
+++ pom-actualizado-Ejercicio5Cliente.xml
@@ -16,3 +16,5 @@
 	<properties>
-		<java.version>17</java.version>
+		<java.version>21</java.version>
+		<sonar.exclusions>**/target/**</sonar.exclusions>
+		<sonar.coverage.jacoco.xmlReportPaths>${project.build.directory}/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
 	</properties>
@@ -48,2 +50,33 @@
 				<artifactId>spring-boot-maven-plugin</artifactId>
 			</plugin>
+			<plugin>
+				<groupId>org.apache.maven.plugins</groupId>
+				<artifactId>maven-surefire-plugin</artifactId>
+				<version>3.2.5</version>
+			</plugin>
+			<plugin>
+				<groupId>org.jacoco</groupId>
+				<artifactId>jacoco-maven-plugin</artifactId>
+				<version>0.8.11</version>
+				<executions>
+					<execution>
+						<goals>
+							<goal>prepare-agent</goal>
+						</goals>
+					</execution>
+					<execution>
+						<id>report</id>
+						<phase>test</phase>
+						<goals>
+							<goal>report</goal>
+						</goals>
+					</execution>
+				</executions>
+			</plugin>
+			<plugin>
+				<groupId>org.sonarsource.scanner.maven</groupId>
+				<artifactId>sonar-maven-plugin</artifactId>
+				<version>5.5.0.6356</version>
+			</plugin>
 		</plugins>
```

---

### 3.2. Microservicio 2: `Ejercicio5MyAPIGestor`

#### Comparativa de Cambios:
- **Versión de Java:** Se promovió de `<java.version>11</java.version>` a `<java.version>21</java.version>`.
- **Exclusión de artefactos en Sonar:** Se añadieron `<sonar.exclusions>**/target/**</sonar.exclusions>` y la ruta de reporte JaCoCo XML.
- **Maven Surefire Plugin:** Se incorporó explícitamente la versión `3.2.5` (anteriormente dependía de la versión heredada del parent 2.7.17, la cual arrojaba incompatibilidades con la JVM 21).
- **JaCoCo Maven Plugin:** Se homologó a la versión `0.8.11` compatible con Java 21 bytecode.
- **Sonar Maven Plugin:** Se integró la versión oficial `5.5.0.6356`.

```diff
--- pom-original-Ejercicio5MyAPIGestor.xml
+++ pom-actualizado-Ejercicio5MyAPIGestor.xml
@@ -16,3 +16,5 @@
 	<properties>
-		<java.version>11</java.version>
+		<java.version>21</java.version>
+		<sonar.exclusions>**/target/**</sonar.exclusions>
+		<sonar.coverage.jacoco.xmlReportPaths>${project.build.directory}/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
 	</properties>
@@ -46,2 +48,7 @@
 				<artifactId>spring-boot-maven-plugin</artifactId>
 			</plugin>
+			<plugin>
+				<groupId>org.apache.maven.plugins</groupId>
+				<artifactId>maven-surefire-plugin</artifactId>
+				<version>3.2.5</version>
+			</plugin>
 			<plugin>
@@ -67,2 +74,7 @@
 				</executions>
 			</plugin>
+			<plugin>
+				<groupId>org.sonarsource.scanner.maven</groupId>
+				<artifactId>sonar-maven-plugin</artifactId>
+				<version>5.5.0.6356</version>
+			</plugin>
 		</plugins>
```

---

## 4. Detalle y Justificación de los Plugins Incorporados

### 4.1. `sonar-maven-plugin` (Versión `5.5.0.6356`)
- **Propósito:** Conector oficial de SonarSource para ejecutar SonarScanner desde el ciclo de vida de Maven.
- **Justificación:** Permite realizar análisis estático de código fuente (SAST), detección de "Code Smells", bugs potenciales, deuda técnica y vulnerabilidades de seguridad según el estándar OWASP Top 10.
- **Directiva de Exclusión:** Se añadió en properties:
  ```xml
  <sonar.exclusions>**/target/**</sonar.exclusions>
  ```
  Esto evita analizar archivos `.class` compilados o librerías temporales, previniendo falsos positivos y acelerando el análisis.

### 4.2. `jacoco-maven-plugin` (Versión `0.8.11`)
- **Propósito:** Herramienta líder para cálculo de cobertura de código (instrucciones, ramas y líneas ejecutadas por pruebas).
- **Justificación:** Las versiones de JaCoCo previas a la 0.8.10 fallan con `Unsupported class file major version 65` al procesar bytecode de Java 21. La versión **0.8.11** ofrece soporte completo y estable para Java 21.
- **Configuración de Fases:**
  - `prepare-agent`: Instrumenta la JVM durante los tests para registrar la ejecución en `target/jacoco.exec`.
  - `report` (en fase `test`): Genera los reportes legibles en `target/site/jacoco/index.html` y en formato XML (`jacoco.xml`) para su ingesta directa en SonarQube o Jenkins.

### 4.3. `maven-surefire-plugin` (Versión `3.2.5`)
- **Propósito:** Ejecución de pruebas unitarias durante el goal `mvn test`.
- **Justificación:** Versiones antiguas de Surefire (v2.x) tienen incompatibilidades con JUnit Platform (JUnit 5) y provocan errores de reflection o carga de agentes dinámicos en Java 21. La versión **3.2.5** soporta la arquitectura modular y los flags modernos del JDK 21.

---

## 5. Guía de Configuración en Jenkins CI/CD

Para asegurar que los pipelines de integración continua funcionen correctamente en Jenkins con Java 21 y Maven, se requiere la siguiente configuración:

### 5.1. Configuración de JDK 21 en Jenkins
1. Ir a **Administrar Jenkins** (*Manage Jenkins*) > **Configuración de herramientas globales** (*Tools*).
2. Localizar la sección **JDK** (*JDK installations*) y hacer clic en **Añadir JDK** (*Add JDK*):
   - **Nombre:** `JDK21` (o `Java21`).
   - **Modo 1 (Instalación Local / Existente):** Desmarcar la casilla *"Instalar automáticamente"* y configurar la ruta absoluta del JDK en el servidor o agente:
     - En Windows: `C:\Program Files\Java\jdk-21.0.12.1`
     - En Linux / Docker: `/usr/lib/jvm/temurin-21-jdk` (o `/opt/java/openjdk`)
   - **Modo 2 (Instalación Automática):** Marcar *"Instalar automáticamente"* > Seleccionar *Extract *.zip/*.tar.gz* o proveedor *Adoptium / Eclipse Temurin* versión `jdk-21.*`.
3. Guardar cambios (*Save / Apply*).

### 5.2. Configuración de Maven Compatible en Jenkins
1. En la misma pantalla de **Tools**, localizar la sección **Maven** (*Maven installations*).
2. Hacer clic en **Añadir Maven** (*Add Maven*):
   - **Nombre:** `Maven 3.9` (o `Maven3.9.6`).
   - **Instalación:** Marcar *"Instalar automáticamente"* y seleccionar **Apache 3.9.6** (o 3.9.9).
   - *(Alternativa Local)*: Desmarcar y definir `MAVEN_HOME = C:\ProgramData\chocolatey\lib\maven...` o la ruta local de Maven.
3. Justificación: Maven 3.9.x es la versión que garantiza compatibilidad plena con el compilador `javac` de Java 21 (`--release 21`).

### 5.3. Configuración en Pipelines de Jenkins (`Jenkinsfile`)
El repositorio incluye un archivo declarativo `Jenkinsfile` listo para ejecutarse en Jenkins:

```groovy
pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven 3.9'
    }

    environment {
        JAVA_HOME = tool 'JDK21'
        M2_HOME = tool 'Maven 3.9'
        PATH = "${JAVA_HOME}/bin:${M2_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Verificar Entorno') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'java -version'
                        sh 'mvn -version'
                    } else {
                        bat 'java -version'
                        bat 'mvn -version'
                    }
                }
            }
        }

        stage('Compilar Microservicios') {
            steps {
                script {
                    if (isUnix()) { sh 'mvn clean compile' }
                    else { bat 'mvn clean compile' }
                }
            }
        }

        stage('Pruebas y Cobertura JaCoCo') {
            steps {
                script {
                    if (isUnix()) { sh 'mvn test' }
                    else { bat 'mvn test' }
                }
            }
        }

        stage('Escaneo SonarQube (Excluyendo target)') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn sonar:sonar -Dsonar.exclusions=**/target/**'
                    } else {
                        bat 'mvn sonar:sonar -Dsonar.exclusions=**/target/**'
                    }
                }
            }
        }
    }

    post {
        always {
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            jacoco(
                execPattern: '**/target/jacoco.exec',
                classPattern: '**/target/classes',
                sourcePattern: '**/src/main/java',
                exclusionPattern: '**/target/**'
            )
        }
    }
}
```

---

## 6. Evidencia de Validación Local y Ejecución de Pruebas

Se ejecutó la suite completa de compilación y pruebas automatizadas sobre el proyecto raíz utilizando el JDK 21 (`Java 21.0.12.1`) y Maven 3.9.6:

```text
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO]   Ejercicio5Cliente [jar]
[INFO]   Ejercicio5MyAPIGestor [jar]
[INFO]   admin-proyectos [pom]
...
[INFO] Running com.mx.cbtis.ClienteIntegracionTests
[INFO] Running com.mx.cbtis.Ejercicio5ClienteApplicationTests
[INFO] Results: Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Loading execution data file ...\Ejercicio5Cliente\target\jacoco.exec
[INFO] Analyzed bundle 'Ejercicio5Cliente' with 7 classes
...
[INFO] Running com.mx.cbtis.gestor.Ejercicio5MyApiGestorApplicationTests
[INFO] Running com.mx.cbtis.gestor.GestorUnitTests
[INFO] Results: Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] Loading execution data file ...\Ejercicio5MyAPIGestor\target\jacoco.exec
[INFO] Analyzed bundle 'Ejercicio5MyAPIGestor' with 11 classes
...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for admin-proyectos 0.0.1-SNAPSHOT:
[INFO] 
[INFO] Ejercicio5Cliente .................................. SUCCESS [ 31.554 s]
[INFO] Ejercicio5MyAPIGestor .............................. SUCCESS [ 13.580 s]
[INFO] admin-proyectos .................................... SUCCESS [  0.002 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time: 46.788 s
[INFO] Finished at: 2026-11-23T12:38:56-06:00
```

---

## 7. Ligas de GitHub y Evidencias de Entrega

El proyecto se encuentra versionado y estructurado en GitHub. A continuación, se detallan las ligas de acceso solicitadas por el docente:

1. **Liga al Microservicio Frontend (`Ejercicio5Cliente`):**  
   [https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/Ejercicio5Cliente](https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/Ejercicio5Cliente)

2. **Liga al Microservicio Backend (`Ejercicio5MyAPIGestor`):**  
   [https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/Ejercicio5MyAPIGestor](https://github.com/mmildred/ejercicio-admin-proyectos/tree/main/Ejercicio5MyAPIGestor)

3. **Liga al Repositorio Principal con el historial de commits y actualización:**  
   [https://github.com/mmildred/ejercicio-admin-proyectos](https://github.com/mmildred/ejercicio-admin-proyectos)

4. **Archivos Físicos de Evidencia de POMs (dentro de la carpeta `evidencias_entrega/`):**
   - [pom-original-Ejercicio5Cliente.xml](https://github.com/mmildred/ejercicio-admin-proyectos/blob/main/evidencias_entrega/pom-original-Ejercicio5Cliente.xml)
   - [pom-actualizado-Ejercicio5Cliente.xml](https://github.com/mmildred/ejercicio-admin-proyectos/blob/main/evidencias_entrega/pom-actualizado-Ejercicio5Cliente.xml)
   - [pom-original-Ejercicio5MyAPIGestor.xml](https://github.com/mmildred/ejercicio-admin-proyectos/blob/main/evidencias_entrega/pom-original-Ejercicio5MyAPIGestor.xml)
   - [pom-actualizado-Ejercicio5MyAPIGestor.xml](https://github.com/mmildred/ejercicio-admin-proyectos/blob/main/evidencias_entrega/pom-actualizado-Ejercicio5MyAPIGestor.xml)

---

## 8. Conclusiones

La actualización a Java 21 y la incorporación de los plugins requeridos (`sonar-maven-plugin` 5.5.0.6356, `jacoco-maven-plugin` 0.8.11 y `maven-surefire-plugin` 3.2.5) modernizan la base de código de ambos microservicios, eliminan cuellos de botella técnicos en la fase de CI/CD y preparan el ecosistema para la ejecución de los tres pipelines funcionales de integración continua requeridos para la siguiente fase de la práctica.
