FROM maven:3.9.16-amazoncorretto-21 as build
COPY . .
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-jdk
COPY --from=build /target/demo-0.0.1-SNAPSHOT.jar demo.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "demo.jar"]

