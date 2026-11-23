pipeline {
    agent any

    tools {
        // Herramientas configuradas en Jenkins -> Administrar Jenkins -> Tools
        jdk 'JDK21'
        maven 'Maven 3.9'
    }

    environment {
        // Variables de entorno para ejecución
        JAVA_HOME = tool 'JDK21'
        M2_HOME = tool 'Maven 3.9'
        PATH = "${JAVA_HOME}/bin:${M2_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout SCM') {
            steps {
                echo '=== Descargando código del repositorio GitHub ==='
                checkout scm
            }
        }

        stage('Validar Versiones de Entorno') {
            steps {
                echo '=== Verificando versiones de Java y Maven en Jenkins ==='
                // Compatible para agentes Windows (bat) o Linux (sh)
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

        stage('Compilar Microservicios (Java 21)') {
            steps {
                echo '=== Compilando ambos microservicios con Java 21 y Maven 3.9 ==='
                script {
                    if (isUnix()) {
                        sh 'mvn clean compile'
                    } else {
                        bat 'mvn clean compile'
                    }
                }
            }
        }

        stage('Ejecutar Pruebas Automatizadas y JaCoCo') {
            steps {
                echo '=== Ejecutando pruebas unitarias/integración y generando reporte JaCoCo ==='
                script {
                    if (isUnix()) {
                        sh 'mvn test'
                    } else {
                        bat 'mvn test'
                    }
                }
            }
        }

        stage('Analisis de Codigo SonarQube') {
            steps {
                echo '=== Ejecutando SonarScanner excluyendo carpetas target ==='
                script {
                    if (isUnix()) {
                        sh 'mvn sonar:sonar -Dsonar.exclusions=**/target/**'
                    } else {
                        bat 'mvn sonar:sonar -Dsonar.exclusions=**/target/**'
                    }
                }
            }
        }

        stage('Empaquetado (Package)') {
            steps {
                echo '=== Generando artefactos JAR de los microservicios ==='
                script {
                    if (isUnix()) {
                        sh 'mvn package -DskipTests'
                    } else {
                        bat 'mvn package -DskipTests'
                    }
                }
            }
        }
    }

    post {
        always {
            echo '=== Publicando reportes de pruebas y cobertura ==='
            // Publicación de reportes JUnit generados por Surefire
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true

            // Publicación de reporte de cobertura JaCoCo
            jacoco(
                execPattern: '**/target/jacoco.exec',
                classPattern: '**/target/classes',
                sourcePattern: '**/src/main/java',
                inclusionPattern: '**/*.class',
                exclusionPattern: '**/target/**'
            )
        }
        success {
            echo 'Pipeline ejecutado exitosamente con JDK 21 y Maven 3.9!'
        }
        failure {
            echo 'Error en la ejecucion del pipeline. Revise los logs de la consola.'
        }
    }
}
