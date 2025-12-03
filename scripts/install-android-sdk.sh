#!/usr/bin/env bash
set -euo pipefail

ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT:-"$HOME/android-sdk"}
CLI_VERSION=${CLI_VERSION:-"11076708"}
CLI_BASE_URL="https://dl.google.com/android/repository"
CLI_ZIP="commandlinetools-linux-${CLI_VERSION}_latest.zip"

for cmd in curl unzip; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing dependency: $cmd" >&2
    exit 1
  fi
done

mkdir -p "$ANDROID_SDK_ROOT"
TEMP_ZIP=$(mktemp)

echo "Downloading Android command line tools to $TEMP_ZIP" >&2
curl -L "${CLI_BASE_URL}/${CLI_ZIP}" -o "$TEMP_ZIP"

echo "Unpacking command line tools" >&2
rm -rf "$ANDROID_SDK_ROOT/cmdline-tools"
mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
unzip -q "$TEMP_ZIP" -d "$ANDROID_SDK_ROOT/cmdline-tools"
rm "$TEMP_ZIP"

# Move into the expected 'latest' folder name for sdkmanager
if [ ! -d "$ANDROID_SDK_ROOT/cmdline-tools/latest" ]; then
  mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"
fi

SDKMANAGER="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"

if [ ! -x "$SDKMANAGER" ]; then
  echo "sdkmanager not found at $SDKMANAGER" >&2
  exit 1
fi

# Accept licenses automatically for CI/dev usage. Using `|| true` to ignore
# the broken pipe exit that `yes` may trigger once sdkmanager stops reading.
yes | "$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" --licenses >/dev/null || true

# Install commonly used components
"$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" \
  "platform-tools" \
  "platforms;android-34" \
  "platforms;android-33" \
  "build-tools;34.0.0"

cat <<INFO
Android SDK installed at: $ANDROID_SDK_ROOT
Add to PATH (example):
  export ANDROID_SDK_ROOT="$ANDROID_SDK_ROOT"
  export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
INFO
