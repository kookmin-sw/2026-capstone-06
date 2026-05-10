FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# plain jar를 제외하고 실행 가능한 jar만 복사하도록 수정
COPY build/libs/*[^plain].jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]