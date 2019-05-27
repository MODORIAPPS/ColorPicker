package com.modori.colorpicker.Api

import com.modori.colorpicker.BuildConfig
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Headers

class RetrofitCreator {
    companion object{

        private const val API_BASE_URL = "https://api.unsplash.com/"

        private fun defaultRetrofit():Retrofit{
            return Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                //.client(createOkHttpClient())
                .build()
        }

        fun<T>create(service:Class<T>):T{
            return defaultRetrofit().create(service)
        }

        private fun createOkHttpClient():OkHttpClient{
            val interceptor = HttpLoggingInterceptor()
//            if(BuildConfig.DEBUG){
//                interceptor.level = HttpLoggingInterceptor.Level.BODY
//            }else{
//                interceptor.level = HttpLoggingInterceptor.Level.NONE
//            }

            interceptor.level = HttpLoggingInterceptor.Level.BODY

            return OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .build()


        }
    }
}