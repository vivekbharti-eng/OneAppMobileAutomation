#!/bin/bash
# ==============================================
# Run tests on Android Real Device
# ==============================================
# This script automatically detects connected Android real devices
# and runs tests using DeviceManager

echo ""
echo "================================================"
echo "Starting tests on Android REAL DEVICE"
echo "================================================"
echo ""

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo "ERROR: ADB not found in PATH"
    echo "Please ensure Android SDK is installed and added to PATH"
    exit 1
fi

# Check for connected devices
echo "Checking for connected devices..."
adb devices -l

# Run tests with local-android-real profile
echo ""
echo "Running tests..."
mvn clean test -Plocal-android-real

echo ""
echo "================================================"
echo "Test execution completed"
echo "Check reports at: target/reports"
echo "================================================"
