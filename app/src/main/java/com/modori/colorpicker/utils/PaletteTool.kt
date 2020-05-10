package com.modori.colorpicker.utils

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

class PaletteTool {

    companion object {

        fun getColorSet(bitmap: Bitmap): MutableSet<Int> {
            println(bitmap.width)
            println(bitmap.height)
            val colorSet: MutableSet<Int> = mutableSetOf()
            val palette = Palette.from(bitmap).generate()

            val vibrantSwatch = palette.vibrantSwatch
            val darkMutedSwatch = palette.darkMutedSwatch
            val darkVibrantSwatch = palette.darkVibrantSwatch
            val dominantSwatch = palette.dominantSwatch
            val lightMutedSwatch = palette.lightMutedSwatch
            val lightVibrantSwatch = palette.lightVibrantSwatch
            val mutedSwatch = palette.mutedSwatch

            if (vibrantSwatch != null) {
                println(vibrantSwatch.rgb)
                colorSet.add(vibrantSwatch.rgb)

            }

            if (darkMutedSwatch != null) {
                colorSet.add(darkMutedSwatch.rgb)
            }

            if (darkVibrantSwatch != null) {
                colorSet.add(darkVibrantSwatch.rgb)
            }

            if (dominantSwatch != null) {
                colorSet.add(dominantSwatch.rgb)

            }

            if (lightMutedSwatch != null) {
                colorSet.add(lightMutedSwatch.rgb)

            }
            if (lightVibrantSwatch != null) {
                colorSet.add(lightVibrantSwatch.rgb)

            }

            if (mutedSwatch != null) {
                colorSet.add(mutedSwatch.rgb)

            }

            println("모은 색깔... $colorSet")

            println(colorSet)
            return colorSet

        }


    }

}