FROM amazoncorretto:17

# TODO: build here
RUN mkdir /app && chown nobody /app
USER nobody
WORKDIR /app
COPY target/scala-3.3.1/rss2discord-bis-assembly-0.1.0-SNAPSHOT.jar /app/main.jar

CMD ["java", "-jar", "/app/main.jar"]
