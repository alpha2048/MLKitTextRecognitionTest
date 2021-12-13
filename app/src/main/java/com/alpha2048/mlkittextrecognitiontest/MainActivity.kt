package com.alpha2048.mlkittextrecognitiontest

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.modernstorage.filesystem.AndroidFileSystems

@RequiresApi(Build.VERSION_CODES.O)
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

            MaterialTheme {
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
                            }
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