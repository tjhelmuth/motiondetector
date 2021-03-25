FROM openjdk:16
COPY /build/libs/motiondetector-0.1.jar /usr/bin/motiondetector.jar
COPY config.yaml /usr/bin/config.yaml
WORKDIR /usr/bin
CMD ["java", "-jar", "motiondetector.jar"]