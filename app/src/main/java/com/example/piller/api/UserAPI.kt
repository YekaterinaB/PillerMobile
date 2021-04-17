package com.example.piller.api

import com.example.piller.models.User
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*


interface UserAPI {
    @Headers("Content-Type: application/json")

    @POST("user/register")
    fun registerUser(@Body user: User): Call<ResponseBody>

    @POST("user/authenticate")
    fun loginUser(@Body user: User): Call<ResponseBody>

    @DELETE("user/{email}")
    fun deleteUser(@Path("email") email: String): Call<ResponseBody>

    @PUT("user/{userId}")
    fun updateUserEmail(@Path("userId") userId: String, @Body user: User): Call<ResponseBody>

    @PUT("user/updatePassword/{userId}")
    fun updatePassword(@Path("userId") userId: String, @Body user: JSONObject): Call<ResponseBody>

    @GET("user/resetPassword/{email}")
    fun resetPassword(@Path("email") email: String): Call<ResponseBody>
}