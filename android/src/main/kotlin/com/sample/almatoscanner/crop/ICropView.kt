package com.sample.almatoscanner.crop

import android.widget.ImageView
import com.sample.almatoscanner.view.PaperRectangle


/**
 * Created by pengke on 15/09/2017.
 */
class ICropView {
    interface Proxy {
        fun getPaper(): ImageView
        fun getPaperRect(): PaperRectangle
        fun getCroppedPaper(): ImageView
    }
}