FROM gcr.io/distroless/java25-debian13:nonroot@sha256:8ac5525549d14b8ddd34562f9fc8e6ba4840c995363a1507b17cadf8bb7ab57e

ARG VEO_HISTORY_VERSION

LABEL org.opencontainers.image.title="verinice.veo history"
LABEL org.opencontainers.image.description="Provides archived revisions of veo resources."
LABEL org.opencontainers.image.ref.name=verinice.veo-history
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.licenses=AGPL-3.0
LABEL org.opencontainers.image.source=https://github.com/verinice/verinice-veo-history

USER nonroot

ENV JDK_JAVA_OPTIONS "-Djdk.serialFilter=maxbytes=0"

COPY --chown=nonroot:nonroot build/libs/veo-history-${VEO_HISTORY_VERSION}.jar /app/veo-history.jar

WORKDIR /app
EXPOSE 8084
CMD ["veo-history.jar"]
