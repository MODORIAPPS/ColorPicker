package com.modori.colorpicker.model

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ActivityModel : ViewModel() {


    private val _bitmapStore = MutableLiveData<Bitmap>()


    fun setBitmap(bitmap: Bitmap) {
        Log.d("ActivityModel", "Bitmap이 ViewModel에 저장됨")
        _bitmapStore.value = bitmap
    }


    fun getBitmaps(): MutableLiveData<Bitmap> {
        Log.d("ActivityModel", "ViewModel 이 Bitmap을 반환함")
        return _bitmapStore
    }


    override fun onCleared() {
        super.onCleared()
    }
}