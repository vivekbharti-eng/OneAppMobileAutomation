#!/bin/bash

# Script to run Android tests
# Usage: 
#   ./run_android.sh                    (runs on local device)
#   ./run_android.sh local @smoke       (runs smoke tests locally)
#   ./run_android.sh browserstack       (runs on BrowserStack)
#   ./run_android.sh browserstack @smoke (runs smoke tests on BrowserStack)

echo "=========================================="
echo "Running Android Tests"
echo "=========================================="

EXECUTION_TYPE=${1:-local}
TAGS=$2

# Validate execution type
if [ "$EXECUTION_TYPE" == "local" ] || [ "$EXECUTION_TYPE" == "browserstack" ]; then
    echo "Execution Type: $EXECUTION_TYPE"
else
    # If first parameter looks like tags, use it as tags with local execution
    TAGS=$EXECUTION_TYPE
    EXECUTION_TYPE="local"
    echo "Execution Type: local (default)"
fi

if [ "$EXECUTION_TYPE" == "local" ]; then
    # Check if Appium is running
    if ! pgrep -f "appium" > /dev/null; then
        echo "Starting Appium server..."
        appium > appium.log 2>&1 &
        sleep 5
    fi

    # Check if device is connected
    if ! adb devices | grep -q "device$"; then
        echo "Error: No Android device/emulator connected"
        echo "Please connect a device or start an emulator"
        exit 1
    fi

    echo "Device connected:"
    adb devices
else
    echo "Using BrowserStack for execution"
    echo "Make sure BrowserStack credentials are configured"
fi

# Run tests based on profile
PROFILE="${EXECUTION_TYPE}-android"

if [ -z "$TAGS" ]; then
    echo "Running all Android tests on $EXECUTION_TYPE..."
    mvn clean test -P$PROFILE
else
    echo "Running Android tests with tags: $TAGS on $EXECUTION_TYPE..."
    mvn clean test -P$PROFILE -Dcucumber.filter.tags="$TAGS"
fi

echo "=========================================="
echo "Test execution completed!"
echo "Check reports at: target/reports/"
echo "=========================================="
