package com.alpha2048.mlkittextrecognitiontest

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.modernstorage.filesystem.AndroidFileSystems

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val actionOpenImageFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { mainViewModel.recognizeFromImage(it) }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidFileSystems.initialize(application)

        setContent {
            val uiState by mainViewModel.state.collectAsState()

            MyThema {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(onClick = { actionOpenImageFile.launch(arrayOf("image/*")) }) {
                                        Text(text = "ここを押して調べる")
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                        )

                    },
                    content = {
                        when (uiState.state) {
                            is UiState.Loading -> LoadingLayout()
                            is UiState.Error -> ErrorLayout(text = "解析に失敗しました") {}
                            is UiState.Initial -> InitialLayout()
                            is UiState.Loaded -> {
                                LazyColumn {
                                    uiState.blocks.forEach { block ->
                                        item {
                                            Card(modifier = Modifier
                                                .padding(12.dp)
                                                .wrapContentSize()
                                            ) {
                                                Box(modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                                ) {
                                                    Text(text = block.text)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InitialLayout(
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(text = "ここに結果が表示されます。", modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun LoadingLayout(
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorLayout (
    text: String,
    onClickRetry: () -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = text, modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun MyThema(
    content: @Composable () -> Unit
) {
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val myColorScheme = when {
        dynamicColor -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        else -> MyLightColorScheme
    }

    MaterialTheme(
        colorScheme = myColorScheme
    ) {
        // TODO (M3): MaterialTheme doesn't provide LocalIndication, remove when it does
        val rippleIndication = rememberRipple()
        CompositionLocalProvider(
            LocalIndication provides rippleIndication,
            content = content
        )
    }
}

val MyLightColorScheme = lightColorScheme(
    primary = Color.Magenta,
    onPrimary = Color.White,
)