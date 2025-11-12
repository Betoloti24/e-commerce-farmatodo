# ETAPA 1: Construcción/Compilación (Builder)
# Usamos eclipse-temurin:21-jdk-jammy para la compilación (basado en Ubuntu 22.04)
FROM eclipse-temurin:21-jdk-jammy AS builder

# Establece el directorio de trabajo
WORKDIR /app

# Copia el JAR generado por Maven
COPY target/apigetway-0.0.1-SNAPSHOT.jar app.jar

# ETAPA 2: Ejecución (Runner)
# Usamos eclipse-temurin:21-jre-jammy para la ejecución, que es más ligera
FROM eclipse-temurin:21-jre-jammy

# Crea el grupo y usuario 'spring' para ejecutar la aplicación de forma segura
RUN groupadd spring && useradd spring -g spring

# Asigna el usuario 'spring' como el usuario por defecto
USER spring

# Copia el JAR desde la etapa 'builder'
COPY --from=builder /app/app.jar /home/spring/app.jar

# Puerto de la aplicación
EXPOSE 8080

# Comando de ejecución
ENTRYPOINT ["java", "-jar", "/home/spring/app.jar"]