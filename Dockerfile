FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

RUN sed -i 's/\r$//' ./gradlew && chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system barbearia && useradd --system --gid barbearia barbearia

COPY --from=build /app/build/libs/*.jar app.jar

USER barbearia

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
