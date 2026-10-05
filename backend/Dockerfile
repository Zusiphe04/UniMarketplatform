FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip \
    && rm -rf /var/lib/apt/lists/*
COPY . .
RUN chmod +x mvnw && ./mvnw -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system communitystore && useradd --system --gid communitystore communitystore \
    && mkdir -p /var/data/uploads && chown -R communitystore:communitystore /app /var/data/uploads
COPY --from=build /workspace/target/UniMarket-*.jar /app/community-store-api.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh
EXPOSE 10000
ENV PORT=10000
ENTRYPOINT ["/app/docker-entrypoint.sh"]
