#!/usr/bin/env bash
# Cloud Agent install script for the Qualtive Android client library.
# Installs the Android SDK (if missing) and primes Gradle dependencies.
# Must be idempotent: it may run repeatedly against cached state.
set -euo pipefail

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
CMDLINE_TOOLS_VERSION="11076708"
PLATFORM="platforms;android-37.0"
BUILD_TOOLS="build-tools;37.0.0"

mkdir -p "$ANDROID_SDK_ROOT"

# Install command-line tools if the sdkmanager is not already present.
if [[ ! -x "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]]; then
  tmp_zip="$(mktemp --suffix=.zip)"
  curl -fsSL \
    "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" \
    -o "$tmp_zip"
  mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
  rm -rf "$ANDROID_SDK_ROOT/cmdline-tools/latest"
  unzip -q -o "$tmp_zip" -d "$ANDROID_SDK_ROOT/cmdline-tools"
  mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"
  rm -f "$tmp_zip"
fi

SDKMANAGER="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"

yes | "$SDKMANAGER" --licenses >/dev/null 2>&1 || true
"$SDKMANAGER" "platform-tools" "$PLATFORM" "$BUILD_TOOLS"

# Point Gradle at the SDK without relying on shell-profile env vars.
echo "sdk.dir=$ANDROID_SDK_ROOT" > "$PWD/local.properties"

# Prime the Gradle dependency cache so agent builds start warm.
export ANDROID_HOME="$ANDROID_SDK_ROOT"
./gradlew --no-daemon :qualtive:dependencies :demo:dependencies >/dev/null
