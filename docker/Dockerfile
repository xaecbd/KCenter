FROM adoptopenjdk/openjdk8:jre8u252-b09-alpine
LABEL author="Turman.P.Du"
ENV PROJECT_BASE_DIR /opt/app/kafka-center/
WORKDIR ${PROJECT_BASE_DIR}

COPY *.jar ${PROJECT_BASE_DIR}/
COPY *.sh ${PROJECT_BASE_DIR}/

RUN chmod +x *.sh
ENTRYPOINT ["sh","start.sh"]