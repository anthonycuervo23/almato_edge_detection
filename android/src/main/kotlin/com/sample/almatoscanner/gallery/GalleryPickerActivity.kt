package com.sample.almatoscanner.gallery

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sample.almatoscanner.AlmatoScannerHandler
import com.sample.almatoscanner.REQUEST_CODE
import com.sample.almatoscanner.SCANNED_RESULT
import com.sample.almatoscanner.SourceManager
import com.sample.almatoscanner.base.BaseActivity
import com.sample.almatoscanner.crop.CropActivity
import com.sample.almatoscanner.processor.processPicture
import com.sample.almatoscanner.scan.ScanPresenter
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.IOException
import java.io.InputStream

class GalleryPickerActivity : BaseActivity() {

    private lateinit var mPresenter: ScanPresenter;
    private val REQUEST_STORAGE_PERMISSION = 0
    private lateinit var initialBundle: Bundle
    private val REQUEST_GALLERY_CODE = 1;

    override fun provideContentViewId(): Int = 123;

    override fun initPresenter() {
        supportActionBar?.hide();
        initialBundle = intent.getBundleExtra(AlmatoScannerHandler.INITIAL_BUNDLE) as Bundle;
        mPresenter = ScanPresenter(this, null, initialBundle)
    }

    override fun prepare() {
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "loading opencv error, exit")
            finish()
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_STORAGE_PERMISSION
            )
        } else if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        }else{
            pickupFromGallery();
        }




        val initialBundle = intent.getBundleExtra(AlmatoScannerHandler.INITIAL_BUNDLE) as Bundle;

        this.title = initialBundle.getString(AlmatoScannerHandler.SCAN_TITLE) as? String


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        var allGranted = false
        var indexPermission = -1

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.count() == 1) {
                if (permissions.indexOf(android.Manifest.permission.CAMERA) >= 0) {
                    indexPermission = permissions.indexOf(android.Manifest.permission.CAMERA)
                }
                if (permissions.indexOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) >= 0) {
                    indexPermission =
                        permissions.indexOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                if (indexPermission >= 0 && grantResults[indexPermission] == PackageManager.PERMISSION_GRANTED) {
                    allGranted = true

                }
            }
        }

        if (allGranted) {
            pickupFromGallery();
        }else{
            Toast.makeText(this, "Permissions are required to access the gallery", Toast.LENGTH_LONG).show();
            finish();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    fun pickupFromGallery() {
        val gallery = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply{
            type="image/*"
        }
        ActivityCompat.startActivityForResult(this, gallery, 1, null);
    }

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
                 finish()
            }
        }
        if (requestCode == 1) {
            Log.i(TAG, "request code $requestCode")
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "request code OK")
                val uri: Uri = data!!.data!!
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    onImageSelected(uri)
                }
            } else {
                finish()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun onImageSelected(imageUri: Uri) {
        Log.i(TAG, "uri => $imageUri")
        val iStream: InputStream = contentResolver.openInputStream(imageUri)!!
        val exif = ExifInterface(iStream);
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
        Log.i(TAG, "rotation:" + rotation)

        var imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).toDouble()
        var imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).toDouble()
        if (rotation == Core.ROTATE_90_CLOCKWISE || rotation == Core.ROTATE_90_COUNTERCLOCKWISE) {
            imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).toDouble()
            imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).toDouble()
        }
        Log.i(TAG, "width:" + imageWidth)
        Log.i(TAG, "height:" + imageHeight)

        val inputData: ByteArray? = readBytesFromUri(contentResolver, imageUri)

        val mat = Mat(Size(imageWidth, imageHeight), CvType.CV_8U)
        mat.put(0, 0, inputData)
        val pic = Imgcodecs.imdecode(mat, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
        if (rotation > -1) Core.rotate(pic, pic, rotation)
        mat.release()
        SourceManager.corners = processPicture(pic)
        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_RGB2BGRA)
        SourceManager.pic = pic

        val cropIntent = Intent(this, CropActivity::class.java);
        cropIntent.putExtra(AlmatoScannerHandler.INITIAL_BUNDLE, this.initialBundle)
        ActivityCompat.startActivityForResult(this, cropIntent, REQUEST_CODE, null);
//        mPresenter.detectEdge(pic);
//        finish();
    }

    @Throws(IOException::class)
    fun readBytesFromUri(contentResolver: ContentResolver, uri: Uri): ByteArray? {
        val inputStream = contentResolver.openInputStream(uri)
        return inputStream?.use {
            it.readBytes()
        }
    }
}