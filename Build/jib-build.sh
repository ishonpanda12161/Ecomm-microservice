#!/bin/bash

cd ..
cd ConfigServer && ./mvnw clean compile jib:build && cd ..
cd EurekaServer && ./mvnw clean compile jib:build && cd ..
cd Gateway && ./mvnw clean compile jib:build && cd ..
cd NotificationModule && ./mvnw clean compile jib:build && cd ..
cd OrderModule && ./mvnw clean compile jib:build && cd ..
cd ProductModule && ./mvnw clean compile jib:build && cd ..
cd UserModule && ./mvnw clean compile jib:build && cd ..
