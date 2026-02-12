FROM maven:3.9.12-amazoncorretto-25 AS build

WORKDIR /app
ENV JENKINS_HOME=/app/work

EXPOSE 8080

CMD ["sh", "-c", "while sleep 3600; do :; done"]
