FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle* ./

RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar \
    --no-daemon \
    --no-parallel \
    -Dorg.gradle.workers.max=2 \
    -Dorg.gradle.jvmargs="-Xmx1200m -XX:MaxMetaspaceSize=384m"

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]