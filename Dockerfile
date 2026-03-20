FROM eclipse-temurin:25-jre
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]


#/root/app/db/postgresql/16/volume