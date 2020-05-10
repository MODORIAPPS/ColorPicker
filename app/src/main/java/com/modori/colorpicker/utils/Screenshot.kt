package com.modori.colorpicker.utils

import android.graphics.Bitmap
import android.view.View

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