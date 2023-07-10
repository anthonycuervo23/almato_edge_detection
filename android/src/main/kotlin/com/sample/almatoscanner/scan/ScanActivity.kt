package com.sample.almatoscanner.scan

import androidx.core.content.ContextCompat
//
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle

import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.sample.almatoscanner.AlmatoScannerHandler
import com.sample.almatoscanner.R
import com.sample.almatoscanner.REQUEST_CODE
import com.sample.almatoscanner.SCANNED_RESULT
import com.sample.almatoscanner.base.BaseActivity
import com.sample.almatoscanner.view.PaperRectangle
import com.sample.almatoscanner.databinding.ActivityScanBinding
//import kotlinx.android.synthetic.main.activity_scan.*
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import java.io.*

class ScanActivity : BaseActivity(), IScanView.Proxy {

    private val REQUEST_CAMERA_PERMISSION = 0
    private val REQUEST_GALLERY_CODE = 2

    private lateinit var mPresenter: ScanPresenter

    private lateinit var initialBundle: Bundle

    private lateinit var binding: ActivityScanBinding

    override fun provideContentViewId(): Int = R.layout.activity_scan

    override fun initPresenter() {
        binding = ActivityScanBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        initialBundle = intent.getBundleExtra(AlmatoScannerHandler.INITIAL_BUNDLE) as Bundle
        mPresenter = ScanPresenter(this, this, initialBundle)
        prepare()
    }

    override fun prepare() {
        this.title = initialBundle.getString(AlmatoScannerHandler.SCAN_TITLE)



        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "loading opencv error, exit")
            finish()
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }

        // Utiliza las vistas con "binding" en lugar de las sintaxis kotlinx.android.synthetic
        binding.shut.setOnClickListener {
            if (mPresenter.canShut) {
                mPresenter.shut()
            }
        }

        binding.flash.visibility =
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && baseContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
            ) View.VISIBLE else View.GONE
        binding.flash.setOnClickListener {
            mPresenter.toggleFlash()
        }

        val initialBundle = intent.getBundleExtra(AlmatoScannerHandler.INITIAL_BUNDLE) as Bundle

        this.title = initialBundle.getString(AlmatoScannerHandler.SCAN_TITLE)

        binding.gallery.setOnClickListener {
            pickupFromGallery()
        }

            if (initialBundle.containsKey(AlmatoScannerHandler.FROM_GALLERY) && initialBundle.getBoolean(
                    AlmatoScannerHandler.FROM_GALLERY,
                    false
                )
            ) {

                pickupFromGallery()
            }

    }

    private fun pickupFromGallery() {
        mPresenter.stop()
        val gallery = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply{
            type="image/*"
        }
        ActivityCompat.startActivityForResult(this, gallery, REQUEST_GALLERY_CODE, null)
    }


    override fun onStart() {
        super.onStart()
        mPresenter.start()
    }

    override fun onStop() {
        super.onStop()
        mPresenter.stop()
    }

    override fun exit() {
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.count() == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMessage(R.string.camera_grant)
                mPresenter.initCamera()
                mPresenter.updateCamera()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun getCurrentDisplay(): Display? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.display
        } else {
            @Suppress("DEPRECATION")
            this.windowManager.defaultDisplay
        }
    }

    override fun getSurfaceView(): SurfaceView = binding.surface

    override fun getPaperRect(): PaperRectangle = binding.paperRect

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if(data != null && data.extras != null){
                    val path = data.extras!!.getString(SCANNED_RESULT)
                    setResult(Activity.RESULT_OK, Intent().putExtra(SCANNED_RESULT, path))
                    finish()
                }
            } else {
                if (intent.hasExtra(AlmatoScannerHandler.FROM_GALLERY) && intent.getBooleanExtra(
                        AlmatoScannerHandler.FROM_GALLERY, false
                    )
                )
                    finish()
            }
        }

        if (requestCode == REQUEST_GALLERY_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val uri: Uri = data!!.data!!
                onImageSelected(uri)
            } else {
                if (intent.hasExtra(AlmatoScannerHandler.FROM_GALLERY) && intent.getBooleanExtra(
                        AlmatoScannerHandler.FROM_GALLERY,
                        false
                    )
                )
                    finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onImageSelected(imageUri: Uri) {
        Log.i(TAG, "uri => $imageUri")
        val iStream: InputStream = contentResolver.openInputStream(imageUri)!!
        val exif = ExifInterface(iStream)
        var rotation = -1
        val orientation: Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotation = Core.ROTATE_90_CLOCKWISE
            ExifInterface.ORIENTATION_ROTATE_180 -> rotation = Core.ROTATE_180
            ExifInterface.ORIENTATION_ROTATE_270 -> rotation = Core.ROTATE_90_COUNTERCLOCKWISE
        }
        Log.i(TAG, "rotation:$rotation")

        var imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).toDouble()
        var imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).toDouble()
        if (rotation == Core.ROTATE_90_CLOCKWISE || rotation == Core.ROTATE_90_COUNTERCLOCKWISE) {
            imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).toDouble()
            imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).toDouble()
        }
        Log.i(TAG, "width:$imageWidth")
        Log.i(TAG, "height:$imageHeight")
        val inputData: ByteArray? = readBytesFromUri(contentResolver, imageUri)

        val mat = Mat(Size(imageWidth, imageHeight), CvType.CV_8U)
        mat.put(0, 0, inputData)
        val pic = Imgcodecs.imdecode(mat, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
        if (rotation > -1) Core.rotate(pic, pic, rotation)
        mat.release()

        mPresenter.detectEdge(pic)
    }

    @Throws(IOException::class)
    fun readBytesFromUri(contentResolver: ContentResolver, uri: Uri): ByteArray? {
        val inputStream = contentResolver.openInputStream(uri)
        return inputStream?.use {
            it.readBytes()
        }
    }

}
