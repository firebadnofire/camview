package org.archuser.camview

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CamviewScreen()
                }
            }
        }
    }
}

@Composable
private fun CamviewScreen() {
    var urlInput by rememberSaveable { mutableStateOf("") }
    var playbackUrl by rememberSaveable { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Enter RTSP URL",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("rtsp://...") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
                onGo = {
                    playbackUrl = urlInput.takeIf { it.isNotBlank() }
                    focusManager.clearFocus()
                }
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                playbackUrl = urlInput.takeIf { it.isNotBlank() }
                focusManager.clearFocus()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Play")
        }
        Spacer(modifier = Modifier.height(16.dp))
        VideoPlayer(
            url = playbackUrl,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun VideoPlayer(
    url: String?,
    modifier: Modifier = Modifier
) {
    val targetUrl = remember(url) { url }

    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                setMediaController(MediaController(context).apply {
                    setAnchorView(this@apply)
                })
            }
        },
        modifier = modifier,
        update = { videoView ->
            if (targetUrl.isNullOrBlank()) {
                videoView.stopPlayback()
            } else {
                val uri = Uri.parse(targetUrl)
                videoView.setVideoURI(uri)
                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    videoView.start()
                }
                videoView.setOnErrorListener { _, _, _ -> false }
            }
        }
    )
}
