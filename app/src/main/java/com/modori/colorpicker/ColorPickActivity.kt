package com.modori.colorpicker

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.activity_color_pick.*
import android.R.attr.bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.ImageView


class ColorPickActivity : AppCompatActivity() {

    private val PICTURE_REQUEST_CODE: Int = 123
    var imageBitmap: Bitmap? = null

    var imageUri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_pick)

        openGallery_p.setOnClickListener {
            val getFromGallery = Intent(Intent.ACTION_PICK)
            getFromGallery.type = "image/*"
            getFromGallery.putExtra(Intent.ACTION_GET_CONTENT, true)
            getFromGallery.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(Intent.createChooser(getFromGallery, "Select Picture"), PICTURE_REQUEST_CODE)
        }

        imageView_p.setOnTouchListener { v, event ->


            val bitmap:Bitmap = imageView_p.drawable.toBitmap()
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()
            val pixel = bitmap.getPixel(x, y)

            //then do what you want with the pixel data, e.g
            val redV = Color.red(pixel)
            val blueV = Color.blue(pixel)
            val greenV = Color.green(pixel)

            showRgb.text = "RGB ( $redV, $greenV, $blueV ) "

            colorBlock.setBackgroundColor(Color.parseColor(rgbToHexString(redV, greenV, blueV)))
            val hexColor: String =
                String.format("#%06X", (0xFFFFFF and Color.parseColor(rgbToHexString(redV, greenV, blueV))))
            showHexString.text = "HEX : $hexColor"

            false
        }

//        imageView_p.setOnTouchListener { _, event ->
//            var pixel = 0
//
//            try {
//                pixel = imageView_p.drawable.toBitmap().getPixel(event.x.toInt(), event.y.toInt())
//
//            } catch (e: Exception) {
//                Log.d("오류상황", e.message)
//            }
//
//            val redV = Color.red(pixel)
//            val greenV = Color.green(pixel)
//            val blueV = Color.blue(pixel)
//
//            showRgb.text = "RGB ( $redV, $greenV, $blueV ) "
//
//            colorBlock.setBackgroundColor(Color.parseColor(rgbToHexString(redV, greenV, blueV)))
//            val hexColor: String =
//                String.format("#%06X", (0xFFFFFF and Color.parseColor(rgbToHexString(redV, greenV, blueV))))
//            showHexString.text = "HEX : $hexColor"
//            return@setOnTouchListener true
//
//        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                imageUri = data.data

                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                imageBitmap = bitmap

                imageView_p.scaleType = ImageView.ScaleType.CENTER_INSIDE
                imageView_p.setImageBitmap(bitmap)
                openGallery_p.visibility = View.GONE
            }
        }


    }

    private fun rgbToHexString(r: Int, g: Int, b: Int): String {
        return String.format("#%02x%02x%02x", r, g, b)

    }


}
