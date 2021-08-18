FROM openjdk:11-jre-slim

ARG VEO_HISTORY_VERSION

LABEL org.opencontainers.image.title="verinice.veo history"
LABEL org.opencontainers.image.description="Provides archived revisions of veo resources."
LABEL org.opencontainers.image.ref.name=verinice.veo-history
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.licenses=AGPL-3.0
LABEL org.opencontainers.image.source=https://github.com/verinice/verinice-veo-history

RUN adduser --home /app --disabled-password --gecos '' veo
USER veo
WORKDIR /app

COPY build/libs/veo-history-${VEO_HISTORY_VERSION}.jar veo-history.jar

EXPOSE 8084
CMD ["java", "-jar", "veo-history.jar"]
