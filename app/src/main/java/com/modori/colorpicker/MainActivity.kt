package com.modori.colorpicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.modori.colorpicker.Model.RandomImageModel
import com.modori.colorpicker.RA.ColorAdapter
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Response
import android.graphics.BitmapFactory as BitmapFactory1


class MainActivity : AppCompatActivity() {

    var vibrantSwatch: Palette.Swatch? = null
    var darkMutedSwatch: Palette.Swatch? = null
    var darkVibrantSwatch: Palette.Swatch? = null
    var dominantSwatch: Palette.Swatch? = null
    var lightMutedSwatch: Palette.Swatch? = null
    var lightVibrantSwatch: Palette.Swatch? = null
    var mutedSwatch: Palette.Swatch? = null

    private val PICTURE_REQUEST_CODE: Int = 123
    private val MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionCheck()

        val bitmap: Bitmap = android.graphics.BitmapFactory.decodeResource(resources, R.drawable.sample_image2)
        createPaletteAsync(bitmap)

        openGallery.setOnClickListener {
            val getFromGallery = Intent(Intent.ACTION_PICK)
            getFromGallery.type = "image/*"
            getFromGallery.putExtra(Intent.ACTION_GET_CONTENT, true)
            getFromGallery.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(Intent.createChooser(getFromGallery, "Select Picture"), PICTURE_REQUEST_CODE)

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val data = data.data
                val uri: Uri = data

                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                createPaletteAsync(bitmap)
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {

            }
        }
    }

    private fun createPaletteAsync(bitmap: Bitmap) {
        Palette.from(bitmap).generate {

            vibrantSwatch = it?.vibrantSwatch
            darkMutedSwatch = it?.darkMutedSwatch
            darkVibrantSwatch = it?.darkVibrantSwatch
            dominantSwatch = it?.dominantSwatch
            lightMutedSwatch = it?.lightMutedSwatch
            lightVibrantSwatch = it?.lightVibrantSwatch
            mutedSwatch = it?.mutedSwatch

            //colorData = ArrayList()
            val colorSet: MutableSet<Int> = mutableSetOf()


            if (vibrantSwatch != null) {
                colorSet.add(vibrantSwatch!!.rgb)


            }

            if (darkMutedSwatch != null) {
                colorSet.add(darkMutedSwatch!!.rgb)


            }

            if (darkVibrantSwatch != null) {
                colorSet.add(darkVibrantSwatch!!.rgb)


            }

            if (dominantSwatch != null) {
                colorSet.add(dominantSwatch!!.rgb)

            }

            if (lightMutedSwatch != null) {
                colorSet.add(lightMutedSwatch!!.rgb)

            }
            if (lightVibrantSwatch != null) {
                colorSet.add(lightVibrantSwatch!!.rgb)

            }

            if (mutedSwatch != null) {
                colorSet.add(mutedSwatch!!.rgb)

            }

            val adapter = ColorAdapter(colorSet.toList(), this)



            Log.d("색류", colorSet.toString())
            colorsRV.layoutManager = LinearLayoutManager(this)
            colorsRV.adapter = adapter

            adapter.notifyDataSetChanged()


        }
    }

    private fun permissionCheck() {
        val ReadpermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (ReadpermissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE
            )

        } else {

        }

    }
}
