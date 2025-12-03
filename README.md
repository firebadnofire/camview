# CamView

Android project for camera viewing and related utilities.

## Development setup

1. Ensure Java 17+ is available.
2. Fetch the Android SDK using the helper script:

```bash
./scripts/install-android-sdk.sh
```

The script downloads the official Android command line tools, installs common
platforms/build-tools, and accepts licenses for local development. By default,
the SDK is placed in `~/android-sdk`; override with `ANDROID_SDK_ROOT` if needed.

3. Add the SDK to your `PATH` (replace the path if you customized
   `ANDROID_SDK_ROOT`):

```bash
export ANDROID_SDK_ROOT="$HOME/android-sdk"
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
```

After the SDK is installed, you can build the project with:

```bash
./gradlew assembleDebug
```

## Usage

1. Launch the app and paste or type your RTSP URL (for example,
   `rtsp://camera-address/stream`).
2. Tap **Start stream** to begin playback. The player shows only the video to
   keep the interface focused on live viewing.
