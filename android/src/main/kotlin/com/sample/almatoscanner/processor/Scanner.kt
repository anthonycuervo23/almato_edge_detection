package com.sample.almatoscanner.processor

import java.util.ArrayList
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class Scan(image: Mat, kernelSize: Int, blackPoint: Double, whitePoint: Double) {
    // Mat objects to store input image, filtered image from HPF & scanned image
    private val inputImg: Mat
    private val filtered: Mat = Mat()
    private var processedImg: Mat = Mat()
    private var kSize: Int
    private val blackPoint: Double
    private var whitePoint: Double

    // enum type to help select mode of scanning
    enum class ScanMode {
        GCMODE, RMODE, SMODE
    }

    // constructor
    init {
        inputImg = image.clone()
        kSize = kernelSize
        this.blackPoint = blackPoint
        this.whitePoint = whitePoint
    }

    /* High Pass Filter
     * Output of HPF depends on the kernel size provided through the constructor
     * Links to the docuementation:
     *  Introduction: https://github.com/sourabhkhemka/DocumentScanner/wiki/Scan:-Introduction
     *  HPF: https://github.com/sourabhkhemka/DocumentScanner/wiki/GCMODE
     */
    private fun highPassFilter() {
        if (kSize % 2 == 0) kSize++
        var kernel: Mat = Mat.ones(kSize, kSize, CvType.CV_32FC1)
        kernel = kernel.mul(kernel, 1.0 / (kSize.toFloat() * kSize.toFloat()))
        Imgproc.filter2D(inputImg, filtered, -1, kernel)

        // Convert both to float to avoid saturation of pixel values to 255
        filtered.convertTo(filtered, CvType.CV_32FC3)
        inputImg.convertTo(inputImg, CvType.CV_32FC3)
        Core.subtract(inputImg, filtered, filtered)
        kernel = Mat.zeros(inputImg.size(), CvType.CV_32FC3)
        kernel.setTo(Scalar(1.0, 1.0, 1.0))
        Core.multiply(kernel, Scalar(127.0, 127.0, 127.0), kernel)
        Imgproc.cvtColor(filtered, filtered, Imgproc.COLOR_RGBA2RGB)
        Core.add(filtered, kernel, filtered)
        filtered.convertTo(filtered, CvType.CV_8UC3)

        // "filtered" now contains high pass filtered image.
    }

    /* Method to select whitePoint in the image
     *
     * Links to documentation:
     *  Introduction: https://github.com/sourabhkhemka/DocumentScanner/wiki/Scan:-Introduction
     *  White Point Select: https://github.com/sourabhkhemka/DocumentScanner/wiki/White-Point-Select
     */
    private fun whitePointSelect() {

        // refer repository's wiki page for detailed explanation
        Imgproc.threshold(processedImg, processedImg, whitePoint, 255.0, Imgproc.THRESH_TRUNC)
        Core.subtract(processedImg, Scalar(0.0, 0.0, 0.0), processedImg)
        val tmp = 255.0 / (whitePoint.toFloat() - 0)
        Core.multiply(processedImg, Scalar(tmp, tmp, tmp), processedImg)
    }

    /* Method to select black point in the image
     *
     * Links to documentation:
     *  Introduction: https://github.com/sourabhkhemka/DocumentScanner/wiki/Scan:-Introduction
     *  Black Point Select: https://github.com/sourabhkhemka/DocumentScanner/wiki/Black-Point-Select
     */
    private fun blackPointSelect() {

        // refer repository's wiki page for detailed explanation
        Core.subtract(processedImg, Scalar(blackPoint, blackPoint, blackPoint), processedImg)
        val tmp = 255.0 / (255.0 - blackPoint)
        Core.multiply(processedImg, Scalar(tmp, tmp, tmp), processedImg)
    }

    /* Method to process image in LAB color space to generate black and white images
     *  Wiki link: https://github.com/sourabhkhemka/DocumentScanner/wiki/SMODE:-Black-&-White
     */
    private fun blackAndWhite() {

        // refer repository's wiki page for detailed explanation
        val lab: List<Mat> = ArrayList()
        val subA = Mat()
        val subB = Mat()
        Imgproc.cvtColor(processedImg, processedImg, Imgproc.COLOR_BGR2Lab)
        Core.split(processedImg, lab)
        Core.subtract(lab[0], lab[1], subA)
        Core.subtract(lab[0], lab[2], subB)
        Core.add(subA, subB, processedImg)
    }

    /* Method scanImage is the only public method of the Scan class.
     *  This method will be called to scan the image provided at time of
     *  constructing Scan class' object.
     *
     * scanImage method uses switch-case to execute required methods in correct order
     *  to implemnt desired mode of scanning.
     *
     * This method takes enum type as argument.
     *
     * whitePointSelect() and blackPointSelect() methods are designed to process "processedImg"
     *  hence "inputImg" is copied to "processedImg" in RMODE and SMODE.
     *  highPassFilter() outputs filtered image as "filtered" hence we need to copy "filtered"
     *  to "processedImg" so that whitePointSelect() can further process it.
     */
    fun scanImage(mode: ScanMode?): Mat {
        when (mode) {
            ScanMode.GCMODE -> {
                highPassFilter()
                processedImg = filtered.clone()
                // Fix white point value at 127 for GCMODE
                whitePoint = 127.0
                whitePointSelect()
                blackPointSelect()
            }
            ScanMode.RMODE -> {
                processedImg = inputImg.clone()
                blackPointSelect()
                whitePointSelect()
            }
            ScanMode.SMODE -> {
                processedImg = inputImg.clone()
                blackPointSelect()
                whitePointSelect()
                blackAndWhite()
            }
            else -> System.out.println("Error: Incorrect ScanMode supplied. Expected input: GCSCAN/RSCAN/SCAN")
        }
        return processedImg
    }
}