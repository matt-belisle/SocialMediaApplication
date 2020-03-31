FROM openjdk:8-jre-alpine

COPY build/libs/SocialMedia-1.0-SNAPSHOT-all.jar /opt/apps/SocialMedia.jar
ENTRYPOINT ["java", "-jar", "/opt/apps/SocialMedia.jar"]
