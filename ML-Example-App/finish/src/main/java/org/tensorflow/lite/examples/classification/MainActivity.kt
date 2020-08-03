/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.classification

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.classification.ml.FlowerModel
import org.tensorflow.lite.examples.classification.ui.RecognitionAdapter
import org.tensorflow.lite.examples.classification.util.YuvToRgbConverter
import org.tensorflow.lite.examples.classification.viewmodel.Recognition
import org.tensorflow.lite.examples.classification.viewmodel.RecognitionListViewModel
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import java.util.concurrent.Executors

// Constants
private const val MAX_RESULT_DISPLAY = 3 // Maximum number of results displayed
private const val TAG = "TFL Classify" // Name for logging
private const val REQUEST_CODE_PERMISSIONS = 999 // Return code after asking for permission
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA) // permission needed

// Listener for the result of the ImageAnalyzer
typealias RecognitionListener = (recognition: List<Recognition>) -> Unit

/**
 * Main entry point into TensorFlow Lite Classifier
 */
class MainActivity : AppCompatActivity() {

    // CameraX variables
    private lateinit var preview: Preview // Preview use case, fast, responsive view of the camera
    private lateinit var imageAnalyzer: ImageAnalysis // Analysis use case, for running ML code
    private lateinit var camera: Camera
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // Views attachment
    private val resultRecyclerView by lazy {
        findViewById<RecyclerView>(R.id.recognitionResults) // Display the result of analysis
    }
    private val viewFinder by lazy {
        findViewById<PreviewView>(R.id.viewFinder) // Display the preview image from Camera
    }

    // Contains the recognition result. Since  it is a viewModel, it will survive screen rotations
    private val recogViewModel: RecognitionListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Initialising the resultRecyclerView and its linked viewAdaptor
        val viewAdapter = RecognitionAdapter(this)
        resultRecyclerView.adapter = viewAdapter

        // Disable recycler view animation to reduce flickering, otherwise items can move, fade in
        // and out as the list change
        resultRecyclerView.itemAnimator = null

        // Attach an observer on the LiveData field of recognitionList
        // This will notify the recycler view to update every time when a new list is set on the
        // LiveData field of recognitionList.
        recogViewModel.recognitionList.observe(this,
            Observer {
                viewAdapter.submitList(it)
            }
        )

    }

    /**
     * Check all permissions are granted - use for Camera permission in this example.
     */
    private fun allPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * This gets called after the Camera permission pop up is shown.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // Exit the app if permission is not granted
                // Best practice is to explain and offer a chance to re-request but this is out of
                // scope in this sample. More details:
                // https://developer.android.com/training/permissions/usage-notes
                Toast.makeText(
                    this,
                    getString(R.string.permission_deny_text),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * Start the Camera which involves:
     *
     * 1. Initialising the preview use case
     * 2. Initialising the image analyser use case
     * 3. Attach both to the lifecycle of this activity
     * 4. Pipe the output of the preview object to the PreviewView on the screen
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()

            imageAnalyzer = ImageAnalysis.Builder()
                // This sets the ideal size for the image to be analyse, CameraX will choose the
                // the most suitable resolution which may not be exactly the same or hold the same
                // aspect ratio
                .setTargetResolution(Size(224, 224))
                // How the Image Analyser should pipe in input, 1. every frame but drop no frame, or
                // 2. go to the latest frame and may drop some frame. The default is 2.
                // STRATEGY_KEEP_ONLY_LATEST. The following line is optional, kept here for clarity
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysisUseCase: ImageAnalysis ->
                    analysisUseCase.setAnalyzer(cameraExecutor, ImageAnalyzer(this) { items ->
                        // updating the list of recognised objects
                        recogViewModel.updateData(items)
                    })
                }

            // Select camera, back is the default. If it is not available, choose front camera
            val cameraSelector =
                if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA))
                    CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera - try to bind everything at once and CameraX will find
                // the best combination.
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

                // Attach the preview to preview view, aka View Finder
                preview.setSurfaceProvider(viewFinder.createSurfaceProvider())
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private class ImageAnalyzer(ctx: Context, private val listener: RecognitionListener) :
        ImageAnalysis.Analyzer {

        // TODO 6. Optional GPU acceleration
        private val options = Model.Options.Builder().setDevice(Model.Device.GPU).build()

        // TODO 1: Add class variable TensorFlow Lite Model
        private val flowerModel = FlowerModel.newInstance(ctx, options)

        override fun analyze(imageProxy: ImageProxy) {

            val items = mutableListOf<Recognition>()

            // TODO 2: Convert Image to Bitmap then to TensorImage
            val tfImage = TensorImage.fromBitmap(toBitmap(imageProxy))

            // TODO 3: Process the image using the trained model, sort and pick out the top results
            val outputs = flowerModel.process(tfImage)
                .probabilityAsCategoryList.apply {
                    sortByDescending { it.score } // Sort with highest confidence first
                }.take(MAX_RESULT_DISPLAY) // take the top results

            // TODO 4: Converting the top probability items into a list of recognitions
            for (output in outputs) {
                items.add(Recognition(output.label, output.score))
            }

//            // START - Placeholder code at the start of the codelab. Comment this block of code out.
//            for (i in 0..MAX_RESULT_DISPLAY-1){
//                items.add(Recognition("Fake label $i", Random.nextFloat()))
//            }
//            // END - Placeholder code at the start of the codelab. Comment this block of code out.

            // Return the result
            listener(items.toList())

            // Close the image,this tells CameraX to feed the next image to the analyzer
            imageProxy.close()
        }

        /**
         * Convert Image Proxy to Bitmap
         */
        private val yuvToRgbConverter = YuvToRgbConverter(ctx)
        private lateinit var bitmapBuffer: Bitmap
        private lateinit var rotationMatrix: Matrix

        @SuppressLint("UnsafeExperimentalUsageError")
        private fun toBitmap(imageProxy: ImageProxy): Bitmap? {

            val image = imageProxy.image ?: return null

            // Initialise Buffer
            if (!::bitmapBuffer.isInitialized) {
                // The image rotation and RGB image buffer are initialized only once
                Log.d(TAG, "Initalise toBitmap()")
                rotationMatrix = Matrix()
                rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                bitmapBuffer = Bitmap.createBitmap(
                    imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
                )
            }

            // Pass image to an image analyser
            yuvToRgbConverter.yuvToRgb(image, bitmapBuffer)

            // Create the Bitmap in the correct orientation
            return Bitmap.createBitmap(
                bitmapBuffer,
                0,
                0,
                bitmapBuffer.width,
                bitmapBuffer.height,
                rotationMatrix,
                false
            )
        }

    }

}
