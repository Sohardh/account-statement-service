# Account Statement Service [![build status](https://github.com/Sohardh/account-statement-service/actions/workflows/build.yml/badge.svg)](https://github.com/Sohardh/account-statement-service/actions/workflows/build.yml)

The service is responsible of the followings : 
1. Using Gmail API, download the HDFC statement.
2. Uses the hdfc-statement-parser to parse the statement into firefly 3 importer format.
3. Uploads the parsed data to firefly 3.

## Technologies Used
1. Spring boot
2. Gmail API
3. Spring Batch

## Building
To build with docker image
```shell
./gradlew build
```