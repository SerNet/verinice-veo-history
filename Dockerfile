FROM openjdk:11-jre-slim

RUN apt-get update && apt-get install -y curl=7.64.* && rm -rf /var/lib/apt/lists/*

LABEL org.opencontainers.image.title="vernice.veo history"
LABEL org.opencontainers.image.description="Provides archived revisions of veo resources."
LABEL org.opencontainers.image.ref.name=verinice.veo-history
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.licenses=LGPL-3.0
LABEL org.opencontainers.image.source=https://github.com/verinice/verinice-veo-history

COPY scripts/healthcheck /usr/local/bin/veo-healthcheck

RUN adduser --home /app --disabled-password --gecos '' veo
USER veo
WORKDIR /app

# If by accident we have more than one veo-history-*.jar docker will complain, which is what we want.
COPY build/libs/veo-history-*.jar veo-history.jar

HEALTHCHECK --start-period=15s CMD ["/usr/local/bin/veo-healthcheck"]
EXPOSE 8084
CMD ["java", "-jar", "veo-history.jar"]
