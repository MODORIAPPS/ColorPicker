package com.modori.colorpicker

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crashlytics.android.Crashlytics
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.modori.colorpicker.Api.RandomImage
import com.modori.colorpicker.model.RandomImageModel
import com.modori.colorpicker.RA.ColorAdapter
import com.modori.colorpicker.Utils.PaletteTool
import com.modori.colorpicker.model.ActivityModel
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream


class MainActivity : AppCompatActivity() {


    //var photoId: String = "eee"
    var photoBitmap: Bitmap? = null
    var colorList: IntArray? = null
    var retrofit: Retrofit? = null

    lateinit var imageUri: Uri
    lateinit var colorSet: MutableSet<Int>


    //var imageType: Boolean? = null


    private val PICTURE_REQUEST_CODE: Int = 123
    private val MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2
    lateinit var viewModel: ActivityModel
    lateinit var pDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Fabric.with(this, Crashlytics())
        pDialog = ProgressDialog(this)

        viewModel = ViewModelProviders.of(this).get(ActivityModel::class.java)
        viewModel.getBitmaps().observe(this, androidx.lifecycle.Observer {
            if (viewModel.getBitmaps().value != null) {
                setRecyclerView(PaletteTool.getColorSet(viewModel.getBitmaps().value!!))
                setImageView(viewModel.getBitmaps().value!!)
            }
        })

        val ReadpermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (ReadpermissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE
            )

        } else {

            if (savedInstanceState != null) {
//
//                if(savedInstanceState.getString("photoId") != null){
//                    val mPhotoId: String = savedInstanceState.getString("photoId")
//                    val mPhotoUri: Uri = savedInstanceState.getParcelable("photoUri")
//                    val mPhotoType: Boolean = savedInstanceState.getBoolean("photoType")
//                    imageType = mPhotoType
//                    //getPhotoById(mPhotoId)
//                    imageUri = mPhotoUri
//                    photoId = mPhotoId
//                    imageview.setImageURI(mPhotoUri)
//                    createPaletteAsync(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))
//
//                }else{
//                    getRandomPhoto()
//                }


            } else {
                getRandomPhoto()

            }


        }



        shareBtn.setOnClickListener {
            //            val intent = Intent(this, ScreenshotActivity::class.java)
//            startActivity(intent)
//
            sendBitmapBundle()
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

//    private fun getPhotoById(mPhotoId: String) {
//
//        Log.d("getPhotoByID", mPhotoId)
//
//        retrofit = Retrofit.Builder()
//            .baseUrl("https://api.unsplash.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val service = retrofit!!.create(RandomImage::class.java)
//        val call = service.getPhotoById(mPhotoId)
//
//        call.enqueue(object : Callback<RandomImageModel> {
//            override fun onFailure(call: Call<RandomImageModel>, t: Throwable) {
//                Log.d("IDsearch 실패", t.message)
//            }
//
//            override fun onResponse(call: Call<RandomImageModel>, response: Response<RandomImageModel>) {
//                Log.d("받아온 값", response.body().toString())
//                //photoId = response.body()!!.id
//                Glide.with(applicationContext).asBitmap().load(response.body()!!.urls!!.regular)
//                    .listener(object : RequestListener<Bitmap> {
//                        override fun onLoadFailed(
//                            e: GlideException?,
//                            model: Any?,
//                            target: Target<Bitmap>?,
//                            isFirstResource: Boolean
//                        ): Boolean {
//                            return false
//                        }
//
//                        override fun onResourceReady(
//                            resource: Bitmap,
//                            model: Any?,
//                            target: Target<Bitmap>?,
//                            dataSource: DataSource?,
//                            isFirstResource: Boolean
//                        ): Boolean {
//                            imageUri = "null".toUri()
//                            setImageView(resource)
//                            photoBitmap = resource
//                            createPaletteAsync(resource)
//                            return true
//                        }
//                    }).into(imageview)
//            }
//        })
//
//    }

    private fun getRandomPhoto() {

        setUpDialog()
        pDialog.show()


        retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit!!.create(RandomImage::class.java)
        val call = service.getRandomPhoto()
        println("새 사진을 불러옴")

        val getPhoto = GlobalScope.launch(Dispatchers.Default) {
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
                                    photoBitmap = resource
                                    viewModel.setBitmap(photoBitmap!!)
                                    setRecyclerView(PaletteTool.getColorSet(photoBitmap!!))
                                    return true
                                }
                            }).into(imageview)

                        if (pDialog != null && pDialog.isShowing) {
                            pDialog.dismiss()
                        }

                    }
                }

                override fun onFailure(call: Call<RandomImageModel>, t: Throwable) {
                    Log.d("통신 실패 사유", t.message)

                    if (pDialog != null && pDialog.isShowing) {
                        pDialog.dismiss()
                    }

                }


            })
        }


    }

    private fun setUpDialog() {
        pDialog.setMessage("이미지를 불러오고 있어요.")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Log.d("photoId", photoId)


        if (photoBitmap != null) {
            //outState.putString("photoId", photoId)
            viewModel.setBitmap(photoBitmap!!)
            //imageUri = getImageUri(this, photoBitmap!!)
            //outState.putParcelable("photoUri", imageUri)
            //outState.putBoolean("photoType", imageType!!)

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val inputStream: InputStream = contentResolver.openInputStream(data.data)
                photoBitmap = BitmapFactory.decodeStream(inputStream)
                //Log.d("ImageUri", imageUri.toString())
                viewModel.setBitmap(photoBitmap!!)
                imageview.setImageBitmap(photoBitmap)
                setRecyclerView(PaletteTool.getColorSet(viewModel.getBitmaps().value!!))

                //imageType = false
            }
        }

    }

    private fun sendBitmapBundle() {
        //val bitmap: Bitmap = viewModel.getBitmaps().value!!
        val bitmap: Bitmap = photoBitmap!!
        val stream = ByteArrayOutputStream()

        val scale: Float = (1024 / bitmap.width.toFloat())
        val imageW: Int = (bitmap.width * scale).toInt()
        val imageH: Int = (bitmap.height * scale).toInt()

        val resize: Bitmap = Bitmap.createScaledBitmap(bitmap, imageW, imageH, true)
        resize.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        val colorArray = colorSet.toIntArray()

        val intent = Intent(this, ScreenshotActivity::class.java)
        intent.putExtra("image", byteArray)
        intent.putExtra("color", colorArray)

        startActivity(intent)

    }

//    private fun getResizedBitmap(uri: Uri): Bitmap {
//        val options: android.graphics.BitmapFactory.Options = android.graphics.BitmapFactory.Options()
//        options.inJustDecodeBounds = true
//
//        val input: InputStream = contentResolver.openInputStream(uri)!!
//
//        Log.d("받아온 InputStream", input.toString())
//        android.graphics.BitmapFactory.decodeStream(input, null, options)
//        //options.inSampleSize = getResizeRate(options.outWidth, options.outHeight, 900, 900)
//        if (options.outWidth * options.outHeight >= 900 * 900) {
//            options.inSampleSize = 4
//        } else {
//            options.inSampleSize = 1
//        }
//        Log.d("줄여진 사이즈", options.inSampleSize.toString())
//        options.inJustDecodeBounds = false
//
//        val mInput: InputStream = contentResolver.openInputStream(uri)
////        val bitmap:Bitmap
////        try{
////            bitmap = android.graphics.BitmapFactory.decodeStream(mInput, null, options)
////        }catch (e:Exception){
////            Log.d("실패사유", e.message)
////        }
//
//        return android.graphics.BitmapFactory.decodeStream(mInput, null, options)
//
//
//    }

//    private fun getImageUri(context: Context, inImage: Bitmap): Uri {
//        val bytes = ByteArrayOutputStream()
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//        val path: String = MediaStore.Images.Media.insertImage(
//            context.contentResolver,
//            inImage,
//            UUID.randomUUID().toString() + ".png",
//            "drawing"
//        )
//        return Uri.parse(path)
//    }

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

    private fun setImageView(bitmap: Bitmap) {
        YoYo.with(Techniques.FadeIn)
            .duration(500)
            .repeat(0)
            .playOn(imageview)

        if (bitmap.width > 1000 && bitmap.height > 1000) {
            Glide.with(this).load(bitmap).override(800, 800).into(imageview)
        }
        imageview.setImageBitmap(bitmap)
    }

    private fun setRecyclerView(set: Set<Int>) {
        val adapter = ColorAdapter(set.toList(), this)
        colorList = set.toIntArray()
        colorSet = set as MutableSet<Int>
        Log.d("색류", set.toString())
        colorsRV.layoutManager = LinearLayoutManager(this)
        colorsRV.adapter = adapter

        utilToolBar.setBackgroundColor(colorList!![0])

        YoYo.with(Techniques.FadeIn)
            .duration(500)
            .repeat(0)
            .playOn(colorsRV)

        adapter.notifyDataSetChanged()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getRandomPhoto()
            } else {

            }
        }
    }


}
