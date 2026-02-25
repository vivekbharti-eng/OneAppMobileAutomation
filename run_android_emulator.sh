#!/bin/bash
# ==============================================
# Run tests on Android Emulator
# ==============================================

echo ""
echo "================================================"
echo "Starting tests on Android EMULATOR"
echo "================================================"
echo ""

# Run tests with local-android-emulator profile
mvn clean test -Plocal-android-emulator

echo ""
echo "================================================"
echo "Test execution completed"
echo "Check reports at: target/reports"
echo "================================================"
