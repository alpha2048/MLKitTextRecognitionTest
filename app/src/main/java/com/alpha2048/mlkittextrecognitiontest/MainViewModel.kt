package com.alpha2048.mlkittextrecognitiontest

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.modernstorage.filesystem.AndroidPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    object Loaded : UiState()
    data class Error(val e: Exception): UiState()
}

data class HomeUiState(
    val blocks: List<Text.TextBlock> = listOf(),
    val state: UiState = UiState.Initial,
)

@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel: ViewModel() {

    private val mutableState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState

    private val textRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    fun recognizeFromImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableState.emit(
                HomeUiState(state = UiState.Loading)
            )

            val path: Path = AndroidPaths.get(uri)

            val inputStream = Files.newInputStream(path)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val image = InputImage.fromBitmap(bitmap, 0)

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    viewModelScope.launch(Dispatchers.IO) {
                        mutableState.emit(
                            HomeUiState(
                                state = UiState.Loaded,
                                blocks = visionText.textBlocks
                            )
                        )
                    }

                }
                .addOnFailureListener { e ->
                    viewModelScope.launch(Dispatchers.IO) {
                        mutableState.emit(
                            HomeUiState(state = UiState.Error(e))
                        )
                    }
                }
        }

    }

}