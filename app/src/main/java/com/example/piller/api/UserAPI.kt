package com.example.piller.api

import com.example.piller.models.User
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers


interface UserAPI {
    @Headers("Content-Type: application/json")

    @POST("user/register")
    fun registerUser(@Body user: User): Call<ResponseBody>

    @POST("user/authenticate")
    fun loginUser(@Body user: User): Call<ResponseBody>

}