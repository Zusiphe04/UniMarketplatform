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
COPY --from=build /workspace/target/UniMarket-0.0.1-SNAPSHOT.jar /app/community-store-api.jar
USER communitystore
EXPOSE 10000
ENV PORT=10000
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar /app/community-store-api.jar"]
