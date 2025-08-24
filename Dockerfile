# Etapa 1: Construir la aplicación con Maven
FROM maven:3.8-openjdk-17 AS build

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el pom.xml para descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el resto del código fuente
COPY src ./src

# Construir el paquete de la aplicación, omitiendo los tests
RUN mvn package -DskipTests

# Etapa 2: Crear la imagen final y ligera
FROM openjdk:17-jdk-slim

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el archivo .jar construido desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto en el que corre la aplicación Spring Boot (usualmente 8080)
EXPOSE 8080

# El comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
