FROM eclipse-temurin:17-jre-alpine
WORKDIR /home
COPY target/server-1.0.0-jar-with-dependencies.jar public.pem ./
# libstdc++ is needed for native bullet
RUN apk add libstdc++ && \
    echo ./assets/ > assets.ini && \
    echo ./public.pem > public_auth_key.ini
ENTRYPOINT ["java", "-jar", "server-1.0.0-jar-with-dependencies.jar"]