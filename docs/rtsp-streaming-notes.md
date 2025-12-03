# RTSP streaming reference and plan

## How mpv-android handles RTSP URLs
- The `MPVActivity` intent handler resolves incoming `ACTION_VIEW` or `ACTION_SEND` URIs by whitelisting network schemes and passing them directly to the mpv player. RTSP is treated just like HTTP/HTTPS/RTMP by returning the URI string unchanged, so the underlying libmpv/FFmpeg stack performs the actual streaming.
- Incoming RTSP links arrive either via the intent data or via shared text. If the URI is hierarchical and absolute, mpv-android forwards it to the player without additional preprocessing.

```kotlin
// Simplified from MPVActivity.resolveUri
data.scheme -> when {
    "file" -> data.path
    "content" -> translateContentUri(data)
    "http", "https", "rtmp", "rtmps", "rtp", "rtsp", "mms", "mmst", "mmsh", "tcp", "udp", "lavf", "ftp" -> data.toString()
    else -> null
}
```

## Takeaways for CamView
- We do not need protocol-specific client code for RTSP if we rely on a media engine (mpv/FFmpeg or ExoPlayer RTSP) that already implements the transport; our job is to accept and forward RTSP URIs intact.
- Intent parsing should recognize RTSP alongside other network protocols, mirroring mpv-android’s permissive URI handling to support file-picker and share targets.
- Error feedback and basic controls (play/pause, reconnect) should surround the player since RTSP camera streams are often brittle compared with local files.

## Proposed path forward
1. **Choose and wire a playback engine with RTSP support.** Start with ExoPlayer plus the RTSP extension for a lightweight dependency, while keeping the option to embed libmpv later if we need broader format coverage.
2. **Implement URI intake and validation.** Add UI for manual RTSP URL entry and support for `ACTION_VIEW`/`ACTION_SEND` intents that accept `rtsp://` links and forward them to the player unmodified.
3. **Build a minimal Compose viewer screen.** Use `AndroidView` hosting a `PlayerView` (or mpv surface if chosen) with play/pause, reconnect, and basic status/error messaging tailored to live camera streams.
4. **Handle lifecycle and network stability.** Keep the player in foreground-only mode initially, reconnect on foreground resume, and surface connection failures so users can retry quickly.
5. **Add logging and troubleshooting hooks.** Capture player errors and present a simple diagnostics view (last error, URI, timestamps) to help debug camera connectivity.
