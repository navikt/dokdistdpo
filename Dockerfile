FROM ghcr.io/navikt/baseimages/temurin:21

COPY app/target/app.jar /app/app.jar

COPY dokdistdpo-java-opts.sh /init-scripts/10-dokdistdpo-java-opts.sh
COPY export-vault-secrets.sh /init-scripts/20-export-vault-secrets.sh

USER root
# Brukes for å hente config fra json filer
RUN apt-get install -y --no-install-recommends jq
USER apprunner

ENV MAIN_CLASS="org.springframework.boot.loader.launch.JarLauncher"