FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/gateway-3.5.3.jar gateway.jar
ENTRYPOINT ["java","-jar","gateway.jar"]
