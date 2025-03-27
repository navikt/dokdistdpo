FROM ghcr.io/navikt/baseimages/temurin:21

COPY app/target/app.jar /app/app.jar

ENV MAIN_CLASS="org.springframework.boot.loader.launch.JarLauncher"