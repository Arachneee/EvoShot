#!/bin/bash

APP_NAME="evoshot"
APP_DIR="/home/ubuntu/app"
JAR_FILE="$APP_DIR/EvoShot-1.0-SNAPSHOT-all.jar"
LOG_FILE="$APP_DIR/app.log"

setup_java_port_binding() {
    echo "Setting up Java to bind privileged ports..."

    JAVA_PATH=$(which java)
    if [ -z "$JAVA_PATH" ]; then
        echo "Java not found!"
        exit 1
    fi

    JAVA_REAL_PATH=$(readlink -f "$JAVA_PATH")
    echo "Java binary: $JAVA_REAL_PATH"

    sudo setcap 'cap_net_bind_service=+ep' "$JAVA_REAL_PATH"
    if [ $? -eq 0 ]; then
        echo "Successfully granted port binding capability to Java."
    else
        echo "Failed to set capability. Trying alternative method..."
        sudo apt-get update
        sudo apt-get install -y libcap2-bin
        sudo setcap 'cap_net_bind_service=+ep' "$JAVA_REAL_PATH"
    fi
}

setup_java_port_binding

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
