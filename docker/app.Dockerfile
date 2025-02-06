# Native bullet requires noble instead of alpine
FROM eclipse-temurin:22-jre-noble
WORKDIR /home
COPY target/server-1.0.0-jar-with-dependencies.jar public.pem ./
COPY assets assets
RUN echo -n ./assets/ > assets.ini && \
    echo -n ./public.pem > public_auth_key.ini
ENTRYPOINT ["java", "-jar", "server-1.0.0-jar-with-dependencies.jar"]