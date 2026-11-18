FROM maven:3.9.16-amazoncorretto-21 as build
COPY . .
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-jdk
COPY --from=build /target/store-0.0.1-SNAPSHOT.jar store.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "store.jar"]

