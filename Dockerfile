FROM adoptopenjdk/openjdk8:ubi

ARG JAR_FILE=*.jar

COPY target/${JAR_FILE} application.jar

ENTRYPOINT ["java", "-jar", "application.jar"]
