package com.modori.colorpicker

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.transition.Visibility
import android.util.Log
import android.view.View
import android.view.View.SCROLL_INDICATOR_END
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import butterknife.BindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.modori.colorpicker.Api.RandomImage
import com.modori.colorpicker.Model.RandomImageModel
import com.modori.colorpicker.Utils.Screenshot
import kotlinx.android.synthetic.main.activity_main.*
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

    var photoBitmap: Bitmap? = null
    var mColorList:IntArray? = null
    var stringColors = "33"
    var photoId:String = "eee"

    private val MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshot)

        permissionCheck()

        val view1: View = findViewById(R.id.view1)
        val view2: View = findViewById(R.id.view2)
        val view3: View = findViewById(R.id.view3)
        val view4: View = findViewById(R.id.view4)
        val view5: View = findViewById(R.id.view5)
        val view6: View = findViewById(R.id.view6)
        val view7: View = findViewById(R.id.view7)


        val colorViews: Array<View> = arrayOf(view1, view2, view3, view4, view5, view6, view7)

        if (intent.hasExtra("photoId")) {
            getPhotoById(intent.getStringExtra("photoId"))
            val colorList: IntArray = intent.getIntArrayExtra("colorList")
            mColorList = intent.getIntArrayExtra("colorList")

            var index: Int = 0
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

            colorListView.text = stringColors


        }


        shareBtn_sc.setOnClickListener(this)
        infoBtn.setOnClickListener(this)
        copyBtn.setOnClickListener(this)
        captureBtn.setOnClickListener(this)



    }

    override fun onClick(v: View?) {

        if(photoId != "eee"){
            when(v?.id){


                R.id.shareBtn_sc -> {
                    val intent:Intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"

                    intent.putExtra(Intent.EXTRA_TEXT,stringColors)

                    val chooser:Intent = Intent.createChooser(intent,"색 공유하기")
                    startActivity(chooser)
                }
                R.id.infoBtn ->{}
                R.id.copyBtn -> {
                    val bitmap: Bitmap = Screenshot.takescreenshot(rootView_sc)
                    copyToClipBoard(bitmap)
                }
                R.id.captureBtn -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("사진을 색깔과 함께 저장합니다.")
                    builder.setMessage("사진은 내장메모리의 ColorPicker 폴더안에 저장됩니다.")
                    builder.setPositiveButton("확인") { _, _ ->
                        val bitmap: Bitmap = Screenshot.takescreenshot(rootView_sc)
                        storeScreenShot(bitmap, intent.getStringExtra("photoId") + "_CAP" + ".jpg")

                    }.setNegativeButton("취소") { _, _ -> }

                    builder.show()
                }
            }
        }else{
            Toast.makeText(this,"사진을 받아오고 있습니다.", Toast.LENGTH_SHORT).show()

        }

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

            override fun onResponse(call: Call<RandomImageModel>, response: Response<RandomImageModel>) {
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

    private fun permissionCheck() {
        val ReadpermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (ReadpermissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE
            )

        } else {

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

    private fun storeScreenShot(bitmap: Bitmap, filename: String) {
        var out: OutputStream? = null
        val dir = File(Environment.getExternalStorageDirectory().toString() + "/ColorPicker/")
        dir.mkdir()

        val imageFile: File = File(dir, filename)


        out = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()

        out.close()
        Toast.makeText(this, "스크린 샷을 저장했습니다.", Toast.LENGTH_SHORT).show()

    }

    private fun copyToClipBoard(imageBitmap: Bitmap) {
        val clipBoard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData: ClipData = ClipData.newUri(contentResolver, "Image", getImageUri(imageBitmap))
        clipBoard.primaryClip = clipData

        Toast.makeText(this, "이미지를 클립보드에 복사하였습니다", Toast.LENGTH_SHORT).show()
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(this.contentResolver, bitmap, "title", null)
        return Uri.parse(path)
    }

}
