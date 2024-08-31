FROM openjdk:21-jdk-slim as build

# Install necessary packages for Chrome
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    unzip \
    fonts-liberation \
    libasound2 \
    libatk-bridge2.0-0 \
    libatk1.0-0 \
    libgconf-2-4 \
    libgtk-3-0 \
    libnspr4 \
    libnss3 \
    xdg-utils \
    libx11-xcb1 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm-dev \
    libpango1.0-0 \
    libcups2 \
    libdrm2 \
    libxshmfence1 \
    libnss3-dev

VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]