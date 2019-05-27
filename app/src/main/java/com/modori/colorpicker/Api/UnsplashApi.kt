package com.modori.colorpicker.Api

import com.modori.colorpicker.Model.RandomImageModel
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import java.util.*

class  UnsplashApi{
    interface UnsplashApiImpl{
        @Headers("Authorization: Client-ID 790a2bb347b11e1167cad7a85d7c01c2fc3ccd781b4265a5f7fabae767ed0b38")

        @GET("photos/random")
        fun getRandomPhoto(): Observable<RandomImageModel>
    }


    companion object{
        fun getRandomPhoto():Observable<RandomImageModel>{
            return RetrofitCreator.create(UnsplashApiImpl::class.java).getRandomPhoto()
        }
    }
}