package com.modori.colorpicker.api

import com.modori.colorpicker.model.RandomImageModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface RandomImage {

    @Headers("Authorization: Client-ID fKrKSqr7ZTzT2aq5tVaHe5PzA1LVf9iY36M6uXDeBfk")
    @GET("/photos/random")
    fun getRandomPhoto() : Call<RandomImageModel>

    @GET("/photos/{id}/")
    fun getPhotoById(@Path("id") photoId:String) :Call<RandomImageModel>
}