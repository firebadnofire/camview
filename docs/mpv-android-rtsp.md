# mpv-android RTSP playback implementation notes

## Entry points for RTSP URLs
- The Android manifest registers `rtsp` (alongside `rtmp`, `rtp`, `mms`, etc.) in the `ACTION_VIEW` intent filter so other apps can hand RTSP links directly to mpv-android:

  ```xml
  <intent-filter> <!-- Media protocols -->
      <action android:name="android.intent.action.VIEW" />
      <category android:name="android.intent.category.DEFAULT" />
      <category android:name="android.intent.category.BROWSABLE" />
      <data android:scheme="rtmp" />
      <data android:scheme="rtmps" />
      <data android:scheme="rtp" />
      <data android:scheme="rtsp" />
      <data android:scheme="mms" />
      <data android:scheme="mmst" />
      <data android:scheme="mmsh" />
      <data android:scheme="tcp" />
      <data android:scheme="udp" />
  </intent-filter>
  ```

- When the player activity receives an intent, `MPVActivity.resolveUri` treats `rtsp` as a known network scheme and returns the URI string unchanged so it can be passed straight to mpv/libmpv:

  ```kotlin
  private fun resolveUri(data: Uri): String? {
      val filepath = when (data.scheme) {
          "file" -> data.path
          "content" -> translateContentUri(data)
          "data" -> "data://${data.schemeSpecificPart}"
          "http", "https", "rtmp", "rtmps", "rtp", "rtsp", "mms", "mmst", "mmsh",
          "tcp", "udp", "lavf", "ftp"
          -> data.toString()
          else -> null
      }
      ...
  }
  ```

- The utility `PROTOCOLS` set includes `rtsp`, allowing the file picker and content resolvers to accept RTSP links in the same way as other network protocols:

  ```kotlin
  val PROTOCOLS = setOf(
      "file", "content", "http", "https", "data", "ftp",
      "rtmp", "rtmps", "rtp", "rtsp", "mms", "mmst", "mmsh", "tcp", "udp", "lavf"
  )
  ```

## Playback handling
- There is no RTSP-specific playback code in the Android layer. After intent parsing, the URI is handed off to libmpv/FFmpeg, which provides the actual RTSP implementation (transport handling, authentication, etc.).
