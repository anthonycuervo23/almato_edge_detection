package com.sample.almatoscanner


import com.sample.almatoscanner.processor.Corners
import org.opencv.core.Mat

class SourceManager {
    companion object {
        var pic: Mat? = null
        var corners: Corners? = null
    }
}