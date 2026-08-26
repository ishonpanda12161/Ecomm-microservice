#!/bin/bash

cd ..
cd ConfigServer && ./mvnw compile jib:build && cd ..
cd EurekaServer && ./mvnw compile jib:build && cd ..
cd Gateway && ./mvnw compile jib:build && cd ..
cd NotificationModule && ./mvnw compile jib:build && cd ..
cd OrderModule && ./mvnw compile jib:build && cd ..
cd ProductModule && ./mvnw compile jib:build && cd ..
cd UserModule && ./mvnw compile jib:build && cd ..
