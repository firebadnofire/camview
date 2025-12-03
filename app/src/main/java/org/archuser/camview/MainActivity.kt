package org.archuser.camview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import org.archuser.camview.ui.theme.CamviewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CamviewTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RtspViewer()
                }
            }
        }
    }
}

@Composable
private fun RtspViewer() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var inputUrl by rememberSaveable { mutableStateOf("") }
    var currentStreamUrl by remember { mutableStateOf<String?>(null) }

    val player = remember {
        ExoPlayer.Builder(context)
            .build()
    }

    DisposableEffect(lifecycleOwner, player) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                player.pause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                player.release()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    LaunchedEffect(currentStreamUrl) {
        val uri = currentStreamUrl
        if (uri.isNullOrBlank()) {
            player.stop()
        } else {
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            player.playWhenReady = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter RTSP URL",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = inputUrl,
            onValueChange = { inputUrl = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("rtsp://camera-address/stream") }
        )

        Button(
            onClick = { currentStreamUrl = inputUrl.trim().ifEmpty { null } },
            enabled = inputUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start stream")
        }

        if (!currentStreamUrl.isNullOrBlank()) {
            Text(
                text = "Streaming: ${currentStreamUrl}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            PlayerSurface(player = player)
        }
    }
}

@Composable
private fun PlayerSurface(player: ExoPlayer) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                this.player = player
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(240.dp),
        update = { view -> view.player = player }
    )
}
