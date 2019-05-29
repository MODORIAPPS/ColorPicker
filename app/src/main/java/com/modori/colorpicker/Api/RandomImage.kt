package com.modori.colorpicker.Api

import com.modori.colorpicker.Model.RandomImageModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface RandomImage {

    @Headers("Authorization: Client-ID 790a2bb347b11e1167cad7a85d7c01c2fc3ccd781b4265a5f7fabae767ed0b38")
    @GET("photos/random")
    fun getRandomPhoto() : Call<RandomImageModel>

    @Headers("Authorization: Client-ID 790a2bb347b11e1167cad7a85d7c01c2fc3ccd781b4265a5f7fabae767ed0b38")
    @GET("photos/{id}/")
    fun getPhotoById(@Path("id") photoId:String) :Call<RandomImageModel>
}