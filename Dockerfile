FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# 빌드된 실행 가능한 jar만 복사
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]