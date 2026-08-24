#!/bin/bash

cd ..
cd ConfigServer && ./mvnw spring-boot:build-image -DskipTests && cd ..
cd EurekaServer && ./mvnw spring-boot:build-image -DskipTests && cd ..
cd Gateway && ./mvnw spring-boot:build-image -DskipTests && cd ..
cd NotificationModule && ./mvnw spring-boot:build-image -DskipTests && cd ..
cd OrderModule && ./mvnw spring-boot:build-image -DskipTests && cd ..
cd ProductModule && ./mvnw spring-boot:build-image -DskipTests && cd ..
cd UserModule && ./mvnw spring-boot:build-image -DskipTests && cd ..