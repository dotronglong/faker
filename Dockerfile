FROM mcr.microsoft.com/java/jre-headless:17-zulu-alpine

ARG APP_VERSION=3.0.0
ENV FAKER_PORT=3030
ENV APP_DIR=/app
ENV APP_FILE=${APP_DIR}/faker.jar
ENV MOCK_DIR=${APP_DIR}/mocks
ENV LOG_FILE=${APP_DIR}/faker.log
ENV APP_RELEASE=https://github.com/dotronglong/faker/releases/download/v${APP_VERSION}/faker.jar

RUN mkdir -p ${APP_DIR} ${MOCK_DIR}
ADD ${APP_RELEASE} ${APP_FILE}

EXPOSE 3030
ENTRYPOINT [ "java" ]
CMD [ "-Dserver.port=${FAKER_PORT}", "-Dfaker.source=${MOCK_DIR}", "-jar", "/app/faker.jar" ]
