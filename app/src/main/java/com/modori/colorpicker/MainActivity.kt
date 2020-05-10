package com.modori.colorpicker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.SkeletonLayout
import com.faltenreich.skeletonlayout.applySkeleton
import com.modori.colorpicker.api.RandomImage
import com.modori.colorpicker.model.RandomImageModel
import com.modori.colorpicker.adapter.ColorAdapter
import com.modori.colorpicker.utils.PaletteTool
import com.modori.colorpicker.model.ActivityModel
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


class MainActivity : AppCompatActivity(), View.OnClickListener {


    //var photoId: String = "eee"
    var photoBitmap: Bitmap? = null
    var colorList: IntArray? = null
    var retrofit: Retrofit? = null
    var photoId: String? = null
    lateinit var colorsRvMask: Skeleton

    lateinit var imageUri: Uri
    lateinit var colorSet: MutableSet<Int>


    //var imageType: Boolean? = null

    private val PICTURE_REQUEST_CODE: Int = 123
    private val MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2
    lateinit var viewModel: ActivityModel

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.shareBtn -> {
                if (photoBitmap != null) {
                    sendBitmapBundle()

                }
            }
            R.id.refreshBtn -> getRandomPhoto()
            R.id.openGallery -> {
                val getFromGallery = Intent(Intent.ACTION_PICK)
                getFromGallery.type = "image/*"
                getFromGallery.putExtra(Intent.ACTION_GET_CONTENT, true)
                getFromGallery.type = MediaStore.Images.Media.CONTENT_TYPE
                startActivityForResult(
                    Intent.createChooser(getFromGallery, "Select Picture"),
                    PICTURE_REQUEST_CODE
                )
            }
            R.id.colorizeBtn -> startActivity(Intent(this, ColorPickActivity::class.java))

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        colorsRvMask = findViewById<SkeletonLayout>(R.id.colorRVMask)
        colorsRvMask = colorsRV.applySkeleton(R.layout.color_items_vertical, 6)

        viewModel = ViewModelProviders.of(this).get(ActivityModel::class.java)

        if (viewModel.getBitmaps().value != null) {
            photoBitmap = viewModel.getBitmaps().value!!
        }

        photoId = viewModel.photoId.value

        viewModel.getBitmaps().observe(this, androidx.lifecycle.Observer {
            if (viewModel.getBitmaps().value != null) {
                setRecyclerView(PaletteTool.getColorSet(viewModel.getBitmaps().value!!))
                setImageView(viewModel.getBitmaps().value!!)
            } else {
                getRandomPhoto()
            }
        })

        val readPermissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (readPermissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE
            )

        } else {
            if (photoBitmap == null) {
                getRandomPhoto()
            }
        }

        shareBtn.setOnClickListener(this)
        refreshBtn.setOnClickListener(this)
        openGallery.setOnClickListener(this)
        colorizeBtn.setOnClickListener(this)


    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetwork != null
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

        // network check
        if (!isNetworkAvailable(this)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("네트워크 없음").setMessage("이미지를 불러오기 위해 네트워크가 필요합니다.").setPositiveButton(
                "확인"
            ) { _, _ -> }
            val alertDialog = builder.create()
            alertDialog.show()

            return
        }

//        // Fade Animation
//        YoYo.with(Techniques.FadeOut)
//            .duration(500)
//            .repeat(0)
//            .playOn(colorsRV)
//
//        YoYo.with(Techniques.FadeOut)
//            .duration(500)
//            .repeat(0)
//            .playOn(imageView)


        // skeleton
        imageMask.showSkeleton()
        colorsRvMask.showSkeleton()


        // api call
        retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit!!.create(RandomImage::class.java)
        val call = service.getRandomPhoto()
        println("새 사진을 불러옴")

        GlobalScope.launch(Dispatchers.Default) {
            call.enqueue(object : Callback<RandomImageModel> {
                override fun onResponse(
                    call: Call<RandomImageModel>,
                    response: Response<RandomImageModel>
                ) {
                    if (response.isSuccessful) {

                        Log.d("이미지 로딩", "성공")

                        Glide.with(applicationContext).asBitmap()
                            .load(response.body()!!.urls!!.regular)
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

                                    colorsRvMask.showOriginal()
                                    imageMask.showOriginal()

                                    Log.d("MainActivity", "OnResourceReady() ")

                                    photoBitmap = resource

                                    viewModel.setBitmap(photoBitmap!!)
                                    setRecyclerView(PaletteTool.getColorSet(photoBitmap!!))

                                    return true
                                }
                            }).into(imageView)

                        photoId = response.body()!!.id


                    }
                }

                override fun onFailure(call: Call<RandomImageModel>, t: Throwable) {
                    Log.d("통신 실패 사유", t.message)
                    colorsRvMask.showOriginal()
                    imageMask.showOriginal()
                }


            })
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (photoBitmap != null) {
            viewModel.setBitmap(photoBitmap!!)
            viewModel.photoId.value = photoId
        }

    }

    fun getResizedBitmap(data: Uri): Bitmap? {
        val inputStream: InputStream? = contentResolver.openInputStream(data)
        val options = BitmapFactory.Options()
        options.inSampleSize = 4
        return BitmapFactory.decodeStream(inputStream, null, options)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    photoBitmap = getResizedBitmap(data.data!!)

                    viewModel.setBitmap(photoBitmap!!)
                    imageView.setImageBitmap(photoBitmap)
                    setRecyclerView(PaletteTool.getColorSet(viewModel.getBitmaps().value!!))
                } catch (err: Error) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("이미지가 너무 큽니다.").setMessage("기기가 처리할 수 없을 정도로 이미지가 큽니다.")
                    val alertDialog = builder.create()
                    alertDialog.show()
                }

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
        resize.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val colorArray = colorSet.toIntArray()

        val intent = Intent(this, ScreenshotActivity::class.java)
        intent.putExtra("photoId", photoId)
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

    private fun setImageView(bitmap: Bitmap) {
//        YoYo.with(Techniques.FadeIn)
//            .duration(500)
//            .repeat(0)
//            .playOn(imageView)

        if (bitmap.width > 1000 && bitmap.height > 1000) {
            Glide.with(this).load(bitmap).override(800, 800).into(imageView)
        }
        imageView.setImageBitmap(bitmap)
    }

    private fun setRecyclerView(set: Set<Int>) {
        val adapter = ColorAdapter(set.toList(), this)
        colorList = set.toIntArray()
        colorSet = set as MutableSet<Int>
        Log.d("색류", set.toString())
        colorsRV.layoutManager = LinearLayoutManager(this)
        colorsRV.adapter = adapter

        utilToolBar.setBackgroundColor(colorList!![0])

        // Fade Animation
        YoYo.with(Techniques.FadeIn)
            .duration(500)
            .repeat(0)
            .playOn(colorsRV)

        adapter.notifyDataSetChanged()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getRandomPhoto()
            } else {

            }
        }
    }


}
