# Native bullet requires focal instead of alpine
FROM eclipse-temurin:17-jre-focal
WORKDIR /home
COPY target/server-1.0.0-jar-with-dependencies.jar assets/ public.pem ./
RUN echo ./assets/ > assets.ini && \
    echo ./public.pem > public_auth_key.ini
ENTRYPOINT ["java", "-jar", "server-1.0.0-jar-with-dependencies.jar"]