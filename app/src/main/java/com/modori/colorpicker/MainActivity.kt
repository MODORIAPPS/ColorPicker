package com.modori.colorpicker

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
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
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import android.graphics.BitmapFactory as BitmapFactory1


class MainActivity : AppCompatActivity() {

    var vibrantSwatch: Palette.Swatch? = null
    var darkMutedSwatch: Palette.Swatch? = null
    var darkVibrantSwatch: Palette.Swatch? = null
    var dominantSwatch: Palette.Swatch? = null
    var lightMutedSwatch: Palette.Swatch? = null
    var lightVibrantSwatch: Palette.Swatch? = null
    var mutedSwatch: Palette.Swatch? = null

    var photoId: String = "eee"
    var photoBitmap: Bitmap? = null
    var colorList: IntArray? = null
    var retrofit: Retrofit? = null

    lateinit var imageUri: Uri

    var imageType: Boolean? = null


    private val PICTURE_REQUEST_CODE: Int = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        if (savedInstanceState != null) {

            if(savedInstanceState.getString("photoId") != null){
                val mPhotoId: String = savedInstanceState.getString("photoId")
                val mPhotoUri: Uri = savedInstanceState.getParcelable("photoUri")
                val mPhotoType: Boolean = savedInstanceState.getBoolean("photoType")
                imageType = mPhotoType
                //getPhotoById(mPhotoId)
                imageUri = mPhotoUri
                photoId = mPhotoId
                imageview.setImageURI(mPhotoUri)
                createPaletteAsync(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))

            }else{
                getRandomPhoto()
            }


        } else {
            getRandomPhoto()

        }

        shareBtn.setOnClickListener {
            val intent = Intent(this, ScreenshotActivity::class.java)

            if (imageType != null) {
                when (imageType) {
                    true -> {
                        intent.putExtra("photoId", photoId)
                        intent.putExtra("colorList", colorList)
                        startActivity(intent)

                    }
                    false -> {
                        intent.putExtra("photoId", "eee")
                        intent.putExtra("imageUri", imageUri)
                        intent.putExtra("colorList", colorList)
                        startActivity(intent)
                    }
                }
            } else {
                Toast.makeText(this, "사진을 받아오고 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
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

        colorizeBtn.setOnClickListener {
            startActivity(Intent(this, ColorPickActivity::class.java))
        }


    }

    private fun getPhotoById(mPhotoId: String) {

        Log.d("getPhotoByID", mPhotoId)

        retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit!!.create(RandomImage::class.java)
        val call = service.getPhotoById(mPhotoId)

        call.enqueue(object : Callback<RandomImageModel> {
            override fun onFailure(call: Call<RandomImageModel>, t: Throwable) {
                Log.d("IDsearch 실패", t.message)
            }

            override fun onResponse(call: Call<RandomImageModel>, response: Response<RandomImageModel>) {
                Log.d("받아온 값", response.body().toString())
                photoId = response.body()!!.id
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
                            imageUri = "null".toUri()
                            setImageView(resource)
                            photoBitmap = resource
                            createPaletteAsync(resource)
                            return true
                        }
                    }).into(imageview)
            }
        })

    }

    private fun getRandomPhoto() {
        retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit!!.create(RandomImage::class.java)
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
                                imageType = true
                                photoId = response.body()!!.id
                                photoBitmap = resource
                                imageUri = getImageUri(application, resource)
                                Log.d("초기 id", photoId)
                                createPaletteAsync(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))
                                return true
                            }
                        }).into(imageview)


                }
            }

            override fun onFailure(call: Call<RandomImageModel>, t: Throwable) {
                Log.d("통신 실패 사유", t.message)
            }
        })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("photoId", photoId)


        if (photoBitmap != null) {
            outState.putString("photoId", photoId)

            //imageUri = getImageUri(this, photoBitmap!!)
            outState.putParcelable("photoUri", imageUri)
            outState.putBoolean("photoType", imageType!!)

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                imageUri = data.data
                photoId = "eee"

                Log.d("ImageUri", imageUri.toString())
                getResizedBitmap(imageUri)

                val bitmap: Bitmap = getResizedBitmap(imageUri)
                //getResizedBitmap(imageUri)

                imageview.setImageBitmap(bitmap)

                createPaletteAsync(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))
                imageType = false
            }
        }

    }

    private fun getResizedBitmap(uri: Uri): Bitmap {
        val options: android.graphics.BitmapFactory.Options = android.graphics.BitmapFactory.Options()
        options.inJustDecodeBounds = true

        val input: InputStream = contentResolver.openInputStream(uri)!!

        Log.d("받아온 InputStream", input.toString())
        android.graphics.BitmapFactory.decodeStream(input, null, options)
        //options.inSampleSize = getResizeRate(options.outWidth, options.outHeight, 900, 900)
        if (options.outWidth * options.outHeight >= 900 * 900) {
            options.inSampleSize = 4
        } else {
            options.inSampleSize = 1
        }
        Log.d("줄여진 사이즈", options.inSampleSize.toString())
        options.inJustDecodeBounds = false

        val mInput: InputStream = contentResolver.openInputStream(uri)
//        val bitmap:Bitmap
//        try{
//            bitmap = android.graphics.BitmapFactory.decodeStream(mInput, null, options)
//        }catch (e:Exception){
//            Log.d("실패사유", e.message)
//        }

        return android.graphics.BitmapFactory.decodeStream(mInput, null, options)


    }

    private fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            inImage,
            UUID.randomUUID().toString() + ".png",
            "drawing"
        )
        return Uri.parse(path)
    }

    private fun getResizeRate(outWidth: Int, outHeight: Int, wantWidth: Int, wantHeight: Int): Int {
        var size: Int = 1

        var mOutWidth = outWidth
        var mOutHeight = outHeight

        Log.d("받은 사진 가로", mOutWidth.toString())
        Log.d("받은 사진 세로", mOutHeight.toString())

        return if (outWidth * outHeight < wantWidth * wantHeight) {
            while (mOutWidth < wantWidth || mOutHeight < wantHeight) {
                mOutWidth /= 2
                mOutHeight /= 2

                size *= 2

            }

            size
        } else {
            1

        }


    }


    private fun createPaletteAsync(bitmap: Bitmap) {
        Palette.from(bitmap).generate {


            photoBitmap = bitmap
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

    private fun setImageView(bitmap: Bitmap) {
        YoYo.with(Techniques.FadeIn)
            .duration(500)
            .repeat(0)
            .playOn(imageview)
        imageview.setImageBitmap(bitmap)
    }


    private fun setRecyclerView(colorSet: Set<Int>) {
        val adapter = ColorAdapter(colorSet.toList(), this)
        colorList = colorSet.toIntArray()
        Log.d("색류", colorSet.toString())
        colorsRV.layoutManager = LinearLayoutManager(this)
        colorsRV.adapter = adapter

        utilToolBar.setBackgroundColor(colorList!![0])

        YoYo.with(Techniques.FadeIn)
            .duration(500)
            .repeat(0)
            .playOn(colorsRV)

        adapter.notifyDataSetChanged()

    }


}
