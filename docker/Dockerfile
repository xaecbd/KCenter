FROM adoptopenjdk:x86_64-alpine-jre-11.0.4_11
LABEL author="Turman.P.Du"
ENV PROJECT_BASE_DIR /opt/app/kafka-center/
WORKDIR ${PROJECT_BASE_DIR}
COPY *.jar ${PROJECT_BASE_DIR}/
COPY *.sh ${PROJECT_BASE_DIR}/

RUN chmod +x *.sh
ENTRYPOINT ["bash","start.sh"]