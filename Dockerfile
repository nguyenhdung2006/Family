FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S hometree && adduser -S hometree -G hometree
COPY --from=build /workspace/target/*.jar app.jar
USER hometree
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
