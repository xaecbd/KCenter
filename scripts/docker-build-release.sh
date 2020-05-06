#!/usr/bin/env bash

DOCKER_IMAGE_NAME="xaecbd/kafka-center"

VERSION=${TRAVIS_TAG#v}

echo "KafkaCenter version: $VERSION"

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
cp $TRAVIS_BUILD_DIR/KafkaCenter-Core/target/*.jar $TRAVIS_BUILD_DIR/docker/
docker build  -t $DOCKER_IMAGE_NAME:$VERSION $TRAVIS_BUILD_DIR/docker/
docker images
docker push $DOCKER_IMAGE_NAME:$VERSION