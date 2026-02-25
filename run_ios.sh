#!/bin/bash

# Script to run iOS tests (Mac only for local, cross-platform for BrowserStack)
# Usage: 
#   ./run_ios.sh                    (runs on local simulator)
#   ./run_ios.sh local @smoke       (runs smoke tests locally)
#   ./run_ios.sh browserstack       (runs on BrowserStack)
#   ./run_ios.sh browserstack @smoke (runs smoke tests on BrowserStack)

echo "=========================================="
echo "Running iOS Tests"
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
    # Check if running on Mac (only for local execution)
    if [[ "$OSTYPE" != "darwin"* ]]; then
        echo "Error: Local iOS testing is only supported on macOS"
        echo "For cross-platform testing, use BrowserStack:"
        echo "  ./run_ios.sh browserstack"
        exit 1
    fi

    # Check if Appium is running
    if ! pgrep -f "appium" > /dev/null; then
        echo "Starting Appium server..."
        appium > appium.log 2>&1 &
        sleep 5
    fi

    # Check if simulator is available
    if ! xcrun simctl list devices | grep -q "Booted"; then
        echo "No iOS simulator is running. Please start a simulator."
        echo "Available simulators:"
        xcrun simctl list devices available
        exit 1
    fi

    echo "iOS Simulator detected"
else
    echo "Using BrowserStack for execution"
    echo "Make sure BrowserStack credentials are configured"
fi

# Run tests based on profile
PROFILE="${EXECUTION_TYPE}-ios"

if [ -z "$TAGS" ]; then
    echo "Running all iOS tests on $EXECUTION_TYPE..."
    mvn clean test -P$PROFILE
else
    echo "Running iOS tests with tags: $TAGS on $EXECUTION_TYPE..."
    mvn clean test -P$PROFILE -Dcucumber.filter.tags="$TAGS"
fi

echo "=========================================="
echo "Test execution completed!"
echo "Check reports at: target/reports/"
echo "=========================================="
