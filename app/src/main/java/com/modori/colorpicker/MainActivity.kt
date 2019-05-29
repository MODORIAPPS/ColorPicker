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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.modori.colorpicker.Api.RandomImage
import com.modori.colorpicker.Model.RandomImageModel
import com.modori.colorpicker.RA.ColorAdapter
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        getRandomPhoto()

        refreshBtn.setOnClickListener {
            getRandomPhoto()
        }


        openGallery.setOnClickListener {
            val getFromGallery = Intent(Intent.ACTION_PICK)
            getFromGallery.type = "image/*"
            getFromGallery.putExtra(Intent.ACTION_GET_CONTENT, true)
            getFromGallery.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(Intent.createChooser(getFromGallery, "Select Picture"), PICTURE_REQUEST_CODE)

        }


    }

    private fun getRandomPhoto() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(RandomImage::class.java)
        val call = service.getRandomPhoto()
        call.enqueue(object : Callback<RandomImageModel> {
            override fun onResponse(call: Call<RandomImageModel>, response: Response<RandomImageModel>) {
                if (response.isSuccessful) {

                    Glide.with(applicationContext).asBitmap().load(response.body()!!.urls!!.regular)
                        .listener(object : RequestListener<Bitmap> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Bitmap,
                                model: Any?,
                                target: Target<Bitmap>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                setImageView(resource)
                                createPaletteAsync(resource)
                                return true
                            }
                        }).into(imageview)
                    //Glide.with(applicationContext).load(response.body()!!.urls!!.regular).into(imageview)


                }
            }

            override fun onFailure(call: Call<RandomImageModel>, t: Throwable) {
                Log.d("통신 실패 사유", t.message)
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val mDAta = data.data
                val uri: Uri? = mDAta

                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                imageview.setImageBitmap(bitmap)
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

            setRecyclerView(colorSet)




        }
    }

    private fun setImageView(bitmap: Bitmap){
        YoYo.with(Techniques.FadeIn)
            .duration(300)
            .repeat(0)
            .playOn(imageview)
        imageview.setImageBitmap(bitmap)
    }

    private fun setRecyclerView(colorSet:Set<Int>){
        val adapter = ColorAdapter(colorSet.toList(), this)
        Log.d("색류", colorSet.toString())
        colorsRV.layoutManager = LinearLayoutManager(this)
        colorsRV.adapter = adapter

        YoYo.with(Techniques.FadeIn)
            .duration(300)
            .repeat(0)
            .playOn(colorsRV)

        adapter.notifyDataSetChanged()

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
