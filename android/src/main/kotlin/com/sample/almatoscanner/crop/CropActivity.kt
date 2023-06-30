package com.sample.almatoscanner.crop

import android.content.Intent
import com.sample.almatoscanner.SCANNED_RESULT
//
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.sample.almatoscanner.AlmatoScannerHandler
import com.sample.almatoscanner.R
import com.sample.almatoscanner.REQUEST_CODE
import com.sample.almatoscanner.base.BaseActivity
import com.sample.almatoscanner.processor.Scan
import com.sample.almatoscanner.view.PaperRectangle
import com.sample.almatoscanner.databinding.ActivityCropBinding


class CropActivity : BaseActivity(), ICropView.Proxy {

    private var showMenuItems = false

    private lateinit var mPresenter: CropPresenter

    private lateinit var initialBundle: Bundle;

    private lateinit var binding: ActivityCropBinding

    override fun prepare() {
        this.initialBundle = intent.getBundleExtra(AlmatoScannerHandler.INITIAL_BUNDLE) as Bundle;
        this.title = initialBundle.getString(AlmatoScannerHandler.CROP_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //we have to initialize everything in post when the view has been drawn and we have the actual height and width of the whole view
        binding.paper.post {  // Usa `binding` para acceder a las vistas
            mPresenter.onViewsReady(binding.paper.width, binding.paper.height)
        }
    }

    override fun provideContentViewId(): Int = R.layout.activity_crop


    override fun initPresenter() {
        binding = ActivityCropBinding.inflate(layoutInflater)  // Infla la clase de binding aquí
        setContentView(binding.root)  // Usa binding.root en lugar de la constante de layout
        val initialBundle = intent.getBundleExtra(AlmatoScannerHandler.INITIAL_BUNDLE) as Bundle;
        mPresenter = CropPresenter(this, this, initialBundle)
        binding.crop.setOnClickListener {
            mPresenter.crop()
            changeMenuVisibility(true)
        }
    }

    override fun getPaper(): ImageView = binding.paper  // Usa `binding` para acceder a las vistas

    override fun getPaperRect(): PaperRectangle = binding.paperRect  // Usa `binding` para acceder a las vistas

    override fun getCroppedPaper(): ImageView = binding.pictureCropped  // Usa `binding` para acceder a las vistas

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.crop_activity_menu, menu)

//        menu.setGroupVisible(R.id.enhance_group, showMenuItems)
        menu.setGroupVisible(R.id.enhance_group, false)

        menu.findItem(R.id.rotation_image).isVisible = showMenuItems

        menu.findItem(R.id.grey).title =
            initialBundle.getString(AlmatoScannerHandler.CROP_BLACK_WHITE_TITLE) as String
        menu.findItem(R.id.magic).title =
            initialBundle.getString(AlmatoScannerHandler.CROP_MAGIC_TITLE) as String
        menu.findItem(R.id.hpf).title =
            initialBundle.getString(AlmatoScannerHandler.CROP_HPF_TITLE) as String
        menu.findItem(R.id.reset).title =
            initialBundle.getString(AlmatoScannerHandler.CROP_RESET_TITLE) as String

        if (showMenuItems) {
            menu.findItem(R.id.action_label).isVisible = true
            findViewById<ImageView>(R.id.crop).visibility = View.GONE
        } else {
            menu.findItem(R.id.action_label).isVisible = false
            findViewById<ImageView>(R.id.crop).visibility = View.VISIBLE
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun changeMenuVisibility(showMenuItems: Boolean) {
        this.showMenuItems = showMenuItems
        invalidateOptionsMenu()
    }

    // handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        when (item.itemId) {
            R.id.action_label -> {
                Log.e(TAG, "Saved touched!")
                // Bug fix: prevent clicking more than one time
                item.setEnabled(false)
                val path = mPresenter.save()
                Log.e(TAG, "Saved touched! $path")
                setResult(Activity.RESULT_OK, Intent().putExtra(SCANNED_RESULT, path))
                //
                System.gc()
                finish()
                return true
            }
            R.id.rotation_image -> {
                Log.e(TAG, "Rotate touched!")
                mPresenter.rotate()
                return true
            }
            R.id.grey -> {
                mPresenter.reset()
                Log.e(TAG, "Black White filter touched!")
                mPresenter.enhance(Scan.ScanMode.SMODE)
                return true
            }
            R.id.magic -> {
                mPresenter.reset()
                Log.e(TAG, "Magic filter touched!")
                mPresenter.enhance(Scan.ScanMode.RMODE)
                return true
            }
            R.id.hpf -> {
                Log.e(TAG, "HPF touched!")
                mPresenter.reset()
                mPresenter.enhance(Scan.ScanMode.GCMODE)
                return true
            }
            R.id.reset -> {
                Log.e(TAG, "Reset touched!")
                mPresenter.reset()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }
}
