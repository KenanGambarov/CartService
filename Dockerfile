FROM alpine:3.18.3
# For Java 17, try this
RUN apk add --no-cache openjdk17

# Refer to Maven build -> finalName
ARG JAR_FILE=build/libs/CartService-0.0.1-SNAPSHOT.jar

# cd /opt/app
WORKDIR /opt/app

# cp target/spring-boot-web.jar /opt/app/app.jar
COPY ${JAR_FILE} CartService-0.0.1.jar

# java -jar /opt/app/app.jar
ENTRYPOINT ["java","-jar","CartService-0.0.1.jar"]