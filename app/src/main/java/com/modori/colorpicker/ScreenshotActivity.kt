package com.modori.colorpicker

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.modori.colorpicker.api.RandomImage
import com.modori.colorpicker.model.RandomImageModel
import com.modori.colorpicker.utils.Screenshot
import kotlinx.android.synthetic.main.activity_screenshot.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ScreenshotActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var photoBitmap: Bitmap
    lateinit var colorSet: MutableSet<Int>

    lateinit var mColorList: IntArray
    var rgbList: ArrayList<String> = ArrayList()
    var stringColors = "33"
    var photoId: String? = null

    private val MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.saveImage -> {
                if (permissionCheck()) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("사진을 색깔과 함께 저장합니다.")
                    builder.setMessage("사진은 내장메모리의 Pictures 폴더안에 저장됩니다.")
                    builder.setPositiveButton("확인") { _, _ ->
                        val bitmap: Bitmap = Screenshot.takescreenshot(rootView_sc)
                        storeScreenShot(bitmap, photoId + "_CAP" + ".jpg")

                    }.setNegativeButton("취소") { _, _ -> }

                    builder.show()
                }

            }

            R.id.copyImage -> {
                val bitmap: Bitmap = Screenshot.takescreenshot(rootView_sc)
                copyToClipBoard(bitmap)
            }

            R.id.backBtn -> {
                finish()
            }

            //            R.id.shareImage -> {
//                val intent = Intent(Intent.ACTION_SEND)
//                intent.type = "text/plain"
//
//                intent.putExtra(Intent.EXTRA_TEXT, stringColors)
//
//                val chooser: Intent = Intent.createChooser(intent, "색 공유하기")
//                startActivity(chooser)
//            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshot)

        val view1: View = findViewById(R.id.view1)
        val view2: View = findViewById(R.id.view2)
        val view3: View = findViewById(R.id.view3)
        val view4: View = findViewById(R.id.view4)
        val view5: View = findViewById(R.id.view5)
        val view6: View = findViewById(R.id.view6)
        val view7: View = findViewById(R.id.view7)

        backBtn.setOnClickListener(this)
        copyImage.setOnClickListener(this)
        saveImage.setOnClickListener(this)


        val colorViews: Array<View> = arrayOf(view1, view2, view3, view4, view5, view6, view7)
        val colorList: IntArray

        val extras: Bundle = intent.extras!!
        val byteArray = extras.getByteArray("image")
        val colorArray = extras.getIntArray("color")
        photoId = extras.getString("photoId")
        val photoBitmap: Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        Glide.with(this).load(photoBitmap).override(800, 800).into(imageView_sc)

        colorList = colorArray!!
        println(colorList)


        var index = 0
        for (item in colorList) {
            colorViews[index].setBackgroundColor(item)
            index++
        }

        for (i in index until colorViews.size) {
            colorViews[i].visibility = View.GONE
        }

        val stringColor: ArrayList<String> = arrayListOf()

        for (item in colorList) {
            stringColor.add(String.format("#%06X", (0xFFFFFF and item)))

        }


        stringColors = stringColor.toString()

        for (item in stringColor) {
            val color: Int = Color.parseColor(item)
            rgbList.add("RGB( ${color.red} , ${color.green} , ${color.blue} )")
        }
        screenshot_toolbar.setBackgroundColor(colorList[0])
        colorListView.text = stringColors
        colorRGBView.text = rgbList.toString()


        permissionCheck()
    }

    private fun notifyForGallery(file: File) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // or image/png
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }


    private fun getPhotoById(mPhotoId: String) {

        Log.d("getPhotoByID", mPhotoId)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(RandomImage::class.java)
        val call = service.getPhotoById(mPhotoId)

        call.enqueue(object : Callback<RandomImageModel> {
            override fun onFailure(call: Call<RandomImageModel>, t: Throwable) {
                Log.d("IDsearch 실패", t.message)
            }

            override fun onResponse(
                call: Call<RandomImageModel>,
                response: Response<RandomImageModel>
            ) {
                Log.d("받아온 값", response.body().toString())
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
                            photoId = response.body()!!.id
                            imageView_sc.setImageBitmap(resource)
                            return true
                        }
                    }).into(imageView_sc)
            }
        })

    }

//    private fun shareImage(bitmap: Bitmap){
//        try{
//            val intent:Intent = Intent(Intent.ACTION_SEND)
//            intent.type = "image/*"
//            intent.putExtra(Intent.EXTRA_STREAM, getImageUri(bitmap))
//            intent.setPackage("com.kakao.talk")
//            startActivityForResult(intent,REQUEST_IMG_SEND)
//        }catch (e:ActivityNotFoundException){
//            val  uriMarket:Uri = Uri.parse("market://deatils?id=com.kakao.talk")
//            val intent = Intent(Intent.ACTION_VIEW, uriMarket)
//            startActivity(intent)
//        }
//
//
//
//    }

    private fun permissionCheck(): Boolean {
        val ReadpermissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (ReadpermissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE
            )

            return false

        } else {
            return true
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {

            }
        }
    }

    private fun storeScreenShot(bitmap: Bitmap, filename: String) {
        var out: OutputStream? = null
        //val dir = File(Environment.getExternalStorageDirectory().toString() + "/ColorPicker/")
        //dir.mkdir()

        val dir = File(Environment.getExternalStorageDirectory().absolutePath + "/ColorPicker/")
        dir.mkdir()
        val imageFile = File(dir, filename)

        out = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()

        out.close()
        Toast.makeText(this, "스크린 샷을 저장했습니다.", Toast.LENGTH_SHORT).show()

        //this.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, getImageUri(bitmap)))
        notifyForGallery(dir)

    }

    private fun copyToClipBoard(imageBitmap: Bitmap) {
        val clipBoard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData: ClipData = ClipData.newUri(contentResolver, "Image", getImageUri(imageBitmap))
        clipBoard.setPrimaryClip(clipData)

        Toast.makeText(this, getString(R.string.Screenshot_copyedToast), Toast.LENGTH_SHORT).show()
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String =
            MediaStore.Images.Media.insertImage(this.contentResolver, bitmap, "title", null)
        return Uri.parse(path)
    }

    override fun onDestroy() {
        super.onDestroy()


    }


}
