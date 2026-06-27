# Etapa de construcción
FROM eclipse-temurin:21-jdk-alpine AS build
COPY . .

# Damos permisos de ejecución al wrapper de Maven y compilamos
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Etapa final (más ligera)
FROM eclipse-temurin:21-jre-alpine
# Copiamos el jar generado en la etapa anterior
COPY --from=build /target/tacos-el-micro-api-0.0.1-SNAPSHOT.jar app.jar

# Ejecutamos la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]
