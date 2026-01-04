#!/bin/bash

APP_NAME="evoshot"
APP_DIR="/home/ubuntu/app"
JAR_FILE="$APP_DIR/EvoShot-1.0-SNAPSHOT-all.jar"
LOG_FILE="$APP_DIR/app.log"

echo "Stopping existing application..."
pkill -f "java.*EvoShot" || true
sleep 2

echo "Starting application..."
cd $APP_DIR
nohup java -jar $JAR_FILE > $LOG_FILE 2>&1 &

sleep 3

if pgrep -f "java.*EvoShot" > /dev/null; then
    echo "Application started successfully!"
    echo "PID: $(pgrep -f 'java.*EvoShot')"
else
    echo "Failed to start application. Check logs at $LOG_FILE"
    exit 1
fi

