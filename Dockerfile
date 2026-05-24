FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

COPY src src

RUN ./mvnw -DskipTests package

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

RUN useradd --create-home --shell /usr/sbin/nologin appuser \
    && mkdir -p /app/uploads \
    && chown -R appuser:appuser /app

COPY --from=build /workspace/target/*.jar /app/app.jar

ENV SERVER_PORT=8080
ENV APP_UPLOAD_DIR=/app/uploads

EXPOSE 8080

USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]