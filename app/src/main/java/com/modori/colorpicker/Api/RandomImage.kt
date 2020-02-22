package com.modori.colorpicker.Api

import com.modori.colorpicker.R
import com.modori.colorpicker.model.RandomImageModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface RandomImage {

    @Headers("Authorization: Client-ID UNSPLASH_KEY")
    @GET("/photos/random")
    fun getRandomPhoto() : Call<RandomImageModel>

    @GET("/photos/{id}/")
    fun getPhotoById(@Path("id") photoId:String) :Call<RandomImageModel>
}