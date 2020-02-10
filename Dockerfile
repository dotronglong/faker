FROM mcr.microsoft.com/java/jre-headless:8u192-zulu-alpine
LABEL maintainer="me@dotronglong.com"

ENV FAKER_VERSION=2.0.1
ENV FAKER_PORT=3030
ENV APP_DIR=/app
ENV APP_FILE=${APP_DIR}/faker.jar
ENV MOCK_DIR=${APP_DIR}/mocks
ENV LOG_FILE=${APP_DIR}/faker.log
ENV APP_RELEASE=https://github.com/dotronglong/faker/releases/download/v$FAKER_VERSION/faker.jar

RUN mkdir -p ${APP_DIR} ${MOCK_DIR}
ADD ${APP_RELEASE} ${APP_FILE}

EXPOSE 3030
ENTRYPOINT [ "java" ]
CMD [ "-Dserver.port=${FAKER_PORT}", "-Dfaker.source=${MOCK_DIR}", "-jar", "/app/faker.jar" ]