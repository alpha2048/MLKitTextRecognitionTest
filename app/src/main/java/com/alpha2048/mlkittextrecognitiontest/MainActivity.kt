package com.alpha2048.mlkittextrecognitiontest

import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.modernstorage.filesystem.AndroidFileSystems
import com.google.modernstorage.filesystem.AndroidPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.nio.file.Files
import java.nio.file.Path

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    private val actionOpenImageFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val path: Path = AndroidPaths.get(uri)

                    val inputStream = Files.newInputStream(path)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    val image = InputImage.fromBitmap(bitmap, 0)

                    recognizeText(image)
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidFileSystems.initialize(application)

        actionOpenImageFile.launch(arrayOf("image/*"))
    }

    private fun recognizeText(image: InputImage) {

        // [START get_detector_default]
        val recognizer = getTextRecognizer()


        // [END get_detector_default]

        // [START run_detector]
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                // [START_EXCLUDE]
                // [START get_text]

                visionText.textBlocks.forEach { block ->
                    val boundingBox = block.boundingBox
                    val cornerPoints = block.cornerPoints
                    val text = block.text

                    Timber.d("検出文字: $text")

//                    block.lines.forEach { line ->
//                        line.elements.forEach { element ->
//                        }
//                    }
                }

                // [END get_text]
                // [END_EXCLUDE]
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
            }
        // [END run_detector]
    }

    private fun processTextBlock(result: Text) {
        // [START mlkit_process_text_block]
        val resultText = result.text
        for (block in result.textBlocks) {
            val blockText = block.text
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            for (line in block.lines) {
                val lineText = line.text
                val lineCornerPoints = line.cornerPoints
                val lineFrame = line.boundingBox
                for (element in line.elements) {
                    val elementText = element.text
                    val elementCornerPoints = element.cornerPoints
                    val elementFrame = element.boundingBox
                }
            }
        }
        // [END mlkit_process_text_block]
    }

    private fun getTextRecognizer(): TextRecognizer {
        // [START mlkit_local_doc_recognizer]
        return TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
        // [END mlkit_local_doc_recognizer]
    }
}