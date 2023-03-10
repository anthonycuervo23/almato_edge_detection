package com.sample.almatoscanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sample.almatoscanner.scan.ScanActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

class AlmatoScannerPlugin : FlutterPlugin, ActivityAware {
    private var handler: AlmatoScannerHandler? = null

    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        handler = AlmatoScannerHandler()
        val channel = MethodChannel(
            binding.binaryMessenger, "almato_scanner"
        )
        channel.setMethodCallHandler(handler)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {}

    override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
        handler?.setActivityPluginBinding(activityPluginBinding)
    }

    override fun onDetachedFromActivityForConfigChanges() {}
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}
    override fun onDetachedFromActivity() {}
}

class AlmatoScannerHandler : MethodCallHandler, PluginRegistry.ActivityResultListener {
    private var activityPluginBinding: ActivityPluginBinding? = null
    private var result: Result? = null
    private var methodCall: MethodCall? = null

    companion object {
        public const val INITIAL_BUNDLE = "initial_bundle"
        public const val FROM_GALLERY = "from_gallery"
//        public const val SAVE_TO = "save_to"
//        public const val CAN_USE_GALLERY = "can_use_gallery"
        public const val SCAN_TITLE = "scan_title"
        public const val CROP_TITLE = "crop_title"
        public const val CROP_BLACK_WHITE_TITLE = "crop_black_white_title"
        public const val CROP_RESET_TITLE = "crop_reset_title"
        public const val CROP_MAGIC_TITLE = "crop_magic_title"
        public const val CROP_HPF_TITLE = "crop_hpf_title"
    }

    fun setActivityPluginBinding(activityPluginBinding: ActivityPluginBinding) {
        activityPluginBinding.addActivityResultListener(this)
        this.activityPluginBinding = activityPluginBinding
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when {
            getActivity() == null -> {
                result.error(
                    "no_activity",
                    "almato_scanner plugin requires a foreground activity.",
                    null
                )
                return
            }
            call.method.equals("edge_detect") -> {
                openCameraActivity(call, result)
            }
            call.method.equals("edge_detect_gallery") -> {
                openGalleryActivity(call, result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun getActivity(): Activity? {
        return activityPluginBinding?.activity
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if(data != null && data.extras != null){
                    val filePath = data.extras!!.getString(SCANNED_RESULT)
//                    finishWithSuccess(true)
                    finishWithSuccess(filePath)
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
//                finishWithSuccess(false)
                finishWithSuccess(null)
            }
            return true
        }
        return false
    }

    private fun openCameraActivity(call: MethodCall, result: Result) {
        if (!setPendingMethodCallAndResult(call, result)) {
            finishWithAlreadyActiveError()
            return
        }

        val initialIntent =
            Intent(Intent(getActivity()?.applicationContext, ScanActivity::class.java))
        val bundle = Bundle();
//        bundle.putString(SAVE_TO, call.argument<String>(SAVE_TO) as String)
        bundle.putString(SCAN_TITLE, call.argument<String>(SCAN_TITLE) as String)
        bundle.putString(CROP_TITLE, call.argument<String>(CROP_TITLE) as String)
        bundle.putString(
            CROP_BLACK_WHITE_TITLE,
            call.argument<String>(CROP_BLACK_WHITE_TITLE) as String
        )
        bundle.putString(
            CROP_MAGIC_TITLE,
            call.argument<String>(CROP_MAGIC_TITLE) as String
        )
        bundle.putString(
            CROP_HPF_TITLE,
            call.argument<String>(CROP_HPF_TITLE) as String
        )
        bundle.putString(CROP_RESET_TITLE, call.argument<String>(CROP_RESET_TITLE) as String)
//        bundle.putBoolean(CAN_USE_GALLERY, call.argument<Boolean>(CAN_USE_GALLERY) as Boolean)
        initialIntent.putExtra(INITIAL_BUNDLE, bundle)
        getActivity()?.startActivityForResult(initialIntent, REQUEST_CODE)
    }

    private fun openGalleryActivity(call: MethodCall, result: Result) {
        if (!setPendingMethodCallAndResult(call, result)) {
            finishWithAlreadyActiveError()
            return
        }
        val initialIntent =
            Intent(Intent(getActivity()?.applicationContext, ScanActivity::class.java))
        val bundle = Bundle();
//        bundle.putString(SAVE_TO, call.argument<String>(SAVE_TO) as String)
        bundle.putString(CROP_TITLE, call.argument<String>(CROP_TITLE) as String)
        bundle.putString(
            CROP_BLACK_WHITE_TITLE,
            call.argument<String>(CROP_BLACK_WHITE_TITLE) as String
        )
        bundle.putString(
            CROP_MAGIC_TITLE,
            call.argument<String>(CROP_MAGIC_TITLE) as String
        )
        bundle.putString(
            CROP_HPF_TITLE,
            call.argument<String>(CROP_HPF_TITLE) as String
        )
        bundle.putString(CROP_RESET_TITLE, call.argument<String>(CROP_RESET_TITLE) as String)
        bundle.putBoolean(FROM_GALLERY, true)
        initialIntent.putExtra(INITIAL_BUNDLE, bundle)
        getActivity()?.startActivityForResult(initialIntent, REQUEST_CODE)
    }

    private fun setPendingMethodCallAndResult(
        methodCall: MethodCall,
        result: Result
    ): Boolean {
        if (this.result != null) {
            return false
        }
        this.methodCall = methodCall
        this.result = result
        return true
    }

    private fun finishWithAlreadyActiveError() {
        finishWithError("already_active", "Edge detection is already active")
    }

    private fun finishWithError(errorCode: String, errorMessage: String) {
        result?.error(errorCode, errorMessage, null)
        clearMethodCallAndResult()
    }

    private fun finishWithSuccess(imagePath: String?) {
        result?.success(imagePath)
        clearMethodCallAndResult()
    }

    private fun clearMethodCallAndResult() {
        methodCall = null
        result = null
    }
}
