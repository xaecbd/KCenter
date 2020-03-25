FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
LABEL author="Turman.P.Du"
ENV PROJECT_BASE_DIR /opt/app/kafka-center/
WORKDIR ${PROJECT_BASE_DIR}

COPY *.jar ${PROJECT_BASE_DIR}/
COPY *.sh ${PROJECT_BASE_DIR}/

RUN chmod +x *.sh
ENTRYPOINT ["sh","start.sh"]