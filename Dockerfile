# ----------- 1. Build Stage (Maven + JDK 17) -----------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# copy pom.xml and pre-download dependencies (cache)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# copy source
COPY src ./src

# build project
RUN mvn -B clean package -DskipTests=true

# ----------- 2. Run Stage (JRE 17 only) -----------------------
FROM eclipse-temurin:17-jre
WORKDIR /app

# copy built jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
