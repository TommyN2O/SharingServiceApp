package com.example.sharingserviceapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApiServiceInstance {
    companion object {
//        const val BASE_URL = "http://192.168.56.1:3001/"
        const val BASE_URL = "http://10.0.2.2:3001/"
        const val BASE_API_URL = BASE_URL + "api/"
    }

    object Auth {
        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiServices: ApiService = retrofit.create(ApiService::class.java)
    }
}