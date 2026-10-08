FROM gcr.io/distroless/java25-debian13:nonroot@sha256:28a3986989d7d74cb5cfd6ba369e64d3d634f83ac9fa562dac8db2d20827f90e

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
