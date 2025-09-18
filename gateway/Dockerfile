FROM eclipse-temurin:24-jdk
WORKDIR /app
COPY target/gateway-3.3.6.jar gateway.jar
ENTRYPOINT ["java","-jar","gateway.jar"]
