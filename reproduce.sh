#!/bin/bash
PROJECT_ID=YOUR-PROJECT-ID

echo "checking gcloud command:"
which gcloud

if [ $? -ne 0 ]; then
	echo "can't find gcloud command. please check that it has been already installed and in your PATH."
	exit 255
fi

echo "checking mvn command:"
which mvn

if [ $? -ne 0 ]; then
	echo "can't find mvn command. please check that it has been already installed and in your PATH."
	exit 255
fi

# override pom.xml
sed -i "s/YOUR-PROJECT-ID/${PROJECT_ID}/g" ./pom.xml

# setting target GCP project
gcloud config set project "${PROJECT_ID}"
if [ $? -ne 0 ]; then
	echo "fail setting target project."
	exit 255
fi

# build project
mvn clean package
if [ $? -ne 0 ]; then
	echo "fail to build project."
	exit 255
fi

# make openapi.json
mvn endpoints-framework:openApiDocs
if [ $? -ne 0 ]; then
	echo "fail to make openapi.json."
	exit 255
fi

# deploy endpoint
gcloud endpoints services deploy target/openapi-docs/openapi.json --project=${PROJECT_ID}
if [ $? -ne 0 ]; then
	echo "fail to deploy endpoints."
	exit 255
fi

# deploy GAE
mvn appengine:deploy
if [ $? -ne 0 ]; then
	echo "fail to deploy GAE service."
	exit 255
fi

# create PubSub Topic
gcloud pubsub topics create my-topic --project=${PROJECT_ID}
if [ $? -ne 0 ]; then
	echo "fail to create topic."
	exit 255
fi

# create PubSub Subscription
gcloud pubsub subscriptions create my-sub --topic my-topic --project=${PROJECT_ID}
if [ $? -ne 0 ]; then
	echo "fail to create subscription."
	exit 255
fi

# call my endpoint
curl --header "Content-Type: application/json" --request POST --data '{"message":"hello world"}' https://${PROJECT_ID}.appspot.com/_ah/push-handler/echo/v1/echo

