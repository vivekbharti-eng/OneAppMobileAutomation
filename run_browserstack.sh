#!/bin/bash
# ==============================================
# Run tests on BrowserStack
# ==============================================
# Usage: ./run_browserstack.sh [android|ios]

PLATFORM=${1:-android}

echo ""
echo "================================================"
echo "Starting tests on BrowserStack - $PLATFORM"
echo "================================================"
echo ""

# Validate BrowserStack credentials
if [ -z "$BROWSERSTACK_USERNAME" ]; then
    echo "WARNING: BROWSERSTACK_USERNAME not set"
    echo "Set environment variables or configure in config.properties"
    echo ""
fi

if [ -z "$BROWSERSTACK_ACCESS_KEY" ]; then
    echo "WARNING: BROWSERSTACK_ACCESS_KEY not set"
    echo "Set environment variables or configure in config.properties"
    echo ""
fi

# Run tests with BrowserStack profile
if [ "$PLATFORM" == "android" ]; then
    mvn clean test -Pbs-android
elif [ "$PLATFORM" == "ios" ]; then
    mvn clean test -Pbs-ios
else
    echo "Invalid platform: $PLATFORM"
    echo "Usage: ./run_browserstack.sh [android|ios]"
    exit 1
fi

echo ""
echo "================================================"
echo "Test execution completed"
echo "Check reports at: target/reports"
echo "================================================"
