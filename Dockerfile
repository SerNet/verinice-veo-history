FROM gcr.io/distroless/java25-debian13:nonroot@sha256:bb5fd2c016691aef1cf9ff376294c1eeaa5b635af240a0a3ed3242e2877f7866

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
