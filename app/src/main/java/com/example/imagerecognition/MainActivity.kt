package com.example.imagerecognition

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.imagerecognition.ml.Mobilenet
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private var bitmap: Bitmap? = null
    private lateinit var labels: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val button: Button = findViewById(R.id.button)
        imageView = findViewById(R.id.image)
        textView = findViewById(R.id.textview)

        // Load labels from the assets directory
        labels = loadLabels()


        imageView.setOnClickListener {
            val intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        val predict: Button = findViewById(R.id.prediction)
        predict.setOnClickListener {
            bitmap?.let {
                val resized: Bitmap = Bitmap.createScaledBitmap(it, 224, 224, true)
                val model = Mobilenet.newInstance(this)

                val tbuffer = TensorImage.fromBitmap(resized)
                val byteBuffer = tbuffer.buffer

                // Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)
                inputFeature0.loadBuffer(byteBuffer)

                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                val probabilities = outputFeature0.floatArray

                // Find the index of the maximum probability
                val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

                if (maxIndex != -1) {
                    textView.text = labels[maxIndex]
                } else {
                    textView.text = "Prediction failed"
                }

                // Releases model resources if no longer used.
                model.close()
            } ?: run {
                textView.text = "Please select an image first"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            uri?.let {
                imageView.setImageURI(uri)
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            }
        }
    }

    private fun loadLabels(): List<String> {
        val labels = mutableListOf<String>()
        val reader = BufferedReader(InputStreamReader(assets.open("labels.txt")))
        var line: String? = reader.readLine()
        while (line != null) {
            labels.add(line)
            line = reader.readLine()
        }
        reader.close()
        return labels
    }
}


//package com.example.imagerecognition
//
//import android.content.Intent
//import android.graphics.Bitmap
//import android.os.Bundle
//import android.provider.MediaStore
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.example.imagerecognition.ml.Mobilenet
//import org.tensorflow.lite.DataType
//import org.tensorflow.lite.support.image.TensorImage
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
//
//class MainActivity<uri> : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        val button : Button = findViewById(R.id.button)
//        val imageView: ImageView= findViewById(R.id.image)
//        var textView : TextView = findViewById(R.id.textview)
//        button.setOnClickListener {
//            var intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "image/*"
//            startActivityForResult(intent,100)
//
//        }
//        var predict: Button = findViewById(R.id.prediction)
//        predict.setOnClickListener {
//            var resized: Bitmap = Bitmap.createScaledBitmap( bitmap, 224, 224, true)
//            val model = Mobilenet.newInstance(this)
//
//            var tbuffer = TensorImage.fromBitmap(resized)
//            var byteBuffer = tbuffer.buffer
//
//// Creates inputs for reference.
//            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)
//            inputFeature0.loadBuffer(byteBuffer)
//
//// Runs model inference and gets result.
//            val outputs = model.process(inputFeature0)
//            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//            textView.setText(outputFeature0.floatArray[10].toString())
//
//// Releases model resources if no longer used.
//            model.close()
//
//        }
//
//        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//            super.onActivityResult(requestCode, resultCode, data)
//            imageView.setImageURI(data?.data)
//            var uri:uri? = data?.data
//            var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
//
//        }
//
//
//    }
//}