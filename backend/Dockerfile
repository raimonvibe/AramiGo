# Render has no native Java runtime — deploy this API as Docker.
# Multi-stage: build with Maven, run on a slim JRE.

FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:25-jre-alpine
# Latest CA bundle (eMudhra intermediate compromise, Oct 2026).
RUN apk add --no-cache --upgrade ca-certificates
# Distrust eMudhra (emSign) roots in Java's own trust store: their intermediate keys were
# compromised (Oct 2026) and Java ignores the OS ca-certificates bundle.
RUN keytool -list -v -cacerts -storepass changeit | sed -n 's/^Alias name: //p' | grep -i emsign \
    | while IFS= read -r a; do keytool -delete -cacerts -storepass changeit -alias "$a"; done \
    && ! keytool -list -cacerts -storepass changeit | grep -qi emsign
WORKDIR /app
COPY --from=build /app/target/aramigo-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
