package com.sample.almatoscanner.scan

import android.view.Display
import android.view.SurfaceView
import com.sample.almatoscanner.view.PaperRectangle

interface IScanView {
    interface Proxy {
        fun exit()
        fun getCurrentDisplay(): Display?
        fun getSurfaceView(): SurfaceView
        fun getPaperRect(): PaperRectangle
    }
}