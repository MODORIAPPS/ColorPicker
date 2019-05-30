package com.modori.colorpicker.Utils

import android.graphics.Bitmap
import android.os.Environment
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class Screenshot {

    companion object {
        fun takescreenshot(v: View): Bitmap {
            v.isDrawingCacheEnabled = true
            v.buildDrawingCache(true)
            val bitmap: Bitmap = Bitmap.createBitmap(v.drawingCache)

            v.isDrawingCacheEnabled = false
            return bitmap

        }

        fun takeScreenShotOfRootView(v: View): Bitmap {
            return takescreenshot(v.rootView)
        }


    }

}